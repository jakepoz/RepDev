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
}
