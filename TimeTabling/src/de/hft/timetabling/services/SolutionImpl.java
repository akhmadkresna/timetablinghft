package de.hft.timetabling.services;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;

/**
 * Immutable solution implementation.
 * 
 * @author Alexander Weickmann
 * 
 * @see ISolution
 */
final class SolutionImpl implements ISolution {

	/**
	 * The coding of the solution associates courses with time periods
	 * (x-dimension) and rooms (y-dimension).
	 */
	private final ICourse[][] coding;

	private final IProblemInstance problemInstance;

	private int recombinations = 0;

	SolutionImpl(ICourse[][] coding, IProblemInstance problemInstance) {
		this.coding = coding;
		this.problemInstance = problemInstance;
	}

	@Override
	public IProblemInstance getProblemInstance() {
		return problemInstance;
	}

	@Override
	public ICourse[][] getCoding() {
		return coding;
	}

	@Override
	public String toString() {
		return "Solution: " + coding.toString();
	}

	@Override
	public int getRecombinationCount() {
		return recombinations;
	}

	@Override
	public void increaseRecombinationCount() {
		recombinations++;
	}

	@Override
	public void setRecombinationCount(int nrOfRecombinations) {
		recombinations = nrOfRecombinations;
	}

}
