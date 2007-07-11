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
	private String aixUsername, aixPassword, userID;
	public static String lastUsername = Config.getLastUsername() == null ? "" : Config.getLastUsername();
	public static String lastPassword = (RepDevMain.DEVELOPER && Config.getLastPassword() != null) ? Config.getLastPassword() : "";
	public static String lastUserID = (RepDevMain.DEVELOPER && Config.getLastUserID() != null) ? Config.getLastUserID() : "";
	
	private void create(Shell parent, int inSym) {
		result = -1;

		FormLayout layout = new FormLayout();
		layout.marginTop = 5;
		layout.marginBottom = 5;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.spacing = 5;

		shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		shell.setText("Sym Login");
		shell.setLayout(layout);

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

				aixUsername = aixUserText.getText().trim();
				aixPassword = aixPasswordText.getText().trim();
				userID = userIDText.getText().trim();

				lastUsername = aixUsername;
				lastPassword = aixPassword;
				lastUserID = userID;
				
				Config.setLastPassword(lastPassword);
				Config.setLastUsername(lastUsername);
				Config.setLastUserID(lastUserID);

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
		symLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(symLabel);
		data.top = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.width = 160;
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
			if (Config.getServer().equals("test"))
				session = new TestingSymitarSession();
			else
				session = new DirectSymitarSession();

			RepDevMain.SYMITAR_SESSIONS.put(sym, session);
		}

		if (session.isConnected()) {
			me.result = -1;
			return;
		}

		SessionError error = session.connect(Config.getServer(), aixUsername, aixPassword, sym, userID);
		
		if (error == SessionError.NONE){
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
