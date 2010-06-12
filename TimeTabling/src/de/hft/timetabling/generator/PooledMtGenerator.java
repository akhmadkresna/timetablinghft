package de.hft.timetabling.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.IGeneratorService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;

public final class PooledMtGenerator implements IGeneratorService {

	private IProblemInstance problemInstance;

	private final List<Callable<ISolution>> taskGroup = new ArrayList<Callable<ISolution>>();

	private final ISolutionTableService solutionTable = ServiceLocator
			.getInstance().getSolutionTableService();

	private final ExecutorService exec = Executors.newFixedThreadPool(Runtime
			.getRuntime().availableProcessors());

	private final IGeneratorService generator;

	public PooledMtGenerator(IGeneratorService generator) {
		this.generator = generator;
	}

	@Override
	public void fillSolutionTable(final IProblemInstance problemInstance) {
		if (this.problemInstance != problemInstance) {
			this.problemInstance = problemInstance;
			taskGroup.clear();
			final ISolutionTableService solutionTable = ServiceLocator
					.getInstance().getSolutionTableService();

			for (int i = 0; i < solutionTable.getMaximumSize(); i++) {
				taskGroup.add(new SolutionTask(problemInstance,
						new YetAnotherGenerator()));
			}
		}

		try {
			final List<Future<ISolution>> futureList = exec.invokeAll(taskGroup
					.subList(0, solutionTable.getNumberOfEmptySlots()));

			for (Future<ISolution> future : futureList) {
				final ISolution sol = future.get();
				solutionTable.addSolution(sol);
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ICourse[][] generateFeasibleSolution(IProblemInstance problemInstance)
			throws NoFeasibleSolutionFoundException {
		return generator.generateFeasibleSolution(problemInstance);
	}
}

final class SolutionTask implements Callable<ISolution> {

	public static final Object CREATE_LOCK = new Object();

	private IProblemInstance problemInstance;

	private final ISolutionTableService solutionTable = ServiceLocator
			.getInstance().getSolutionTableService();

	private final IGeneratorService generator;

	public SolutionTask(final IProblemInstance problemInstance,
			IGeneratorService generator) {
		this.problemInstance = problemInstance;
		this.generator = generator;
	}

	@Override
	public ISolution call() {
		ISolution newSolution = null;

		while (newSolution == null) {
			try {
				ICourse[][] coding = generator
						.generateFeasibleSolution(problemInstance);

				synchronized (CREATE_LOCK) {
					newSolution = solutionTable.createNewSolution(coding,
							problemInstance);
				}
			} catch (NoFeasibleSolutionFoundException e) {
				e.printStackTrace();
			}
		}
		return newSolution;
	}
}