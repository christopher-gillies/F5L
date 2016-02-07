package org.gillies;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kidneyomics.hmm.HMM;
import org.kidneyomics.hmm.Symbol;

public class GameObjectTest {
	
	@Test
	public void test() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("f5.2000.01.02.20016.2.5.json").getFile());
		
		DataReader reader = new DataReader();
		
		List<DrawingResult> results = reader.getResults(file);
		
		GameObject go = GameObject.getGameObject(results, 39);
		Map<String,List<Symbol>> map = go.getMap();
		
		Map<String,HMM> hmms = go.getHMMs();
		
		
		int count = 0;
		for(Map.Entry<String, List<Symbol>> entry : map.entrySet()) {
			//System.err.println(entry.getKey());
			//System.err.println(entry.getValue().size());
			assertEquals(4154,entry.getValue().size());
			count++;
			
			HMM hmm = hmms.get(entry.getKey());
			assertNotNull(hmm);
			
		}
		
		assertEquals(39,count);
		
		
		go.train(3000);
		
		LinkedList<Symbol> sym = new LinkedList<Symbol>();
		sym.add(go.played);
		
		for(Map.Entry<String, HMM> entry : hmms.entrySet()) {
			System.out.println(entry.getKey());

			double probOfP = entry.getValue().evaluate(sym, false);
			
			System.out.println("Prob of play: " + probOfP);
			entry.getValue().printProbabilities();
		}
		
		go.test(10, 20);
		
	}
}
