package de.hft.timetabling.reader;

import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.IRoom;

/**
 * Immutable room implementation.
 * 
 * @author Alexander Weickmann
 */
final class RoomImpl implements IRoom {

	private final String id;

	private final int capacity;

	private final int uniqueNumber;

	private final IProblemInstance problemInstance;

	RoomImpl(String id, int capacity, int uniqueNumber,
			IProblemInstance problemInstance) {

		this.id = id;
		this.capacity = capacity;
		this.uniqueNumber = uniqueNumber;
		this.problemInstance = problemInstance;
	}

	@Override
	public IProblemInstance getProblemInstance() {
		return problemInstance;
	}

	@Override
	public int getCapacity() {
		return capacity;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getUniqueNumber() {
		return uniqueNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((problemInstance == null) ? 0 : problemInstance.hashCode());
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
		RoomImpl other = (RoomImpl) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (problemInstance == null) {
			if (other.problemInstance != null) {
				return false;
			}
		} else if (!problemInstance.equals(other.problemInstance)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Room: " + id + " (" + uniqueNumber + ")";
	}

}
