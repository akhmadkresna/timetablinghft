package de.hft.timetabling.reader;

import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;

/**
 * @author Alexander Weickmann
 */
public class CourseImplTest extends AbstractReaderTest {

	private static final String ID = "c1";

	private static final int MIN_WORKING_DAYS = 2;

	private static final int NUMBER_OF_LECTURES = 4;

	private static final int NUMBER_OF_STUDENTS = 25;

	private static final String TEACHER = "Teacher1";

	private ICourse course;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		course = new CourseImpl(ID, MIN_WORKING_DAYS, NUMBER_OF_LECTURES,
				NUMBER_OF_STUDENTS, TEACHER, instance);
	}

	public void testGetProblemInstance() {
		assertEquals(instance, course.getProblemInstance());
	}

	public void testGetId() {
		assertEquals(ID, course.getId());
	}

	public void testGetMinWorkingDays() {
		assertEquals(MIN_WORKING_DAYS, course.getMinWorkingDays());
	}

	public void testGetNumberOfLectures() {
		assertEquals(NUMBER_OF_LECTURES, course.getNumberOfLectures());
	}

	public void testGetNumberOfStudents() {
		assertEquals(NUMBER_OF_STUDENTS, course.getNumberOfStudents());
	}

	public void testGetTeacher() {
		assertEquals(TEACHER, course.getTeacher());
	}

	public void testGetCurricula() {
		CurriculumImpl curriculum1 = new CurriculumImpl("cur1", 2, instance);
		CurriculumImpl curriculum2 = new CurriculumImpl("cur2", 1, instance);
		CurriculumImpl curriculum3 = new CurriculumImpl("cur3", 1, instance);

		ICourse otherCourse = new CourseImpl("cOther", 1, 1, 1, "Teacher2",
				instance);
		instance.addCourse(otherCourse);
		assertEquals(0, otherCourse.getCurricula().size());

		curriculum1.addCourse(otherCourse);
		curriculum1.addCourse(course);
		curriculum2.addCourse(course);
		curriculum3.addCourse(otherCourse);

		instance.addCurriculum(curriculum1);
		instance.addCurriculum(curriculum2);
		instance.addCurriculum(curriculum3);

		Set<ICurriculum> courseCurricula = course.getCurricula();
		assertEquals(2, courseCurricula.size());
		assertTrue(courseCurricula.contains(curriculum1));
		assertTrue(courseCurricula.contains(curriculum2));

		Set<ICurriculum> otherCourseCurricula = otherCourse.getCurricula();
		assertEquals(2, otherCourseCurricula.size());
		assertTrue(otherCourseCurricula.contains(curriculum1));
		assertTrue(otherCourseCurricula.contains(curriculum3));
	}

	public void testToString() {
		assertEquals("Course: " + ID + " (" + TEACHER + ")", course.toString());
	}

}
