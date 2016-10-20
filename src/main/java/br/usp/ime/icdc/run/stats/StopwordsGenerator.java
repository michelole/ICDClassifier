package br.usp.ime.icdc.run.stats;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;
import br.usp.ime.icdc.dao.DAOFactory;
import br.usp.ime.icdc.dao.DceDAO;
import br.usp.ime.icdc.dao.SighDAO;
import br.usp.ime.icdc.model.DCE;
import br.usp.ime.icdc.model.SighReport;

public class StopwordsGenerator {

	private static final Logger LOG = Logger
			.getLogger(StopwordsGenerator.class);

	public static final Integer N = 50;

	public static void main(String[] args) throws IOException {
		Pattern whiteSpace = Pattern.compile("\\p{javaWhitespace}");
		Pattern stopSymbol = Pattern.compile("[,:\\*\\.\\(\\)\\-_\\+%\"]");

		LOG.debug("(0/4) Begin!");

		Map<String, Integer> wordFrequency = new HashMap<String, Integer>();		
		
		DceDAO dceDao = DAOFactory.getDAOFactory().getDceDAO();
		Iterator<DCE> dceIter = dceDao.list().iterator();
		
		LOG.debug("(1/4) Creating hashmap DCE...");
		while (dceIter.hasNext()) {
			DCE r = dceIter.next();
			String text = r.getText().toLowerCase();
			text = stopSymbol.matcher(text).replaceAll("");
			String[] tokens = whiteSpace.split(text);
			for (String s : tokens) {
				Integer count = 0;
				if (wordFrequency.containsKey(s)) {
					// Token is already at the map.
					count = wordFrequency.remove(s);
					count++;
				} else {
					// New token.
					count = 1;
				}
				wordFrequency.put(s, count);
			}
		}
					
		SighDAO sighDao = DAOFactory.getDAOFactory().getSighDAO();
		Iterator<SighReport> sighIter = sighDao.list().iterator();
		
		LOG.debug("(2/4) Creating hashmap SIGH...");
		while (sighIter.hasNext()) {
			// DCE r = iter.next();
			SighReport r = sighIter.next();
			String text = r.getText().toLowerCase();
			text = stopSymbol.matcher(text).replaceAll("");
			String[] tokens = whiteSpace.split(text);
			for (String s : tokens) {
				Integer count = 0;
				if (wordFrequency.containsKey(s)) {
					// Token is already at the map.
					count = wordFrequency.remove(s);
					count++;
				} else {
					// New token.
					count = 1;
				}
				wordFrequency.put(s, count);
			}
		}

		/* ************************************************* */

		LOG.debug("(3/4) Creating treemap...");

		TreeMap<Integer, List<String>> invertedWordFreq = new TreeMap<Integer, List<String>>();

		Iterator<Map.Entry<String, Integer>> wfIter = wordFrequency.entrySet()
				.iterator();
		while (wfIter.hasNext()) {
			Map.Entry<String, Integer> entry = wfIter.next();
			String token = entry.getKey();
			Integer count = entry.getValue();
			List<String> tokens = null;
			if (invertedWordFreq.containsKey(count)) {
				// Count is already taken. Add new token to the value array.
				tokens = invertedWordFreq.remove(count);
			} else {
				// New key in map. Create a new array with a single token.
				tokens = new ArrayList<String>();
			}
			tokens.add(token);
			invertedWordFreq.put(count, tokens);
		}

		/* ************************************************* */

		LOG.debug("(4/4) Printing stoplist...");
		
		CSVWriter writer = new CSVWriter(new FileWriter("stopwords.csv"), ';');

		Iterator<Map.Entry<Integer, List<String>>> iwfIter = invertedWordFreq
				.descendingMap().entrySet().iterator();
		int i = 0;
		boolean enough = false;
		while (iwfIter.hasNext() && !enough) {
			Map.Entry<Integer, List<String>> entry = iwfIter.next();
			for (String s : entry.getValue()) {
				String[] line = {s, String.valueOf(entry.getKey())};
				writer.writeNext(line);
				i++;
				if (i == StopwordsGenerator.N) {
					enough = true;
					break;
				}
			}
		}
		
		writer.close();
	}

}
