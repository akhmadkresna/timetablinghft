package de.hft.timetabling.reader;

import de.hft.timetabling.common.IRoom;

/**
 * @author Alexander Weickmann
 */
public class RoomImplTest extends AbstractReaderTest {

	private static final int UNIQUE_NUMBER = 0;

	private static final String ID = "r1";

	private static final int CAPACITY = 20;

	private IRoom room;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		room = new RoomImpl(ID, CAPACITY, UNIQUE_NUMBER, instance);
	}

	public void testGetProblemInstance() {
		assertEquals(instance, room.getProblemInstance());
	}

	public void testGetId() {
		assertEquals(ID, room.getId());
	}

	public void testGetCapacity() {
		assertEquals(CAPACITY, room.getCapacity());
	}

	public void testGetUniqueNumber() {
		assertEquals(UNIQUE_NUMBER, room.getUniqueNumber());
	}

	public void testToString() {
		assertEquals("Room: " + ID + " (" + UNIQUE_NUMBER + ")", room
				.toString());
	}

}
