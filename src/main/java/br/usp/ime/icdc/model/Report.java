package br.usp.ime.icdc.model;

import java.util.Map;

import br.usp.ime.icdc.Configuration;

public interface Report {
	public String getText();
	
	public String[] getTexts();
	
	/**
	 * 
	 * @return non-empty keys only
	 */
	public Map<Configuration.Sections, String> getZonedTexts();
}
