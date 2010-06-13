package de.hft.timetabling.solutiontable;

import java.util.Arrays;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;

/**
 * Implementation of the solution interface.
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(coding);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SolutionImpl other = (SolutionImpl) obj;
		if (!Arrays.equals(coding, other.coding)) {
			return false;
		}
		return true;
	}

}
