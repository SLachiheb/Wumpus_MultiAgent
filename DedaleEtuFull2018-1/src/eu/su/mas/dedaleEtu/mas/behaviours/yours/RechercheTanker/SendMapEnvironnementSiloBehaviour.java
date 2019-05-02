package eu.su.mas.dedaleEtu.mas.behaviours.yours.RechercheTanker;

import java.io.IOException;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentCollecteur;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.Carte;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien.ActionTanker;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class SendMapEnvironnementSiloBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8927800593878802455L;
	private AgentCollecteur 	agent;
	
	public SendMapEnvironnementSiloBehaviour (final AgentCollecteur agent) {
		super(agent);
		this.agent = agent;
	}
	
	
	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " est dans SendMapEnvironnementBehaviour");

		// 1) Creation d'un message :
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("CARTE_ENVIRRONEMENT");
		// 2) Modifier l'expéditeur du message :
		msg.setSender(this.getAgent().getAID());
		// 3) Ajouter l'expéditeur :
		ActionTanker actionTanker = (ActionTanker)this.agent.getActionPDM();
		msg.addReceiver(new AID(actionTanker.getEtatTanker().getName(), AID.ISLOCALNAME));
		this.agent.setActionPDM(null);

        // 4) Rajouter la carte du danger et la carte d'exploration :
		Carte carte = new Carte(this.agent.getCarteTresors(), this.agent.getCarteDangers());
		try {
			msg.setContentObject(carte);
		} catch (IOException e) {
			e.printStackTrace();
		}
        // 5) Envoyer le message :
    	this.agent.sendMessage(msg);
		//System.out.println("**** " + this.agent.getLocalName() + " j'envoie ma carte au silo " + actionTanker.getEtatTanker().getName());

    	
    	// 2) Reception des messages :
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("CARTE_ENVIRRONEMENT"));

		ACLMessage msgRecu = this.agent.blockingReceive(msgTemplate, 500);
		while(msgRecu != null) {
			try {
				//System.out.println("**** " + this.agent.getLocalName() + " Info : j'ai reçu la carte environnement de " + msgRecu.getSender().getLocalName());

				Carte carteRecu = (Carte) msgRecu.getContentObject();
				// MaJ de la carte
				carteRecu.updateInsertCartes(this.agent.getCarteTresors(), this.agent.getCarteDangers());
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			// Next message dans la boite au lettre :
			msgRecu = this.agent.receive(msgTemplate);
		} 	
	}
	
	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + " sort de SendMapEnvironnementBehaviour\n");
		return AgentCollecteur.T_CHECK_SIGNAL_AFTER_SEND_MAP_ENV;
	}

}
