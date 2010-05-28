package de.hft.timetabling.common;

/**
 * An <tt>IRoom</tt> captures all the information provided by the competition
 * for a specific room.
 * 
 * @author Alexander Weickmann
 */
public interface IRoom {

	/**
	 * Returns the ID of the room.
	 */
	String getId();

	/**
	 * Returns how many visitors the room can hold.
	 */
	int getCapacity();

	/**
	 * Returns the problem instance this room belongs to.
	 */
	IProblemInstance getProblemInstance();

}
