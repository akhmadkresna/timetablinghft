package de.hft.timetabling.services;

public final class ServiceLocator {

	private static final ServiceLocator instance = new ServiceLocator();

	public static ServiceLocator getInstance() {
		return instance;
	}

	private IReaderService readerService;

	private IOutputService outputService;

	private ServiceLocator() {
		// Singleton constructor.
	}

	public IOutputService getOutputService() {
		if (outputService == null) {
			throw new RuntimeException("Output service not available.");
		}
		return outputService;
	}

	public IReaderService getReaderService() {
		if (readerService == null) {
			throw new RuntimeException("Reader service not available.");
		}
		return readerService;
	}

	public void setOutputService(IOutputService outputService) {
		this.outputService = outputService;
	}

	public void setReaderService(IReaderService readerService) {
		this.readerService = readerService;
	}

}
