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

	private int recombinations;

	private int age;

	private int penalty;

	private int fairness;

	SolutionImpl(final ICourse[][] coding,
			final IProblemInstance problemInstance) {
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
	public void setRecombinationCount(final int nrOfRecombinations) {
		recombinations = nrOfRecombinations;
	}

	@Override
	public int getAge() {
		return age;
	}

	void increaseAge() {
		age++;
	}

	@Override
	public int getFairness() {
		return fairness;
	}

	void setFairness(final int fairness) {
		this.fairness = fairness;
	}

	@Override
	public int getPenalty() {
		return penalty;
	}

	void setPenalty(final int penalty) {
		this.penalty = penalty;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(coding);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SolutionImpl other = (SolutionImpl) obj;
		if (!Arrays.equals(coding, other.coding)) {
			return false;
		}
		return true;
	}

}
