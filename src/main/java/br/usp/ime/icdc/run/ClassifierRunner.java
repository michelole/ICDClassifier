package br.usp.ime.icdc.run;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;
import br.usp.ime.icdc.Configuration;
import br.usp.ime.icdc.Configuration.Chunkers;
import br.usp.ime.icdc.Configuration.Classifiers;
import br.usp.ime.icdc.Configuration.Criteria;
import br.usp.ime.icdc.Configuration.MetastasisStatus;
import br.usp.ime.icdc.Configuration.SentenceDetectors;
import br.usp.ime.icdc.Configuration.SmoothingTechniques;
import br.usp.ime.icdc.Configuration.Sources;
import br.usp.ime.icdc.Configuration.Stemmers;
import br.usp.ime.icdc.Configuration.Targets;
import br.usp.ime.icdc.Configuration.Tokenizers;
import br.usp.ime.icdc.Constants;
import br.usp.ime.icdc.logic.classifier.ExperimentalCipeClassifier;

public class ClassifierRunner {

	private static final Logger LOG = Logger.getLogger(ClassifierRunner.class);

	public static void main(String[] args) throws Exception {
		
		// TODO modo interativo

		// TODO unificar dataset para morpho e topo e não precisar importar
		// mais. Usar Remove?

		final List<Configuration> configs = new ArrayList<Configuration>();

		Targets[] targets = new Targets[1];
		targets[0] = Targets.TOPOGRAPHY_GROUP;
//		targets[1] = Targets.MORPHOLOGY_GROUP;
//		targets[2] = Targets.TOPOGRAPHY_CATEGORY;				
//		targets[3] = Targets.TOPOGRAPHY;
//		targets[4] = Targets.MORPHOLOGY;
		
//		for (Targets t : targets)
//			configs.add(new Configuration(Criteria.MANY_REPORT_ONE_REGISTRY,
//					Classifiers.NAIVE, SmoothingTechniques.ADD_ONE, Sources.ALL, false,
//					SentenceDetectors.NONE, Tokenizers.WORD, Stemmers.NONE,
//					Chunkers.NONE, t, 1, -1,
//					MetastasisStatus.NONM1));
		
		for (Targets t : targets)
		configs.add(new Configuration(Criteria.MANY_REPORT_ONE_REGISTRY,
				Classifiers.BAYES, SmoothingTechniques.ADD_ONE, Sources.ALL, false,
				SentenceDetectors.NONE, Tokenizers.WORD, Stemmers.NONE,
				Chunkers.NONE, t, 1, -1,
				MetastasisStatus.NONM1));
		
//		for (Targets t : targets)
//			for (int i = 1; i <= 30; i++)
//				configs.add(new Configuration(Criteria.MANY_REPORT_ONE_REGISTRY,
//					Classifiers.BAYES, SmoothingTechniques.ADD_ONE, Sources.ALL, false,
//					SentenceDetectors.NONE, Tokenizers.WORD, Stemmers.NONE,
//					Chunkers.NONE, t, i, -1,
//					MetastasisStatus.NONM1));
		
		/* Suavization */
//		for (Targets t : targets)
//			configs.add(new Configuration(Criteria.MANY_REPORT_ONE_REGISTRY,
//				Classifiers.BAYES, SmoothingTechniques.GOOD_TURING, Sources.ALL, false,
//				SentenceDetectors.NONE, Tokenizers.WORD, Stemmers.NONE,
//				Chunkers.NONE, t, 1, -1,
//				MetastasisStatus.NONM1));
	
//		for (Targets t : targets)
//			configs.add(new Configuration(Criteria.MANY_REPORT_ONE_REGISTRY,
//				Classifiers.BAYES, SmoothingTechniques.ADD_ALPHA, Sources.ALL, false,
//				SentenceDetectors.NONE, Tokenizers.WORD, Stemmers.NONE,
//				Chunkers.NONE, t, 1, -1,
//				MetastasisStatus.NONM1));
		
		/* NLP */
//		for (Targets t : targets)
//		configs.add(new Configuration(Criteria.MANY_REPORT_ONE_REGISTRY,
//				Classifiers.BAYES, SmoothingTechniques.ADD_ALPHA, Sources.ALL, true,
//				SentenceDetectors.COGROO, Tokenizers.COGROO, Stemmers.NONE,
//				Chunkers.NONE, t, 1, -1,
//				MetastasisStatus.NONM1));
		
//		for (Targets t : targets)
//			configs.add(new Configuration(Criteria.MANY_REPORT_ONE_REGISTRY,
//					Classifiers.BAYES, SmoothingTechniques.ADD_ALPHA, Sources.ALL, true,
//					SentenceDetectors.COGROO, Tokenizers.COGROO, Stemmers.PORTER,
//					Chunkers.NONE, t, 1, -1,
//					MetastasisStatus.NONM1));

//		for (Targets t : targets)
//			configs.add(new Configuration(Criteria.MANY_REPORT_ONE_REGISTRY,
//					Classifiers.BAYES, SmoothingTechniques.ADD_ALPHA, Sources.ALL, true,
//					SentenceDetectors.COGROO, Tokenizers.COGROO, Stemmers.COGROO,
//					Chunkers.NONE, t, 1, -1,
//					MetastasisStatus.NONM1));
		
//		for (Targets t : targets)
//			configs.add(new Configuration(Criteria.MANY_REPORT_ONE_REGISTRY,
//					Classifiers.BAYES, SmoothingTechniques.ADD_ALPHA, Sources.ALL, true,
//					SentenceDetectors.COGROO, Tokenizers.COGROO, Stemmers.COGROO,
//					Chunkers.COGROO, t, 1, -1,
//					MetastasisStatus.NONM1));	
		
		ExperimentalCipeClassifier c;
		
		CSVWriter writer = ExperimentalCipeClassifier.getWriter("evaluation.csv");

		for (Configuration config : configs) {
			Constants.CONFIG = config;

			LOG.info("Let's begin.");
			LOG.info(config.getStringRepresentation());
			c = new ExperimentalCipeClassifier();
			
			//c.printCorpusStats();

			// LOG.info("Single evaluation.");
			// c.singleEvaluate();

			// LOG.info("Results as CSV");
			// c.resultsAsCsv(new File("icdc-27-rgh.csv"), 1);
			
			LOG.info("Cross validation");
			c.crossValidate();			
			c.printStatsAsCsvLine(writer, String.valueOf(config.getMinReports()));
			

//			LOG.info("Let's print stats to a file.");
//			c.printStats();
//
//			LOG.info("Let's print classifier details to a file.");
//			c.printClassifier();

//			LOG.info("Increasing words.");
//			c.increasingWords();
			
//			LOG.info("Increasing cross-validate.");
//			c.increasingCrossValidate();
		}
		
		writer.close();

		LOG.info("Finished");
	}
}
