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
/**
 * @author root
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FiniteState {
	// next links in the fsm -- TODO: optimize this, why all single chars?
//	public FiniteState[] next = new FiniteState['Z'-'A'];
	Hashtable next = new Hashtable();
	boolean used = false;
	boolean fin = false;
	
//	public String toString() {
//		String string = "";
//		for (int i=0;i<next.length;i++){
//			if (next[i] != null) {
//				string += (char)('A'+i);
//			}
//		}
//		return string;
//	}
}
