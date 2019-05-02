package eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourCollecteur;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentCollecteur;
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
	private AgentCollecteur 	agent;
	private List<AID>			listSilo;
	private Integer				numTransition;
	
	public CheckPositionSiloBehaviour (final AgentCollecteur agentCollecteur) {
		super(agentCollecteur);
		this.agent 				=   agentCollecteur;
		this.listSilo			= new ArrayList<AID>();
		this.numTransition		= null;
	}
	
	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " est dans ReceivePositionSilo");
		this.agent.setStrategie(Strategie.CheckPositionSilo);

		// Reboot :
		this.listSilo.clear();
		
		// 1) Reception des messages des Silos :
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("POSITION_SILO"));

		ACLMessage msgRecuPositionSilo = this.agent.receive(msgTemplate);
		while(msgRecuPositionSilo != null) {
			//System.out.println("\n*******************> " + this.agent.getLocalName() + " -> J'ai recu la position d'un Tanker\n");
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
		
		// Est-ce que j'ai reçu la position d'un Silo :
		if (this.agent.getPositionTanker().isEmpty() == false) {
			this.numTransition = AgentCollecteur.T_CHECK_SIGNAL_AFTER_CHECK_POSITION_SILO;
		} else {
			this.numTransition = AgentCollecteur.T_RECHERCHE_POSITION_SILO;
		}
	}

	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + " sort de ReceivePositionSilo");
		return this.numTransition;
	}
}
