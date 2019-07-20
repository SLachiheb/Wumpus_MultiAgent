package eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourCollecteur;


import eu.su.mas.dedaleEtu.mas.agents.yours.AgentCollecteur;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.CarteTresors.Tresor;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien.ActionTresor;
import jade.core.behaviours.OneShotBehaviour;

public class ActionGestionTresorIndividuelBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7397292552914664662L;
	private AgentCollecteur agent;
	
	public ActionGestionTresorIndividuelBehaviour (final AgentCollecteur agentCollecteur) {
		super(agentCollecteur);
		this.agent 				=   agentCollecteur;
	}
	@Override
	public void action() {		
		this.agent.attendre();
		
		// MaJ des cartes Dangers et Tresors :
		this.agent.updateCarte();
		
		// Verifie si il nous reste des noeuds dans le chemin but :
		if (this.agent.getNodesBut().size() == 0) {
			// Récupère l'action trésor collectable individuellement :
			ActionTresor actionTresor = ((ActionTresor)this.agent.getActionPDM());
			// Si je me trouve à la position du trésor :
			if (actionTresor.getEtatTresor().getPositionTresor().equals(this.agent.getCurrentPosition()) == true) {
				// Récupérer le Trésor de la position current dans ma carte récemment mise à jour :
				Tresor tresor = this.agent.getCarteTresors().getCarteTresors().get(this.agent.getCurrentPosition());
				// Si il y a encore un trésor en cette position :
				if (tresor != null) {
					// Si le trésor est ouvert :
					if (tresor.getLockStatus() == 1) {
						// Je fais un pick dessus :
						this.pick();
					} else {
						// Si le trésor est encore vérrouillé, j'essaye de l'ouvrir :
						if (this.agent.openLock(tresor.getTypeTresor()) == true) {
							// Je fais un pick dessus :
							this.pick();
						} 
					}
				} else {
					// Si il n'y a plus de trésor :
					// Reboot l'action :
					this.agent.setActionPDM(null);
					// Reboot la satisfaction :
					this.agent.setActionSatisfaction(false);
				}
			}
		} else {
			// Dans tout les cas, on continue d'avancer :
			this.seDeplacer();	
		}	
	}

	
	private boolean pick () {
		int pick = this.agent.pick();
		if (pick > 0) {
			// Après avoir fait un pick, je mets à jour ma carte :
			this.agent.getCarteTresors().pickTresor(this.agent.getCurrentPosition(), pick);
			this.agent.updateCarte();
			//System.out.println(this.agent.getCarteTresors());
			// Reboot l'action :
			this.agent.setActionPDM(null);
			// Reboot la satisfaction :
			this.agent.setActionSatisfaction(false);
			return true;
		}
		return false;
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
		return AgentCollecteur.T_CHECK_SIGNAL_AFTER_TRESOR_IND;
	}
}
