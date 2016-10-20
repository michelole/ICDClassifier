package br.usp.ime.icdc.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import br.usp.ime.icdc.model.DCE;

public class DceDAO {

	private Session session;
	
	public DceDAO(Session session) {
		this.session = session;
	}

	public void save(DCE p) {
		this.session.save(p);
	}

	public void delete(DCE p) {
		this.session.delete(p);
	}
	
	public boolean exists(Long id) {
		return this.session.get(DCE.class, id) == null ? false : true;
	}
	
	@SuppressWarnings("unchecked")
	public List<DCE> list() {
		return this.session.createCriteria(DCE.class).list();
	}
	
	public DCE locate(Integer hospitalId) {
		return (DCE) this.session.createCriteria(DCE.class)
				.add(Restrictions.eq("hospitalId", hospitalId)).uniqueResult();
	}

	public DCE load(Long id) {
		return (DCE) this.session.load(DCE.class, id);
	}

	public void update(DCE p) {
		this.session.update(p);
	}
	
	public void saveOrUpdate(DCE p) {
		this.session.saveOrUpdate(p);
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
	
}
