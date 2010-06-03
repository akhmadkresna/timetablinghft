package de.hft.timetabling.genetist;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IGenetist;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.util.ValidatorImpl;

public class CrazyGenetist implements IGenetist {

	private ISolutionTableService solution;
	private double probability = 0.5;

	@Override
	public ISolution startRecombination(ISolutionTableService solution) {
		setSolution(solution);

		int n = (int) (ISolutionTableService.TABLE_SIZE - 1 * Math.random()) + 2;

		ISolution bestSolution = solution.getBestSolution();
		ISolution otherSolution = solution.getSolution(n);

		ISolution back;

		if (Math.random() < probability) {
			back = mutateRoomStability(recombindation1(bestSolution,
					otherSolution));
		} else {
			back = mutateRoomStability(recombindation2(bestSolution,
					otherSolution));
		}

		bestSolution.increaseRecombinationCount();
		otherSolution.increaseRecombinationCount();

		return back;

	}

	public void setSolution(ISolutionTableService solution) {
		this.solution = solution;
	}

	public ISolutionTableService getSolution() {
		return solution;
	}

	private ISolution mutateRoomStability(ISolution solution) {

		IProblemInstance pi = solution.getProblemInstance();
		ICourse[][] courses = solution.getCoding();
		int roomY = 0, periodX = 0;
		ICurriculum myCurriculum = null;

		while (myCurriculum == null) {
			roomY = (int) (pi.getRooms().size() * Math.random());
			periodX = (int) (pi.getNumberOfPeriods() * Math.random());

			if (courses[periodX][roomY] != null) {
				Set<ICurriculum> cur = courses[periodX][roomY].getCurricula();
				int curriculumNr = (int) (cur.size() * Math.random());

				ICurriculum[] curr = (ICurriculum[]) cur.toArray();
				myCurriculum = curr[curriculumNr];
			}
		}
		// course[!][] --> course.length
		// course[][!] --> course[i].length

		for (int i = 0; i < courses.length; i++) {
			if (i != periodX) {

				for (int j = 0; j < courses[i].length; j++) {
					ICourse selectedCourse = courses[i][j];
					if (selectedCourse != null) {

						Set<ICurriculum> tmpCur = selectedCourse.getCurricula();
						ICurriculum[] tmpCurr = (ICurriculum[]) tmpCur
								.toArray();
						for (int k = 0; k < tmpCurr.length; k++) {
							if (tmpCurr[k].getId().equals(myCurriculum.getId())) {

								courses[i][j] = courses[i][roomY];
								courses[i][roomY] = selectedCourse;
								// No check neccessary because we only changed
								// the
								// room --> soft constraint!?
							}
						}
					}
				}
			}
		}

		ISolution newSolution = this.solution.createNewSolution(courses,
				solution.getProblemInstance());
		newSolution.setRecombinationCount(solution.getRecombinationCount() + 1);

		return newSolution;
	}

	private ISolution recombindation1(ISolution bestSolution,
			ISolution otherSolution) {

		ValidatorImpl vi = new ValidatorImpl();

		do {

			IProblemInstance pi1 = bestSolution.getProblemInstance();
			int periodX1 = (int) (pi1.getNumberOfPeriods() * Math.random());

			IProblemInstance pi2 = otherSolution.getProblemInstance();
			int periodX2 = (int) (pi2.getNumberOfPeriods() * Math.random());

			ICourse[] oldCourses = bestSolution.getCoding()[periodX1];
			ICourse[] newCourses = otherSolution.getCoding()[periodX2];
			bestSolution.getCoding()[periodX1] = otherSolution.getCoding()[periodX2];

			Set<ICourse> oldCourseSet = getCourseSet(oldCourses);
			Set<ICourse> newCourseSet = getCourseSet(newCourses);

			Set<ICourse> doubleInList = newCourseSet;
			doubleInList.removeAll(oldCourseSet);

			Set<ICourse> notInList = oldCourseSet;
			notInList.removeAll(newCourseSet);

			Iterator<ICourse> ite = notInList.iterator();

			for (int i = 0; i < bestSolution.getCoding().length; i++) {
				for (int j = 0; j < bestSolution.getCoding().length; j++) {
					if (doubleInList.contains(bestSolution.getCoding()[i][j])) {
						bestSolution.getCoding()[i][j] = null;
					} else if (bestSolution.getCoding()[i][j] == null) {
						if (ite.hasNext()) {
							bestSolution.getCoding()[i][j] = ite.next();
						}
					}
				}
			}
		} while (!vi.isValidSolution(bestSolution));

		return bestSolution;

	}

	private Set<ICourse> getCourseSet(ICourse[] courses) {
		Set<ICourse> back = new HashSet<ICourse>();
		for (int i = 0; i < courses.length; i++) {
			back.add(courses[i]);
		}
		return back;
	}

	private ISolution recombindation2(ISolution bestSolution,
			ISolution otherSolution) {
		ICourse[][] oldBestSolution;
		ValidatorImpl vi = new ValidatorImpl();
		Set<ICourse> savingList = new HashSet<ICourse>();
		for (int i = 0; i < bestSolution.getCoding().length; i++) {
			for (int j = 0; j < bestSolution.getCoding()[j].length; j++) {
				if (bestSolution.getCoding()[i][j] == null) {
					oldBestSolution = bestSolution.getCoding();
					if (otherSolution.getCoding()[i][j] != null) {

						bestSolution.getCoding()[i][j] = otherSolution
								.getCoding()[i][j];
						if (!vi.isValidSolution(bestSolution)) {
							bestSolution.getCoding()[i][j] = oldBestSolution[i][j];
						} else {
							Map<ICourse, CoursePosition> courseCountNew = countLectures(
									bestSolution.getCoding(), otherSolution
											.getCoding()[i][j].getId());
							int expectedSize = otherSolution.getCoding()[i][j]
									.getNumberOfLectures();
							if (courseCountNew.size() >= expectedSize) {// Gab
								// and
								// lectures

								Iterator<CoursePosition> iter = courseCountNew
										.values().iterator();

								while (iter.hasNext()) {
									CoursePosition me = iter.next();
									int tmpX = me.getX();
									int tmpY = me.getY();
									if ((tmpX != i) && (tmpY != j)) {

										savingList
												.add(bestSolution.getCoding()[tmpX][tmpY]);
										bestSolution.getCoding()[tmpX][tmpY] = null;
										otherSolution.getCoding()[i][j] = null;
									}
								}
							}
						}
					}
				}
			}
		}
		return bestSolution;
	}

	private Map<ICourse, CoursePosition> countLectures(ICourse[][] course,
			String id) {

		Map<ICourse, CoursePosition> positions = new HashMap<ICourse, CoursePosition>();

		for (int i = 0; i < course.length; i++) {
			for (int j = 0; j < course[j].length; j++) {
				if (course[i][j].getId().equals(id)) {
					positions.put(course[i][j], new CoursePosition(i, j));
				}
			}
		}
		return positions;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}
}
