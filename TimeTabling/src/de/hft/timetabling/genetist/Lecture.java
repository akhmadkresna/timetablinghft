package de.hft.timetabling.genetist;

import de.hft.timetabling.common.ICourse;

/**
 * A lecture describes the association of a course to a given period-room slot.
 * 
 * @author Alexander Weickmann
 */
class Lecture {

	private final ICourse course;

	private final TimeTableSlot slot;

	public Lecture(ICourse course, int period, int room) {
		this.course = course;
		slot = new TimeTableSlot(period, room);
	}

	public ICourse getCourse() {
		return course;
	}

	public TimeTableSlot getSlot() {
		return slot;
	}

}