package com.repdev.tests;

import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.repdev.Config;
import com.repdev.MainShell;
import com.repdev.RepDevMain;

/**
 * Tests some basic UI functions
 * 
 * TODO: Totally not done yet, at all
 * @author Jake Poznanski
 *
 */
public class MainUITest {
	Display display;
	MainShell mainShell;
	
	Thread main = new Thread(new Runnable(){

		public void run() {
			try {
				RepDevMain.main(new String[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	});
	
	@Before
	public void createUIThread() throws InterruptedException{
		main.start();
		
		//Wait for everything to init in the UI thread
		while(RepDevMain.mainShell == null || RepDevMain.mainShell.getShell() == null || RepDevMain.mainShell.getShell().getDisplay() == null){
			Thread.sleep(100);
		}
		
		display = RepDevMain.mainShell.getShell().getDisplay();
		mainShell = RepDevMain.mainShell;
	}
	
	@After
	public void disposeUIThread(){
		display.syncExec(new Runnable(){

			public void run() {
				display.dispose();
			}
			
		});
	}
	
	@Test
	public void developerMode() throws InterruptedException{
		Assert.assertTrue(RepDevMain.DEVELOPER);
		Assert.assertNotNull(Config.getLastUsername());
		Assert.assertNotSame("",Config.getLastUsername());
		
		Assert.assertNotNull(Config.getLastPassword());
		Assert.assertNotSame("",Config.getLastPassword());
	}
	
	@Test
	public void mountDirectory(){
		mainShell.showOptions();
	}
}
