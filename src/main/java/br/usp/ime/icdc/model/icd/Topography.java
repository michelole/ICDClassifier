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
import br.usp.ime.icdc.dao.TopographyCategoryDAO;
import br.usp.ime.icdc.dao.TopographyDAO;

@Entity
public class Topography implements Classifiable {
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
	@JoinColumn(name="category_fk")
	private TopographyCategory category;
	
	public Topography() {
		
	}
	
	public Topography(String code, String description) {
		this.code = code;
		this.description = description;
	}
	
	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public TopographyCategory getCategory() {
		return category;
	}

	public void setCategory(TopographyCategory category) {
		this.category = category;
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

		TopographyDAO topoDao = factory.getTopographyDAO();
		TopographyCategoryDAO categoryDao = factory.getTopographyCategoryDAO();
		
		topoDao.beginTransaction();
		while (iter.hasNext()) {
			String[] entry = iter.next();
			String code = entry[0];
			
			if (code.length() == 3)
				code = "0" + code;
			
			TopographyCategory topoCategory = categoryDao.locate(code.substring(0, code.lastIndexOf('.')));
			if (topoCategory == null) {
				System.err.println("Topography category not found! Code: " + code);
				continue;
			}
			Topography t = new Topography(code, entry[1]);
			topoCategory.addTopography(t);
			topoDao.save(t);
		}
		topoDao.commit();
	}
}
