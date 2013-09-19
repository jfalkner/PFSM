/*
 * Copyright 2004 Jayson Falkner
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.proteomecommons.pfsm;

import java.util.*;
import java.util.Iterator;
import java.util.Vector;

import org.proteomecommons.jaf.*;
import org.proteomecommons.jaf.util.*;

import org.proteomecommons.jaf.Residue;

public class MSMSNonDeterministicFiniteStateAutonoma {
	// the model used by this search engine
	public MSMSModel model;

	LinkedList autonoma = new LinkedList();

	// array for dynamic translation from char to index
	// saves space in transitions, and time in references
	public static final int[] residueIndex = new int[256];
	static {
		// init the residueIndex
		for (int i = 0; i < residueIndex.length; i++) {
			residueIndex[i] = -1;
		}

		// set up the appropriate index
		residueIndex['A'] = 0;
		residueIndex['C'] = 1;
		residueIndex['D'] = 2;
		residueIndex['E'] = 3;
		residueIndex['F'] = 4;
		residueIndex['G'] = 5;
		residueIndex['H'] = 6;
		residueIndex['I'] = 7;
		residueIndex['K'] = 8;
		residueIndex['L'] = 9;
		residueIndex['M'] = 10;
		residueIndex['N'] = 11;
		residueIndex['P'] = 12;
		residueIndex['Q'] = 13;
		residueIndex['R'] = 14;
		residueIndex['S'] = 15;
		residueIndex['T'] = 16;
		residueIndex['V'] = 17;
		residueIndex['W'] = 18;
		residueIndex['Y'] = 19;		
	}

	public static final char[] indexResidue = new char[20];
	static {
		// init an index to residue matrix
		indexResidue[0] = 'A';
		indexResidue[1] = 'C';
		indexResidue[2] = 'D';
		indexResidue[3] = 'E';
		indexResidue[4] = 'F';
		indexResidue[5] = 'G';
		indexResidue[6] = 'H';
		indexResidue[7] = 'I';
		indexResidue[8] = 'K';
		indexResidue[9] = 'L';
		indexResidue[10] = 'M';
		indexResidue[11] = 'N';
		indexResidue[12] = 'P';
		indexResidue[13] = 'Q';
		indexResidue[14] = 'R';
		indexResidue[15] = 'S';
		indexResidue[16] = 'T';
		indexResidue[17] = 'V';
		indexResidue[18] = 'W';
		indexResidue[19] = 'Y';
	}

	public int getStateCount() {
		return autonoma.size();
	}

	//	optimized public constructor -- resuse cache and model
	public MSMSNonDeterministicFiniteStateAutonoma(MSMSModel model) {
		// reference the model
		this.model = model;
		
	}

	// TODO: this is where memory is being lost....
	public void run() {
		// pool all the links
		ArrayList linkPool = new ArrayList();
		for (Iterator peakIterator = model.peaks.iterator(); peakIterator
				.hasNext();) {
			GreedyPeak peak = (GreedyPeak) peakIterator.next();
			// convert all forward links to finite states
			for (Iterator linkIterator = peak.starts.iterator(); linkIterator
					.hasNext();) {
				// add to the pool
				linkPool.add(linkIterator.next());
				// ditch the link (clean up some space)
				linkIterator.remove();
			}
		}
		
		// sort the links, do local ones first
		Collections.sort(linkPool);
		
		// convert each link in to a proper NDFA
		for (Iterator linkIterator = linkPool.iterator();linkIterator.hasNext();) {
			GreedyPeakLink link = (GreedyPeakLink) linkIterator.next();
			// check for null ends
			if (link.end == null) {
				System.out.println("End link is null?!?"+link.start.massOverChargeInDaltons);
				continue;
			}
			// enumerate all the hits
			for (int hitIndex = 0; hitIndex < link.hits.length; hitIndex++) {
				// add a finite state for each entry
				PermutationGenerator pg = new PermutationGenerator(
						link.hits[hitIndex].residues.length);
				ResidueCombinationCacheEntry hit = link.hits[hitIndex];
				while (pg.hasMore()) {
					// add state for each
					int[] perm = pg.getNext();
					// check that n-terminal/c-terminal mods aren't put
					// elsewhere
					boolean skip = false;
					for (int i = 0; i < perm.length; i++) {
						Residue residue = link.hits[hitIndex].residues[perm[i]];
						// check c-terminus only is valid
						if (residue instanceof CTerminusOnly
								&& i != perm.length - 1) {
							skip = true;
						}
						// check n-terminus only is valid
						if (residue instanceof NTerminusOnly
								&& i != 0) {
							skip = true;
						}
					}
					if (skip) {
						continue;
					}
					// keep track of what state you are in
					NDFAState state = link.start;
					for (int permIndex = 0; permIndex < perm.length - 1; permIndex++) {
						// get the right residue permutation
						Residue residue = link.hits[hitIndex].residues[perm[permIndex]];
						// get the appropriate link for this
						state = state.getState(residue);
					}
					// add a step from the last finite state to the peak
					Residue residue = link.hits[hitIndex].residues[perm[perm.length - 1]];
					// forcefully set the transition to the following
					state.setState(residue, link.end);
				}
			}
		}

		// purge all the old links
		for (Iterator i = autonoma.iterator(); i.hasNext();) {
			NDFAState s = (NDFAState) i.next();
			if (s instanceof GreedyPeak) {
				GreedyPeak gp = (GreedyPeak) s;
				gp.ends = null;
				gp.starts = null;
			}
		}
		
//		// print the converted NDFA
//		System.out.println("NDFA Using Links");
//		HashSet used = new HashSet();
//		LinkedList stack = new LinkedList();
//		stack.add(MSMSModel.nTerminus);
//		while (stack.size()>0){
//			NDFAState s = (NDFAState)stack.removeLast();
//			for (Linker temp = s.links;temp!=null;temp = temp.next){
//				System.out.println(s.id+" ("+temp.r.getName()+")-> "+temp.state.id);
//				if (used.contains(temp.state)){
//					continue;
//				}
//				stack.add(temp.state);
//				used.add(temp.state);
//			}
//		}
//		
//		// print using getStates()
//		System.out.println("NDFA Using getStates()");
//		used.clear();
//		stack.add(MSMSModel.nTerminus);
//		while (stack.size()>0){
//			NDFAState s = (NDFAState)stack.removeLast();
//			for (int i=0;i<MSMSNonDeterministicFiniteStateAutonoma.indexResidue.length;i++){
//				NDFAState[] ss = s.getStates(MSMSNonDeterministicFiniteStateAutonoma.indexResidue[i]);
//				if (ss == null || ss.length ==0){
//					continue;
//				}
//				for (int j=0;j<ss.length;j++){
//					System.out.println(s.id+" ("+MSMSNonDeterministicFiniteStateAutonoma.indexResidue[i]+")-> "+ss[j].id);
//					if (used.contains(ss[j])){
//						continue;
//					}
//					stack.add(ss[j]);
//					used.add(ss[j]);
//				}
//			}
//		}
	}

	//	// helper method to get a valid peptide from a sequence
	//	public Peptide[] getPeptide(String sequence) {
	//		MakePeptideState state = new MakePeptideState();
	//		state.position = 0;
	//		state.state = model.nTerminus;
	//		state.mods = new int[sequence.length()];
	//		state.residues = new Residue[sequence.length()];
	//		// keep a fake stack
	//		Stack stack = new Stack();
	//		stack.push(state);
	//		// go through the whole stack
	//		while (stack.size() > 0){
	//			// pop the top
	//			MakePeptideState mps = (MakePeptideState)stack.pop();
	//			char c = sequence.charAt(mps.position);
	//			ArrayList next = mps.state.next[c];
	//			// check if there are more states to go to
	//			for (int i=0;next != null && i<next.size();i++) {
	//			  // make a new state for each place to go
	//				MakePeptideState temp = new MakePeptideState();
	//				temp.state = (NDFAState)next.get(i);
	//				temp.position = mps.position++;
	//				temp.mods = new Residue[];
	//			}
	//		}
	//		
	//		return null;
	//	}
}

class MakePeptideState {
	NDFAState state = null;

	int position = 0;

	Residue[] residues = null;

	int[] mods = null;
}