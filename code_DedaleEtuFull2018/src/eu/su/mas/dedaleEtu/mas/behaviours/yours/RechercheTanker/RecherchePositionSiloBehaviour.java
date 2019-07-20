package eu.su.mas.dedaleEtu.mas.behaviours.yours.RechercheTanker;

import java.util.List;
import java.util.Map;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import jade.core.behaviours.OneShotBehaviour;


public class RecherchePositionSiloBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6466283672889017618L;
	private AgentAbstrait 	agent;
	private Integer			numTransition;
	
	public RecherchePositionSiloBehaviour (final AgentAbstrait agent) {
		super(agent);
		this.agent 	=   agent;
		this.numTransition = null;
	}
	
	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " est dans RecherchePositionSiloBehaviour");
		this.agent.setStrategie(Strategie.RecherchePositionSilo);

		this.agent.attendre();
		this.agent.updateCarte();
		
		// Initialise une seule fois les noeuds fermés et ouverts des silos à visiter :
		if (this.agent.getNodeOpenSiloExploTresor().isEmpty() == true && this.agent.getNodeCloseSiloExploTresor().isEmpty() == true) {			
			// Recherche dans le graphe les noeuds de plus haut degré :
			// Récupérer Nb à visiter pour trouver un Tanker :
			if (this.agent.getNbNodeVisitedSilo() <= this.agent.getCarteExploration().getGraph().getNodeCount()) {// degreeNode.size()) {
				for (int i=0; i < this.agent.getNbNodeVisitedSilo(); i++) {
					// Ajoute dans les noeuds à visiter pour le collecteur pour trouver le tanker :
					this.agent.getNodeOpenSiloExploTresor().add(this.agent.getDegreeNode().positionTanker(i));//degreeNode.get(i).getId());
				}
			} else {
				// on prend pour la nombre de noeud à visiter pour trouver un Silo : degreeNode.size()
				for (int i=0; i < this.agent.getCarteExploration().getGraph().getNodeCount()/*degreeNode.size()*/; i++) {
					// Ajoute dans les noeuds à visiter pour le collecteur pour trouver le tanker :
					//this.agent.getNodeOpenSilo().add(degreeNode.get(i).getId());
					this.agent.getNodeOpenSiloExploTresor().add(this.agent.getDegreeNode().positionTanker(i));
				}
			}
			// Verifie si je ne connais pas déjà ce Silo : (nomSilo, positionSilo)
			for (Map.Entry<String, String> entry : this.agent.getPositionTanker().entrySet()) {
				if (this.agent.getNodeOpenSiloExploTresor().contains(entry.getValue()) == true) {
					// Supprimer ce Silo des ouverts car on le connait déjà :
					this.agent.getNodeOpenSiloExploTresor().remove(entry.getValue());
				}
			}
			
			if(this.agent.getNodeOpenSiloExploTresor().isEmpty() == false) {
				// Calcul l'itinéraire pour aller au noeud potentiel Tanker1 :
				List<String> cheminBut = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition(), this.agent.getNodeOpenSiloExploTresor().get(0));
				// Supprime des noeuds ouverts + Insertion dans les noeuds fermées:
				this.agent.getNodeCloseSiloExploTresor().add(this.agent.getNodeOpenSiloExploTresor().remove(0));
				// On veut allez au noeud voisin du tanker :
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
			}
		} 
		
		// Execution du déplacement :
		if (this.agent.getNodesBut().isEmpty() == true) {	
			if(this.agent.getNodeOpenSiloExploTresor().isEmpty() == true) { /***/
				this.agent.setNodeOpenSiloExploTresor(this.agent.getNodeCloseSiloExploTresor());
				this.agent.getNodeCloseSiloExploTresor().clear();
			}
			
			// Aller à l'autre position Silo :
			// Calcul l'itinéraire pour aller au noeud potentiel Tanker1 :
			List<String> cheminBut = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition(), this.agent.getNodeOpenSiloExploTresor().get(0));
			// Supprime des noeuds ouverts + Insertion dans les noeuds fermées:
			this.agent.getNodeCloseSiloExploTresor().add(this.agent.getNodeOpenSiloExploTresor().remove(0));
			// On veut allez au noeud voisin du tanker :
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
			// Verifier le boite de checkPosition Silo :
			this.numTransition = AgentAbstrait.T_CHECK_POS_SILO;
		} else {
			// Si il me reste des nodes à visiter :
			this.seDeplacer();
			this.numTransition = AgentAbstrait.T_CHECK_SIGNAL_AFTER_RECHERCHE_SILO;
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
		return this.numTransition;
	}
}