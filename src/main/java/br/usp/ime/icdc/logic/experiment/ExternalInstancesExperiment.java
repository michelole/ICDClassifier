package br.usp.ime.icdc.logic.experiment;

import weka.core.Instances;
import weka.experiment.Experiment;

public class ExternalInstancesExperiment extends Experiment {
	
	public void setCurrentInstances(Instances instances) {
		this.m_CurrentInstances = instances;
		this.m_ResultProducer.setInstances(m_CurrentInstances);
	}

}
