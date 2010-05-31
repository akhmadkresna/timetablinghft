package de.hft.timetabling.genetist;

import java.util.ArrayList;
import java.util.HashSet;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IGenetist;
import de.hft.timetabling.services.ISolutionTableService;

public class CrazyGenetist implements IGenetist {

	private ISolutionTableService solution;
	private ICourse[][] bestSolution;
	private ICourse[][] otherSolution;

	@Override
	public void startRecombination(ISolutionTableService solution) {
		this.solution = solution;
		int n = (int) (ISolutionTableService.TABLE_SIZE - 1 * Math.random()) + 2;

		HashSet<ICourse> savingList = new HashSet<ICourse>();

		bestSolution = solution.getBestSolution().getCoding();
		otherSolution = solution.getSolution(n).getCoding();

		ICourse[][] oldBestSolution;
		for (int i = 0; i < bestSolution.length; i++) {
			for (int j = 0; j < bestSolution[j].length; j++) {
				if (bestSolution[i][j] == null) {
					oldBestSolution = bestSolution;
					if (otherSolution[i][j] != null) {
						CoursePosition position = getDoubleCourses(
								bestSolution, otherSolution[i][j].getId());
						bestSolution[i][j] = otherSolution[i][j];
						savingList.add(bestSolution[position.getX()][position
								.getY()]);
						bestSolution[position.getX()][position.getY()] = null;
						otherSolution[i][j] = null;
						
						if()
						
					}
				}
			}
		}

	}

	/*
	 * private ICourse getMissingCourse(ICourse[][] basicSolution) {
	 * 
	 * HashSet<String> courselist = new HashSet<String>();
	 * 
	 * for (int i = 0; i < basicSolution.length; i++) { for (int j = 0; j <
	 * basicSolution[j].length; j++) { if
	 * (!courselist.add(basicSolution[i][j].getId())) { // Exception } } }
	 * 
	 * for (int k = 0; k < ISolutionTableService.TABLE_SIZE; k++) { ICourse[][]
	 * tmpSolution = solution.getSolution(k).getCoding(); if
	 * (!tmpSolution.equals(bestSolution) && !tmpSolution.equals(otherSolution))
	 * { for (int i = 0; i < tmpSolution.length; i++) { for (int j = 0; j <
	 * tmpSolution[j].length; j++) {
	 * 
	 * if (courselist.add(tmpSolution[i][j].getId())) { return
	 * tmpSolution[i][j]; }
	 * 
	 * } } } } }
	 */

	private CoursePosition checkForDoubleCourses(ICourse[][] course) {
		ArrayList<CoursePosition> myCourse = checkForDoubleCourses(course, 1);
		if (myCourse.size() == 1) {
			return myCourse.get(0);
		}
	}

	private ArrayList<CoursePosition> checkForDoubleCourses(ICourse[][] course,
			int needed) {
		ArrayList<CoursePosition> courses = new ArrayList<CoursePosition>();
		HashSet<String> courselist = new HashSet<String>();

		for (int i = 0; i < course.length; i++) {
			for (int j = 0; j < course[j].length; j++) {
				if (!courselist.add(course[i][j].getId())) {
					courses.add(new CoursePosition(i, j));
					if ((needed == -1) || (needed >= courses.size())) {
						return courses;
					}
				}
			}
		}
		return courses;
	}

	private CoursePosition getDoubleCourses(ICourse[][] course, String id) {

		for (int i = 0; i < course.length; i++) {
			for (int j = 0; j < course[j].length; j++) {
				if (course[i][j].getId().equals(id)) {
					return new CoursePosition(i, j);

				}
			}
		}
		return null;
	}

}
