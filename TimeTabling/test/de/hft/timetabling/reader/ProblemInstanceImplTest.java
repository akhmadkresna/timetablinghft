package de.hft.timetabling.reader;

import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IRoom;

/**
 * @author Alexander Weickmann
 */
public class ProblemInstanceImplTest extends AbstractReaderTest {

	public void testGetName() {
		assertEquals(NAME, instance.getName());
	}

	public void testGetNumberOfCourses() {
		assertEquals(NUMBER_OF_COURSES, instance.getNumberOfCourses());
	}

	public void testGetNumberOfRooms() {
		assertEquals(NUMBER_OF_ROOMS, instance.getNumberOfRooms());
	}

	public void testGetNumberOfDays() {
		assertEquals(NUMBER_OF_DAYS, instance.getNumberOfDays());
	}

	public void testGetPeriodsPerDay() {
		assertEquals(PERIODS_PER_DAY, instance.getPeriodsPerDay());
	}

	public void testGetNumberOfCurricula() {
		assertEquals(NUMBER_OF_CURRICULA, instance.getNumberOfCurricula());
	}

	public void testGetNumberOfConstraints() {
		assertEquals(NUMBER_OF_CONSTRAINTS, instance.getNumberOfConstraints());
	}

	public void testGetCourses() {
		assertEquals(0, instance.getCourses().size());

		ICourse course1 = new CourseImpl("c1", 1, 2, 20, "Teacher1", instance);
		ICourse course2 = new CourseImpl("c2", 1, 1, 15, "Teacher1", instance);
		instance.addCourse(course1);
		instance.addCourse(course2);

		Set<ICourse> courses = instance.getCourses();
		assertEquals(2, courses.size());
		assertTrue(courses.contains(course1));
		assertTrue(courses.contains(course2));
		try {
			courses.remove(course1);
			fail();
		} catch (UnsupportedOperationException e) {
			// Expected exception.
		}
	}

	public void testGetRooms() {
		assertEquals(0, instance.getRooms().size());

		IRoom room1 = new RoomImpl("r1", 15, instance);
		IRoom room2 = new RoomImpl("r2", 30, instance);
		instance.addRoom(room1);
		instance.addRoom(room2);

		Set<IRoom> rooms = instance.getRooms();
		assertEquals(2, rooms.size());
		assertTrue(rooms.contains(room1));
		assertTrue(rooms.contains(room2));
		try {
			rooms.remove(room1);
			fail();
		} catch (UnsupportedOperationException e) {
			// Expected exception.
		}
	}

	public void testGetCurricula() {
		assertEquals(0, instance.getCurricula().size());

		ICurriculum curriculum1 = new CurriculumImpl("c1", 1, instance);
		ICurriculum curriculum2 = new CurriculumImpl("c2", 2, instance);
		instance.addCurriculum(curriculum1);
		instance.addCurriculum(curriculum2);

		Set<ICurriculum> curricula = instance.getCurricula();
		assertEquals(2, curricula.size());
		assertTrue(curricula.contains(curriculum1));
		assertTrue(curricula.contains(curriculum2));
		try {
			curricula.remove(curriculum1);
			fail();
		} catch (UnsupportedOperationException e) {
			// Expected exception.
		}
	}

	public void testGetUnavailabilityConstraints() {
		ICourse course1 = new CourseImpl("c1", 1, 2, 20, "Teacher1", instance);
		ICourse course2 = new CourseImpl("c2", 1, 1, 15, "Teacher1", instance);
		instance.addCourse(course1);
		instance.addCourse(course2);

		assertEquals(0, instance.getUnavailabilityConstraints(course1).size());

		instance.addUnavailabilityConstraint(course1, 0);
		instance.addUnavailabilityConstraint(course1, 3);
		instance.addUnavailabilityConstraint(course2, 1);

		Set<Integer> constraintsCourse1 = instance
				.getUnavailabilityConstraints(course1);
		assertEquals(2, constraintsCourse1.size());
		assertTrue(constraintsCourse1.contains(0));
		assertTrue(constraintsCourse1.contains(3));

		Set<Integer> constraintsCourse2 = instance
				.getUnavailabilityConstraints(course2);
		assertEquals(1, constraintsCourse2.size());
		assertTrue(constraintsCourse2.contains(1));

		try {
			constraintsCourse1.remove(0);
			fail();
		} catch (UnsupportedOperationException e) {
			// Expected exception.
		}
	}

	public void testGetCourseById() {
		ICourse course1 = new CourseImpl("c1", 1, 2, 20, "Teacher1", instance);
		ICourse course2 = new CourseImpl("c2", 1, 1, 15, "Teacher1", instance);
		instance.addCourse(course1);
		instance.addCourse(course2);

		assertEquals(course2, instance.getCourseById("c2"));
		assertEquals(course1, instance.getCourseById("c1"));
		assertNull(instance.getCourseById("c3"));
	}

	public void testToString() {
		assertEquals("Problem Instance: " + NAME, instance.toString());
	}

}
