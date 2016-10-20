package br.usp.ime.icdc.run.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import br.usp.ime.icdc.Constants;
import br.usp.ime.icdc.dao.DAOFactory;
import br.usp.ime.icdc.dao.DceDAO;
import br.usp.ime.icdc.dao.PatientDAO;
import br.usp.ime.icdc.model.DCE;
import br.usp.ime.icdc.model.Patient;

public class DCELoader extends Thread {

	// Longest known valid sequence of numbers has 15 characters.
	private static final Pattern HEXA = Pattern.compile("[a-f0-9]{20,}");

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
		
		String line = null;
		BufferedReader br = null;
		String text = null;
		
		int i = -1;
		long start = System.currentTimeMillis();
		while (iter.hasNext()) {
			i++;
			if (i % Constants.BATCH_SIZE == 0) {
				long now = System.currentTimeMillis();
				double milisecondsPerEntry = (now - start) / (double) i;
				int remain = (int) ((milisecondsPerEntry * (total - i)) / 1000);
				LOG.debug(i + "/" + total + " (" + 100 * i / total + "%) "
						+ remain + " seconds remaining");
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
				br = new BufferedReader(new InputStreamReader(
						new FileInputStream(f), "UTF-8"));
				sb = new StringBuilder();
				for (line = br.readLine(); line != null; line = br
						.readLine()) {
					line = HEXA.matcher(line).replaceAll("");
					sb.append(line + "\n");
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			text = sb.toString();

			Patient p = patientDao.locate(rgh);

			DCE dce = new DCE(laudoId, text);
			p.addDCE(dce);
			dceDao.save(dce);



		}
		dceDao.commit();
	}
}
