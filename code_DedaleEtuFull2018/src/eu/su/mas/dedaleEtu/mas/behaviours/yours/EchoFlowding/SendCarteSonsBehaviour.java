package eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding;


import java.io.IOException;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.Carte;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.SerializableCarteExploration;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction.Identifiant;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendCarteSonsBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1149876595498124834L;
	
	private AgentAbstrait agent;
	private Integer 		 numberTransition;

	public SendCarteSonsBehaviour(final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent = agentAbstrait;
		this.numberTransition	= null;
	}	
	
	@Override
	public void action() {
		// Creation d'un message :
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("X");
		// Modifier l'expéditeur du message :
		msg.setSender(this.getAgent().getAID());
		// Ajouter les destinataires :
		for(AID son: this.agent.getEchoFlowding().getSons()) {
			msg.addReceiver(son);
		}	
		
        // Rendre le carteExploration serializable :
        SerializableCarteExploration carteExploSerializable = new SerializableCarteExploration
        		(this.agent.getCarteExploration());
        
        // Creation de l'identifiant :
        Identifiant id = new Identifiant(this.agent.getLocalName(), this.agent.getIdAgent());
        Carte msgCarte = new Carte(id, carteExploSerializable , this.agent.getCarteTresors(),
        		this.agent.getCarteDangers());
    	try {
			msg.setContentObject(msgCarte);
		} catch (IOException e) {
			e.printStackTrace();
		}

        // Envoie de la carte au destinataire qui est le père :
		this.agent.sendMessage(msg);	
		
		// Modification de la transition :
		this.numberTransition = AgentExplorateur.T_SEND_CARTE_SONS_TO_PRUGE_ECHOFLOWDING;
	}

	
	@Override
	public int onEnd(){
		return this.numberTransition;
	}
}
