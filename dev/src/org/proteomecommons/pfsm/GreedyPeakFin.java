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

import org.proteomecommons.io.*;

/**
 * @author Jayson Falkner - jfalkner@umich.edu
 * 
 * Final states keep track of what peak list generated them.
 */
public class GreedyPeakFin extends GreedyPeak {
	public PeakList peaklist;
	public GreedyPeakFin(PeakList peaklist, double intensity,
			double massOverChargeInDaltons) {
		super(intensity, massOverChargeInDaltons);
		this.peaklist = peaklist;
		this.fin = true;
	}
}