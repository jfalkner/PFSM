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
package org.proteomecommons.pfsm.util;

import org.proteomecommons.pfsm.*;
import java.util.*;


/**
 * @author Jayson Falkner - jfalkner@umich.edu
 * 
 * A bucket sort/hash to make NDFA->DFA creation speedy.
 */
public class FastNDFAArrayMaker {
	private boolean[] bucket = null;
	private NDFAState[] valids = null;
	private int validsIndex = 0;
	// for adding arrays
	int i = 0;

	// array of makers
	private static final int maxMakers = 10;
	private static FastNDFAArrayMaker[] makers = new FastNDFAArrayMaker[maxMakers];
	private static Thread[] makerThreads = new Thread[maxMakers];
	private static int makerIndex =0;
	
	// debug if any redundant states are being made (they shouldn't be, you can delete this!)
//	int countStates = 0;
	
	public static void reset() {
		makerIndex = 0;
		// nuke the old array makers
		for (int i=0;i<makerThreads.length;i++){
			makerThreads[i] = null;
		}
	}
	
	// helper method to get array makers
	public static FastNDFAArrayMaker getInstance() {
		Thread t = Thread.currentThread();
   // try all the makers
		for (int i=0;i<makerIndex;i++){
			if (t.equals(makerThreads[i])) {
				return makers[i];
			}
		}
		
		// make a new one
		if (makerIndex >= maxMakers) {
			throw new RuntimeException("Too many maker threads!!!");
		}
		
		// make a new one
		makers[makerIndex] = new FastNDFAArrayMaker(new NDFAState().id);
		makerThreads[makerIndex] = t;
		makerIndex++;
		
		return makers[makerIndex-1];
	}
	
	public int size() {
		return validsIndex;
	}

	/**
	 * Init with an appropriate bucket size.
	 * 
	 * @param bucketSize
	 */
	public FastNDFAArrayMaker(int bucketSize) {
		bucket = new boolean[bucketSize];
		// this likely need not be this big!
		valids =  new NDFAState[bucketSize/10];
	}

	/**
	 * Ensure the array is big enough
	 *
	 */
	private void checkSize() {
		if (validsIndex >= valids.length) {
			// make new arrays
			NDFAState[] tempValids = new NDFAState[valids.length+1000];
			System.arraycopy(valids, 0, tempValids, 0, valids.length);
			// set as the array -- used to skip checkSize()!
			valids = tempValids;
		}
	}
	
	/**
	 * A method to add things to the bucket.
	 * 
	 * @param state
	 */
	public void add(NDFAState[] states) {
//		countStates+= states.length;
		while (i < states.length) {
			// if it hasn't been seen, keep track of it
			if (!bucket[states[i].id]) {
				// if unseen, flag it and add to valids
				bucket[states[i].id] = true;
				// TODO: put an array check here!! speed isn't safety!
				valids[validsIndex] = states[i];
				validsIndex++;
				// check size
//				checkSize();
			}
			i++;
		}
		// reset i
		i = 0;
	}

	/**
	 * A method to add things to the bucket.
	 * 
	 * @param state
	 */
	public void add(NDFAState state) {
//		countStates++;
		// if it hasn't been seen, keep track of it
		if (!bucket[state.id]) {
			// if unseen, flag it and add to valids
			bucket[state.id] = true;
			// TODO: put an array check here!! speed isn't safety!
			valids[validsIndex] = state;
			validsIndex++;
//			checkSize();
		}
	}

	public NDFAState[] getStates() {
//		if (countStates!=validsIndex) {
//			System.out.println("Fast Array Maker consolidted! count:"+countStates+", valids:"+validsIndex);
//		}
//		countStates = 0;
		
		NDFAState[] validStates = new NDFAState[validsIndex];
		for (int i = 0; i < validsIndex; i++) {
			// populate array to return
			validStates[i] = valids[i];
			// remove from the bucket
			bucket[valids[i].id] = false;
		}
		// reset index
		validsIndex = 0;

		// return the valid bucket items
		return validStates;
	}

}