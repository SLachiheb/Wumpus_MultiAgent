package eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import jade.util.leap.Serializable;

public class SignalInterblocage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5290440777424318018L;
	
	private Date 			dateEmission;
	private Double			intensiteSignal;
	private Identifiant		id;
	private	List<String> 	cheminBut;
	private String			positionCurrent;
	
	public SignalInterblocage (Identifiant id, Double intensite, List<String> cheminBut, String positionCurrent) {
		this.id 		= id;
		this.dateEmission 		= new Date();
		this.intensiteSignal 	= intensite;
		this.cheminBut			= cheminBut;
		this.positionCurrent 	=  positionCurrent;
	}
	
	public String toString () {
		String s = "Signal :";
		s+= this.id + "\n";
		s+= "Intensité signal : " + this.intensiteSignal + "\n";
		s+= "Date             : " + this.dateEmission + "\n";
		s+= "Chemin but       : " + this.cheminBut + "\n";
		s+= "Position agent voisin  : " + this.positionCurrent;
		return s;
	}
	
	/**
	 * Compare des signaux de valeur non absolu :
	 */
	public static Comparator<SignalInterblocage> ComparatorSignaux = new Comparator<SignalInterblocage >() {
		@Override
		public int compare(SignalInterblocage  s1, SignalInterblocage  s2) {
	        Integer resultat = s1.getIntensiteSignal().compareTo(s2.getIntensiteSignal());
	        if (resultat == -1) {
	        	return 1;
	        }
	        if (resultat == 1) {
	        	return -1;
	        }
	        return resultat;
	    }
	};

	/**
	 * Retourne False si signal1 est plus recent.
	 * Retourne True  si signal2 est plus recent. 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public boolean isMoreRecent(SignalInterblocage signal2) {
		if (this.dateEmission.before(signal2.dateEmission)) {
			return false;
		} 
		return true;
	}
	
	
	/*GETTER*/ 
	public Double 	getIntensiteSignal () {
		return this.intensiteSignal;
	}
	
	public Date 	getDate () {
		return this.dateEmission;
	}
	
	public Identifiant	getIdentifiant () {
		return this.id;
	}
	
	public List<String> getCheminBut () {
		return this.cheminBut;
	}
	
	public String 	getPositionCurrent () {
		return this.positionCurrent;
	}
	
}
