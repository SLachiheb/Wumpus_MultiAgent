package eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding;

import java.io.IOException;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.EchoFlowding.ProtocoleEcho;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * Attends pendant un temps limité, la confirmation de ses demandes de père,
 * auprès de ces voisins. En d'autre terme, il attends la confirmation de ses
 * fils potentiel.
 * Si il reçoit un message acceptation : envoit un message de confirmation de Dad.
 * Si il reçoit un message refus       : Si il n'a pas de père le rajoute comme père parmis les plus prioritaires.
 * Si il ne reçoit rien    : attends la fin du temps et passe à une autre transition. 
 * @author sarah
 */

public class AttendreConfirmationsFils extends OneShotBehaviour {//SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6519598221010586250L;

	private final AgentAbstrait 	agent;
	private Integer	numTransition;

	/**
	 * Constructeur
	 * @param agentAbstrait
	 */
	public AttendreConfirmationsFils (final AgentAbstrait  agentAbstrait) {
		super(agentAbstrait);
		this.agent	 			= agentAbstrait;
		this.numTransition 		= null;
	}
	
	@Override
	public void action() {		
		MessageTemplate msgYes = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
				MessageTemplate.MatchProtocol("ECHO"));
	
		ACLMessage msgRecu = this.agent.blockingReceive(msgYes, 1000);

		if (msgRecu != null) {
			// L'agent this reçoit des messages du type "Je suis l'agentX et j'accepte d'être ton fils" :
			while (msgRecu != null) {
				ProtocoleEcho msgContent = null;
				try {
					msgContent = (ProtocoleEcho) msgRecu.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				// Vérification de la péremption de du protocole :
				if (msgContent != null && 
						msgContent.getNumEcho() >= this.agent.getEchoFlowding().getIterationEcho(msgRecu.getSender())) {
					
					// Si l'agent qui m'accepte comme père à le meme protocole alors de l'accepte comme fils : A VOIR !!!!
					if (this.agent.getEchoFlowding().getProtocolEcho().getNumEcho().equals(msgContent.getNumEcho()) &&
							this.agent.getEchoFlowding().getProtocolEcho().getID().equals(msgContent.getID())) {						
						ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
						msg.setProtocol("DAD");
						// 2) Modifier l'expéditeur du message :
						msg.setSender(this.getAgent().getAID());
						// 3) Ajouter l'expéditeur :
						msg.addReceiver(msgRecu.getSender());
						// 4) Insert le protcole dans le message :
						try {
							msg.setContentObject(this.agent.getEchoFlowding().getProtocolEcho());
						} catch (IOException e) {
							e.printStackTrace();
						}
						// 5) Envoie message :
						this.agent.sendMessage(msg);
						this.agent.getEchoFlowding().addSon(msgRecu.getSender()); 
					} 
				}
				msgRecu = this.agent.receive(msgYes);
			}
			this.numTransition = AgentExplorateur.T_ECHO_FLOWDING;
		}
	
		
		// Si tu n'as pas de père à cette état, on te donne une seconde cherche de trouver un père :
		if (this.agent.getEchoFlowding().isRacine() && this.agent.getEchoFlowding().isSecondeChance() == false) {
			this.agent.getEchoFlowding().setSecondeChance(true);
			this.numTransition = AgentExplorateur.T_CHECK_ECHO_SECONDE_CHANCE;
		} else {
			this.numTransition = AgentExplorateur.T_ECHO_FLOWDING;
		}
	}
		
	
	@Override
	public int onEnd(){
		// MaJ de la racine :
		if (this.agent.getEchoFlowding().isDad() && this.agent.getEchoFlowding().isRacine()) {
			this.agent.getEchoFlowding().setRacine(false);
		}
		return this.numTransition;
	}
}
