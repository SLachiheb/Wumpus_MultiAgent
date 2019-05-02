package eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding;


import java.io.IOException;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.Carte;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.SerializableCarteExploration;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction.Identifiant;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendCarteDadBehaviour extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7525164256438451497L;
	
	private AgentAbstrait agent;
	private Integer 		 numberTransition;

	public SendCarteDadBehaviour(final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent = agentAbstrait;
		this.numberTransition	= null;
	}	
	
	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " <---- est dans SendCarteDadBehaviour");

		// Creation d'un message :
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("CARTE_DAD");
		// Modifier l'expéditeur du message :
		msg.setSender(this.getAgent().getAID());
		// Ajouter un destinataire :
		msg.addReceiver(this.agent.getEchoFlowding().getDad());
		
		
		//System.out.println("***********************" + this.agent.getEchoFlowding().getDad());
		//System.out.println("***********************" + this.agent.getEchoFlowding());

		
		//System.out.println(this.agent.getLocalName() + " sendMAP au Dad " + this.agent.getEchoFlowding().getDad().getLocalName());
		
        // Rendre le carteExploration serializable :
        SerializableCarteExploration carteExploSerializable = new SerializableCarteExploration
        		(this.agent.getCarteExploration());
        
        //System.out.println(this.agent.getLocalName() + " Avant : \n" + carteExploSerializable);
        
        // Creation de l'identifiant :
        Identifiant id = new Identifiant(this.agent.getLocalName(), this.agent.getIdAgent());
        Carte msgCarte = new Carte(id,carteExploSerializable , this.agent.getCarteTresors(),
        		this.agent.getCarteDangers());
    	try {
			msg.setContentObject(msgCarte);
		} catch (IOException e) {
			e.printStackTrace();
		}
    
        // Envoie de la carte au destinataire qui est le père :
		this.agent.sendMessage(msg);	
		
		// Modification de la transition :
		this.numberTransition = AgentExplorateur.T_WAIT_CARTE_DAD;
	}

	
	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + " ----> sort de SendCarteDadBehaviour\n");
		return this.numberTransition;
	}

}
