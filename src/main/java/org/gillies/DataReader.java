package org.gillies;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class DataReader {
	 //https://www.michiganlottery.com/v1/milotto/players/winning_numbers/past/5/2000-01-01/2016-02-5.json?both=0
	
	 public List<JSONObject> readLotteryResults(File file) throws IOException {
		 String s = FileUtils.readFileToString(file);
		 Object obj = JSONValue.parse(s);
		 JSONObject head=(JSONObject)obj;
		 JSONObject result = (JSONObject) head.get("result");
		 JSONArray array = (JSONArray) result.get("winning_numbers");
	 	 List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
	 	 for(Object object : array) {
	 		 JSONObject jsonObj = (JSONObject) object;
	 		jsonObjects.add(jsonObj);
	 	 }
	 	 
	 	 return jsonObjects;
	 }
	 
	 public List<DrawingResult> getResults(File file) throws IOException {
		 
		 List<JSONObject> objects = readLotteryResults(file);
		 List<DrawingResult> results = new ArrayList<DrawingResult>(objects.size());
		 for(JSONObject object : objects) {
			 results.add(DrawingResult.getDrawingFromJson(object));
		 }
		 return results;
	 }
	 
	 
}
