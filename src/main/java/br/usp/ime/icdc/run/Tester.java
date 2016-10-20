package br.usp.ime.icdc.run;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class Tester {

	public static void sentdetect() {
		InputStream modelIn = null;

		try {
			modelIn = new FileInputStream("./target/classes/pt-sent.bin");
			SentenceModel model = new SentenceModel(modelIn);
			SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
			String sentences[] = sentenceDetector
					.sentDetect("O paciente foi consultado pelo Dr. Fausto Silva. Consulte o prontuário médico.");
			for (String s : sentences) {
				System.out.println(s);
			}
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
	
	public static void tokenize() {
		InputStream modelIn = null;

		try {
			modelIn = new FileInputStream("./target/classes/pt-token.bin");
			TokenizerModel model = new TokenizerModel(modelIn);
			Tokenizer tokenizer = new TokenizerME(model);
			String tokens[] = tokenizer.tokenize("O paciente foi consultado pela Dra. Fernando de Oliveira.");
			for (String s : tokens) {
				System.out.println(s);
			}
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

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) {
		Tester.sentdetect();
		Tester.tokenize();
	}

}
