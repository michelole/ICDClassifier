package br.usp.ime.icdc.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import br.usp.ime.icdc.Constants;
import br.usp.ime.icdc.model.DCE;
import br.usp.ime.icdc.model.Patient;
import br.usp.ime.icdc.model.RHC;
import br.usp.ime.icdc.model.SighReport;
import br.usp.ime.icdc.model.icd.Morphology;
import br.usp.ime.icdc.model.icd.MorphologyGroup;
import br.usp.ime.icdc.model.icd.Topography;
import br.usp.ime.icdc.model.icd.TopographyCategory;
import br.usp.ime.icdc.model.icd.TopographyGroup;

/**
 * 
 * @author Michel
 * @version 1.2
 */
public class DAOFactory {
	// TODO migrate to JPA Persistence:
	// http://docs.jboss.org/hibernate/stable/entitymanager/reference/en/html_single/
	private static final int CONNECTIONS = Constants.CONNECTIONS;

	private static final DAOFactory instance = new DAOFactory();

	public static DAOFactory getDAOFactory() {
		return instance;
	}

	private SessionFactory sessionFactory;
	private Session[] session = new Session[CONNECTIONS];

	private TopographyDAO[] topographyDAO = new TopographyDAO[CONNECTIONS];
	private TopographyCategoryDAO[] topographyCategoryDAO = new TopographyCategoryDAO[CONNECTIONS];
	private TopographyGroupDAO[] topographyGroupDAO = new TopographyGroupDAO[CONNECTIONS];

	private MorphologyDAO[] morphologyDAO = new MorphologyDAO[CONNECTIONS];
	private MorphologyGroupDAO[] morphologyGroupDAO = new MorphologyGroupDAO[CONNECTIONS];

	private DceDAO[] dceDAO = new DceDAO[CONNECTIONS];
	private SighDAO[] sighDAO = new SighDAO[CONNECTIONS];
	private RhcDAO[] rhcDAO = new RhcDAO[CONNECTIONS];
	private PatientDAO[] patientDAO = new PatientDAO[CONNECTIONS];

	private DAOFactory() {
		AnnotationConfiguration cfg = new AnnotationConfiguration();

		cfg.addAnnotatedClass(Topography.class);
		cfg.addAnnotatedClass(TopographyCategory.class);
		cfg.addAnnotatedClass(TopographyGroup.class);

		cfg.addAnnotatedClass(Morphology.class);
		cfg.addAnnotatedClass(MorphologyGroup.class);

		cfg.addAnnotatedClass(RHC.class);
		cfg.addAnnotatedClass(DCE.class);
		cfg.addAnnotatedClass(SighReport.class);
		cfg.addAnnotatedClass(Patient.class);
		cfg.configure();

		// TODO set fetch size according to Constants.BATCH_SIZE

		if (Constants.RECREATE_DB) {
			SchemaExport se = new SchemaExport(cfg);
			se.create(true, true);
		}

		this.sessionFactory = cfg.buildSessionFactory();

		for (int i = 0; i < CONNECTIONS; i++)
			session[i] = sessionFactory.openSession();

		for (int i = 0; i < CONNECTIONS; i++)
			topographyDAO[i] = new TopographyDAO(session[i]);
		for (int i = 0; i < CONNECTIONS; i++)
			topographyCategoryDAO[i] = new TopographyCategoryDAO(session[i]);
		for (int i = 0; i < CONNECTIONS; i++)
			topographyGroupDAO[i] = new TopographyGroupDAO(session[i]);

		for (int i = 0; i < CONNECTIONS; i++)
			morphologyDAO[i] = new MorphologyDAO(session[i]);
		for (int i = 0; i < CONNECTIONS; i++)
			morphologyGroupDAO[i] = new MorphologyGroupDAO(session[i]);

		for (int i = 0; i < CONNECTIONS; i++)
			dceDAO[i] = new DceDAO(session[i]);
		for (int i = 0; i < CONNECTIONS; i++)
			sighDAO[i] = new SighDAO(session[i]);
		for (int i = 0; i < CONNECTIONS; i++)
			rhcDAO[i] = new RhcDAO(session[i]);
		for (int i = 0; i < CONNECTIONS; i++)
			patientDAO[i] = new PatientDAO(session[i]);
	}

	int sess = 0;

	private int getNextSession() {
		sess++;
		if (sess >= CONNECTIONS)
			sess = 0;
		return sess;
	}

	public ClassifiableDAO getIcdClassDAO() {
		switch (Constants.CONFIG.getTarget()) {
		case MORPHOLOGY:
			return (ClassifiableDAO) getMorphologyDAO();
		case MORPHOLOGY_GROUP:
			return (ClassifiableDAO) getMorphologyGroupDAO();
		case TOPOGRAPHY:
			return (ClassifiableDAO) getTopographyDAO();
		case TOPOGRAPHY_CATEGORY:
			return (ClassifiableDAO) getTopographyCategoryDAO();
		case TOPOGRAPHY_GROUP:
			return (ClassifiableDAO) getTopographyGroupDAO();
		default:
			return null;
		}
	}

	public TopographyDAO getTopographyDAO() {
		return topographyDAO[getNextSession()];
	}

	public TopographyCategoryDAO getTopographyCategoryDAO() {
		return topographyCategoryDAO[getNextSession()];
	}

	public TopographyGroupDAO getTopographyGroupDAO() {
		return topographyGroupDAO[getNextSession()];
	}

	public MorphologyDAO getMorphologyDAO() {
		return morphologyDAO[getNextSession()];
	}

	public MorphologyGroupDAO getMorphologyGroupDAO() {
		return morphologyGroupDAO[getNextSession()];
	}

	public DceDAO getDceDAO() {
		return dceDAO[getNextSession()];
	}

	public SighDAO getSighDAO() {
		return sighDAO[getNextSession()];
	}

	public RhcDAO getRhcDAO() {
		return rhcDAO[getNextSession()];
	}

	public PatientDAO getPatientDAO() {
		return patientDAO[getNextSession()];
	}

	public void close() {
		for (Session s : session)
			s.close();
		sessionFactory.close();
	}

}
