package eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourTanker;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentTanker;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveConfirmationPositionSiloBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3765937457522673427L;
	private AgentTanker 		agent;
	
	public ReceiveConfirmationPositionSiloBehaviour (final AgentTanker agentTanker) {
		super(agentTanker);
		this.agent 				=   agentTanker;
	}
	@Override
	public void action() {
		this.agent.setStrategie(Strategie.ReceiveConfirmationPositionSilo);
		
		// 1) Reception des messages des Silos :
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("CONFIRMATION_POSITION_SILO"));

		ACLMessage msgRecuPositionSilo = this.agent.receive(msgTemplate);
		while(msgRecuPositionSilo != null) {			
			// Verifie si on a pas déjà l'agent silo dans la BD :
			if (this.agent.getKnowledgePositionSilo().contains(msgRecuPositionSilo.getSender().getLocalName()) == false) {
				// Ajouter la position du silo, si on a pas l'agent silo dans la HashMap :
				this.agent.getKnowledgePositionSilo().add(msgRecuPositionSilo.getSender().getLocalName());
			}
			// Next message dans la boite au lettre :
			msgRecuPositionSilo = this.agent.receive(msgTemplate);
		} 	
		
	}

	@Override
	public int onEnd(){
		return AgentTanker.T_PLANIFICATION_AFTER_RECEIVE;
	}
}
