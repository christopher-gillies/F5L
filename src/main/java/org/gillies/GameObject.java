package org.gillies;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kidneyomics.hmm.Symbol;

public class GameObject {
	
	private final Map<String,List<Symbol>> map;
	
	private GameObject() {
		map = new HashMap<String, List<Symbol>>();
	}
	
	public static GameObject getGameObject(List<DrawingResult> in, int maxNumber) {
		
		Symbol played = Symbol.createSymbol("P");
		Symbol notPlayed = Symbol.createSymbol("N");
		GameObject go = new GameObject();
		
		
		
		for(DrawingResult result : in) {
			Map<String,Symbol> vector = result.toVector(maxNumber, played, notPlayed);
			
			for(Map.Entry<String, Symbol> entry : vector.entrySet()) {
				if(go.map.containsKey(entry.getKey())) {
					List<Symbol> symbols = go.map.get(entry.getKey());
					symbols.add(entry.getValue());
				} else {
					List<Symbol> symbols = new LinkedList<Symbol>();
					symbols.add(entry.getValue());
					go.map.put(entry.getKey(), symbols);
				}
			}
		}
		
		
		
		return go;
	}
	
	public Map<String, List<Symbol>> getMap() {
		return this.map;
	}
}
