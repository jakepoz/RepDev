/**
 * VERY IMPORTANT LICENSE INFORMATION!!!
 * 
 * This code is technically under the EPL vs GPL for all other packages in this project!!! (As well as com.repdev.compare)
 * 
 * I am porting it over ASAP to it's own SF.net project that will be a SWT Control
 * for comparing two text files that we will use the object code version in this
 * GPL program. If anyone has any issues with this slight delay, please be patient!
 */

package com.repdev.compare;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Slider;

/**
 * How to use:
 * It's a standard SWT Composite, not much too it, add it as a control somewhere
 * 
 * getLeft/RightTxt return a styledtext you can work with
 * refreshDiff() redoes that
 * @author poznanja
 *
 */
public class BasicCompareComposite extends Composite{
	protected StyledText leftTxt, rightTxt;
	private Composite parent;
	private Canvas center;
	private Slider slider;
	private double[] fBasicCenterCurve;
	protected RangeDifference[] diffs;
	private boolean fInScrolling;
	private String leftName, rightName;
	private Image leftImage, rightImage;
	public static Color boxFill = new Color(Display.getCurrent(),new RGB(247,247,247));
	public static Color boxOutline = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY); 
	
	public BasicCompareComposite(Composite parent, String leftName, String rightName, Image leftImage, Image rightImage){
		super(parent,SWT.NONE);
		this.leftName = leftName;
		this.leftImage= leftImage;
		this.rightName = rightName;
		this.rightImage = rightImage;
		this.parent = parent;

		buildGUI();
	}

	public void buildGUI() {
		GridLayout layout = new GridLayout();
		layout.numColumns =6;
		//layout.marginLeft = layout.marginRight = 0;
		layout.horizontalSpacing = 0;
		setLayout(layout);
		
		final Label imageLeft = new Label(this,SWT.NONE);
		imageLeft.setImage(leftImage);
		
		Label nameLeft = new Label(this,SWT.NONE);	
		nameLeft.setText(leftName);
		nameLeft.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		
		Label sep1 = new Label(this,SWT.NONE);
		sep1.setLayoutData(new GridData(SWT.NONE,SWT.NONE,false,false));
		
		final Label imageRight = new Label(this,SWT.NONE);
		imageRight.setImage(rightImage);
		
		Label nameRight = new Label(this,SWT.NONE);
		nameRight.setText(rightName);
		nameLeft.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		
		Label sep2 = new Label(this,SWT.NONE);
		sep2.setLayoutData(new GridData(SWT.NONE,SWT.NONE,false,false));
		
		
		leftTxt = new StyledText(this,SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL| SWT.BORDER);
		leftTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,2,1));
		leftTxt.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
				syncViewport(leftTxt);
				center.redraw();
			}

			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		leftTxt.getVerticalBar().setVisible(false);
		leftTxt.getVerticalBar().addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				 //syncViewport(leftTxt);
				 center.redraw();
				 rightTxt.redraw();
				 leftTxt.redraw();
			}		
		});
		
		center = new Canvas(this,SWT.NONE);
		GridData data = new GridData(SWT.NONE, SWT.FILL, false, true,1,1);
		data.widthHint = getCenterWidth();
		center.setLayoutData(data);
		
		rightTxt = new StyledText(this,SWT.READ_ONLY | SWT.H_SCROLL |SWT.V_SCROLL| SWT.BORDER);
		rightTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2,1));
		rightTxt.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
				syncViewport(rightTxt);
				center.redraw();
			}

			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		rightTxt.getVerticalBar().setVisible(false);
		rightTxt.getVerticalBar().addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				// syncViewport(rightTxt);
				 center.redraw();
			     rightTxt.redraw();
				 leftTxt.redraw();
			}		
		});
		
		center.addPaintListener(new PaintListener(){

			public void paintControl(PaintEvent e) {
				int fPts[]= new int[8];	// scratch area for polygon drawing
				boolean fUseSingleLine = true, fUseSplines = true;
				Display display= center.getDisplay();
				GC g = e.gc;
				
				int lineHeight= leftTxt.getLineHeight();
	
				int visibleHeight= leftTxt.getClientArea().height;

				Point size= center.getSize();
				int x= 0;
				int w= size.x;
						
				g.setBackground(center.getBackground());
				g.fillRectangle(x+1, 0, w-2, size.y);
				
				if (diffs != null) {
					
					//int lshift= leftTxt.getTopIndex() * lineHeight * -1;
					//int rshift= rightTxt.getTopIndex() * lineHeight * -1;
					int lshift = -1 * leftTxt.getVerticalBar().getSelection();
					int rshift = -1 * rightTxt.getVerticalBar().getSelection();
					
					Point region= new Point(0, 0);
				
					for( RangeDifference diff : diffs)
					{				
						if( diff.kind() == RangeDifference.NOCHANGE )
							continue;
						
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
					
						Color fillColor= boxFill;
						Color strokeColor= boxOutline;
						
					
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
					}
				}				
			}
			
		});
		
		//Horizontal scroll sync
		hsynchViewport(leftTxt, rightTxt);
		hsynchViewport(rightTxt, leftTxt);
		
		slider = new Slider(this,SWT.VERTICAL);
		slider.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false, true, 1,2));
		slider.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				int vpos= slider.getSelection();
				synchronizedScrollVertical(vpos);
	
			}	
		});
		
		refreshDiffs();
		
		//If we layout now, before we add any text, then the two texts will be the same width
		pack();
		
		leftTxt.redrawRange(0,leftTxt.getCharCount(),true);
		rightTxt.redrawRange(0,rightTxt.getCharCount(),true);
		
		leftTxt.addListener(SWT.MouseWheel, new Listener(){
			public void handleEvent(Event event) {
				slider.setSelection(Math.min(slider.getMaximum(),Math.max(0,slider.getSelection()-event.count)));
				synchronizedScrollVertical(slider.getSelection());
			}			
		});
		
		rightTxt.addListener(SWT.MouseWheel, new Listener(){
			public void handleEvent(Event event) {
				slider.setSelection(Math.min(slider.getMaximum(),Math.max(0,slider.getSelection()-event.count)));
				synchronizedScrollVertical(slider.getSelection());
			}			
		});
		
		leftTxt.addPaintListener(new PaintListener(){
			public void paintControl(PaintEvent e) {
				doPaint(leftTxt,e);
			}
		});
		
		rightTxt.addPaintListener(new PaintListener(){
			public void paintControl(PaintEvent e) {
				doPaint(rightTxt,e);
			}
		});
	}
	
	
	
	@Override
	public void redraw() {
		super.redraw();
		
		leftTxt.redraw();
		rightTxt.redraw();
	}

	protected void doPaint(StyledText txt, PaintEvent event) {
		if (diffs == null)
			return;

		int LW = 1;
		
		Control canvas= (Control) event.widget;
		GC g= event.gc;
		
		Display display= canvas.getDisplay();
		
		int lineHeight= txt.getLineHeight();			
		int w= canvas.getSize().x;
		int shift= txt.getVerticalBar().getSelection() + (2-LW);
		int maxh= event.y+event.height; 	// visibleHeight
		
		//if (fIsMotif)
		//	shift+= fTopInset;
				
		Point range= new Point(0, 0);
				
		for( RangeDifference diff : diffs){
			if( diff.kind() == RangeDifference.NOCHANGE )
				continue;
			
			if( txt == leftTxt){
				range.x = leftTxt.getLineHeight() * diff.leftStart();
				range.y = leftTxt.getLineHeight() * diff.leftLength();
			}
			else{
				range.x = rightTxt.getLineHeight() * diff.rightStart();
				range.y = rightTxt.getLineHeight() * diff.rightLength();
			}
			
			int y= (range.x ) - shift;
			int h= range.y;
			
			if (y+h < event.y)
				continue;
			if (y > maxh)
				break;
			
			g.setBackground(boxOutline);
			g.fillRectangle(0, y-1, w, LW);
			g.fillRectangle(0, y+h-1, w, LW);
		}
	}

	protected void refreshDiffs() {
		//Perform the diff
		diffs = RangeDifferencer.findRanges(new LineRangeComparator(leftTxt), new LineRangeComparator(rightTxt));
		updateVScrollBar();
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
	
	private void hsynchViewport(final StyledText st1, final StyledText st2) {
		final ScrollBar sb1 = st1.getHorizontalBar();
		sb1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				int max = sb1.getMaximum() - sb1.getThumb();
				double v = 0.0;
				if (max > 0)
					v = (float) sb1.getSelection() / (float) max;
				if (st2.isVisible()) {
					ScrollBar sb2 = st2.getHorizontalBar();
					st2.setHorizontalPixel((int) ((sb2.getMaximum() - sb2.getThumb()) * v));
				}

			}
		});
	}
	
	
	private void synchronizedScrollVertical(int vpos) {
		scrollVertical(vpos, vpos, null);
	}
	
	
	/*
	 * Calculates virtual height (in lines) of views
	 */
	private int getVirtualHeight() {
		return Math.max(leftTxt.getLineCount(), rightTxt.getLineCount());
	}
	
	/*
	 * The height of the TextEditors in lines.
	 */
	private int getViewportHeight() {
		StyledText te= leftTxt;
		
		int vh= te.getClientArea().height;
	
		return vh / te.getLineHeight();
	}
	
	/*
	 * Returns the virtual position for the given view position.
	 * 
	 * Line to pixel scroll value
	 */
	private int realToVirtualPosition(StyledText txt, int vpos) {
		if (diffs == null)
			return vpos;
				
		int viewPos= 0;		// real view position
		int virtualPos= 0;	// virtual position
		Point region= new Point(0, 0);
		
		for( RangeDifference diff : diffs){
			//Position pos= diff.getPosition(w);
			//w.getLineRange(pos, region);
			if( txt == leftTxt){
				region.y = leftTxt.getLineHeight() * diff.leftLength();
			}
			else{
				region.y = rightTxt.getLineHeight() * diff.rightLength();
			}
				
			int realHeight= diff.getMaxDiffHeight();
			int virtualHeight= region.y;
			if (vpos <= viewPos + realHeight) {	// OK, found!
				vpos-= viewPos;	// make relative to this slot
				// now scale position within this slot to virtual slot
				if (realHeight <= 0)
					vpos= 0;
				else
					vpos= (vpos*virtualHeight)/realHeight;
				return virtualPos+vpos;
			}
			viewPos+= realHeight;
			virtualPos+= virtualHeight;
		}
		return virtualPos;
	}
		
	private void scrollVertical( int lvpos, int rvpos, StyledText allBut) {					
		int s= 0;
		StyledText otherTxt;
		
		if( allBut == null )
			otherTxt = rightTxt;
		else if( allBut == leftTxt)
			otherTxt = rightTxt;
		else
			otherTxt = leftTxt;
		
		s= otherTxt.getTopIndex() - rvpos;
		int height= getViewportHeight()/3;
		if (s < 0)
			s= 0;
		if (s > height)
			s= height;
		

		fInScrolling= true;
				
		if (allBut != leftTxt) {
			int y= realToVirtualPosition(leftTxt, lvpos+s)-s;
			leftTxt.setTopIndex(y/leftTxt.getLineHeight());
		}

		if (allBut != rightTxt) {
			int y= realToVirtualPosition(rightTxt, rvpos+s)-s;
			rightTxt.setTopIndex(y/rightTxt.getLineHeight());
		}
		
		fInScrolling= false;
		
		center.redraw();
	}
		
	/*
	 * Updates Scrollbars with viewports.
	 * 
	 * TODO: Not totally correct, their code was funny here. I just hooked scroll wheel events
	 * so that it just updates the right slider, as needed correctly.
	 */
	private void syncViewport(StyledText txt) {
		if (fInScrolling)
			return;
		
		StyledText otherTxt;
		
		if( txt == leftTxt )
			otherTxt = rightTxt;
		else
			otherTxt = leftTxt;
	

		int ix= txt.getTopIndex();
		//int ix2= txt.getDocumentRegionOffset();
		//int ix2 = 0;
	
		int ix2 = 0;
	
		
		//int viewPosition= virtualToRealPosition(txt,realToVirtualPosition(txt, ix-ix2));
		int viewPosition = ix;
				
		scrollVertical(viewPosition, viewPosition, txt);	// scroll all but the given views
		
	
		if (slider != null) {
			int value= Math.max(0, Math.min(viewPosition, getVirtualHeight() - getViewportHeight()));
			slider.setSelection(value);
			//refreshBirdEyeView();
		}
	}
	
	/**
	 */
	private void updateVScrollBar() {
		int virtualHeight= getVirtualHeight();
		int viewPortHeight= getViewportHeight();
		int pageIncrement= viewPortHeight-1;
		int thumb= (viewPortHeight > virtualHeight) ? virtualHeight : viewPortHeight;
					
		slider.setPageIncrement(pageIncrement);
		slider.setMaximum(virtualHeight);
		slider.setThumb(thumb);				
	}
	
	/*
	 * maps given virtual position into a real view position of this view.
	 * 
	 * Pixel scroll value to line
	 */
	private int virtualToRealPosition(StyledText txt, int v) {
		if (diffs == null)
			return v;
					
		int virtualPos= 0;
		int viewPos= 0;
		Point region= new Point(0, 0);
		
		for( RangeDifference diff : diffs){
			Point pos = new Point(0,0);
			
			if( txt == leftTxt){
				region.y = leftTxt.getLineHeight() * diff.leftLength();
			}
			else{
				region.y = rightTxt.getLineHeight() * diff.rightLength();
			}
			
			int viewHeight= diff.getMaxDiffHeight();
			int virtualHeight= region.y;
			if (v < (virtualPos + virtualHeight)) {
				v-= virtualPos;		// make relative to this slot
				if (viewHeight <= 0) {
					v= 0;
				} else {
					v= (int) (v * ((double)viewHeight/virtualHeight));
				}
				return viewPos+v;
			}
			virtualPos+= virtualHeight;
			viewPos+= viewHeight;
		}
		return viewPos;
	}
	
}
