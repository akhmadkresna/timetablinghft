package de.hft.timetabling.services;

import junit.framework.TestCase;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.reader.Reader;

/**
 * @author Alexander Weickmann
 */
public abstract class AbstractServicesTest extends TestCase {

	protected IProblemInstance instance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		instance = new Reader().readInstance("test.ctt");
	}

}
