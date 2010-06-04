package de.hft.timetabling.generator;

import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.genetist.ValidatorImpl;
import de.hft.timetabling.reader.Reader;
import de.hft.timetabling.services.ServiceLocator;
import de.hft.timetabling.services.SolutionTable;
import de.hft.timetabling.writer.Writer;

/**
 * The main class contains the main function that's needed to start the program.
 * 
 * @author Alexander Weickmann
 */
public final class MyPrivateMain {

	public static void main(String[] args) {
		setUpServices();

		exampleUsage();
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
	private static void exampleUsage() {
		// TODO Doesn't work any longer like this (interface changed).
		// IReaderService readerService = ServiceLocator.getInstance()
		// .getReaderService();
		// IProblemInstance problemInstance = readerService
		// .readInstance("comp05.ctt");
		//
		// IGeneratorService gen = ServiceLocator.getInstance()
		// .getGeneratorService();
		// IValidatorService val = ServiceLocator.getInstance()
		// .getValidatorService();
		//
		// try {
		// ICourse[][] coding = gen.generateFeasibleSolution(problemInstance);
		// System.out.println(val.isValidSolution(problemInstance, coding));
		// } catch (NoFeasibleSolutionFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
}
