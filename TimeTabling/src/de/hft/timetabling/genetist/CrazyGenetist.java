package de.hft.timetabling.genetist;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.ICrazyGenetistService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;

public class CrazyGenetist implements ICrazyGenetistService {

	private ISolutionTableService solution;

	private double probability = 0.5;

	@Override
	public ISolution recombineAndMutate() {

		int noFeasibleRecombinationFoundExceptionCounter = 0;

		ServiceLocator serviceLocator = ServiceLocator.getInstance();

		setSolution(serviceLocator.getSolutionTableService());

		ISolution back = null;

		if (solution.getActualSolutionTableCount() > 1) {
			ISolution otherSolution = null;
			ISolution bestSolution = null;
			while ((otherSolution == null) || (bestSolution == null)
					|| bestSolution.equals(otherSolution)) {
				System.out.println("Trying to find solution to work with ...");
				int n1 = (int) (solution.getActualSolutionTableCount() * Math
						.random());
				int n2 = (int) (solution.getActualSolutionTableCount() * Math
						.random());
				bestSolution = solution.getSolution(n1);
				otherSolution = solution.getSolution(n2);

				System.out.println("... found " + n1 + ", " + n2 + " ...");
			}
			System.out.println(" ... take it!");
			if (Math.random() < probability) {

				try {
					back = mutateRoomStability(recombindation1(bestSolution,
							otherSolution));
				} catch (NoFeasibleRecombinationFoundException e) {
					System.out
							.println("[Caught NoFeasibleRecombinationFoundException]");
					noFeasibleRecombinationFoundExceptionCounter++;
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
		System.out.println("Crazy Genetist is done. (Created "
				+ noFeasibleRecombinationFoundExceptionCounter
				+ " non feasible Solution with recombination1)");
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
			// System.out.println("... trying to find curriculum [" + periodX+
			// "][" + roomY + "] ...");
			if (courses[periodX][roomY] != null) {
				Set<ICurriculum> cur = courses[periodX][roomY].getCurricula();

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

	private ISolution recombindation1(ISolution solution1, ISolution solution2)
			throws NoFeasibleRecombinationFoundException {
		System.out.println("Start recombnination 1 process ...");
		ValidatorImpl vi = new ValidatorImpl();
		int iterationCount = 0;

		ISolution newSolution = null;

		do {

			newSolution = solution1.clone();
			IProblemInstance pi1 = newSolution.getProblemInstance();
			int periodX1 = (int) (pi1.getNumberOfPeriods() * Math.random());

			IProblemInstance pi2 = solution2.getProblemInstance();
			int periodX2 = (int) (pi2.getNumberOfPeriods() * Math.random());

			ICourse[] oldCourses = newSolution.getCoding()[periodX1];
			ICourse[] newCourses = solution2.getCoding()[periodX2];
			newSolution.getCoding()[periodX1] = solution2.getCoding()[periodX2];
			Set<ICourse> oldCourseSet = getCourseSet(oldCourses);
			Set<ICourse> newCourseSet = getCourseSet(newCourses);

			/*
			 * By the way you get an error here: It is a known bug by
			 * Eclipse/Java. Please go to Preferences > Java > Compiler >
			 * References/Warnings > Generic Types > Unchecked Generic type
			 * operation and set it to "Warning".
			 */
			Set<ICourse> doubleInList = ((Set<ICourse>) ((HashSet<ICourse>) newCourseSet)
					.clone());
			doubleInList.retainAll(oldCourseSet);

			Set<ICourse> notInList = ((Set<ICourse>) ((HashSet<ICourse>) oldCourseSet)
					.clone());
			notInList.removeAll(newCourseSet);

			Iterator<ICourse> ite = notInList.iterator();
			// Here is something wrong!

			for (int i = 0; i < newSolution.getCoding().length; i++) {
				for (int j = 0; j < newSolution.getCoding()[i].length; j++) {
					if (doubleInList.contains(newSolution.getCoding()[i][j])) {
						newSolution.getCoding()[i][j] = null;
					} else if (newSolution.getCoding()[i][j] == null) {
						if (ite.hasNext()) {
							newSolution.getCoding()[i][j] = ite.next();
						}
					}
				}
			}

			if (iterationCount++ > 100) {
				throw new NoFeasibleRecombinationFoundException(
						"Recombination 1 was not successful.");
			}

			// System.out.println("... recombindation1: did " + iterationCount +
			// " iteration ...");

		} while (!vi.isValidSolution(newSolution));
		System.out.println("... done with recombination 1 process.");
		return newSolution;

	}

	private Set<ICourse> getCourseSet(ICourse[] courses) {
		Set<ICourse> back = new HashSet<ICourse>();
		for (int i = 0; i < courses.length; i++) {
			back.add(courses[i]);
		}
		return back;
	}

	private ISolution recombindation2(ISolution solution1, ISolution solution2) {
		System.out.println("Start recombnination 2 process ...");
		ICourse[][] oldBestSolution;
		ValidatorImpl vi = new ValidatorImpl();
		Set<ICourse> savingList = new HashSet<ICourse>();
		ISolution newSolution = solution1.clone();
		for (int i = 0; i < newSolution.getCoding().length; i++) {
			for (int j = 0; j < newSolution.getCoding()[i].length; j++) {
				// System.out.println("... course[" + i + "][" + j + "] ...");
				if (newSolution.getCoding()[i][j] == null) {
					oldBestSolution = newSolution.getCoding();
					if (solution2.getCoding()[i][j] != null) {
						newSolution.getCoding()[i][j] = solution2.getCoding()[i][j];
						if (!vi.isValidSolution(newSolution)) {
							newSolution.getCoding()[i][j] = oldBestSolution[i][j];
						} else {
							Map<ICourse, CoursePosition> courseCountNew = countLectures(
									newSolution.getCoding(), solution2
											.getCoding()[i][j].getId());
							int expectedSize = solution2.getCoding()[i][j]
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
												.add(newSolution.getCoding()[tmpX][tmpY]);
										newSolution.getCoding()[tmpX][tmpY] = null;
										solution2.getCoding()[i][j] = null;
									}
								}
							}
						}
					}
				}
			}
		}
		System.out.println("... done with recombindation 2 process. ("
				+ !checkForNullCourse(newSolution) + ")");
		if (checkForNullCourse(newSolution)) {
			System.out.println("ALL NULL");
		}
		return newSolution;
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
