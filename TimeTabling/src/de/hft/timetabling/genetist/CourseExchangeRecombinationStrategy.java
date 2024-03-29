package de.hft.timetabling.genetist;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;
import de.hft.timetabling.util.HardConstraintUtil;
import de.hft.timetabling.util.PeriodUtil;

/**
 * This recombination strategy performs recombination by taking one half of all
 * courses from one solution and the other half from the other solution.
 * <p>
 * Of course, multiple hard constraint violations can occur during this
 * procedure. The individual lectures are assigned to the new solution one after
 * the other. When placing each assignment, it is checked if this specific
 * assignment will be hard constraint valid. If it is not, the lecture to be
 * assigned will be stored in a separate list containing all lectures yet to be
 * assigned. The position where that course should be originally assigned is
 * also stored. The same will be done, if a course cannot be placed because
 * there is already another assignment belonging to those provided by the other
 * solution.
 * <p>
 * At the end of the recombination an attempt is made to assign all remaining
 * lectures. Starting from the original locations where they should have been
 * assigned but where it was not possible, new locations are computed. This is
 * done by first checking the other rooms in the period. If this fails it will
 * be either placed at a lower period or at a higher period. This depends on the
 * distance to the nearest valid empty slot, which will be computed.
 * <p>
 * If at least one lecture cannot be assigned, <tt>null</tt> will be returned as
 * the result of the recombination. In this case the two solutions can be
 * considered as being not compatible with each other.
 * 
 * @author Alexander Weickmann
 */
public final class CourseExchangeRecombinationStrategy extends
		RecombinationStrategy {

	/** Value between 0.0 and 1.0. */
	private static final double START_MUTATION_PROBABILITY = 0.05;

	private static final int START_MIN_ELIMINATION_AGE = 4;

	/** Value between 0 and 100. */
	private static final int RECOMBINATION_PERCENTAGE = 85;

	private static final int SOLUTION_TABLE_SIZE = 50;

	private double mutationProbability;

	private int minEliminationAge;

	/**
	 * List of lectures that could not be assigned during the first step of the
	 * algorithm.
	 */
	private final List<Lecture> notAssignedLectures;

	/** Set of courses that shall be provided by solution 1. */
	private final Set<ICourse> courses1;

	/** Set of courses that shall be provided by solution 2. */
	private final Set<ICourse> courses2;

	private IProblemInstance instance;

	private ICourse[][] solution1Coding;

	private ICourse[][] solution2Coding;

	/** The coding that is being build during the algorithm. */
	private ICourse[][] childCoding;

	public CourseExchangeRecombinationStrategy() {
		courses1 = new HashSet<ICourse>();
		courses2 = new HashSet<ICourse>();
		notAssignedLectures = new LinkedList<Lecture>();
	}

	@Override
	protected void reset() {
		instance = null;
		courses1.clear();
		courses2.clear();
		solution1Coding = null;
		solution2Coding = null;
		childCoding = null;
		notAssignedLectures.clear();
	}

	@Override
	public ISolution recombine(final ISolution solution1,
			final ISolution solution2) {
		instance = solution1.getProblemInstance();
		solution1Coding = solution1.getCoding();
		solution2Coding = solution2.getCoding();

		final int nrPeriods = instance.getNumberOfPeriods();
		final int nrRooms = instance.getNumberOfRooms();
		childCoding = new ICourse[nrPeriods][nrRooms];

		determineCourseSets();

		performRecombination();

		ISolution childSolution = null;
		final boolean success = assignNotAssignedLectures();
		if (success) {
			childSolution = getSolutionTable().createNewSolution(childCoding,
					instance);
		}

		return childSolution;
	}

	/** Determines the course sets that will be provided by each solution. */
	private void determineCourseSets() {
		final Set<ICourse> allCourses = instance.getCourses();
		final int halfSize = allCourses.size() / 2;
		int i = 0;
		for (final ICourse course : allCourses) {
			if (i < halfSize) {
				courses1.add(course);
			} else {
				courses2.add(course);
			}
			i++;
		}
	}

	/** Executes the first step of the recombination. */
	private void performRecombination() {
		for (int period = 0; period < instance.getNumberOfPeriods(); period++) {
			for (int room = 0; room < instance.getNumberOfRooms(); room++) {

				ICourse solution1Course = solution1Coding[period][room];
				ICourse solution2Course = solution2Coding[period][room];

				// 1) No lecture in this slot at all.
				if ((solution1Course == null) && (solution2Course == null)) {
					continue;
				}

				/*
				 * If a course of a solution is not part of the courses assigned
				 * to this solution then we are not interested in that course.
				 */
				if (solution1Course != null) {
					if (!(courses1.contains(solution1Course))) {
						solution1Course = null;
					}
				}
				if (solution2Course != null) {
					if (!(courses2.contains(solution2Course))) {
						solution2Course = null;
					}
				}

				// 2) There are no relevant courses in this slot.
				if ((solution1Course == null) && (solution2Course == null)) {
					continue;
				}

				// 3) Relevant assignment only in solution 1.
				if ((solution1Course != null) && (solution2Course == null)) {
					recombineOne(solution1Course, period, room);
					continue;
				}

				// 4) Relevant assignment only in solution 2.
				if ((solution1Course == null) && (solution2Course != null)) {
					recombineOne(solution2Course, period, room);
					continue;
				}

				// 5) Relevant assignments in both solutions.
				if ((solution1Course != null) && (solution2Course != null)) {
					recombineTwo(solution1Course, solution2Course, period, room);
					continue;
				}
			}
		}
	}

	/**
	 * Handles the situation in which only one solution offers a relevant
	 * assignment.
	 */
	private void recombineOne(final ICourse course, final int period,
			final int room) {
		final boolean success = assign(course, period, room);
		if (!(success)) {
			final Lecture lecture = new Lecture(course, period, room);
			notAssignedLectures.add(lecture);
		}
	}

	/**
	 * Handles the situation in which both solutions offer relevant assignments.
	 */
	private void recombineTwo(final ICourse solution1Course,
			final ICourse solution2Course, final int period, final int room) {

		ICourse courseToAssign = solution1Course;
		ICourse courseNotAssigned = solution2Course;
		final Random random = new Random();
		if (random.nextBoolean()) {
			courseToAssign = solution2Course;
			courseNotAssigned = solution1Course;
		}

		final boolean success = assign(courseToAssign, period, room);
		if (success) {
			notAssignedLectures
					.add(new Lecture(courseNotAssigned, period, room));
		} else {
			notAssignedLectures.add(new Lecture(courseToAssign, period, room));
			final boolean successOther = assign(courseNotAssigned, period, room);
			if (!(successOther)) {
				notAssignedLectures.add(new Lecture(courseNotAssigned, period,
						room));
			}
		}
	}

	/**
	 * Attempts to assign those lectures that could not be assigned during the
	 * recombination process. If at least one lecture cannot be assigned,
	 * <tt>false</tt> is returned.
	 */
	private boolean assignNotAssignedLectures() {
		for (final Lecture lecture : notAssignedLectures) {
			final ICourse course = lecture.getCourse();
			final TimeTableSlot slot = lecture.getSlot();

			/*
			 * First try to assign the lecture to another room in the same
			 * period.
			 */
			if (assignToFreeValidSlotInPeriod(slot.getPeriod(), course)) {
				continue;
			}

			final TimeTableSlot newSlot = findNearestFreeValidSlot(slot, course);
			if (newSlot == null) {
				return false;
			}
			safeAssign(course, newSlot);
		}
		return true;
	}

	private TimeTableSlot findNearestFreeValidSlot(
			final TimeTableSlot baseSlot, final ICourse course) {

		final TimeTableSlot nextFree = findNextFreeValidSlot(baseSlot, course,
				true);
		if (nextFree == null) {
			return null;
		}
		final TimeTableSlot previousFree = findNextFreeValidSlot(baseSlot,
				course, false);
		if (Math.abs(baseSlot.getPeriod() - nextFree.getPeriod()) > Math
				.abs(baseSlot.getPeriod() - previousFree.getPeriod())) {
			return previousFree;
		}
		return nextFree;
	}

	private TimeTableSlot findNextFreeValidSlot(final TimeTableSlot baseSlot,
			final ICourse course, final boolean directionNext) {

		final int startPeriod = baseSlot.getPeriod();
		final int numberOfPeriods = instance.getNumberOfPeriods();
		int nextPeriod = directionNext ? PeriodUtil.getNextPeriod(startPeriod,
				numberOfPeriods) : PeriodUtil.getPreviousPeriod(startPeriod,
				numberOfPeriods);
		while (!(nextPeriod == startPeriod)) {
			for (int room = 0; room < instance.getNumberOfRooms(); room++) {
				if (isValidToAssign(course, nextPeriod, room)) {
					return new TimeTableSlot(nextPeriod, room);
				}
			}
			nextPeriod = directionNext ? PeriodUtil.getNextPeriod(nextPeriod,
					numberOfPeriods) : PeriodUtil.getPreviousPeriod(nextPeriod,
					numberOfPeriods);
		}
		return null;
	}

	/** Tries to assign the given course to some room in the given period. */
	private boolean assignToFreeValidSlotInPeriod(final int period,
			final ICourse course) {
		boolean success = false;
		for (int room = 0; room < childCoding[period].length; room++) {
			success = assign(course, period, room);
			if (success) {
				break;
			}
		}
		return success;
	}

	/**
	 * Tries to assign the given course at the given period and room. Returns
	 * <tt>true</tt> if successful. Returns <tt>false</tt> if the assignment was
	 * not possible due to hard constraint violation or because there is already
	 * an assignment.
	 */
	private boolean assign(final ICourse course, final int period,
			final int room) {
		if (isValidToAssign(course, period, room)) {
			safeAssign(course, new TimeTableSlot(period, room));
			return true;
		}
		return false;
	}

	/**
	 * This method is based on the assumption that it is called only when it is
	 * safe to assign the given course to the given slot.
	 */
	private void safeAssign(final ICourse course, final TimeTableSlot slot) {
		if (course == null) {
			throw new NullPointerException();
		}
		childCoding[slot.getPeriod()][slot.getRoom()] = course;
	}

	/**
	 * Checks if it is possible to assign the given course to the given period
	 * and room without violating any hard constraints or overwriting an
	 * existing assignment.
	 */
	private boolean isValidToAssign(final ICourse course, final int period,
			final int room) {
		if (childCoding[period][room] != null) {
			return false;
		}
		final boolean teacherViolated = HardConstraintUtil
				.existsTeacherInPeriod(childCoding, course.getTeacher(), period);
		final boolean curriculaViolated = HardConstraintUtil
				.existsCurriculaInPeriod(childCoding, course.getCurricula(),
						period);
		final boolean unavailabilityConstraintViolated = HardConstraintUtil
				.existsUnavailabilityConstraint(course, period);
		return !(teacherViolated || unavailabilityConstraintViolated || curriculaViolated);
	}

	@Override
	protected ISolution mutate(ISolution recombinedSolution) {
		if (Math.random() < mutationProbability) {
			recombinedSolution = MutationOperators
					.mutateRoomStability(recombinedSolution);
			final double randomValue = Math.random();
			if (randomValue < 0.05) {
				recombinedSolution = MutationOperators
						.mutateCourseIsolation(recombinedSolution);
			}
		}
		return recombinedSolution;
	}

	@Override
	protected void configure() {
		final ISolutionTableService solutionTable = ServiceLocator
				.getInstance().getSolutionTableService();
		solutionTable.setMaximumSize(SOLUTION_TABLE_SIZE);
	}

	@Override
	protected void newInterationStarted(final int iteration,
			final int totalIterations) {
		if (iteration == 1) {
			mutationProbability = START_MUTATION_PROBABILITY;
			minEliminationAge = START_MIN_ELIMINATION_AGE;
		}

		if (iteration % (totalIterations / 6) == 0) {
			minEliminationAge--;
		}

		/*
		 * Slightly increasing the probability to mutate as the time goes on so
		 * we explore new things. At some time we need to stop increasing the
		 * probability however.
		 */
		if (mutationProbability < 0.42) {
			mutationProbability += 0.0025;
		}
	}

	@Override
	protected void eliminate(final ISolution parent1, final ISolution parent2,
			final Set<ISolution> eliminatedSolutions) {

		final ISolution worstSolution = getSolutionTable().removeWorstSolution(
				minEliminationAge);
		eliminatedSolutions.add(worstSolution);

		/*
		 * With some probability we additionally want to remove a solution with
		 * a high recombination count. This way we want to avoid that the
		 * solutions get too one-sided.
		 */
		if (Math.random() < 0.40) {
			final ISolution mostRecombinedSolution = getSolutionTable()
					.getSolutionMostOftenRecombined();
			/*
			 * Only eliminate if the solution is really old cause we don't want
			 * to loose a good rooster that hasn't got the chance to recombine
			 * that much yet.
			 */
			if (mostRecombinedSolution.getRecombinationCount() > (SOLUTION_TABLE_SIZE / 2)) {
				getSolutionTable().remove(mostRecombinedSolution);
				eliminatedSolutions.add(mostRecombinedSolution);
			}
		}
	}

	@Override
	public int getRecombinationPercentage() {
		return RECOMBINATION_PERCENTAGE;
	}

	@Override
	public String getName() {
		return "Course Exchange v8";
	}

}
