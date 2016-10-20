package br.usp.ime.icdc.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import br.usp.ime.icdc.model.icd.Classifiable;
import br.usp.ime.icdc.model.icd.TopographyGroup;

public class TopographyGroupDAO implements ClassifiableDAO {
	private Session session;

	public TopographyGroupDAO(Session session) {
		this.session = session;
	}

	public void save(TopographyGroup t) {
		this.session.save(t);
	}

	public void delete(TopographyGroup t) {
		this.session.delete(t);
	}

	public TopographyGroup load(Long id) {
		return (TopographyGroup) this.session.load(TopographyGroup.class, id);
	}

	public TopographyGroup locate(String category) {
		if (category.startsWith("C") || category.startsWith("c"))
			category = category.substring(1);
		Integer search = Integer.parseInt(category);

		return (TopographyGroup) this.session
				.createCriteria(TopographyGroup.class)
				.add(Restrictions.le("startCode", search))
				.add(Restrictions.ge("endCode", search)).uniqueResult();
	}
	
	public TopographyGroup locateCode(String code) {
		return (TopographyGroup) this.session
				.createCriteria(TopographyGroup.class)
				.add(Restrictions.eq("code", code)).uniqueResult();
	}

	public void update(TopographyGroup t) {
		this.session.update(t);
	}

	public void beginTransaction() {
		this.session.beginTransaction();
	}

	public void commit() {
		this.session.getTransaction().commit();
	}

	public void flushAndClear() {
		this.session.flush();
		this.session.clear();
	}
	
	@SuppressWarnings("unchecked")
	public List<Classifiable> list() {
		return this.session.createCriteria(TopographyGroup.class).list();
	}
}
