package eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.Carte;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class WaitCarteDadBehaviour extends SimpleBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3622146105725190942L;
	
	private AgentAbstrait 	agent;
	private Integer 		 	numberTransition;
	private boolean 			finished;

	public WaitCarteDadBehaviour(final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent 				= agentAbstrait;
		this.numberTransition	= null;
		this.finished 			= false;

	}	

	@Override
	public void action() {
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("X"));	

		ACLMessage msg = this.agent.receive(msgTemplate);
		if (msg != null) {		
			if (this.agent.getEchoFlowding().isSons() && this.agent.getEchoFlowding().isDad()) {
				this.numberTransition = AgentExplorateur.T_SEND_CARTE_SONS_INTERNE;
			} else {
				this.numberTransition = AgentExplorateur.T_WAIT_CARTE_DAD_TO_PRUGE_ECHOFLOWDING;
			}
			// MAJ de la carte :
			try {
				// Extrait le contenu du message dans msgCarte :
				Carte msgCarte = (Carte)msg.getContentObject();
				// MAJ des cartes par insertion :
				msgCarte.updateInsertCartes(this.agent.getCarteExploration(),
						this.agent.getCarteTresors(), 
						this.agent.getCarteDangers());
				// Active Eco MAp seulement si il y a eu des changements dans la carte :
				if (this.agent.getCarteExploration().getChangeMap() == true) {
					// Declare le partage de Map :
					this.agent.setShareMap(true);
					// L'identidiant du dernier agent ayant partager sa carte :
					this.agent.setIdentifiantShareMap(msgCarte.getIdentifiant());
					// Reboot :
					this.agent.getCarteExploration().setChangeMap(false);
				}
		        
		        
				//System.out.println("hahah mise a jour de la carte faite");
			} catch (UnreadableException e) {
				e.printStackTrace();
			}		
			this.finished = true;
		}else{
			this.agent.blockingReceive(msgTemplate, 1000);
			this.numberTransition = AgentExplorateur.T_WAIT_CARTE_DAD_TO_PRUGE_ECHOFLOWDING;
			this.finished = true;
		}
	}
	
	@Override
	public int onEnd(){
		return this.numberTransition;
	}

	@Override
	public boolean done() {
		return this.finished;
	}

}
