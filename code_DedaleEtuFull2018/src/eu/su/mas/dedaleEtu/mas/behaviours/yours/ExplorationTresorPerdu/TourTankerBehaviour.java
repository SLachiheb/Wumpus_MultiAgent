package eu.su.mas.dedaleEtu.mas.behaviours.yours.ExplorationTresorPerdu;

import java.io.IOException;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.Carte;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class TourTankerBehaviour extends OneShotBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2814436083872083444L;
	private AgentAbstrait	agent;

	public TourTankerBehaviour (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent 				=   agentAbstrait;
	}
	
	@Override
	public void action() {		
		// Patienter :
		this.agent.attendre();

		// MaJ des cartes Tresor et Danger :
		this.agent.updateCarte();
		
		// 1) Cas où le but est atteind :
		if (this.agent.getNodesBut().isEmpty() == true) {
			
			if (this.agent.getTankerVisited() != null) {
				// Cela veut dire que je suis arrivée à ma destination :
				// 1) Creation d'un message :
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("CARTE_ENVIRRONEMENT");
				// 2) Modifier l'expéditeur du message :
				msg.setSender(this.getAgent().getAID());
				// 3) Envoie à l'agent tanker visité pour le tour :
				msg.addReceiver(new AID(this.agent.getTankerVisited(), AID.ISLOCALNAME));
		        // 4) Rajouter la carte du danger et la carte des Tresors  :
				Carte carte = new Carte(this.agent.getCarteTresors(), this.agent.getCarteDangers());
				try {
					msg.setContentObject(carte);
				} catch (IOException e) {
					e.printStackTrace();
				}
		        // 5) Envoyer le message :
		    	this.agent.sendMessage(msg);
			}
			
			// 2) Rechercher le prochain Tanker à visiter :
			if (this.agent.getTourTanker().size() > 0) {
				// Cela veut dire, qu'il reste des tankers à identifier :
				List<String> cheminBut = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition()
						, this.agent.getTourTanker().get(0));
				// Supprime le but tanker à visiter:
				this.agent.setVisitedTanker(this.agent.getTourTanker().remove(0));
				//
				if (cheminBut.isEmpty() == true) {
					// Prendre un noeud voisin à l'agent silo si on se trouve déjà sur la position du tanker:
					//Liste des observables à partir de la position actuelle de l'agent
					List<Couple<String,List<Couple<Observation,Integer>>>> lobs= this.agent.observe();
					//Parcours les observables pour voir si l'agentX est son voisin : 
					for (Couple<String,List<Couple<Observation,Integer>>> obs: lobs) {
						if (this.agent.getCurrentPosition().equals(obs.getLeft()) == false) {
							cheminBut.add(obs.getLeft());
							break;
						}
					}
				} else {
					int taille = cheminBut.size();
					cheminBut.remove(taille-1);
				}
				// Mettre à jour le nouveau cheminBut :
				this.agent.setCheminPlanification(cheminBut);
				
				if(this.agent.getNodesBut().isEmpty() == false) {
					this.seDeplacer();
				}
			} else {
				this.agent.setVisitedTanker(null);
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
			this.agent.cheminButRemove(0); 
		}
	}
		
	@Override
	public int onEnd(){
		return AgentAbstrait.T_CHECK_SIGNAUX_AFTER_TOUR_TANKER;
	}

}
