package de.hft.timetabling.services;

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
	 */
	void outputBestSolution();

}
