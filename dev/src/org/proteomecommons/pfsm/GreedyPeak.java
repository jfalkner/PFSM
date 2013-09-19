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

import java.util.*;
import org.proteomecommons.jaf.*;

public class GreedyPeak extends NDFAState implements Comparable {
	// keep track if this peak has been used before
	boolean used = false;
	// things that start here
	public LinkedList starts = new LinkedList();
	// things that end here
	public LinkedList ends = new LinkedList();
	public double intensity;
	public double massOverChargeInDaltons;

	public int compareTo(Object o) {
		if (o instanceof GreedyPeak) {
		GreedyPeak gp = (GreedyPeak) o;
		if (gp.massOverChargeInDaltons - massOverChargeInDaltons == 0) {
			return 0;
		}
		if (gp.massOverChargeInDaltons - massOverChargeInDaltons > 0) {
			return -1;
		}
		return 1;
		}
		else {
			return super.compareTo(o);
		}
	}

	// get the longest path forward
	public int getLongestPath(){
int longest = 0;

		// try all siblings
		for (int i=0;i<starts.size();i++){
			GreedyPeakLink link = (GreedyPeakLink)starts.get(i);
			int temp = 1 + link.end.getLongestPath();
			if (temp > longest) {
				longest = temp;
			}
		}
		
		return longest;
	}
	
	// return how deep this goes forward (i.e. to c terminus)
	public boolean checkForwardDepth(int depth) {
		// if at the end return true
		if (depth == 0) {
			return true;
		}
		
		// try all siblings
		for (int i=0;i<starts.size();i++){
			GreedyPeakLink link = (GreedyPeakLink)starts.get(i);
			if (link.end.checkForwardDepth(depth-1)) {
				return true;
			}
		}
		
		return false;
	}

	// return how deep this goes forward (i.e. to n terminus)
	public boolean checkBackwardDepth(int depth) {
		// if at the end return true
		if (depth == 0) {
			return true;
		}
		
		// try all siblings
		for (int i=0;i<ends.size();i++){
			GreedyPeakLink link = (GreedyPeakLink)ends.get(i);
			if (link.start.checkForwardDepth(depth-1)) {
				return true;
			}
		}
		
		return false;
	}
	
	// helper method to check if something is connected backwards
	public boolean isConnectedTo(GreedyPeak peak, int depth) {
		// if at the end return true
		if (depth == 0 && this.equals(peak)) {
			return true;
		}
		
		// try all siblings
		for (Iterator i=ends.iterator();i.hasNext();){
			GreedyPeakLink link = (GreedyPeakLink)i.next();
			if (link.start.isConnectedTo(peak, depth-1)) {
				return true;
			}
		}
		
		return false;
	}

	
	public GreedyPeak(double intensity, double massOverChargeInDaltons) {
		this.intensity = intensity;
		this.massOverChargeInDaltons = massOverChargeInDaltons;
	}
	
	public String toString() {
		return "("+this.massOverChargeInDaltons+", "+this.intensity+")";
	}
}