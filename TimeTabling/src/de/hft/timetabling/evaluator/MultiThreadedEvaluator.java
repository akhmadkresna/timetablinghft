package de.hft.timetabling.evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;

public class MultiThreadedEvaluator {

	private final List<EvaluatorTask> taskGroup = new ArrayList<EvaluatorTask>();

	private final ISolutionTableService solutionTable = ServiceLocator
			.getInstance().getSolutionTableService();

	private final ExecutorService exec = Executors.newFixedThreadPool(Runtime
			.getRuntime().availableProcessors());

	public MultiThreadedEvaluator() {
		for (int i = 0; i < solutionTable.getMaximumSize(); i++) {
			taskGroup.add(new EvaluatorTask(new NewEvaluator()));
		}
	}

	public SolutionEvaluation evaluateSolution(final ISolution newSolution) {
		// TODO Auto-generated method stub
		return null;
	}

	public void evaluateSolutions() {
		final List<ISolution> unvotedSolutions = solutionTable
				.getNotVotedSolutions();

		for (int i = 0; i < unvotedSolutions.size(); i++) {
			taskGroup.get(i).setSolution(unvotedSolutions.get(i));
		}

		try {
			final List<Future<SolutionEvaluation>> futureList = exec
					.invokeAll(taskGroup.subList(0, unvotedSolutions.size()));

			int i = 0;
			for (final Future<SolutionEvaluation> future : futureList) {
				final SolutionEvaluation result = future.get();
				solutionTable.voteForSolution(i++, result.getTotalPenalty(),
						result.getTotalFairness());
			}
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

final class EvaluatorTask implements Callable<SolutionEvaluation> {

	private ISolution solution;

	private final NewEvaluator evaluator;

	public EvaluatorTask(final NewEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public void setSolution(final ISolution solution) {
		this.solution = solution;
	}

	@Override
	public SolutionEvaluation call() {
		return evaluator.evaluateSolution(solution);
	}
}