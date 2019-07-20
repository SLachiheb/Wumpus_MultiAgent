package eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourTanker;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentTanker;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;

public class SendPositionSiloBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7542877469355109359L;
	
	private AgentTanker agent;
	
	public SendPositionSiloBehaviour (final AgentTanker agentTanker) {
		super(agentTanker);
		this.agent 				=   agentTanker;
	}


	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " est dans SendPositionSilo");
		this.agent.setStrategie(Strategie.SendPositionSilo);

		// Envoie un message de Confirmation pour l'agent le plus prioritaire :
		// 1) Creation d'un message :
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("POSITION_SILO");
		// 2) Modifier l'expéditeur du message :
		msg.setSender(this.getAgent().getAID());
		// 3) Ajouter l'expéditeur :
		DFAgentDescription description = new DFAgentDescription();
        DFAgentDescription[] resultats;
        try {
			resultats = DFService.search(this.getAgent(), description);
            if (resultats.length > 0) {
                for (DFAgentDescription agent : resultats) {
                	// Ne communique ni avec lui même, ni avec les agents ayant connaissance de sa position :
                	if (this.agent.getKnowledgePositionSilo().contains(agent.getName()) == false 
                			&&  agent.getName().equals(this.getAgent().getAID()) == false) {
                    	//System.out.println(this.agent.getLocalName() + "----> Position Silo à " + agent.getName().getLocalName());
                    	// Ajout les expéditeurs du message :
                    	msg.addReceiver(agent.getName());
                	}
                }
            }
		} catch (FIPAException e1) {
			e1.printStackTrace();
		}
        // 4) Rajouter la position du Silo comme contenu du message :
        msg.setContent(this.agent.getPositionSilo());
        // 5) Envoyer le message :
    	this.agent.sendMessage(msg);
	}
	
	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + " sort de SendPositionSilo");
		return AgentTanker.T_PLANIFICATION_AFTER_SEND_POSITION_SILO;
	}
}
