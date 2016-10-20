package br.usp.ime.icdc.logic.weka.evaluation;

import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * Adds other evaluations values to Weka's evaluation.
 * 
 * @author michel
 * 
 */
public class ExtendedEvaluation extends Evaluation {

	// TODO standard deviation?
	
	public ExtendedEvaluation(Instances data) throws Exception {
		super(data);
	}

	/**
	 * Calculate the F-Measure with respect to a particular class and a given
	 * beta.
	 * 
	 * @param classIndex
	 *            the index of the class to consider as "positive"
	 * @param beta
	 *            the beta-index
	 * @return the F-Measure
	 */
	public double fMeasure(int classIndex, int beta) {

		double precision = precision(classIndex);
		double recall = recall(classIndex);
		if ((precision + recall) == 0) {
			return 0;
		}
		return ((Math.pow(beta, 2) + 1) * precision * recall)
				/ (Math.pow(beta, 2) * precision + recall);
	}

	/**
	 * Calculates the weighted (by class size) F-Measure for a given beta.
	 * 
	 * @return the weighted F-Measure.
	 */
	public double weightedFMeasure(int beta) {
		double[] classCounts = new double[m_NumClasses];
		double classCountSum = 0;

		for (int i = 0; i < m_NumClasses; i++) {
			for (int j = 0; j < m_NumClasses; j++) {
				classCounts[i] += m_ConfusionMatrix[i][j];
			}
			classCountSum += classCounts[i];
		}

		double fMeasureTotal = 0;
		for (int i = 0; i < m_NumClasses; i++) {
			double temp = fMeasure(i, beta);
			fMeasureTotal += (temp * classCounts[i]);
		}

		return fMeasureTotal / classCountSum;
	}

	/**
	 * Calculates the macro averaged F-Measure for a given beta.
	 * 
	 * @return the macro averaged F-Measure.
	 */
	public double macroAveragedFMeasure(int beta) {
		double fMeasureTotal = 0;
		for (int i = 0; i < m_NumClasses; i++) {
			double temp = fMeasure(i, beta);
			fMeasureTotal += temp;
		}
		return fMeasureTotal / m_NumClasses;
	}
	
	/**
	 * Calculates the micro averaged F-Measure for a given beta.
	 * 
	 * @return the micro averaged F-Measure.
	 */
	public double microAveragedFMeasure(int beta) {
		return weightedFMeasure(beta);
	}

	/**
	 * Calculates the macro averaged precision. Macro averaging considers equal
	 * weight for all classes. The result therefore is closer to the least
	 * frequency class precision.
	 * 
	 * @return the macro averaged precision.
	 */
	public double macroAveragedPrecision() {
		double precisionTotal = 0;
		for (int i = 0; i < m_NumClasses; i++) {
			double temp = precision(i);
			precisionTotal += temp;
		}
		return precisionTotal / m_NumClasses;
	}
	
	/**
	 * Calculates the micro averaged recall.
	 * 
	 * @return the micro averaged recall.
	 */
	public double microAveragedPrecision() {
		return weightedPrecision();
	}

	/**
	 * Calculates the micro averaged recall.
	 * 
	 * @return the micro averaged recall.
	 */
	public double microAveragedRecall() {
		return weightedRecall();
	}

	/**
	 * Calculates the macro averaged recall.
	 * 
	 * @return the macro averaged recall.
	 */
	public double macroAveragedRecall() {
		return macroAveragedTruePositiveRate();
	}

	/**
	 * Calculates the macro averaged true positive rate.
	 * 
	 * @return the macro averaged true positive rate.
	 */
	public double macroAveragedTruePositiveRate() {
		double truePosTotal = 0;
		for (int i = 0; i < m_NumClasses; i++) {
			double temp = truePositiveRate(i);
			truePosTotal += temp;
		}

		return truePosTotal / m_NumClasses;
	}

	/**
	 * Calculates the accuracy.
	 * 
	 * @return the accuracy.
	 */
	public final double accuracy() {
		return m_Correct / m_WithClass;
	}

}
