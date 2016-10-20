package br.usp.ime.icdc.logic.weka.nlp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

import weka.core.OptionHandler;
import weka.core.RevisionHandler;

/**
 * A superclass for all sentence detector algorithms.
 * 
 * @author  michel
 * @version 1.0
 */
public abstract class SentenceDetector implements Enumeration, OptionHandler,
		Serializable, RevisionHandler {

	/**
	 * Returns a string describing the stemmer
	 * 
	 * @return a description suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public abstract String globalInfo();

	/**
	 * Returns an enumeration of all the available options..
	 * 
	 * @return an enumeration of all available options.
	 */
	public Enumeration listOptions() {
		return (new Vector()).elements();
	}

	/**
	 * Gets the current option settings for the OptionHandler.
	 * 
	 * @return the list of current option settings as an array of strings
	 */
	public String[] getOptions() {
		return new String[0];
	}

	/**
	 * Sets the OptionHandler's options using the given list. All options will
	 * be set (or reset) during this call (i.e. incremental setting of options
	 * is not possible).
	 * 
	 * @param options
	 *            the list of options as an array of strings
	 * @throws Exception
	 *             if an option is not supported
	 */
	public void setOptions(String[] options) throws Exception {
		// nothing in this class
	}

	/**
	 * Tests if this enumeration contains more elements.
	 * 
	 * @return true if and only if this enumeration object contains at least one
	 *         more element to provide; false otherwise.
	 */
	public abstract boolean hasMoreElements();

	/**
	 * Returns the next element of this enumeration if this enumeration object
	 * has at least one more element to provide.
	 * 
	 * @return the next element of this enumeration.
	 */
	public abstract Object nextElement();

	public abstract void sentDetect(String s);

	public static String[] sentDetect(SentenceDetector sentenceDetector,
			String[] options) throws Exception {
		Vector<String> result;
		Vector<String> tmpResult;
		Vector<String> data;
		int i;
		boolean processed;
		BufferedReader reader;
		String line;

		result = new Vector<String>();

		// init sentence detector
		sentenceDetector.setOptions(options);

		// for storing the data to process
		data = new Vector<String>();

		// run over all un-processed strings in the options array
		processed = false;
		for (i = 0; i < options.length; i++) {
			if (options[i].length() != 0) {
				processed = true;
				data.add(options[i]);
			}
		}

		// if no strings in option string then read from stdin
		if (!processed) {
			reader = new BufferedReader(new InputStreamReader(System.in));
			while ((line = reader.readLine()) != null) {
				data.add(line);
			}
		}

		// process data
		for (i = 0; i < data.size(); i++) {
			tmpResult = new Vector<String>();
			sentenceDetector.sentDetect(data.get(i));
			while (sentenceDetector.hasMoreElements())
				tmpResult.add((String) sentenceDetector.nextElement());
			// add to result
			result.addAll(tmpResult);
		}

		return result.toArray(new String[result.size()]);
	}

	/**
	 * initializes the given sentence detector with the given options and runs the
	 * sentence detector over all the remaining strings in the options array. The
	 * generated tokens are then printed to stdout. If no strings remained in
	 * the option string then data is read from stdin, line by line.
	 * 
	 * @param sentenceDetector
	 *            the sentence detector to use
	 * @param options
	 *            the options for the sentence detector
	 */
	public static void runSentenceDetector(SentenceDetector sentenceDetector, String[] options) {
		String[] result;
		int i;

		try {
			result = sentDetect(sentenceDetector, options);
			for (i = 0; i < result.length; i++)
				System.out.println(result[i]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
