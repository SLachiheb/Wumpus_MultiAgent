package eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding;

import java.io.IOException;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.EchoFlowding.EchoFlowding.AidProtocole;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Le comportement est appelé si l'agent a reçu au moins un message de demande de père.
 * Donne une reponse positive au père => pour le protocole prioritaire.
 * Donne une reponse negative au père => pour le reste sinon.
 * @author cassandre
 *
 */

public class SendConfirmerEchoDadBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2431931077306316741L;
	private AgentAbstrait agent;
	
	public SendConfirmerEchoDadBehaviour (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent = agentAbstrait;
	}
	
	@Override
	public void action() {
		// 0) Recupère le protocole le plus prioritaire dans la liste des père potentiels : 
		// Trie des protocoles sur la priorité croissante des agents en fonction de leur ID:
		this.agent.getEchoFlowding().sortDadPotentiel();
		
		// Envoie un message de Confirmation pour l'agent le plus prioritaire :
		// 1) Creation d'un message :
		ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
		msg.setProtocol("ECHO");
		// 2) Modifier l'expéditeur du message :
		msg.setSender(this.getAgent().getAID());
		// 3) Ajouter l'expéditeur :
		AidProtocole dadProtocoleC = this.agent.getEchoFlowding().getRemove();
		msg.addReceiver(dadProtocoleC.getDad());
		//3Bis) Supprime les autres pères de la liste des pères potentielles :
		this.agent.getEchoFlowding().getAddDadPotentiel().clear();
    	// 4) Mettre à jour le père potentiel et le protocole potentiel :
    	this.agent.getEchoFlowding().addDad(dadProtocoleC.getDad(), dadProtocoleC.getProtocole());
    	// 5) Mettre le protocole dans les messages :
    	try {
			msg.setContentObject(dadProtocoleC.getProtocole());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Envoie le message :
    	this.agent.sendMessage(msg);
	}

	@Override
	public int onEnd(){
		return AgentExplorateur.T_ATTENDRE_CONFIRMATION_DAD;
	}
}

