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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Stack;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import com.repdev.parser.RepgenParser;
import com.repdev.parser.Token;

/**
 * Code folding for the RepDev editor. Foldable regions come from the RepGen
 * parser's existing block matcher: every head/end pair (DEFINE..END, SETUP..END,
 * DO..END, PROCEDURE..END, HEADERS..END, bracket comments [...], etc.) that
 * spans more than one line can be folded.
 *
 * Folding is implemented by physically removing the hidden lines from the
 * StyledText buffer and caching them in-memory. On save, getUnfoldedText()
 * reassembles the full source.
 */
public class FoldingManager {

	/** Width in pixels of the gutter column that holds the fold triangle. */
	public static final int FOLD_COLUMN_WIDTH = 18;

	/** A currently-collapsed region. */
	public static class FoldRegion {
		public final int headerLine;
		public final String hiddenText;

		FoldRegion(int headerLine, String hiddenText) {
			this.headerLine = headerLine;
			this.hiddenText = hiddenText;
		}
	}

	/** A precomputed head/end pair in the current buffer that can be collapsed. */
	public static class FoldableRange {
		public final int headerLine;
		public final int endLine;
		public final int endTokenOffset;
		public final boolean bracket;

		FoldableRange(int headerLine, int endLine, int endTokenOffset, boolean bracket) {
			this.headerLine = headerLine;
			this.endLine = endLine;
			this.endTokenOffset = endTokenOffset;
			this.bracket = bracket;
		}
	}

	private final StyledText txt;
	private final RepgenParser parser;
	private final EditorComposite editor;

	private final ArrayList<FoldRegion> folded = new ArrayList<FoldRegion>();
	private final ArrayList<FoldableRange> foldable = new ArrayList<FoldableRange>();

	private final Color markerColor;
	private boolean inFoldOp = false;

	public FoldingManager(EditorComposite editor, StyledText txt, RepgenParser parser) {
		this.editor = editor;
		this.txt = txt;
		this.parser = parser;
		this.markerColor = new Color(txt.getDisplay(), new RGB(90, 90, 90));
		install();
	}

	private void install() {
		txt.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				paintMarkers(e);
			}
		});

		txt.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (e.button != 1 || txt.getLineHeight() == 0) return;
				int gutter = editor.calcWidth();
				int foldColStart = gutter - FOLD_COLUMN_WIDTH;
				if (foldColStart < 0) foldColStart = 0;
				if (e.x < foldColStart || e.x > gutter) return;
				int line = txt.getTopIndex() + (e.y / txt.getLineHeight());
				if (line < 0 || line >= txt.getLineCount()) return;
				toggleAtLine(line);
			}
		});
	}

	public int getExtraGutterWidth() { return FOLD_COLUMN_WIDTH; }

	public boolean isInFoldOp() { return inFoldOp; }

	public boolean hasActiveFolds() { return !folded.isEmpty(); }

	/**
	 * Shift the headerLine of every fold whose header is strictly below the
	 * given edit line. Called after a user edit changes the newline count above
	 * folded regions, so they keep pointing at the right buffer lines.
	 */
	public void shiftFoldsBelow(int editLine, int delta) {
		if (delta == 0) return;
		for (int i = 0; i < folded.size(); i++) {
			FoldRegion fr = folded.get(i);
			if (fr.headerLine > editLine) {
				folded.set(i, new FoldRegion(fr.headerLine + delta, fr.hiddenText));
			}
		}
	}

	/**
	 * Recompute foldable ranges from the current token list. Must be called
	 * after the parser has caught up with the current buffer state.
	 */
	public void recomputeRanges() {
		foldable.clear();
		if (parser == null) return;
		ArrayList<Token> tokens = parser.getLtokens();
		if (tokens == null || tokens.isEmpty()) { txt.redraw(); return; }

		// Head tokens on folded header lines are orphans: their matching end
		// has been removed from the buffer, so pushing them onto the stack
		// would mispair outer regions. Collect those lines up front and skip.
		HashSet<Integer> foldedHeaderLines = new HashSet<Integer>();
		for (int i = 0; i < folded.size(); i++) foldedHeaderLines.add(folded.get(i).headerLine);

		Stack<Integer> stack = new Stack<Integer>();
		int charCount = txt.getCharCount();
		for (int i = 0; i < tokens.size(); i++) {
			Token t = tokens.get(i);
			String s = t.getStr();
			// Only consider block-level head/end pairs: skip string/date/paren openers
			// which produce noisy single-line folds.
			if (t.isRealHead() && !"\"".equals(s) && !"'".equals(s)
					&& !"(".equals(s) && !":(".equals(s)) {
				int tStart = t.getStart();
				if (tStart < 0 || tStart >= charCount) continue;
				int tLine;
				try { tLine = txt.getLineAtOffset(tStart); }
				catch (IllegalArgumentException ex) { continue; }
				if (foldedHeaderLines.contains(tLine)) continue; // orphan head inside a collapsed region
				stack.push(i);
			} else if (t.isRealEnd() && !")".equals(s) && !"\"".equals(s) && !"'".equals(s)) {
				if (!stack.isEmpty()) {
					int headIdx = stack.pop();
					Token head = tokens.get(headIdx);
					int hStart = head.getStart();
					int eStart = t.getStart();
					if (hStart < 0 || eStart < 0 || hStart >= charCount || eStart > charCount) continue;
					try {
						int hl = txt.getLineAtOffset(hStart);
						int el = txt.getLineAtOffset(eStart);
						boolean isBracket = "[".equals(head.getStr());
						if (el - hl >= 1) foldable.add(new FoldableRange(hl, el, eStart, isBracket));
					} catch (IllegalArgumentException ignored) { }
				}
			}
		}
		txt.redraw();
	}

	public FoldableRange foldableAtLine(int line) {
		for (int i = 0; i < foldable.size(); i++) {
			FoldableRange r = foldable.get(i);
			if (r.headerLine == line) return r;
		}
		return null;
	}

	public FoldRegion foldedAtLine(int line) {
		for (int i = 0; i < folded.size(); i++) {
			FoldRegion r = folded.get(i);
			if (r.headerLine == line) return r;
		}
		return null;
	}

	public boolean isFoldableLine(int line) {
		return foldableAtLine(line) != null || foldedAtLine(line) != null;
	}

	/** Toggle the fold at the given line; returns true if it's now collapsed. */
	public boolean toggleAtLine(int line) {
		FoldRegion existing = foldedAtLine(line);
		if (existing != null) {
			expand(existing);
			return false;
		}
		FoldableRange fr = foldableAtLine(line);
		if (fr == null) return false;
		collapse(fr);
		return true;
	}

	/** Collapse the innermost foldable region that contains the caret. */
	public void collapseAtCaret() {
		int caretLine = txt.getLineAtOffset(txt.getCaretOffset());
		FoldableRange best = null;
		for (int i = 0; i < foldable.size(); i++) {
			FoldableRange r = foldable.get(i);
			if (r.headerLine <= caretLine && caretLine <= r.endLine) {
				if (best == null || r.headerLine > best.headerLine) best = r;
			}
		}
		if (best != null && foldedAtLine(best.headerLine) == null) collapse(best);
	}

	public void expandAtCaret() {
		int line = txt.getLineAtOffset(txt.getCaretOffset());
		FoldRegion fr = foldedAtLine(line);
		if (fr != null) expand(fr);
	}

	public void collapseAll() {
		// Fold from bottom up so earlier line numbers stay stable during iteration.
		ArrayList<FoldableRange> ranges = new ArrayList<FoldableRange>(foldable);
		Collections.sort(ranges, new Comparator<FoldableRange>() {
			public int compare(FoldableRange a, FoldableRange b) { return b.headerLine - a.headerLine; }
		});
		for (int i = 0; i < ranges.size(); i++) {
			FoldableRange r = ranges.get(i);
			FoldableRange fresh = foldableAtLine(r.headerLine);
			if (fresh != null && foldedAtLine(fresh.headerLine) == null) collapse(fresh);
		}
	}

	public void expandAll() {
		// Unfold innermost-first (largest line number first).
		ArrayList<FoldRegion> regions = new ArrayList<FoldRegion>(folded);
		Collections.sort(regions, new Comparator<FoldRegion>() {
			public int compare(FoldRegion a, FoldRegion b) { return b.headerLine - a.headerLine; }
		});
		for (int i = 0; i < regions.size(); i++) expand(regions.get(i));
	}

	private void collapse(FoldableRange range) {
		// Expand any nested folds inside this range so the captured hidden text is complete.
		ArrayList<FoldRegion> nested = new ArrayList<FoldRegion>();
		for (int i = 0; i < folded.size(); i++) {
			FoldRegion fr = folded.get(i);
			if (fr.headerLine > range.headerLine && fr.headerLine <= range.endLine) nested.add(fr);
		}
		Collections.sort(nested, new Comparator<FoldRegion>() {
			public int compare(FoldRegion a, FoldRegion b) { return b.headerLine - a.headerLine; }
		});
		for (int i = 0; i < nested.size(); i++) expand(nested.get(i));

		// Reacquire the range after any line shifts from the nested expansions.
		FoldableRange fresh = foldableAtLine(range.headerLine);
		if (fresh == null) return;
		range = fresh;

		int lineCount = txt.getLineCount();
		if (range.headerLine + 1 >= lineCount) return;
		int sliceStart = txt.getOffsetAtLine(range.headerLine + 1);
		int sliceEnd;
		if (range.bracket) {
			// Keep the ']' visible so the comment highlighter still sees the close.
			sliceEnd = range.endTokenOffset;
		} else if (range.endLine + 1 >= lineCount) {
			sliceEnd = txt.getCharCount();
		} else {
			sliceEnd = txt.getOffsetAtLine(range.endLine + 1);
		}
		if (sliceEnd <= sliceStart) return;

		String hidden = txt.getText(sliceStart, sliceEnd - 1);
		int linesBefore = txt.getLineCount();

		inFoldOp = true;
		try {
			if (parser != null) parser.setReparse(false);
			txt.setRedraw(false);
			txt.replaceTextRange(sliceStart, sliceEnd - sliceStart, "");
		} finally {
			txt.setRedraw(true);
			if (parser != null) {
				parser.setReparse(true);
				parser.reparseAll();
			}
			inFoldOp = false;
		}

		int removedLines = linesBefore - txt.getLineCount();
		// Compute the original last visible line that moved up by removedLines.
		// After collapse: the "]" (bracket case) or nothing extra stays at headerLine+1.
		int breakLine = range.bracket ? range.headerLine : range.endLine;
		for (int i = 0; i < folded.size(); i++) {
			FoldRegion fr = folded.get(i);
			if (fr.headerLine > breakLine) {
				folded.set(i, new FoldRegion(fr.headerLine - removedLines, fr.hiddenText));
			}
		}
		folded.add(new FoldRegion(range.headerLine, hidden));

		recomputeRanges();
	}

	private void expand(FoldRegion region) {
		int lineCount = txt.getLineCount();
		int insertOffset = (region.headerLine + 1 >= lineCount)
				? txt.getCharCount()
				: txt.getOffsetAtLine(region.headerLine + 1);

		inFoldOp = true;
		try {
			if (parser != null) parser.setReparse(false);
			txt.setRedraw(false);
			txt.replaceTextRange(insertOffset, 0, region.hiddenText);
		} finally {
			txt.setRedraw(true);
			if (parser != null) {
				parser.setReparse(true);
				parser.reparseAll();
			}
			inFoldOp = false;
		}

		folded.remove(region);
		int addedLines = countNewlines(region.hiddenText);
		for (int i = 0; i < folded.size(); i++) {
			FoldRegion fr = folded.get(i);
			if (fr.headerLine > region.headerLine) {
				folded.set(i, new FoldRegion(fr.headerLine + addedLines, fr.hiddenText));
			}
		}

		recomputeRanges();
	}

	/**
	 * Reassembles the complete source by re-inserting all hidden regions. Used
	 * at save time so folded files persist correctly.
	 */
	public String getUnfoldedText() {
		if (folded.isEmpty()) return txt.getText();
		ArrayList<FoldRegion> sorted = new ArrayList<FoldRegion>(folded);
		// Insert from bottom up so earlier insert offsets stay valid.
		Collections.sort(sorted, new Comparator<FoldRegion>() {
			public int compare(FoldRegion a, FoldRegion b) { return b.headerLine - a.headerLine; }
		});
		StringBuilder sb = new StringBuilder(txt.getText());
		for (int i = 0; i < sorted.size(); i++) {
			FoldRegion fr = sorted.get(i);
			int offset = offsetAtLineStart(sb, fr.headerLine + 1);
			sb.insert(offset, fr.hiddenText);
		}
		return sb.toString();
	}

	private static int offsetAtLineStart(CharSequence cs, int line) {
		if (line <= 0) return 0;
		int found = 0;
		for (int i = 0; i < cs.length(); i++) {
			if (cs.charAt(i) == '\n') {
				found++;
				if (found == line) return i + 1;
			}
		}
		return cs.length();
	}

	private static int countNewlines(String s) {
		int n = 0;
		for (int i = 0; i < s.length(); i++) if (s.charAt(i) == '\n') n++;
		return n;
	}

	private void paintMarkers(PaintEvent e) {
		if (txt.isDisposed() || txt.getLineHeight() == 0) return;
		int lh = txt.getLineHeight();
		int topLine = txt.getTopIndex();
		int clientH = txt.getClientArea().height;
		int maxLine = topLine + (clientH / lh) + 2;
		if (maxLine > txt.getLineCount()) maxLine = txt.getLineCount();

		int gutterW = editor.calcWidth();
		int cx = gutterW - (FOLD_COLUMN_WIDTH / 2) - 1;

		GC gc = e.gc;
		Color oldFg = gc.getForeground();
		Color oldBg = gc.getBackground();
		gc.setForeground(markerColor);
		gc.setBackground(markerColor);

		for (int line = topLine; line < maxLine; line++) {
			boolean isFolded = foldedAtLine(line) != null;
			boolean isFoldable = isFolded || (foldableAtLine(line) != null);
			if (!isFoldable) continue;
			int y;
			try {
				y = txt.getLocationAtOffset(txt.getOffsetAtLine(line)).y;
			} catch (IllegalArgumentException ex) { continue; }
			int cy = y + (lh / 2);
			int s = 6;
			if (isFolded) {
				// Right-pointing triangle (region is collapsed)
				int[] pts = { cx - (s - 2), cy - s, cx + (s - 1), cy, cx - (s - 2), cy + s };
				gc.fillPolygon(pts);
			} else {
				// Down-pointing triangle (region is expanded, click to collapse)
				int[] pts = { cx - s, cy - (s - 3), cx + s, cy - (s - 3), cx, cy + (s - 1) };
				gc.fillPolygon(pts);
			}
		}
		gc.setForeground(oldFg);
		gc.setBackground(oldBg);
	}

	public void dispose() {
		if (markerColor != null && !markerColor.isDisposed()) markerColor.dispose();
	}
}
