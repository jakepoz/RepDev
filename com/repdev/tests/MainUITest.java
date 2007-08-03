package com.repdev.tests;

import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;

import com.repdev.RepDevMain;

/**
 * Tests some basic UI functions
 * @author Jake Poznanski
 *
 */
public class MainUITest {
	RepDevMain repDevMain;
	
	@Test
	public void testUI() throws InterruptedException{
		Thread main = new Thread(new Runnable(){

			public void run() {
				try {
					RepDevMain.main(new String[1]);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		
		main.start();
		
		//Wait for everything to init in the UI thread
		while(RepDevMain.mainShell == null || RepDevMain.mainShell.getShell() == null || RepDevMain.mainShell.getShell().getDisplay() == null){
			Thread.sleep(100);
		}
		
		
	}
}
