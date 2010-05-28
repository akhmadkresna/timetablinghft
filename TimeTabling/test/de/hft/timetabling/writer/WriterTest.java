package de.hft.timetabling.writer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.reader.Reader;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;
import de.hft.timetabling.services.SolutionTable;

public class WriterTest extends TestCase {

	private Writer writer;

	private IProblemInstance instance;

	private ISolutionTableService solutionTable;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		writer = new Writer();
		instance = new Reader().readInstance("test.txt");
		ICourse[] courses = instance.getCourses().toArray(
				new ICourse[instance.getNumberOfCourses()]);

		// Course 0: SceCosC
		// Course 1: ArcTec
		// Course 2: TecCos
		// Course 3: Geotec
		ICourse[][] coding = { //
		{ null, courses[2] }, // Period 0
				{ courses[2], courses[1] }, // Period 1
				{ null, null }, // Period 2
				{ null, null }, // Period 3
				{ null, null }, // Period 4
				{ null, courses[1] }, // Period 5
				{ null, courses[1] }, // Period 6
				{ null, null }, // Period 7
				{ null, null }, // Period 8
				{ null, null }, // Period 9
				{ courses[3], courses[2] }, // Period 10
				{ courses[3], null }, // Period 11
				{ courses[3], courses[3] }, // Period 12
				{ courses[3], courses[3] }, // Period 13
				{ null, null }, // Period 14
				{ null, null }, // Period 15
				{ courses[0], null }, // Period 16
				{ null, null }, // Period 17
				{ courses[3], courses[2] }, // Period 18
				{ null, courses[2] }, // Period 19
		};

		solutionTable = new SolutionTable();
		ServiceLocator.getInstance().setSolutionTableService(solutionTable);
		ISolution solution = solutionTable.createNewSolution(coding, instance);
		solutionTable.putSolution(0, solution);
		solutionTable.voteForSolution(0, 1000);
	}

	public void testOutputBestSolution() throws IOException {
		String fileName = "test/temp.ctt";
		writer.outputSolution(fileName, solutionTable.getBestSolution(),
				instance);

		FileInputStream fileStream = new FileInputStream(fileName);
		DataInputStream dataStream = new DataInputStream(fileStream);
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(dataStream));
		List<String> lines = new ArrayList<String>();
		String line = bufferedReader.readLine();
		while (line != null) {
			lines.add(line);
			line = bufferedReader.readLine();
		}
		bufferedReader.close();

		assertEquals(16, lines.size());
		assertEquals("TecCos B 0 0", lines.get(0));
		assertEquals("TecCos A 0 1", lines.get(1));
		assertEquals("ArcTec B 0 1", lines.get(2));
		assertEquals("ArcTec B 1 1", lines.get(3));
		assertEquals("ArcTec B 1 2", lines.get(4));
		assertEquals("Geotec A 2 2", lines.get(5));
		assertEquals("TecCos B 2 2", lines.get(6));
		assertEquals("Geotec A 2 3", lines.get(7));
		assertEquals("Geotec A 3 0", lines.get(8));
		assertEquals("Geotec B 3 0", lines.get(9));
		assertEquals("Geotec A 3 1", lines.get(10));
		assertEquals("Geotec B 3 1", lines.get(11));
		assertEquals("SceCosC A 4 0", lines.get(12));
		assertEquals("Geotec A 4 2", lines.get(13));
		assertEquals("TecCos B 4 2", lines.get(14));
		assertEquals("TecCos B 4 3", lines.get(15));

		/*
		 * Delete the temporary output file again as it was only created for
		 * testing purposes.
		 */
		File file = new File(fileName);
		file.delete();
	}
}
