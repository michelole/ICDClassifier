package br.usp.ime.icdc.model;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import br.usp.ime.icdc.Configuration.MetastasisStatus;
import br.usp.ime.icdc.Constants;
import br.usp.ime.icdc.dao.DAOFactory;
import br.usp.ime.icdc.dao.PatientDAO;

/**
 * An iterator over patients. Access database and returns only patients that
 * match some criteria.
 * 
 * @author Michel
 * @version 1.0
 */
public class PatientIterator implements Iterator<Patient> {

	private static final Logger LOG = Logger.getLogger(PatientIterator.class);

	private List<Patient> patientList;
	private int currentPosition = -1;
	private int nextPosition = -1;

	public PatientIterator() {
		final PatientDAO patientDAO = DAOFactory.getDAOFactory()
				.getPatientDAO();
		this.patientList = patientDAO.list();
		this.nextPosition = findNextPosition();
	}

	private int findNextPosition() {
		Patient p;
		boolean accept;
		for (int i = currentPosition + 1; i < patientList.size(); i++) {
			// TODO define at global location
			// ICDC-5 we isolate 10% of patients for validation.
			if (i % 10 == 0)
				continue;
			p = patientList.get(i);
			
			accept = false;
			switch (Constants.CONFIG.getCriteria()) {
			case ONE_REPORT_ONE_REGISTRY:
				accept = p.getTexts() != null && p.getRhc() != null
						&& p.getTexts().size() == 1
						&& p.getDistinctRhc().size() == 1;
				if (accept && Constants.CONFIG.getPatientYear() != -1)
					accept &= p.getDistinctRhc().get(0).getAnoDiagnostico() == Constants.CONFIG
							.getPatientYear();
				if (accept
						&& Constants.CONFIG.getMetastasisStatus() == MetastasisStatus.NONM1)
					accept &= !p.getDistinctRhc().get(0).getMetastasis()
							.equals("1");
				break;
			case MANY_REPORT_ONE_REGISTRY:
				accept = p.getTexts() != null
						&& p.getRhc() != null
						&& p.getTexts().size() >= Constants.CONFIG
								.getMinReports()
						&& p.getDistinctRhc().size() == 1;
				if (accept && Constants.CONFIG.getPatientYear() != -1)
					accept &= p.getDistinctRhc().get(0).getAnoDiagnostico() == Constants.CONFIG
							.getPatientYear();
				if (accept
						&& Constants.CONFIG.getMetastasisStatus() == MetastasisStatus.NONM1)
					accept &= !p.getDistinctRhc().get(0).getMetastasis()
							.equals("1");
				break;
			case MANY_REPORT_MANY_REGISTRY:
				// FIXME Does not filter by year.
				// FIXME Does not filter out metastasis.
				accept = p.getTexts() != null && p.getRhc() != null
						&& p.getTexts().size() >= 1
						&& p.getDistinctRhc().size() >= 1;
				break;

			default:
				LOG.fatal("No criterion defined on Constants class.");
				break;
			}
			if (accept)
				return i;
		}

		return -1;
	}

	public boolean hasNext() {
		return this.nextPosition > this.currentPosition ? true : false;
	}

	public Patient next() {
		if (this.nextPosition <= this.currentPosition)
			return null;
		this.currentPosition = this.nextPosition;
		this.nextPosition = findNextPosition();

		Patient p = patientList.get(this.currentPosition);
		// Instance carrier = new Instance(p.getDce().get(0).getText(),
		// p.getRhc()
		// .get(0).getTopography().getCode(), p.getRgh().toString(), null);
		// return carrier;
		return p;

	}

	public void remove() {
		throw new IllegalStateException(
				"This Iterator<Instance> does not support remove().");

	}

}
