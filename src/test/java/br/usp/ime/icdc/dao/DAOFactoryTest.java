package br.usp.ime.icdc.dao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DAOFactoryTest {

	private DAOFactory dao;

	@Before
	public void init() {
		dao = DAOFactory.getDAOFactory();
	}

	@Test
	public void testGetIcdClassDAO() {
		PatientDAO patient = dao.getPatientDAO();
		Integer actual = patient.count();
		
		// Dummy assertion to ensure connection is OK.
		Assert.assertTrue(actual >= 0);
	}

	@After
	public void destroy() {
		dao.close();
	}
}
