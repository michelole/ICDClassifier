package br.usp.ime.icdc.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Index;

import br.usp.ime.icdc.Constants;

@Entity
public class Patient {
	@Id
	@GeneratedValue
	private Long id;

	@Column(unique=true)
	@Index(name = "rgh_ndx")
	private Integer rgh;

	// bidirectional relationship
	@OneToMany(mappedBy="rgh")
	private List<RHC> rhc;

	// bidirectional relationship
	@OneToMany(mappedBy="rgh")
	private List<DCE> dce;
	
	// bidirectional relationship
	@OneToMany(mappedBy="rgh")
	private List<SighReport> sighReport;
	
	public Patient() {
	}

	public Patient(Integer rgh) {
		
		this.rgh = rgh;
	}

	public void addRHC(RHC rhc) {
		rhc.setRgh(this);
		if (this.rhc == null)
			this.rhc = new ArrayList<RHC>();
		this.rhc.add(rhc);
	}
	
	public void addDCE(DCE dce) {
		dce.setRgh(this);
		if (this.dce == null)
			this.dce = new ArrayList<DCE>();
		this.dce.add(dce);
	}
	
	public void addSighReport(SighReport report) {
		report.setRgh(this);
		if (this.sighReport == null)
			this.sighReport = new ArrayList<SighReport>();
		this.sighReport.add(report);
	}

	public Long getId() {
		return id;
	}

	public Integer getRgh() {
		return rgh;
	}

	public List<RHC> getRhc() {
		return rhc;
	}
	
	public List<RHC> getDistinctRhc() {
		List<RHC> ret = new ArrayList<RHC>();
		Set<String> control = new HashSet<String>();
		String code;
		for (RHC r : rhc) {
			code = r.getIcdClass().getCode();
			if (!control.contains(code)) {
				control.add(code);
				ret.add(r);
			}
		}
		return ret;
	}

	/**
	 * 
	 * @return
	 * @deprecated use getTexts instead.
	 */
	public List<DCE> getDce() {
		return dce;
	}
	
	/**
	 * 
	 * @return
	 * @deprecated use getTexts instead.
	 */
	public List<SighReport> getSighReport() {
		return sighReport;
	}
	
	public List getTexts() {
		switch (Constants.CONFIG.getSource()) {
		case SIGH_REPORT:
			return sighReport;
		case DCE_REPORT:
			return dce;
		case ALL:
			List<Report> all = new ArrayList<Report>(sighReport);
			all.addAll(dce);
			return all;
		case TOTLAUD_REPORT:
			// TODO
		default:
			return null;
		}
	}

}
