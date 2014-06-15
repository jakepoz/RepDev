

package com.repdev;

/**
 * Given the buffer content of the EASE Menu Selection, and the SYM
 * number, getEaseSelection will return the menu selection for the
 * SYM.  This could be modified by each individual Credit Unions to
 * fit their menu structure.
 * 
 * @author Brucec Chang
 */
public class EaseSelection {

	public static int getEASESelection(String strBuffer, int sym)
	{
		int start, end, tmpInt;
		String tmpSelection;

		// Find the SYM in the Menu
		end = strBuffer.indexOf("- SYM "+String.format("%03d", sym)) - 1;
		if(end != -2)
		{
			System.out.println("EASE: SYM selection found in menu.");
		}
		
		// Find the start of the line preceding the SYM selection
		start=strBuffer.lastIndexOf("\n", end);
		if(start == -1)
		{
			System.out.println("EASE: Unexpected Error - Could not find the begining of the line.");
			return -1;
		}
		
		// Get the selection number for that SYM
		tmpSelection = strBuffer.substring(start, end+1).trim();
		try
		{
			// Convert the selection to a number
			tmpInt = Integer.parseInt(tmpSelection);
			return tmpInt;
		}
		catch (NumberFormatException e)
		{
			System.out.println("EASE: Selection is not a number.");
			return -1;
		}
	}
}
