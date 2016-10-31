package br.usp.ime.icdc.run;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import br.usp.ime.icdc.Configuration;
import br.usp.ime.icdc.Configuration.Chunkers;
import br.usp.ime.icdc.Configuration.Classifiers;
import br.usp.ime.icdc.Configuration.Criteria;
import br.usp.ime.icdc.Configuration.MetastasisStatus;
import br.usp.ime.icdc.Configuration.Sections;
import br.usp.ime.icdc.Configuration.SentenceDetectors;
import br.usp.ime.icdc.Configuration.SmoothingTechniques;
import br.usp.ime.icdc.Configuration.Sources;
import br.usp.ime.icdc.Configuration.Stemmers;
import br.usp.ime.icdc.Configuration.Targets;
import br.usp.ime.icdc.Configuration.Tokenizers;
import br.usp.ime.icdc.Configuration.WeightingSchemes;
import br.usp.ime.icdc.Constants;
import br.usp.ime.icdc.dao.DAOFactory;
import br.usp.ime.icdc.dao.TopographyGroupDAO;
import br.usp.ime.icdc.logic.classifier.CipeClassifier;

public class ClassifierFacade {
	
	private static final Logger LOG = Logger.getLogger(CipeClassifier.class);

	public static void main(String[] args) throws Exception {
		Set<Sections> sections = new HashSet<Sections>();
		sections.add(Sections.MACROSCOPY);
		
		// FIXME do we really need all the configs just to run?
		Constants.CONFIG = new Configuration(sections, Criteria.MANY_REPORT_ONE_REGISTRY,
				Classifiers.BAYES, WeightingSchemes.TFIDF, SmoothingTechniques.ADD_ONE, 1, Sources.ALL,
				false, SentenceDetectors.NONE, Tokenizers.WORD, Stemmers.NONE,
				Chunkers.NONE, Targets.TOPOGRAPHY_GROUP, 1, -1,
				MetastasisStatus.NONM1);

		TopographyGroupDAO dao = (TopographyGroupDAO) DAOFactory.getDAOFactory().getIcdClassDAO();
		
		CipeClassifier c = new CipeClassifier();
		c.buildClassifier();
		

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			LOG.info("Entre com os laudos do paciente seguido de END:");
			StringBuilder sb = new StringBuilder();
			while (true) {
				String line = br.readLine();
				if (!line.equals("END")) {
					sb.append(line);
					sb.append("\n");
				}
				else break;
			};

			LOG.info("Classificando...");
			String code = c.classify(sb.toString());
			String description = dao.locateCode(code).getDescription();
			LOG.info(code + " = " + description);
		}

	}
}
