package br.usp.ime.icdc.logic.classifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.cogroo.analyzer.Analyzer;
import org.cogroo.analyzer.ComponentFactory;
import org.cogroo.text.Chunk;
import org.cogroo.text.Document;
import org.cogroo.text.Sentence;
import org.cogroo.text.Token;
import org.cogroo.text.impl.DocumentImpl;

import au.com.bytecode.opencsv.CSVWriter;
import br.usp.ime.icdc.Configuration;
import br.usp.ime.icdc.Constants;
import br.usp.ime.icdc.dao.DAOFactory;
import br.usp.ime.icdc.logic.evaluation.Stats;
import br.usp.ime.icdc.logic.weka.classifiers.AddAlphaNaiveBayesMultinomial;
import br.usp.ime.icdc.logic.weka.classifiers.GoodTuringNaiveBayesMultinomial;
import br.usp.ime.icdc.logic.weka.classifiers.NaiveClassifier;
import br.usp.ime.icdc.logic.weka.evaluation.ExtendedEvaluation;
import br.usp.ime.icdc.model.Patient;
import br.usp.ime.icdc.model.PatientIterator;
import br.usp.ime.icdc.model.RHC;
import br.usp.ime.icdc.model.Report;
import br.usp.ime.icdc.model.icd.Classifiable;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.stemmers.PTStemmer;
import weka.core.tokenizers.AlphabeticTokenizer;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class CipeClassifier {

	protected Instances dataset;
	protected Classifier model;

	// TODO remove (as its dependent on dataset used to eval).
	protected ExtendedEvaluation eval;

	protected Stats stats = new Stats();

	// TODO DB to ARFF converter to be used on GUI

	private static final Logger LOG = Logger.getLogger(CipeClassifier.class);

	public CipeClassifier() {
		parseClassifier();
		loadInstances();
		// buildClassifier();
	}

	public void setWordsToKeep(int wordsToKeep) {
		MultiFilter mf = (MultiFilter) ((FilteredClassifier) model).getFilter();
		Filter[] filters = mf.getFilters();
		for (Filter f : filters)
			if (f instanceof StringToWordVector)
				((StringToWordVector) f).setWordsToKeep(wordsToKeep);

		// TODO Precisa setar novamente o filter?
	}

	protected void parseClassifier() {
		String refDir = Constants.REF_DIR;

		LOG.debug("Config: " + Constants.CONFIG);
		switch (Constants.CONFIG.getClassifier()) {
		case NAIVE:
			model = new NaiveClassifier();
			break;
		case BAYES: {
			model = new FilteredClassifier();
			StringToWordVector f = new StringToWordVector();
			f.setAttributeIndices("first");
			f.setDoNotOperateOnPerClassBasis(true);
			f.setLowerCaseTokens(true);
			f.setMinTermFreq(1);
			f.setOutputWordCounts(true); // XXX This is Bayes!
			f = parseStemmer(f);
			// TODO qual a ordem em relação ao stemmer? stopwords devem ser
			// eliminadas ANTES!
			f.setStopwords(new File(refDir + "stopwords-sigh.txt")); // Stopwords!

			f = parseTokenizer(f);

			f.setUseStoplist(Constants.CONFIG.getStoplist()); // Stopwords!
			f.setWordsToKeep(5000);
			
			switch (Constants.CONFIG.getWeightScheme()) {
			case TF:
				f.setTFTransform(true);
				break;
			case IDF:
				f.setIDFTransform(true);
				break;
			case TFIDF:
				f.setTFTransform(true);
				f.setIDFTransform(true);
			default:
				break;
			}

			NaiveBayesMultinomial nbm = parseSmoothing();
			// nbm.setOptions(options);

			Remove remove = new Remove();
			remove.setAttributeIndices("1,2,4,5");

			MultiFilter mf = new MultiFilter();
			mf.setFilters(new Filter[] { remove, f });

			((FilteredClassifier) model).setFilter(mf);
			((FilteredClassifier) model).setClassifier(nbm);

			break;
		}
		case BERNOULLI: {
			model = new FilteredClassifier();
			StringToWordVector f = new StringToWordVector();
			f.setAttributeIndices("first");
			f.setDoNotOperateOnPerClassBasis(true);
			f.setLowerCaseTokens(true);
			f.setMinTermFreq(1);
			f.setOutputWordCounts(false); // XXX This is Bernoulli!
			f = parseStemmer(f);
			// TODO generate list again with 90% of all data.
			f.setStopwords(new File(refDir + "stopwords-sigh.txt")); // Stopwords!

			f = parseTokenizer(f);

			f.setUseStoplist(Constants.CONFIG.getStoplist()); // Stopwords!
			f.setWordsToKeep(5000);

			NaiveBayesMultinomial nbm = parseSmoothing();

			// nbm.setOptions(options);

			((FilteredClassifier) model).setFilter(f);
			((FilteredClassifier) model).setClassifier(nbm);
			break;
		}
		case SVM: {
			model = new FilteredClassifier();
			StringToWordVector f = new StringToWordVector();
			f.setAttributeIndices("first");
			f.setDoNotOperateOnPerClassBasis(true);
			f.setLowerCaseTokens(true);
			f.setMinTermFreq(1);
			f.setOutputWordCounts(true);
			f = parseStemmer(f);
			// TODO qual a ordem em relação ao stemmer? stopwords devem ser
			// eliminadas ANTES!
			f.setStopwords(new File(refDir + "stopwords-sigh.txt")); // Stopwords!

			f = parseTokenizer(f);

			f.setUseStoplist(Constants.CONFIG.getStoplist()); // Stopwords!
			f.setWordsToKeep(5000);
			
			switch (Constants.CONFIG.getWeightScheme()) {
			case TF:
				f.setTFTransform(true);
				break;
			case IDF:
				f.setIDFTransform(true);
				break;
			case TFIDF:
				f.setTFTransform(true);
				f.setIDFTransform(true);
			default:
				break;
			}

			LibSVM svm = new LibSVM();
			
			// -K <int>
			// Set type of kernel function (default: 2)
			// 0 = linear: u'*v
			// 1 = polynomial: (gamma*u'*v + coef0)^degree
			// 2 = radial basis function: exp(-gamma*|u-v|^2)
			// 3 = sigmoid: tanh(gamma*u'*v + coef0)
			// Linear kernel is commonly recommended for text classification
			svm.setKernelType(new SelectedTag(0, LibSVM.TAGS_KERNELTYPE));
			
			svm.setCost(Constants.CONFIG.getSvmCostParameter());

			Remove remove = new Remove();
			remove.setAttributeIndices("1,2,4,5");

			MultiFilter mf = new MultiFilter();
			mf.setFilters(new Filter[] { remove, f });

			((FilteredClassifier) model).setFilter(mf);
			((FilteredClassifier) model).setClassifier(svm);

			break;
		}
		default:
			throw new IllegalArgumentException(Constants.CONFIG.getClassifier() + " is not implemented.");
		}
	}

	private StringToWordVector parseTokenizer(StringToWordVector filter) {
		switch (Constants.CONFIG.getTokenizer()) {
		case ALPHABETIC:
			// Avoid. Does not support diacritics (ã, á, é, etc.)
			filter.setTokenizer(new AlphabeticTokenizer());
			break;
		case WORD:
			NGramTokenizer tokenizer = new NGramTokenizer();
			tokenizer.setNGramMaxSize(Constants.CONFIG.getNGrams());
			filter.setTokenizer(tokenizer);
			break;
		case OPENNLP:
			// TODO use WordTokenizer with a specific delimiter set via options
			// and printed by OpenNLP.
			break;
		case COGROO:
			// TODO use WordTokenizer with a specific delimiter set via options
			// and printed by CoGrOO.
			break;
		default:
			throw new IllegalArgumentException(Constants.CONFIG.getTokenizer() + " is not implemented.");
		}
		return filter;
	}

	private StringToWordVector parseStemmer(StringToWordVector filter) {
		switch (Constants.CONFIG.getStemmer()) {
		case NONE:
			break;
		case PORTER: {
			// Portuguese Snowball stemmer
			PTStemmer stemmer = new PTStemmer();
			stemmer.setStemmer(new SelectedTag(PTStemmer.STEMMER_PORTER, PTStemmer.TAGS_STEMMERS));
			filter.setStemmer(stemmer);
			break;
		}
		case ORENGO: {
			PTStemmer stemmer = new PTStemmer();
			stemmer.setStemmer(new SelectedTag(PTStemmer.STEMMER_ORENGO, PTStemmer.TAGS_STEMMERS));
			filter.setStemmer(stemmer);
			break;
		}
		// TODO Savoy requires PTStemmer V2, which is not currently implemented
		// on the Weka Wrapper.
		// We do have a prototype for it in V1 of the current project.
		// (see https://github.com/fracpete/ptstemmer-weka-package/issues/1)
		// case SAVOY: {
		// PTStemmer stemmer = new PTStemmer();
		// stemmer.setStemmer(new SelectedTag(PTStemmer.STEMMER_SAVOY,
		// PTStemmer.TAGS_STEMMERS));
		// filter.setStemmer(stemmer);
		// break;
		// }
		case COGROO:
			// CoGrOO is implemented during import phase.
			break;
		default:
			throw new IllegalArgumentException(Constants.CONFIG.getStemmer() + " is not implemented.");
		}
		return filter;
	}

	private NaiveBayesMultinomial parseSmoothing() {
		NaiveBayesMultinomial nbm;
		switch (Constants.CONFIG.getSmoothing()) {
		case ADD_ONE:
			nbm = new NaiveBayesMultinomial();
			break;
		case ADD_ALPHA:
			nbm = new AddAlphaNaiveBayesMultinomial();
			((AddAlphaNaiveBayesMultinomial) nbm).setAlpha(Constants.ALPHA);
			break;
		case GOOD_TURING:
			nbm = new GoodTuringNaiveBayesMultinomial();
			break;
		default:
			throw new IllegalArgumentException(Constants.CONFIG.getSmoothing() + " is not implemented.");
		}
		// NaiveBayes nbm = new NaiveBayes();
		return nbm;
	}

	public void buildClassifier() {
		LOG.debug("Building classifier...");
		try {
			model.buildClassifier(dataset);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveToFile(File file) {
		LOG.debug("Saving to file...");
		// FIXME build and serialize model!
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(file);
			out = new ObjectOutputStream(fos);
			out.writeObject(dataset);
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void loadFromFile(File file) {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(file);
			in = new ObjectInputStream(fis);
			dataset = (Instances) in.readObject();
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	private void loadInstances() {
		String filename = Constants.CONFIG.getInstanceDependentStringRepresentation() + ".bin";
		File file = new File(filename);
		try {
			LOG.debug(file.getCanonicalFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!Constants.CACHE || !file.exists()) {
			LOG.debug("Will import dataset.");
			// FIXME do not depend on patient iterator. maybe receive some
			// superclass for both iterator or file.
			PatientIterator iter = new PatientIterator();
			importDataset(iter);
			saveToFile(file);
			return;
		}
		LOG.debug("Will load from file.");
		loadFromFile(file);
	}

	/**
	 * 
	 * @param iter
	 * @return
	 */
	public Instances importDataset(Iterator iter) {
		final int numAttributes = 6;
		FastVector attributes = new FastVector(numAttributes);

		LOG.debug("Will generate classes.");
		FastVector nominalCids = new FastVector();
		List<Classifiable> icds = DAOFactory.getDAOFactory().getIcdClassDAO().list();
		Iterator<Classifiable> icdIter = icds.iterator();
		while (icdIter.hasNext()) {
			Classifiable c = icdIter.next();
			nominalCids.addElement(c.getCode());
		}
		stats.setNumClasses(icds.size());

		attributes.addElement(new Attribute("id"));
		attributes.addElement(new Attribute("year"));
		attributes.addElement(new Attribute("text", (FastVector) null));
		attributes.addElement(new Attribute("m", (FastVector) null));
		attributes.addElement(new Attribute("ec", (FastVector) null));
		attributes.addElement(new Attribute("code", nominalCids));

		Instances data = new Instances("CID", attributes, 1);

		// Detects if CoGrOO is needed and instantiates it.
		Analyzer cogroo = null;
		Document document = null;
		if (Constants.CONFIG.getSentenceDetector() == Configuration.SentenceDetectors.COGROO
				|| Constants.CONFIG.getTokenizer() == Configuration.Tokenizers.COGROO
				|| Constants.CONFIG.getStemmer() == Configuration.Stemmers.COGROO
				|| Constants.CONFIG.getChunker() == Configuration.Chunkers.COGROO) {
			ComponentFactory factory = null;
			try {
				factory = ComponentFactory.create(new FileInputStream("src/main/resources/models.xml"));
				cogroo = factory.createPipe();
				document = new DocumentImpl();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		// Map<String, String> map = new HashMap<String, String>();

		Patient p;
		List<Report> list;
		List<String> texts;
		StringBuilder sb;

		RHC rhc;

		String[] textsArray;
		String[] sentences;
		String[] tokens;
		String[] lemmas;
		String text, s, prefix;
		String code, m, ec;

		Integer id, year;

		Instance inst;

		// String[] sections;
		Map<Configuration.Sections, String> sections;

		int i = 0;
		final int total = 19560; // TODO remove hardcoded value
		long start = System.currentTimeMillis();
		while (iter.hasNext()) {
			i++;
			if (i % 100 == 0) {
				long now = System.currentTimeMillis();
				double milisecondsPerEntry = (now - start) / (double) i;
				int remain = (int) ((milisecondsPerEntry * (total - i)) / (60 * 1000));
				LOG.trace(i + "/" + total + " (" + 100 * i / total + "%) " + remain + " minutes remaining");
			}

			p = (Patient) iter.next();

			list = p.getTexts();
			int numDocs = list.size();
			stats.incNumDocs(numDocs);

			texts = new ArrayList<String>();
			for (int j = 0; j < numDocs; j++) {
				// sections = list.get(j).getTexts();
				sections = list.get(j).getZonedTexts();

				for (Map.Entry<Configuration.Sections, String> entry : sections.entrySet()) {
					// Zone selection
					if (Constants.CONFIG.getSections().contains(entry.getKey())) {
						texts.add(entry.getValue());
					}
				}
			}
			// We didn't find any valid text.
			if (texts.isEmpty()) {
				LOG.debug("Found empty texts array for RGH: " + p.getRgh());
				continue;
			}

			textsArray = new String[texts.size()];
			textsArray = texts.toArray(textsArray);

			sentences = textsArray;
			tokens = sentences;

			sb = new StringBuilder();
			for (int j = 0; j < tokens.length; j++) {
				sb.append(tokens[j]);
				sb.append(" ");
			}
			text = sb.toString();

			if (Constants.STATS)
				stats.countCharTypes(text);

			/* CoGrOO processing */
			if (cogroo != null) {

				// TODO change pipe to what we need. change dinamically
				// according to config??
				// TODO exclude opennlp sentdetect and tokenizer (?)
				document.setText(text);
				try {
					cogroo.analyze(document);
				} catch (Exception e) {
					System.out.println("Error while analyzing text: ");
					System.out.println(text);
					System.out.flush();
					e.printStackTrace();
					System.err.flush();
					continue;
				}

				sb = new StringBuilder();
				for (Sentence sentence : document.getSentences()) {
					stats.incNumSentences();

					switch (Constants.CONFIG.getChunker()) {
					case COGROO: {
						for (Chunk chunk : sentence.getChunks()) {
							prefix = "";
							for (Token token : chunk.getTokens()) {
								stats.incNumTokens();
								if (Constants.CONFIG.getStemmer() == Configuration.Stemmers.COGROO) {
									lemmas = token.getLemmas();
									s = lemmas.length > 0 ? lemmas[0] : token.getLexeme();
								} else
									s = token.getLexeme();
								sb.append(prefix);
								prefix = "_";
								sb.append(s);
							}
							sb.append(" ");
						}
						break;
					}
					case NONE: {
						for (Token token : sentence.getTokens()) {
							stats.incNumTokens();
							if (Constants.CONFIG.getStemmer() == Configuration.Stemmers.COGROO) {
								lemmas = token.getLemmas();
								s = lemmas.length > 0 ? lemmas[0] : token.getLexeme();
							} else
								s = token.getLexeme();
							sb.append(s);
							sb.append(" ");
						}
						break;
					}
					}
				}
				text = sb.toString();
			}

			// XXX We get the first one.
			rhc = p.getRhc().get(0);
			code = rhc.getIcdClass().getCode();

			if (Constants.STATS) {
				stats.incNumPatientsPerClass(code, 1);
				stats.incNumDocsPerClass(code, numDocs);
			}

			id = p.getRgh();

			year = rhc.getAnoDiagnostico();
			m = rhc.getMetastasis();
			ec = rhc.getEstadioClinico();

			// map.put(text, code);
			// }

			// Iterator<Map.Entry<String, String>> mapIter =
			// map.entrySet().iterator();
			// i = 0;
			// while (mapIter.hasNext()) {
			// i++;
			// if (i % 100 == 0)
			// LOG.trace("2nd phase: imported " + i + " instances.");
			// Map.Entry<String, String> entry = mapIter.next();

			// String text = entry.getKey();
			// String code = entry.getValue();

			inst = new Instance(numAttributes);
			inst.setValue((Attribute) attributes.elementAt(0), id);
			inst.setValue((Attribute) attributes.elementAt(1), year);
			inst.setValue((Attribute) attributes.elementAt(2), text);
			inst.setValue((Attribute) attributes.elementAt(3), m);
			inst.setValue((Attribute) attributes.elementAt(4), ec);
			inst.setValue((Attribute) attributes.elementAt(5), code);
			data.add(inst);
		}

		stats.setNumPatients(i);

		data.setClassIndex(numAttributes - 1);

		this.dataset = data;
		return data;
	}

	public void crossValidate() {
		try {
			LOG.debug("Will cross-validate.");
			eval = new ExtendedEvaluation(dataset);

			long start = System.currentTimeMillis();
			eval.crossValidateModel(model, dataset, 10, new Random(42));
			long end = System.currentTimeMillis();

			LOG.info("Cross-validate in " + ((end - start) / 1000) + " seconds.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printClassifier() {
		String filename = Constants.CONFIG.getStringRepresentation() + ".arff";
		printClassifier(new File(filename));
	}

	public void printClassifier(File file) {
		LOG.debug("Will build classifier.");
		try {
			model.buildClassifier(dataset);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		LOG.debug("Will write file");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(model.toString());
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// TODO receber corpora como parâmetros.
	// Permitir tanto ICDC-3 (treinamento = teste), como ICDC-5
	// (validação isolada em PatientIterator e carregada em importDataset()
	// separado).
	public void singleEvaluate() {
		try {
			int totalSize = dataset.numInstances();
			int testSize = (int) (totalSize * Constants.TEST_RATIO);
			int trainSize = totalSize - testSize;

			final Random rand = new Random(42);
			dataset.randomize(rand);

			Instances trainSet = new Instances(dataset, 0, trainSize);
			Instances testSet = new Instances(dataset, trainSize, testSize);

			// TODO same dataset for training and test to estimate maximum
			// (ICDC-3)
			model.buildClassifier(trainSet);
			eval = new ExtendedEvaluation(trainSet);
			eval.evaluateModel(model, testSet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void resultsAsCsv(File file, int threshold) {
		// TODO DRY singleEvaluate()
		int totalSize = dataset.numInstances();
		int testSize = (int) (totalSize * Constants.TEST_RATIO);
		int trainSize = totalSize - testSize;

		final Random rand = new Random(42);
		dataset.randomize(rand);

		Instances trainSet = new Instances(dataset, 0, trainSize);
		Instances testSet = new Instances(dataset, trainSize, testSize);

		try {
			model.buildClassifier(trainSet);

			Instances filteredTestSet = Filter.useFilter(testSet, ((FilteredClassifier) model).getFilter());

			Instance original = null;
			Instance filtered = null;

			CSVWriter writer = new CSVWriter(new FileWriter(file), ';');

			String[] header = { "raw", "filtered", "correct", "decided", "pos", "rgh", "year", "m", "ec" };
			writer.writeNext(header);

			for (int i = 0; i < testSet.numInstances(); i++) {
				original = testSet.instance(i);
				filtered = filteredTestSet.instance(i);

				double[] dist = model.distributionForInstance(original);
				double cls = model.classifyInstance(original);

				int pos = getCorrectPosition(dist, original.classValue());
				if (pos < threshold)
					continue;

				// inst.setValue((Attribute) attributes.elementAt(0), id);
				// inst.setValue((Attribute) attributes.elementAt(1), year);
				// inst.setValue((Attribute) attributes.elementAt(2), text);
				// inst.setValue((Attribute) attributes.elementAt(3), m);
				// inst.setValue((Attribute) attributes.elementAt(4), ec);
				// inst.setValue((Attribute) attributes.elementAt(5), code);

				String rawText = original.toString(2);
				String correctCode = original.toString(5);
				String filteredText = buildBagWords(filtered);
				String decidedCode = original.attribute(5).value((int) cls);
				String rgh = original.toString(0);
				String year = original.toString(1);
				String m = original.toString(3);
				String ec = original.toString(4);

				String[] entries = { rawText, filteredText, correctCode, decidedCode, String.valueOf(pos), rgh, year, m,
						ec };
				writer.writeNext(entries);
			}

			writer.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @todo move to classifier??
	 * @todo junit
	 */
	private int getCorrectPosition(double[] dist, double correct) {
		int pos = 0;
		double min = dist[(int) correct];
		for (int i = 0; i < dist.length; i++) {
			if (dist[i] > min)
				pos++;
		}
		return pos;
	}

	/**
	 * @todo move to classifier??
	 * @todo junit
	 */
	private String buildBagWords(Instance instance) {
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < instance.numAttributes(); i++) {
			for (int j = 0; j < (int) instance.value(i); j++) {
				sb.append(instance.attribute(i).name());
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	public String classify(String text) {
		// Try something like this:
		//
		// FilteredClassifier model = new FilteredClassifier();
		// model.setFilter(new StringToWordVector());
		// model.setClassifier(new NaiveBayes());
		// model.buildClassifier(trainInsts);
		// System.out.println(model);
		//
		// And then this for classifying your text:
		//
		// for (int i = 0; i < classifyInsts.numInstances(); i++) {
		// classifyInsts.instance(i).setClassMissing();
		// double cls = model.classifyInstance(classifyInsts.instance(i));
		// classifyInsts.instance(i).setClassValue(cls);
		// }
		// System.out.println(classifyInsts);

		final int numAttributes = 6;
		FastVector attributes = new FastVector(numAttributes);

		LOG.debug("Will generate classes.");
		FastVector nominalCids = new FastVector();
		List<Classifiable> icds = DAOFactory.getDAOFactory().getIcdClassDAO().list();
		Iterator<Classifiable> icdIter = icds.iterator();
		while (icdIter.hasNext()) {
			Classifiable c = icdIter.next();
			nominalCids.addElement(c.getCode());
		}

		attributes.addElement(new Attribute("id"));
		attributes.addElement(new Attribute("year"));
		attributes.addElement(new Attribute("text", (FastVector) null));
		attributes.addElement(new Attribute("m", (FastVector) null));
		attributes.addElement(new Attribute("ec", (FastVector) null));
		attributes.addElement(new Attribute("code", nominalCids));

		// XXX Weka BUG. attributes index is incorrectly initialized inside the
		// Instances constructor (reference updated).
		Instances classifyInsts = new Instances("CID", attributes, 1);

		Instance inst = new Instance(numAttributes);
		// inst.setValue((Attribute) attributes.elementAt(0), 1);
		// inst.setValue((Attribute) attributes.elementAt(1), 1);
		inst.setValue((Attribute) attributes.elementAt(2), text);
		// inst.setValue((Attribute) attributes.elementAt(3), 1);
		// inst.setValue((Attribute) attributes.elementAt(4), 1);
		// inst.setValue((Attribute) attributes.elementAt(5), "");

		// inst.setValue(2, text);

		classifyInsts.add(inst);

		classifyInsts.setClassIndex(numAttributes - 1);

		double cls = 0;
		try {
			// classifyInsts.instance(0).setClassMissing();
			cls = model.classifyInstance(classifyInsts.instance(0));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		classifyInsts.instance(0).setClassValue(cls);
		String ret = classifyInsts.instance(0).classAttribute().value((int) cls);

		return ret;
	}

}
