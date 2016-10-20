package br.usp.ime.icdc.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import br.usp.ime.icdc.model.icd.Classifiable;
import br.usp.ime.icdc.model.icd.Morphology;

public class MorphologyDAO implements ClassifiableDAO {
	private Session session;

	public MorphologyDAO(Session session) {
		this.session = session;
	}

	public void save(Morphology m) {
		this.session.save(m);
	}

	public void delete(Morphology m) {
		this.session.delete(m);
	}

	public Morphology load(Long id) {
		return (Morphology) this.session.load(Morphology.class, id);
	}

	public Morphology locate(String code) {
		return (Morphology) this.session.createCriteria(Morphology.class)
				.add(Restrictions.eq("code", code)).uniqueResult();
	}

	public void update(Morphology m) {
		this.session.update(m);
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
		return this.session.createCriteria(Morphology.class).list();
	}
}
