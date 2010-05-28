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
		curriculum = new CurriculumImpl(ID, NUMBER_OF_COURSES);
	}

	public void testGetId() {
		assertEquals(ID, curriculum.getId());
	}

	public void testGetNumberOfCourses() {
		assertEquals(NUMBER_OF_COURSES, curriculum.getNumberOfCourses());
	}

	public void testGetCourses() {
		assertEquals(0, curriculum.getCourses().size());

		ICourse course1 = new CourseImpl("c1", 1, 2, 20, "Teacher1", instance);
		ICourse course2 = new CourseImpl("c2", 1, 1, 15, "Teacher1", instance);
		instance.addCourse(course1);
		instance.addCourse(course2);

		curriculum.addCourse(course1);
		curriculum.addCourse(course2);

		Set<ICourse> courses = curriculum.getCourses();
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

	public void testContainsCourse() {
		ICourse course1 = new CourseImpl("c1", 1, 2, 20, "Teacher1", instance);
		ICourse course2 = new CourseImpl("c2", 1, 1, 15, "Teacher1", instance);
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
