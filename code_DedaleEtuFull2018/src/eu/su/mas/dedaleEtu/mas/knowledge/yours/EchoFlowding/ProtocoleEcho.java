package eu.su.mas.dedaleEtu.mas.knowledge.yours.EchoFlowding;

import java.io.Serializable;
import java.util.Comparator;

public class ProtocoleEcho implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2017760657373327444L;

	private Integer 	idAgentRacine;
	private Integer 	numEcho;
	
	public ProtocoleEcho (Integer idRacine, Integer idEcho) {
		this.idAgentRacine 	= idRacine;
		this.numEcho		= idEcho;
	}
	
	public static Comparator<ProtocoleEcho> ComparatorProtocole = new Comparator<ProtocoleEcho>() {
		@Override
		public int compare(ProtocoleEcho  p1, ProtocoleEcho  p2) {
			return p1.getID().compareTo(p2.getID());
	    }
	};
	
	public String toString () {
		return "Protocol : (Agent"+ this.idAgentRacine + ", echo" + this.numEcho.toString() + ")";
	}
	
	public Integer getID () {
		return this.idAgentRacine;
	}
	
	public Integer getNumEcho () {
		return this.numEcho;
	}
}
