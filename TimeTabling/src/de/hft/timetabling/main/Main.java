package de.hft.timetabling.main;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.IRoom;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.evaluator.EvaluateSoftConstrains;
import de.hft.timetabling.generator.Generator2;
import de.hft.timetabling.generator.NoFeasibleSolutionFoundException;
import de.hft.timetabling.genetist.CrazyGenetist;
import de.hft.timetabling.genetist.ValidatorImpl;
import de.hft.timetabling.reader.Reader;
import de.hft.timetabling.services.IReaderService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;
import de.hft.timetabling.services.SolutionTable;
import de.hft.timetabling.writer.Writer;

/**
 * The main class contains the main function that's needed to start the program.
 * 
 * @author Alexander Weickmann
 */
public final class Main {

	public static void main(String[] args) {
		setUpServices();

		try {
			exampleUsage();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			TestRun();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * At first, the individual sub systems must be initialized and registered
	 * with the service locator.
	 */
	private static void setUpServices() {
		ServiceLocator serviceLocator = ServiceLocator.getInstance();
		serviceLocator.setReaderService(new Reader());
		serviceLocator.setSolutionTableService(new SolutionTable());
		serviceLocator.setWriterService(new Writer());
		serviceLocator.setGeneratorService(new Generator2());
		serviceLocator.setValidatorService(new ValidatorImpl());
	}

	/**
	 * Small test to show how the {@link IProblemInstance} API works.
	 */
	private static void exampleUsage() throws IOException {
		IReaderService readerService = ServiceLocator.getInstance()
				.getReaderService();
		IProblemInstance problemInstance = readerService
				.readInstance("test.txt");

		System.out.println("GENERAL INFORMATION");
		System.out.println("Name: " + problemInstance.getName());
		System.out.println("Courses: " + problemInstance.getNumberOfCourses());
		System.out.println("Rooms: " + problemInstance.getNumberOfRooms());
		System.out.println("Days: " + problemInstance.getNumberOfDays());
		System.out.println("Periods per day: "
				+ problemInstance.getPeriodsPerDay());
		System.out.println("Curricula: "
				+ problemInstance.getNumberOfCurricula());
		System.out.println("Constraints: "
				+ problemInstance.getNumberOfConstraints());
		System.out.println("");

		System.out.println("COURSES");
		for (ICourse course : problemInstance.getCourses()) {
			System.out.println(course.getId() + " " + course.getTeacher() + " "
					+ course.getNumberOfLectures() + " "
					+ course.getMinWorkingDays() + " "
					+ course.getNumberOfStudents());
		}
		System.out.println("");

		System.out.println("ROOMS");
		for (IRoom room : problemInstance.getRooms()) {
			System.out.println(room.getId() + " " + room.getCapacity());
		}
		System.out.println("");

		System.out.println("CURRICULA");
		for (ICurriculum curriculum : problemInstance.getCurricula()) {
			System.out.print(curriculum.getId() + " "
					+ curriculum.getNumberOfCourses());
			for (ICourse course : curriculum.getCourses()) {
				System.out.print(" " + course.getId());
			}
			System.out.print("\n");
		}
		System.out.println("");

		System.out.println("UNAVAILABILITY CONSTRAINTS");
		for (ICourse course : problemInstance.getCourses()) {
			Set<Integer> unavailabilityConstraints = problemInstance
					.getUnavailabilityConstraints(course);
			if (unavailabilityConstraints.size() > 0) {
				System.out.print(course.getId());
				for (Integer forbiddenPeriod : unavailabilityConstraints) {
					System.out.print(" " + forbiddenPeriod);
				}
				System.out.print("\n");
			}
		}
	}

	/**
	 * Method to create a solutionTable, generate solutions and insert them into
	 * the Solution Table
	 * 
	 * @throws IOException
	 * 
	 * @author Roy
	 * 
	 */

	private static void TestRun() throws IOException { // initial service
		// call and instance instantiation
		IReaderService readerService = ServiceLocator.getInstance()
				.getReaderService();
		IProblemInstance problemInstance = readerService
				.readInstance("comp01.ctt");

		// declare the generator
		Generator2 g = new Generator2();
		ISolution solution;
		ISolutionTableService solutionTable;
		ICourse course[][] = null;

		// declare evaluator
		EvaluateSoftConstrains eval = new EvaluateSoftConstrains();
		Set<ICurriculum> currentCurriculumSet;
		ICurriculum currentCurricula;
		int iCost = 0;

		currentCurriculumSet = problemInstance.getCurricula();
		// Create the solutionTable and instantiate it
		solutionTable = ServiceLocator.getInstance().getSolutionTableService();

		// Call the generator
		try {
			course = g.generateFeasibleSolution(problemInstance);
		} catch (NoFeasibleSolutionFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		solution = solutionTable.createNewSolution(course, problemInstance);

		// Create the solutionTable, getting issue here.
		// solutionTable.createNewSolution(course, problemInstance);
		solutionTable.putSolution(0, solution);

		// Call the generator
		try {
			course = g.generateFeasibleSolution(problemInstance);
		} catch (NoFeasibleSolutionFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		solution = solutionTable.createNewSolution(course, problemInstance);
		solutionTable.putSolution(1, solution);

		// solutionTable.
		// call the eval for each curriculum
		Iterator<ICurriculum> it = currentCurriculumSet.iterator();
		while (it.hasNext()) {
			currentCurricula = it.next();
			// Penalty calculation for first solution
			iCost = 0;
			iCost += eval.CostsOnRoomCapacity(solutionTable.getSolution(0),
					currentCurricula);
			iCost += eval.CostsOnMinWorkingDays(solutionTable.getSolution(0),
					currentCurricula);
			solutionTable.voteForSolution(0, iCost);

			// Penalty calculation for second solution
			iCost = 0;
			iCost += eval.CostsOnRoomCapacity(solutionTable.getSolution(1),
					currentCurricula);
			iCost += eval.CostsOnMinWorkingDays(solutionTable.getSolution(1),
					currentCurricula);
			solutionTable.voteForSolution(1, iCost);
		}
		iCost = solutionTable.getPenaltySumForSolution(0);
		System.out.println("Penalty for solution 0: " + iCost);
		iCost = solutionTable.getPenaltySumForSolution(1);
		System.out.println("Penalty for solution 1: " + iCost);

		// Test CrazyGenetist

		CrazyGenetist cg = new CrazyGenetist();
		// --> EvaluatorInput
		for (int i = 0; i < 1000; i++) {
			System.out.println("Test " + i);
			cg.startRecombination(solutionTable);

		}
	}

}