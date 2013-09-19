package org.proteomecommons.pfsm;

import org.proteomecommons.jaf.*;

/**
 * @author Jayson Falkner - jfalkner@umich.edu
 *  
 */
public class Configuration {
	private double massAccuracyPPM = 200;
	private double minTolerance = 0.05;
	private double massAccuracyDaltons = 0;
	private int maxIonSeriesGap = 2;
	private double nTerminusMassInDaltons = Atom.H.getMassInDaltons();
	private double cTerminusMassInDaltons = Atom.H.getMassInDaltons()+Atom.O.getMassInDaltons();

	/**
	 * Helper method to get the mass accuracy for a given mass.
	 * 
	 * @param mz
	 * @return
	 */
	public double getMaxMassAccuracyError(double mz) {
//		if (massAccuracyDaltons > 0) {
//			return massAccuracyDaltons;
//		}
		double tol = mz * massAccuracyPPM / 1000000;
		if (tol < minTolerance){
			return minTolerance;
		}
		return tol;
	}

	/**
	 * Set method for the mass accuracy in PPM.
	 * @param ppm
	 */
	public void setMassAccuracyPPM(double ppm) {
		this.massAccuracyPPM = ppm;
	}
	/**
	 * Set method for the mass accuracy in PPM.
	 * @param ppm
	 */
	public double getMassAccuracyPPM() {
		return massAccuracyPPM;
	}

	/**
	 * Set method for the mass accuracy in Da.
	 * @param da
	 */
	public void setMassAccuracyDaltons(double da) {
		this.massAccuracyDaltons = da;
	}
	
	/**
	 * Sets how many residues to consider in gaps.
	 * @param length
	 */
	public void setMaxIonSeriesGap(int length) {
		this.maxIonSeriesGap = length;
	}
	
	/**
	 * Get method for the max residues to consider in a gap.
	 * @return
	 */
	public int getMaxIonSeriesGap() {
		return maxIonSeriesGap;
	}
	
	public double getNTerminus() {
		return nTerminusMassInDaltons;
	}
	public void setNTerminus(double n){
		this.nTerminusMassInDaltons = n;
	}
	public double getCTerminus() {
		return cTerminusMassInDaltons;
	}
	public void setCTerminus(double c){
		this.cTerminusMassInDaltons = c;
	}
}