package br.usp.ime.icdc.dao;

import org.hibernate.Session;

import br.usp.ime.icdc.model.RHC;

public class RhcDAO {
	private Session session;

	public RhcDAO(Session session) {
		this.session = session;
	}

	public void save(RHC p) {
		this.session.save(p);
	}

	public void delete(RHC p) {
		this.session.delete(p);
	}

	public RHC load(Long id) {
		return (RHC) this.session.load(RHC.class, id);
	}

	public void update(RHC p) {
		this.session.update(p);
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
