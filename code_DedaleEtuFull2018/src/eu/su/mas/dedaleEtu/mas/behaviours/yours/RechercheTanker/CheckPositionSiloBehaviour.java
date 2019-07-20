package eu.su.mas.dedaleEtu.mas.behaviours.yours.RechercheTanker;

import java.util.ArrayList;
import java.util.List;


import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CheckPositionSiloBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6061497315047065660L;
	private AgentAbstrait 		agent;
	private List<AID>			listSilo;
	private Integer				numTransition;
	
	public CheckPositionSiloBehaviour (final AgentAbstrait agentCollecteur) {
		super(agentCollecteur);
		this.agent 				=   agentCollecteur;
		this.listSilo			= new ArrayList<AID>();
		this.numTransition		= null;
	}
	
	@Override
	public void action() {
		this.agent.setStrategie(Strategie.CheckPositionSilo);

		// Reboot :
		this.listSilo.clear();
		
		// 1) Reception des messages des Silos :
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("POSITION_SILO"));

		ACLMessage msgRecuPositionSilo = this.agent.receive(msgTemplate); //.blockingReceive(msgTemplate, 500);
		while(msgRecuPositionSilo != null) {
			// Ajoute dans la liste à contacter :
			this.listSilo.add(msgRecuPositionSilo.getSender());
			// Récupérer le contenu du message : 
			String positionSilo = msgRecuPositionSilo.getContent();
			// Verifie si on a pas déjà l'agent silo dans la BD :
			if (this.agent.getPositionTanker().containsKey(msgRecuPositionSilo.getSender().getLocalName()) == false) {
				// Ajouter la position du silo, si on a pas l'agent silo dans la HashMap :
				this.agent.getPositionTanker().put(msgRecuPositionSilo.getSender().getLocalName(), positionSilo);
			}
			// Next message dans la boite au lettre :
			msgRecuPositionSilo = this.agent.receive(msgTemplate);
		} 	
		
		// 2) Envoie de confirmation aux agents Silos :
		// 1) Creation d'un message :
		if (this.listSilo.isEmpty() == false ) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("CONFIRMATION_POSITION_SILO");
			// 2) Modifier l'expéditeur du message :
			msg.setSender(this.getAgent().getAID());
			// 3) Ajouter l'expéditeur :
			for (AID agent: this.listSilo) {
				msg.addReceiver(agent);
			}
	        // 5) Envoyer le message :
	    	this.agent.sendMessage(msg);
		}
		
		// Est-ce que j'ai connais assez de position Silo :
		if (this.agent.getPositionTanker().size() < this.agent.getNbNodeVisitedSilo()) {
			this.numTransition = AgentAbstrait.T_RECHERCHE_POS_SILO;
		} else {
			this.agent.setCheminPlanification(new ArrayList<String>());
			this.numTransition = AgentAbstrait.T_CHECK_SIGNAUX_AFTER_CHECK_POS_SILO;
		}
	}

	@Override
	public int onEnd(){
		return this.numTransition;
	}
}
