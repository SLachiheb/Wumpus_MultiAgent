package eu.su.mas.dedaleEtu.mas.behaviours.yours.Exploration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.CarteDangers;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.CarteExploration;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.CarteTresors;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.CarteExploration.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
  
public class ExplorationBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/**
	 * Connaissance actuelle de l'agent en matière d'environnement
	 */
	private CarteExploration myCarteExploration;
	
	/**
	 * Connaissance actuelle de l'agent en matière de Trésor
	 */
	private CarteTresors myCarteTresors;
	
	/**
	 * Connaissance actuelle de l'agent sur les dangers
	 */
	private CarteDangers myCarteDangers;
	
	/**
	 * Type de l'agent
	 */
	private AgentAbstrait agent;
	
	
	/**
	 * Constructeur de l'objet ExploSoloBehaviour
	 * @param agentAbstrait
	 * @param myMap
	 * @param myCarteTresors
	 */
	public ExplorationBehaviour(final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent 				=   agentAbstrait;
		this.myCarteExploration =	this.agent.getCarteExploration();
		this.myCarteTresors 	= 	this.agent.getCarteTresors();
		this.myCarteDangers		= 	this.agent.getCarteDangers();
	}	
	
	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " est dans ExploSoloBehaviour");
		// Modification du comportement :
		this.agent.setStrategie(Strategie.Exploration);
		
		//0) Récupérer la position actuelle
		String myPosition= this.agent.getCurrentPosition();
		
		// MAJ de la position dans la carte d'exploration :
		this.myCarteExploration.setMyPosition(myPosition); // Pas besoin
	
		if (myPosition!=null){
			String nextNode=null;
			
			//Liste des observables à partir de la position actuelle de l'agent
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs= this.agent.observe();//myPosition
			
			this.agent.attendre();

			//1) Supprimer le noeud actuel des nodeOpen et l'ajoute aux nodeClose.
			this.myCarteExploration.addNodeClose(myPosition);
			this.myCarteExploration.removeNodeOpen(myPosition);
			
			//2) Vérification des mises à jour des attributs du noeud + Carte au tresor:
			//Liste des observations associés à la position actuel de l'agent :
			List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
			if (lObservations.isEmpty()) {
				this.myCarteExploration.addNode(myPosition);
			} else {
				//Mise à jour de l'attribut open du noeud du graphe
				this.myCarteExploration.addNode(myPosition);
				//Mise à jour de la carte au trésor :
				this.myCarteTresors.addTresors(myPosition, lObservations);
				// Mise a jour de la carte de Danger 
				this.myCarteDangers.addDangers(myPosition, lObservations);
			}
			

			//2) récupérez les noeuds environnants et, si ce n’est pas dans noeudsFerme, ajoutez-les aux noeuds ouverts.
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				/*Identifiant du noeud*/
				String nodeId=iter.next().getLeft();
				if (!this.myCarteExploration.getNodesClose().contains(nodeId)){
					if (!this.myCarteExploration.getNodesOpen().contains(nodeId)){
						this.myCarteExploration.addNodeOpen(nodeId);
						this.myCarteExploration.addNode(nodeId, MapAttribute.open);
						this.myCarteExploration.addEdge(myPosition, nodeId);	
					}else{
						//the node exist, but not necessarily the edge
						this.myCarteExploration.addEdge(myPosition, nodeId);
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
			if (this.myCarteExploration.getNodesOpen().isEmpty()){
				// Indication que l'exploration est terminée :
				this.agent.setIsExploration(false);
				// Affichage de l'etat des ressources de la Map:
				System.out.println(this.myCarteTresors);
				System.out.println(this.myCarteDangers);
				//Exploration terminée
				System.out.println("Exploration successufully done, behaviour removed.");
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
						this.agent.setCheminBut(this.myCarteExploration
								.getShortestPath(myPosition,this.myCarteExploration.getNodesOpen())); 
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
					this.agent.cheminButRemove(0); // Supprime l'élément current si on a pu bougé
				}
			}
		}
	}
	
	@Override
	public int onEnd(){
		return AgentExplorateur.T_EXPLORATEUR;
	}
	
}
