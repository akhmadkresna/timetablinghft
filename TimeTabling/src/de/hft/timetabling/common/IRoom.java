package de.hft.timetabling.common;

/**
 * An <tt>IRoom</tt> captures all the information provided by the competition
 * for a specific room. Additionally, a unique number is assigned to each room
 * so it can be used as array index for solution codings.
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

	/**
	 * Returns the unique number assigned to this room.
	 */
	int getUniqueNumber();

}
