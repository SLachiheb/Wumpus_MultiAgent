package eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien;

import java.util.Comparator;

import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.CarteTresors.Tresor;

public class EtatTresor extends Etat{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2913760151914473510L;
	
	private String positionTresor;
	private Tresor tresor;
	
	public EtatTresor(Integer id, String positionTresor, Tresor tresor) {
		super(id);
		this.positionTresor = positionTresor;
		this.tresor			= tresor;
	}
	
	public String toString () {
		String s = "EtatTresor n°" + this.getId() + "\n";
		s += "Position Tresor: " + this.positionTresor + "\n";
		s += "Recompense : " + this.getRecompense() + "\n";
		s += tresor;
		return s;
	}
	
	public static Comparator<EtatTresor > ComparatorEtatTresor = new Comparator<EtatTresor >() {
		@Override
		public int compare(EtatTresor  p1, EtatTresor  p2) {
			if ( p1.getTresor().getDate().compareTo(p2.getTresor().getDate()) > 0)
				return -1;
			if ( p1.getTresor().getDate().compareTo(p2.getTresor().getDate()) < 0)
				return 1;
	        return p1.getTresor().getDate().compareTo(p2.getTresor().getDate());
	    }
	};	
	
	public String getname () {
		return "Tresor" + this.getId();
	}

	public String	getPositionTresor() {return this.positionTresor;}
	public Tresor	getTresor()			{return this.tresor;}
}
