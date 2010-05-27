package de.hft.timetabling.services;

/**
 * The service locator is a singleton granting access to the various sub systems
 * (called services) of the overall system. By doing so, it reduces the coupling
 * between the individual sub systems, because the sub system don't need to know
 * the details of the other sub systems that way. Each sub system only
 * communicates with other sub systems trough it's service interface.
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
