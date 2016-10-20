package br.usp.ime.icdc.model.icd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Index;

import au.com.bytecode.opencsv.CSVReader;
import br.usp.ime.icdc.dao.DAOFactory;
import br.usp.ime.icdc.dao.MorphologyGroupDAO;

@Entity
public class MorphologyGroup implements Classifiable {
	@Id
	@GeneratedValue
	private Long id;

	@Column
	private String code;

	@Column
	private String description;
	
	@Column
	@Index(name = "startcode_ndx")
	private Integer startCode;
	
	@Column
	@Index(name = "endcode_ndx")
	private Integer endCode;
	
	// bidirectional relationship
	@OneToMany(mappedBy="group")
	private List<Morphology> morphology;
	
	public MorphologyGroup() {
		
	}
	
	public MorphologyGroup(String code, String description, Integer from, Integer to) {
		this.code = code;
		this.description = description;
		this.startCode = from;
		this.endCode = to;
	}
	
	public void addMorphology(Morphology m) {
		m.setGroup(this);
		if (this.morphology == null)
			this.morphology = new ArrayList<Morphology>();
		this.morphology.add(m);
	}
	
	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

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

		Iterator<String[]> iter = myEntries.iterator();
		iter.next();
		
		DAOFactory factory = DAOFactory.getDAOFactory();

		MorphologyGroupDAO groupDao = factory.getMorphologyGroupDAO();
		
		groupDao.beginTransaction();
		while (iter.hasNext()) {
			String[] entry = iter.next();
			String code = entry[0];
			
			int hyphenIndex = code.lastIndexOf('-');
			Integer from = null;
			String toStr = code.substring(hyphenIndex+1);
			Integer to = Integer.parseInt(toStr + "9");
			if (hyphenIndex != -1)
				from = Integer.parseInt(code.substring(0, hyphenIndex) + "0");
			else
				from = Integer.parseInt(toStr + "0");
			
			MorphologyGroup m = new MorphologyGroup(code, entry[1], from, to);
			groupDao.save(m);
		}
		groupDao.commit();
	}
}
