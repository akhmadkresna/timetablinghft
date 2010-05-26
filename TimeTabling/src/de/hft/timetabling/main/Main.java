package de.hft.timetabling.main;

import java.io.IOException;

import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.reader.Reader;
import de.hft.timetabling.services.IReaderService;
import de.hft.timetabling.services.ServiceLocator;

public class Main {

	public static void main(String[] args) {
		ServiceLocator.getInstance().setReaderService(new Reader());

		IReaderService readerService = ServiceLocator.getInstance()
				.getReaderService();
		try {
			IProblemInstance problemInstance = readerService
					.readInstance("example.txt");
			System.out.println("Name: " + problemInstance.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
