package eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourCollecteur;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentCollecteur;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien.ActionTanker;
import jade.core.behaviours.OneShotBehaviour;

public class ActionGestionTankerBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6948096320289987025L;
	private AgentCollecteur agent;
	private	Integer 		numTransition;
	
	public ActionGestionTankerBehaviour (final AgentCollecteur agentCollecteur) {
		super(agentCollecteur);
		this.agent 				=   agentCollecteur;
		this.numTransition		= null;
	}
	
	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " est dans ActionGestionTankerBehaviour");
		
		// MaJ de la carte :
		this.agent.updateCarte();
		this.agent.attendre();

		
		if (this.agent.getNodesBut().size() == 0) {
			// Je suis arrivée au noeud voisin du tanker, établir la communication avec le tanker :
			// Recuperer l'action du Tanker pour prendre son nom :
			ActionTanker actionTanker = (ActionTanker)this.agent.getActionPDM();
			// Est-ce que je peux vider on sac :
			if (this.agent.emptyMyBackPack(actionTanker.getEtatTanker().getName()) == true) {
				//System.out.println("**** " + this.agent.getLocalName() + " -- > Je vide mon sac dans le " + actionTanker.getEtatTanker().getName() +
				//		"*****************************\n****************************************");
				//System.out.println("**** " + this.agent.getLocalName() + " -- > mon sac contient : " + this.agent.getBackPackFreeSpace() + "\n");
				// J'ai pu vider mon sac chez le Tanker :
				// Supprimer l'actionTanker :
				// Mettre à jour les variables du collecteur:
				this.agent.setActionSatisfaction(false);
				this.agent.setSearchTresorCollectif(false);
				this.agent.setAttenteTanker(false);

				//this.agent.setActionPDM(null);
				// Mettre la transition pour envoyer la Map :
				this.numTransition = AgentCollecteur.T_SEND_MAP_ENV;
			} else {
				//System.out.println("**** " + this.agent.getLocalName() + "Le " + actionTanker.getEtatTanker().getName() + " n'est pas à sa position, RCA");
				// Je n'ai pas reussi à vider mon sac :
				this.agent.setAttenteTanker(true);
				this.numTransition = AgentCollecteur.T_CHECK_SIGNAL_AFTER_A_TANKER;
			}
		} else {
			// Je ne suis pas arrivée à mon but, je continue d'avancer :
			this.seDeplacer();
			this.numTransition = AgentCollecteur.T_CHECK_SIGNAL_AFTER_A_TANKER;
		}
	}
	
	/**
	 * Permet de faire un déplacement d'une case voisine :
	 */
	private void seDeplacer () {
		// Si je ne suis pas en position en Silo, je me déplace :
		this.agent.setIsMove(this.agent.moveTo(this.agent.getNodesBut().get(0))); // Problem
		
		if (this.agent.getIsMove()) {
			//System.out.println("**** " + this.agent.getLocalName() + " Je bouge en " +this.agent.getCurrentPosition() + "\n");
			// Entretient du chemin dans les données de l'agent :
			this.agent.cheminButRemove(0); // Supprime l'élément current si on a pu bougé
		} else {
			//System.out.println("**** " + this.agent.getLocalName() + "Move impossible vers " + this.agent.getNodesBut().get(0) 
			//		+ ", je reste en " + this.agent.getCurrentPosition() + "\n");
		}
	}
	
	
	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + " sort de ActionGestionTankerBehaviour \n ");
		return this.numTransition;
	}
}
