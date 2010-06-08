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
	 *            solve. If a second argument is available it's treated as the
	 *            amount of milliseconds to sleep between each iteration.
	 * 
	 * @throws IllegalArgumentException
	 *             If the length of <tt>args</tt> is smaller than 1.
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			throw new IllegalArgumentException(
					"The program's first argument must be the name of the problem instance file to solve.");
		}
		long sleepTime = 0;
		if (args.length == 2) {
			sleepTime = Long.valueOf(args[1]);
		}

		setUpServices();

		try {
			run(args[0], sleepTime);
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
	private static void run(String fileName, long sleepMilliSeconds)
			throws IOException, NoFeasibleSolutionFoundException {

		ServiceLocator locator = ServiceLocator.getInstance();
		IReaderService reader = locator.getReaderService();
		IProblemInstance instance = reader.readInstance(fileName);

		for (int i = 0; i < ITERATIONS; i++) {

			// Variables for performance measurement.
			long startMillis;
			long time;

			System.out.println("");
			System.out.println("------ ITERATION " + i + " ------");

			startMillis = System.currentTimeMillis();
			IGeneratorService generator = locator.getGeneratorService();
			generator.fillSolutionTable(instance);
			time = System.currentTimeMillis() - startMillis;
			System.out.println("GENERATOR: Finished after " + time + "ms.");

			startMillis = System.currentTimeMillis();
			IEvaluatorService evaluator = locator.getEvaluatorService();
			evaluator.evaluateSolutions();
			time = System.currentTimeMillis() - startMillis;
			System.out.println("EVALUATOR: Finished after " + time + "ms.");

			startMillis = System.currentTimeMillis();
			ICrazyGenetistService genetist = locator.getCrazyGenetistService();
			genetist.recombineAndMutate();
			time = System.currentTimeMillis() - startMillis;
			System.out
					.println("CRAZY GENETIST: Finished after " + time + "ms.");

			startMillis = System.currentTimeMillis();
			ISolutionTableService solutionTable = locator
					.getSolutionTableService();
			solutionTable.update();
			time = System.currentTimeMillis() - startMillis;
			System.out.println("SOLUTION TABLE (Update): Finished after "
					+ time + "ms.");

			startMillis = System.currentTimeMillis();
			IEliminatorService eliminatorService = locator
					.getEliminatorService();
			eliminatorService.eliminateSolutions();
			time = System.currentTimeMillis() - startMillis;
			System.out.println("ELIMINATOR: Finished after " + time + "ms.");

			printBestSolution();
			printFairestSolution();

			shortSleep(sleepMilliSeconds);
		}

		IWriterService writer = locator.getWriterService();
		writer.outputBestSolution();
	}

	private static void printBestSolution() {
		ISolutionTableService solutionTable = ServiceLocator.getInstance()
				.getSolutionTableService();

		System.out.println("----------------------------");
		System.out.println("-- Best Penalty Solution: Penalty: "
				+ solutionTable.getBestPenaltySolutionPenalty()
				+ ", Fairness: "
				+ solutionTable.getBestPenaltySolutionFairness());
	}

	private static void printFairestSolution() {
		ISolutionTableService solutionTable = ServiceLocator.getInstance()
				.getSolutionTableService();

		System.out.print("-- Best Fairness Solution: Penalty: "
				+ solutionTable.getBestFairnessSolutionPenalty()
				+ ", Fairness: "
				+ solutionTable.getBestFairnessSolutionFairness() + "\n");
		System.out.println("");
	}

	private static void shortSleep(long sleepMilliSeconds) {
		try {
			Thread.sleep(sleepMilliSeconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}