package de.hft.timetabling.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.evaluator.Evaluator;
import de.hft.timetabling.generator.PooledMtGenerator;
import de.hft.timetabling.generator.YetAnotherGenerator;
import de.hft.timetabling.genetist.CrazyGenetist;
import de.hft.timetabling.reader.Reader;
import de.hft.timetabling.services.ICrazyGenetistService;
import de.hft.timetabling.services.IReaderService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.IWriterService;
import de.hft.timetabling.services.ServiceLocator;
import de.hft.timetabling.solutiontable.SolutionTable;
import de.hft.timetabling.validator.Validator;
import de.hft.timetabling.writer.Writer;

/**
 * The main class contains the main function that's needed to start the program.
 * 
 * @author Alexander Weickmann
 */
public final class Main {

	public static int generatorSuccess = 0;

	public static int generatorFailure = 0;

	public static int mutateRecombineSuccess = 0;

	public static int mutateRecombineFailure = 0;

	public static int solutionTableInsertionSuccess = 0;

	public static int solutionTableInsertionFailure = 0;

	/**
	 * The number of iterations to perform until the best solution will be
	 * printed.
	 */
	private static int iterations = 1000;

	/**
	 * Runs the program.
	 * 
	 * @param args
	 *            1) The first argument is the name of the problem instance file
	 *            to solve. 2) If a second argument is available it is treated
	 *            as the number of iterations to perform. 3) If a third argument
	 *            is available it's treated as the amount of milliseconds to
	 *            sleep between each iteration. 4) If a fourth argument is
	 *            provided it is treated as the name of the directory where
	 *            initial solutions shall be read from.
	 * 
	 * @throws IllegalArgumentException
	 *             If the length of <tt>args</tt> is smaller than 1.
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			throw new IllegalArgumentException(
					"The program's first argument must either be the "
							+ "name of the problem instance file to solve or "
							+ "'ALL' to run all instances.");
		}

		long sleepTime = 0;
		String initialSolutionDirectory = null;

		if (args.length >= 2) {
			iterations = Integer.valueOf(args[1]);
			if (args.length >= 3) {
				sleepTime = Long.valueOf(args[2]);
				if (args.length == 4) {
					initialSolutionDirectory = args[3];
				}
			}
		}

		setUpServices();

		try {
			if (args[0].equals("ALL")) {
				runAllInstances(initialSolutionDirectory);
			} else {
				run(args[0], initialSolutionDirectory, sleepTime);
			}
		} catch (IOException e) {
			handleException(e);
		}

		// MR: band-aid fix until I find out why the app does not terminate
		System.exit(0);
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
		serviceLocator.setGeneratorService(new PooledMtGenerator(
				new YetAnotherGenerator()));
		serviceLocator.setValidatorService(new Validator());
		serviceLocator.setCrazyGenetistService(new CrazyGenetist());
		serviceLocator.setEvaluatorService(new Evaluator());
	}

	/**
	 * Runs the main loop of the program. Returns the duration of the program in
	 * milliseconds.
	 */
	private static long run(String fileName, String initialSolutionDirectory,
			long sleepMilliSeconds) throws IOException {

		resetStatistics();

		long startTime = System.currentTimeMillis();

		ServiceLocator locator = ServiceLocator.getInstance();
		IReaderService reader = locator.getReaderService();
		IProblemInstance instance = (initialSolutionDirectory == null) ? reader
				.readInstance(fileName) : reader
				.readInstanceUsingInitialSolutionDirectory(fileName,
						initialSolutionDirectory);

		getSolutionTable().clear();

		for (int i = 0; i < iterations; i++) {
			System.out.println("");
			System.out.println("------ ITERATION " + i + " ------");

			callGenerator(instance);

			callEvaluator();

			updateSolutionTable();

			callCrazyGenetist();

			callEvaluator();

			updateSolutionTable();

			printBestSolution();
			printFairestSolution();

			shortSleep(sleepMilliSeconds);
		}

		printStatistics();
		writeBestSolution();

		return System.currentTimeMillis() - startTime;
	}

	private static void resetStatistics() {
		generatorSuccess = 0;
		generatorFailure = 0;
		mutateRecombineSuccess = 0;
		mutateRecombineFailure = 0;
		solutionTableInsertionFailure = 0;
		solutionTableInsertionSuccess = 0;
	}

	private static void callGenerator(IProblemInstance instance) {
		long startMillis = System.currentTimeMillis();
		ServiceLocator.getInstance().getGeneratorService().fillSolutionTable(
				instance);
		long time = System.currentTimeMillis() - startMillis;
		System.out.println("GENERATOR: Finished after " + time + "ms.");
	}

	private static void callCrazyGenetist() {
		long startMillis = System.currentTimeMillis();
		ServiceLocator.getInstance().getCrazyGenetistService()
				.recombineAndMutate();
		long time = System.currentTimeMillis() - startMillis;
		System.out.println("CRAZY GENETIST: Finished after " + time + "ms.");
	}

	private static void callEvaluator() {
		long startMillis = System.currentTimeMillis();
		ServiceLocator.getInstance().getEvaluatorService().evaluateSolutions();
		long time = System.currentTimeMillis() - startMillis;
		System.out.println("EVALUATOR: Finished after " + time + "ms.");
	}

	private static void writeBestSolution() throws IOException {
		IWriterService writer = ServiceLocator.getInstance().getWriterService();
		writer.outputBestSolution();
	}

	private static void updateSolutionTable() {
		getSolutionTable().update();
	}

	private static void printBestSolution() {
		System.out.println("----------------------------");
		System.out.println("-- Best Penalty Solution (Penalty / Fairness): "
				+ getSolutionTable().getBestPenaltySolutionPenalty() + " / "
				+ getSolutionTable().getBestPenaltySolutionFairness());
	}

	private static void printFairestSolution() {
		System.out.print("-- Best Fairness Solution (Penalty / Fairness): "
				+ getSolutionTable().getBestFairnessSolutionPenalty() + " / "
				+ getSolutionTable().getBestFairnessSolutionFairness() + "\n");
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
				+ getSolutionTable().getBestPenaltySolutionPenalty() + " / "
				+ getSolutionTable().getBestPenaltySolutionFairness());
		System.out.println("-- Best Fairness Solution (Penalty / Fairness): "
				+ getSolutionTable().getBestFairnessSolutionPenalty() + " / "
				+ getSolutionTable().getBestFairnessSolutionFairness());

		System.out.println();

		int generatorSuccessRatio = (Main.generatorSuccess * 100)
				/ (Main.generatorSuccess + Main.generatorFailure);
		System.out.println("-- Generator (Success / Failure): "
				+ Main.generatorSuccess + " / " + Main.generatorFailure + " ("
				+ generatorSuccessRatio + " %)");

		int genetistSuccessRatio = (Main.mutateRecombineSuccess * 100)
				/ (Main.mutateRecombineSuccess + Main.mutateRecombineFailure);
		System.out.println("-- Genetist (Success / Failure): "
				+ Main.mutateRecombineSuccess + " / "
				+ Main.mutateRecombineFailure + " (" + genetistSuccessRatio
				+ " %)");

		int solutionTableSuccessRatio = (Main.solutionTableInsertionSuccess * 100)
				/ (Main.solutionTableInsertionSuccess + Main.solutionTableInsertionFailure);
		System.out.println("-- Solution Table Insertion (Success / Failure): "
				+ Main.solutionTableInsertionSuccess + " / "
				+ Main.solutionTableInsertionFailure + " ("
				+ solutionTableSuccessRatio + "%)");
		System.out.println("----------------------------");

		System.out.println();
	}

	private static void shortSleep(final long sleepMilliSeconds) {
		try {
			Thread.sleep(sleepMilliSeconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void runAllInstances(String initialSolutionsDirectory)
			throws IOException {

		String dateString = new Date().toString();
		dateString = dateString.replaceAll(" ", "-");
		dateString = dateString.replaceAll(":", "-");
		final String logFileName = "doc/logs/allinstances_" + dateString
				+ ".txt";

		final File logFile = new File(logFileName);
		if (logFile.exists()) {
			logFile.delete();
			logFile.createNewFile();
		}

		final File instancesDir = new File("instances");

		final File[] instanceFiles = instancesDir
				.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".ctt");
					}
				});

		final BufferedWriter writer = new BufferedWriter(
				new FileWriter(logFile));

		createLogFileHeader(writer);

		long totalDuration = 0;

		for (int i = 0; i < instanceFiles.length; i++) {
			long duration = run("instances/" + instanceFiles[i].getName(),
					initialSolutionsDirectory, 0);
			totalDuration += duration;
			writeResult(writer, instanceFiles[i], duration);
		}

		createLogFileFooter(writer, totalDuration);

		writer.close();
	}

	private static void createLogFileHeader(final BufferedWriter writer)
			throws IOException {

		ServiceLocator serviceLocator = ServiceLocator.getInstance();

		writer.write("Log file created on " + new Date());
		writer.newLine();
		writer.write("--------------------------------------------------");
		writer.newLine();
		writer.newLine();
		writer.write("Maximum Solution Table Size: "
				+ serviceLocator.getSolutionTableService().getMaximumSize());
		writer.newLine();
		writer.write("Iterations: " + iterations);
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

		final ISolutionTableService solutionTable = getSolutionTable();

		writer.write(instanceFile.getName());

		writer.write(" (Duration: " + toTimeString(duration) + "):");
		writer.newLine();
		writer.write("--------------");
		writer.newLine();
		writer.write("Best penalty/penalty: "
				+ solutionTable.getBestPenaltySolutionPenalty());
		writer.newLine();
		writer.write("Best penalty/fairness: "
				+ solutionTable.getBestPenaltySolutionFairness());
		writer.newLine();
		writer.write("Best fairness/penalty: "
				+ solutionTable.getBestFairnessSolutionPenalty());
		writer.newLine();
		writer.write("Best fairness/fairness: "
				+ solutionTable.getBestFairnessSolutionFairness());
		writer.newLine();
		writer.write("Worst penalty/penalty: "
				+ solutionTable.getWorstPenaltySolutionPenalty());
		writer.newLine();
		writer.write("Worst penalty/fairness: "
				+ solutionTable.getWorstPenaltySolutionFairness());
		writer.newLine();
		writer.write("Worst fairness/penalty: "
				+ solutionTable.getWorstFairnessSolutionPenalty());
		writer.newLine();
		writer.write("Worst fairness/fairness: "
				+ solutionTable.getWorstFairnessSolutionFairness());
		writer.newLine();
		writer.write("Generator success: " + Main.generatorSuccess);
		writer.newLine();
		writer.write("Generator failure: " + Main.generatorFailure);
		writer.newLine();
		writer.write("Mutation/recombination success: "
				+ Main.mutateRecombineSuccess);
		writer.newLine();
		writer.write("Mutation/recombination failure: "
				+ Main.mutateRecombineFailure);
		writer.newLine();
		writer.newLine();
		writer.newLine();

		writer.flush();
	}

	private static void createLogFileFooter(BufferedWriter writer,
			long totalDuration) throws IOException {
		writer.write("Total duration: " + toTimeString(totalDuration));
	}

	private static String toTimeString(long timeInMillis) {
		final long hours = TimeUnit.MILLISECONDS.toHours(timeInMillis);
		final long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis)
				- TimeUnit.HOURS.toMinutes(hours);
		final long seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis)
				- TimeUnit.MINUTES.toSeconds(minutes);

		return String.format("%d h, %d m, %d s", hours, minutes, seconds);
	}

}