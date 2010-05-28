package de.hft.timetabling.reader;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import junit.framework.TestCase;
import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.IRoom;

/**
 * @author Alexander Weickmann
 */
public class ReaderTest extends TestCase {

	private Reader reader;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		reader = new Reader();
	}

	/**
	 * Tests whether a specific problem instance input file is read correctly.
	 */
	public void testReadInstance() throws IOException {
		IProblemInstance instance = reader.readInstance("test.txt");

		// # GENERAL INFORMATION
		assertEquals("ToyExample", instance.getName());
		assertEquals(4, instance.getNumberOfCourses());
		assertEquals(2, instance.getNumberOfRooms());
		assertEquals(5, instance.getNumberOfDays());
		assertEquals(4, instance.getPeriodsPerDay());
		assertEquals(2, instance.getNumberOfCurricula());
		assertEquals(8, instance.getNumberOfConstraints());

		// # COURSES
		Set<ICourse> courses = instance.getCourses();
		assertEquals(4, courses.size());
		ICourse[] courseArray = courses.toArray(new ICourse[4]);

		// - Course 1
		assertEquals("SceCosC", courseArray[0].getId());
		assertEquals("Ocra", courseArray[0].getTeacher());
		assertEquals(3, courseArray[0].getNumberOfLectures());
		assertEquals(3, courseArray[0].getMinWorkingDays());
		assertEquals(30, courseArray[0].getNumberOfStudents());

		// - Course 2
		assertEquals("ArcTec", courseArray[1].getId());
		assertEquals("Indaco", courseArray[1].getTeacher());
		assertEquals(3, courseArray[1].getNumberOfLectures());
		assertEquals(2, courseArray[1].getMinWorkingDays());
		assertEquals(42, courseArray[1].getNumberOfStudents());

		// - Course 3
		assertEquals("TecCos", courseArray[2].getId());
		assertEquals("Rosa", courseArray[2].getTeacher());
		assertEquals(5, courseArray[2].getNumberOfLectures());
		assertEquals(4, courseArray[2].getMinWorkingDays());
		assertEquals(40, courseArray[2].getNumberOfStudents());

		// - Course 4
		assertEquals("Geotec", courseArray[3].getId());
		assertEquals("Scarlatti", courseArray[3].getTeacher());
		assertEquals(5, courseArray[3].getNumberOfLectures());
		assertEquals(4, courseArray[3].getMinWorkingDays());
		assertEquals(18, courseArray[3].getNumberOfStudents());

		// # ROOMS
		Set<IRoom> rooms = instance.getRooms();
		assertEquals(2, rooms.size());
		IRoom[] roomArray = rooms.toArray(new IRoom[2]);

		// - Room 1
		assertEquals("A", roomArray[0].getId());
		assertEquals(32, roomArray[0].getCapacity());

		// - Room 2
		assertEquals("B", roomArray[1].getId());
		assertEquals(50, roomArray[1].getCapacity());

		// # CURRICULA
		Set<ICurriculum> curricula = instance.getCurricula();
		assertEquals(2, curricula.size());
		ICurriculum[] curriculaArray = curricula.toArray(new ICurriculum[2]);

		// - Curriculum 1
		assertEquals("Cur1", curriculaArray[0].getId());
		assertEquals(3, curriculaArray[0].getNumberOfCourses());
		ICourse[] curricula1Courses = curriculaArray[0].getCourses().toArray(
				new ICourse[3]);
		assertEquals("SceCosC", curricula1Courses[0].getId());
		assertEquals("ArcTec", curricula1Courses[1].getId());
		assertEquals("TecCos", curricula1Courses[2].getId());

		// - Curriculum 2
		assertEquals("Cur2", curriculaArray[1].getId());
		assertEquals(2, curriculaArray[1].getNumberOfCourses());
		ICourse[] curricula2Courses = curriculaArray[1].getCourses().toArray(
				new ICourse[2]);
		assertEquals("TecCos", curricula2Courses[0].getId());
		assertEquals("Geotec", curricula2Courses[1].getId());

		// # UNAVAILABILITY CONSTRAINTS

		// - Course 1
		Set<Integer> course1Constraints = instance
				.getUnavailabilityConstraints(courseArray[0]);
		assertEquals(0, course1Constraints.size());

		// - Course 2
		Set<Integer> course2Constraints = instance
				.getUnavailabilityConstraints(courseArray[1]);
		assertEquals(4, course2Constraints.size());
		assertTrue(course2Constraints.contains(16));
		assertTrue(course2Constraints.contains(17));
		assertTrue(course2Constraints.contains(18));
		assertTrue(course2Constraints.contains(19));

		// - Course 3
		Set<Integer> course3Constraints = instance
				.getUnavailabilityConstraints(courseArray[2]);
		assertEquals(4, course3Constraints.size());
		assertTrue(course3Constraints.contains(8));
		assertTrue(course3Constraints.contains(9));
		assertTrue(course3Constraints.contains(14));
		assertTrue(course3Constraints.contains(15));

		// - Course 4
		Set<Integer> course4Constraints = instance
				.getUnavailabilityConstraints(courseArray[3]);
		assertEquals(0, course4Constraints.size());
	}

	/**
	 * Just reads every instance in the instances folder to make sure that no
	 * exception occurs while reading any of the input files.
	 */
	public void testReadAllInstances() throws IOException {
		File instancesFolder = new File("instances");
		for (String fileName : instancesFolder.list()) {
			if (fileName.equals(".svn")) {
				continue;
			}
			reader.readInstance(fileName);
		}
	}

	public void testToString() {
		assertEquals("Reader", reader.toString());
	}

}
