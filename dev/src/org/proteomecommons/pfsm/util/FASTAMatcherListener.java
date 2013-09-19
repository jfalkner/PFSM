package org.proteomecommons.pfsm.util;

import org.proteomecommons.pfsm.*;

/**
 * Simple serial listener for FASTAMatcher instances.
 * @author Jayson Falkner - jfalkner@umich.edu
 */
public interface FASTAMatcherListener {
  void searchStarted(FASTAMatcher matcher);
  void searchFinished(FASTAMatcher matcher);
  void peptideMatched(byte[] proteinName, int proteinNameOffset, int proteinNameLength, byte[] protein, int proteinOffset, int proteinLength, int sequenceOffset, int sequenceLength, DFAState finState);
}
