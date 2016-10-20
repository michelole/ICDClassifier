package br.usp.ime.icdc.logic.weka.classifiers;

import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Adds other suavization techniques to Weka's Naive Bayes Multinomial.
 * 
 * @author michel
 * 
 */
public class GoodTuringNaiveBayesMultinomial extends NaiveBayesMultinomial {

	protected int m_threshold = 2;

	/**
	 * 
	 */
	private static final long serialVersionUID = -8331109311228425776L;

	public void setThreshold(int k) {
		this.m_threshold = k;
	}

	/**
	 * Generates the classifier.
	 * 
	 * @param instances
	 *            set of instances serving as training data
	 * @throws Exception
	 *             if the classifier has not been generated successfully
	 */
	public void buildClassifier(Instances instances) throws Exception {
		// can classifier handle the data?
		getCapabilities().testWithFail(instances);

		// remove instances with missing class
		instances = new Instances(instances);
		instances.deleteWithMissingClass();

		m_headerInfo = new Instances(instances, 0);
		m_numClasses = instances.numClasses();
		m_numAttributes = instances.numAttributes();
		m_probOfWordGivenClass = new double[m_numClasses][m_numAttributes];

		// enumerate through the instances
		Instance instance;
		int classIndex;
		double numOccurences;
		double[] docsPerClass = new double[m_numClasses];
		double[] wordsPerClass = new double[m_numClasses];

		java.util.Enumeration enumInsts = instances.enumerateInstances();
		while (enumInsts.hasMoreElements()) {
			instance = (Instance) enumInsts.nextElement();
			classIndex = (int) instance.value(instance.classIndex());
			docsPerClass[classIndex] += instance.weight();

			for (int a = 0; a < instance.numValues(); a++)
				if (instance.index(a) != instance.classIndex()) {
					if (!instance.isMissing(a)) {
						numOccurences = instance.valueSparse(a)
								* instance.weight();
						if (numOccurences < 0)
							throw new Exception(
									"Numeric attribute values must all be greater or equal to zero.");
						wordsPerClass[classIndex] += numOccurences;
						m_probOfWordGivenClass[classIndex][instance.index(a)] += numOccurences;
					}
				}
		}

		/*
		 * normalising probOfWordGivenClass values and saving each value as the
		 * log of each value
		 */
		double sumOfFrequencies;
		for (int c = 0; c < m_numClasses; c++) {
			m_probOfWordGivenClass[c] = adjustFrequencies(m_probOfWordGivenClass[c]);
			sumOfFrequencies = sumOfFrequencies(m_probOfWordGivenClass[c]);
			for (int v = 0; v < m_numAttributes; v++) {
				m_probOfWordGivenClass[c][v] = Math
						.log(m_probOfWordGivenClass[c][v] / sumOfFrequencies);
				if (m_probOfWordGivenClass[c][v] != m_probOfWordGivenClass[c][v]) {// NaN
					System.out.println(c + ":" + v + "=" + m_probOfWordGivenClass[c][v]);
				}
			}
		}

		/*
		 * calculating Pr(H) NOTE: Good-Turing estimator introduced in case a
		 * class does not get mentioned in the set of training instances
		 */
		docsPerClass = adjustFrequencies(docsPerClass);
		sumOfFrequencies = sumOfFrequencies(docsPerClass);
		m_probOfClass = new double[m_numClasses];
		for (int h = 0; h < m_numClasses; h++)
			m_probOfClass[h] = (double) (docsPerClass[h]) / sumOfFrequencies;

	}

	protected double[] adjustFrequencies(double[] frequency) {
		double[] frequencyOfFrequency = new double[m_threshold + 1];
		double[] adjustedFrequency = new double[frequency.length];
		for (int h = 0; h < frequency.length; h++)
			if (frequency[h] <= m_threshold)
				frequencyOfFrequency[(int) frequency[h]] += 1;
		for (int h = 0; h < frequency.length; h++) {
			int r = (int) frequency[h];
			adjustedFrequency[h] = (r < m_threshold) ? (frequencyOfFrequency[r + 1]
					* (r + 1) / frequencyOfFrequency[r])
					: frequency[h];
		}
		return adjustedFrequency;
	}

	protected double sumOfFrequencies(double[] frequency) {
		double numDocs = 0;
		for (int h = 0; h < m_numClasses; h++)
			numDocs += frequency[h];
		return numDocs != 0 ? numDocs : Double.MIN_VALUE;
	}

}
