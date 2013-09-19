/*
 * Created on Nov 2, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.proteomecommons.pfsm;

import org.proteomecommons.pfsm.*;
import org.proteomecommons.jaf.*;

/**
 * @author Jayson Falkner - jfalkner@umich.edu
 *  
 */
public class NDFAStateTransition {
	NDFAState[] next;

	Residue residue;

	public NDFAStateTransition(NDFAState[] next, Residue residue) {
		this.next = next;
		this.residue = residue;
	}

	public String toString() {
		String string = "" + residue.getName();
		return string;
	}
}