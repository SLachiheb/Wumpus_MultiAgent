package eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import jade.core.behaviours.OneShotBehaviour;
//import eu.su.mas.dedaleEtu.mas.knowledge.yours.EchoFlowding;

public class PurgeDataEchoFlowdingBehaviour extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8682863903516812668L;
	
	private AgentAbstrait 	agent;
	private Integer 			numTransition;
		
	public PurgeDataEchoFlowdingBehaviour (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent = agentAbstrait;
		this.numTransition = null;
	}

	@Override
	public void action() {
		
		//System.out.println("**** " + this.agent.getLocalName() + " <---- est dans PurgeDataEchoFlowdingBehaviour");
		
		// Reboot des données de l'echoFlrowding :
		this.agent.getEchoFlowding().purgeData();		
		//EchoFlowding.printEchoFlowding();
		
		// Changement de transition dans le graphe d'état :
		this.numTransition = AgentExplorateur.T_PRUGE_ECHOFLOWDING_TO_PLANIFICATION;
	}
	
	@Override
	public int onEnd() {
		//System.out.println("**** " + this.agent.getLocalName() + " ----> sorts PurgeDataEchoFlowdingBehaviour\n");
		return this.numTransition;
	}
}
