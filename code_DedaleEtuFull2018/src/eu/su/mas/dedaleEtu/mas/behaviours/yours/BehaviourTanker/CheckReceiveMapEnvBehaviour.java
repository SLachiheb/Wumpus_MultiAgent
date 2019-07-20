package eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourTanker;

import java.io.IOException;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentTanker;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.Carte;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CheckReceiveMapEnvBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6123488723806358640L;
	private AgentTanker agent;
	
	public CheckReceiveMapEnvBehaviour (final AgentTanker agentTanker) {
		super(agentTanker);
		this.agent 				=   agentTanker;
	}

	@Override
	public void action() {
		this.agent.setStrategie(Strategie.CheckReceiveMapEnv);
		
		// 2) Reception des messages :
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("CARTE_ENVIRRONEMENT"));

		ACLMessage msgRecu = this.agent.receive(msgTemplate);
		while(msgRecu != null) {
			try {
				Carte carteRecu = (Carte) msgRecu.getContentObject();
				// MaJ de la carte
				carteRecu.updateInsertCartes(this.agent.getCarteTresors(), this.agent.getCarteDangers());
				// Pour chaque message reçu par le tanker, il envoie une réponse à avec sa carte :
				// 1) Creation d'un message :
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("CARTE_ENVIRRONEMENT");
				// 2) Modifier l'expéditeur du message :
				msg.setSender(this.getAgent().getAID());
				// 3) ajouter le destinataire :
				msg.addReceiver(msgRecu.getSender());
				 // 4) Rajouter la carte du danger et la carte d'exploration :
				Carte carte = new Carte(this.agent.getCarteTresors(), this.agent.getCarteDangers());
				try {
					msg.setContentObject(carte);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// 5) Envoyer le message :
		    	this.agent.sendMessage(msg);
				
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			// Next message dans la boite au lettre :
			msgRecu = this.agent.receive(msgTemplate);
		} 	
	}

	@Override
	public int onEnd(){
		return AgentTanker.T_PLANIFICATION_AFTER_CHECK_MAP_ENV;
	}
}
