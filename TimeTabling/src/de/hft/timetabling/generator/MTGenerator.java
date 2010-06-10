package de.hft.timetabling.generator;

import java.util.ArrayList;
import java.util.List;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.IGeneratorService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;

public class MTGenerator implements IGeneratorService {

	private static final Object CREATE_LOCK = new Object();

	private static final Object ADD_LOCK = new Object();

	private static final int NUM_THREADS = 2;

	private int threadCount = 0;

	private final Generator generator = new Generator();

	private final ISolutionTableService solutionTable = ServiceLocator
			.getInstance().getSolutionTableService();

	@Override
	public void fillSolutionTable(final IProblemInstance problemInstance) {

		// TODO thread pool

		if (solutionTable.getNumberOfEmptySlots() == 1) {
			while (solutionTable.getNumberOfEmptySlots() > 0) {
				try {
					ICourse[][] coding = generator
							.generateFeasibleSolution(problemInstance);
					ISolution sol = solutionTable.createNewSolution(coding,
							problemInstance);
					solutionTable.addSolution(sol);
				} catch (NoFeasibleSolutionFoundException e) {
					e.printStackTrace();
				}
			}
			return;
		}

		final List<Thread> threads = new ArrayList<Thread>();

		for (int i = 0; i < NUM_THREADS; i++) {
			Thread thread = new Thread(new Runnable() {
				private final Generator generator = new Generator();
				private int threadId = threadCount++;

				@Override
				public void run() {
					ISolutionTableService table = ServiceLocator.getInstance()
							.getSolutionTableService();

					int i = 0;

					while (table.getNumberOfEmptySlots() > 0) {
						System.out.println("Thread " + threadId
								+ ", iteration " + i++);
						ICourse[][] coding;
						try {
							coding = generator
									.generateFeasibleSolution(problemInstance);
							ISolution sol;

							synchronized (CREATE_LOCK) {
								sol = table.createNewSolution(coding,
										problemInstance);
							}
							synchronized (ADD_LOCK) {
								if (table.getNumberOfEmptySlots() > 0) {
									System.out.println("Empty slots: "
											+ table.getNumberOfEmptySlots());
									table.addSolution(sol);
								}
							}
						} catch (NoFeasibleSolutionFoundException e) {
							e.printStackTrace();
						}
					}
				}
			});
			thread.start();
			threads.add(thread);
		}

		for (int i = 0; i < threads.size(); i++) {
			try {
				threads.get(i).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		threadCount = 0;
	}
}
