package de.hft.timetabling.services;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;

public interface IValidatorService {

	boolean solutionValid(ISolution sol);

	boolean solutionValid(IProblemInstance instance, ICourse[][] coding);
}
