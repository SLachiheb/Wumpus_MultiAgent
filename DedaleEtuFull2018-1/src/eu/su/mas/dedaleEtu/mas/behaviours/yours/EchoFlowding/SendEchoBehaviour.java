package eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding;

import java.io.IOException;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;

public class SendEchoBehaviour extends OneShotBehaviour{

	/**
	 * serial
	 */
	private static final long serialVersionUID = 7044849547874022337L;
	
	private final AgentAbstrait 	agent;

	/**
	 * Constructeur
	 * @param agentAbstrait
	 */
	public SendEchoBehaviour (final AgentAbstrait  agentAbstrait) {
		super(agentAbstrait);
		this.agent	 			= agentAbstrait;
	}
	
	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + "<---- est dans SendEchoBehaviour");

		// 1) Creation d'un message :
		ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
		msg.setProtocol("ECHO");
		
		// Modifier l'expéditeur du message :
		msg.setSender(this.getAgent().getAID());
		
		// 2) Recherche des agents dans le DFS : 
		DFAgentDescription description = new DFAgentDescription();
        DFAgentDescription[] resultats;
        try {
			resultats = DFService.search(this.getAgent(), description);
            if (resultats.length > 0) {
                for (DFAgentDescription agent : resultats) {
                	// Ne communique ni avec son pere et ni avec lui même :
                	if (!this.agent.getEchoFlowding().getNoEcho().contains(agent.getName()) &&  agent.getName().equals(this.getAgent().getAID()) == false) {
                    	//System.out.println(this.agent.getLocalName() + "----> echo" + this.agent.getEchoFlowding().getProtocolEcho().getNumEcho()
                    	//		+" de protocole : " + this.agent.getEchoFlowding().getProtocolEcho() + " à " + agent.getName().getLocalName());
                    	// Ajout les expéditeurs du message :
                    	msg.addReceiver(agent.getName());
                	}
                }
            }
		} catch (FIPAException e1) {
			e1.printStackTrace();
		}
        
        // 3) Initialisation du contenu message:
        try {
			msg.setContentObject(this.agent.getEchoFlowding().getProtocolEcho());
		} catch (IOException e) {
			//System.out.println("Error : SendEchoBehaviour --> setContentObject");
			e.printStackTrace();
		}
        
        // 4) Envoie le message :
     	this.agent.sendMessage(msg);
     	
        // Attendre un petit moment :
        try {
			this.myAgent.doWait(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + " ----> sort de SendEchoBehaviour\n");
		return AgentExplorateur.T_ATTENDRE_CONFIRMATION_FILS;
	}

}
