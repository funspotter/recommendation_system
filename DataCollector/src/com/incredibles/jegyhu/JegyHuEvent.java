package com.incredibles.jegyhu;

import java.util.HashMap;
import java.util.List;

import com.incredibles.data.Event;


public class JegyHuEvent extends Event {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5690690841887274757L;

	protected String ido;
	protected String helyszin;
	protected String varos;
	protected String eloadas;
	protected String shortDescription;
	protected String hosszuLeiras;
	protected HashMap<String,String> jegyTipusok;
	protected List<String> eloadasok;
	
	public JegyHuEvent() {
		super(Type.JEGYHU);
	}
	
	public JegyHuEvent(String name) {
		super(name, Type.JEGYHU);
	}
	
	public List<String> getEloadasok() {
		return eloadasok;
	}
	public void setEloadasok(List<String> eloadasok) {
		this.eloadasok = eloadasok;
	}
	public HashMap<String, String> getJegyTipusok() {
		return jegyTipusok;
	}
	public void setJegyTipusok(HashMap<String, String> jegyTipusok) {
		this.jegyTipusok = jegyTipusok;
	}
	public String getHosszuLeiras() {
		return hosszuLeiras;
	}
	public void setHosszuLeiras(String hosszuLeiras) {
		this.hosszuLeiras = hosszuLeiras;
	}
	public String getShortDescription() {
		return shortDescription;
	}
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
	public String getEloadas() {
		return eloadas;
	}
	public void setEloadas(String eloadas) {
		this.eloadas = eloadas;
	}
	public String getIdo() {
		return ido;
	}
	public void setIdo(String ido) {
		this.ido = ido;
	}
	public String getHelyszin() {
		return helyszin;
	}
	public void setHelyszin(String helyszin) {
		this.helyszin = helyszin;
	}
	public String getVaros() {
		return varos;
	}
	public void setVaros(String varos) {
		this.varos = varos;
	}
	
	@Override
	public String getDescription() {
		String ret;
		
		if (shortDescription != null && description != null) {
			
			ret = String.format("%s\n%s", shortDescription, description);
			
		} else if (shortDescription != null && description == null) {
			
			ret = shortDescription;
			
		} else if (shortDescription == null && description != null) {
			
			ret = description;
			
		} else {
			
			ret = null;
			
		}
		return ret;
	}
	
	@Override
	public String toString() {
		return String.format(
				"=================\n" + 
				">> %s <<\n" +
				"=================\n" + 
				"shortDescription: %s\n" +
				"-- %s",
				name, shortDescription, description);
	}

}
