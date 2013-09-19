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
import org.proteomecommons.jaf.*;

public class NDFAState implements Comparable {
	// array of states and char index
	StatesLink[] transitions = null;

	Linker links = null;

	// is this the final state
	boolean fin = false;

	// state number (for fast comparision later)
	public int id;

	// reference number
	private static int num = 0;

	public NDFAState() {
		// assign an id from the current num
		id = getID();
	}

	private synchronized int getID() {
		num++;
		return num;
	}

	public int compareTo(Object o) {
		NDFAState a = (NDFAState) o;
		if (a.id > id) {
			return -1;
		}
		if (a.id < id) {
			return 1;
		}
		return 0;
	}

	// helper to traverse the NDFA
	public NDFAStateTransition[] getTransitions() {
		// array of transitions
		LinkedList trans = new LinkedList();

		for (Linker temp = links; temp != null; temp = temp.next) {
			// get the current reside
			Residue residue = temp.r;
			LinkedList ll = new LinkedList();
			ll.add(temp.state);
			// get all of the same
			while (temp.next != null && temp.next.r.equals(residue)) {
				temp = temp.next;
			}
			// make the transition
			NDFAStateTransition nst = new NDFAStateTransition((NDFAState[]) ll
					.toArray(new NDFAState[0]), residue);
			// add to the group
			trans.add(nst);
		}
		return (NDFAStateTransition[]) trans
				.toArray(new NDFAStateTransition[0]);
	}

	// helper method to make transitions from the link of linkers
	private synchronized void makeTransitions() {
		// handle sync issues
		if (transitions != null) {
			return;
		}
		// handle null issues
		if (links == null) {
			transitions = new StatesLink[0];
			return;
		}

		// make the array - TODO:reside this according to residue # in config
		LinkedList newTrans = new LinkedList();

		// make a transition for each residue type in the linkers
		LinkedList matches = new LinkedList();
		CommonResidue[] commonResidues = GenericResidue.getCommonResidues();
		for (int i = 0; i < commonResidues.length; i++) {
			// clear old matches
			matches.clear();

			// count how many of that residue exist
			for (Linker temp = links; temp != null; temp = temp.next) {
				// if it matches, add it
				if (temp.r.getFASTAChar() == commonResidues[i]
						.getFASTAChar()) {
					matches.add(temp.state);
				}
			}

			// finalize the StatesLink entry
			//newTrans[MSMSNonDeterministicFiniteStateAutonoma.residueIndex[Residue.commonResidues[i].getFASTAChar()]]
			// = new StatesLink(Residue.commonResidues[i].getFASTAChar(),
			// (NDFAState[])matches.toArray(new NDFAState[0]));
			if (matches.size() == 0) {
				continue;
			}
			newTrans.add(new StatesLink(commonResidues[i]
					.getFASTAChar(), (NDFAState[]) matches
					.toArray(new NDFAState[0])));
		}

		// set the transitions
		transitions = (StatesLink[]) newTrans.toArray(new StatesLink[0]);
	}

	// helper method to get states given a symbol
	public NDFAState[] getStates(char c) {
		// if null, make the states
		if (transitions == null) {
			// make them!!
			makeTransitions();
		}

		// find and return the transitions -- TODO: if you sort these, it'll go
		// faster
		for (int i = 0; i < transitions.length; i++) {
			// if the residue matches, return it.
			if (transitions[i].c == c) {
				return transitions[i].states;
			}
		}
		return null;
	}

	// helper method to set the states forcefully. throws an exception if states
	// already exist.
	public void setStates(char c, NDFAState[] states) {
		// if states for the given char are already set, throw an exception
		for (int i = 0; transitions != null && i < transitions.length; i++) {
			if (transitions[i].c == c) {
				throw new RuntimeException("You can only set states once!");
			}
		}

		// if null, make it
		if (transitions == null) {
			// increase the states size by 1
			StatesLink[] newTransitions = new StatesLink[1];
			newTransitions[0] = new StatesLink(c, states);
			// set the new trans
			transitions = newTransitions;
		}
		// if not, put it in alphabetical order
		else {
			// increase the states size by 1
			StatesLink[] newTransitions = new StatesLink[transitions.length + 1];
			int i = 0;
			// fill before
			for (; i < transitions.length && transitions[i].c < c; i++) {
				newTransitions[i] = transitions[i];
			}
			// fill next
			newTransitions[i] = new StatesLink(c, states);
			// fill after
			for (; i < transitions.length; i++) {
				newTransitions[i + 1] = transitions[i];
			}
			//set the new trans
			transitions = newTransitions;
		}
	}

	// helper method to forcefully set a state, exception is thrown if the state
	// already exists.
	public void setState(Residue r, NDFAState state) {
		// forcefully make the link
		Linker link = new Linker(r, state);

		// if no links, make this the first
		if (links == null) {
			// set this as the state
			links = link;
			return;
		}

		// if less than fi, insert before
		if (links.r.getFASTAChar() > r.getFASTAChar()) {
			// set this next ref to the old
			link.next = links;
			// set the old to this
			links = link;
			return;
		}

		// if there are links, put in alphabetically
		for (Linker temp = links; temp != null; temp = temp.next) {
			// if null, add this to the end
			if (temp.next == null) {
				// set this next ref to the old
				temp.next = link;
				// reset the temp link
				temp = null;
				return;
			}

			// TODO: figure out a way to consolidate this...same states don't
			// need to be repeated
			// if the same, return the match (cache!)
			if (temp.r.getFASTAChar() == r.getFASTAChar()
					&& !temp.state.equals(state)) {
				// check that the other state isn't after
				for (Linker nextCheck = temp; nextCheck != null; nextCheck = nextCheck.next) {
					if (!(nextCheck.r.getFASTAChar() == r.getFASTAChar())) {
						break;
					}
					if (nextCheck.state.equals(state)) {
						return;
					}
				}
				// put the forced state *after* the state that is being used as
				// it. This saves space and allows the code to consolidate NDFA
				// states.
				link.next = temp.next;
				// set previous link's next to this
				temp.next = link;
				//								System.err.println("Same transition already exists!
				// Existing:"+temp.state+" ("+temp.state.getID()+"
				// "+temp.state.links+"), Forced:"+state+"("+state.getID()+"
				// "+state.links+").");
				return;
			}
			// if it is the same state, skip it
			else if (temp.r.getFASTAChar() == r.getFASTAChar()
					&& temp.state.equals(state)) {
				return;
			}

			// if less, insert
			if (temp.next.r.getFASTAChar() > r.getFASTAChar()) {
				// link old ref to the end of the new link
				link.next = temp.next;
				// set previous link's next to this
				temp.next = link;
				return;
			}
		}
	}

	/**
	 * Helper method to use when constructing the NDFA. This method will return
	 * the transition state that matches the given residue. If none exist, one
	 * is made from scratch.
	 */
	public NDFAState getState(Residue r) {
		//		System.out.println("Adding: "+r.name+", "+p.name);
		// if no links, make this the first
		if (links == null) {
			// make a new link/NDFA state
			Linker link = new Linker(r, new NDFAState());
			// set this as the state
			links = link;
			// return the new link
			return link.state;
		}

		// scan to see if there is already a match for this r/p pair (make min
		// NDFA)
		// if less than fi, insert before
		if (links.r.getFASTAChar() > r.getFASTAChar()) {
			// no match exist, make it
			Linker link = new Linker(r, new NDFAState());
			// set this next ref to the old
			link.next = links;
			// set the old to this
			links = link;
			// return the new NDFA
			return link.state;
		}

		// if there are links, put in alphabetically
		for (Linker temp = links; temp != null; temp = temp.next) {
			// if null, add this to the end
			if (temp.next == null) {
				// no match exist, make it
				Linker link = new Linker(r, new NDFAState());
				// set this next ref to the old
				temp.next = link;
				// reset the temp link
				temp = null;
				// return the new NDFA
				return link.state;
			}

			// if the same, return the match (cache!)
			if (temp.r.getFASTAChar() == r.getFASTAChar()) {
				return temp.state;
			}

			// if less, insert
			if (temp.next.r.getFASTAChar() > r.getFASTAChar()) {
				// make a new state
				Linker link = new Linker(r, new NDFAState());
				// link old ref to the end of the new link
				link.next = temp.next;
				// set previous link's next to this
				temp.next = link;
				// return the new link
				return link.state;
			}
		}

		// shouldn't reach here
		return null;
	}
}

// helper to link NDFAs

class Linker {
	// pointer to next state
	Linker next = null;

	// residue this links
	Residue r;

	NDFAState state;

	// make the linker
	public Linker(Residue r, NDFAState state) {
		this.r = r;
		this.state = state;
	}
}

// helper class to link collections of states via single letter codes

class StatesLink {
	// single letter aa code
	char c;

	NDFAState[] states;

	public StatesLink(char c, NDFAState[] states) {
		this.c = c;
		this.states = states;
	}
}