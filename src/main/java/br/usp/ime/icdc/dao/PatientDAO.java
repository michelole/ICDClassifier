package br.usp.ime.icdc.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import br.usp.ime.icdc.model.Patient;

public class PatientDAO {
	private Session session;

	// public static final String USER_ENTITY = Patient.class.getName();

	public PatientDAO(Session session) {
		this.session = session;
	}

	public void save(Patient p) {
		this.session.save(p);
	}

	public void delete(Patient p) {
		this.session.delete(p);
	}

	public Patient load(Long id) {
		return (Patient) this.session.load(Patient.class, id);
	}

	public Patient locate(Integer rgh) {
		return (Patient) this.session.createCriteria(Patient.class)
				.add(Restrictions.eq("rgh", rgh)).uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<Patient> list() {
		return this.session.createCriteria(Patient.class).addOrder(Order.asc("rgh")).list();
	}

	public void update(Patient p) {
		this.session.update(p);
	}
	
	public void saveOrUpdate(Patient p) {
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
	
	public void merge(Patient p) {
		this.session.merge(p);
	}
}
