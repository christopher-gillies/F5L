package org.gillies;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kidneyomics.hmm.HMM;
import org.kidneyomics.hmm.State;
import org.kidneyomics.hmm.Symbol;
import org.kidneyomics.hmm.HMM.LEARN_MODE;

public class GameObject {
	
	private final Map<String,List<Symbol>> map;
	private final Map<String,List<Symbol>> train;
	private final Map<String,List<Symbol>> test;
	
	private final Map<String,HMM> hmms;
	final Symbol played;
	final Symbol notPlayed;
	
	private GameObject() {
		train = new HashMap<String, List<Symbol>>();
		test = new HashMap<String, List<Symbol>>();
		map = new HashMap<String, List<Symbol>>();
		hmms = new HashMap<String, HMM>();
		played = Symbol.createSymbol("P");
		notPlayed = Symbol.createSymbol("N");
	}
	
	public static GameObject getGameObject(List<DrawingResult> in, int maxNumber) {
		
		GameObject go = new GameObject();
		
		
		
		for(DrawingResult result : in) {
			Map<String,Symbol> vector = result.toVector(maxNumber, go.played, go.notPlayed);
			
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
		
		
		//buildHMMS
		go.buildHMMs();
		return go;
	}
	
	public void train(int numTrain) {
		for(String num : map.keySet()) {
			System.err.println("Training: " + num);
			HMM hmm = hmms.get(num);
			List<Symbol> symbols = map.get(num);
			List<Symbol> trainSyms = new LinkedList<Symbol>();
			List<Symbol> testSyms = new LinkedList<Symbol>();
			
			train.put(num, trainSyms);
			test.put(num, testSyms);
			
			int count = 0;
			for(Symbol symbol : symbols) {
				if(count > numTrain) {
					testSyms.add(symbol);
				} else {
					trainSyms.add(symbol);
				}
			}
			
			hmm.learnEMSingle(trainSyms, LEARN_MODE.CUSTOM);
		}
	}
	
	private void buildHMMs() {
		for(String num : map.keySet()) {
			HMM hmm = buildHMM();
			this.hmms.put(num, hmm);
		}
	}
	
	private HMM buildHMM() {
		State start = State.createStartState();
		
		
		State fair = State.createState("F");
		//set emission probabilities for fair state
		fair.getEmissions().setProbability(played, 0.5);
		fair.getEmissions().setProbability(notPlayed, 0.5);
		
		
		
		State biased = State.createState("B");
		//set emission probabilities for biased state
		biased.getEmissions().setProbability(played, 0.5);
		biased.getEmissions().setProbability(notPlayed, 0.5);
		
		//equal chance to be in fair or biased state
		start.getTransitions().setProbability(fair, 0.5);
		start.getTransitions().setProbability(biased, 0.5);
		
		fair.getTransitions().setProbability(fair, 0.5);
		fair.getTransitions().setProbability(biased, 0.1);
		
		biased.getTransitions().setProbability(fair, 0.5);
		biased.getTransitions().setProbability(biased, 0.5);
		
		HMM hmm = HMM.createHMMFromStartState(start);
		
		hmm.initializeStateCounts(LEARN_MODE.PSEUDO_COUNT);
		fair.getTransitions().addToCount(fair, 1);
		biased.getTransitions().addToCount(biased, 1);
		
		biased.getEmissions().addToCount(notPlayed, 1);
		return hmm;
	}
	
	public Map<String, List<Symbol>> getMap() {
		return this.map;
	}
	
	public Map<String,HMM> getHMMs() {
		return this.hmms;
	}
}
