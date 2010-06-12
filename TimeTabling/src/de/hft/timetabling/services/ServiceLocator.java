package de.hft.timetabling.services;

/**
 * The service locator is a singleton granting access to the various sub systems
 * (called services) of the overall system. By doing so, it reduces the coupling
 * between the individual sub systems, because the sub system don't need to know
 * the details of the other sub systems that way. Each sub system only
 * communicates with other sub systems trough their service interfaces.
 * 
 * @author Alexander Weickmann
 */
public final class ServiceLocator {

	private static final ServiceLocator instance = new ServiceLocator();

	public static ServiceLocator getInstance() {
		return instance;
	}

	private ISolutionTableService solutionTableService;

	private IReaderService readerService;

	private IWriterService writerService;

	private IGeneratorService generatorService;

	private IValidatorService validatorService;

	private ICrazyGenetistService crazyGenetistService;

	private IEvaluatorService evaluatorService;

	private ServiceLocator() {
		// Singleton constructor.
	}

	public IWriterService getWriterService() {
		if (writerService == null) {
			throw new RuntimeException("Writer service not available.");
		}
		return writerService;
	}

	public IReaderService getReaderService() {
		if (readerService == null) {
			throw new RuntimeException("Reader service not available.");
		}
		return readerService;
	}

	public ISolutionTableService getSolutionTableService() {
		if (solutionTableService == null) {
			throw new RuntimeException("Solution table service not available.");
		}
		return solutionTableService;
	}

	public IGeneratorService getGeneratorService() {
		if (generatorService == null) {
			throw new RuntimeException("Generator service not available.");
		}
		return generatorService;
	}

	public IValidatorService getValidatorService() {
		if (validatorService == null) {
			throw new RuntimeException("Validator service not available.");
		}
		return validatorService;
	}

	public ICrazyGenetistService getCrazyGenetistService() {
		if (crazyGenetistService == null) {
			throw new RuntimeException("Crazy genetist service not available.");
		}
		return crazyGenetistService;
	}

	public IEvaluatorService getEvaluatorService() {
		if (evaluatorService == null) {
			throw new RuntimeException("Evaluator service not available.");
		}
		return evaluatorService;
	}

	public void setSolutionTableService(
			ISolutionTableService solutionTableService) {
		this.solutionTableService = solutionTableService;
	}

	public void setWriterService(IWriterService writerService) {
		this.writerService = writerService;
	}

	public void setReaderService(IReaderService readerService) {
		this.readerService = readerService;
	}

	public void setGeneratorService(IGeneratorService generatorService) {
		this.generatorService = generatorService;
	}

	public void setValidatorService(IValidatorService validatorService) {
		this.validatorService = validatorService;
	}

	public void setCrazyGenetistService(
			ICrazyGenetistService crazyGenetistService) {
		this.crazyGenetistService = crazyGenetistService;
	}

	public void setEvaluatorService(IEvaluatorService evaluatorService) {
		this.evaluatorService = evaluatorService;
	}

	@Override
	public String toString() {
		return "Service Locator";
	}

}
