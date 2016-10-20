package br.usp.ime.icdc.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import br.usp.ime.icdc.model.icd.Classifiable;
import br.usp.ime.icdc.model.icd.TopographyCategory;

public class TopographyCategoryDAO implements ClassifiableDAO {
	private Session session;

	public TopographyCategoryDAO(Session session) {
		this.session = session;
	}

	public void save(TopographyCategory t) {
		this.session.save(t);
	}

	public void delete(TopographyCategory t) {
		this.session.delete(t);
	}

	public TopographyCategory load(Long id) {
		return (TopographyCategory) this.session.load(TopographyCategory.class, id);
	}

	public TopographyCategory locate(String code) {
		return (TopographyCategory) this.session.createCriteria(TopographyCategory.class)
				.add(Restrictions.eq("code", code)).uniqueResult();
	}

	public void update(TopographyCategory t) {
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
		return this.session.createCriteria(TopographyCategory.class).list();
	}
}
