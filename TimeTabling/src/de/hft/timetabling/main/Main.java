package de.hft.timetabling.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.evaluator.Evaluator;
import de.hft.timetabling.evaluator.MultiThreadedEvaluator;
import de.hft.timetabling.generator.Generator;
import de.hft.timetabling.generator.MultiThreadedGenerator;
import de.hft.timetabling.genetist.CrazyGenetist;
import de.hft.timetabling.reader.Reader;
import de.hft.timetabling.services.ICrazyGenetistService;
import de.hft.timetabling.services.IReaderService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.IValidatorService;
import de.hft.timetabling.services.IWriterService;
import de.hft.timetabling.services.ServiceLocator;
import de.hft.timetabling.solutiontable.SolutionTable;
import de.hft.timetabling.util.DateUtil;
import de.hft.timetabling.validator.Validator;
import de.hft.timetabling.writer.Writer;

/**
 * The main class contains the main function that's needed to start the program.
 * 
 * @author Alexander Weickmann
 */
public final class Main {

	// band-aid solution since interfaces of both evaluators are incompatible
	private static final boolean NEW_EVALUATOR = true;
	private static MultiThreadedEvaluator evaluator = null;

	public static int generatorSuccess = 0;

	public static int generatorFailure = 0;

	public static int recombinationSuccess = 0;

	public static int recombinationFailure = 0;

	public static int mutationSuccess = 0;

	public static int mutationFailure = 0;

	public static int solutionTableInsertionSuccess = 0;

	public static int solutionTableInsertionFailure = 0;

	public static long duration = 0;

	/**
	 * The number of iterations to perform until the best solution will be
	 * printed.
	 */
	public static int iterations = 250;

	public static int nrExecutions = 0;

	public static String initialSolutionDirectory = "";

	public static boolean outputAllSolutions = false;

	/**
	 * Runs the program.
	 * 
	 * @param args
	 *            1) The first argument is the name of the problem instance file
	 *            to solve. 2) If a second argument is available it is treated
	 *            as the number of iterations to perform. 3) If a third argument
	 *            is available it's treated as the number of times to batch-run
	 *            the program 4) If a fourth argument is available it's treated
	 *            as the amount of milliseconds to sleep between each iteration.
	 *            5) If a fifth argument is provided it is treated as a flag
	 *            specifying whether to output only the best solutions (0) or
	 *            all solutions (1) 6) If a sixth argument is provided it is
	 *            treated as the name of the directory where initial solutions
	 *            shall be read from.
	 * 
	 * @throws IllegalArgumentException
	 *             If the length of <tt>args</tt> is smaller than 1.
	 */
	public static void main(final String[] args) {
		if (args.length < 1) {
			throw new IllegalArgumentException(
					"The program's first argument must either be the "
							+ "name of the problem instance file to solve or "
							+ "'ALL' to run all instances.");
		}

		long sleepTime = 0;
		Main.nrExecutions = 1;
		if (args.length >= 2) {
			Main.iterations = Integer.valueOf(args[1]);
			if (args.length >= 3) {
				Main.nrExecutions = Integer.valueOf(args[2]);
				if (args.length >= 4) {
					sleepTime = Long.valueOf(args[3]);
					if (args.length >= 5) {
						Main.outputAllSolutions = Integer.valueOf(args[4]) > 0;
						if (args.length == 6) {
							Main.initialSolutionDirectory = args[5];
						}
					}
				}
			}
		}

		Main.setUpServices();

		for (int i = 0; i < Main.nrExecutions; i++) {
			try {
				if (args[0].equals("ALL")) {
					Main.runAllInstances(Main.initialSolutionDirectory);
				} else {
					Main.run(args[0], Main.initialSolutionDirectory, sleepTime);

				}
			} catch (final IOException e) {
				Main.handleException(e);
			}
		}

		// MR: band-aid fix until I find out why the app does not terminate
		System.exit(0);
	}

	private static void handleException(final Exception e) {
		e.printStackTrace();
	}

	/**
	 * At first, the individual sub systems must be initialized and registered
	 * with the service locator.
	 */
	private static void setUpServices() {
		final ServiceLocator serviceLocator = ServiceLocator.getInstance();
		serviceLocator.setReaderService(new Reader());
		serviceLocator.setSolutionTableService(new SolutionTable());
		serviceLocator.setWriterService(new Writer());
		serviceLocator.setGeneratorService(new MultiThreadedGenerator(
				new Generator()));
		serviceLocator.setValidatorService(new Validator());
		serviceLocator.setCrazyGenetistService(new CrazyGenetist());
		serviceLocator.setEvaluatorService(new Evaluator());
	}

	/**
	 * Runs the main loop of the program.
	 */
	private static void run(final String fileName,
			final String initialSolutionDirectory, final long sleepMilliSeconds)
			throws IOException {

		Main.resetStatistics();
		Main.getSolutionTable().clear();

		final long startTime = System.currentTimeMillis();

		final ServiceLocator locator = ServiceLocator.getInstance();
		final IReaderService reader = locator.getReaderService();
		final IProblemInstance instance = (initialSolutionDirectory.length() == 0) ? reader
				.readInstance(fileName)
				: reader.readInstanceUsingInitialSolutionDirectory(fileName,
						initialSolutionDirectory);

		for (int i = 0; i < Main.iterations; i++) {
			System.out.println("");
			System.out.println("------ ITERATION " + (i + 1) + " ------");

			Main.callGenerator(instance);

			Main.callEvaluator();

			Main.updateSolutionTable();

			Main.callCrazyGenetist(i + 1);

			Main.callEvaluator();

			Main.updateSolutionTable();

			Main.printBestSolution();
			Main.printFairestSolution();

			Main.shortSleep(sleepMilliSeconds);
		}

		Main.printStatistics();

		Main.checkBestSolutionForValidity();

		Main.duration = System.currentTimeMillis() - startTime;
		System.out.println("Duration: " + DateUtil.toTimeString(Main.duration));

		Main.outputSolutions();
	}

	private static void checkBestSolutionForValidity() {
		final ServiceLocator locator = ServiceLocator.getInstance();
		final ISolutionTableService solutionTable = locator
				.getSolutionTableService();
		final ISolution bestSolution = solutionTable.getBestPenaltySolution();
		final IValidatorService validator = locator.getValidatorService();
		if (!(validator.isValidSolution(bestSolution))) {
			System.out.println("VALIDATOR: Ups, the solution is not valid!");
		}
	}

	private static void resetStatistics() {
		Main.duration = 0;
		Main.generatorSuccess = 0;
		Main.generatorFailure = 0;
		Main.recombinationSuccess = 0;
		Main.recombinationFailure = 0;
		Main.solutionTableInsertionFailure = 0;
		Main.solutionTableInsertionSuccess = 0;
	}

	private static void callGenerator(final IProblemInstance instance) {
		final long startMillis = System.currentTimeMillis();
		ServiceLocator.getInstance().getGeneratorService().fillSolutionTable(
				instance);
		final long time = System.currentTimeMillis() - startMillis;
		System.out.println("GENERATOR: Finished after " + time + "ms.");
	}

	private static void callCrazyGenetist(final int iteration) {
		final long startMillis = System.currentTimeMillis();
		ServiceLocator.getInstance().getCrazyGenetistService()
				.recombineAndMutate(iteration, Main.iterations);
		final long time = System.currentTimeMillis() - startMillis;
		System.out.println("CRAZY GENETIST: Finished after " + time + "ms.");
	}

	private static void callEvaluator() {
		final long startMillis = System.currentTimeMillis();
		if (NEW_EVALUATOR) {
			if (evaluator == null) {
				evaluator = new MultiThreadedEvaluator();
			}
			evaluator.evaluateSolutions();
		} else {
			ServiceLocator.getInstance().getEvaluatorService()
					.evaluateSolutions();
		}

		final long time = System.currentTimeMillis() - startMillis;
		System.out.println("EVALUATOR: Finished after " + time + "ms.");
	}

	private static void outputSolutions() throws IOException {
		final IWriterService writer = ServiceLocator.getInstance()
				.getWriterService();
		if (Main.outputAllSolutions) {
			for (int i = 0; i < Main.getSolutionTable().getSize(false); i++) {
				writer.outputSolution(Main.getSolutionTable().getSolution(i));
				/*
				 * Sleep for 1 second to ensure that the time stamp for the file
				 * name is a new one.
				 */
				Main.shortSleep(1000);
			}
		} else {
			writer.outputBestSolution();
		}
	}

	private static void updateSolutionTable() {
		Main.getSolutionTable().update();
	}

	private static void printBestSolution() {
		System.out.println("----------------------------");
		System.out.println("-- Best Penalty Solution (Penalty / Fairness): "
				+ Main.getSolutionTable().getBestPenaltySolution().getPenalty()
				+ " / "
				+ Main.getSolutionTable().getBestPenaltySolution()
						.getFairness());
	}

	private static void printFairestSolution() {
		System.out.print("-- Best Fairness Solution (Penalty / Fairness): "
				+ Main.getSolutionTable().getBestFairnessSolution()
						.getPenalty()
				+ " / "
				+ Main.getSolutionTable().getBestFairnessSolution()
						.getFairness() + "\n");
	}

	private static ISolutionTableService getSolutionTable() {
		return ServiceLocator.getInstance().getSolutionTableService();
	}

	private static void printStatistics() {
		System.out.println();
		System.out.println();

		System.out.println("Algorithm terminated.");
		System.out.println("----------------------------");
		System.out.println("-- Best Penalty Solution (Penalty / Fairness): "
				+ Main.getSolutionTable().getBestPenaltySolution().getPenalty()
				+ " / "
				+ Main.getSolutionTable().getBestPenaltySolution()
						.getFairness());
		System.out.println("-- Best Fairness Solution (Penalty / Fairness): "
				+ Main.getSolutionTable().getBestFairnessSolution()
						.getPenalty()
				+ " / "
				+ Main.getSolutionTable().getBestFairnessSolution()
						.getFairness());

		System.out.println();

		System.out.println("-- Generator (Success / Failure): "
				+ Main.generatorSuccess + " / " + Main.generatorFailure + " ("
				+ Main.getGeneratorSuccessRatio() + " %)");

		System.out.println("-- Recombination (Success / Failure): "
				+ Main.recombinationSuccess + " / " + Main.recombinationFailure
				+ " (" + Main.getRecombinationSuccessRatio() + " %)");

		System.out.println("-- Mutation (Success / Failure): "
				+ Main.mutationSuccess + " / " + Main.mutationFailure + " ("
				+ Main.getMutationSuccessRatio() + " %)");

		System.out.println("-- Solution Table Insertion (Success / Failure): "
				+ Main.solutionTableInsertionSuccess + " / "
				+ Main.solutionTableInsertionFailure + " ("
				+ Main.getSolutionTableInsertionSuccessRatio() + "%)");
		System.out.println("----------------------------");

		System.out.println();
	}

	public static int getSolutionTableInsertionSuccessRatio() {
		final int total = Main.solutionTableInsertionSuccess
				+ Main.solutionTableInsertionFailure;
		if (total == 0) {
			return 0;
		}
		return (Main.solutionTableInsertionSuccess * 100) / total;
	}

	public static int getGeneratorSuccessRatio() {
		final int total = Main.generatorSuccess + Main.generatorFailure;
		if (total == 0) {
			return 0;
		}
		return (Main.generatorSuccess * 100) / total;
	}

	public static int getRecombinationSuccessRatio() {
		final int total = Main.recombinationSuccess + Main.recombinationFailure;
		if (total == 0) {
			return 0;
		}
		return (Main.recombinationSuccess * 100) / total;
	}

	public static int getMutationSuccessRatio() {
		final int total = Main.mutationSuccess + Main.mutationFailure;
		if (total == 0) {
			return 0;
		}
		return (Main.mutationSuccess * 100) / total;
	}

	private static void shortSleep(final long sleepMilliSeconds) {
		try {
			Thread.sleep(sleepMilliSeconds);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void runAllInstances(final String initialSolutionsDirectory)
			throws IOException {

		final String logFileName = "doc/logs/allinstances_"
				+ DateUtil.getTimeStamp(new Date()) + ".txt";

		final File logFile = new File(logFileName);
		if (logFile.exists()) {
			logFile.delete();
			logFile.createNewFile();
		}

		final File instancesDir = new File("instances");

		final File[] instanceFiles = instancesDir
				.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(final File dir, final String name) {
						return name.endsWith(".ctt");
					}
				});

		final BufferedWriter writer = new BufferedWriter(
				new FileWriter(logFile));

		Main.createLogFileHeader(writer);

		long totalDuration = 0;
		int totalPenalty = 0;
		int totalFairness = 0;
		for (final File instanceFile : instanceFiles) {
			Main.run("instances/" + instanceFile.getName(),
					initialSolutionsDirectory, 0);
			totalDuration += Main.duration;
			totalPenalty += Main.getSolutionTable().getBestPenaltySolution()
					.getPenalty();
			totalFairness += Main.getSolutionTable().getBestPenaltySolution()
					.getFairness();
			Main.writeResult(writer, instanceFile, Main.duration);
		}

		Main.createLogFileFooter(writer, totalDuration, totalPenalty,
				totalFairness);

		writer.close();
	}

	private static void createLogFileHeader(final BufferedWriter writer)
			throws IOException {

		final ServiceLocator serviceLocator = ServiceLocator.getInstance();

		writer.write("Log file created on " + new Date());
		writer.newLine();
		writer.write("--------------------------------------------------");
		writer.newLine();
		writer.newLine();
		writer.write("Maximum Solution Table Size: "
				+ serviceLocator.getSolutionTableService().getMaximumSize());
		writer.newLine();
		writer.write("Iterations: " + Main.iterations);
		writer.newLine();
		writer.write("Reproduction: "
				+ serviceLocator.getCrazyGenetistService()
						.getRecombinationPercentage() + "%");
		writer.newLine();
		writer.write("Strategy: "
				+ ICrazyGenetistService.RECOMBINATION_STRATEGY.getName());
		writer.newLine();
		writer.newLine();

		writer.flush();
	}

	private static void writeResult(final BufferedWriter writer,
			final File instanceFile, final long duration) throws IOException {

		final ISolutionTableService solutionTable = Main.getSolutionTable();

		writer.write(instanceFile.getName());

		writer.write(" (Duration: " + DateUtil.toTimeString(duration) + "):");
		writer.newLine();
		writer.write("--------------");
		writer.newLine();
		writer.write("Best penalty/penalty: "
				+ solutionTable.getBestPenaltySolution().getPenalty());
		writer.newLine();
		writer.write("Best penalty/fairness: "
				+ solutionTable.getBestPenaltySolution().getFairness());
		writer.newLine();
		writer.write("Best fairness/penalty: "
				+ solutionTable.getBestFairnessSolution().getPenalty());
		writer.newLine();
		writer.write("Best fairness/fairness: "
				+ solutionTable.getBestFairnessSolution().getFairness());
		writer.newLine();
		writer.write("Worst penalty/penalty: "
				+ solutionTable.getWorstPenaltySolution().getPenalty());
		writer.newLine();
		writer.write("Worst penalty/fairness: "
				+ solutionTable.getWorstPenaltySolution().getFairness());
		writer.newLine();
		writer.write("Worst fairness/penalty: "
				+ solutionTable.getWorstFairnessSolution().getPenalty());
		writer.newLine();
		writer.write("Worst fairness/fairness: "
				+ solutionTable.getWorstFairnessSolution().getFairness());
		writer.newLine();
		writer.write("Generator success: " + Main.generatorSuccess);
		writer.newLine();
		writer.write("Generator failure: " + Main.generatorFailure);
		writer.newLine();
		writer.write("Recombination success: " + Main.recombinationSuccess);
		writer.newLine();
		writer.write("Recombination failure: " + Main.recombinationFailure);
		writer.newLine();
		writer.write("Mutation success: " + Main.mutationSuccess);
		writer.newLine();
		writer.write("Mutation failure: " + Main.mutationFailure);
		writer.newLine();
		writer.newLine();
		writer.newLine();

		writer.flush();
	}

	private static void createLogFileFooter(final BufferedWriter writer,
			final long totalDuration, final int totalPenalty,
			final int totalFairness) throws IOException {

		writer.write("Total duration: " + DateUtil.toTimeString(totalDuration));
		writer.newLine();
		writer.write("Total penalty: " + totalPenalty);
		writer.newLine();
		writer.write("Total fairness: " + totalFairness);
	}

}