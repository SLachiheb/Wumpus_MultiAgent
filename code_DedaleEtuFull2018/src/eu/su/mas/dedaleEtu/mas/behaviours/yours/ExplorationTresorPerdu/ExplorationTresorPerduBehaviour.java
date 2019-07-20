package eu.su.mas.dedaleEtu.mas.behaviours.yours.ExplorationTresorPerdu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import jade.core.behaviours.OneShotBehaviour;

public class ExplorationTresorPerduBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -721049758664466035L;
	private AgentAbstrait agent;
	
	public ExplorationTresorPerduBehaviour (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent 				=   agentAbstrait;
	}
	
	@Override
	public void action() {		
		// Modification du comportement :
		this.agent.setStrategie(Strategie.ExplorationTresorPerdu);
		
		
		//0) Récupérer la position actuelle
		String myPosition= this.agent.getCurrentPosition();
	
		if (myPosition!=null){
			String nextNode=null;
			
			
			this.agent.attendre();

			//1) Supprimer le noeud actuel des nodeOpen et l'ajoute aux nodeClose.
			this.agent.getNodeCloseExploTresor().add(myPosition);
			this.agent.getNodeOpenExploTresor().remove(myPosition);
			
			//Liste des observables à partir de la position actuelle de l'agent
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs= this.agent.observe();//myPosition
			
			//2) Vérification des mises à jour des attributs du noeud + Carte au tresor:
			//Liste des observations associés à la position actuel de l'agent :
			List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
			if (lObservations.isEmpty() == false) {
				for (Couple<Observation,Integer> obs : lObservations) {
					if (obs.getLeft() == Observation.LOCKSTATUS) {
						//Mise à jour de la carte au trésor :
						boolean tresorPerdu = this.agent.getCarteTresors().addTresors(this.agent.getCurrentPosition(), lObservations);
						if (tresorPerdu == true) {
							this.agent.setTresorPerdu(true);
						}
						// Mise a jour de la carte de Danger 
						this.agent.getCarteDangers().addDangers(this.agent.getCurrentPosition(), lObservations);
					}
				}	
			}		
			
			if (this.agent.getCarteTresors().getCarteTresors().containsKey(this.agent.getCurrentPosition()) == true) {
				//Mise à jour de la carte au trésor :
				this.agent.getCarteTresors().addTresors(this.agent.getCurrentPosition(), lObservations);
			}
			

			//2) récupérez les noeuds environnants et, si ce n’est pas dans noeudsFerme, ajoutez-les aux noeuds ouverts.
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				/*Identifiant du noeud*/
				String nodeId=iter.next().getLeft();
				if (!this.agent.getNodeCloseExploTresor().contains(nodeId)){
					if (!this.agent.getNodeOpenExploTresor().contains(nodeId)){
						this.agent.getNodeOpenExploTresor().add(nodeId);
					}
					if (this.agent.getNodesBut().isEmpty() == true) {
						// Visite le premier voisin decouvert dans la liste des observations
						if (nextNode==null) {
							nextNode=nodeId;
						}
					}
				}
			}
			
			//3) Si la liste des ouverts est vide on s'arrête:
			if (this.agent.getNodeOpenExploTresor().isEmpty()==true){
				// Indication que l'exploration est terminée :
				this.agent.getNodeCloseExploTresor().clear();
				this.agent.getNodeOpenExploTresor().clear();
			}else{
				//4) Selectionner un nouveau mouvement :
				//4.1 Si il n'y a pas de noeud ouvert voisin de myPosition			
				if (nextNode==null){
					// Verifie si on est pas déjà en direction d'un noeud but
					if (this.agent.getNodesBut().isEmpty() == false) { 
						//System.out.println("Chemin Djisktra " + this.agent.getNodesBut());
					} else {
						//Visite le premier voisin dans la liste des nodeOpen le plus près de lui
						// ----Mise à jour de la destination de l'agent dans ses donnée:
						this.agent.setCheminBut(this.agent.getCarteExploration()
								.getShortestPath(myPosition,this.agent.getNodeOpenExploTresor())); 
					}
					nextNode = this.agent.getNodesBut().get(0);
				}else {
					// il existe un noeud ouvert directement accessible
					// ----Mise à jour de la destination de l'agent dans ses donnée:
					List<String> nodes = new ArrayList<String>();
					nodes.add(nextNode);
					this.agent.setCheminBut(nodes);
				}

				this.agent.setIsMove(this.agent.moveTo(nextNode));
				
				if (this.agent.getIsMove()) {
					// Entretient du chemin dans les données de l'agent :
					this.agent.cheminButRemove(0); 
					this.agent.incrementeCptNodeVisited();
				} 
			}
		}

	}

	@Override
	public int onEnd(){
		return AgentAbstrait.T_CHECK_SIGNAUX_AFTER_EXPLO_TRESOR_PERDU;
	}
}
