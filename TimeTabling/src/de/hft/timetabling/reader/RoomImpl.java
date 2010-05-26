package de.hft.timetabling.reader;

import de.hft.timetabling.common.IRoom;

final class RoomImpl implements IRoom {

	private final String id;

	private final int capacity;

	RoomImpl(String id, int capacity) {
		this.id = id;
		this.capacity = capacity;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		return true;
	}

	@Override
	public String toString() {
		return "Room: " + id + " (" + capacity + ")";
	}

}
