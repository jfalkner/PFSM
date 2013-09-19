/*
 * Created on Mar 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.proteomecommons.pfsm.util;

import org.proteomecommons.pfsm.*;
import org.proteomecommons.io.*;

/**
 * @author Jayson Falkner - jfalkenr@umich.edu
 *
 */
public class ConsoleFASTAMatcherListener implements FASTAMatcherListener {

	/* (non-Javadoc)
	 * @see org.proteomecommons.pfsm.util.FASTAMatcherListener#peptideMatched(byte[], int, int, byte[], int, int, int, int, org.proteomecommons.pfsm.DFAState)
	 */
	public void peptideMatched(byte[] proteinName, int proteinNameOffset,
			int proteinNameLength, byte[] protein, int proteinOffset,
			int proteinLength, int sequenceOffset, int sequenceLength,
			DFAState finState) {

		// print the name
		for (int i=proteinNameOffset;i<proteinNameOffset+proteinNameLength;i++){
			System.out.print((char)proteinName[i]);
		}
		System.out.println();
		
// display the match
		System.out.print("Peptide Match: ");
		for (int z = 0; z < sequenceLength; z++) {
			System.out.print((char) protein[sequenceOffset+ z]);
		}
		// print each peaklist
		GreedyPeakFin[] gpfs = (GreedyPeakFin[]) finState.getFinals();
		for (int z = 0; z < gpfs.length; z++) {
			System.out.print(" " + ((TandemPeakList)gpfs[z].peaklist).getParent().getMassOverCharge());
		}
		System.out.println();

	}
	/* (non-Javadoc)
	 * @see org.proteomecommons.pfsm.util.FASTAMatcherListener#searchFinished(org.proteomecommons.pfsm.util.FASTAMatcher)
	 */
	public void searchFinished(FASTAMatcher matcher) {
		System.out.println("Search Complete.");
	}
	/* (non-Javadoc)
	 * @see org.proteomecommons.pfsm.util.FASTAMatcherListener#searchStarted(org.proteomecommons.pfsm.util.FASTAMatcher)
	 */
	public void searchStarted(FASTAMatcher matcher) {
		System.out.println("Search Started");
	}
}
