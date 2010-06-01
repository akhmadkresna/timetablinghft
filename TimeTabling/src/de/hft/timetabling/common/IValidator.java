package de.hft.timetabling.common;

public interface IValidator {

	boolean solutionValid(ISolution sol);

	boolean solutionValid(IProblemInstance instance, ICourse[][] coding);
}
