package eu.su.mas.dedaleEtu.mas.behaviours.yours.OuvertureTresorCollectif;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CheckConfirmationProposalHelBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7626149340355561205L;
	private AgentAbstrait agent;
	
	public CheckConfirmationProposalHelBehaviour (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent 				=   agentAbstrait;
	}
	@Override
	public void action() {		
		this.agent.attendre();
		
		// Attents durante nb seconde la réponse du géneur :
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
				MessageTemplate.MatchProtocol("HELP"));

		ACLMessage msgRecu = this.agent.blockingReceive(msgTemplate, 500);
		while(msgRecu != null) {
			// Pas besoin de vérification car le destinataire du messsage à faire les vérifications requise.
			this.agent.setAttenteTresor(true);
			// Next message dans la boite au lettre :
			msgRecu = this.agent.receive(msgTemplate);
		} 	
	}

	@Override
	public int onEnd(){
		return AgentAbstrait.T_CHECK_SIGNAL_AFTER_CHECK_CONFIRMATION_HELP;
	}
}
