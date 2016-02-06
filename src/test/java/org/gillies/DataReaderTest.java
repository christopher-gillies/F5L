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
		
		List<JSONObject> objs = reader.read(file);
		
		int count = 0;
		for(JSONObject obj : objs) {
			count++;
			//System.err.println(obj);
			Long num1 = (Long) obj.get("draw_num_1");
			Long num2 = (Long) obj.get("draw_num_2");
			Long num3 = (Long) obj.get("draw_num_3");
			Long num4 = (Long) obj.get("draw_num_4");
			Long num5 = (Long) obj.get("draw_num_5");
			String date = (String) obj.get("draw_date");
			System.err.println(date + ": " + num1 + "," + num2 + "," + num3 + "," + num4 + "," + num5);
		}
		System.err.println(count);
	}

}
