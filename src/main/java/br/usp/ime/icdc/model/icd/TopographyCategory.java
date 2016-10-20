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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Index;

import au.com.bytecode.opencsv.CSVReader;
import br.usp.ime.icdc.dao.DAOFactory;
import br.usp.ime.icdc.dao.TopographyCategoryDAO;
import br.usp.ime.icdc.dao.TopographyGroupDAO;

@Entity
public class TopographyCategory implements Classifiable {
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
	private TopographyGroup group;

	// bidirectional relationship
	@OneToMany(mappedBy = "category")
	private List<Topography> topography;

	public TopographyCategory() {

	}

	public TopographyCategory(String code, String description) {
		this.code = code;
		this.description = description;
	}

	public void addTopography(Topography t) {
		t.setCategory(this);
		if (this.topography == null)
			this.topography = new ArrayList<Topography>();
		this.topography.add(t);
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public TopographyGroup getGroup() {
		return group;
	}

	public void setGroup(TopographyGroup group) {
		this.group = group;
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

		TopographyCategoryDAO categoryDao = factory.getTopographyCategoryDAO();
		TopographyGroupDAO groupDao = factory.getTopographyGroupDAO();

		categoryDao.beginTransaction();
		while (iter.hasNext()) {
			String[] entry = iter.next();
			String code = entry[0];
			TopographyGroup topoGroup = groupDao.locate(code);
			if (topoGroup == null) {
				System.err.println("Topography group not found! Code: " + code);
				continue;
			}
			TopographyCategory tc = new TopographyCategory(code, entry[1]);
			topoGroup.addTopographyCategory(tc);
			categoryDao.save(tc);
		}
		categoryDao.commit();
	}
}
