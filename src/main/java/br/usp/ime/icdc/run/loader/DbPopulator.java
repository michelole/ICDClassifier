package br.usp.ime.icdc.run.loader;

import java.io.File;

import org.apache.log4j.Logger;

import br.usp.ime.icdc.Constants;
import br.usp.ime.icdc.dao.DAOFactory;
import br.usp.ime.icdc.model.DCE;
import br.usp.ime.icdc.model.RHC;
import br.usp.ime.icdc.model.SighReport;
import br.usp.ime.icdc.model.icd.Morphology;
import br.usp.ime.icdc.model.icd.MorphologyGroup;
import br.usp.ime.icdc.model.icd.Topography;
import br.usp.ime.icdc.model.icd.TopographyCategory;
import br.usp.ime.icdc.model.icd.TopographyGroup;

public class DbPopulator {

	private static final Logger LOG = Logger.getLogger(DbPopulator.class);

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String refDir = Constants.REF_DIR;
		String dataDir = Constants.DATA_DIR;
		
		long before = 0, after = 0;

		/* ************************************************ */

		LOG.info("Will load ICD now.");
		before = System.currentTimeMillis();
		
		TopographyGroup.loadFromFile(new File(refDir
				+ "cido_topografia_grupos.csv"));
		TopographyCategory.loadFromFile(new File(refDir
				+ "cido_topografia_categorias.csv"));
		Topography.loadFromFile(new File(refDir
				+ "cido_topografia.csv"));

		MorphologyGroup.loadFromFile(new File(refDir
				+ "cido_morfologia_grupos.csv"));
		Morphology.loadFromFile(new File(refDir
				+ "cido_morfologia.csv"));
		after = System.currentTimeMillis();
		
		LOG.info("ICD loaded in " + (after - before) / 1000 + " seconds");

		/* ************************************************ */
		
		LOG.info("Will load SIGH now.");
		before = System.currentTimeMillis();

		SighReport.loadFromFile(new File(dataDir + "sigh.xml"));
		after = System.currentTimeMillis();
		LOG.info("SIGH loaded in " + (after - before) / 1000 + " seconds");
		
		/* ************************************************ */

		LOG.info("Will load DCE now.");
		before = System.currentTimeMillis();

		DCE.loadFromDirectory(new File(dataDir + "dce_rgh.txt"),
				new File(dataDir + "dce"));
		after = System.currentTimeMillis();
		LOG.info("DCE loaded in " + (after - before) / 1000 + " seconds");

		/* ************************************************ */

		LOG.info("Will load RHC now.");
		before = System.currentTimeMillis();
		RHC.loadFromFile(new File(dataDir + "rhc.csv"));
		after = System.currentTimeMillis();
		LOG.info("Loaded RHC in " + (after - before) / 1000 / 60 + " minutes");

		/* ************************************************ */

		DAOFactory.getDAOFactory().close();
	}
}
