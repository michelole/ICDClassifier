package br.usp.ime.icdc.run;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;
import br.usp.ime.icdc.Configuration;
import br.usp.ime.icdc.Configuration.Chunkers;
import br.usp.ime.icdc.Configuration.Classifiers;
import br.usp.ime.icdc.Configuration.Criteria;
import br.usp.ime.icdc.Configuration.MetastasisStatus;
import br.usp.ime.icdc.Configuration.Sections;
import br.usp.ime.icdc.Configuration.SentenceDetectors;
import br.usp.ime.icdc.Configuration.SmoothingTechniques;
import br.usp.ime.icdc.Configuration.Sources;
import br.usp.ime.icdc.Configuration.Stemmers;
import br.usp.ime.icdc.Configuration.Targets;
import br.usp.ime.icdc.Configuration.Tokenizers;
import br.usp.ime.icdc.Configuration.WeightingSchemes;
import br.usp.ime.icdc.Constants;
import br.usp.ime.icdc.logic.classifier.ExperimentalCipeClassifier;

public class ClassifierRunner {

	private static final Logger LOG = Logger.getLogger(ClassifierRunner.class);

	public static void main(String[] args) throws Exception {

		// TODO modo interativo

		// TODO unificar dataset para morpho e topo e não precisar importar
		// mais. Usar Remove?

		final List<Configuration> configs = new ArrayList<Configuration>();

		Set<Sections> sections = new HashSet<Sections>();
		sections.add(Sections.MACROSCOPY);
		sections.add(Sections.MICROSCOPY);
		sections.add(Sections.CYTOPATHOLOGY);
		sections.add(Sections.OTHERS);
		sections.add(Sections.CONCLUSION);
		sections.add(Sections.LIQUID_CYTOPATHOLOGY);

		double cost;
		int power[] = {-7, -3, -11};
		for (int p : power) {
			cost = Math.pow(2, p);
			configs.add(new Configuration(sections, Criteria.MANY_REPORT_ONE_REGISTRY, Classifiers.SVM,
					WeightingSchemes.TFIDF, SmoothingTechniques.ADD_ONE, cost, Sources.ALL, true, 2,
					SentenceDetectors.NONE, Tokenizers.WORD, Stemmers.NONE, Chunkers.NONE, Targets.TOPOGRAPHY_GROUP, 1,
					-1, MetastasisStatus.NONM1));

		}

		ExperimentalCipeClassifier c;

		CSVWriter writer = ExperimentalCipeClassifier.getWriter(Constants.CONFIG.getStringRepresentation() + ".csv");

		for (Configuration config : configs) {
			Constants.CONFIG = config;

			LOG.info("Let's begin.");
			LOG.info(config.getStringRepresentation());
			c = new ExperimentalCipeClassifier();

			LOG.info("Let's print corpus stats to a file.");
			c.printCorpusStats();

			// LOG.info("Single evaluation.");
			// c.singleEvaluate();

			// LOG.info("Results as CSV");
			// c.resultsAsCsv(new File("icdc-27-rgh.csv"), 1);

			LOG.info("Cross validation");
			c.crossValidate();
			c.printStatsAsCsvLine(writer, Double.toString(config.getSvmCostParameter()));

			LOG.info("Let's print stats to a file.");
			c.printStats();
			//
			LOG.info("Let's print classifier details to a file.");
			c.printClassifier();

			// LOG.info("Increasing words.");
			// c.increasingWords();

			// LOG.info("Increasing cross-validate.");
			// c.increasingCrossValidate();
		}

		writer.close();

		LOG.info("Finished");
	}
}
