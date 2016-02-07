package org.gillies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kidneyomics.hmm.HMM;
import org.kidneyomics.hmm.State;
import org.kidneyomics.hmm.Symbol;
import org.kidneyomics.hmm.HMM.LEARN_MODE;
import org.kidneyomics.hmm.ViterbiGraph;

public class GameObject {
	
	private final Map<String,List<Symbol>> map;
	private final Map<String,List<Symbol>> train;
	private final Map<String,List<Symbol>> test;
	private final List<DrawingResult> drawingResults;
	private final List<DrawingResult> drawingResultsTrain;
	private final List<DrawingResult> drawingResultsTest;
	private final Map<String,HMM> hmms;
	final Symbol played;
	final Symbol notPlayed;
	
	private GameObject() {
		drawingResults = new ArrayList<DrawingResult>();
		drawingResultsTrain = new ArrayList<DrawingResult>();
		drawingResultsTest = new ArrayList<DrawingResult>();
		train = new HashMap<String, List<Symbol>>();
		test = new HashMap<String, List<Symbol>>();
		map = new HashMap<String, List<Symbol>>();
		hmms = new HashMap<String, HMM>();
		played = Symbol.createSymbol("P");
		notPlayed = Symbol.createSymbol("N");
	}
	
	public static GameObject getGameObject(List<DrawingResult> in, int maxNumber) {
		
		GameObject go = new GameObject();
		go.drawingResults.addAll(in);
		
		
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
	
	public void test(int pastWindowSize, int numberToPlay) {
		
		//39 nums for each drawingDate;
		
		List<ArrayList<PlayProbability>> numsPerDate = new ArrayList<ArrayList<PlayProbability>>();
		
		for(int i = 0; i < drawingResultsTest.size() - pastWindowSize - 1; i++) {
			numsPerDate.add(new ArrayList<GameObject.PlayProbability>());
		}
		
		for(String num : map.keySet()) {
			List<Symbol> testSyms = test.get(num);
			HMM hmm = hmms.get(num);
			for(int i = 0; i < testSyms.size(); i++) {
				List<Symbol> window = new LinkedList<Symbol>();
				for(int j = 0; j < pastWindowSize; j++) {
					//if size = 10
					//9 > 10 - 1 false
					if(i + j >= testSyms.size()) {
						break;
					}
					window.add( testSyms.get(i + j));
				}
				
				//do not use this window if it is less than windowSize
				if(window.size() != pastWindowSize) {
					break;
				}
				
				
				//skip if there are no more next days
				if(i + pastWindowSize >= drawingResultsTest.size()) {
					break;
				}
				
				//drawing result for the next day
				DrawingResult drawingResult = drawingResultsTest.get(i + pastWindowSize);
				
				State biased = hmm.getStateByName("B");
				State fair = hmm.getStateByName("F");
				
				ViterbiGraph graph = hmm.getViterbiGraph(window);
				
				double biasedProb = hmm.probInStateAtPositionGivenSequence(graph, biased, window.size() - 1, false);
				double fairProb = hmm.probInStateAtPositionGivenSequence(graph, fair, window.size() - 1, false);
				HashMap<State,Double> probsOfEachState = new HashMap<State, Double>();
				probsOfEachState.put(biased, biasedProb);
				probsOfEachState.put(fair, fairProb);
				
				double probOfPlay = hmm.calcProbOfSymbolGivenStateProbs(played,probsOfEachState);
				
				PlayProbability pp = new PlayProbability(num, probOfPlay, drawingResult);
				
				ArrayList<PlayProbability> listForDate = numsPerDate.get(i);
				listForDate.add(pp);
				
			}
		}
		
		//print results
		int totalCorrect = 0;
		int numPlayed = 0;
		for(ArrayList<PlayProbability> listForDate : numsPerDate) {
			if(listForDate.size() > 0) {
				numPlayed++;
				Collections.sort(listForDate);
				PlayProbability pp1 = listForDate.get(0);
				DrawingResult result = pp1.getDrawingResult();
				
				List<String> topNums = new LinkedList<String>();
				for(int i = 0; i < numberToPlay; i++) {
					topNums.add(listForDate.get(i).getNum());
				}
				int numCorrect = result.numContained(topNums);
				totalCorrect += numCorrect;
				System.err.println(result.toString() + " Number correctly guessed in top " + numberToPlay + " is: " + numCorrect);
			}
		}
		System.err.println("Avg correct: " + (totalCorrect / (double) numPlayed));
	}
	
	class PlayProbability implements Comparable<PlayProbability>{
		private final String num;
		private final double probOfPlay;
		private final DrawingResult drawingResult;
		
		public PlayProbability(String num, double probOfPlay, DrawingResult drawingResult) {
			this.num = num;
			this.probOfPlay = probOfPlay;
			this.drawingResult = drawingResult;
		}
		
		public String getNum() {
			return num;
		}

		public double getProbOfPlay() {
			return probOfPlay;
		}
		
		public DrawingResult getDrawingResult() {
			return this.drawingResult;
		}

		//sort from largest to smallest
		public int compareTo(PlayProbability o) {
			return -1 * Double.compare(this.getProbOfPlay(), o.getProbOfPlay());
		}

		
		
	}
	
	public void train(int numTrain) {
		for(String num : map.keySet()) {
			System.err.println("Training: " + num);
			HMM hmm = hmms.get(num);
			List<Symbol> symbols = map.get(num);
			List<Symbol> trainSyms = new ArrayList<Symbol>(numTrain);
			List<Symbol> testSyms = new ArrayList<Symbol>(symbols.size() - numTrain);
			
			train.put(num, trainSyms);
			test.put(num, testSyms);
			
			int count = 0;
			for(Symbol symbol : symbols) {
				if(count > numTrain) {
					testSyms.add(symbol);
				} else {
					trainSyms.add(symbol);
				}
				count++;
			}
			
			count = 0;
			for(DrawingResult drawingResult : drawingResults) {
				if(count > numTrain) {
					drawingResultsTest.add(drawingResult);
				} else {
					drawingResultsTrain.add(drawingResult);
				}
				count++;
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
