package eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien;

public class EtatVide extends Etat {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5744462602732654120L;

	public EtatVide(Integer id) {
		super(id);
	}
	
	public String toString () {
		String s = "EtatVide n°" + this.getId() + "\n";
		s += "Recompense : " + this.getRecompense();
		return s;
	}
	
	public String getname () {
		return "Vide";
	}
}
