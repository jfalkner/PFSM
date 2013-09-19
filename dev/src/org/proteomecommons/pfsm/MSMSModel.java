/*
 *    Copyright 2005 University of Michigan
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
package org.proteomecommons.pfsm;

import java.util.*;

import org.proteomecommons.io.*;
import org.proteomecommons.jaf.*;
import org.proteomecommons.jaf.util.*;

/**
 * A Java implementation of Peptide Finite State Machines (PFSM) models as
 * described in Falkner and Andrew's 2005 bioinformatics paper.
 * 
 * @author Jayson Falkner - jfalkner@umich.edu
 *  
 */
public class MSMSModel {
	// the model used by this search engine
	private Configuration config;

	// reference to the peak list that is being used
	public TandemPeakList peaklist;

	// TODO: this is static so that merged states consolidate themselves. it is
	// a good feature, but a bad implementation. fix it!
	public static GreedyPeak nTerminus;

	public static boolean useB = true;

	public static boolean useY = true;

	public static boolean mergePairs = true;

	public static boolean trimWorsePaths = false;

	public static boolean onlyConsiderFullPeptides = true;

	public GreedyPeak cTerminus;

	// reference to every state in the autonoma
	public Vector peaks = new Vector();

	// keep track of model graph

	// some constants
	ResidueCombinationCache cache;

	//	optimized public constructor -- resuse cache and model
	public MSMSModel(Configuration config, ResidueCombinationCache cache,
			TandemPeakList peaklist) throws Exception {
		// make the model
		this.config = config;
		// reference the peak list
		this.peaklist = peaklist;
		// make the cache
		this.cache = cache;
		// build the spectrum graph
		run();
	}

	private void run() throws Exception {
		// reference the peaks
		Peak[] peaks = peaklist.getPeaks();

		// list of peaks to make in to a NDFA
		ArrayList nSeriesPeaks = new ArrayList();

		// required n-terminus fragments (non-trypsin specific)
		if (nTerminus == null) {
			nTerminus = new GreedyPeak(0, config.getNTerminus());
		}
		nSeriesPeaks.add(nTerminus);

		// add the peak series of interest
		for (int i = 0; i < peaks.length; i++) {
			// conditionally consider y-ions
			if (useY) {
				// mesh y-ions
				GreedyPeak y = new GreedyPeak(peaks[i].getIntensity(), peaklist
						.getParent().getMassOverCharge()
						- peaks[i].getMassOverCharge()
						+ Atom.H.getMassInDaltons());
				nSeriesPeaks.add(y);
			}

			// conditionally consider b-ions
			if (useB) {
				// make/add b ions
				GreedyPeak b = new GreedyPeak(peaks[i].getIntensity(),
						peaks[i].getMassOverCharge());
				nSeriesPeaks.add(b);
			}
		}

		// make a greedy peak with a reference to its peak list
		// subtract a proton, this is the b-series
		GreedyPeakFin nTermParent = new GreedyPeakFin(peaklist, 0, peaklist
				.getParent().getMassOverCharge()
				- config.getCTerminus() - Atom.H.getMassInDaltons());
		nSeriesPeaks.add(nTermParent);
		cTerminus = nTermParent;

		// sort the collection
		Collections.sort(nSeriesPeaks);

		// coditionally trim out overlaps of ion series
		if (mergePairs) {
			// trim b/y pairs (saves lots of memory) -- make surrogate peak
			for (int i = 0; i < nSeriesPeaks.size() - 1; i++) {
				GreedyPeak a = (GreedyPeak) nSeriesPeaks.get(i);
				GreedyPeak b = (GreedyPeak) nSeriesPeaks.get(i + 1);
				// check if the peaks fall within the closest ppm
				if (Math.abs(a.massOverChargeInDaltons
						- b.massOverChargeInDaltons) < config
						.getMaxMassAccuracyError((a.massOverChargeInDaltons + b.massOverChargeInDaltons) / 2)) {
					// add a surrogate peak
					double intensity = (a.intensity + b.intensity) / 2;
					double mz = (a.massOverChargeInDaltons + b.massOverChargeInDaltons) / 2;
					GreedyPeak p = new GreedyPeak(intensity, mz);
					nSeriesPeaks.add(i, p);
					// remove the old peaks
					nSeriesPeaks.remove(a);
					nSeriesPeaks.remove(b);
				}
			}
		}

		// convert to an array
		GreedyPeak[] greedyPeaks = (GreedyPeak[]) nSeriesPeaks
				.toArray(new GreedyPeak[0]);

		// stack for the greedy states
		ArrayList validHits = new ArrayList();
		for (int i = greedyPeaks.length - 1; i > -1; i--) {
			for (int j = i + 1; j < greedyPeaks.length; j++) {
				// only check as far as is needed
				if (greedyPeaks[j].massOverChargeInDaltons > greedyPeaks[i].massOverChargeInDaltons
						+ cache.getLargestMass()) {
					break;
				}

				// find any possible matches.
				double mass = greedyPeaks[j].massOverChargeInDaltons
						- greedyPeaks[i].massOverChargeInDaltons;
				// if this is a valid start, put it on the stack
				ResidueCombinationCacheEntry[] hits = cache.getCache(mass,
						config.getMassAccuracyPPM());
				boolean nTerminus = greedyPeaks[i].equals(this.nTerminus);
				boolean cTerminus = greedyPeaks[j].equals(nTermParent);
				validHits.clear();
				if (hits != null && hits.length > 0) {

					if (trimWorsePaths) {
						// make sure this isn't making a long, redundant path
						// -TODO:
						// does this speed things up? Make this more
						// intelligent, don't trim all. If we trim out all paths, this starts to nuke peptides...
						boolean worsePath = false;
						for (int hitIndex = 0; hitIndex < hits.length
								&& hits[hitIndex].residues.length > 2; hitIndex++) {
							if (greedyPeaks[j].isConnectedTo(greedyPeaks[i],
									hits[hitIndex].residues.length)) {
								worsePath = true;
								break;
							}
						}
						if (worsePath) {
							continue;
						}
					}

					// add each hit
					for (int k = 0; k < hits.length; k++) {
						// assume cache is good
						boolean skip = false;
						for (int l = 0; l < hits[k].residues.length; l++) {
							// skip invalid termini
							if ((hits[k].residues[l] instanceof CTerminusOnly && !cTerminus)
									|| (hits[k].residues[l] instanceof NTerminusOnly && !nTerminus)) {
								skip = true;
								break;
							}
						}
						if (skip) {
							continue;
						}
						// add it as a hit
						validHits.add(hits[k]);

					}
				}
				// if there were any valid, keep them
				if (validHits.size() > 0) {
					ResidueCombinationCacheEntry[] goodHits = (ResidueCombinationCacheEntry[]) validHits
							.toArray(new ResidueCombinationCacheEntry[0]);
					GreedyPeakLink gpl = new GreedyPeakLink(greedyPeaks[i],
							greedyPeaks[j], goodHits);
					greedyPeaks[i].starts.add(gpl);
					greedyPeaks[j].ends.add(gpl);
				}
			}
		}

		// put all the peaks in a list
		LinkedList nSeries = new LinkedList();
		for (int i = 0; i < greedyPeaks.length; i++) {
			nSeries.add(greedyPeaks[i]);
		}

		// conditional graph trimming if we are looking for full peptide
		if (onlyConsiderFullPeptides) {
			// trim out forward peaks without links
			for (int i = nSeries.size() - 1; i > -1; i--) {
				GreedyPeak gp = (GreedyPeak) nSeries.get(i);
				// if it isn't a parent peak, nuke it
				if (gp.equals(nTerminus)) {
					continue;
				}
				if (gp.ends.size() == 0) {
					nSeries.remove(i);
					// remove everything that used this peak as start
					for (int j = 0; j < gp.starts.size(); j++) {
						GreedyPeakLink gpl = (GreedyPeakLink) gp.starts.get(j);
						GreedyPeak badEnd = gpl.end;
						badEnd.ends.remove(gpl);
					}
				}
			}

			// Trim out paths that dont' reach an fin state. This saves memory.
			for (int i = nSeries.size() - 1; i > -1; i--) {
				GreedyPeak gp = (GreedyPeak) nSeries.get(i);
				// if this is a valid end peak, skip it -- don't remove
				// c-termius
				if (gp instanceof GreedyPeakFin || gp.fin
						|| gp.equals(nTermParent)) {
					continue;
				}
				// if this peak is a dead end, remove it
				if (gp.starts.size() == 0) {
					// remove the bad end peak
					nSeries.remove(gp);
					// remove every link that used this parent
					for (int j = 0; j < gp.ends.size(); j++) {
						// get the linker that connects to this peak
						GreedyPeakLink gpl = (GreedyPeakLink) gp.ends.get(j);
						// find the starting peak
						GreedyPeak badStart = gpl.start;
						// remove the bad link from the starts
						badStart.starts.remove(gpl);
					}
				}
			}
		}
		// if tags should be considered - TODO: fix sequence tags later
		//		else {
		//			// if a state has enough depth back, label it a fin
		//			for (int i = 0; i < nSeries.size(); i++) {
		//				GreedyPeak gp = (GreedyPeak) nSeries.get(i);
		//				if (gp.checkBackwardDepth(config.minResidueLength)) {
		//					gp.fin = true;
		//				}
		//			}
		//
		//			// if a state has enough depth, make it a start
		//			for (int i = 0; i < nSeries.size(); i++) {
		//				GreedyPeak gp = (GreedyPeak) nSeries.get(i);
		//				if (gp.checkForwardDepth(config.minResidueLength)) {
		//					// merge with start state
		//					for (int j = 0; j < gp.next.length; j++) {
		//						// skip nulls
		//						if (gp.next[j] != null) {
		//							// null check
		//							if (nTerminus.next[j] == null) {
		//								nTerminus.next[j] = new HashSet();
		//							}
		//							nTerminus.next[j].addAll(gp.next[j]);
		//						}
		//					}
		//				}
		//			}
		//		}

		// we're done using the cache, de-reference it
		this.cache = null;

		// set the peaks
		this.peaks.addAll(nSeries);
		// label the c-terminus as the end
		cTerminus.fin = true;
	}

	// helper to get the number of valid peaks
	public int getNodeCount() {
		return peaks.size();
	}

	// helper to get the count of links
	public int getArcCount() {
		int arcCount = 0;
		for (int i = 0; i < peaks.size(); i++) {
			GreedyPeak peak = (GreedyPeak) peaks.get(i);
			arcCount += peak.starts.size();
		}
		return arcCount;
	}

	public void printNDFA() {
		for (Iterator it = peaks.iterator(); it.hasNext();) {
			GreedyPeak peak = (GreedyPeak) it.next();
			System.out.println(peak.id + ", mass: "
					+ peak.massOverChargeInDaltons);
			for (Iterator pit = peak.starts.iterator(); pit.hasNext();) {
				GreedyPeakLink gpl = (GreedyPeakLink) pit.next();
				System.out.println("  " + gpl.toString() + " -> " + gpl.end.id);
			}
		}
	}
}