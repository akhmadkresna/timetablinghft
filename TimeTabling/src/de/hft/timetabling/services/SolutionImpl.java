package de.hft.timetabling.services;

import java.util.Arrays;

import de.hft.timetabling.common.ISolution;

final class SolutionImpl implements ISolution, Cloneable {

	private final String[][] coding;

	SolutionImpl(String[][] coding) {
		this.coding = coding;
	}

	@Override
	public String[][] getCoding() {
		return coding;
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