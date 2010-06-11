package de.hft.timetabling.services;

/**
 * Provides eliminiation of solutions.
 * 
 * @author Alexander Weickmann
 */
public interface IEliminatorService {

	/** The percentage of the population to eliminate. */
	int PERCENTAGE = 5;

	void eliminateSolutions();

}
