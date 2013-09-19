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

import org.proteomecommons.jaf.util.*;

/**
 * @author Jayson Falkner - jfalkner@umich.edu
 *
 */
public class GreedyPeakLink implements Comparable{
	public GreedyPeak start;
	public GreedyPeak end;
	public ResidueCombinationCacheEntry[] hits;
	public GreedyPeakLink(GreedyPeak start, GreedyPeak end, ResidueCombinationCacheEntry[] hits) {
		this.start = start;
		this.end = end;
		this.hits = hits;
	}
	
	public String toString() {
		java.io.StringWriter sw = new java.io.StringWriter();
		for (int i=0;i<hits.length;i++){
			for (int j=0;j<hits[i].residues.length;j++){
				sw.write(hits[i].residues[j].getFASTAChar());
			}
			sw.write("("+hits[i].getMassInDaltons()+")");
		}
		
		
		return sw.toString();
	}
	
	// helper to organize compares
	public int compareTo(Object o){
		GreedyPeakLink gpl = (GreedyPeakLink)o;
		// smaller end first
		if (gpl.end.massOverChargeInDaltons > end.massOverChargeInDaltons) {
			return -1;
		}
		// bigger last
		if (gpl.end.massOverChargeInDaltons < end.massOverChargeInDaltons) {
			return 1;
		}
		
		// if equal, do smallest first
		if (gpl.start.massOverChargeInDaltons < start.massOverChargeInDaltons) {
			return 1;
		}
		// bigger last
		if (gpl.start.massOverChargeInDaltons > start.massOverChargeInDaltons) {
			return -1;
		}		
		return 0;
	}
}