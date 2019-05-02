package eu.su.mas.dedaleEtu.princ;

public enum EntityType {
	AgentExplorateur("AgentExplorateur"),
	AgentCollecteur("AgentCollecteur"),
	AgentTanker("AgentTanker");
	
	private String type = "";
	
	EntityType (String type) {
		this.type = type;
	}
	
	public String toString() {
		return type;
	}
}
