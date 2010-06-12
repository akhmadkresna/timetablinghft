package de.hft.timetabling.genetist;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hft.timetabling.common.ICourse;

public class CrazyGenetistUtility {

	/**
	 * Method for changing a ICourse[] to a Set<ICourse>
	 * 
	 * @param courses
	 *            Array of ICourses
	 * @return Set<ICourse>
	 */
	public static Set<ICourse> getCourseSet(ICourse[] courses) {
		Set<ICourse> back = new HashSet<ICourse>();
		for (int i = 0; i < courses.length; i++) {
			back.add(courses[i]);
		}
		return back;
	}

	/**
	 * Method for getting a map out of ICourses and their position in a
	 * ISolution.
	 * 
	 * @param course
	 *            ICourse[][] that should be searched in
	 * @param id
	 *            of course that should be looked for
	 * @return Map which contains the ICourse element and the position where it
	 *         was found.
	 */
	public static Map<ICourse, Lecture> countLectures(ICourse[][] course,
			String id) {

		Map<ICourse, Lecture> positions = new HashMap<ICourse, Lecture>();

		for (int i = 0; i < course.length; i++) {
			for (int j = 0; j < course[i].length; j++) {
				if ((course[i][j] != null) && course[i][j].getId().equals(id)) {
					positions
							.put(course[i][j], new Lecture(course[i][j], i, j));
				}
			}
		}
		return positions;
	}

	/**
	 * Method to write one specific coding in a string. For every course the
	 * toString() method is called.
	 * 
	 * @param courses
	 *            course that should be in the string
	 * @return string of all courses
	 */
	public static String coursesToStringOb(ICourse[][] courses) {
		String back = "";
		for (int i = 0; i < courses.length; i++) {
			back += "[";
			for (int j = 0; j < courses[i].length; j++) {
				if (courses[i][j] != null) {
					back += courses[i][j].toString();
				} else {
					back += "null";
				}
				if (j != courses[i].length - 1) {
					back += ",";
				}
			}
			back += "]\n";
		}
		return back;
	}

	/**
	 * Method to write one specific coding in a string. For every course the ID
	 * is put to the string.
	 * 
	 * @param courses
	 *            course that should be in the string
	 * @return string of all courses
	 */
	public static String coursesToStringId(ICourse[][] courses) {
		String back = "";
		for (int i = 0; i < courses.length; i++) {
			back += "[";
			for (int j = 0; j < courses[i].length; j++) {
				if (courses[i][j] != null) {
					back += courses[i][j].getId();
				} else {
					back += "null";
				}
				if (j != courses[i].length - 1) {
					back += ",";
				}
			}
			back += "]\n";
		}
		return back;
	}

}
