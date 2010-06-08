package de.hft.timetabling.main;

import java.io.IOException;

import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.eliminator.Eliminator;
import de.hft.timetabling.evaluator.Evaluator;
import de.hft.timetabling.generator.Generator;
import de.hft.timetabling.generator.NoFeasibleSolutionFoundException;
import de.hft.timetabling.genetist.CrazyGenetist;
import de.hft.timetabling.genetist.ValidatorImpl;
import de.hft.timetabling.reader.Reader;
import de.hft.timetabling.services.ICrazyGenetistService;
import de.hft.timetabling.services.IEliminatorService;
import de.hft.timetabling.services.IEvaluatorService;
import de.hft.timetabling.services.IGeneratorService;
import de.hft.timetabling.services.IReaderService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.IWriterService;
import de.hft.timetabling.services.ServiceLocator;
import de.hft.timetabling.services.SolutionTable;
import de.hft.timetabling.writer.Writer;

/**
 * The main class contains the main function that's needed to start the program.
 * 
 * @author Alexander Weickmann
 */
public final class Main {

	/**
	 * The number of iterations to perform until the best solution will be
	 * printed.
	 */
	private static final int ITERATIONS = 1000;

	/**
	 * Runs the program.
	 * 
	 * @param args
	 *            The first argument is the name of the problem instance file to
	 *            solve.
	 * 
	 * @throws IllegalArgumentException
	 *             If the length of <tt>args</tt> is not exactly 1.
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException(
					"The program first argument must be the name of the problem instance file to solve.");
		}
		setUpServices();
		try {
			run(args[0]);
		} catch (IOException e) {
			handleException(e);
		} catch (NoFeasibleSolutionFoundException e) {
			handleException(e);
		}
	}

	private static void handleException(Exception e) {
		e.printStackTrace();
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
		serviceLocator.setGeneratorService(new Generator());
		serviceLocator.setValidatorService(new ValidatorImpl());
		serviceLocator.setCrazyGenetistService(new CrazyGenetist());
		serviceLocator.setEvaluatorService(new Evaluator());
		serviceLocator.setEliminatorService(new Eliminator());
	}

	/**
	 * Runs the main loop of the program.
	 */
	private static void run(String fileName) throws IOException,
			NoFeasibleSolutionFoundException {

		ServiceLocator locator = ServiceLocator.getInstance();
		IReaderService reader = locator.getReaderService();
		IProblemInstance instance = reader.readInstance(fileName);

		for (int i = 0; i < ITERATIONS; i++) {
			System.out.println("");
			System.out.println("------ ITERATION " + i + " ------");

			// Fills empty slots in the solution table.
			IGeneratorService generator = locator.getGeneratorService();
			generator.fillSolutionTable(instance);

			IEvaluatorService evaluator = locator.getEvaluatorService();
			evaluator.evaluateSolutions();

			ICrazyGenetistService genetist = locator.getCrazyGenetistService();
			genetist.recombineAndMutate();

			ISolutionTableService solutionTable = locator
					.getSolutionTableService();
			System.out.println("----------------------------");
			System.out.println("-- Best solution penalty: "
					+ solutionTable.getBestSolutionPenaltySum()
					+ ", Fairness: " + solutionTable.getBestSolutionFairness());
			System.out.println("-- Fairest solution penalty: "
					+ solutionTable.getFairestSolutionPenalty()
					+ "and it\'s Fairness: "
					+ solutionTable.getFairestSolutionFairness());
			System.out.println("");

			IEliminatorService eliminatorService = locator
					.getEliminatorService();
			eliminatorService.eliminateSolutions();

			shortSleep();
		}

		IWriterService writer = locator.getWriterService();
		writer.outputBestSolution();
	}

	private static void shortSleep() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}