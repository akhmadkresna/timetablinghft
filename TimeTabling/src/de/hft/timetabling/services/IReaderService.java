package de.hft.timetabling.services;

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
	 * Reads the input file specified by the file name into an
	 * {@link IProblemInstance} and returns it.
	 * 
	 * @param fileName
	 *            The file name of the input file.
	 * 
	 * @throws IOException
	 *             If no file can be found using the given file name or any
	 *             other IO error occurs.
	 */
	IProblemInstance readInstance(String fileName) throws IOException;

}
