package eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentTanker;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentCollecteur;

import jade.core.AID;

/**
 * La classe satisfaction permet de mettre à jour les paramètres de satisfaction de l'agent en
 * fonction de la progression de vers son but et de son environnements à partir de signaux interactifs.
 * @author sarah
 */

public class Satisfaction implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3136549813029264868L;
	private AgentAbstrait		agent;
	
	/**
	 * Un agent est dans un etat Egoiste, si sa statisfaction personnelle P(t) >= Iext reçu de ces voisins.
	 * Un agent est dans un etat Altruiste, si sa statisfaction personnelle P(t) < Iext reçe des ces voisins.
	 * @author sarah
	 */
	public enum EtatAgent implements Serializable{
		egoiste,altruiste
	}
	
	/**
	 * La classe d'énumération LevelProgression permet d'affecter une valeur de progression en fonction
	 * de la progression de l'agent vers son but à chaque itération.
	 * @author sarah
	 */
	public enum LevelProgression implements Serializable {
		PROGRESSION(new Random().nextDouble() * (3 - 1) + 1),
		ELOIGNEMENT(new Random().nextDouble() * (-2 - (-1)) + (-1)),
		IMMOBILE(new Random().nextDouble() * ((-4) - (-3)) + (-3));
		
		private Double value = 0.;
		
		LevelProgression(Double value) {
			this.value = value;
		}
		
		public Double getValue() {
			return this.value;
		}
	}

	public class Task implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7964463265796149976L;
		private List<String>			cheminButInitiale;
		//l'index de la position de l'agent sur le chemunButInitiale pour évaluer sa progresion.
		private Integer					indexProgression; 
		// L'état est déterminé en fonction de l'index de progression de l'agent.
		private LevelProgression		etatProgression;
		// Satisfaction de la tache :
		private Double					satP;
		
		public Task () {
			this.cheminButInitiale = null;
			this.indexProgression  = 0;
			this.etatProgression   = null;
			this.satP			   = VALUE_START;	
		}
		
		public Task (List<String> chemin) {
			this.cheminButInitiale = new ArrayList<String>(chemin);
			this.indexProgression  = 0;
			this.etatProgression   = LevelProgression.PROGRESSION;
			this.satP			   = VALUE_START;	

		}
		
		public Task (List<String> chemin, Integer index, LevelProgression	etat, Double satP) {
			this.cheminButInitiale	= new ArrayList<String>(chemin);
			this.indexProgression 	= index;
			this.etatProgression 	= etat;
			this.satP			   = satP;	

		}
		
		public String toString () {
			String s = "Tache courant/Echappatoire <=> \n";
			s += "StaP   : " + this.satP + "\n";
			s += "Chemin : " + this.cheminButInitiale + '\n';
			s += "Index  : " + this.indexProgression + '\n';
			s += "Etat   : " + this.etatProgression;
			return s;
		}
		
		public void updateSatP (Double value) {
			this.satP += value;
		}
		
		/*GETTER*/
		public List<String> getChemin () {return this.cheminButInitiale;}
		public Integer getIndex () {return this.indexProgression;}
		public LevelProgression getEtat () {return this.etatProgression;}
		public Double getStaP () {return this.satP;}
		
		/*SETTER*/
		public void setChemin (List<String> c) {this.cheminButInitiale = c;}
		public void setIndex (Integer i) {this.indexProgression = i;}
		public void setEtat (LevelProgression e) {this.etatProgression = e;}
		public void setSatP (Double v) {this.satP = v;}
	}
	
	// Borne de satisfaction personnelle :
	private static final Double 		PMAX 			= 30.;//100.; // 20 50.
	private final static Integer		MIN				= 5;
	private final static Integer		MAX				= 10;
	private final static Double			SEUIL			= PMAX/2;
	private Double 						VALUE_START		= new Random().nextDouble() * (MAX - MIN) + MIN;
	
	// Etat du comportement de l'agent :
	private EtatAgent				etat;
	
	private Task 					task_current;
	private Task					task_echappatoire;
	private List<String>			list_tabou;
	
	// Niveau d'insastifaction du signal :
	private SignalInterblocage		signalI;
	
	// Chemin echappatoire pour un agent altruiste :
	private boolean 				searchEchappatoire;
	private boolean					findEchappement;
	private boolean 				isRecentAltruiste;
	private boolean					needNewCheminBut;
	private boolean					saveSatisfaction;
	
	// Base de donnée pour retracé l'ancienté d'un message de type signal :
	private HashMap<AID, Date>	    bdSignal;
	
	private Random 					random;
	
	/**
	 * Constructeur :
	 * @param agentAbstrait
	 */
	public Satisfaction (final AgentAbstrait agentAbstrait) {
		this.agent 				= agentAbstrait;
		this.etat 				= EtatAgent.egoiste;
				
		this.task_current		= new Task ();
		this.task_echappatoire	= new Task ();
		this.list_tabou			= new ArrayList<String>();
		this.searchEchappatoire = false;
		this.findEchappement	= false;
		this.isRecentAltruiste  = false;
		this.needNewCheminBut   = false;
		this.saveSatisfaction   = false;

		this.signalI			= null;
		
		this.bdSignal 			= new HashMap<AID, Date>();
		
		this.random				= new Random();
	}
	
	/**
	 * Permet de voir si l'agent a déjà envoyé un msg de type Signal à l'agent this.
	 * Si oui, il test si le signal ne soit pas ancien comparé a sa base donnée.
	 * Si non, l'ajoute dans sa base.
	 * @param agent
	 * @param signal
	 * @return
	 */
	public boolean isValideBdSignal(AID agent, SignalInterblocage signal) {
		if(this.bdSignal.containsKey(agent)) {
			if (this.bdSignal.get(agent).before(signal.getDate())) {
				this.bdSignal.put(agent, signal.getDate());
				return true;
			} else {
				return false;
			}
		} else {
			this.bdSignal.put(agent, signal.getDate());
			return true;
		}
	}
	
	
	/**
	 * Mise à jour de la nouvelle tâche a exécuter de l'agent :
	 * Cette méthode est appelé à chaque fois l'agent planifie un autre but.
	 * @param newChemin
	 */
	public void newCheminBut (List<String> newChemin) {
		// Reboot :
		this.findEchappement = false;
		// Creation d'une copie du chemin :
		ArrayList<String> copyChemin = new ArrayList<String>(newChemin);
		Double satP = 0.0;
		// Save satisfaction personnelle :
		if (this.saveSatisfaction == true) {
			// Save la satisfaction Personnel :
			satP = this.task_current.getStaP();
		}
		// Initialisation nouvelle task_current:
		this.task_current = new Task (copyChemin);
		if (this.saveSatisfaction == true) {
			// Modifie la satisfaction Personnel :
			this.task_current.setSatP(satP);
		}
	}
	
	/**
	 * Methode principale de la classe Satisfaction :
	 */
	public void run () {
		// Evaluation de la progression de la tâche current:
		this.evaluationProgressionTask();
		// Mise à jour de sa satisfaction current: 
		this.updateLevelSatisfactionP();
	}
	
	/**
	 * Etape 1 :
	 * Maj l'etat de progression de la tache en fonction de la position courante de l'agent :
	 */
	private void evaluationProgressionTask () {
		// Récupére la position courant de l'agent :
		String position = this.agent.getCurrentPosition();
		
		if (this.task_current.getIndex() == -2) {
			// Si l'agent est arrivé sur son noeud but et qu'il n'y a pas de MàJ :
			this.task_current.setEtat(LevelProgression.IMMOBILE);
		} else {
			// Si l'agent est en situation de NON blocage :
			if (this.agent.getIsMove()) {
				// Récupére  dans le cheminButInitiale la position dans la liste de la position courant :
				Integer analyseP = this.task_current.getChemin().indexOf(position);
				// l'indice du chemin ne fait pas partie du cheminInitial de l'agent :
				if (analyseP == -1 && this.searchEchappatoire == true) {
					this.task_current.setEtat(LevelProgression.IMMOBILE);					
				} else if (analyseP < this.task_current.getIndex()) {
					this.task_current.setEtat(LevelProgression.ELOIGNEMENT);
				} else {
					this.task_current.setEtat(LevelProgression.PROGRESSION);
				}
			} else {
				// Si l'agent est en situation de blocage :
				this.task_current.setEtat(LevelProgression.IMMOBILE);
			}
		}
	}
	
	/**
	 * Etape 2 :
	 * Mise à jour de la satisfaction personnelle :
	 * (f < n < 0 < m)
	 */
	private void updateLevelSatisfactionP () {
		if (this.agent.getClass() == AgentTanker.class) {
			AgentTanker agentTanker = (AgentTanker)this.agent;
			
			if (agentTanker.getPositionSilo() != null &&
					this.agent.getCurrentPosition().equals(agentTanker.getPositionSilo())) {
				// MaJ de la satisfaction personnelle :
				this.task_current.setSatP(0.);
			} else {
				// MaJ de la satisfaction personnelle :
				this.task_current.updateSatP(this.task_current.getEtat().getValue());
			}
		}
		
		if (this.agent.getClass() == AgentExplorateur.class) {
			AgentExplorateur agentExplorateur = (AgentExplorateur)this.agent;
			if (agentExplorateur.getAttenteTresor() == true) {
				this.task_current.setSatP(0.);
			} else {
				this.task_current.updateSatP(this.task_current.getEtat().getValue());
			}
		}
		
		if (this.agent.getClass() == AgentCollecteur.class) {
			AgentCollecteur agentCollecteur = (AgentCollecteur)this.agent;
			if (agentCollecteur.getAttenteTanker() == true) {
				this.task_current.setSatP(0.);
			} 
			else if (agentCollecteur.getAttenteTresor() == true) {
				this.task_current.setSatP(0.);
			}
			else {
				this.task_current.updateSatP(this.task_current.getEtat().getValue());
			}
		}

		// Vérification des bornes de la satsfaction après MaJ de la statisfaction P:
		if (this.task_current.getStaP() > PMAX) {
			this.task_current.setSatP(PMAX);
		}
		if (this.task_current.getStaP() < -PMAX) {
			this.task_current.setSatP(-PMAX);
		}

		if (this.searchEchappatoire == false) {
			// Maj de l'index de progression pour le prochain mouvement prévue :
			String position = this.agent.getCurrentPosition();
			
			// Mettre l'index à la position de l'agent sur le cheminBut (Cohérence si eloignement ou immobilisation):
			this.task_current.setIndex(this.task_current.getChemin().indexOf(position));
			
			if (this.task_current.getIndex() + 1 > this.task_current.getChemin().size() - 1) {
				// Si plus noeud à visité dans le chemin but :
				this.task_current.setIndex(-2); // Rester Immobile.
			} else {
				// Le déplacement prévue corresponds à la position prochaine de la position courant de l'agent :
				this.task_current.setIndex(this.task_current.getChemin().indexOf(position) + 1);
			}
		}
		
		// Test si je poursuis ma tâche :
		this.isPoursuivreTask();
	}
	
	/**
	 * Evalue le niveau de satisfaction pour définir le comportement de l'agent :
	 * Si il doit poursuivre la tache ou abandonner la tache en question.
	 */
	public boolean isPoursuivreTask () {
		boolean isPoursuivre = true;
		if (this.task_current.getStaP() == -PMAX) {
			// Echec de currentTask, redéfinir une nouvelle mission :
			// Trouver un nouveau noeud but en fonction du type de l'agent:
			List<String> newChemin = this.findChemin();
			// MaJ du cheminBut :
			this.agent.setCheminBut(newChemin); //
			// Devenir Egoiste :
			this.setEtatSociable(EtatAgent.egoiste);
			this.list_tabou.clear(); 
			// Mettre à jour les variables :
			this.needNewCheminBut = true;
			this.searchEchappatoire = false;
			this.saveSatisfaction = false;
			// Supprimer le signal :
			this.signalI = null;
		}
		return isPoursuivre;
	}

	
	public List<String> findChemin () { 
		ArrayList<String> cheminFind = new ArrayList<String>();
		
		if (this.agent.getClass() == AgentCollecteur.class && this.agent.getExploration() == false) {
			AgentCollecteur agentCollecteur = (AgentCollecteur)this.agent;
			agentCollecteur.setActionSatisfaction(true);
			agentCollecteur.setSearchTresorCollectif(false);
			// Annuler l'action en cours :
			agentCollecteur.setActionPDM(null);
		}

		// Si je suis toujours en état d'exploration :
		if (this.agent.getExploration() == true) {
			// Recherche les chemins but possible dans les noeuds ouvert :
			List<List<String>> list_chemin_NO = this.agent.getCarteExploration().getShortestPathNodes(this.agent.getCurrentPosition(),
					this.agent.getCarteExploration().getNodesOpen(), 5);
			
			// Supprime mon noeud but non accessible pour le moment :
			if (this.agent.getNodesBut().isEmpty() == false) {
				int indexCheminBut = this.agent.getNodesBut().size() - 1;
				for (int i=0; i < list_chemin_NO.size() ; i++) {
					// Index pour recupérer le noeud but d'un chemin y allant :
					int indexList = list_chemin_NO.get(i).size() - 1;
					if (list_chemin_NO.get(i).isEmpty() == false) {
						if (list_chemin_NO.get(i).get(indexList).equals(this.agent.getNodesBut().get(indexCheminBut)) == true) {
							// Supprimer de la liste :
							list_chemin_NO.remove(i);
							break;
						}
					}
				}	
			}
			
			// Supprimer les chemins passant par le noeud bloqué :
			if (this.agent.getNodesBut().isEmpty() == false) {
				String nodeBlocked = this.agent.getNodesBut().get(0); // Le tanker risque de ne plus avoir de noeud but
				for (int i=0; i < list_chemin_NO.size() ; i++) {
					for (int j=0; j < list_chemin_NO.get(i).size() ; j++) {
						if (list_chemin_NO.get(i).get(j).equals(nodeBlocked) == true) {
							list_chemin_NO.remove(i);
							i = 0;
							break;
						}
					}
				}
			}
			
			
			// Si il me reste moins d'un seul noeud but à visiter:
			if (list_chemin_NO.size() <= 1) {
				// Supprimer ma position courant du noeud close pour le moment pour eviter de rester immobile :
				ArrayList<String> nodeCloses = new ArrayList<String>();
				for (String node: this.agent.getCarteExploration().getNodesClose()) {
					if (node.equals(this.agent.getCurrentPosition()) == false) 
						nodeCloses.add(node);
				}
				
				if (nodeCloses.isEmpty() == false) {
					// Choisir un noeud au hasard dans la liste des NodesClose : // Noeud voisin
					List<List<String>> list_chemin_close = this.agent.getCarteExploration().getShortestPathNodes(this.agent.getCurrentPosition(),
							new ArrayList<String>(nodeCloses), 5);		
					
					// Supprime les chemins passant par le noeud bloqué :
					if (this.agent.getNodesBut().isEmpty() == false) {
						String nodeBlocked = this.agent.getNodesBut().get(0); // Le tanker risque de ne plus avoir de noeud but
						for (int i=0; i < list_chemin_close.size() ; i++) {
							for (int j=0; j < list_chemin_close.get(i).size() ; j++) {
								if (list_chemin_close.get(i).get(j).equals(nodeBlocked) == true) {
									list_chemin_close.remove(i);
									i = 0;
									break;
								}
							}
						}
					}
					if (list_chemin_close.isEmpty() == false) {
						// Taille de la liste des noeuds fermées :
						int taille_list_close = list_chemin_close.size();
						// Prendre un chemin de taille de la moitié du pire cas :
						int alea = this.random.nextInt(taille_list_close); //
						cheminFind = new ArrayList<String>(list_chemin_close.get(alea));
					}	
				}
			} else {
				// Si il me reste plus d'un noeud but à visiter :
				int taille_list = list_chemin_NO.size();
				int alea = this.random.nextInt(taille_list);
				cheminFind = new ArrayList<String>(list_chemin_NO.get(alea));
			}
		} else {
			/*Si on est plus en exploration : l'abandon de tache ce fait par Type d'agent :*/
			if (this.agent.getClass() == AgentTanker.class) {
				// Si je suis un tanker et que je suis obligé d'abandonner ma tâche pour rejoindre ma position silo :
				AgentTanker agentTanker = (AgentTanker)this.agent;
				// Recherche un chemin pour débloquer la situation :
				cheminFind = (ArrayList<String>) agentTanker.searchChemin_isAbandonTask();
			}
			/*Si on est plus en exploration : l'abandon de tache ce fait par Type d'agent :*/
			if (this.agent.getClass() == AgentCollecteur.class) {
				// Si je suis un tanker et que je suis obligé d'abandonner ma tâche pour rejoindre ma position silo :
				AgentCollecteur agentCollecteur = (AgentCollecteur)this.agent;
				// Recherche un chemin pour débloquer la situation :
				cheminFind = (ArrayList<String>) agentCollecteur.searchChemin_isAbandonTask();
			}
			/*Si on est plus en exploration : l'abandon de tache ce fait par Type d'agent :*/
			if (this.agent.getClass() == AgentExplorateur.class) {
				// Si je suis un tanker et que je suis obligé d'abandonner ma tâche pour rejoindre ma position silo :
				AgentExplorateur agentExplorateur = (AgentExplorateur)this.agent;
				// Recherche un chemin pour débloquer la situation :
				cheminFind = (ArrayList<String>) agentExplorateur.searchChemin_isAbandonTask();
			}
		}
		return cheminFind;
	}
	
	/**
	 * 
	 * @param newSignal
	 */
	public void updateSignalInteractif (SignalInterblocage newSignal) { 	
		if (this.signalI != null) {
			// Si newSignal est plus négatif que le signal antérieur et plus recent :
			if (newSignal.getIntensiteSignal() < this.signalI.getIntensiteSignal()) {
				this.updateParametreSignal(newSignal);
			} else {
				System.out.println("Signal pas pris en compte car moins intense !!! :: " + newSignal.getIntensiteSignal() +", " + this.signalI.getIntensiteSignal());
			}
		} else  {
			this.updateParametreSignal(newSignal);
		}
	}	
	
	
	private boolean checkSeuilAltruiste (SignalInterblocage newSignal) {
		boolean isPrioritaire = false;
		// Vérifie si le seuil a été dépassé :
		if (newSignal.getIntensiteSignal() <= SEUIL) {
			// Vérifie si l'agent (this) est mon prioritaire que l'expéditeur du signal :
			if (this.agent.getIdAgent() < newSignal.getIdentifiant().getId()) {
				// Vérifie que l'agent soit égoiste :
				if (this.etat == EtatAgent.egoiste) 
					isPrioritaire = true;
			}
		}
		return isPrioritaire;
	}
	
	private void updateParametreSignal (SignalInterblocage newSignal) { // PB
		// Calcul pour determiner si l'agent sera altruiste:
		Double Iext 	= newSignal.getIntensiteSignal();
		//Double alpha	= this.agent.getTauxSociabilite();
		if (Iext < this.task_current.getStaP() || this.checkSeuilAltruiste(newSignal)) {
			this.etat = EtatAgent.altruiste;
			// MàJ du signal Iext de l'agent, celui-ci est pris en compte.
			this.signalI = newSignal;
			// Modification du chemin But :
			String posAgentGenant = newSignal.getCheminBut().remove(0);
			if (posAgentGenant.equals(this.agent.getCurrentPosition())) {
				ArrayList<String> newChemin = new ArrayList<String>(newSignal.getCheminBut());
				// Déclarer l'agent comme étant un récent altruiste :
				
				// Si je suis un agentCollecteur et que je deviens altruiste alors je dois mettre ma satisfaction à true :
				if (this.agent.getClass() == AgentCollecteur.class) {
					AgentCollecteur agentCollecteur = (AgentCollecteur)this.agent;
					agentCollecteur.setActionSatisfaction(true);
					agentCollecteur.setSearchTresorCollectif(false);
					// Annuler l'action en cours :
					agentCollecteur.setActionPDM(null);
				}
				
				if (this.agent.getClass() == AgentExplorateur.class) {
					AgentExplorateur agentExplorateur = (AgentExplorateur)this.agent;
					agentExplorateur.setActionSatisfaction(true);
					agentExplorateur.setSearchTresorCollectif(false);
				}
				
				// Reboot  :
				this.isRecentAltruiste = true;
				this.needNewCheminBut = false;
				this.saveSatisfaction = false;
				this.searchEchappatoire = false;
				this.findEchappement = false;
				// Modification du but de l'agent :
				this.agent.setCheminBut(newChemin);
				// MàJ de sa satisfaction personnelle avec Iext => Iext = P(t).
				this.task_current.setSatP(this.signalI.getIntensiteSignal());
				// Ajoute la position du voisin :
				this.list_tabou.add(newSignal.getPositionCurrent());
			} 
		} else if (Iext >= this.task_current.getStaP() && this.etat == EtatAgent.altruiste) {
			// Garder le signal ancien.
		} else {
			this.etat = EtatAgent.egoiste;
			// Il ne prend pas en compte le signal de repulsion.
			this.signalI = null;
		}
	}
	
	/**
	 * 
	 * @param list
	 */
	public void newTaskEchappatoire (List<String> list) {
		if (this.searchEchappatoire == true) {
			this.task_echappatoire = new Task (list);
		} else {
			System.out.println("*****************************************");
		}
	}
	
	public List<String> buildCheminBut () {
		ArrayList<String> buildChemin = new ArrayList<String> ();
		for (int i=this.task_current.getIndex(); i < this.task_current.getChemin().size(); i++) {
			buildChemin.add(this.task_current.getChemin().get(i));
		}
		return buildChemin;
	}
	
	public void reset () {
		this.etat = EtatAgent.egoiste;
		this.findEchappement = false;
		this.isRecentAltruiste = false;
		this.list_tabou.clear();
		this.needNewCheminBut = false;
		this.saveSatisfaction = false;
		this.searchEchappatoire = false;
		this.signalI = null;
		this.task_current = null;
		this.task_echappatoire = null;
	}
		
	public String toString () {
		String s = "Carnet Satisfaction : \n";
		s += "Comportement  : " + this.etat + "\n";
		s += "Signal Iext   <=> \n " + this.signalI + "\n";
		s += "Search Echappatoire : " + this.searchEchappatoire + "\n";
		s += "FindEchappement : " + this.findEchappement + "\n";
		s += "NeedNewCheminBut : " + this.needNewCheminBut + "\n";
		s += "RecentAltruiste  : " + this.isRecentAltruiste + "\n";
		s += "SaveSatistique   : " + this.saveSatisfaction + "\n";
		if (this.searchEchappatoire == true) {
			s += "Tache echappatoire : \n";
			s += this.task_echappatoire + "\n";
			s += "Echappatoire trouvée : " + this.findEchappement + "\n";
		}
		s += this.task_current + "\n";
		s += "Position actuelle : " + this.agent.getCurrentPosition() + "\n";
		s += "Base de donnée : \n";
		for(Entry<AID, Date> entry : this.bdSignal.entrySet()) {
		    AID cle = entry.getKey();
		    Date valeur = entry.getValue();
		    s += "(" + cle.getLocalName() + ", " + valeur.toString() + ")\n";
		}
		s += "CheminButAGENT : " + this.agent.getNodesBut() + "\n";
		return s;
	}

	/**
	 * GETTER
	 */
	public EtatAgent getEtatSociabilite () {
		return this.etat;
	}
	public SignalInterblocage getSignal() {
		return this.signalI;
	}
	public Double	getSignalP() {
		return this.task_current.getStaP();
	}
	public boolean getSeachEchappatoire () {
		return this.searchEchappatoire;
	}
	public Task getTaskCurrent () {
		return this.task_current;
	}
	public Task getTaskEchappatoire () {
		return this.task_echappatoire;
	}
	public boolean getFindNodeEchappement () {
		return this.findEchappement;
	}
	public boolean getRecentAltruiste () {
		return this.isRecentAltruiste;
	}
	public boolean getNeedNewCheminBut () {
		return this.needNewCheminBut;
	}
	public boolean getSaveSatisfaction () {
		return this.saveSatisfaction;
	}
	public List<String> getListTabou () {
		return this.list_tabou;
	}

	/**
	 * SETTER
	 */
	public void setEtatSociable (EtatAgent value) {
		this.etat = value;
	}
	public void setSearchEchappatoire (boolean value) {
		this.searchEchappatoire = value;
	}
	public void setFindNodeEchappement (boolean value) {
		this.findEchappement = value;
	}
	public void setRecentAltruiste (boolean value) {
		this.isRecentAltruiste = value;
	}
	public void setNeedNewCheminBut(boolean value) {
		this.needNewCheminBut = value;
	}
	public void setSaveSatisfaction (boolean value) {
		this.saveSatisfaction = value;
	}
	public void setSignalI (SignalInterblocage value) {
		this.signalI = value;
	}
}
