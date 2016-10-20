package br.usp.ime.icdc.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Index;

import br.usp.ime.icdc.Constants;
import br.usp.ime.icdc.dao.DAOFactory;
import br.usp.ime.icdc.dao.PatientDAO;
import br.usp.ime.icdc.run.loader.DCELoader;

@Entity
public class DCE implements Report {
	@Id
	@GeneratedValue
	private Long id;

	@Column
	@Index(name = "internalid_ndx")
	private Integer hospitalId;

	// Longest known (939586) contains 21448 characters.
	@Column(length = 30000)
	private String text;

	// bidirectional relationship
	@ManyToOne
	@JoinColumn(name="patient_fk")
	private Patient rgh;

	public DCE() {

	}

	public void setRgh(Patient rgh) {
		this.rgh = rgh;
	}

	public DCE(Integer hospitalId, String text) {
		this.hospitalId = hospitalId;
		this.text = text;
	}

	public Long getId() {
		return id;
	}

	public Integer getHospitalId() {
		return hospitalId;
	}

	public String getText() {
		return text;
	}
	
	public String[] getTexts() {
		return new String[]{text};
	}

	public Patient getRgh() {
		return rgh;
	}
	
	private static final Logger LOG = Logger.getLogger(DCE.class);
	
	public static final int THREADS = Constants.THREADS;
	private static final Pattern COMMA = Pattern.compile(",");
	
	/**
	 * 
	 * @param index
	 * @param directory
	 */
	public static void loadFromDirectory(File index, File directory) {
		if (directory == null || index == null || !directory.isDirectory()
				|| !index.isFile() || !index.canRead()) {
			System.err.println("Error loading DCE.");
			return;
		}

		Map<Integer, Integer>[] map = new HashMap[THREADS];
		for (int i = 0; i < THREADS; i++)
			map[i] = new HashMap<Integer, Integer>();

		/* ************************************************************************** */
		
		PatientDAO patientDao = DAOFactory.getDAOFactory().getPatientDAO();
		patientDao.beginTransaction();
		
		try {
			BufferedReader indexReader = new BufferedReader(new FileReader(
					index));
			int i = 0, k = 1;
			indexReader.readLine(); // Header
			for (String entry = indexReader.readLine(); entry != null
					&& !entry.isEmpty(); entry = indexReader.readLine()) {
				k++;
				if (k % Constants.BATCH_SIZE == 0) {
					LOG.debug(k);
					patientDao.flushAndClear();
				}

				String[] cells = COMMA.split(entry);
				if (cells.length != 2)
					continue;

				Integer laudoId = Integer.parseInt(cells[0]);
				Integer rgh = Integer.parseInt(cells[1]);
				
				Patient p = patientDao.locate(rgh);
				if (p == null) {
					p = new Patient(rgh);
					patientDao.save(p);
				}		
				
				map[i].put(laudoId, rgh);
				i = (i+1)%THREADS;
			}
			indexReader.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		patientDao.commit();
		
		DCELoader[] dceLoader = new DCELoader[THREADS];
		for (int i = 0; i < THREADS; i++) {
			dceLoader[i] = new DCELoader(map[i], directory);
			dceLoader[i].start();
		}
		
		for (int i = 0; i < THREADS; i++) {
			try {
				dceLoader[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

}
