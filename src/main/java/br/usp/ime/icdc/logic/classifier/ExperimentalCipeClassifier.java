package br.usp.ime.icdc.logic.classifier;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.filters.Filter;
import au.com.bytecode.opencsv.CSVWriter;
import br.usp.ime.icdc.Constants;
import br.usp.ime.icdc.logic.evaluation.Stats;
import br.usp.ime.icdc.logic.weka.evaluation.ExtendedEvaluation;

public class ExperimentalCipeClassifier extends CipeClassifier {

	private final Random rand = new Random(42);

	public static CSVWriter getWriter(String filename) {
		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new FileWriter(filename), ';');
			String[] header = { "Var", "N", "P", "R", "F1", "F2", "A" };
			writer.writeNext(header);
			writer.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer;
	}

	/**
	 * @todo use threads
	 */
	public void increasingCrossValidate() {
		final String id = "increasing-cross-validate-"
				+ Constants.CONFIG.getStringRepresentation();
		CSVWriter writer = getWriter(id + ".csv");
		printBaseline(writer);

		int points = 100;
		int numInstances = dataset.numInstances();

		dataset.randomize(rand);

		double logp = Math.log(points);
		double log;
		int max;
		Instances test = null;
		ExtendedEvaluation e = null;

		for (int i = 0; i < points; i++) {
			log = Math.log(points - i) / logp;
			max = (int) ((numInstances * (1 - log)) + 10);
			max = (max > numInstances) ? numInstances : max;
			test = new Instances(dataset, 0, max);
			try {
				e = new ExtendedEvaluation(test);
				e.crossValidateModel(model, test, 10, rand);
			} catch (Exception ex) {
				ex.printStackTrace();
				continue;
			}

			printStatsAsCsvLine(writer, String.valueOf(max), test, e);
		}

		try {
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void increasingWords() {
		final String id = "increasing-words-"
				+ Constants.CONFIG.getStringRepresentation();
		CSVWriter writer = getWriter(id + ".csv");
		printBaseline(writer);

		int points = 100;
		int n, max = 10000;
		double logp = Math.log(points);
		double log;

		for (int i = 0; i < points; i++) {
			log = Math.log(points - i) / logp;
			n = (int) ((max * (1 - log)) + 1);
			n = (n > max) ? max : n;

			setWordsToKeep(n);
			crossValidate();
			printStatsAsCsvLine(writer, String.valueOf(max));
		}

		try {
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Print config and stats as a CSV line.
	 */
	public void printStatsAsCsvLine(CSVWriter writer, String id,
			Instances dataset, ExtendedEvaluation eval) {
		int n = dataset.numInstances();
		double p = eval.microAveragedPrecision();
		double r = eval.microAveragedRecall();
		double f1 = eval.microAveragedFMeasure(1);
		double f2 = eval.microAveragedFMeasure(2);
		double a = eval.accuracy();

		String[] line = { id, String.valueOf(n), String.valueOf(p),
				String.valueOf(r), String.valueOf(f1), String.valueOf(f2),
				String.valueOf(a) };
		writer.writeNext(line);
		try {
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Print current config and stats as a CSV line.
	 */
	public void printStatsAsCsvLine(CSVWriter writer, String id) {
		printStatsAsCsvLine(writer, id, this.dataset, this.eval);
	}

	public void printBaseline(CSVWriter writer) {
		String[] baseline = { "0", "0", "0", "0", "0", "0", "0" };
		writer.writeNext(baseline);
		try {
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printStats() {
		String filename = Constants.CONFIG.getStringRepresentation() + ".txt";
		try {
			printStats(new PrintWriter(filename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printCorpusStats() {
		String filename = "corpusStats-"
				+ Constants.CONFIG.getStringRepresentation() + ".txt";
		try {
			printCorpusStats(new PrintWriter(filename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printCorpusStats(PrintWriter out) {
		// TODO unify with printStats
		if (!Constants.STATS)
			return;

		out.println(Constants.CONFIG.getStringRepresentation());
		out.println();

		out.println("Number of classes: " + stats.getNumClasses());
		out.println("Number of docs: " + stats.getNumDocs());
		out.println("Number of patients: " + stats.getNumPatients());
		out.println("Number of sentences: " + stats.getNumSentences());
		out.println("Number of tokens: " + stats.getNumTokens());
		out.println("Number of chars: " + stats.getNumChars());
		out.println("Ratio char/sentence: " + stats.getRatioCharSentence());
		out.println("Ratio token/sentence: " + stats.getRatioTokenSentence());
		out.println("Ratio upper/lower: " + stats.getRatioUpperLower());
		out.println("Ratio numbers/chars: " + stats.getRatioNumbersChars());
		out.println();

		out.println("Number of docs per class:");
		out.println(stats.getNumDocsPerClassAsTable());

		out.println("Number of patients per class:");
		out.println(stats.getNumPatientsPerClassAsTable());

		out.close();
	}

	public void printStats(PrintWriter out) {
		// out.println(eval.numInstances());
		// out.println();
		// out.println(eval.pctCorrect());
		// out.println();

		out.println(Constants.CONFIG.getStringRepresentation());

		// out.println(classifier.eval.confusionMatrix());

		try {
			// out.println(eval.toCumulativeMarginDistributionString());
			out.println(eval.toSummaryString(/* true */));
			out.println(eval.toClassDetailsString());
			out.println(eval.toMatrixString());
			// out.println(dataset.toSummaryString());
			out.println("Macro-Precision: " + eval.macroAveragedPrecision());
			out.println("Micro-Precision: " + eval.microAveragedPrecision());
			out.println("Macro-Recall: " + eval.macroAveragedRecall());
			out.println("Micro-Recall: " + eval.microAveragedRecall());
			out.println("Macro-F1: " + eval.macroAveragedFMeasure(1));
			out.println("Micro-F1: " + eval.microAveragedFMeasure(1));
			out.println("Macro-F2: " + eval.macroAveragedFMeasure(2));
			out.println("Micro-F2: " + eval.microAveragedFMeasure(2));
			out.println("Accuracy: " + eval.accuracy());
			out.println("Number of classes: " + dataset.numClasses());
			out.println("Number of attributes: " + dataset.numAttributes());
			if (model instanceof FilteredClassifier) {
				model.buildClassifier(dataset);
				int att = Filter
						.useFilter(dataset,
								((FilteredClassifier) model).getFilter())
						.stringFreeStructure().numAttributes();
				out.println("Number of attributes after filter: " + att);
			}
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Stats getStats() {
		return this.stats;
	}

}
