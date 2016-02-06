package org.gillies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.kidneyomics.hmm.Symbol;

public class DrawingResult {
	
	private final String[] nums = new String[5];
	private final String date;
	
	private DrawingResult(JSONObject obj) {
		//System.err.println(obj);
		
		int i = 0;
		Long num1 = (Long) obj.get("draw_num_1");
		nums[i++] = num1.toString();
		
		Long num2 = (Long) obj.get("draw_num_2");
		nums[i++] = num2.toString();
		
		Long num3 = (Long) obj.get("draw_num_3");
		nums[i++] = num3.toString();
		
		Long num4 = (Long) obj.get("draw_num_4");
		nums[i++] = num4.toString();
		
		Long num5 = (Long) obj.get("draw_num_5");
		nums[i++] = num5.toString();
		
		
		date = (String) obj.get("draw_date");
	}
	
	
	public static DrawingResult getDrawingFromJson(JSONObject obj) {
		return new DrawingResult(obj);
	}
	
	
	public String[] getNums() {
		return this.nums;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(date);
		sb.append("\t");
		for(int i = 0; i < nums.length; i++) {
			
			sb.append(nums[i]);
			if(i != nums.length - 1) {
				sb.append("\t");
			}
		}
		
		return sb.toString();
	}
	
	public Map<String,Symbol> toVector(int maxNumber, Symbol played, Symbol notPlayed) {
		Map<String,Symbol> result = new HashMap<String, Symbol>();
		
		//played numbers
		for(String num : nums) {
			result.put(num, played);
		}
		
		//not played numbers
		for(Integer i = 1; i <= maxNumber; i++) {
			String num = i.toString();
			if(!result.containsKey(num)) {
				result.put(num, notPlayed);
			}
			
		}
		
		
		return result;
	}
	
}
