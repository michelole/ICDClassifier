package br.usp.ime.icdc.model;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Index;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import br.usp.ime.icdc.Configuration;
import br.usp.ime.icdc.Constants;
import br.usp.ime.icdc.dao.DAOFactory;
import br.usp.ime.icdc.dao.PatientDAO;
import br.usp.ime.icdc.dao.SighDAO;

@Entity
public class SighReport implements Report {
	@Id
	@GeneratedValue
	private Long id;

	@Column
	@Index(name = "internalid_ndx")
	private String internalId;

	@Column
	private Calendar creationDate;

	@Column(length = 100000)
	private String macroscopy;

	@Column(length = 100000)
	private String microscopy;

	@Column(length = 100000)
	private String conclusion;

	// bidirectional relationship
	@ManyToOne
	@JoinColumn(name = "patient_fk")
	private Patient rgh;

	private static final Logger LOG = Logger.getLogger(SighReport.class);

	public SighReport() {
	}

	public SighReport(String internalId, Calendar creationDate, String macroscopy, String microscopy,
			String conclusion) {
		this.internalId = internalId;
		this.creationDate = creationDate;
		this.macroscopy = macroscopy;
		this.microscopy = microscopy;
		this.conclusion = conclusion;
	}

	public void setRgh(Patient rgh) {
		this.rgh = rgh;
	}

	public Long getId() {
		return id;
	}

	public String getInternalId() {
		return internalId;
	}

	public String[] getTexts() {
		return new String[] { macroscopy, microscopy, conclusion };
	}

	public Map<Configuration.Sections, String> getZonedTexts() {
		Map<Configuration.Sections, String> ret = new HashMap<Configuration.Sections, String>();
		// TODO might be null
		if (!macroscopy.isEmpty())
			ret.put(Configuration.Sections.MACROSCOPY, macroscopy);
		if (!microscopy.isEmpty())
			ret.put(Configuration.Sections.MICROSCOPY, microscopy);
		if (!conclusion.isEmpty())
			ret.put(Configuration.Sections.CONCLUSION, conclusion);
		return ret;
	}

	public String getText() {
		return macroscopy + System.getProperty("line.separator") + microscopy + System.getProperty("line.separator")
				+ conclusion;
	}

	@Override
	public String toString() {
		return "SighReport [internalId=" + internalId + ", creationDate=" + creationDate + ", macroscopy=" + macroscopy
				+ ", microscopy=" + microscopy + ", conclusion=" + conclusion + ", rgh=" + rgh + "]";
	}

	public static void loadFromFile(File file) {

		final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");

		org.jdom.Document d = null;
		try {
			d = new SAXBuilder().build(file);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LOG.debug("File loaded.");

		if (d == null) {
			LOG.fatal("Could not load document from file " + file.getName());
			return;
		}

		DAOFactory factory = DAOFactory.getDAOFactory();

		/*
		 * *********************************************************************
		 * *****
		 */

		PatientDAO patientDao = factory.getPatientDAO();
		patientDao.beginTransaction();

		Map<Integer, List<SighReport>> map = new HashMap<Integer, List<SighReport>>();

		List list = d.getRootElement().getChildren();
		Iterator iter = list.iterator();

		int i = 0;
		while (iter.hasNext()) {
			i++;
			if (i % Constants.BATCH_SIZE == 0) {
				LOG.debug(i);
				patientDao.flushAndClear();
			}
			org.jdom.Element e = (org.jdom.Element) iter.next();
			String internallId = e.getChild("IDENTIFICADOR").getText();
			String macro = e.getChild("MACROSCOPIA").getText();
			String micro = e.getChild("MICROSCOPIA").getText();
			String conclusion = e.getChild("CONCLUSAO").getText();
			Integer rgh = new Integer(e.getChild("RGH").getText());

			Calendar creationDate = Calendar.getInstance();
			creationDate.clear();
			try {
				creationDate.setTime(DATE_FORMAT.parse(e.getChild("DATA_CRIADO").getText()));
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			SighReport r = new SighReport(internallId, creationDate, macro, micro, conclusion);

			Patient p = patientDao.locate(rgh);
			if (p == null) {
				p = new Patient(rgh);
				patientDao.save(p);
			}

			List<SighReport> value = null;
			if (map.containsKey(rgh)) {
				value = map.remove(rgh);
			} else {
				value = new ArrayList<SighReport>();
			}
			value.add(r);
			map.put(rgh, value);

		}
		patientDao.commit();

		int size = i;

		/*
		 * *********************************************************************
		 * *****
		 */

		SighDAO sighDao = factory.getSighDAO();
		sighDao.beginTransaction();

		Iterator<Map.Entry<Integer, List<SighReport>>> mapIter = map.entrySet().iterator();

		i = 1;
		while (mapIter.hasNext()) {
			if (i % Constants.BATCH_SIZE == 0) {
				LOG.debug(i + "/" + size + " (" + 100 * i / size + "%)");
				sighDao.flushAndClear();
			}
			Map.Entry<Integer, List<SighReport>> entry = mapIter.next();

			Integer rgh = entry.getKey();
			List<SighReport> value = entry.getValue();

			Patient p = patientDao.locate(rgh);
			if (p == null) {
				LOG.fatal("Patient not found where it was supposed to exist.");
				continue;
			}

			for (SighReport r : value) {
				p.addSighReport(r);
				sighDao.save(r);
				i++;
			}

		}

		sighDao.commit();

	}

}
