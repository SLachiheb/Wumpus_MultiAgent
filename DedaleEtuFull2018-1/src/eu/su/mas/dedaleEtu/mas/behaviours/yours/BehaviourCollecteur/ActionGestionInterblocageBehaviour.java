package eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourCollecteur;


import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentCollecteur;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import jade.core.behaviours.OneShotBehaviour;

public class ActionGestionInterblocageBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1857309157871082898L;
	private AgentAbstrait agent;
	private Integer 		numTransition;

	
	public ActionGestionInterblocageBehaviour (final AgentAbstrait agent) {
		super(agent);
		this.agent 				=   agent;
		this.numTransition		= 	null;
	}

	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " est dans ActionGestionInterblocageBehaviour");

		//this.attendre();
		this.agent.attendre();
		
		// MaJ des cartes Dangers et Tresors :
		this.agent.updateCarte();
		
		// Verifie si il nous reste des noeuds dans le chemin but :
		if (this.agent.getNodesBut().size() == 0) {
			// Indiquer qu'on est plus sous l'action d'un interblocage :
			this.agent.setActionSatisfaction(false);
			
			if (this.agent.getClass() == AgentExplorateur.class) {
				this.numTransition = AgentExplorateur.T_TRESOR_COLLECTIF_EXPLO_AFTER_A_INTERBLOCAGE;
			}
			if (this.agent.getClass() == AgentCollecteur.class) {
				this.numTransition = AgentCollecteur.T_PDM_AFTER_INTERBLOCAGE;
			}
		} else {
			// Continue d'avancer, si il n'a pas fini sa tache :
			this.seDeplacer();		
			
			if (this.agent.getClass() == AgentExplorateur.class) {
				this.numTransition = AgentExplorateur.T_CHECK_SIGNAUX_AFTER_A_INTERBLOCAGE;
			}
			if (this.agent.getClass() == AgentCollecteur.class) {
				this.numTransition = AgentCollecteur.T_CHECK_SIGNAL_AFTER_INTERBLOCAGE;
			}
		}
	}
	

	private void seDeplacer () {
		// Si je ne suis pas en position en Silo, je me déplace :
		this.agent.setIsMove(this.agent.moveTo(this.agent.getNodesBut().get(0)));
		
		if (this.agent.getIsMove()) {
			// Entretient du chemin dans les données de l'agent :
			this.agent.cheminButRemove(0); // Supprime l'élément current si on a pu bougé
		}
	}
	
	
	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + " sort de ActionGestionInterblocageBehaviour");
		return this.numTransition;
	}
}
