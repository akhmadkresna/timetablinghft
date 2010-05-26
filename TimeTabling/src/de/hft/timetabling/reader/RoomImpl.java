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
	public String toString() {
		return "Room: " + id + " (" + capacity + ")";
	}

}
