package br.usp.ime.icdc.run.stats;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.cogroo.analyzer.Analyzer;
import org.cogroo.analyzer.ComponentFactory;
import org.cogroo.text.Chunk;
import org.cogroo.text.Document;
import org.cogroo.text.Sentence;
import org.cogroo.text.Token;
import org.cogroo.text.impl.DocumentImpl;

import br.usp.ime.icdc.dao.DAOFactory;
import br.usp.ime.icdc.model.DCE;
import weka.core.SelectedTag;
import weka.core.stemmers.PTStemmer;

public class ExamplesGenerator {
	private static final Logger LOG = Logger.getLogger(ExamplesGenerator.class);

	public static void main(String[] args) throws FileNotFoundException {

		ComponentFactory factory = ComponentFactory.create(new FileInputStream(
				"src/main/resources/models.xml"));
		Analyzer cogroo = factory.createPipe();
		Document document = new DocumentImpl();

		PTStemmer stemmer = new PTStemmer();
		stemmer.setStemmer(new SelectedTag(PTStemmer.STEMMER_PORTER,
				PTStemmer.TAGS_STEMMERS));

		final String openBracket = "[";
		final String closeBracket = "]";
		int hospitalId = 1032588;

		DCE dceReport = DAOFactory.getDAOFactory().getDceDAO()
				.locate(hospitalId);

		String text = dceReport.getTexts()[0];

		LOG.info("Original:");
		System.out.println(text);

		document.setText(text);
		cogroo.analyze(document);
		LOG.info("Sentences:");
		for (Sentence sentence : document.getSentences())
			System.out.println(openBracket + sentence.getText() + closeBracket);

		LOG.info("Tokens:");
		for (Sentence sentence : document.getSentences()) {
			for (Token token : sentence.getTokens())
				System.out.print(openBracket + token.getLexeme() + closeBracket
						+ " ");
			System.out.println();
		}

		LOG.info("Stems:");
		for (Sentence sentence : document.getSentences()) {
			for (Token token : sentence.getTokens()) {
				String stem = stemmer.stem(token.getLexeme());
				System.out.print(openBracket + stem + closeBracket + " ");
			}
			System.out.println();
		}

		LOG.info("Lemmas:");
		for (Sentence sentence : document.getSentences()) {
			for (Token token : sentence.getTokens()) {
				String[] lemmas = token.getLemmas();
				String lemma = lemmas.length > 0 ? lemmas[0] : token
						.getLexeme();
				System.out.print(openBracket + lemma + closeBracket + " ");
			}
			System.out.println();
		}

		LOG.info("Chunks:");
		for (Sentence sentence : document.getSentences()) {
			for (Chunk chunk : sentence.getChunks()) {
				System.out.print(openBracket);
				for (Token token : chunk.getTokens()) {
					String[] lemmas = token.getLemmas();
					String lemma = lemmas.length > 0 ? lemmas[0] : token
							.getLexeme();
					System.out.print(lemma + " ");
				}
				System.out.print(closeBracket + " ");
			}
			System.out.println();
		}
	}
}
