package br.usp.ime.icdc.dao;

import java.util.List;

import org.hibernate.Session;

import br.usp.ime.icdc.model.SighReport;

public class SighDAO {

	private Session session;
	
	public SighDAO(Session session) {
		this.session = session;
	}

	public void save(SighReport p) {
		this.session.save(p);
	}

	public void delete(SighReport p) {
		this.session.delete(p);
	}
	
	public boolean exists(Long id) {
		return this.session.get(SighReport.class, id) == null ? false : true;
	}

	public SighReport load(Long id) {
		return (SighReport) this.session.load(SighReport.class, id);
	}
	
	@SuppressWarnings("unchecked")
	public List<SighReport> list() {
		return this.session.createCriteria(SighReport.class).list();
	}

	public void update(SighReport p) {
		this.session.update(p);
	}
	
	public void saveOrUpdate(SighReport p) {
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
