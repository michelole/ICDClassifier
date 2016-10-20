package br.usp.ime.icdc.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import br.usp.ime.icdc.model.icd.Classifiable;
import br.usp.ime.icdc.model.icd.MorphologyGroup;

public class MorphologyGroupDAO implements ClassifiableDAO {
	private Session session;

	public MorphologyGroupDAO(Session session) {
		this.session = session;
	}

	public void save(MorphologyGroup m) {
		this.session.save(m);
	}

	public void delete(MorphologyGroup m) {
		this.session.delete(m);
	}

	public MorphologyGroup load(Long id) {
		return (MorphologyGroup) this.session.load(MorphologyGroup.class, id);
	}

	public MorphologyGroup locate(String morphology) {
		int slashIndex = morphology.lastIndexOf('/');
		if (slashIndex != -1) {
			morphology = morphology.substring(0, slashIndex);
		}
		Integer search = Integer.parseInt(morphology);

		return (MorphologyGroup) this.session
				.createCriteria(MorphologyGroup.class)
				.add(Restrictions.le("startCode", search))
				.add(Restrictions.ge("endCode", search)).uniqueResult();
	}
	
	public MorphologyGroup locateCode(String code) {
		return (MorphologyGroup) this.session
				.createCriteria(MorphologyGroup.class)
				.add(Restrictions.eq("code", code)).uniqueResult();
	}

	public void update(MorphologyGroup m) {
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
		return this.session.createCriteria(MorphologyGroup.class).list();
	}
}
