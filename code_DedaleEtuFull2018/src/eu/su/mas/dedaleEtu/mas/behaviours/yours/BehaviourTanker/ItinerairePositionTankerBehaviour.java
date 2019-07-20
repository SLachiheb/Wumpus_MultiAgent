package eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourTanker;

import java.util.List;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentTanker;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import jade.core.behaviours.OneShotBehaviour;

public class ItinerairePositionTankerBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6429359057419257783L;
	
	private AgentTanker agent;
	
	public ItinerairePositionTankerBehaviour (final AgentTanker agentTanker) {
		super(agentTanker);
		this.agent 				=   agentTanker;
	}

	@Override
	public void action() {
		this.agent.setStrategie(Strategie.IntinerairePositionSilo);
		this.agent.attendre();
		this.agent.updateCarte();
		
		// Si j'ai plus de noeuds dans mon chemin But :
		if (this.agent.getNodesBut().isEmpty() == true) {
			// Vérifier si je suis en position Silo :
			if (this.agent.getPositionSilo() != null &&
					this.agent.getPositionSilo().equals(this.agent.getCurrentPosition())) {
				// Je ne bouge pas :
			} else {
				// Si je ne suis pas en position en Silo, je me déplace :
				// Recalcule le chemin pour aller en position Silo :
				// Calculer le chemin pour aller de la position current à la position Tanker :
				List<String> cheminBut = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition(), this.agent.getPositionSilo());
				// Mettre à jour le nouveau cheminBut :
				this.agent.setCheminPlanification(cheminBut);
				// Se deplacer :
				this.seDeplacer();
			}
		} else {
			// Si il me reste des nodes à visiter :
			this.seDeplacer();
		}
	}
	
	private void seDeplacer () {
		// Si je ne suis pas en position en Silo, je me déplace :
		this.agent.setIsMove(this.agent.moveTo(this.agent.getNodesBut().get(0)));
		
		if (this.agent.getIsMove()) {
			// Entretient du chemin dans les données de l'agent :
			this.agent.cheminButRemove(0); // Supprime l'élément current si on a pu bouger
		}
	}
	
	@Override
	public int onEnd(){
		return AgentTanker.T_PLANIFICATION_AFTER_ITINERAIRE_SILO;
	}

}

