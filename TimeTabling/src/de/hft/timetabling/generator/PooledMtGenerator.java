package de.hft.timetabling.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.IGeneratorService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;

public class PooledMtGenerator implements IGeneratorService {

	private IProblemInstance problemInstance;

	private final List<Callable<ISolution>> taskGroup = new ArrayList<Callable<ISolution>>();

	private final ISolutionTableService solutionTable = ServiceLocator
			.getInstance().getSolutionTableService();

	private final ExecutorService exec = java.util.concurrent.Executors
			.newFixedThreadPool(2);

	@Override
	public void fillSolutionTable(IProblemInstance problemInstance) {
		if (this.problemInstance != problemInstance) {
			this.problemInstance = problemInstance;
			taskGroup.clear();
			int taskCount = 0;

			for (int i = 0; i < ISolutionTableService.TABLE_SIZE; i++) {
				taskGroup.add(new SolutionTask(problemInstance, taskCount++));
			}
		}

		try {
			List<Future<ISolution>> futureList = new ArrayList<Future<ISolution>>();

			futureList = exec.invokeAll(taskGroup.subList(0, solutionTable
					.getNumberOfEmptySlots()));

			for (Future<ISolution> future : futureList) {
				ISolution sol = future.get();
				solutionTable.addSolution(sol);
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}

class SolutionTask implements Callable<ISolution> {

	private static final Object CREATE_LOCK = new Object();

	private final Generator gen = new Generator();

	private final IProblemInstance instance;

	private final ISolutionTableService solutionTable = ServiceLocator
			.getInstance().getSolutionTableService();

	private final int id;

	public SolutionTask(IProblemInstance instance, int id) {
		this.instance = instance;
		this.id = id;
	}

	@Override
	public ISolution call() {
		ISolution sol = null;

		int iteration = 0;

		while (sol == null) {
			System.out.println("Task " + id + ", iteration " + iteration++);

			try {
				ICourse[][] coding = gen.generateFeasibleSolution(instance);

				synchronized (CREATE_LOCK) {
					sol = solutionTable.createNewSolution(coding, instance);
				}
			} catch (NoFeasibleSolutionFoundException e) {
				e.printStackTrace();
			}
		}
		return sol;
	}
}