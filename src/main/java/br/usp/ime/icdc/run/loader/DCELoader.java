package br.usp.ime.icdc.run.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import br.usp.ime.icdc.Configuration;
import br.usp.ime.icdc.Constants;
import br.usp.ime.icdc.dao.DAOFactory;
import br.usp.ime.icdc.dao.DceDAO;
import br.usp.ime.icdc.dao.PatientDAO;
import br.usp.ime.icdc.model.DCE;
import br.usp.ime.icdc.model.Patient;

public class DCELoader extends Thread {

	// Longest known valid sequence of numbers has 15 characters.
	private static final Pattern HEXA = Pattern.compile("[a-f0-9]{20,}");

	private static final String SECTION_DIVIDER = "---------";

	// Ordered by corpus frequency
	private static final Map<String, Configuration.Sections> sectionsTranslator = new HashMap<String, Configuration.Sections>();
	static {
		sectionsTranslator.put("MACROSCOPIA", Configuration.Sections.MACROSCOPY);
		sectionsTranslator.put("MICROSCOPIA", Configuration.Sections.MICROSCOPY);
		sectionsTranslator.put("EXAME CITOPATOLÓGICO CÉRVICO-VAGINAL", Configuration.Sections.CYTOPATHOLOGY);
//		sections.add("LAUDO COMPLEMENTAR DE IMUNOISTOQUÍMICA");
//		sections.add("IMUNOISTOQUÍMICA");
//		sections.add("REVISÃO DE LMINAS");	// sic
//		sections.add("EXAME CITOLÓGICO");
//		sections.add("PUNÇÃO ASPIRATIVA");
		sectionsTranslator.put("EXAME CITOPATOLÓGICO CÉRVICO-VAGINAL EM MEIO LÍQUIDO", Configuration.Sections.LIQUID_CYTOPATHOLOGY);
	}

	/**
	 * reportId => patientId
	 */
	private Map<Integer, Integer> map;

	private File dir;

	private DceDAO dceDao = DAOFactory.getDAOFactory().getDceDAO();
	private PatientDAO patientDao = DAOFactory.getDAOFactory().getPatientDAO();

	private static final Logger LOG = Logger.getLogger(DCELoader.class);

	public DCELoader(Map<Integer, Integer> map, File dir) {
		this.map = map;
		this.dir = dir;
	}

	@Override
	public void run() {
		int total = map.keySet().size();

		dceDao.beginTransaction();

		StringBuilder sb = null;
		Iterator<Map.Entry<Integer, Integer>> iter = map.entrySet().iterator();

		String line = null, oldText = null, newText = null;
		BufferedReader br = null;

		// zoneDescription => text
		Map<Configuration.Sections, String> sections = null;
		Configuration.Sections currentSection = null;

		int i = -1;
		long start = System.currentTimeMillis();

		// Runs for each file
		while (iter.hasNext()) {
			i++;
			if (i % Constants.BATCH_SIZE == 0) {
				long now = System.currentTimeMillis();
				double milisecondsPerEntry = (now - start) / (double) i;
				int remain = (int) ((milisecondsPerEntry * (total - i)) / 1000);
				LOG.debug(i + "/" + total + " (" + 100 * i / total + "%) " + remain + " seconds remaining");
				dceDao.flushAndClear();
			}

			Map.Entry<Integer, Integer> entry = iter.next();
			Integer laudoId = entry.getKey();
			Integer rgh = entry.getValue();

			File f = new File(dir, laudoId + ".txt");
			if (!f.exists() || !f.canRead()) {
				System.err.println("Cannot read file " + f.getName());
				continue;
			}

			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
				sb = new StringBuilder();
				sections = new HashMap<Configuration.Sections, String>();
				currentSection = null;

				// The first section might not be prepended by the section
				// divider.
				line = br.readLine();
				if (!line.equals("")) {
					currentSection = getNewSection(line);
					sb.append(line);
					sb.append("\n");
				}

				// Runs for each line
				for (line = br.readLine(); line != null; line = br.readLine()) {
					line = HEXA.matcher(line).replaceAll("");

					// After the section divider, we have the section header.
					if (line.equals(SECTION_DIVIDER)) {
						// currentSection == null iff the file starts with a section divider
						if (currentSection != null) {
							newText = sb.toString();
							oldText = "";
							// Updates the current section with the new value.
							// Should not happen that often, if ever.
							if (sections.containsKey(currentSection)) {
								LOG.debug("Found a document with at least two " + currentSection + " sections in file " + f.getName());
								oldText = sections.get(currentSection);
							}
							sections.put(currentSection, oldText + newText);
							sb = new StringBuilder();
						}

						line = br.readLine();
						currentSection = getNewSection(line);
					}
					sb.append(line);

					// TODO change to OS dependent line separator (is Weka
					// compatible?)
					sb.append("\n");
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}

			Patient p = patientDao.locate(rgh);

			DCE dce = new DCE(laudoId, sections);
			p.addDCE(dce);
			dceDao.save(dce);
		}

		dceDao.commit();
	}
	
	/**
	 * Check if the found section should be stratified
	 * @param lineHeader
	 * @return
	 */
	private Configuration.Sections getNewSection(String lineHeader) {
		if (sectionsTranslator.containsKey(lineHeader)) {
			return sectionsTranslator.get(lineHeader);
		} else {
			LOG.trace("Found non-default header: " + lineHeader + ".");
			return Configuration.Sections.OTHERS;
		}
	}
}
