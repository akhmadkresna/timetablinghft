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

	private final ExecutorService exec = Executors.newFixedThreadPool(2);

	@Override
	public void fillSolutionTable(final IProblemInstance problemInstance) {
		if (this.problemInstance != problemInstance) {
			this.problemInstance = problemInstance;
			taskGroup.clear();
			int taskCount = 0;

			for (int i = 0; i < ISolutionTableService.TABLE_SIZE; i++) {
				taskGroup.add(new SolutionTask(problemInstance, taskCount++));
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
		if (solutionTable.getNumberOfEmptySlots() > 0) {
			System.err.println("NOT FULL!!!!!");
			System.exit(1);
		}
	}
}

final class SolutionTask implements Callable<ISolution> {

	public static final Object CREATE_LOCK = new Object();

	private final Generator gen = new Generator();

	private final IProblemInstance instance;

	private final ISolutionTableService solutionTable = ServiceLocator
			.getInstance().getSolutionTableService();

	private final int id;

	public SolutionTask(final IProblemInstance instance, final int id) {
		this.instance = instance;
		this.id = id;
	}

	@Override
	public ISolution call() {
		ISolution sol = null;

		while (sol == null) {
			System.out.println("Task " + id);

			try {
				ICourse[][] coding = gen.generateFeasibleSolution(instance);

				synchronized (CREATE_LOCK) {
					sol = solutionTable.createNewSolution(coding, instance);
				}
			} catch (NoFeasibleSolutionFoundException e) {
				// nothing to do
			}
		}
		return sol;
	}
}