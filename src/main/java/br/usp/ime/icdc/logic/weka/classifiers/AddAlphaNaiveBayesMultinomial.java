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
public class AddAlphaNaiveBayesMultinomial extends NaiveBayesMultinomial {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8331109311218425776L;

	protected double alpha = 1.0d;

	public void setAlpha(double alpha) {
		// TODO check != 0
		this.alpha = alpha;
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
		m_probOfWordGivenClass = new double[m_numClasses][];

		/*
		 * initialising the matrix of word counts NOTE: Add-alpha estimator
		 * introduced in case a word that does not appear for a class in the
		 * training set does so for the test set
		 */
		for (int c = 0; c < m_numClasses; c++) {
			m_probOfWordGivenClass[c] = new double[m_numAttributes];
			for (int att = 0; att < m_numAttributes; att++) {
				m_probOfWordGivenClass[c][att] = alpha;
			}
		}

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
		// TODO why the decrement of 1 on m_numAttributes?
		for (int c = 0; c < m_numClasses; c++)
			for (int v = 0; v < m_numAttributes; v++)
				m_probOfWordGivenClass[c][v] = Math
						.log(m_probOfWordGivenClass[c][v]
								/ (wordsPerClass[c] + alpha
										* (m_numAttributes - 1)));

		/*
		 * calculating Pr(H) NOTE: Add-alpha estimator introduced in case a
		 * class does not get mentioned in the set of training instances
		 */
		final double numDocs = instances.sumOfWeights() + alpha * m_numClasses;
		m_probOfClass = new double[m_numClasses];
		for (int h = 0; h < m_numClasses; h++)
			m_probOfClass[h] = (double) (docsPerClass[h] + alpha) / numDocs;
	}
}
