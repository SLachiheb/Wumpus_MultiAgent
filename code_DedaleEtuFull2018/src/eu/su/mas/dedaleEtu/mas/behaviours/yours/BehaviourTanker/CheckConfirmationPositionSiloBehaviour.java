package eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourTanker;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentTanker;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CheckConfirmationPositionSiloBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7637743511408345056L;
	
	private AgentTanker agent;
	
	public CheckConfirmationPositionSiloBehaviour (final AgentTanker agentTanker) {
		super(agentTanker);
		this.agent 				=   agentTanker;
	}

	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " est dans ReceiveConfirmationPositionSiloBehaviour");
		this.agent.setStrategie(Strategie.CheckConfirmationPositionSilo);

		// 1) Verification si il y a des messages de type Signaux dans la boite aux lettres :
		MessageTemplate msgTemplateEcho = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
				MessageTemplate.MatchProtocol("POSITION_SILO"));
			
		ACLMessage msgRecu = null;
		do {
			msgRecu = this.agent.receive(msgTemplateEcho);
			if (msgRecu != null) {
				// Ajoute l'expéditeur dans la liste des agents à ne pas contacter pour le broadcast de la position du Silo :
				if(this.agent.getKnowledgePositionSilo().contains(msgRecu.getSender().getLocalName()) == false) {
					this.agent.getKnowledgePositionSilo().add(msgRecu.getSender().getLocalName());
				}
			}
		}while(msgRecu != null);
	}

	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + " sort de ReceiveConfirmationPositionSiloBehaviour");
		return AgentTanker.T_PLANIFICATION_AFTER_CHECK_CONFIRMATION_SILO;
	}
}
