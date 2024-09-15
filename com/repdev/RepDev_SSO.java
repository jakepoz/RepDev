package com.repdev;

import java.security.*;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class RepDev_SSO {

	private static final int KEYLEN = 256;
	private static final int ITERATIONS = 58189;
	private static final String SALT = "RepDev";
	private static final String TITLE = "Single Sign-On";

	/* public static void main(String[] args) {

		// Define your secret key and salt (keep these secure and don't hardcode in
		// production)
		String secretKey = "MySecretKey";
		byte[] passHash = md5Hash(secretKey);
		
		// String to be encrypted
		String originalString = "Hello, this is a secret message.";

		// Encrypt the string
		String encryptedString = AES_256.encrypt(passHash, originalString);
		if (encryptedString != null) {
			System.out.println("Encrypted: " + encryptedString);
		} else {
			System.err.println("Encryption failed.");
			return;
		}

		// Decrypt the string
		String decryptedString = AES_256.decrypt(passHash, encryptedString);
		if (decryptedString != null) {
			System.out.println("Decrypted: " + decryptedString);
		} else {
			System.err.println("Decryption failed.");
		}
	} */

	// String encryptedString = AES_256.encrypt(AES_256.md5Hash(secretKey), originalString);
	// String decryptedString = AES_256.decrypt(md5Hash(secretKey), encryptedString);
	public static String encrypt(byte[] masterPassword, String plainText) {

		try {
			SecureRandom secureRandom = new SecureRandom();
			byte[] iv = new byte[16];
			secureRandom.nextBytes(iv);
			IvParameterSpec ivspec = new IvParameterSpec(iv);

			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec ks = new PBEKeySpec(bytesToChar(masterPassword), SALT.getBytes(), ITERATIONS, KEYLEN);
			SecretKey sk = skf.generateSecret(ks);
			SecretKeySpec sks = new SecretKeySpec(sk.getEncoded(), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, sks, ivspec);

			plainText = "repdev" + plainText;
			byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));
			byte[] encryptedData = new byte[iv.length + cipherText.length];
			System.arraycopy(iv, 0, encryptedData, 0, iv.length);
			System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length);

			return Base64.getEncoder().encodeToString(encryptedData);
		} catch (Exception e) {
			return "";
		}
	}

	public static String decrypt(byte[] masterPassword, String encryptedText) {

		try {
			byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
			byte[] iv = new byte[16];
			System.arraycopy(encryptedBytes, 0, iv, 0, iv.length);
			IvParameterSpec ivspec = new IvParameterSpec(iv);

			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec ks = new PBEKeySpec(bytesToChar(masterPassword), SALT.getBytes(), ITERATIONS, KEYLEN);
			SecretKey sk = skf.generateSecret(ks);
			SecretKeySpec sks = new SecretKeySpec(sk.getEncoded(), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, sks, ivspec);

			byte[] cipherText = new byte[encryptedBytes.length - 16];
			System.arraycopy(encryptedBytes, 16, cipherText, 0, cipherText.length);

			byte[] decryptedBytes = cipher.doFinal(cipherText);
			String decryptedText = new String(decryptedBytes, "UTF-8");
			if (decryptedText.length() > 0 && decryptedText.substring(0, 6).equals("repdev")) {
				return decryptedText.substring(6);
			} else {
				return "";
			}
		} catch (Exception e) {
			return "";
		}
	}

	public static byte[] md5Hash(String string) {
		try {
			byte[] bytesOfMessage = string.getBytes("UTF-8");

			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] theMD5digest = md.digest(bytesOfMessage);

			return theMD5digest;
		} catch (Exception e) {
			return null;
		}
	}

	public static char[] bytesToChar(byte[] bytes) {
		char[] buffer = new char[bytes.length >> 1];
		for (int i = 0; i < buffer.length; i++) {
			int bpos = i << 1;
			char c = (char) (((bytes[bpos] & 0x00FF) << 8) + (bytes[bpos + 1] & 0x00FF));
			buffer[i] = c;
		}
		return buffer;
	}
	
	public static void changeServerPasswords(Shell shell) {
		String msg = "This will change all of the saved AIX Password for a server.     \n\nEnter the server name\n\n";
		String server = InputShell.getInput(shell, TITLE, msg, "Symitar", false);
		if (server != null && !server.contentEquals("")) {
			String syms = "";
			int count = 0;
			
			for (int sym : RepDevMain.SESSION_INFO.keySet()) {
				if (RepDevMain.SESSION_INFO.get(sym).getServer().toLowerCase().contentEquals(server.toLowerCase())) {
					count++;
					syms += "SYM" + sym + "\n";
				}
			}
			
			if (count>0) {
				msg = "There are "+count+" SYM(s) using this server\n" + syms + "\nPlease enter the new AIX Password.\n\n";
				String aixPass = getPassword(shell, msg, false);
				if (aixPass != null && !aixPass.contentEquals("")) {
					String encPass = encrypt(RepDevMain.MASTER_PASSWORD_HASH, aixPass);
					for (int sym : RepDevMain.SESSION_INFO.keySet()) {
						if (RepDevMain.SESSION_INFO.get(sym).getServer().toLowerCase().contentEquals(server.toLowerCase())) {
							RepDevMain.SESSION_INFO.get(sym).setAixPassword(encPass);
						}
					}
					
					MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
					dialog.setText(TITLE);
					dialog.setMessage("The AIX Password was updated for the following SYM(s);\n" + syms);
					dialog.open();
				} else {
					MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
					dialog.setText(TITLE);
					dialog.setMessage("New AIX Password was not specified.");
					dialog.open();
				}
			} else {
				MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				dialog.setText(TITLE);
				dialog.setMessage("The server is not used by any SYMs.");
				dialog.open();
			}
			
		} else {
			MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			dialog.setText(TITLE);
			dialog.setMessage("Server name was not entered.");
			dialog.open();
		}
	}
	
	public static String getRepDevPassword(Shell shell) {
		String msg = "A RepDev password is required to encrypt your saved     \npasswords.  Please create a RepDev Password.     \n\nMinimum: 8 characters, 1 upper, 1 lower,     \n1 number, 1 special character\n\n";
		String pass1 = getPassword(shell, msg, true);
		
		return encrypt(md5Hash(pass1), "RepDev");
	}
	
	static String getPassword(Shell shell, String prompt, boolean minPassReq) {
		boolean bDone = false;
		
		while (!bDone){
			boolean bError = false;
			
			String pass1 = InputShell.getInput(shell, TITLE, prompt, "", true);
			if (pass1 == null) {
				MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				dialog.setText(TITLE);
				dialog.setMessage("Cancelled.");
				dialog.open();
				
				bError = true;
				bDone = true;
			} else if (pass1.contentEquals("")) {
				MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				dialog.setText(TITLE);
				dialog.setMessage("The passwords cannot be blank.  Please try again.");
				dialog.open();
				
				bError = true;
			} else if (!passMinRequirment(pass1) && minPassReq) {
				MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				dialog.setText(TITLE);
				dialog.setMessage("The passwords does not meet the minimum requirments.  Please try again.");
				dialog.open();
				
				bError = true;
			}
			
			if (!bError) {
				String pass2 = InputShell.getInput(shell, TITLE, "Reenter password.\n\n", "", true);
				if (pass1.equals(pass2) && !pass1.contentEquals("")) {
					return pass1;
				} else {
					MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
					dialog.setText(TITLE);
					dialog.setMessage("The passwords do not match.  Please try again.");
					dialog.open();
				}
			}
		}
		
		return "";
	}
	
	static boolean passMinRequirment(String pass) {
		int MINIMUM_LENGTH = 8;
		int MINIMUM_UPPER = 1;
		int MINIMUM_LOWER = 1;
		int MINIMUM_NUMBER = 1;
		int MINIMUM_SPECIAL = 1;
		
		int iUpper = 0;
		int iLower = 0;
		int iNumber = 0;
		int iSpecial = 0;
		
		String sUpper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String sLower = "abcdefghijklmnopqrstuvwxyz";
		String sNumber = "0123456789";
		String sSpecial = "`~!@#$%^&*()_+-= {}[]|\\:;'\",./<>?";
		
		if (pass.length() < MINIMUM_LENGTH) {
			return false;
		} else {
			for (int i = 0 ; i < pass.length() ; i++) {
				if (sUpper.indexOf(pass.substring(i, i + 1)) >= 0) iUpper++;
				if (sLower.indexOf(pass.substring(i, i + 1)) >= 0) iLower++;
				if (sNumber.indexOf(pass.substring(i, i + 1)) >= 0) iNumber++;
				if (sSpecial.indexOf(pass.substring(i, i + 1)) >= 0) iSpecial++;
			}
			System.out.println(iUpper+":"+iLower+":"+iNumber+":"+iSpecial);
			if (iUpper < MINIMUM_UPPER || iLower < MINIMUM_LOWER || iNumber < MINIMUM_NUMBER || iSpecial < MINIMUM_SPECIAL) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	public static void login(Shell shell) {
		boolean bDone = false;
		
		while (!bDone) {
			String pass1 = InputShell.getInput(shell, TITLE, "Please enter your RepDev Password.     \n\n", "", true);
			if (pass1 != null) {
				if (pass1.contentEquals("")) {
					MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
					dialog.setText(TITLE);
					dialog.setMessage("The passwords cannot be blank.  Please try again.");
					dialog.open();
				} else {
					String valText = decrypt(md5Hash(pass1), Config.getPasswordValidator());
					if (!valText.contentEquals("RepDev")) {
						MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
						dialog.setText(TITLE);
						dialog.setMessage("The passwords is incorrect.  Please try again.");
						dialog.open();
					} else {
						RepDevMain.MASTER_PASSWORD_HASH = md5Hash(pass1);
						
						bDone = true;
					}
				}
			} else {
				MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				dialog.setText(TITLE);
				dialog.setMessage("RepDev sign in cancelled");
				dialog.open();
				
				bDone = true;
			}
		}
	}
	
	public static boolean isLoggedIn() {
		if (RepDevMain.MASTER_PASSWORD_HASH != null && RepDevMain.MASTER_PASSWORD_HASH.length > 0) {
			return true;
		}
		
		return false;
	}
}
