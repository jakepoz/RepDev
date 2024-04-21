/**
 *  RepDev - RepGen IDE for Symitar
 *  Copyright (C) 2007  Jake Poznanski, Ryan Schultz, Sean Delaney
 *  http://repdev.org/ <support@repdev.org>
 *
 *  This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.repdev;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Logon to symitar shell
 * @author Jake Poznanski
 *
 */
public class SymLoginShell {
	private static SymLoginShell me = new SymLoginShell();
	private Shell shell;

	private SymLoginShell() {
	}

	private int LABEL_WIDTH = 80;
	private int result = -1;
	private int sym;
	private String aixUsername, aixPassword, userID, server;
	public static String lastServer = "";
	public static String lastUsername = Config.getLastUsername() == null ? "" : Config.getLastUsername();
	public static String lastPassword = (RepDevMain.DEVELOPER && Config.getLastPassword() != null) ? Config.getLastPassword() : "";
	public static String lastUserID = (RepDevMain.DEVELOPER && Config.getLastUserID() != null) ? Config.getLastUserID() : "";
	
	private void create(Shell parent, int inSym) {
		result = -1;
		lastServer = Config.getServer() == null ? "" : Config.getServer();
		if (RepDev_SSO.isLoggedIn()) {
			lastUsername = "";
			lastPassword = "";
			lastUserID = "";
		}

		//TODO: init encrypted password here -- if (inSym != -1)
		if (inSym != -1) {
			if (!RepDevMain.SESSION_INFO.get(inSym).getServer().equals("")) lastServer = RepDevMain.SESSION_INFO.get(inSym).getServer();
			
			if (RepDevMain.MASTER_PASSWORD_HASH != null && RepDevMain.MASTER_PASSWORD_HASH.length > 0) {
				if (!RepDevMain.SESSION_INFO.get(inSym).getAixUserName().equals("")){
					lastUsername = RepDevMain.SESSION_INFO.get(inSym).getAixUserName();
					lastPassword = RepDev_SSO.decrypt(RepDevMain.MASTER_PASSWORD_HASH, RepDevMain.SESSION_INFO.get(inSym).getAixPassword());
					lastUserID = RepDev_SSO.decrypt(RepDevMain.MASTER_PASSWORD_HASH, RepDevMain.SESSION_INFO.get(inSym).getUserID());
				}
			}
		}
		
		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;

		shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		shell.setText("Sym Login");
		shell.setLayout(layout);
		shell.setImage(RepDevMain.smallSymAddImage);

		Label serverLabel = new Label(shell, SWT.NONE);
		serverLabel.setText("Server:");

		Label symLabel = new Label(shell, SWT.NONE);
		symLabel.setText("Sym:");

		Label aixUserLabel = new Label(shell, SWT.NONE);
		aixUserLabel.setText("AIX Username:");

		Label aixPasswordLabel = new Label(shell, SWT.NONE);
		aixPasswordLabel.setText("AIX Password:");

		Label userIDLabel = new Label(shell, SWT.NONE);
		userIDLabel.setText("UserID:");

		final Text symText = new Text(shell, SWT.BORDER | (inSym != -1 ? SWT.READ_ONLY : SWT.NONE));

		if (inSym != -1)
			symText.setText(String.valueOf(inSym));
		/*try {
			sym = Integer.parseInt(symText.getText().trim());
			if (RepDevMain.SESSION_INFO.get(sym).getServer() != null) {
				if(RepDevMain.SESSION_INFO.get(sym).getServer().length() != 0) {
					lastServer = RepDevMain.SESSION_INFO.get(sym).getServer();
				}
			}
		} catch (Exception ex) {
			
		}*/
		
		final Text serverText = new Text(shell, SWT.BORDER);
		serverText.setText(lastServer);

		final Text aixUserText = new Text(shell, SWT.BORDER);
		aixUserText.setText(lastUsername);

		final Text aixPasswordText = new Text(shell, SWT.BORDER | SWT.PASSWORD);
		aixPasswordText.setText(lastPassword);

		final Text userIDText = new Text(shell, SWT.BORDER | SWT.PASSWORD);
		userIDText.setText(lastUserID);
		
		Button ok = new Button(shell, SWT.PUSH);
		ok.setText("Login");
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				try {
					sym = Integer.parseInt(symText.getText().trim());
				} catch (Exception ex) {
					MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					dialog.setMessage("The Sym number you entered was invalid");
					dialog.setText("Input Error");
					dialog.open();
					symText.setFocus();
					return;
				}
				//TODO: look for anything interesting here
				server = serverText.getText().trim();
				aixUsername = aixUserText.getText().trim();
				aixPassword = aixPasswordText.getText().trim();
				userID = userIDText.getText().trim();

				if (!RepDev_SSO.isLoggedIn()) {
					lastUsername = aixUsername;
					lastPassword = aixPassword;
					lastUserID = userID;
					
					Config.setLastPassword(lastPassword);
					Config.setLastUsername(lastUsername);
					Config.setLastUserID(lastUserID);
				}
				result = 1000;

				shell.dispose();
			}
		});

		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = -1;
				shell.dispose();
			}
		});
		
		if( symText.isEnabled() && symText.getText().trim().equals(""))
			symText.setFocus();
		else if( aixUserText.getText().trim().equals("") )
			aixUserText.setFocus();
		else if ( aixPasswordText.getText().trim().equals("") )
			aixPasswordText.setFocus();
		else if( userIDText.getText().trim().equals("") )
			userIDText.setFocus();

		FormData data;

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		data.width = LABEL_WIDTH;
		serverLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(serverLabel);
		data.top = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.width = 160;
		serverText.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(serverText);
		data.width = LABEL_WIDTH;
		symLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(symLabel);
		data.top = new FormAttachment(serverText);
		data.right = new FormAttachment(100);
		symText.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(symText);
		data.width = LABEL_WIDTH;
		aixUserLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(aixUserLabel);
		data.top = new FormAttachment(symText);
		data.right = new FormAttachment(100);
		aixUserText.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(aixUserText);
		data.width = LABEL_WIDTH;
		aixPasswordLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(aixPasswordLabel);
		data.top = new FormAttachment(aixUserText);
		data.right = new FormAttachment(100);
		aixPasswordText.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(aixPasswordText);
		data.width = LABEL_WIDTH;
		userIDLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(userIDLabel);
		data.top = new FormAttachment(aixPasswordText);
		data.right = new FormAttachment(100);
		userIDText.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(userIDText);
		data.right = new FormAttachment(100);
		cancel.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(userIDText);
		data.right = new FormAttachment(cancel);
		ok.setLayoutData(data);

		shell.setDefaultButton(ok);
		shell.pack();
		shell.open();
	}

	private void symLogin() {
		SymitarSession session = RepDevMain.SYMITAR_SESSIONS.get(sym);

		if (session == null) {
			if( Config.getServer().equalsIgnoreCase("testsession")) //Allows for a testing mode when no symitar server's are available
				session = new TestingSymitarSession();
			else
				session = new DirectSymitarSession();

			RepDevMain.SYMITAR_SESSIONS.put(sym, session);
			RepDevMain.SESSION_INFO.put(sym, new SessionInfo("", "", "", "", ""));
		}

		if (session.isConnected()) {
			me.result = -1;
			return;
		}
		
		((DirectSymitarSession)session).enableKeepAlive(Config.getNeverTerminate(), Config.getTerminateHour(),Config.getTerminateMinute());
		SessionError error = session.connect(server, Config.getPort(), aixUsername, aixPassword, sym, userID);
		int retry=0;
		while(error == SessionError.USERID_INVALID){
			retry++;
			if(retry==4){
				MessageBox diag=new MessageBox(new Shell(),SWT.OK | SWT.ICON_WARNING);
				diag.setText("Password Retry");
				diag.setMessage("You have one last try before getting locked out ! ! !");
				diag.open();
			}
			String pass = FailedLogonShell.checkPass();
			//Config.setLastUserID(pass);
			lastUserID = pass;
			userID = pass;
			error = session.loginUser(pass);
		}
		if (error == SessionError.NONE){
			if(session.getSym()!=((DirectSymitarSession)session).getActualSym()){
				MessageBox diag=new MessageBox(new Shell(),SWT.OK | SWT.ICON_WARNING);
				diag.setText("SYM Inconsistent");
				diag.setMessage("WARNING: You specified SYM " + session.getSym() + " during login, but you are actually logged into SYM " + ((DirectSymitarSession)session).getActualSym());
				diag.open();
			}
			//TODO: encrypt and stuff passwords here
			RepDevMain.SESSION_INFO.get(sym).setServer(server);
			if (RepDev_SSO.isLoggedIn()) {
				RepDevMain.SESSION_INFO.get(sym).setCredential(aixUsername, RepDev_SSO.encrypt(RepDevMain.MASTER_PASSWORD_HASH, aixPassword), RepDev_SSO.encrypt(RepDevMain.MASTER_PASSWORD_HASH, userID));
			}
			me.result = sym;
			return;
		}
		else{
			me.result = -1;
			error.showError();
		}
	}

	// returns -1 on cancel
	// Pass -1 to sym to hide sym field
	public static int symLogin(Display display, Shell parent, int sym) {
		me.create(parent, sym);

		while (!me.shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		if (me.result != -1)
			me.symLogin();

		return me.result;
	}
}
