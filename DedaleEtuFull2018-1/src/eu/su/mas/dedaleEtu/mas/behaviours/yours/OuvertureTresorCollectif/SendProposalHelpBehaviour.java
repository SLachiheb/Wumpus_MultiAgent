package eu.su.mas.dedaleEtu.mas.behaviours.yours.OuvertureTresorCollectif;

import java.io.IOException;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.Carte;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction.Identifiant;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;

public class SendProposalHelpBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8499490860311985309L;
	private AgentAbstrait agent;
	
	public SendProposalHelpBehaviour (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent 				=   agentAbstrait;
	}

	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " est dans SendProposalHelpBehaviour");
		
		// Envoyer une proposition d'aide au géneur :
		// 1) Creation d'un message :
		ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
		msg.setProtocol("HELP");
		// 2) Modifier l'expéditeur du message :
		msg.setSender(this.getAgent().getAID());
		// 3) Ajouter l'expéditeur de type Collecteur / Expediteur:
		DFAgentDescription description = new DFAgentDescription();
        DFAgentDescription[] resultats;
        try {
			resultats = DFService.search(this.getAgent(), description);
            if (resultats.length > 0) {
                for (DFAgentDescription agent : resultats) {
                	// Ne communique ni avec son pere et ni avec lui même :
                	if (agent.getName().equals(this.getAgent().getAID()) == false) {
                    	// Ajout les expéditeurs du message :
                    	msg.addReceiver(agent.getName());
                	}
                }
            }
		} catch (FIPAException e1) {
			e1.printStackTrace();
		}
        Identifiant id = new Identifiant(this.getAgent().getLocalName(), this.agent.getIdAgent(), this.agent.getCurrentPosition());
        // 4) Rajouter la carte du danger et la carte d'exploration pour la mise à jour des données + Position tresor collectif :
		Carte carte = new Carte(id, this.agent.getCarteTresors(), this.agent.getCarteDangers(), this.agent.getPosistionTresorCollectif());
        try {
			msg.setContentObject(carte);
		} catch (IOException e) {
			e.printStackTrace();
		}
        // 5) Envoyer le message :
    	this.agent.sendMessage(msg);
	}
	
	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + " sort de SendProposalHelpBehaviour");
		return AgentAbstrait.T_CHECK_CONFIRMATION_HELP;
	}
}


