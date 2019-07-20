package eu.su.mas.dedaleEtu.mas.behaviours.yours;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.CarteTresors.Tresor;
import jade.core.behaviours.OneShotBehaviour;


public class SelectionTresorCollectifBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1256093575824726328L;
	private AgentExplorateur agent;
	
	public SelectionTresorCollectifBehaviour (final AgentExplorateur agentExplorateur) {
		super(agentExplorateur);
		this.agent 				=   agentExplorateur;
	}

	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " est dans SelectionTresorCollectifBehaviour");
		
		// 1) Vérifier si on a aucune action et aucun comportement (altruiste/Abandon de task) en cours :
		// 2) Aiguillage du comportement en fonction de l'action :
		if (this.agent.getActionSatisfaction() == true) {
			// Maj variable :
			this.agent.setSearchTresorCollectif(false);
			this.agent.setAttenteTresor(false);
			// Affichage :
			//System.out.println("**** " + this.agent.getLocalName() + " Action Interblocage\n");	
		} 
		else {
			if (this.agent.getSearchTresorCollectif() == false) {
				// 1) Recherche un nouveau Trésor collectif :
				// Inserer l'itinéraire pour le cheminBut pour aller à un Trésor collectif:
				String positionTresor = this.chooseTresorCollectif(); 
				if (positionTresor != null) {
					List<String> cheminBut = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition(),
							positionTresor);
					// Mettre à jour le nouveau cheminBut :
					this.agent.setPosistionTresorCollectif(positionTresor);
					this.agent.setExplorationTresorPerdu(false);
					this.agent.setSearchTresorCollectif(true);
					this.agent.setCheminPlanification(cheminBut);
				} else {
					// Si aucun trésor à dévérouiller :
					this.agent.setExplorationTresorPerdu(true);
					this.agent.setSearchTresorCollectif(false);
					this.agent.setAttenteTresor(false); /***/
				}
				// Affichage :
				//System.out.println("**** " + this.agent.getLocalName() + " -> Action Tresor collectif Explorateur\n");
			}
		}
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
			// Si je trouve une trésor un trésor qui est encore fermé, je vais aller l'ouvrir :
			if (entry.getValue().getLockStatus() == 0) {
				aucunTresorAOuvrir = false;
			}
		}
		//entry.getValue().getTypeTresor() == this.agent.getMyTreasureType()
		if (aucunTresorAOuvrir == true) {
			return null;
		}
		
		// 1) Selectionner le ou les trésor verrouillés qui demande le moins de compétence :
		ArrayList<Tresor> leastSkills = new ArrayList<Tresor>();
		// Recherche le niveau de compétence le plus bas :
		int levelSkill = Integer.MAX_VALUE;
		for (Map.Entry<String, Tresor> entry : this.agent.getCarteTresors().getCarteTresors().entrySet()) {
			if (entry.getValue().getLockStatus() == 0) { /****************************/
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
		//System.out.println("**** " + this.agent.getLocalName() + " sort de SelectionTresorCollectifBehaviour");
		if (this.agent.getExplorationTresorPerdu() == true) {
			return AgentExplorateur.T_CHECK_SIGNAUX_AFTER_SELECT_TRESOR_COLLECTIF_EXPLO;
		} else {
			if (this.agent.getActionSatisfaction() == true) {
				// Action Interblocage
				return AgentExplorateur.T_ACTION_INTERBLOCAGE_EXPLO;
			} else {
				// Action Collectif
				return AgentExplorateur.T_ACTION_COLLECTIF_AFTER_SELECT_TRESOR;
			}
		}
	}
}