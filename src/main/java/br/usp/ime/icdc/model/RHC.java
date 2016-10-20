package br.usp.ime.icdc.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import br.usp.ime.icdc.Constants;
import br.usp.ime.icdc.dao.DAOFactory;
import br.usp.ime.icdc.dao.MorphologyDAO;
import br.usp.ime.icdc.dao.PatientDAO;
import br.usp.ime.icdc.dao.RhcDAO;
import br.usp.ime.icdc.dao.TopographyDAO;
import br.usp.ime.icdc.model.icd.Classifiable;
import br.usp.ime.icdc.model.icd.Morphology;
import br.usp.ime.icdc.model.icd.Topography;

@Entity
public class RHC {
	@Id
	@GeneratedValue
	private Long id;

	// bidirectional relationship
	@ManyToOne
	@JoinColumn(name = "patient_fk")
	private Patient rgh;

	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Topography topography;

	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Morphology morphology;

	@Column
	private String estadioClinico;
	
	@Column
	private String t;

	@Column
	private String n;

	@Column
	private String m;

	@Column
	private String pt;

	@Column
	private String pn;

	@Column
	private String pm;
	
	@Column
	private Integer anoDiagnostico;
	
	@Transient
	private Classifiable icdClass;
	
	private static final Logger LOG = Logger.getLogger(RHC.class);

	public RHC() {
	}

	public void setRgh(Patient rgh) {
		this.rgh = rgh;
	}

	public RHC(Topography topography, Morphology morphology,
			String estadioClinico, String t, String n, String m, String pt, String pn, String pm, Integer anoDiagnostico) {
		super();
		this.topography = topography;
		this.morphology = morphology;
		this.estadioClinico = estadioClinico;
		this.t = t;
		this.n = n;
		this.m = m;
		this.pt = pt;
		this.pn = pn;
		this.pm = pm;
		this.anoDiagnostico = anoDiagnostico;
	}

	public Topography getTopography() {
		return topography;
	}

	public Morphology getMorphology() {
		return morphology;
	}
	
	
	public boolean isMetastasis() {
		// FIXME
		return false;
	}
	
	public Classifiable getIcdClass() {
		switch (Constants.CONFIG.getTarget()) {
		case MORPHOLOGY:
			return morphology;
		case MORPHOLOGY_GROUP:
			return morphology.getGroup();
		case TOPOGRAPHY:
			return topography;
		case TOPOGRAPHY_CATEGORY:
			return topography.getCategory();
		case TOPOGRAPHY_GROUP:
			return topography.getCategory().getGroup();			
		default:
			return null;
		}
	}
	
	public String getMetastasis() {
		return m;
	}

	public String getEstadioClinico() {
		return estadioClinico;
	}
	
	public Integer getAnoDiagnostico() {
		return anoDiagnostico;
	}

	/**
	 * 
	 * @param file
	 */
	public static void loadFromFile(File file) {
		List<String[]> myEntries = null;
		try {
			CSVReader reader = new CSVReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));
			myEntries = reader.readAll();
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DAOFactory factory = DAOFactory.getDAOFactory();
		
		/* ************************************************************************** */

		Iterator<String[]> iter = myEntries.iterator();
		iter.next();
		int size = myEntries.size();

		PatientDAO patientDao = factory.getPatientDAO();
		patientDao.beginTransaction();

		List<String[]> list = new ArrayList<String[]>();

		int i = 0;
		while (iter.hasNext()) {
			i++;
			if (i % Constants.BATCH_SIZE == 0) {
				LOG.debug(i + "/" + size + " (" + 100 * i / size
						+ "%)");
				patientDao.flushAndClear();
			}

			String[] entry = iter.next();

			Integer rgh = Integer.parseInt(entry[0]);

			Patient p = patientDao.locate(rgh);
			if (p == null) {
				p = new Patient(rgh);
				patientDao.save(p);
			}

			// TODO criar RHC aqui e salvar num Map<Integer,List<RHC>>. acelera no segundo loop.
			list.add(entry);

		}
		patientDao.commit();

		/* ************************************************************************** */

		RhcDAO rhcDao = factory.getRhcDAO();
		rhcDao.beginTransaction();

		TopographyDAO topoDao = factory.getTopographyDAO();
		MorphologyDAO morphoDao = factory.getMorphologyDAO();

		Iterator<String[]> listIter = list.iterator();

		i = 1;
		boolean error = false;
		while (listIter.hasNext()) {
			i++;
			if (i % Constants.BATCH_SIZE == 0) {
				LOG.debug(i + "/" + size + " (" + 100 * i
						/ size + "%)");
				rhcDao.flushAndClear();
			}

			String[] entry = listIter.next();

			error = false;
			
			////"rgh","topografia","morfologia","estadioclinico","pt","pn","pm"
			//rgh,topografia,morfologia,estadioclinico,t,n,m,pt,pn,pm,anodiag

			String code = new StringBuilder(entry[1].substring(1, 4)).insert(2,
					".").toString();
			Topography topography = topoDao.locate(code);
			if (topography == null) {
				LOG.error("Topography not found! Code: " + entry[1]
						+ " -> " + code);
				error = true;
			}

			code = new StringBuilder(entry[2]).insert(4, "/").toString();
			Morphology morphology = morphoDao.locate(code);
			if (morphology == null) {
				LOG.error("Morphology not found! Code: " + entry[2]
						+ " -> " + code);
				error = true;
			}

			Integer rgh = Integer.parseInt(entry[0]);

			Patient p = patientDao.locate(rgh);
			if (p == null) {
				LOG.error("Patient not found!");
				error = true;
			}
			
			Integer anoDiagnostico = Integer.parseInt(entry[10]);

			if (error != true) {
				RHC c = new RHC(topography, morphology, entry[3], entry[4],
						entry[5], entry[6], entry[7], entry[8], entry[9], anoDiagnostico);
				p.addRHC(c);
				rhcDao.save(c);
			}
		}
		rhcDao.commit();
	}
}
