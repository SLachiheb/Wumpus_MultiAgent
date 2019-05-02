package eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien;

import java.io.Serializable;


public abstract class  Etat implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1269489363697916670L;
	
	private Integer idT;
	private Integer recompense;	
	
	public Etat (Integer idS) {
		this.idT 			= idS;
		System.out.println(this.idT);
		this.recompense		= 0;
	}
	
	public abstract String getname();
	
	public Integer 	getId() 			{return this.idT;}
	public Integer	getRecompense()		{return this.recompense;}
	public void 	setRecompense(Integer value) {this.recompense = value;}
}
