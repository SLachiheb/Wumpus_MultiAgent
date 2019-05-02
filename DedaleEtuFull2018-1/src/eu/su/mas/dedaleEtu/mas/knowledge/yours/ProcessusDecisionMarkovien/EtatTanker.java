package eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien;

public class EtatTanker extends Etat {

	/**
	 * 
	 */
	private static final long serialVersionUID = 869760554256116026L;
	
	private String	nameTanker;
	private	String 	position;

	public EtatTanker(Integer id,String name, String position) {
		super(id);
		this.nameTanker = name;
		this.position = position;
	}
	
	public String toString () {
		String s = "EtatTanker n°" + this.getId() + "\n";
		s += "Position tanker: " + this.position + "\n";
		s += "Recompense : " + this.getRecompense();
		return s;
	}
	
	public String getname () {
		return "Tanker";
	}
	
	public String getPositionTanker () {return this.position;}
	public String	  getName () {return this.nameTanker;}
}
