package eu.su.mas.dedaleEtu.mas.behaviours.yours.Satisfaction;

import java.io.IOException;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction.Identifiant;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction.SignalInterblocage;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction.Satisfaction.EtatAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;

public class SendEmissionRepulsionBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 432263461506312964L;

	private AgentAbstrait 	agent;
	private Integer 			numTransition;
	
	public SendEmissionRepulsionBehaviour (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent = agentAbstrait;
		this.numTransition = null;
	}
	@Override
	public void action() {		
		// 1) Creation d'un message :
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST); 
		msg.setProtocol("SIGNAL");

		// Modifier l'expéditeur du message :
		msg.setSender(this.getAgent().getAID());
		
		// 2) Recherche des agents dans le DFS : 
		DFAgentDescription description = new DFAgentDescription();
        DFAgentDescription[] resultats;
        try {
			resultats = DFService.search(this.getAgent(), description);
            if (resultats.length > 0) {
                for (DFAgentDescription agent : resultats) {
                	// Ne communique pas avec lui même:
                    if (agent.getName().equals(this.getAgent().getAID()) == false ){
                    	msg.addReceiver(agent.getName());
                    }
                }
            }
		} catch (FIPAException e1) {
			e1.printStackTrace();
		}
        
        // Creation d'un identifiant pour d'écrire l'agent expéditeur (this):
        Identifiant id = new Identifiant(this.agent.getLocalName(), this.agent.getIdAgent());
        
        // Creation du signalInterblocage :
        //SignalInterblocage newSignal = new SignalInterblocage(id, this.agent.getN);
        
        if (EtatAgent.altruiste == this.agent.getSatisfaction().getEtatSociabilite()) {
        	// Envoie l'intensité du signal Iext:
        	 try {
        		Double powerIext = this.agent.getSatisfaction().getSignal().getIntensiteSignal();
        		Double powerSatP = this.agent.getSatisfaction().getTaskCurrent().getStaP();
        		Double power = 0.0;
        		if (powerIext < powerSatP) {
        			power = powerIext;
        		} else {
        			power = powerSatP;
        		}

     			msg.setContentObject(new SignalInterblocage(id, power, this.agent.getNodesBut(), this.agent.getCurrentPosition()));
     		} catch (IOException e) {
     			System.out.println("Error : Message Content Object dans SendEmissionRepulsion");
     			e.printStackTrace();
     		}
        } else {
        	// Envoie l'intensité du signal personnelle:
	       	 try {
	    			msg.setContentObject(new SignalInterblocage(id, this.agent.getSatisfaction().getSignalP(),
	    					this.agent.getNodesBut(), this.agent.getCurrentPosition()));
	    	} catch (IOException e) {
	    			System.out.println("Error : Message Content Object dans SendEmissionRepulsion");
	    			e.printStackTrace();
	    	}
        }

        // 4) Envoie le message :
     	this.agent.sendMessage(msg);
     	     	
     	// 5) Modification de transition 
     	this.numTransition = AgentExplorateur.T_SEND_SIGNAL_TO_PLANIFICATION;
	}
	
	@Override
	public int onEnd(){
		return this.numTransition;
	}

}
