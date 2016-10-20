package br.usp.ime.icdc.logic.evaluation;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class Stats {

	private static final Logger LOG = Logger.getLogger(Stats.class);

	private int numClasses = 0;
	private int numDocs = 0;
	private int numPatients = 0;

	private int numSentences = 0;
	private int numTokens = 0;

	private int numUpperCaseChars = 0;
	private int numLowerCaseChars = 0;
	private int numNumbers = 0;
	private int numSymbols = 0;

	private Map<String, Integer> numPatientsPerClass = new HashMap<String, Integer>();
	private Map<String, Integer> numDocsPerClass = new HashMap<String, Integer>();

	public void setNumClasses(int numClasses) {
		this.numClasses = numClasses;
	}

	public void incNumDocs(int numDocs) {
		this.numDocs += numDocs;
	}

	public void setNumPatients(int numPatients) {
		this.numPatients = numPatients;
	}

	public void incNumSentences() {
		this.numSentences += 1;
	}

	public void incNumTokens() {
		this.numTokens += 1;
	}

	public void incNumPatientsPerClass(String clazz, int numPatients) {
		if (numPatientsPerClass.containsKey(clazz)) {
			Integer value = numPatientsPerClass.get(clazz);
			value += numPatients;
			numPatientsPerClass.put(clazz, value);
		} else {
			numPatientsPerClass.put(clazz, numPatients);
		}

	}

	public void incNumDocsPerClass(String clazz, int numDocs) {
		if (numDocsPerClass.containsKey(clazz)) {
			Integer value = numDocsPerClass.get(clazz);
			value += numDocs;
			numDocsPerClass.put(clazz, value);
		} else {
			numDocsPerClass.put(clazz, numDocs);
		}
	}

	public void countCharTypes(String text) {
		int lower = 0, upper = 0, symbol = 0, number = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (Character.isLowerCase(c))
				lower++;
			else if (Character.isUpperCase(c))
				upper++;
			else if (Character.isDigit(c))
				number++;
			else if (!Character.isWhitespace(c)) {
				//LOG.trace("Symbol char found: " + c);
				symbol++;
			}
		}

		this.numLowerCaseChars += lower;
		this.numUpperCaseChars += upper;
		this.numSymbols += symbol;
		this.numNumbers += number;
	}

	public int getNumClasses() {
		return numClasses;
	}

	public int getNumDocs() {
		return numDocs;
	}

	public int getNumPatients() {
		return numPatients;
	}

	public int getNumSentences() {
		return numSentences;
	}

	public int getNumTokens() {
		return numTokens;
	}

	public int getNumChars() {
		return numLowerCaseChars + numUpperCaseChars + numSymbols + numNumbers;
	}
	
	public float getRatioCharSentence() {
		return getNumChars() / (float) getNumSentences();
	}
	
	public float getRatioTokenSentence() {
		return getNumTokens() / (float) getNumSentences();
	}

	public float getRatioUpperLower() {
		return numUpperCaseChars / (float)(numLowerCaseChars + numUpperCaseChars);
	}

	public float getRatioNumbersChars() {
		return numNumbers / (float)getNumChars();
	}

	public String getNumDocsPerClassAsTable() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Integer> entry : numDocsPerClass.entrySet()) {
			sb.append(entry.getKey());
			sb.append("\t");
			sb.append(entry.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}

	public String getNumPatientsPerClassAsTable() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Integer> entry : numPatientsPerClass.entrySet()) {
			sb.append(entry.getKey());
			sb.append("\t");
			sb.append(entry.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}

}
