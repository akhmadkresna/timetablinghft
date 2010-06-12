package de.hft.timetabling.services;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;

public interface IValidatorService {

	boolean isValidSolution(ISolution sol);

	boolean isValidSolution(IProblemInstance instance, ICourse[][] coding);

}
