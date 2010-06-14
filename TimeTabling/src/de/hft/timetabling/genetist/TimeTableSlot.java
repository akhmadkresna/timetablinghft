package de.hft.timetabling.genetist;

/**
 * A time table slot is described as the combination of a period and a room.
 * 
 * @author Alexander Weickmann
 */
final class TimeTableSlot {

	private final int period;

	private final int room;

	public TimeTableSlot(final int period, final int room) {
		this.period = period;
		this.room = room;
	}

	public int getPeriod() {
		return period;
	}

	public int getRoom() {
		return room;
	}

	@Override
	public String toString() {
		return "Period: " + period + ", Room: " + room;
	}

}