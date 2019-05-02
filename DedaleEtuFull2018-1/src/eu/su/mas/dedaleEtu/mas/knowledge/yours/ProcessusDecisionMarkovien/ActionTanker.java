package eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien;

public class ActionTanker extends Action {

	/**
	 * 
	 */
	private static final long serialVersionUID = 821793104263905896L;
	private EtatTanker etatTanker;
	
	public ActionTanker (Integer id, EtatTanker etatTanker){
		super(id);
		this.etatTanker = etatTanker;
	}
	
	public String toString () {
		return "ActionTanker";
	}
	
	public EtatTanker getEtatTanker () {return this.etatTanker;}
}
