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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.proteomecommons.io.*;
import org.proteomecommons.io.filter.*;
import org.proteomecommons.jaf.*;
import org.proteomecommons.jaf.residues.*;
import org.proteomecommons.jaf.util.*;
import org.proteomecommons.pfsm.*;

/**
 * A search engine that uses Peptide Finite State Machines (PFSM) and FASTA
 * sequence files.
 * 
 * @author Jayson Falkner - jfalkner@umich.edu
 *  
 */
public class FASTAMatcher {
	// list of registered listeners -- stream, don't buffer, hits
	private LinkedList searchListeners = new LinkedList();

	// general purpose buffer
	private final byte[] buffer = new byte[512 * 1000];

	/**
	 * Adds the given listener to the list of listeners for this class.
	 * 
	 * @param l
	 *            The listener to add.
	 */
	public void addFASTAMatcherListener(FASTAMatcherListener l) {
		this.searchListeners.add(l);
	}

	/**
	 * Removes a listener from this matcher.
	 * 
	 * @param l
	 */
	public void removeFASTAMatcherListener(FASTAMatcherListener l) {
		this.searchListeners.remove(l);
	}

	/**
	 * Gets all the listeners for this matcher.
	 * 
	 * @return
	 */
	public FASTAMatcherListener[] getFASTAMatcherListeners() {
		return (FASTAMatcherListener[]) searchListeners
				.toArray(new FASTAMatcherListener[0]);
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
				System.out.println("PFSM FASTA Search\n");
				System.out
						.println("Usage: java FASTASequenceMatcher <config file>");
				System.out
						.println("\nPlease direct feedback to Jayson Falkner: jfalkner@umich.edu");
				return;
			}

			// time the whole process
			long startTotalTime = System.currentTimeMillis();

			// make a basic config
			Configuration config = new Configuration();
			config.setMassAccuracyPPM(20);
			config.setMaxIonSeriesGap(3);

			//			 flag off peak joining
			//			MSMSModel.mergePairs = false;
			//						MSMSModel.useY = false;

			System.out.print("Loading Cache (max gap size "
					+ config.getMaxIonSeriesGap() + "):");
			long startCache = System.currentTimeMillis();

			// use all the common residues, plus carbamidomidomethylation
			CommonResidue[] commonResidues = GenericResidue.getCommonResidues();
			Residue[] residues = new Residue[commonResidues.length+3];
			System.arraycopy(commonResidues, 0, residues, 0,
					commonResidues.length);
			// add in the default modifications
			for (int i=0;i<residues.length;i++){
//				// oxidize the methionine
//				if(residues[i].equals(Residue.M)){
//			      residues[i] = new MethionineOxidized();
//				}
				// block the cysteine
				if(residues[i] != null && residues[i].equals(Residue.C)){
			      residues[i] = new CysteineIodoaceticAcidDerivative();
				}
			}
			residues[residues.length - 1] = new MethionineOxidized();
			residues[residues.length - 2] = new PyroglutamineFromGlutamine();
			residues[residues.length - 3] = new PyroglutamineFromGlutamicAcid();

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
						
						// if not tandem, skip
						if (peaklist instanceof TandemPeakList){
							System.out.println("Skipping "+possibleFiles[i]+" unknown precursor m/z.");
							continue;
						}
						
						// keep peaks only that are below the parent
						KeepOnlyBelowParentIon below = new KeepOnlyBelowParentIon();
						peaklist = below.filter(peaklist);
						
						peaklists.add(peaklist);
//						System.out.println("Using peak list "
//								+ possibleFiles[i]);
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
			FASTAMatcher search = new FASTAMatcher();

			// search for matches
			// args 2 is the fasta file
			File fastaFile = new File(args[1]);
			FileInputStream fis = new FileInputStream(fastaFile);
			search.addFASTAMatcherListener(new ConsoleFASTAMatcherListener());
			search.matchPeptides(dfa, fis);
			fis.close();

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Helper method to merge peak lists.
	 * @param peakListFiles
	 * @param config
	 * @param cache
	 * @return
	 */
	public static DFAState mergePeakLists(PeakList[] peakListFiles,
			Configuration config, ResidueCombinationCache cache) {
		// keep track of all the models
		ArrayList graphs = new ArrayList();

		// merge the peaklist
		for (int i = 0; i < peakListFiles.length; i++) {
			try {
				PeakList peaklist = peakListFiles[i];

				// make the search engine
//				PeakList fpl = PeakList.filterReduceToSingleCharge(peaklist);
				// minimize memory
				MSMSModel m = new MSMSModel(config, cache, (TandemPeakList)peaklist);
				//				m.printNDFA();
				MSMSNonDeterministicFiniteStateAutonoma gse = new MSMSNonDeterministicFiniteStateAutonoma(
						m);
				gse.run();
				graphs.add(gse.model.nTerminus);
			} catch (Exception e) {
				System.err.println("Skipping " + peakListFiles[i]
						+ ", can't read the file.");
				e.printStackTrace();
			}
		}
		Runtime runtime = Runtime.getRuntime();

		// if there are no graphs, return null
		if (graphs.size() == 0) {
			return null;
		}

		// convert to one DFA
		NDFAToDFAConverter dfa = new NDFAToDFAConverter((NDFAState) graphs
				.get(0));
		// always run on-the-fly
		dfa.runOnTheFly();

		// return the starting state
		return dfa.DFA;
	}

	/**
	 * Search a FASTA file.
	 */
	public synchronized void matchPeptides(DFAState DFA, InputStream fasta) {
		// check if it is null
		if (DFA == null || fasta == null) {
			return;
		}

		// track how many peptides were matched per protein
		int matched = 0;

		// notify listeners of a start
		for (Iterator it = searchListeners.iterator(); it.hasNext();) {
			FASTAMatcherListener listener = (FASTAMatcherListener) it.next();
			listener.searchStarted(this);
		}

		// keep track of where the protein is in the file
		long sequenceOffset = 0;

		// keep track of the state
		DFAState state = DFA;

		// buffer the protein
		byte[] proteinName = new byte[10000];
		int proteinNameIndex = 0;
		byte[] proteinSequence = new byte[1000000];
		int proteinSequenceIndex = 0;
		// flag for buffered protein
		boolean bufferedProtein = false;

		//search through the whole FASTA file
		try {
			// move i out here so that the buffer won't skip possible
			// peptides
			int i = 0;
			Runtime runtime = Runtime.getRuntime();
			// flag for skipping a sequence
			boolean isSequence = false;
			// the sequence to read
			int bytesRead = 0;
			// make the input stream
			//			RandomAccessFile fasta = new RandomAccessFile(fastaFile, "r");
			for (bytesRead = fasta.read(buffer); bytesRead != -1; bytesRead = fasta
					.read(buffer)) {
				// do a reality check on the heap - TODO: there must be a
				// better
				// way to do this
				if (runtime.freeMemory() < runtime.totalMemory() / 10) {
					// warn the user
					//					System.out.println("\nWarning! The Java Virtual Machine
					// (JVM) just ran out of memory. This either means your
					// computer needs more RAM, or that you need to allocate
					// more memory to the JVM.");

					freeMemory(DFA);
				}

				// try all characters in the protein
				for (i = 0; i < bytesRead; i++) {
					// handle low memory situations
					try {
						// skip all of the header
						if (!isSequence) {
							// check for end of the header
							if (buffer[i] == '\n') {
								isSequence = true;
								continue;
							}
							// add the letter
							proteinName[proteinNameIndex] = buffer[i];
							proteinNameIndex++;
							continue;
						} else {
							// buffer sequence
							if (buffer[i] == '>') {
								// flag the end of the protein
								bufferedProtein = true;
							} else {
								// don't consider invalid characters
								if (MSMSNonDeterministicFiniteStateAutonoma.residueIndex[buffer[i]] == -1) {
									continue;
								}
								// add the char to the sequence
								proteinSequence[proteinSequenceIndex] = buffer[i];
								proteinSequenceIndex++;
								continue;
							}
						}

						// analyze the buffered protein
						analyzeProtein(DFA, proteinName, proteinNameIndex,
								proteinSequence, proteinSequenceIndex);

						// reset the buffered protein
						proteinNameIndex = 1;
						proteinSequenceIndex = 0;
						bufferedProtein = false;
						isSequence = false;
					} catch (OutOfMemoryError o) {
						// warn the user
						//						System.out.println("\nWarning! The Java Virtual
						// Machine (JVM) just ran out of memory. This either
						// means your computer needs more RAM, or that you need
						// to allocate more memory to the JVM.");

						// try to clear up some memory
						freeMemory(DFA);
						break;
					}
				}
			}
			// analyze the buffered protein
			analyzeProtein(DFA, proteinName, proteinNameIndex, proteinSequence,
					proteinSequenceIndex);
		} catch (Exception e) {
			// noop?
			e.printStackTrace();
		}

		// clear out the buffer of DFA states, i.e. free up some memory
		freeMemory(DFA);

		// notify listeners of a finish
		for (Iterator it = searchListeners.iterator(); it.hasNext();) {
			FASTAMatcherListener listener = (FASTAMatcherListener) it.next();
			listener.searchFinished(this);
		}
	}

	/**
	 * Helper method to analyze a protein.
	 * 
	 * @param DFA
	 *            The DFA representing the valid peptides.
	 * @param proteinName
	 *            byte array representing the protein's name
	 * @param proteinNameIndex
	 *            how far in the array to use.
	 * @param proteinSequence
	 *            byte array representing the protein's sequence
	 * @param proteinSequenceIndex
	 *            how far in the sequence array to use
	 */
	private void analyzeProtein(DFAState DFA, byte[] proteinName,
			int proteinNameIndex, byte[] proteinSequence,
			int proteinSequenceIndex) {

		DFAState state = null;
		// analyze the buffered protein
		for (int j = 0; j < proteinSequenceIndex; j++) {
			// reset the state
			state = DFA;

			for (int k = j; state != null && k < proteinSequenceIndex; k++) {
				// do the transition
				state = state.transition((char) proteinSequence[k]);
//				System.out.print((char)proteinSequence[k]);

				// check if we've hit the end
				if (state != null && state.isFin()) {
					// notify listeners
					for (Iterator it = searchListeners.iterator(); it.hasNext();) {
						FASTAMatcherListener l = (FASTAMatcherListener) it
								.next();
						l.peptideMatched(proteinName, 0, proteinNameIndex,
								proteinSequence, 0, proteinSequenceIndex, j, k
										- j + 1, state);
					}
				}

			}
//			System.out.println();
		}
	}

	/**
	 * A helper method that attempts to free up any available memory. TODO: have
	 * this method notify listeners.
	 * 
	 * @return The amount of memory made available, in bytes.
	 */
	private long freeMemory(DFAState DFA) {
		// reference the runtime
		Runtime runtime = Runtime.getRuntime();
		// figure how much memory was left
		long freeMemoryBefore = runtime.freeMemory();

		// reset the transition table
		DFA.next = new DFAState[MSMSNonDeterministicFiniteStateAutonoma.indexResidue.length];

		// force garbage collection
		System.gc();

		// figure out how much memory is available
		long freeMemoryAfter = runtime.freeMemory();
		//		System.out.println("Space saved: "+ (freeMemoryAfter -
		// freeMemoryBefore));
		// return the difference
		return freeMemoryAfter - freeMemoryBefore;
	}
}