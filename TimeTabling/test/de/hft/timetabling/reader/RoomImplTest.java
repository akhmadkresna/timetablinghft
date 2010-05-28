package de.hft.timetabling.reader;

import de.hft.timetabling.common.IRoom;

/**
 * @author Alexander Weickmann
 */
public class RoomImplTest extends AbstractReaderTest {

	private static final String ID = "r1";

	private static final int CAPACITY = 20;

	private IRoom room;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		room = new RoomImpl(ID, CAPACITY);
	}

	public void testGetId() {
		assertEquals(ID, room.getId());
	}

	public void testGetCapacity() {
		assertEquals(CAPACITY, room.getCapacity());
	}

	public void testToString() {
		assertEquals("Room: " + ID + " (" + CAPACITY + ")", room.toString());
	}

}
