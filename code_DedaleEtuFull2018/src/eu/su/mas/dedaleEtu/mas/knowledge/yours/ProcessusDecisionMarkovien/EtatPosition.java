package eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien;

public class EtatPosition extends Etat {

	/** 
	 * 
	 */
	private static final long serialVersionUID = -5810884070531395449L;
	
	private	String position;

	public EtatPosition(Integer id, String position) {
		super(id);
		this.position = position;
	}
	
	public String toString () {
		String s = "EtatPosition n°" + this.getId() + "\n";
		s += "Position : " + this.position + "\n";
		s += "Recompense : " + this.getRecompense();
		return s;
	}
	
	public String getname () {
		return "Position";
	}

	public String getPosition () {return this.position;}
}
