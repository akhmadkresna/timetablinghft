package de.hft.timetabling.genetist;

import java.util.HashSet;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IGenetist;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.util.ValidatorImpl;

public class CrazyGenetist implements IGenetist {

	private ISolutionTableService solution;
	private ISolution bestSolution;
	private ISolution otherSolution;

	@Override
	public void startRecombination(ISolutionTableService solution) {
		setSolution(solution);
		int n = (int) (ISolutionTableService.TABLE_SIZE - 1 * Math.random()) + 2;

		HashSet<ICourse> savingList = new HashSet<ICourse>();
		ValidatorImpl vi = new ValidatorImpl();

		bestSolution = solution.getBestSolution();
		otherSolution = solution.getSolution(n);

		ICourse[][] oldBestSolution;
		for (int i = 0; i < bestSolution.getCoding().length; i++) {
			for (int j = 0; j < bestSolution.getCoding()[j].length; j++) {
				if (bestSolution.getCoding()[i][j] == null) {
					oldBestSolution = bestSolution.getCoding();
					if (otherSolution.getCoding()[i][j] != null) {
						CoursePosition position = getDoubleCourses(bestSolution
								.getCoding(), otherSolution.getCoding()[i][j]
								.getId());
						bestSolution.getCoding()[i][j] = otherSolution
								.getCoding()[i][j];
						if (!vi.isValidSolution(bestSolution)) {
							bestSolution.getCoding()[i][j] = oldBestSolution[i][j];
						} else {
							savingList.add(bestSolution.getCoding()[position
									.getX()][position.getY()]);
							bestSolution.getCoding()[position.getX()][position
									.getY()] = null;
							otherSolution.getCoding()[i][j] = null;
						}

					}
				}
			}
		}

	}

	// private CoursePosition checkForDoubleCourses(ICourse[][] course) {
	// ArrayList<CoursePosition> myCourse = checkForDoubleCourses(course, 1);
	// if (myCourse.size() == 1) {
	// return myCourse.get(0);
	// }
	// return null;
	// }
	//
	// private ArrayList<CoursePosition> checkForDoubleCourses(ICourse[][]
	// course,
	// int needed) {
	// ArrayList<CoursePosition> courses = new ArrayList<CoursePosition>();
	// HashSet<String> courselist = new HashSet<String>();
	//
	// for (int i = 0; i < course.length; i++) {
	// for (int j = 0; j < course[j].length; j++) {
	// if (!courselist.add(course[i][j].getId())) {
	// courses.add(new CoursePosition(i, j));
	// if ((needed == -1) || (needed >= courses.size())) {
	// return courses;
	// }
	// }
	// }
	// }
	// return courses;
	// }

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

	public void setSolution(ISolutionTableService solution) {
		this.solution = solution;
	}

	public ISolutionTableService getSolution() {
		return solution;
	}

}
