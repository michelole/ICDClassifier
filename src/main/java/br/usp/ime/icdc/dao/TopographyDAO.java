package br.usp.ime.icdc.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import br.usp.ime.icdc.model.icd.Classifiable;
import br.usp.ime.icdc.model.icd.Topography;

public class TopographyDAO implements ClassifiableDAO {
	private Session session;

	public TopographyDAO(Session session) {
		this.session = session;
	}

	public void save(Topography t) {
		this.session.save(t);
	}

	public void delete(Topography t) {
		this.session.delete(t);
	}

	public Topography load(Long id) {
		return (Topography) this.session.load(Topography.class, id);
	}

	public Topography locate(String code) {
		return (Topography) this.session.createCriteria(Topography.class)
				.add(Restrictions.eq("code", code)).uniqueResult();
	}

	public void update(Topography t) {
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
		return this.session.createCriteria(Topography.class).list();
	}
}
