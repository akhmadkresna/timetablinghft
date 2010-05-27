package de.hft.timetabling.services;

public final class ServiceLocator {

	private static final ServiceLocator instance = new ServiceLocator();

	public static ServiceLocator getInstance() {
		return instance;
	}

	private ISolutionTableService solutionTableService;

	private IReaderService readerService;

	private IWriterService writerService;

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

	public void setSolutionTableService(
			ISolutionTableService solutionTableService) {

		this.solutionTableService = solutionTableService;
	}

	public void setOutputService(IWriterService outputService) {
		writerService = outputService;
	}

	public void setReaderService(IReaderService readerService) {
		this.readerService = readerService;
	}

	@Override
	public String toString() {
		return "Service Locator";
	}

}
