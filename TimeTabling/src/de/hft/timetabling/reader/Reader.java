package de.hft.timetabling.reader;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.services.IReaderService;

public final class Reader implements IReaderService {

	@Override
	public IProblemInstance readInstance(String fileName) throws IOException {
		ProblemInstance instance = new ProblemInstance();
		readFile(fileName, instance);
		return instance;
	}

	private BufferedReader getBufferedReader(String fileName)
			throws FileNotFoundException {

		FileInputStream fileStream = new FileInputStream("instances/"
				+ fileName);
		DataInputStream dataStream = new DataInputStream(fileStream);
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(dataStream));
		return bufferedReader;
	}

	private void readFile(String fileName, ProblemInstance instance)
			throws IOException {

		BufferedReader bufferedReader = getBufferedReader(fileName);
		String line = bufferedReader.readLine();
		for (int i = 0; line != null; i++) {
			if (i <= 6) {
				readGeneralInformation(line, i, instance);
			}
			line = bufferedReader.readLine();
		}
		bufferedReader.close();
	}

	private void readGeneralInformation(String line, int lineNumber,
			ProblemInstance instance) {

		String value = line.substring(line.lastIndexOf(":") + 2);
		switch (lineNumber) {
		case 0:
			instance.setName(value);
			break;
		case 1:
			instance.setNumberOfCourses(Integer.valueOf(value));
			break;
		case 2:
			instance.setNumberOfRooms(Integer.valueOf(value));
			break;
		case 3:
			instance.setNumberOfDays(Integer.valueOf(value));
			break;
		case 4:
			instance.setPeriodsPerDay(Integer.valueOf(value));
			break;
		case 5:
			instance.setNumberOfCurricula(Integer.valueOf(value));
			break;
		case 6:
			instance.setNumberOfConstraints(Integer.valueOf(value));
			break;
		}
	}

}
