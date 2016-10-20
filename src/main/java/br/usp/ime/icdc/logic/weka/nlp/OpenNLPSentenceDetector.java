package br.usp.ime.icdc.logic.weka.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import br.usp.ime.icdc.Constants;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

/**
 * OpenNLP sentence detector wrapper compatible with Weka API.
 * 
 * @author michel
 * 
 */
public class OpenNLPSentenceDetector extends br.usp.ime.icdc.logic.weka.nlp.SentenceDetector {

	private SentenceDetector sentenceDetector = null;

	private String[] sentences = null;

	private int pos = 0;

	public OpenNLPSentenceDetector() {
		String modelsDir = Constants.MODEL_DIR;
		
		InputStream modelIn = null;
		try {
			modelIn = new FileInputStream(modelsDir + "pt-sent.bin");
			SentenceModel model = new SentenceModel(modelIn);
			sentenceDetector = new SentenceDetectorME(model);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public String getRevision() {
		return "Revision: 1.0";
	}

	@Override
	public String globalInfo() {
		return "Weka wrapper to Brazilian Portuguese OpenNLP sentence detector";
	}
	
	@Override
	public boolean hasMoreElements() {
		if (pos < sentences.length)
			return true;
		else
			return false;
	}

	@Override
	public Object nextElement() {
		return sentences[pos++];
	}

	@Override
	public void sentDetect(String s) {
		reset();
		sentences = sentenceDetector.sentDetect(s);
	}

	private void reset() {
		pos = 0;
		sentences = null;
	}
	
	public static void main(String[] args) throws IOException {
		runSentenceDetector(new OpenNLPSentenceDetector(), args);
	}
}
