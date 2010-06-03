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

public class CrazyGenetist implements IGenetist {

	private ISolutionTableService solution;
	private double probability = 0.5;

	@Override
	public ISolution startRecombination(ISolutionTableService solution) {
		setSolution(solution);

		ISolution back = null;

		if (solution.getActualSolutionTableCount() > 1) {
			ISolution otherSolution = null;
			ISolution bestSolution = null;
			while ((otherSolution == null) || (bestSolution == null)
					|| bestSolution.equals(otherSolution)) {
				System.out.println("Trying to find solution to work with ...");
				int n = (int) (solution.getActualSolutionTableCount() * Math
						.random());

				bestSolution = solution.getBestSolution();
				otherSolution = solution.getSolution(n);

				System.out.println("... found " + n + " ...");
			}
			System.out.println(" ... take it!");
			if (Math.random() < probability) {
				try {
					back = mutateRoomStability(recombindation1(bestSolution,
							otherSolution));
				} catch (NoFeasibleRecombinationFoundException ex) {
					back = mutateRoomStability(recombindation2(bestSolution,
							otherSolution));
				}
			} else {
				back = mutateRoomStability(recombindation2(bestSolution,
						otherSolution));
			}

			bestSolution.increaseRecombinationCount();
			otherSolution.increaseRecombinationCount();
		}
		return back;

	}

	public void setSolution(ISolutionTableService solution) {
		this.solution = solution;
	}

	public ISolutionTableService getSolution() {
		return solution;
	}

	private ISolution mutateRoomStability(ISolution solution) {
		System.out.println("Starting mutation ...");
		IProblemInstance pi = solution.getProblemInstance();
		ICourse[][] courses = solution.getCoding();
		int roomY = 0, periodX = 0;
		ICurriculum myCurriculum = null;

		while (myCurriculum == null) {
			roomY = (int) (pi.getRooms().size() * Math.random());
			periodX = (int) (pi.getNumberOfPeriods() * Math.random());
			System.out.println("... trying to find curriculum [" + periodX
					+ "][" + roomY + "] ...");
			if (courses[periodX][roomY] != null) {
				Set<ICurriculum> cur = courses[periodX][roomY].getCurricula();

				for (ICurriculum asd : cur) {
					System.out.println("### " + asd.getId());
				}

				String curriculumNr = String.format("q%03d",
						(int) (cur.size() * Math.random()));

				myCurriculum = getCurriculumOutOfSet(cur, curriculumNr);
			}
		}

		System.out.println("... mutate ...");
		for (int i = 0; i < courses.length; i++) {
			if (i != periodX) {

				for (int j = 0; j < courses[i].length; j++) {
					ICourse selectedCourse = courses[i][j];
					if (selectedCourse != null) {

						Set<ICurriculum> tmpCur = selectedCourse.getCurricula();

						for (ICurriculum iCurriculum : tmpCur) {
							if (iCurriculum.getId()
									.equals(myCurriculum.getId())) {

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
		System.out.println("... mutation done.");
		return newSolution;
	}

	private ISolution recombindation1(ISolution bestSolution,
			ISolution otherSolution)
			throws NoFeasibleRecombinationFoundException {
		System.out.println("Start recombnination 1 process ...");
		ValidatorImpl vi = new ValidatorImpl();
		int iterationCount = 0;

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
				for (int j = 0; j < bestSolution.getCoding()[i].length; j++) {
					if (doubleInList.contains(bestSolution.getCoding()[i][j])) {
						bestSolution.getCoding()[i][j] = null;
					} else if (bestSolution.getCoding()[i][j] == null) {
						if (ite.hasNext()) {
							bestSolution.getCoding()[i][j] = ite.next();
						}
					}
				}
			}

			if (iterationCount > 1000) {
				throw new NoFeasibleRecombinationFoundException(
						"Recombination 1 was not successful.");
			}

			System.out.println("... recombindation1: did " + iterationCount++
					+ " iteration ...");
		} while (!vi.isValidSolution(bestSolution));
		System.out.println("... done with recombination 1 process.");
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
		System.out.println("Start recombnination 2 process ...");
		ICourse[][] oldBestSolution;
		ValidatorImpl vi = new ValidatorImpl();
		Set<ICourse> savingList = new HashSet<ICourse>();
		for (int i = 0; i < bestSolution.getCoding().length; i++) {
			for (int j = 0; j < bestSolution.getCoding()[i].length; j++) {
				System.out.println("... course[" + i + "][" + j + "]");
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
		System.out.println("... done with recombindation 2 process. ("
				+ checkForNullCourse(bestSolution) + ")");
		if (checkForNullCourse(bestSolution)) {
			System.out.println("ALL NULL");
		}
		return bestSolution;
	}

	private Map<ICourse, CoursePosition> countLectures(ICourse[][] course,
			String id) {

		Map<ICourse, CoursePosition> positions = new HashMap<ICourse, CoursePosition>();

		for (int i = 0; i < course.length; i++) {
			for (int j = 0; j < course[i].length; j++) {
				if ((course[i][j] != null) && course[i][j].getId().equals(id)) {
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

	public ICurriculum getCurriculumOutOfSet(Set<ICurriculum> set,
			ICurriculum searchedOne) {
		return getCurriculumOutOfSet(set, searchedOne.getId());
	}

	public ICurriculum getCurriculumOutOfSet(Set<ICurriculum> set,
			String searchedOneId) {
		for (ICurriculum iCurriculum : set) {
			if (iCurriculum.getId().equals(searchedOneId)) {
				return iCurriculum;
			}
		}
		return null;
	}

	private boolean checkForNullCourse(ISolution solution) {
		ICourse[][] course = solution.getCoding();
		for (int i = 0; i < course.length; i++) {
			for (int j = 0; j < course[i].length; j++) {
				if (course[i][j] != null) {
					return false;
				}
			}
		}
		return true;
	}

}
