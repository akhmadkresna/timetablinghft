package de.hft.timetabling.services;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.hft.timetabling.common.IProblemInstance;

/**
 * The reader service is capable of reading the competition's problem instance
 * input files. It saves the data into an {@link IProblemInstance} data
 * structure which acts as the basis information source for all other sub
 * systems.
 * 
 * @author Alexander Weickmann
 * 
 * @see IProblemInstance
 */
public interface IReaderService {

	/**
	 * Reads the problem instance input file specified by the file name into an
	 * {@link IProblemInstance} and returns it.
	 * 
	 * @param fileName
	 *            The file name of the problem instance input file.
	 * 
	 * @throws IOException
	 *             If no file can be found using the given file name or any
	 *             other IO error occurs.
	 */
	IProblemInstance readInstance(String fileName) throws IOException;

	/**
	 * Reads the input file specified by the instance file name into an
	 * {@link IProblemInstance} and returns it. In addition, the solution table
	 * is initialized with the solutions stored in the specified solution
	 * directory.
	 * 
	 * @param instanceFileName
	 *            The file name of the problem instance input file.
	 * @param solutionDirectoryName
	 *            The name of the directory where to find initial solutions.
	 * 
	 * @throws FileNotFoundException
	 *             If the specified directory does not exist.
	 * @throws IOException
	 *             If the name provided for the solution directory does not
	 *             actually specify a directory or any other IO error occurs.
	 */
	IProblemInstance readInstanceUsingInitialSolutionDirectory(
			String instanceFileName, String solutionDirectoryName)
			throws IOException;

}
