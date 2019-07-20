package eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Action implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1813217092468701990L;
	
	private Integer 					idAction;
	private HashMap<Integer, Action> 	actions;
	private List<ActionTresor> 			actionsTresors;
	private List<ActionTanker>			actionsTankers;

	public Action (Integer id) {
		this.idAction  		= id;
	}
	
	public Action (String name) {
		this.idAction		= -1;
		this.actions		= new HashMap<Integer, Action>();
		this.actionsTresors = new ArrayList<ActionTresor>();
		this.actionsTankers = new ArrayList<ActionTanker>();
	}
	
	public void addActionTresor (ActionTresor etatTresor) {
		if (this.actions.containsKey(etatTresor.getIdAction()) == false) {
				this.actions.put(etatTresor.getIdAction(), etatTresor);
				this.actionsTresors.add(etatTresor);
		}
	}
	
	public void addActionTanker (ActionTanker etatTanker) {
		if (this.actions.containsKey(etatTanker.getIdAction()) == false) {
				this.actions.put(etatTanker.getIdAction(), etatTanker);
				this.actionsTankers.add(etatTanker);
		}
	}
	
	public Integer size () {
		return this.actions.size();
	}
	
	public String toString () {
		String s = "Actions : \n";
		for (Map.Entry<Integer, Action> entry : this.actions.entrySet()) {
			s += entry.getValue() + ", numero : " + entry.getKey() + "\n"; 
		}
		return s;
	}
	
	public Integer						getIdAction() 		{return this.idAction;}
	public HashMap<Integer, Action>		getActions() 		{return this.actions;}
	public List<ActionTresor> 			getActionsTresors() {return this.actionsTresors;}
	public List<ActionTanker> 			getActionsTankers() {return this.actionsTankers;}
}
