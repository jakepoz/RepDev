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

package com.repdev.parser;

/**
 * Lets the parser ask for text segments that exist in the document but have
 * been hidden from the {@link org.eclipse.swt.custom.StyledText} buffer (e.g.
 * collapsed fold regions).
 *
 * The interface lives in the parser package so {@code RepgenParser} can
 * depend on it without importing back into {@code com.repdev}, avoiding a
 * package-cycle. The implementation (currently {@code FoldingManager}) is
 * responsible for excluding segments where token-shape scans don't make
 * sense — bodies of folded {@code DEFINE...END} blocks (declarations, not
 * usages) and bracket comment bodies.
 */
public interface HiddenTextProvider {

	/**
	 * Snapshot of currently-hidden text segments suitable for usage scans —
	 * i.e. excluding declaration bodies and comment bodies.
	 */
	Iterable<String> getUsageSearchableHiddenText();

	/**
	 * Snapshot of the canonical (unfolded) source text. The parser uses this
	 * for variable discovery and error-position arithmetic so that content
	 * hidden inside collapsed folds still contributes to the symbol table and
	 * error reports.
	 *
	 * <p>Returning {@code null} signals "no projection in effect" — callers
	 * fall back to the live {@code StyledText} buffer. Implementations that
	 * always project the full source should always return non-null.
	 */
	String getFullSourceText();

	/**
	 * Translate a view offset (index into the live {@code StyledText} buffer)
	 * to a model offset (index into the unfolded source). Implementations
	 * with no active projection return {@code viewOffset} unchanged.
	 */
	int viewToModel(int viewOffset);

	/**
	 * Translate a model offset to a view offset, or {@code -1} if the model
	 * offset lies inside a currently-hidden region.
	 */
	int modelToView(int modelOffset);

	/**
	 * Notification that the parser has finished updating its token list.
	 * Folding implementations that derive foldable ranges from
	 * {@code parser.getLtokens()} should rebuild that cache here — driving
	 * the rebuild from this callback rather than a {@code modifyText}
	 * listener avoids the ordering trap where the listener fires before
	 * the parser has processed the same edit.
	 */
	void onTokensUpdated();
}
