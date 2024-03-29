package de.hft.timetabling.reader;

import java.util.Set;

import de.hft.timetabling.common.ICourse;

/**
 * @author Alexander Weickmann
 */
public class CurriculumImplTest extends AbstractReaderTest {

	private static final String ID = "cur1";

	private static final int NUMBER_OF_COURSES = 2;

	private CurriculumImpl curriculum;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		curriculum = new CurriculumImpl(ID, NUMBER_OF_COURSES, instance);
	}

	public void testGetProblemInstance() {
		assertEquals(instance, curriculum.getProblemInstance());
	}

	public void testGetId() {
		assertEquals(ID, curriculum.getId());
	}

	public void testGetNumberOfCourses() {
		assertEquals(NUMBER_OF_COURSES, curriculum.getNumberOfCourses());
	}

	public void testGetCourses() {
		assertEquals(0, curriculum.getCourses().size());

		final ICourse course1 = new CourseImpl("c1", 1, 2, 20, "Teacher1",
				instance);
		final ICourse course2 = new CourseImpl("c2", 1, 1, 15, "Teacher1",
				instance);
		instance.addCourse(course1);
		instance.addCourse(course2);

		curriculum.addCourse(course1);
		curriculum.addCourse(course2);

		final Set<ICourse> courses = curriculum.getCourses();
		assertEquals(2, courses.size());
		assertTrue(courses.contains(course1));
		assertTrue(courses.contains(course2));
		try {
			courses.remove(course1);
			fail();
		} catch (final UnsupportedOperationException e) {
			// Expected exception (immutability).
		}
	}

	public void testContainsCourse() {
		final ICourse course1 = new CourseImpl("c1", 1, 2, 20, "Teacher1",
				instance);
		final ICourse course2 = new CourseImpl("c2", 1, 1, 15, "Teacher1",
				instance);
		instance.addCourse(course1);
		instance.addCourse(course2);

		curriculum.addCourse(course2);

		assertFalse(curriculum.containsCourse(course1));
		assertTrue(curriculum.containsCourse(course2));
	}

	public void testToString() {
		assertEquals("Curriculum: " + ID + " (" + NUMBER_OF_COURSES + ")",
				curriculum.toString());
	}

}
