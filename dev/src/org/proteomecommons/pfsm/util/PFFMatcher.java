/*
 *    Copyright 2004 University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.proteomecommons.pfsm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.proteomecommons.io.*;
import org.proteomecommons.jaf.*;
import org.proteomecommons.jaf.residues.CysteineIodoaceticAcidDerivative;
import org.proteomecommons.jaf.util.*;
import org.proteomecommons.pff.*;
import org.proteomecommons.pfsm.*;

/**
 * A search engine that uses Peptide Finite State Machines (PFSM) and FASTA
 * sequence files.
 * 
 * @author Jayson Falkner - jfalkner@umich.edu
 *  
 */
public class PFFMatcher {
	// list of registered listeners -- stream, don't buffer, hits
	private LinkedList searchListeners = new LinkedList();

	// general purpose buffer
	private final byte[] buffer = new byte[512 * 1000];

	// the encoding
	private CompressionEncoding ce = null;

	/**
	 * Adds the given listener to the list of listeners for this class.
	 * 
	 * @param l
	 *            The listener to add.
	 */
	public void addPFFMatcherListener(PFFMatcherListener l) {
		this.searchListeners.add(l);
	}

	/**
	 * Removes a listener from this matcher.
	 * 
	 * @param l
	 */
	public void removePFFMatcherListener(PFFMatcherListener l) {
		this.searchListeners.remove(l);
	}

	/**
	 * Gets all the listeners for this matcher.
	 * 
	 * @return
	 */
	public PFFMatcherListener[] getPFFMatcherListeners() {
		return (PFFMatcherListener[]) searchListeners
				.toArray(new PFFMatcherListener[0]);
	}

	public void setCompressionEncoding(CompressionEncoding ce) {
		this.ce = ce;
	}

	public CompressionEncoding getCompresionEncoding() {
		return ce;
	}

	/**
	 * Entry point for demos of the code.
	 * 
	 * @param args
	 *            Arguments for the program, try "--help"
	 */
	public static void main(String[] args) {
		try {
			if (args.length < 1) {
				System.out.println("PFSM PFF Search\n");
				System.out
						.println("Usage: java PFFMatcher <regex> <pff file>");
				System.out
						.println("\nPlease direct feedback to Jayson Falkner: jfalkner@umich.edu");
				return;
			}

			// time the whole process
			long startTotalTime = System.currentTimeMillis();

			// make a basic config
			Configuration config = new Configuration();
			config.setMassAccuracyPPM(200);
			config.setMaxIonSeriesGap(3);

			//			 flag off peak joining
			//			MSMSModel.mergePairs = false;
			//						MSMSModel.useY = false;

			System.out.print("Loading Cache (max gap size "
					+ config.getMaxIonSeriesGap() + "):");
			long startCache = System.currentTimeMillis();

			// use all the common residues, plus carbamidomidomethylation
			CommonResidue[] commonResidues = GenericResidue.getCommonResidues();
			Residue[] residues = new Residue[commonResidues.length + 1];
			System.arraycopy(commonResidues, 0, residues, 0,
					commonResidues.length);
			residues[residues.length - 1] = new CysteineIodoaceticAcidDerivative();

			// create the cache for the desired residues
			ResidueCombinationCache cache = new ResidueCombinationCache(config
					.getMaxIonSeriesGap(), residues);
			long endCache = System.currentTimeMillis();
			System.out.println((endCache - startCache) + "ms");

			// fake DFA state for now -- make this a real one
			DFAState dfa = null;

			// consider the arguments as regular expressions
			Pattern p = Pattern.compile(args[0]);
			LinkedList peaklists = new LinkedList();

			// try all files in the local directory
			String[] possibleFiles = new File(".").list();
			System.out.println("Trying " + possibleFiles.length
					+ " files, regexp: " + args[0]);
			for (int i = 0; i < possibleFiles.length; i++) {
				Matcher m = p.matcher(possibleFiles[i]);
				if (m.matches()) {
					try {
						PeakListReader plr = GenericPeakListReader
						.getPeakListReader(possibleFiles[i]);
						PeakList peaklist = plr.getPeakList();
						peaklists.add(peaklist);
						System.out.println("Using peak list "
								+ possibleFiles[i]);
					} catch (Exception e) {
						System.out.println("Skipping " + possibleFiles[i]
								+ " can't parse the peak list.");
					}
				}
			}

			// make a DFA from the peak lists
			dfa = FASTAMatcher.mergePeakLists((PeakList[]) peaklists
					.toArray(new PeakList[0]), config, cache);

			// make a new search
			PFFMatcher search = new PFFMatcher();

			// search for matches
			// args 2 is the fasta file
			File fastaFile = new File(args[1]);
			FileInputStream fis = new FileInputStream(fastaFile);
			// use first byte to check encoding
			fis.read();
			// parse the encoding
			CompressionEncoding ce = CompressionEncoding.loadEncoding(fis,
					false);
			search.setCompressionEncoding(ce);
			// search the rest of the file
			search.addPFFMatcherListener(new ConsolePFFMatcherListener());
			search.matchPeptides(dfa, fis);
			fis.close();

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Search a FASTA file.
	 */
	public synchronized void matchPeptides(DFAState DFA, InputStream pff) {
		// check if it is null
		if (DFA == null || pff == null) {
			return;
		}

		// track how many peptides were matched per protein
		int matched = 0;

		// notify listeners of a start
		for (Iterator it = searchListeners.iterator(); it.hasNext();) {
			PFFMatcherListener listener = (PFFMatcherListener) it.next();
			listener.searchStarted(this);
		}

		byte[] buf = new byte[100000];
		DFAState[] transitions = new DFAState[1000];
		char[] sequence = new char[transitions.length];
		transitions[0] = DFA;
		int transitionIndex = 1;

		try {
			for (int bufLimit = pff.read(buf); bufLimit > 0; bufLimit = pff
					.read(buf)) {

				// parse through and read the PFF
				// update sequence stats
				for (int bufIndex = 0; bufIndex < bufLimit; bufIndex++) {
					int b = 0xFF & buf[bufIndex];
					// handle move backs
					if (b <= ce.maxMoveBack) {
						// get the move back offset
						int moveBack = b;
						transitionIndex -= moveBack;

						//check that the move-back wasn't too far
						if (transitionIndex < 1) {
							transitionIndex = 1;
						}
//						System.out.println("(-"+moveBack+")");
					}
					// decode the sequence
					else {
						// handle multi-length
						for (int seqIndex = 0; seqIndex < ce.decode[b].length; seqIndex++) {
							// check if it is a moveback
							if (ce.decode[b][seqIndex] > ce.maxMoveBack) {
								// update the sequence
								sequence[transitionIndex] = (char) ce.decode[b][seqIndex];
//								System.out.print(sequence[transitionIndex]);
								// update transitions
								if (transitions[transitionIndex - 1] != null) {
									transitions[transitionIndex] = transitions[transitionIndex - 1]
											.transition((char) ce.decode[b][seqIndex]);
									// check for peptide match
									if (transitions[transitionIndex]!=null&&transitions[transitionIndex].isFin()) {
										// fire off the finish event
										for (Iterator it = searchListeners
												.iterator(); it.hasNext();) {
											PFFMatcherListener l = (PFFMatcherListener) it
													.next();
											l
													.peptideMatched(
															sequence,
															1,
															transitionIndex,
															transitions[transitionIndex]);
										}
									}
								} else {
									transitions[transitionIndex] = null;
								}

								// move to the next slot
								transitionIndex++;
							}
							// handle move backs
							else {
								// get the move back offset
								transitionIndex -= ce.decode[b][seqIndex];
								if (transitionIndex < 1) {
									transitionIndex = 1;
								}
//								System.out.println("(-"+ce.decode[b][seqIndex]+")");
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// notify listeners of a finish
		for (Iterator it = searchListeners.iterator(); it.hasNext();) {
			PFFMatcherListener listener = (PFFMatcherListener) it.next();
			listener.searchFinished(this);
		}
	}
}