package br.usp.ime.icdc.run;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import br.usp.ime.icdc.model.Patient;
import br.usp.ime.icdc.model.PatientIterator;

public class ARFFGenerator {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		HashMap<String,HashMap<Integer,Integer>> invertedIndex = new HashMap<String, HashMap<Integer,Integer>>();
		HashSet<String> cids = new HashSet<String>();
		
		int i = 0;
		PatientIterator iter = new PatientIterator();
		while (iter.hasNext()) {
			Patient p = iter.next();
			
			String cid = p.getRhc().get(0).getTopography().getCode();
			cids.add(cid);
			
			String text = p.getDce().get(0).getText();
			text = text.replaceAll("[,:%\"]", "").replaceAll("\\p{javaWhitespace}", " ");
			String[] tokens = text.split(" ");
			for (String t : tokens) {
				if (t.isEmpty())
					continue;
				if (!invertedIndex.containsKey(t)) {
					HashMap<Integer,Integer> docs = new HashMap<Integer,Integer>();
					docs.put(new Integer(i),new Integer(1));
					invertedIndex.put(t,docs);
				}
				else {
					HashMap<Integer,Integer> docs = invertedIndex.get(t);
					if (!docs.containsKey(new Integer(i))) {
						docs.put(new Integer(i), new Integer(1));
					}
					else {
						Integer count = docs.get(new Integer(i));
						docs.put(new Integer(i), count+1);
					}
				}
			}
			i++;
		}
		
		PrintStream out = new PrintStream(new File("bagwords-bayes.arff"));
		out.println("@relation cancer");
		out.println();
		
		for (String word : invertedIndex.keySet())
			out.println("@attribute " + word + " numeric");
		
		StringBuilder sb = new StringBuilder();
		for (String cid : cids)
			sb.append(cid + ",");
		sb.deleteCharAt(sb.length()-1);
		out.println("@attribute class {" + sb.toString() + "}");
		out.println();
		
		out.println("@data");
		
		iter = new PatientIterator();
		i = 0;
		while (iter.hasNext()) {
			Patient p = iter.next();
			Iterator<Map.Entry<String, HashMap<Integer, Integer>>> mapIter = invertedIndex.entrySet().iterator();
			while (mapIter.hasNext()) {
				Map.Entry<String, HashMap<Integer,Integer>> entry = mapIter.next();
				//String contains = entry.getValue().containsKey(new Integer(i)) ? "1" : "0";
				//out.print(contains + ",");
				String count = entry.getValue().containsKey(new Integer(i)) ? entry.getValue().get(new Integer(i)).toString() : "0";
				out.print(count + ",");
			}
			
			//String cancer = p.getRhc().size() > 0 ? "yes" : "no";
			//out.println(cancer);
			out.println(p.getRhc().get(0).getTopography().getCode());
			i++;
		}
		
		out.close();

	}

}
