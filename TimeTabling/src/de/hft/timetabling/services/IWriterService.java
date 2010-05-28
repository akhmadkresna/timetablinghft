package de.hft.timetabling.services;

import java.io.IOException;

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

}
