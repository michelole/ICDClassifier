package br.usp.ime.icdc;

import java.util.Arrays;
import java.util.Set;

public class Configuration {
	public enum Sections {
		MACROSCOPY, MICROSCOPY, CYTOPATHOLOGY, LIQUID_CYTOPATHOLOGY, CONCLUSION, OTHERS;
	}
	
	public enum Criteria {
		ONE_REPORT_ONE_REGISTRY, MANY_REPORT_ONE_REGISTRY, MANY_REPORT_MANY_REGISTRY;
	}

	public enum Classifiers {
		NAIVE, BAYES, BAYES_NET, BERNOULLI;
	}

	public enum SmoothingTechniques {
		ADD_ONE, ADD_ALPHA, GOOD_TURING;
	}

	public enum Sources {
		DCE_REPORT, SIGH_REPORT, TOTLAUD_REPORT, ALL;
	}

	public enum Stemmers {
		// TODO test with PTStemmer V1 available at Maven repositories
		NONE, ORENGO, PORTER, SAVOY, COGROO;
	}

	public enum SentenceDetectors {
		NONE, OPENNLP, COGROO;
	}

	public enum Tokenizers {
		WORD, ALPHABETIC, OPENNLP, COGROO;
	}

	public enum Chunkers {
		NONE, COGROO;
	}

	public enum Targets {
		MORPHOLOGY, MORPHOLOGY_GROUP, TOPOGRAPHY, TOPOGRAPHY_CATEGORY, TOPOGRAPHY_GROUP;
	}

	public enum MetastasisStatus {
		ALL, NONM1;
	}

	private Set<Sections> sections;
	private Criteria criteria;
	private int minReports = 1;
	private Classifiers classifier;
	private SmoothingTechniques smoothing;
	private Sources source;
	private boolean stoplist;
	private SentenceDetectors sentenceDetector;
	private Tokenizers tokenizer;
	private Stemmers stemmer;
	private Chunkers chunker;
	private Targets target;
	private MetastasisStatus meta;
	private int patientYear = -1;

	public Configuration(Set<Sections> sections, Criteria criteria, Classifiers classifier,
			SmoothingTechniques smoothing, Sources source, boolean stoplist,
			SentenceDetectors sentenceDetector, Tokenizers tokenizer,
			Stemmers stemmer, Chunkers chunker, Targets target, int minReports,
			int year, MetastasisStatus meta) {
		super();
		this.sections = sections;
		this.criteria = criteria;
		if (criteria == Criteria.MANY_REPORT_ONE_REGISTRY)
			this.minReports = minReports;
		this.classifier = classifier;
		this.smoothing = smoothing;
		this.source = source;
		this.stoplist = stoplist;
		this.sentenceDetector = sentenceDetector;
		this.tokenizer = tokenizer;
		this.stemmer = stemmer;
		this.chunker = chunker;
		this.target = target;
		this.patientYear = year;
		this.meta = meta;
	}
	
	public Set<Sections> getSections() {
		return sections;
	}

	public Criteria getCriteria() {
		return criteria;
	}

	public int getMinReports() {
		return minReports;
	}

	public Classifiers getClassifier() {
		return classifier;
	}

	public SmoothingTechniques getSmoothing() {
		return smoothing;
	}

	public Sources getSource() {
		return source;
	}

	public boolean getStoplist() {
		return stoplist;
	}

	public SentenceDetectors getSentenceDetector() {
		return sentenceDetector;
	}

	public Tokenizers getTokenizer() {
		return tokenizer;
	}

	public Stemmers getStemmer() {
		return stemmer;
	}

	public Chunkers getChunker() {
		return chunker;
	}

	public Targets getTarget() {
		return target;
	}

	public MetastasisStatus getMetastasisStatus() {
		return meta;
	}

	public int getPatientYear() {
		return patientYear;
	}

	public String getInstanceDependentStringRepresentation() {
		return (Arrays.toString(sections.toArray()) + "-" + criteria + "-" + minReports + "-" + source + "-" + sentenceDetector
				+ "-" + tokenizer + "Tokenizer-" + stemmer + "Stemmer-" + chunker + "Chunker-"
				+ target + "-" + meta + "-" + patientYear).toLowerCase();
	}

	public String getStringRepresentation() {
		return (Arrays.toString(sections.toArray()) + "-" + criteria + "-" + minReports + "-" + classifier + "-"
				+ smoothing + "-" + Constants.ALPHA + "-" + source + "-"
				+ stoplist + "-" + sentenceDetector + "-" + tokenizer + "-"
				+ stemmer + "-" + chunker + "-" + target + "-" + meta + "-" + patientYear)
				.toLowerCase();
	}

}
