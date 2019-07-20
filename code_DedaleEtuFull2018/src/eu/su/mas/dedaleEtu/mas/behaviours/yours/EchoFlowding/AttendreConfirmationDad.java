package eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.EchoFlowding.ProtocoleEcho;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class AttendreConfirmationDad extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2812760244814719637L;
	private AgentAbstrait agent;
	private boolean			 confirmation;

	
	public AttendreConfirmationDad (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent = agentAbstrait;
		this.confirmation = false;
	}

	@Override
	public void action() {
		// Check Son dans la boite au lettre : 
		MessageTemplate messageTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchProtocol("DAD"));
				
		ACLMessage msg = this.agent.blockingReceive(messageTemplate, 2000);			
		
		while (msg != null) {
			if (msg.getPerformative() == ACLMessage.CONFIRM && msg.getProtocol().equals("DAD")) {
				// Ouverture du message contenant le protocole :
				ProtocoleEcho msgContent = null;
				try {
					msgContent = (ProtocoleEcho) msg.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				
				// Si l'agent qui m'accepte comme père à le meme protocole alors je l'accepte comme fils : A VOIR !!!!
				if (this.agent.getEchoFlowding().getProtocolEcho().getNumEcho().equals(msgContent.getNumEcho()) &&
						this.agent.getEchoFlowding().getProtocolEcho().getID().equals(msgContent.getID())) {
				
					// Verifie que la demande de confirmation correspond bien au père potentiel que j'ai retenu et qu'il ne soit pas ancien:
					if (msg.getSender().getLocalName().equals(this.agent.getEchoFlowding().getDad().getLocalName()) &&
							msgContent.getNumEcho() >= this.agent.getEchoFlowding().getIterationEcho(msg.getSender())) {
						// Mettre l'agent sender dans la liste à ne pas envoyer d'echo :
						this.agent.getEchoFlowding().addNoEcho(msg.getSender());
						// Message de confirmation reçu :
						this.confirmation = true;
					} 
			}
			msg = this.agent.receive(messageTemplate);
		}
		if (this.confirmation == false) {
			// Supprimer le père potentiel + Creation d'un protocole :
			this.agent.getEchoFlowding().supprimerDadPotentiel();
		}
	}

	@Override
	public int onEnd(){
		// Ré-initialiser la confirmation à false :
		this.confirmation = false;
		if (this.agent.getEchoFlowding().isSecondeChance() == true) {
			return AgentExplorateur.T_ATTENDRE_CONFIRMATION_FILS_AFTER_CONFIRMATION_DAD;
		}
		return AgentExplorateur.T_SEND_ECHO_AFTER_CONFIRMATION_DAD;
	}
}
