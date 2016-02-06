package org.gillies;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONObject;
import org.junit.Test;

public class DataReaderTest {

	@Test
	public void test() throws URISyntaxException, IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("f5.2000.01.02.20016.2.5.json").getFile());
		
		DataReader reader = new DataReader();
		
		List<DrawingResult> results = reader.getResults(file);
		for(DrawingResult result : results) {
			//System.err.println(result);
		}
		
		//System.err.println("Count: " + results.size());
		
		assertEquals(4154,results.size());
	}

}
