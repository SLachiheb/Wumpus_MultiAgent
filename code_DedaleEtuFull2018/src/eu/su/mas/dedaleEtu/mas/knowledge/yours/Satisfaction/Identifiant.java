package eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction;

import jade.util.leap.Serializable;

public class Identifiant implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1751750283674864214L;
	private String  		expediteur;
	private Integer			identifiant;
	private String			positionExpediteur;
	
	public Identifiant (String e, Integer i) {
		this.expediteur = e;
		this.identifiant = i;
	}
	
	public Identifiant (String e, Integer i, String position) {
		this.expediteur = e;
		this.identifiant = i;
		this.positionExpediteur	= position;
	}
	
	public String toString() {
		String s = "";
		s += "Expediteur : " + this.expediteur + "\n";
		s += "Id         : " + this.identifiant;
		return s;
	}
	
	public String getExpediteur () {return this.expediteur;}
	public Integer getId () {return this.identifiant;}
	public String  getPositionExpediteur () {return this.positionExpediteur;}
}