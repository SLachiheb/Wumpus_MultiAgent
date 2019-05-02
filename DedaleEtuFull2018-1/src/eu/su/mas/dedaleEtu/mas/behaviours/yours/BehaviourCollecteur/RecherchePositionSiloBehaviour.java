package eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourCollecteur;

import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentCollecteur;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import jade.core.behaviours.OneShotBehaviour;

public class RecherchePositionSiloBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6466283672889017618L;
	private AgentCollecteur 	agent;
	
	public RecherchePositionSiloBehaviour (final AgentCollecteur agentCollecteur) {
		super(agentCollecteur);
		this.agent 				=   agentCollecteur;
	}
	
	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " est dans RecherchePositionSiloBehaviour");
		this.agent.setStrategie(Strategie.RecherchePositionSilo);

		this.agent.attendre();

		this.agent.updateCarte();

		// Si j'ai plus de noeuds dans mon chemin But :
		if (this.agent.getNodesBut().isEmpty() == true) {
			if (this.agent.getNodeCloseSilo().isEmpty() == true && this.agent.getNodeOpenSilo().isEmpty() == true) {
				// Cela veut dire, qu'on a pas encore rechercher de position Silo potentiel à visiter :
				
				// Recherche dans le graphe les noeuds de plus haut degré :
				//List<Node> degreeNode = Toolkit.degreeMap(this.agent.getCarteExploration().getGraph());
				
				// Récupérer Nb à visiter pour trouver un Tanker :
				if (this.agent.getNbNodeVisitedSilo() <= this.agent.getCarteExploration().getGraph().getNodeCount()) {// degreeNode.size()) {
					for (int i=0; i < this.agent.getNbNodeVisitedSilo(); i++) {
						// Ajoute dans les noeuds à visiter pour le collecteur pour trouver le tanker :
						this.agent.getNodeOpenSilo().add(this.agent.getDegreeNode().positionTanker(i));//degreeNode.get(i).getId());
					}
				} else {
					// on prend pour la nombre de noeud à visiter pour trouver un Silo : degreeNode.size()
					for (int i=0; i < this.agent.getCarteExploration().getGraph().getNodeCount()/*degreeNode.size()*/; i++) {
						// Ajoute dans les noeuds à visiter pour le collecteur pour trouver le tanker :
						//this.agent.getNodeOpenSilo().add(degreeNode.get(i).getId());
						this.agent.getNodeOpenSilo().add(this.agent.getDegreeNode().positionTanker(i));
					}
				}
				
				// Calcul l'itinéraire pour aller au noeud potentiel Tanker1 :
				List<String> cheminBut = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition(), this.agent.getNodeOpenSilo().get(0));
				
				// Supprimer le dernier noeud qui représente la position du tanker :
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
				
				// Supprime des noeuds ouverts + Insertion dans les noeuds fermées:
				this.agent.getNodeCloseSilo().add(this.agent.getNodeOpenSilo().remove(0));
				// Mettre à jour le nouveau cheminBut :
				this.agent.setCheminPlanification(cheminBut);
			} else {
				// Cela veut dire qu'on a déjà initialiser les nodeOpen et close :
				if (this.agent.getNodeOpenSilo().isEmpty() == true) {
					// Ajoute toute la liste des noeuds closes:
					this.agent.setNodeOpenSilo(new ArrayList<String>(this.agent.getNodeCloseSilo()));
					this.agent.getNodeCloseSilo().clear();
				}

				// Calculer le chemin pour aller de la position current à la position Tanker :
				List<String> cheminBut = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition(), this.agent.getNodeOpenSilo().get(0));
				
				// Supprimer le dernier noeud qui représente la position du tanker :
				if (cheminBut.isEmpty() == true) {
					// Prendre un noeud voisin à l'agent silo si on se trouve déjà sur la position du tanker:
					//Liste des observables à partir de la position actuelle de l'agent
					List<Couple<String,List<Couple<Observation,Integer>>>> lobs= this.agent.observe();
					//Parcours les observables pour voir si l'agentX est son voisin : /*Peut être ajouter alea pour les voisins*/
					for (Couple<String,List<Couple<Observation,Integer>>> obs: lobs) {
						if (this.agent.getCurrentPosition().equals(obs.getLeft()) == false)
							cheminBut.add(obs.getLeft());
					}
				} else {
					int taille = cheminBut.size();
					cheminBut.remove(taille-1);
				}
				
				// Supprime des noeuds ouverts + Insertion dans les noeuds fermées:
				this.agent.getNodeCloseSilo().add(this.agent.getNodeOpenSilo().remove(0));
				// Mettre à jour le nouveau cheminBut :
				this.agent.setCheminPlanification(cheminBut);

				if(this.agent.getNodesBut().isEmpty() == false) {
					this.seDeplacer();
				}
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
			this.agent.cheminButRemove(0); // Supprime l'élément current si on a pu bougé
		}
	}
		
	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + " sort de RecherchePositionSiloBehaviour");
		return AgentCollecteur.T_CHECK_SIGNAL_AFTER_RECHERCHE_POSITION_SILO;
	}
}
