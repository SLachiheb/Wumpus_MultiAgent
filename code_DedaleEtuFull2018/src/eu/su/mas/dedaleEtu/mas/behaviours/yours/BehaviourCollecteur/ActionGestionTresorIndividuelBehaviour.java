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
		//System.out.println("**** " + this.agent.getLocalName() + " est dans ActionGestionTresorIndividuelBehaviour");
		
		this.agent.attendre();
		
		// MaJ des cartes Dangers et Tresors :
		this.agent.updateCarte();
		
		// Verifie si il nous reste des noeuds dans le chemin but :
		if (this.agent.getNodesBut().size() == 0) {
			//System.out.println("**** " + this.agent.getLocalName() + " Je suis arrivée au trésor \n");

			// Récupère l'action trésor collectable individuellement :
			ActionTresor actionTresor = ((ActionTresor)this.agent.getActionPDM());
			// Si je me trouve à la position du trésor :
			if (actionTresor.getEtatTresor().getPositionTresor().equals(this.agent.getCurrentPosition()) == true) {
				// Récupérer le Trésor de la position current dans ma carte récemment mise à jour :
				Tresor tresor = this.agent.getCarteTresors().getCarteTresors().get(this.agent.getCurrentPosition());
				// Si il y a encore un trésor en cette position :
				if (tresor != null) {
					//System.out.println("**** " + this.agent.getLocalName() + " le trésor se trouve à sa place \n");
					// Si le trésor est ouvert :
					if (tresor.getLockStatus() == 1) {
						//System.out.println("**** " + this.agent.getLocalName() + " le trésor est bien ouvert \n");
						// Je fais un pick dessus :
						this.pick();
					} else {
						// Si le trésor est encore vérrouillé, j'essaye de l'ouvrir :
						if (this.agent.openLock(tresor.getTypeTresor()) == true) {
							//System.out.println("**** " + this.agent.getLocalName() + " le trésor était fermé mais je l'ai ouvert \n");
							// Je fais un pick dessus :
							this.pick();
						} 
					}
				} else {
					//System.out.println("**** " + this.agent.getLocalName() + " Plus de trésor reboot \n");
					// Si il n'y a plus de trésor :
					// Reboot l'action :
					this.agent.setActionPDM(null);
					// Reboot la satisfaction :
					this.agent.setActionSatisfaction(false);
				}
			}
		} else {
			//System.out.println("**** " + this.agent.getLocalName() + " Je ne suis pas arrivée au trésor individuel \n");
			// Dans tout les cas, on continue d'avancer :
			this.seDeplacer();	
		}	
	}

	
	private boolean pick () {
		int pick = this.agent.pick();
		if (pick > 0) {
			//System.out.println("**** " + this.agent.getLocalName() + " J'ai fait un PICK de "+ pick + "\n");
			//System.out.println("**** " + this.agent.getLocalName() + " -> Place libre dans mon sac : " + this.agent.getBackPackFreeSpace() + "\n");

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
		//System.out.println("**** " + this.agent.getLocalName() + " sort de ActionGestionTresorIndividuelBehaviour");
		return AgentCollecteur.T_CHECK_SIGNAL_AFTER_TRESOR_IND;
	}
}
