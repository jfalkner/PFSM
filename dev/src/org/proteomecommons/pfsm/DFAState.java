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
import org.proteomecommons.pfsm.util.*;

public class DFAState {
	// next state of transition
	public DFAState[] next = new DFAState[MSMSNonDeterministicFiniteStateAutonoma.indexResidue.length];

	// is this a final state
//	public boolean fin = false;

	// reference to set of NDFA states that made this
	//	ArrayList states = null;
	NDFAState[] states = null;

	// for on-the-fly DFA creation
	protected NDFAState[][] ndfaNext = null;

	// reference to the converted (i.e. the dfa state hash and temp state)
//	public NDFAToDFAConverter converter;
private FastNDFAArrayMaker fast;

	// helper to get the state count
	public int getStateCount() {
// TODO: remove this?
		return -1;
//		return converter.dfaStates.size();
	}

	// for on-the-fly state creation
	public DFAState() {
		// get the ndfa array maker from the thread
		fast = FastNDFAArrayMaker.getInstance();
	}

	// helper method to do on-the-fly conversion from NDFA to DFA
	public synchronized DFAState transition(char input) {
		// translate once, reuse
		int path = MSMSNonDeterministicFiniteStateAutonoma.residueIndex[input];
		
		// if the DFA state exists, use it
		if (next[path] != null) {
			return next[path];
		}

		// lazy init the ndfa next
		if (ndfaNext == null) {
			// if the state didn't exist, try to make it
			ndfaNext = new NDFAState[MSMSNonDeterministicFiniteStateAutonoma.indexResidue.length][0];
			//		ndfaNext = new ArrayList[next.length];
			// load each NDFA array entry
			for (int i = 0; i < next.length; i++) {
				//converter.sourcePaths.clear();
				// try each possible source state
				for (int j = 0; j < states.length; j++) {
					NDFAState source = (NDFAState) states[j];
					NDFAState[] temp = source.getStates(MSMSNonDeterministicFiniteStateAutonoma.indexResidue[i]);
					if (temp != null) {
						// add all the possibilities
						fast.add(temp);
					}
				}
				// set the source paths as the array entry
				if (fast.size() > 0) {
					ndfaNext[i] = fast.getStates();
				}
			}
		}

		// if it is an invalid transition, return null
		if (ndfaNext[path] == null
				|| ndfaNext[path].length == 0) {
			return null;
		}

		// just make a new DFA
		DFAState toReturn = new DFAState();
		// set the reference to the DFA's states accordingly
		toReturn.states = ndfaNext[path];
		// reference from the old state
		this.next[path] = toReturn;

		// if nothing matches, return null
		return toReturn;
	}

	public String toString() {
		String string = new String();
		if (states != null) {
			Arrays.sort(states);
			for (int i = 0; i < states.length; i++) {
				string += states[i].id + ",";
			}
		}

		return string;
	}

	// customized equals, plays well with hash functions
	public boolean equals(Object o) {
		// if it is a DFA state, compare NDFA states to check for equality
		if (o instanceof DFAState) {
			DFAState toCheck = (DFAState) o;

			// check they have the same amount of states
			if (toCheck.states.length != states.length) {
				return false;
			}

			// TODO: make this thread safe and we can remove this sync
			synchronized (states) {
				// check each state
				for (int i = 0; i < states.length; i++) {
					if (!toCheck.states[i].equals(states[i])) {
						return false;
					}
				}
			}

			return true;
		}
		// if it is anything else, check object ids
		return super.equals(o);
	}

	// customized hash function
	public int hashCode() {
		long hash = 0;
		synchronized (states) {
			// TODO: why is this check needed here? This array is dynamically
			// set to the right size
			for (int i = 0; i < states.length; i++) {
				NDFAState state = (NDFAState) states[i];
				hash += state.hashCode();
			}
		}
		return (int) hash;
	}

	// helper to check if this is a final state
	public boolean isFin() {
		for (int i = 0; states != null && i < states.length; i++) {
			NDFAState state = (NDFAState) states[i];
			if (state.fin || state instanceof GreedyPeakFin) {
				return true;
			}
		}
		return false;
	}

	// helper to check if this is a final state
	public GreedyPeakFin[] getFinals() {
		Vector fins = new Vector();
		for (int i = 0; states != null && i < states.length; i++) {
			NDFAState state = (NDFAState) states[i];
			if (state instanceof GreedyPeakFin) {
				fins.add(state);
			}
		}
		return (GreedyPeakFin[]) fins.toArray(new GreedyPeakFin[0]);
	}

}