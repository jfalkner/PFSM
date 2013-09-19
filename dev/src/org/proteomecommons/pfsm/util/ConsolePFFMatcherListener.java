package org.proteomecommons.pfsm.util;

import org.proteomecommons.pfsm.*;
import org.proteomecommons.io.*;

/**
 * Simple text-based listener for PFF matchers.
 * 
 * @author Jayson Falkner - jfalkenr@umich.edu
 *  
 */
public class ConsolePFFMatcherListener implements PFFMatcherListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.proteomecommons.pfsm.util.FASTAMatcherListener#peptideMatched(byte[],
	 *      int, int, byte[], int, int, int, int,
	 *      org.proteomecommons.pfsm.DFAState)
	 */
	public void peptideMatched(char[] sequence, int offset, int length,
			DFAState fin) {
		for (int i = offset; i < offset + length; i++) {
			System.out.print(sequence[i]);
		}
		// display peak lists
		GreedyPeakFin[] pgfs = fin.getFinals();
		for (int i = 0; i < pgfs.length; i++) {
			System.out.print(" "+((TandemPeakList)pgfs[i].peaklist).getParent().getMassOverCharge());
		}
		System.out.println();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.proteomecommons.pfsm.util.FASTAMatcherListener#searchFinished(org.proteomecommons.pfsm.util.FASTAMatcher)
	 */
	public void searchFinished(PFFMatcher matcher) {
		System.out.println("Search Complete.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.proteomecommons.pfsm.util.FASTAMatcherListener#searchStarted(org.proteomecommons.pfsm.util.FASTAMatcher)
	 */
	public void searchStarted(PFFMatcher matcher) {
		System.out.println("Search Started");
	}
}