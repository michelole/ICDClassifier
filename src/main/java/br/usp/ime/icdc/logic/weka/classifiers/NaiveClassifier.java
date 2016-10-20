package br.usp.ime.icdc.logic.weka.classifiers;

import org.apache.log4j.Logger;

import weka.core.Instance;
import weka.core.Instances;

public class NaiveClassifier extends weka.classifiers.Classifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4729890351185372478L;

	private static final Logger LOG = Logger.getLogger(NaiveClassifier.class);

	/** number of class values */
	protected int m_numClasses;

	private int mostFrequentIndex;


	@Override
	public void buildClassifier(Instances data) throws Exception {
		m_numClasses = data.numClasses();

		Instance instance;
		int classIndex;
		double[] docsPerClass = new double[m_numClasses];

		java.util.Enumeration enumInsts = data.enumerateInstances();
		while (enumInsts.hasMoreElements()) {
			instance = (Instance) enumInsts.nextElement();
			classIndex = (int) instance.value(instance.classIndex());
			docsPerClass[classIndex] += instance.weight();
		}

		mostFrequentIndex = 0;
		for (int h = 0; h < m_numClasses; h++)
			if (docsPerClass[h] > docsPerClass[mostFrequentIndex])
				mostFrequentIndex = h;
	}

	@Override
	public double classifyInstance(Instance instance) throws Exception {
		return (double) mostFrequentIndex;
	}
}
