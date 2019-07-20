package eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien;

import java.util.Comparator;

public class ActionTresor extends Action {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5073292486234646622L;

	private EtatTresor etatTresor;
	
	public ActionTresor (Integer id, EtatTresor etatTresor) {
		super(id);
		this.etatTresor = etatTresor;
	}
	
	public String toString () {
		return "ActionTresor" + etatTresor.getId();
	}
	
	public static Comparator<ActionTresor > ComparatorActionTresor = new Comparator<ActionTresor >() {
		@Override
		public int compare(ActionTresor  p1, ActionTresor  p2) {
			if ( p1.getEtatTresor().getTresor().getDate().compareTo(p2.getEtatTresor().getTresor().getDate()) > 0)
				return -1;
			if ( p1.getEtatTresor().getTresor().getDate().compareTo(p2.getEtatTresor().getTresor().getDate()) < 0)
				return 1;
	        return p1.getEtatTresor().getTresor().getDate().compareTo(p2.getEtatTresor().getTresor().getDate());
	    }
	};	
	
	public EtatTresor	getEtatTresor() {return this.etatTresor;}
}
