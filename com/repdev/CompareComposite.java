package com.repdev;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;



import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Slider;

import com.repdev.compare.LineRangeComparator;
import com.repdev.compare.RangeDifference;
import com.repdev.compare.RangeDifferencer;
import com.repdev.parser.RepgenParser;

public class CompareComposite extends Composite implements TabView{
	private SymitarFile left, right;
	private StyledText leftTxt, rightTxt;
	private SyntaxHighlighter leftHighlighter, rightHighlighter;
	private RepgenParser leftParser, rightParser;
	private Composite parent;
	private Canvas center;
	private Slider slider;
	private double[] fBasicCenterCurve;
	private RangeDifference[] diffs;
	private boolean fInScrolling;
	
	public CompareComposite(Composite parent, CTabItem tabItem, SymitarFile leftFile, SymitarFile rightFile){
		super(parent,SWT.NONE);
		this.left = leftFile;
		this.right = rightFile;
		this.parent = parent;
		
		buildGUI();
	}

	private void buildGUI() {
		GridLayout layout = new GridLayout();
		layout.numColumns =6;
		setLayout(layout);
		
		final Label imageLeft = new Label(this,SWT.NONE);
		imageLeft.setImage(RepDevMain.mainShell.getFileImage(left));
		
		Label nameLeft = new Label(this,SWT.NONE);	
		nameLeft.setText(left.getName());
		nameLeft.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		
		Label sep1 = new Label(this,SWT.NONE);
		sep1.setLayoutData(new GridData(SWT.NONE,SWT.NONE,false,false));
		
		final Label imageRight = new Label(this,SWT.NONE);
		imageRight.setImage(RepDevMain.mainShell.getFileImage(right));
		
		Label nameRight = new Label(this,SWT.NONE);
		nameRight.setText(right.getName());
		nameLeft.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		
		Label sep2 = new Label(this,SWT.NONE);
		sep2.setLayoutData(new GridData(SWT.NONE,SWT.NONE,false,false));
		
		leftTxt = new StyledText(this,SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL| SWT.BORDER);
		leftTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,2,1));
		leftTxt.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
				center.redraw();
			}

			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		leftTxt.getVerticalBar().setVisible(false);
		
		center = new Canvas(this,SWT.NONE);
		GridData data = new GridData(SWT.NONE, SWT.FILL, false, true,1,1);
		data.widthHint = getCenterWidth();
		center.setLayoutData(data);
		center.addPaintListener(new PaintListener(){

			public void paintControl(PaintEvent e) {
				int fPts[]= new int[8];	// scratch area for polygon drawing
				boolean fUseSingleLine = true, fUseSplines = true;
				Display display= center.getDisplay();
				GC g = e.gc;
				
				int lineHeight= leftTxt.getLineHeight();
	
				int visibleHeight= rightTxt.getBounds().height;

				Point size= center.getSize();
				int x= 0;
				int w= size.x;
						
				g.setBackground(center.getBackground());
				g.fillRectangle(x+1, 0, w-2, size.y);
				
				/*if (!fIsMotif) {
					// draw thin line between center ruler and both texts
					g.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
					g.fillRectangle(0, 0, 1, size.y);
					g.fillRectangle(w-1, 0, 1, size.y);
				}
					
				if (! fHighlightRanges)
					return;*/

				
				if (diffs != null) {
					
					//int lshift= leftTxt.getTopIndex() * lineHeight * -1;
					//int rshift= rightTxt.getTopIndex() * lineHeight * -1;
					int lshift = -1 * leftTxt.getVerticalBar().getSelection();
					int rshift = -1 * rightTxt.getVerticalBar().getSelection();
					
					Point region= new Point(0, 0);
				
					for( RangeDifference diff : diffs)
					{					
						region.x = diff.leftStart();
						region.y = diff.leftLength();
						
						int ly= ((region.x) * lineHeight) + lshift;
						int lh= region.y * lineHeight;
			
						region.x = diff.rightStart();
						region.y = diff.rightLength();
						int ry= (region.x) * lineHeight + rshift;
						int rh= region.y * lineHeight;
			
						if (Math.max(ly+lh, ry+rh) < 0)
							continue;
						if (Math.min(ly, ry) >= visibleHeight)
							break;
			
						fPts[0]= x;	fPts[1]= ly;	fPts[2]= w;	fPts[3]= ry;
						fPts[6]= x;	fPts[7]= ly+lh;	fPts[4]= w;	fPts[5]= ry+rh;
					
						Color fillColor= display.getSystemColor(SWT.COLOR_CYAN);
						Color strokeColor= display.getSystemColor(SWT.COLOR_MAGENTA);
						
					
						if (fUseSingleLine) {
							int w2= 3;

							g.setBackground(fillColor);
							g.fillRectangle(0, ly, w2, lh);		// left
							g.fillRectangle(w-w2, ry, w2, rh);	// right

							g.setLineWidth(0 /* LW */);
							g.setForeground(strokeColor);
							g.drawRectangle(0-1, ly, w2, lh);	// left
							g.drawRectangle(w-w2, ry, w2, rh);	// right

							if (fUseSplines) {
								int[] points= getCenterCurvePoints(w2, ly+lh/2, w-w2, ry+rh/2);
								for (int i= 1; i < points.length; i++)
									g.drawLine(w2+i-1, points[i-1], w2+i, points[i]);
							} else {
								g.drawLine(w2, ly+lh/2, w-w2, ry+rh/2);
							}
						} else {
							// two lines
							if (fUseSplines) {
								g.setBackground(fillColor);

								g.setLineWidth(0 /* LW */);
								g.setForeground(strokeColor);

								int[] topPoints= getCenterCurvePoints(fPts[0], fPts[1], fPts[2], fPts[3]);
								int[] bottomPoints= getCenterCurvePoints(fPts[6], fPts[7], fPts[4], fPts[5]);
								g.setForeground(fillColor);
								g.drawLine(0, bottomPoints[0], 0, topPoints[0]);
								for (int i= 1; i < bottomPoints.length; i++) {
									g.setForeground(fillColor);
									g.drawLine(i, bottomPoints[i], i, topPoints[i]);
									g.setForeground(strokeColor);
									g.drawLine(i-1, topPoints[i-1], i, topPoints[i]);
									g.drawLine(i-1, bottomPoints[i-1], i, bottomPoints[i]);
								}
							} else {
								g.setBackground(fillColor);
								g.fillPolygon(fPts);

								g.setLineWidth(0 /* LW */);
								g.setForeground(strokeColor);
								g.drawLine(fPts[0], fPts[1], fPts[2], fPts[3]);
								g.drawLine(fPts[6], fPts[7], fPts[4], fPts[5]);
							}
						}
						
						/*if (fUseSingleLine && showResolveUI && diff.isUnresolvedIncomingOrConflicting()) {
							// draw resolve state
							int cx= (w-RESOLVE_SIZE)/2;
							int cy= ((ly+lh/2) + (ry+rh/2) - RESOLVE_SIZE)/2;
							
							g.setBackground(fillColor);
							g.fillRectangle(cx, cy, RESOLVE_SIZE, RESOLVE_SIZE);
							
							g.setForeground(strokeColor);
							g.drawRectangle(cx, cy, RESOLVE_SIZE, RESOLVE_SIZE);
						}*/
					}
				}				
			}
			
		});
		
		rightTxt = new StyledText(this,SWT.READ_ONLY | SWT.H_SCROLL |SWT.V_SCROLL| SWT.BORDER);
		rightTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2,1));
		rightTxt.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
				center.redraw();
			}

			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		rightTxt.getVerticalBar().setVisible(false);
		
		
		slider = new Slider(this,SWT.VERTICAL);
		slider.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false, true, 1,2));
		slider.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				int vpos= slider.getSelection();
				synchronizedScrollVertical(vpos);
			}	
		});
		
		//If we layout now, before we add any text, then the two texts will be the same width
		pack();
		
		String sData = left.getData();
		
		if( sData == null )
			return;
		
		leftTxt.setText(sData);
		
		sData = right.getData();
		
		if( sData == null)
			return;
			
		rightTxt.setText(sData);
		
		//Perform the diff
		diffs = RangeDifferencer.findDifferences(new LineRangeComparator(leftTxt), new LineRangeComparator(rightTxt));
		
		//Create custom diffs line
		ArrayList<Integer> lLines = new ArrayList<Integer>(), rLines = new ArrayList<Integer>();
		
		System.out.println(Arrays.asList(diffs));
		
		for( RangeDifference diff : diffs){
			for( int i = diff.leftStart(); i < diff.leftEnd(); i++)
				lLines.add(i);
			
			for( int i = diff.rightStart(); i < diff.rightEnd(); i++)
				rLines.add(i);
		}
		
		int[] lIntLines = new int[lLines.size()], rIntLines = new int[rLines.size()];
		int count = 0;
		
		for( int i : lLines )
		{
			lIntLines[count] = i;
			count++;
		}
		
		count = 0;
		
		for( int i : rLines )
		{
			rIntLines[count] = i;
			count++;
		}
		
		if( left.getType() == FileType.REPGEN)
		{
			leftParser = new RepgenParser(leftTxt, left);
			leftHighlighter = new SyntaxHighlighter(leftParser, new RGB(247,247,247),lIntLines);
			leftParser.reparseAll();
			leftTxt.redrawRange(0,leftTxt.getCharCount(),true);
		}
		
		if( right.getType() == FileType.REPGEN)
		{
			rightParser = new RepgenParser(rightTxt, right);
			rightHighlighter = new SyntaxHighlighter(rightParser, new RGB(247,247,247),rIntLines);
			rightParser.reparseAll();
			rightTxt.redrawRange(0,rightTxt.getCharCount(),true);
		}
		
		
		System.out.println(Arrays.asList(leftTxt.getStyleRanges()));
	}
	
	private int[] getCenterCurvePoints(int startx, int starty, int endx, int endy) {
		if (fBasicCenterCurve == null)
			buildBaseCenterCurve(endx-startx);
		double height= endy - starty;
		height= height/2;
		int width= endx-startx;
		int[] points= new int[width];
		for (int i= 0; i < width; i++) {
			points[i]= (int) (-height * fBasicCenterCurve[i] + height + starty);
		}
		return points;
	}
	
	private void buildBaseCenterCurve(int w) {
		double width= w;
		fBasicCenterCurve= new double[getCenterWidth()];
		for (int i= 0; i < getCenterWidth(); i++) {
			double r= i / width;
			fBasicCenterCurve[i]= Math.cos(Math.PI * r);
		}
	}

	private int getCenterWidth() {
		return 20;
	}
	
	private void synchronizedScrollVertical(int vpos) {
		//scrollVertical(vpos, vpos, vpos, null);
	}
	
	
//	/*
//	 * Calculates virtual height (in lines) of views by adding the maximum of corresponding diffs.
//	 */
//	private int getVirtualHeight() {
//		int h= 1;
//		if (fAllDiffs != null) {
//			Iterator e= fAllDiffs.iterator();
//			for (int i= 0; e.hasNext(); i++) {
//				Diff diff= (Diff) e.next();
//				h+= diff.getMaxDiffHeight();
//			}
//		}
//		return h;
//	}
//	
//	/*
//	 * Calculates height (in lines) of right view by adding the height of the right diffs.
//	 */
//	private int getRightHeight() {
//		int h= 1;
//		if (fAllDiffs != null) {
//			Iterator e= fAllDiffs.iterator();
//			for (int i= 0; e.hasNext(); i++) {
//				Diff diff= (Diff) e.next();
//				h+= diff.getRightHeight();
//			}
//		}
//		return h;
//	}
//	
//	/*
//	 * The height of the TextEditors in lines.
//	 */
//	private int getViewportHeight() {
//		StyledText te= fLeft.getTextWidget();
//		
//		int vh= te.getClientArea().height;
//		if (vh == 0) {
//			Rectangle trim= te.computeTrim(0, 0, 0, 0);
//			int scrollbarHeight= trim.height;
//			
//			int headerHeight= getHeaderHeight();
//	
//			Composite composite= (Composite) getControl();
//			Rectangle r= composite.getClientArea();
//							
//			vh= r.height-headerHeight-scrollbarHeight;
//		}															
//
//		return vh / te.getLineHeight();
//	}
//	
//	/*
//	 * Returns the virtual position for the given view position.
//	 */
//	private int realToVirtualPosition(MergeSourceViewer w, int vpos) {
//
//		if (! fSynchronizedScrolling || fAllDiffs == null)
//			return vpos;
//				
//		int viewPos= 0;		// real view position
//		int virtualPos= 0;	// virtual position
//		Point region= new Point(0, 0);
//		
//		Iterator e= fAllDiffs.iterator();
//		while (e.hasNext()) {
//			Diff diff= (Diff) e.next();
//			Position pos= diff.getPosition(w);
//			w.getLineRange(pos, region);
//			int realHeight= region.y;
//			int virtualHeight= diff.getMaxDiffHeight();
//			if (vpos <= viewPos + realHeight) {	// OK, found!
//				vpos-= viewPos;	// make relative to this slot
//				// now scale position within this slot to virtual slot
//				if (realHeight <= 0)
//					vpos= 0;
//				else
//					vpos= (vpos*virtualHeight)/realHeight;
//				return virtualPos+vpos;
//			}
//			viewPos+= realHeight;
//			virtualPos+= virtualHeight;
//		}
//		return virtualPos;
//	}
//		
//	private void scrollVertical(int avpos, int lvpos, int rvpos, MergeSourceViewer allBut) {
//						
//		int s= 0;
//		
//		if (fSynchronizedScrolling) {
//			s= getVirtualHeight() - rvpos;
//			int height= fRight.getViewportLines()/4;
//			if (s < 0)
//				s= 0;
//			if (s > height)
//				s= height;
//		}
//
//		fInScrolling= true;
//				
//		if (isThreeWay() && allBut != fAncestor) {
//			if (fSynchronizedScrolling || allBut == null) {
//				int y= virtualToRealPosition(fAncestor, avpos+s)-s;
//				fAncestor.vscroll(y);
//			}
//		}
//
//		if (allBut != fLeft) {
//			if (fSynchronizedScrolling || allBut == null) {
//				int y= virtualToRealPosition(fLeft, lvpos+s)-s;
//				fLeft.vscroll(y);
//			}
//		}
//
//		if (allBut != fRight) {
//			if (fSynchronizedScrolling || allBut == null) {
//				int y= virtualToRealPosition(fRight, rvpos+s)-s;
//				fRight.vscroll(y);
//			}
//		}
//		
//		fInScrolling= false;
//		
//		if (isThreeWay() && fAncestorCanvas != null)
//			fAncestorCanvas.repaint();
//		
//		if (fLeftCanvas != null)
//			fLeftCanvas.repaint();
//		
//		Control center= getCenterControl();
//		if (center instanceof BufferedCanvas)
//			((BufferedCanvas)center).repaint();
//		
//		if (fRightCanvas != null)
//			fRightCanvas.repaint();
//	}
//		
//	/*
//	 * Updates Scrollbars with viewports.
//	 */
//	private void syncViewport(MergeSourceViewer w) {
//		
//		if (fInScrolling)
//			return;
//
//		int ix= w.getTopIndex();
//		int ix2= w.getDocumentRegionOffset();
//		
//		int viewPosition= realToVirtualPosition(w, ix-ix2);
//				
//		scrollVertical(viewPosition, viewPosition, viewPosition, w);	// scroll all but the given views
//		
//		if (fVScrollBar != null) {
//			int value= Math.max(0, Math.min(viewPosition, getVirtualHeight() - getViewportHeight()));
//			fVScrollBar.setSelection(value);
//			//refreshBirdEyeView();
//		}
//	}
}
