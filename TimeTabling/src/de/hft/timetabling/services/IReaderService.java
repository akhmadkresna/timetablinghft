package de.hft.timetabling.services;

import java.io.IOException;

import de.hft.timetabling.common.IProblemInstance;

public interface IReaderService {

	IProblemInstance readInstance(String fileName) throws IOException;

}
