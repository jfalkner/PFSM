/*
 *    Copyright 2004 Jayson Falkner
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

import java.util.ArrayList;
import java.util.List;
import java.util.*;

import org.proteomecommons.jaf.*;
import org.proteomecommons.pfsm.util.*;

/**
 * 
 * @author Jayson Falkner - jfalkner@umich.edu
 * 
 * This class converts a Non-Deterministic Finite State Machine in to a
 * Deterministic Finite State Machine. Note, in the worst case this may take
 * O(2^n)!
 */
public class NDFAToDFAConverter {
	public NDFAState NDFA;
	public DFAState DFA;
	public int statesConverted = 0;

	// reference to all the dfa states
	public Hashtable dfaStates;
	// references to the temporary dfa
	protected DFAState tempDFA = new DFAState();

	// references for the DFA.transition() method (saves space)
	// Note: these aren't thread safe!!!
	protected int pathIndex = 0;
	protected int indexToInsert = 0;

	// tried moving this to a hash for speed.
//		protected ArrayList sourcePaths = new ArrayList();
	
	// the fastest method I could think of for speeding up the transition() method.
FastNDFAArrayMaker sourcePaths = null;
	protected NDFAState toInsert;

	//	public static void main(String[] args) throws Exception {
	//		ModelConfiguration config = new ModelConfiguration(
	//				"/root/workspace/PeptideFragmentationModel/standard-config.xml");
	//		String peptide = "GGGGG";
	//		double mass = config.getNTerminus();
	//		for (int i = 0; i < peptide.length(); i++) {
	//			mass += config.getResidue(peptide.charAt(i)).getBaseMass();
	//			System.out.println(mass + "\t1");
	//		}
	//		mass += config.getCTerminus();
	//		System.out.println("Parent: " + mass);
	//	}

	public NDFAToDFAConverter(NDFAState start) {
		NDFA = start;
	}

	// test on-the-fly method
	public void runOnTheFly() {
		dfaStates = new Hashtable(100000);
		// add init places for start
		DFAState DFAStart = new DFAState();
		//		DFAStart.states = new ArrayList();
		DFAStart.states = new NDFAState[1];
		DFAStart.states[0] = NDFA;
		//		dfaStates.put(DFAStart.states, DFAStart); // add the start
		dfaStates.put(DFAStart, DFAStart); // add the start
		DFA = DFAStart;
	}

	public void run() {
		// doesn't work, throw exception
		if (true){
			throw new RuntimeException("Code broken, fix it!");
		}
//		// convert to a DFA
//		Stack stack = new Stack();
//		// make the hash a respectable size
//		dfaStates = new Hashtable(100000);
//		//	ArrayList dfaStates = new ArrayList();
//		// add init places for start
//		DFAState DFAStart = new DFAState();
//		DFAStart.states = new NDFAState[1];
//		DFAStart.states[0] = NDFA; // is thi sneeded ?
//		//		dfaStates.put(DFAStart, DFAStart); // add the start
//		DFA = DFAStart;
//
//		// push the first state
//		stack.push(DFAStart);
//
//		// use the stack
//		while (stack.size() > 0) {
//			statesConverted++;
//			// pop the first NDFA state
//			DFAState state = (DFAState) stack.pop();
//
//			// make a step for each transition
//			for (int i = 0; i < state.next.length; i++) {
//				// if the bucket is null, make it
//				if (sourcePaths == null) {
//					// make the bucket one larger than the current number of NDFA states
//					sourcePaths = new FastNDFAArrayMaker(new NDFAState().id);
//				}
////				sourcePaths.clear();
//				// check each aggregate state
//				for (int j = 0; j < state.states.length; j++) {
//					NDFAState source = (NDFAState) state.states[j];
//
//					
//					if (source.next[i] != null) {
////					 place in order and check for duplicates
//						for (Iterator k=source.next[i].iterator();k.hasNext();){
//							NDFAState toInsert = (NDFAState)k.next();
//							// use the bucket to remove dupes
//							sourcePaths.add(toInsert);
//							
//							
//							// check if it is a duplicate
////							if (sourcePaths.size()>0){
////								// binary search for index
////								indexToInsert = Collections.binarySearch(sourcePaths, toInsert);
////                // if negative, it's not in the list
////								if (indexToInsert <0){
////									indexToInsert += 1;
////									indexToInsert *= -1;
////							  	sourcePaths.add(indexToInsert, toInsert);
////								}
////							}
////							else {
////								sourcePaths.add(toInsert);
////							}
//						}
//					}
//				}
//
//				// if there were any valid steps, push to the stack
//				if (sourcePaths.size() > 0) {
//					// push for each next possible step
//					DFAState toCheck = new DFAState();
//toCheck.states = sourcePaths.getStates();
//					//toCheck.states = (NDFAState[])sourcePaths.toArray(new NDFAState[0]);
//					DFAState checked = (DFAState) dfaStates.get(toCheck);
//					// if we've yet to see this state, use it
//					if (checked == null) {
//						state.next[i] = toCheck;
//						if (toCheck.isFin()) {
//							toCheck.fin = true;
//						}
//						dfaStates.put(toCheck, toCheck);
//						stack.push(toCheck);
//					}
//					// if we've already seen this state, recycle it
//					else {
//						state.next[i] = checked;
//					}
//				}
//			}
//		}
	}

	// method to check if conversion was successfull
	public boolean checkOnTheFly(char[] seq) {
		DFAState state = DFA;
		int index = 0;
		while (state != null && index < seq.length) {
			//			// track how many states are used
			//			if (!state.hit) {
			//				uniqueStatesUsed++;
			//				statesUsed++;
			//				state.hit = true;
			//			}
			//			else {
			//				statesUsed++;
			//			}

			state = state.transition(seq[index]);
			index++;
		}
		//		System.out.println("");
		if (index == seq.length && state != null && state.isFin()) {
			return true;
		}

		return false;
	}

	// method to check if conversion was successfull
	public boolean check(char[] seq) {
		DFAState state = DFA;
		int index = 0;
		while (state != null && index < seq.length) {
			//			// track how many states are used
			//			if (!state.used) {
			//				uniqueStatesUsed++;
			//				statesUsed++;
			//				state.used = true;
			//			}
			//			else {
			//				statesUsed++;
			//			}

			//			System.out.print(seq[index]);
			state = state.next[seq[index] - 'A'];
			index++;
		}
		//		System.out.println("");
		if (index == seq.length && state != null && state.isFin()) {
			return true;
		}

		return false;
	}

	// check if a state is unique
	private DFAState isUnique(ArrayList a, Hashtable b) {
		// remove dupes
		for (int i = a.size() - 1; i > 0; i--) {
			NDFAState aa = (NDFAState) a.get(i);
			for (int j = i - 1; j > -1; j--) {
				if (aa.equals(a.get(j))) {
					a.remove(j);
					i--;
				}
			}
		}

		return (DFAState) b.get(a);
	}

}

class ConversionState {
	DFAState a;
	ArrayList b;
	int index = 0;

}