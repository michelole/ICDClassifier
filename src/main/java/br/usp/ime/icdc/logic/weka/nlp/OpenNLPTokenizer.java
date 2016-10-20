package br.usp.ime.icdc.logic.weka.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import br.usp.ime.icdc.Constants;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 * 
 * @author michel
 * @version 1.0
 */
public class OpenNLPTokenizer extends weka.core.tokenizers.Tokenizer {

	private Tokenizer tokenizer = null;

	private String[] tokens = null;

	private int pos = 0;

	public OpenNLPTokenizer() {
		String modelsDir = Constants.MODEL_DIR;
		
		InputStream modelIn = null;
		try {
			// TODO set static way?
			modelIn = new FileInputStream(modelsDir + "pt-tok.bin");
			TokenizerModel model = new TokenizerModel(modelIn);
			tokenizer = new TokenizerME(model);
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
		return "Weka wrapper to Brazilian Portuguese OpenNLP Tokenizer";
	}

	@Override
	public boolean hasMoreElements() {
		if (pos < tokens.length)
			return true;
		else
			return false;
	}

	@Override
	public Object nextElement() {
		return tokens[pos++];
	}

	@Override
	public void tokenize(String s) {
		reset();
		tokens = tokenizer.tokenize(s);
	}
	
	private void reset() {
		pos = 0;
		tokens = null;
	}

	/**
	 * Runs the tokenizer with the given options and strings to tokenize. The
	 * tokens are printed to stdout.
	 * 
	 * @param args
	 *            the commandline options and strings to tokenize
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		runTokenizer(new OpenNLPTokenizer(), args);
	}
}
