package eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.Carte;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class WaitCarteSonsBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7909829215444851028L;
	
	private AgentAbstrait 	agent;
	private boolean 			finished;
	private Integer 			numberTransition;
	private Integer				compteur;
	
	public WaitCarteSonsBehaviour (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent 				= agentAbstrait;
		this.finished 			= false;
		this.numberTransition	= null;
		this.compteur			= 0;
	}

	@Override
	public void action() {
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("CARTE_DAD"));	

		ACLMessage msg = this.agent.receive(msgTemplate);
		if (msg != null) {		
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
		        
				// Incrementation du compteur sur le nombre de message correspondant au fils :
				this.compteur++;
				if (this.compteur == this.agent.getEchoFlowding().getSons().size()) {
					this.finished = true;
				}
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}else{
			this.agent.blockingReceive(msgTemplate, 2000);
			this.finished = true;
		}
	}

	@Override
	public boolean done() {
		return this.finished;
	}
	
	@Override
	public int onEnd(){
		if (this.agent.getEchoFlowding().getDad() == null) {
			this.numberTransition = AgentExplorateur.T_SEND_CARTE_SONS;
		} else {
			this.numberTransition = AgentExplorateur.T_SEND_CARTE_DAD;
		}
		return this.numberTransition;
	}

}
