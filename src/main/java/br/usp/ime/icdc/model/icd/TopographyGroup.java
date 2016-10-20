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
import br.usp.ime.icdc.dao.TopographyGroupDAO;

@Entity
public class TopographyGroup implements Classifiable {
	@Id
	@GeneratedValue
	private Long id;

	@Column
	@Index(name = "code_ndx")
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
	private List<TopographyCategory> category;
	
	public TopographyGroup() {

	}
	
	public TopographyGroup(String code, String description, Integer from, Integer to) {
		this.code = code;
		this.description = description;
		this.startCode = from;
		this.endCode = to;
	}
	
	public void addTopographyCategory(TopographyCategory tc) {
		tc.setGroup(this);
		if (this.category == null)
			this.category = new ArrayList<TopographyCategory>();
		this.category.add(tc);
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

		TopographyGroupDAO groupDao = factory.getTopographyGroupDAO();
		
		groupDao.beginTransaction();
		while (iter.hasNext()) {
			String[] entry = iter.next();
			String code = entry[0];
			
			int hyphenIndex = code.lastIndexOf('-');
			Integer from = null;
			Integer to = Integer.parseInt(code.substring(hyphenIndex+2));
			if (hyphenIndex != -1)
				from = Integer.parseInt(code.substring(1, hyphenIndex));
			else
				from = to;
			
			TopographyGroup t = new TopographyGroup(code, entry[1], from, to);
			groupDao.save(t);
		}
		groupDao.commit();
	}

	@Override
	public String toString() {
		return "TopographyGroup [id=" + id + ", code=" + code
				+ ", description=" + description + ", startCode=" + startCode
				+ ", endCode=" + endCode + "]";
	}
}
