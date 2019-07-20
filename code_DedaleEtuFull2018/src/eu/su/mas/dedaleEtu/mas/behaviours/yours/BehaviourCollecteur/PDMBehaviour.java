package eu.su.mas.dedaleEtu.mas.behaviours.yours.BehaviourCollecteur;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentCollecteur;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.CarteTresors.Tresor;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien.Action;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien.ActionTanker;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien.ActionTresor;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien.PDM;
import jade.core.behaviours.OneShotBehaviour;

public class PDMBehaviour extends OneShotBehaviour {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8043459320048940701L;
	private AgentCollecteur agent;
	private Integer 		numTransition;
	
	public PDMBehaviour (final AgentCollecteur agentCollecteur) {
		super(agentCollecteur);
		this.agent 				=   agentCollecteur;
		this.numTransition		= null;
	}

	@Override
	public void action() {
		this.agent.setStrategie(Strategie.PDM);
		
		this.agent.attendre();
		
		if (this.agent.getPositionTanker().isEmpty() == true) {
			// Si on a pas connaissance d'un Tanker, il faut partir à la rechercher d'agent Tanker : 
			this.numTransition = AgentCollecteur.T_CHECK_POSITION_SILO;
		} else {
			// 1) Vérifier si on a aucune action et aucun comportement (altruiste/Abandon de task) en cours :
			// 2) Aiguillage du comportement en fonction de l'action :
			if (this.agent.getActionSatisfaction() == true) {
				this.agent.setIsCollectEnd(false);
				this.agent.setSearchTresorCollectif(false);
				this.agent.setAttenteTanker(false);
				this.agent.setAttenteTresor(false);
				this.agent.setActionPDM(null);
				this.numTransition = AgentCollecteur.T_ACTION_INTERBLOCAGE;
			} 
			else {
				if (this.agent.getActionPDM() == null) {
					// Si pas d'action en cours, j'en recherche une :
					// 1) Recherche une nouvelle action :
					PDM processusMarkovien = new PDM (this.agent, 0.5);
					Action selectAction = processusMarkovien.run();
					if (selectAction != null) {
						// Mettre à jour l'action du PDM :
						if (selectAction instanceof ActionTresor) {
							this.agent.setActionPDM(((ActionTresor)selectAction));
						}
						if (selectAction instanceof ActionTanker) {
							this.agent.setActionPDM(((ActionTanker)selectAction));							
						}
					}
					
					// 2) Aiguillage du comportement en fonction de l'action nouvelle :
					if (this.agent.getActionPDM() instanceof ActionTresor) {
						// Initialiser l'action :
						ActionTresor tresor = (ActionTresor)this.agent.getActionPDM();
						// Recherche chemin vers le Tresor :
						List<String> cheminBut = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition(),
								tresor.getEtatTresor().getPositionTresor());
						// Mettre à jour le nouveau cheminBut :
						this.agent.setCheminPlanification(cheminBut);
						this.agent.setSearchTresorCollectif(false);
						this.numTransition = AgentCollecteur.T_ACTION_TRESOR_INDIVIDUEL;
					} 
					
					else if (this.agent.getActionPDM() instanceof ActionTanker) {
						// 3) Aiguillage des tankers pour savoir si ils ont besoin de vider leurs sac ou d'aller chercher un trésor collectif :
						if (this.needEmptyBag() == true) {
							// Initialiser l'action:
							ActionTanker tanker = (ActionTanker)this.agent.getActionPDM();
							// Recherche chemin vers le Tanker :
							List<String> cheminBut = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition(),
									tanker.getEtatTanker().getPositionTanker());
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
							// Mettre à jour le nouveau cheminBut :
							this.agent.setCheminPlanification(cheminBut);
							this.agent.setSearchTresorCollectif(false);
							this.numTransition = AgentCollecteur.T_ACTION_TANKER;
						} else {
							// Verifier si il y a des trésors à collecter :
							if (this.isTresor() == false) {
								// Le collecteur peut partir en Exploration des tresors Perdus  :
								this.agent.setExplorationTresorPerdu(true);
								this.agent.setSearchTresorCollectif(false);							
								this.agent.setAttenteTanker(false);
								this.agent.setAttenteTresor(false);
								this.numTransition = AgentCollecteur.T_CHECK_SIGNAUX_AFTER_PDM;
							} else {
								// Inserer l'itinéraire pour le cheminBut pour aller à un Trésor collectif:
								String positionTresor = this.chooseTresorCollectif();
								List<String> cheminBut = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition(),
										positionTresor);
								// Mettre à jour le nouveau cheminBut :
								this.agent.setPosistionTresorCollectif(positionTresor);
								this.agent.setSearchTresorCollectif(true);
								this.agent.setCheminPlanification(cheminBut);
								this.agent.setAttenteTanker(false);
								this.numTransition = AgentCollecteur.T_ACTION_TRESOR_COLLECTIF;
							}
						}
					} 
				} else {
					// Si j'ai déjà une action, je m'oriente vers celle-ci :
					if (this.agent.getActionPDM() instanceof ActionTresor) {
						this.numTransition = AgentCollecteur.T_ACTION_TRESOR_INDIVIDUEL;
					} else {
						if (this.agent.getSearchTresorCollectif() == true) {
							this.numTransition = AgentCollecteur.T_ACTION_TRESOR_COLLECTIF;
						} else {
							this.numTransition = AgentCollecteur.T_ACTION_TANKER;
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * Permet de savoir si il existe un trésor individuel pouvant être récolté directement sans demande collectif:
	 * @return
	 */
	private boolean needEmptyBag () {
		boolean tresorOuvrableIndividuellement = false;
		// Parcours l'ensemble des trésors :
		for (Map.Entry<String, Tresor> entry : this.agent.getCarteTresors().getCarteTresors().entrySet()) {
			Tresor tresor = entry.getValue();
			// Vérification des compétences :
			if (this.agent.getMyTreasureType() == tresor.getTypeTresor()) {
				// Si mes compétences ne correspondent pas :
				Set<Couple<Observation,Integer>> competence = this.agent.getMyExpertise();
				boolean isForce = false;
				boolean isSerrure = false;
				for (Couple<Observation,Integer> couple: competence) {
					if (couple.getLeft() == Observation.STRENGH) {
						if (couple.getRight() >= tresor.getStrengh()) {
							isForce = true;
						}
					}
					if (couple.getLeft() == Observation.LOCKPICKING) {
						if (couple.getRight() >= tresor.getLockpicking()) {
							isSerrure = true;
						}
					}
				}
				if (isForce==true && isSerrure==true) {
					tresorOuvrableIndividuellement = true;
				}
			}
		}
		
		// Si il y en a plus d'ouvrable vérifier si l'agent a des ressources dans son sac :
		if (tresorOuvrableIndividuellement == false && this.agent.getBackPackFreeSpace() < this.agent.getCapacityBag()) {
			tresorOuvrableIndividuellement = true;
		}
		
		return tresorOuvrableIndividuellement;
	}
	
	
	private boolean isTresor () {
		// Si je peux collecter un trésor de mon Type, on retourne true :
		for (Map.Entry<String, Tresor> entry : this.agent.getCarteTresors().getCarteTresors().entrySet()) {
			if (entry.getValue().getLockStatus() == 0) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Permet de sélectionner parmis les trésors collectifs celui étant le plus prioritaire à ouvrir:
	 */
	private String chooseTresorCollectif () {
		// A ce stade, il doit nous rester que des trésors de type collectif :
		// Il faut choisir le trésor ayant :
		// 1) Celui qui demande le moins de compétence.
		// 2) Comportant le plus gros trésor.
		// 3) Comportant un String le plus petit.
			
		boolean aucunTresorAOuvrir = true;
		for (Map.Entry<String, Tresor> entry : this.agent.getCarteTresors().getCarteTresors().entrySet()) {
			if (/*entry.getValue().getTypeTresor() == this.agent.getMyTreasureType() || */
					entry.getValue().getLockStatus() == 0) {
				aucunTresorAOuvrir = false;
			}
		}
		
		if (aucunTresorAOuvrir == true) {
			return null;
		}
		
		// 1) Selectionner le ou les trésor verrouillés qui demande le moins de compétence :
		ArrayList<Tresor> leastSkills = new ArrayList<Tresor>();
		// Recherche le niveau de compétence le plus bas :
		int levelSkill = Integer.MAX_VALUE;
		for (Map.Entry<String, Tresor> entry : this.agent.getCarteTresors().getCarteTresors().entrySet()) {
			if (entry.getValue().getLockStatus() == 0) {
				if (levelSkill > (entry.getValue().getLockpicking() + entry.getValue().getStrengh())) {
					levelSkill = entry.getValue().getLockpicking() + entry.getValue().getStrengh();
				}
			}
			
		}
		// Insertion des trésors demandant le moins de compétence dans la liste :
		for (Map.Entry<String, Tresor> entry : this.agent.getCarteTresors().getCarteTresors().entrySet()) {
			if (entry.getValue().getLockStatus() == 0) { 
				if (levelSkill == (entry.getValue().getLockpicking() + entry.getValue().getStrengh())) {
					leastSkills.add(entry.getValue());
				}
			}
		}
		
		// 2) Selectionner le ou les tresor qui comportent le plus de ressource :
		ArrayList<Tresor> moreTresor = new ArrayList<Tresor>();
		int levelRessource = 0;
		for (Tresor entry : leastSkills) {
			if (levelRessource < entry.getRessource()) {
				levelRessource = entry.getRessource();
			}
		}
		// Insertion des trésors demandant le moins de compétence dans la liste :
		for (Tresor entry : leastSkills) {
			if (levelRessource == entry.getRessource()) {
				moreTresor.add(entry);
			}
		}
		
		// 3) Selectionner le ou les tresor qui comportent le plus petit String position :
		ArrayList<String> leastName = new ArrayList<String>();
		HashMap<Tresor, String> convert = this.agent.getCarteTresors().convertHashMap();
		for (Tresor entry : moreTresor) {
			leastName.add(convert.get(entry));
		}
		Collections.sort(leastName);
		return leastName.get(0);
	}	

	@Override
	public int onEnd(){
		return this.numTransition;
	}
}
