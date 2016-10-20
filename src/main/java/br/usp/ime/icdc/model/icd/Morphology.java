package br.usp.ime.icdc.model.icd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Index;

import au.com.bytecode.opencsv.CSVReader;
import br.usp.ime.icdc.dao.DAOFactory;
import br.usp.ime.icdc.dao.MorphologyDAO;
import br.usp.ime.icdc.dao.MorphologyGroupDAO;

@Entity
public class Morphology implements Classifiable {
	@Id
	@GeneratedValue
	private Long id;

	@Column
	@Index(name = "code_ndx")
	private String code;

	@Column
	private String description;

	// bidirectional relationship
	@ManyToOne
	@JoinColumn(name = "group_fk")
	private MorphologyGroup group;

	public Morphology() {

	}

	public Morphology(String code, String description) {
		this.code = code;
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public MorphologyGroup getGroup() {
		return group;
	}

	public void setGroup(MorphologyGroup group) {
		this.group = group;
	}

	public static void loadFromFile(File file) {
		// TODO use reflection to populate object fields accordingly?
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

		Iterator<String[]> iter = myEntries.iterator();
		iter.next();

		DAOFactory factory = DAOFactory.getDAOFactory();

		MorphologyDAO morphoDao = factory.getMorphologyDAO();
		MorphologyGroupDAO groupDao = factory.getMorphologyGroupDAO();

		// XXX no carregamento de morfologia, considerar apenas terceira revisão. ver se funciona com RHC.
		
		morphoDao.beginTransaction();
		while (iter.hasNext()) {
			String[] entry = iter.next();
			String code = entry[0];
			MorphologyGroup morphoGroup = groupDao.locate(code);
			if (morphoGroup == null) {
				System.err.println("Morphology group not found! Code: " + code);
				continue;
			}
			Morphology m = new Morphology(code, entry[1]);
			morphoGroup.addMorphology(m);
			morphoDao.save(m);
		}
		morphoDao.commit();
	}
}
