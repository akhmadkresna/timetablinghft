package de.hft.timetabling.services;

import java.io.IOException;

import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;

/**
 * The writer service cares for the creation of the necessary output files.
 * 
 * @author Alexander Weickmann
 */
public interface IWriterService {

	/**
	 * Takes the best solution from the solution table and writes the
	 * corresponding output file. The output file's name will consist of the
	 * file name of the input file and a time stamp.
	 * 
	 * @throws IOException
	 *             If any I/O error occurs while trying to write the output
	 *             file.
	 */
	void outputBestSolution() throws IOException;

	/**
	 * Writes the given solution to the output file specified by the given file
	 * name.
	 * 
	 * @param fileName
	 *            The file name of the output file.
	 * @param solution
	 *            The solution that has to be written to the output file.
	 * @param problemInstance
	 *            The problem instance that the solution is solving.
	 * 
	 * @throws IOException
	 *             If any I/O error occurs while trying to write the output
	 *             file.
	 */
	void outputSolution(String fileName, ISolution solution,
			IProblemInstance problemInstance) throws IOException;

}
