
package eu.su.mas.dedaleEtu.mas.behaviours.yours.Satisfaction;

import java.util.ArrayList;
import java.util.List;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction.Satisfaction;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction.Satisfaction.EtatAgent;
import jade.core.behaviours.OneShotBehaviour;

public class AltruisteBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 657560580865483119L;
	private AgentAbstrait 	agent;
	private Integer 			numTransition;
	private ArrayList<String> 	echappatoire;

	
	public AltruisteBehaviour (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent = agentAbstrait;
		this.numTransition = null;
		this.echappatoire	= new ArrayList<String>();
	}
	
	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " <---- entre dans AtruismeBehaviour\n");
		
		// 0) Nettayage de la liste d'echappatoire: 
		this.echappatoire.clear();
		
		// 1) Recuperer le chemin du voisin avec son etat de progression :
		// Recupere le tableau de bord de l'agent qui est gêné :		
		List<String> 		cheminVoisin	= this.agent.getSatisfaction().getTaskCurrent().getChemin();
		Satisfaction 		satisfaction	= this.agent.getSatisfaction();

		// MaJ de la liste Tabou :
		satisfaction.getListTabou().add(this.agent.getCurrentPosition());

		if (satisfaction.getRecentAltruiste() == true) {
			// L'agent est un nouveau Altruiste :
			// Tenter de trouver un chemin echappatoire :
			//System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() + " : Je suis récent Altruiste !");
			this.searchChemin(satisfaction, cheminVoisin);
			this.agent.getSatisfaction().setRecentAltruiste(false);
		} else {
			// L'agent est un ancien Altruiste :
			//System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() + " : Je suis ancien Altruiste !");
			if (this.agent.getIsMove() == true) {
				if (satisfaction.getSeachEchappatoire() == true) {
					// L'agent a pu bouger et trouver un noeud echappatoire :
					// Modifier le comportement de l'agent :
					
					// Faire une attente pour laisser l'agent Voisin passer :
					/**
					 * Le faire attendre 700 ms à chaque mouvement
					 */
					try {
						this.agent.doWait(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					satisfaction.setEtatSociable(EtatAgent.egoiste);
					satisfaction.setSignalI(null);
					this.agent.setCheminButFromSatisfaction(new ArrayList<String>());
					// Reboot :
					satisfaction.setSearchEchappatoire(false);
					satisfaction.setFindNodeEchappement(true);
					satisfaction.getListTabou().clear(); // List Tabou
					satisfaction.setNeedNewCheminBut(true);
					satisfaction.setSaveSatisfaction(false);
					this.numTransition = AgentExplorateur.T_ATRUISTE_TO_PLANIFICATION;
					//System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() + " : Je suis sur le noeud Echappatoire YOUPI : " + this.agent.getCurrentPosition());
					
				
					/*if (this.agent.getClass() == AgentCollecteur.class) {
						AgentCollecteur agentCollecteur = (AgentCollecteur)this.agent;
						System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() +agentCollecteur.getActionSatisfaction());
						System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() +agentCollecteur.getActionPDM());
						System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() +agentCollecteur.getAttenteTresor());
						System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() +agentCollecteur.getSearchTresorCollectif());
						 
					}*/
					
				} else {
					// L'itération dernier, on se déplace sur le chemin but, maintenant on
					// regarde si on a une possibilité d'échappement :
					//System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() + " : J'ai avancé sur le chemin But, je repars en recherche Echappatoire");
					this.searchChemin(satisfaction, cheminVoisin);
				}
			} else {
				if (satisfaction.getSeachEchappatoire() == true) {
					// Je suis allée vers un noeud echappatoire mais il y avait déjà un agent inconnu :
					if (satisfaction.getTaskEchappatoire().getChemin().size() > 0) {
						// Il me reste encore des noeuds échappatoires à visiter :
						satisfaction.setSearchEchappatoire(true);
						satisfaction.setFindNodeEchappement(false);
						satisfaction.setNeedNewCheminBut(false);
						satisfaction.setSaveSatisfaction(true);
						// MàJ des noeuds Echappatoire + cheminBut :
						this.updateNodeEchappatoire(satisfaction);
						this.numTransition = AgentExplorateur.T_ATRUISTE_TO_PLANIFICATION;
						//System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() + " : Je n'ai pas reussi à aller vers un noeud Echappatoire, mais Il me reste encore des noeuds échappatoires à visiter");
						//System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() + " " + this.agent.getSatisfaction().getTaskEchappatoire());
					} else {
						// Il ne me reste plus de noeud echappatoire à visiter :
						//System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() + " : Je n'ai pas reussi à aller vers un noeud Echappatoire, et plus de noeuds échappatoires à visiter");
						//System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() + " : plus " + satisfaction.getTaskEchappatoire().getChemin());
						this.searchPoursuivreChemin(satisfaction, cheminVoisin);
					}
				} else {
					// Je suis allée vers un noeud but car pas d'échappatoire et il y a un agent inconnu qui me bloque :
					satisfaction.setSearchEchappatoire(false);
					satisfaction.setFindNodeEchappement(false);
					satisfaction.setNeedNewCheminBut(false);
					satisfaction.setSaveSatisfaction(true);
					this.numTransition = AgentExplorateur.T_SEND_PROPAGATION_SIGNAL_REPULSIF;
					//System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() + " : Je suis allée vers un noeud but car pas d'échappatoire et il y a un agent inconnu qui me bloque");
				}
			}
		}
	}
	
	/**
	 * Vérifie après recherche d'échappatoire si il doit poursuivre sur le chemin but ou exécuter une tâche échappatoire.
	 * @param satisfaction
	 * @param cheminVoisin
	 */
	private void searchChemin (Satisfaction satisfaction, List<String> cheminVoisin) {
		// Recherche une possibilité de chemin échappatoire au noeud (this) :
		this.searchNodesEchappatoire(cheminVoisin);
		
		if (this.echappatoire.size() > 0) {
			// Un chemin échapppatoire a été trouvé :
			satisfaction.setSearchEchappatoire(true);
			satisfaction.setFindNodeEchappement(false); // Reset
			satisfaction.setNeedNewCheminBut(false);
			satisfaction.setSaveSatisfaction(false);
			// Creation d'un chemin échappatoire :
			this.agent.getSatisfaction().newTaskEchappatoire(this.echappatoire);
			//System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() + " : RechercheEchappatoire trouvé avec " + this.echappatoire);
			// MàJ des noeuds Echappatoire + cheminBut :
			this.updateNodeEchappatoire(satisfaction);
		} else {
			this.searchPoursuivreChemin(satisfaction, cheminVoisin);
		}
		this.numTransition = AgentExplorateur.T_ATRUISTE_TO_PLANIFICATION;
	}
	
	/**
	 * Si l'agent lui reste des noeuds echappatoires à tester, il l'insert comme nouveau but :
	 * @param satisfaction
	 */
	private void updateNodeEchappatoire (Satisfaction satisfaction) {
		// Modifier le but avec le premier noeud echappatoire :
		ArrayList<String> nodeEchappatoire = new ArrayList<String>();
		nodeEchappatoire.add(satisfaction.getTaskEchappatoire().getChemin().get(0));
		// Modifie le task_current :
		this.agent.setCheminButFromSatisfaction(nodeEchappatoire); 
		// Supprime le noeud echappatoire pris pour vérification d'accessibilité :
		satisfaction.getTaskEchappatoire().getChemin().remove(0);
	}
	
	/**
	 * Si l'agent n'a pu trouver d'échappatoire, il doit se resoudre un poursuivre sur le chemin but voisin.
	 * @param satisfaction
	 * @param cheminVoisin
	 */
	private void searchPoursuivreChemin (Satisfaction satisfaction, List<String> cheminVoisin) {
		// Pas de chemin échappatoire trouvé :
		satisfaction.setSearchEchappatoire(false);
		satisfaction.setFindNodeEchappement(false);
		// Chercher un chemin sur le chemin But, pas le choix :
		if (cheminVoisin.isEmpty() == true) { // devrait jamais avoir -1 ! /*A Voir*/
			// On est arrivé au dernier noeud du chemin but voisin :
			// Trouver un nouveau chemin: tout en gardant la satisfaction personnelle ....................
			// Reboot :
			satisfaction.getListTabou().clear(); // list Tabou
			satisfaction.setNeedNewCheminBut(true); // demande une nouvelle task_current
			satisfaction.setSaveSatisfaction(true); // Conservation de satisfactionP
			// Reset task + Signal :
			satisfaction.setSignalI(null);
			// Modifier le comportement de l'agent :
			satisfaction.setEtatSociable(EtatAgent.egoiste);
			//System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() + " : Pas d'échappatoire, et pas de cheminBut => trouve un autre chemin But avec la même satP");
		} else {	
			satisfaction.setNeedNewCheminBut(false); // ne demande pas une nouvelle task_current
			satisfaction.setSaveSatisfaction(true); // Conservation de satisfactionP // pas neccessaire !
			//System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() + " :  Pas d'échappatoire, poursuivre sur le cheminBut");

		}
	}
	
	/**
	 * Lorsque l'agent est en searchEchappatoire, il peut chercher des noeuds voisins échappatoires. 
	 * @param cheminVoisin
	 */
	private void searchNodesEchappatoire (List<String> cheminVoisin) {
		// Recherche d'echappatoire au noeud présent :
		// 2) Regarde si il y a noeud voisin différent du chemin de mon voisin où je peux me déplacer :
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs= this.agent.observe();
		//Parcours les observables pour voir si l'agentX est son voisin :
		//if (this.agent.getSatisfaction().getSignal() == null)
			//System.out.println("---------------->Signal null");
		for (Couple<String,List<Couple<Observation,Integer>>> obs: lobs) {
			// non (une position du chemin but + ni dans la liste tabou + ni ma propre position)
			if (cheminVoisin.contains(obs.getLeft()) == false && 
					this.agent.getSatisfaction().getListTabou().contains(obs.getLeft()) == false &&
					this.agent.getCurrentPosition().equals(obs.getLeft()) == false) {
				this.echappatoire.add(obs.getLeft());
			}
		}
		//System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() + " :  Echappatoire trouvé : " + this.echappatoire);
	}

	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + " ----> sort de AtruismeBehaviour\n");
		//System.out.println("\n**ALTRUISTE : " + this.agent.getLocalName() + " : RECAP : \n" + this.agent.getSatisfaction());
		return this.numTransition;
	}

}

