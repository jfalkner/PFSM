package org.proteomecommons.pfsm.util;

import org.proteomecommons.pfsm.*;

/**
 * Simple serial listener for FASTAMatcher instances.
 * @author Jayson Falkner - jfalkner@umich.edu
 */
public interface PFFMatcherListener {
  void searchStarted(PFFMatcher matcher);
  void searchFinished(PFFMatcher matcher);
  void peptideMatched(char[] sequence, int offset, int length, DFAState fin);
}
