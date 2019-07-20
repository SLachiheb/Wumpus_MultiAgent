package eu.su.mas.dedaleEtu.mas.behaviours.yours.OuvertureTresorCollectif;

import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.Carte;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CheckProposalHelpBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7104865525931054554L;
	private AgentAbstrait agent;
	
	public CheckProposalHelpBehaviour (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent 				=   agentAbstrait;
	}
	
	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " est dans CheckProposalHelpBehaviour");
		
		this.agent.attendre();
		
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
				MessageTemplate.MatchProtocol("HELP"));

		ACLMessage msgRecu = this.agent.receive(msgTemplate);
		while(msgRecu != null) {
			//System.out.println("**** " + this.agent.getLocalName() + " : j'ai reçu une proposition d'aide de " + msgRecu.getSender().getLocalName());

			Carte carteRecu = null;
			try {
				carteRecu = (Carte) msgRecu.getContentObject();
				// Mettre à jour la carte de l'agent à partir de la carte reçu :
				carteRecu.updateInsertCartes(this.agent.getCarteTresors(), this.agent.getCarteDangers());
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			// Est-ce qu'il est voisin de ma position et qu'il va vers le même tresor que moi:
			if (this.isVoisin(carteRecu.getIdentifiant().getPositionExpediteur()) )
				if (this.agent.getPosistionTresorCollectif().equals(carteRecu.getPositionTresor())) {
					//System.out.println("**** " + this.agent.getLocalName() + " : j'accepte son aide de " + msgRecu.getSender().getLocalName());
		
					// Envoyer une confirmation d'acceptation de proposition d'aide :
					ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					msg.setProtocol("HELP");
					// 2) Modifier l'expéditeur du message :
					msg.setSender(this.getAgent().getAID());
					// 3) Ajouter l'expéditeur de type Collecteur / Expediteur:
					msg.addReceiver(msgRecu.getSender());
			        // 5) Envoyer le message :
			    	this.agent.sendMessage(msg);
				} else {
					//System.out.println("**** " + this.agent.getLocalName() + " : je refuse son aide car on a pas le même trésor en vue avec " + msgRecu.getSender().getLocalName());
			} else {
				//System.out.println("**** " + this.agent.getLocalName() + " : je refuse son aide car on est pas voisin avec " + msgRecu.getSender().getLocalName());
			}
			// Next message dans la boite au lettre :
			msgRecu = this.agent.receive(msgTemplate);
		} 	
	}
	
	/**
	 * L'agent (this) regarde si l'agentX est son voisin :
	 * @return
	 */
	public boolean isVoisin (String agentX) {
		boolean isVoisin = false;
		//Liste des observables à partir de la position actuelle de l'agent
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs= this.agent.observe();
		//Parcours les observables pour voir si l'agentX est son voisin :
		for (Couple<String,List<Couple<Observation,Integer>>> obs: lobs) {
			if (obs.getLeft().equals(agentX)) {
				isVoisin = true;
			}
		}
		return isVoisin;
	}
	
	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + " sort de CheckProposalHelpBehaviour");
		return AgentAbstrait.T_CHECK_SIGNAL_AFTER_CHECK_PROPOSAL_HELP;
	}
}
