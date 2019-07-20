package eu.su.mas.dedaleEtu.mas.knowledge.yours.ProcessusDecisionMarkovien;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentCollecteur;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.CarteTresors.Tresor;
//import gurobi.*;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;


public class PDM implements Serializable {

	private static final long serialVersionUID = 3066405797640478428L;
	private static final Integer bonusExpertise = 4;
	private static final Integer bonusForce = 4;
	private static final Integer bonusNormalisation = 10;
	
	private AgentCollecteur 					agent;
	
	// Position actuel lors du calcul du PDM :
	private String								myPosition;
	
	// L'ensemble des états :
	private HashMap<Integer, Etat> 				S;
	private int									compteurS;
	// L'ensemble des actions :
	private Action								A;
	private int									compteurA;

	// Matrice de transition :
	private Double [][][]						T;
	
	private final Double						gamma;
	
	
	public PDM (final AgentCollecteur myagent, Double gamma) {
		this.agent 		= myagent;
		this.myPosition	= this.agent.getCurrentPosition();
		
		this.S			= new HashMap<Integer, Etat>();
		this.compteurS	= 0;
		this.A          = new Action("Init");
		this.compteurA	= 0;
		this.T			= null;
		
		this.gamma 		= gamma;	
	}
	
	public Action run () {
		// Creation des états du PDM :
		this.createSetState();                             
		// Creation des actions du PDM :
		this.createSetAction();
		// Inserer pour chaque état une récompense :
		this.recompenseTresors();                          
		// Creation de la matrice de Transition sans initialisation:
		this.createMatriceTransition();
		// Inserer les probabilités dans la Matrice de transition pour les trésors :
		this.updateMatriceTransition();
		// Appel de la résolution du PL :
		Action action = this.PL_PDM();
		// Reboot :
		this.compteurS = 0;
		this.compteurA = 0;
		return action;
	}
	
	/**
	 * Creation des états + Attribut à chaque état un identifiant pour la matrice de transition:
	 */
	private void createSetState () {		
		// 1) Creation d'un état position actuelle lors du calcul du PDM :
		Etat etatPosition = new EtatPosition(this.compteurS, this.myPosition);	
		// Insertion dans l'ensemble des états du PDM :
		this.S.put(this.compteurS++, etatPosition);
		
		// 2) Creation des états Trésor :
		// Récupérer l'ensemble des Trésors dans la CarteTresor :
		HashMap<String, Tresor> setTresors = this.agent.getCarteTresors().getCarteTresors();
		// Creation des états Tresors + Insertion dans le PDM :
		for (Map.Entry<String, Tresor> entry : setTresors.entrySet()) {
			Etat etatTresor = new EtatTresor(this.compteurS, entry.getKey(), entry.getValue());
			this.S.put(this.compteurS++, etatTresor);
		}
		
		// 3) Creation des états Tanker :
		if (this.agent.getPositionTanker().isEmpty() == false) {
			// Si l'agent connait des Tankers :
			// Récupérer l'ensemble des Tankers connu + Creation des états Trésors + Insertion dans le PDM :
			for (Map.Entry<String, String> entry : this.agent.getPositionTanker().entrySet()) {
				EtatTanker etatTanker= new EtatTanker(this.compteurS, entry.getKey(), entry.getValue());
				this.S.put(this.compteurS++, etatTanker);
			}
		} else {
			// Abandoner le PDM et rechercher un Tanker au moins !
			System.out.println("-----------------------Oh zut il n'y a pas de tanker");
		}
		
		// 4) Creation de l'état Vide + insertion de BD des etats :
		Etat etatVide	= new EtatVide(this.compteurS);
		this.S.put(this.compteurS++, etatVide);
	}
	
	/**
	 * Creation de l'ensembles des actions du PDM :
	 */
	private void createSetAction () {
		for (Map.Entry<Integer, Etat> entry : this.S.entrySet()) {
			// Si l'etat est un Tresor, il est vue comme une action :
			if (entry.getValue() instanceof EtatTresor)
				this.A.addActionTresor(new ActionTresor(this.compteurA++,(EtatTresor)entry.getValue()));
			if (entry.getValue() instanceof EtatTanker)
				this.A.addActionTanker(new ActionTanker(this.compteurA++, (EtatTanker)entry.getValue()));
		}
	}
	
	/**
	 * MaJ des recompenses pour chaque etat du PDM :
	 */
	private void recompenseTresors () {
		// Récupérer la plus grande distance au trésor :
		int distMax = -1;
		for (ActionTresor tresor: this.A.getActionsTresors()) {
			// Calcul le chemin pour aller de ma Position au Trésor :
			List<String> chemin = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition(), 
					tresor.getEtatTresor().getPositionTresor());
			if (chemin.size() > distMax) {
				distMax = chemin.size();
			}
		}
		
		// Faire la somme des trésors du type de l'agent :
		int sommeTresor = 0;
		for (ActionTresor tresor: this.A.getActionsTresors()) {
			if (tresor.getEtatTresor().getTresor().getTypeTresor().equals(this.agent.getMyTreasureType()) == true) {
				sommeTresor += tresor.getEtatTresor().getTresor().getRessource();
			}
		}
		
		// Récupérer la plus grande distance au trésor :
		int distMaxTanker = -1;
		for (ActionTanker tanker: this.A.getActionsTankers()) {
			// Calcul le chemin pour aller de ma Position au Trésor :
			List<String> chemin = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition(), 
					tanker.getEtatTanker().getPositionTanker());
			if (chemin.size() > distMaxTanker) {
				distMaxTanker = chemin.size();
			}
		}
		
		// Creation du dictionnaire de recompense : S = <Position, Tresor, Tanker>
		for (Map.Entry<Integer, Etat> entry : this.S.entrySet()) {
			// Declaration d'un score nul :
			int score = 0;
			
			// Si l'etat est un Etat de type EtatPosition :
			if (entry.getValue() instanceof EtatPosition) {
				score = 0;
			}
			
			// Si l'etat est un Etat de type EtatVide :
			if (entry.getValue() instanceof EtatVide) {
				score = 0;
			}
			
			// Si l'etat est un Etat de type EtatTresor :
			if (entry.getValue() instanceof EtatTresor) {
				EtatTresor etatTresor = (EtatTresor)entry.getValue();
				// Est-ce l'agentCollecteur peut collecter ce type de trésor :
				if (this.agent.getMyTreasureType() == etatTresor.getTresor().getTypeTresor()) {
					// L'agent peut collecter se type de trésor :
					// Est-ce que l'agentCollecteur peut récuperer le trésor en 1 pick :					
					// Calculer le nombre de pick pour récupérer le trésor en entier :
					int nbPick = 0;
					if (this.agent.getBackPackFreeSpace() != 0) {
						// Combien de pick pour récupérer le tresor :
						System.out.println(etatTresor.getname() + " " + nbPick);
						System.out.println(etatTresor.getTresor().getRessource() + " " + this.agent.getBackPackFreeSpace());
						nbPick = etatTresor.getTresor().getRessource() / this.agent.getBackPackFreeSpace();
						if (nbPick == 0) {
							nbPick = 1;
						}
						score += (((etatTresor.getTresor().getRessource()/sommeTresor)*PDM.bonusNormalisation) / nbPick);

					} else {
						nbPick = 0;
					}
					// Score en fonction du nombre de pick :
					if (etatTresor.getTresor().getLockStatus() == 0) {
						// Est-ce qu'il a assez d'expertise et de force : 
						Set<Couple<Observation,Integer>> competence = this.agent.getMyExpertise();
						for (Couple<Observation,Integer> couple: competence) {
							if (couple.getLeft() == Observation.STRENGH) {
								if (couple.getRight() >= etatTresor.getTresor().getStrengh()) {
									// L'agent a assez de force :
									score += PDM.bonusForce * etatTresor.getTresor().getStrengh();
								}
							}
							if (couple.getLeft() == Observation.LOCKPICKING) {
								if (couple.getRight() >= etatTresor.getTresor().getStrengh()) {
									// L'agent a assez de force :
									score += PDM.bonusExpertise * etatTresor.getTresor().getLockpicking();
								}
							}
						}
					} else {
						score += PDM.bonusForce + PDM.bonusExpertise;
					}
					// Est-ce que le trésor est loin :
					// Calcul le score de la distance en fonction du plus long chemin vers un trésor :
					List<String> chemin = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition(), 
							etatTresor.getPositionTresor());
					score += (distMax - chemin.size());
				} 
			} 
			
			// Si l'etat est un Etat de type EtatTanker :
			if (entry.getValue() instanceof EtatTanker) {
				EtatTanker etatTanker = (EtatTanker)entry.getValue();	
				
				// Est-ce que le Tanker est loin de ma position :
				List<String> cheminTanker = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition(), 
						etatTanker.getPositionTanker());
				score += (distMaxTanker - cheminTanker.size()) + 1; //
			} 
			
			// MaJ de la recompense de l'état :
			entry.getValue().setRecompense(score);
		}
	}
	
	/**
	 * Creation d'une matrice de transition :
	 */
	private void createMatriceTransition () {
		this.T = new Double [this.S.size()][][];
		// Declaration de la matrice 3D (state x state x action) de valeur 0.:
		for (int i=0; i < this.S.size(); i++) {
			this.T[i] = new Double [this.S.size()][];
			for (int j=0; j < this.S.size(); j++) {
				this.T[i][j] = new Double [this.A.size()];
				for (int k=0; k < this.A.size(); k++) {
					this.T[i][j][k] = 0.0;
				}
			}
		}
	}
	
	/**
	 * Maj de la matrice de transition T à partir des trésors présent sur la carte :
	 */
	private void updateMatriceTransition() {
		// Récupère l'ensemble des trésors de type ActionTrésor :
		ArrayList<ActionTresor> actionTresors = new ArrayList<ActionTresor> (this.A.getActionsTresors());

		// Trésors à prendre en compte pour le trie :
		ArrayList<ActionTresor> tresorsValides = new ArrayList<ActionTresor> ();
		
		// Supprimer l'ensemble des Trésors étant impossible à collecter pour l'agent :
		for (ActionTresor tresor: actionTresors) {
			boolean isValide = true;
			// Si le type du trésor ne correspond pas :
			if (this.agent.getMyTreasureType() != tresor.getEtatTresor().getTresor().getTypeTresor())
				isValide = false;
			// Si mes compétences ne correspondent pas :
			if (tresor.getEtatTresor().getTresor().getLockStatus() == 0) {
				// Si le couvre est fermé
				Set<Couple<Observation,Integer>> competence = this.agent.getMyExpertise();
				for (Couple<Observation,Integer> couple: competence) {
					if (couple.getLeft() == Observation.STRENGH) {
						if (couple.getRight() < tresor.getEtatTresor().getTresor().getStrengh()) {
							isValide = false;
						} else {
							//System.out.println("J'ai la force");
						}
					}
					if (couple.getLeft() == Observation.LOCKPICKING) {
						if (couple.getRight() < tresor.getEtatTresor().getTresor().getLockpicking()) {
							isValide = false;
						} 
					}
				}
			}
			
			if (this.agent.getBackPackFreeSpace() == 0) {
				isValide = false;
			}
			
			if (isValide == true) {
				// Si toute les conditions ne sont pas vérifiées alors je l'ajoute dans les trésors valides :
				tresorsValides.add(tresor);
			}
		}
				
		// Trier la liste selon une date des plus recentes à la plus anciennes :
		Collections.sort(tresorsValides, ActionTresor.ComparatorActionTresor); // voir si l'ordre est correct !!!

		// Récupèrer le nombre de trésor valide :
		Integer nbTresors = tresorsValides.size();
		
		if (nbTresors > 0) {
			// Somme :
			Double somme = 0.;
			for (int i=1; i <= nbTresors; i++) {
				somme += i;
			}
			// Modifie directement dans la matrice de transition + Normalisation :
			for (ActionTresor tresor: tresorsValides) {
				this.T[0][tresor.getEtatTresor().getId()][tresor.getIdAction()] = (nbTresors / somme);
				nbTresors--;
			}
		} 
		
		// Rechercher l'etat vide :
		EtatVide s_vide = null;
		for (Map.Entry<Integer, Etat> entry : this.S.entrySet()) {
			if (entry.getValue() instanceof EtatVide) {
				s_vide = (EtatVide)entry.getValue() ;
			}
		}
		// Declaration de la matrice 3D (state x state x action) de valeur 0.:
		// Pour chaque Etat i:
		for (int i=0; i < this.S.size(); i++) {
			// Pour chaque Etat j:
			for (int j=0; j < this.S.size(); j++) {
				// Récupère chaque etat correspondant au identifiant de la matrice (EtatPosition/EtatTanker/EtatTresor/EtatVide):
				Etat s_i = this.S.get(i);
				Etat s_j = this.S.get(j);
				// Pour chaque Action :
				for (int k=0; k < this.A.size(); k++) {
					Action a = this.A.getActions().get(k);
					// Si on va de Position vers un trésor et que l'action est d'aller vers un trésor :
					if (s_i instanceof EtatPosition && s_j instanceof EtatTresor && a instanceof ActionTresor) {
						// Si l'etat trésor corresponds a action de se trésor : 
						if (s_j.equals(((ActionTresor)a).getEtatTresor())) { // Attention au equals ??????? 
							// Je mets la (1 - proba de ce trésor dans l'état vide) :
							this.T[i][s_vide.getId()][a.getIdAction()] = 1 - this.T[i][j][k];
						}
					}
					// Si on va de Position vers un Tanker et que l'action est d'aller vers un Tanker :
					if (s_i instanceof EtatPosition && s_j instanceof EtatTanker && a instanceof ActionTanker) {
						// Si l'etat tanker corresponds a action de se tanker : 
						if (s_j.equals(((ActionTanker)a).getEtatTanker())) {
							this.T[i][j][k] = 1.;
						}
					}
				}
			}
		}
	}
	
	private Action PL_PDM () {
		Action bestAction 	= null;
        try {
			IloCplex cplex = new IloCplex();
			
			// Create variables decisions :
	        IloNumVar[] V = new IloNumVar[this.S.size()];
	        for (int i=0; i < this.S.size(); i++) {
	        	V[i] = cplex.numVar(Double.MIN_VALUE, Double.MAX_VALUE, IloNumVarType.Float, "V["+i+"]");
	        }
	
			// Set objective : 
	        cplex.addMinimize(cplex.sum(V));
	        
           // Add constraint : 
           for (int s=0; s < this.S.size(); s++) {
        	   // Pour tout action a :
        	   for (int a=0; a < this.A.size(); a++) {
        		   // V(s) - gamma*(Som(T(s,a,sBis)*V(sBis))) >= R(s,a)
        		   IloLinearNumExpr expr = cplex.linearNumExpr();
        		   expr.addTerm(1., V[s]);
        		   
        		   for (int sBis=0; sBis < this.S.size(); sBis++) {
        			   expr.addTerm((-this.gamma)*this.T[s][sBis][a], V[sBis]);
        		   }
        		   
    	           cplex.addGe(expr, this.S.get(s).getRecompense(), "V["+this.S.get(s).getname()+"]");
        	   }
           }
            
           cplex.exportModel("PDM.lp");
	        
	       if (cplex.solve()) {
		        // Récupérer la décision (action) que nous devons prendre à l'état Position :
				// Recherche l'indexPosition :
				Integer indexPosition = null;
				for (Map.Entry<Integer, Etat> entry : this.S.entrySet()) {
					if (entry.getValue() instanceof EtatPosition) {
						indexPosition = ((EtatPosition)entry.getValue()).getId() ;
					}
				}
				
				// Save l'action avec la valeur de la décision :
				HashMap<Action, Double> bd_value = new HashMap<Action, Double>();
			
				// Calculer la meilleurs action/décision lorsqu'on est en P :
				Double choiceValue = 0.;
				for (int a = 0; a < this.A.size(); a++) {
					choiceValue = this.S.get(indexPosition).getRecompense() + 0.;
					Double som = 0.;
					for (int sBis=0; sBis < this.S.size(); sBis++) {
						som += this.T[indexPosition][sBis][a] * cplex.getValue(V[sBis]);
					}
					choiceValue += this.gamma*som;
					bd_value.put(this.A.getActions().get(a), choiceValue);
				}
				// Récupérer la meilleurs action à faire :
				Double bestValue	= Double.MIN_VALUE; // Attention 
				for (Map.Entry<Action, Double> entry : bd_value.entrySet()) {
					if (entry.getValue() > bestValue) {
						bestAction = entry.getKey();
						bestValue  = entry.getValue();
					}
				}
	       }
           cplex.end();
           return  bestAction;
		} catch (IloException e) {
			e.printStackTrace();
		}
		return bestAction;
	}

	
	public String toString () {
		String s = "Matrice de Transition : \n";
		return s;
	}
}


/*//Recupère le nombre de noeud dans le graph :
int nbNodesGaph = this.agent.getCarteExploration().getGraph().getNodeCount();
// Calcul le chemin pour aller de ma Position au Trésor :
List<String> chemin = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition(), 
		etatTanker.getPositionTanker());
// Plus il est près plus il aura de point:
score += nbNodesGaph - (chemin.size() + 1); // Revoir pour la récompense entre Tanker // MODIFIER */


/*try {
System.out.println("JE SUIS PDM !!!!!!!!!!!!!!!!!!!!!");

// Create empty environment , set options , and start
GRBEnv env = new GRBEnv ();
//((GRBEnv)env).start();

// Create empty model
GRBModel model = new GRBModel (env);

// Create variables decisions
GRBVar[] V = new GRBVar[this.S.size()];
for (int i = 0; i < this.S.size(); i++) {
	V[i] = 	model.addVar(-GRB.INFINITY, GRB.INFINITY, 0., GRB.CONTINUOUS, "V[" + i + "]") ; // Attention
}			

// Set objective : 
GRBLinExpr expr = new GRBLinExpr ();
for (int i = 0; i < this.S.size(); i++) {
	expr.addTerm (1.0, V[i]);
}
model.setObjective (expr , GRB.MINIMIZE);

// Add constraint : 
for (int s=0; s < this.S.size(); s++) {
	// Pour tout action a :
	for (int a=0; a < this.A.size(); a++) {
		// V(s) - gamma*(Som(T(s,a,sBis)*V(sBis))) >= R(s,a)
		GRBLinExpr exprG = new GRBLinExpr ();
		exprG.addTerm(1., V[s]);
		
		GRBLinExpr exprSomme = new GRBLinExpr ();		
		for (int sBis=0; sBis < this.S.size(); sBis++) {
			exprSomme.addTerm(this.T[s][a][sBis] , V[sBis]);
		}
		exprG.multAdd(-this.gamma, exprSomme);
		
		model.addConstr (exprG, GRB.GREATER_EQUAL , this.S.get(s).getRecompense() , "c");
	}
}

// Optimize model
model.optimize ();

for (int i = 0; i < this.S.size(); i++) {
	System.out.println (V[i].get(GRB.StringAttr.VarName) + " " + V[i].get(GRB.DoubleAttr.X));
}	
System.out.println ("Obj : " + model.get(GRB.DoubleAttr.ObjVal));

// Récupérer la décision (action) que nous devons prendre à l'état Position :
// Recherche l'indexPosition :
Integer indexPosition = null;
for (Map.Entry<Integer, Etat> entry : this.S.entrySet()) {
	if (entry.getValue() instanceof EtatPosition) {
		indexPosition = ((EtatPosition)entry.getValue()).getId() ;
	}
}

// Save l'action avec la valeur de la décision :
HashMap<Action, Double> bd_value = new HashMap<Action, Double>();

// Calculer la meilleurs action/décision lorsqu'on est en P :
Double choiceValue = 0.;
for (int a = 0; a < this.A.size(); a++) {
	choiceValue = this.S.get(indexPosition).getRecompense() + 0.;
	Double som = 0.;
	for (int sBis=0; sBis < this.S.size(); sBis++) {
		som += this.T[indexPosition][a][sBis] * V[sBis].get(GRB.DoubleAttr.X);
	}
	choiceValue += this.gamma*som;
	bd_value.put(this.A.getActions().get(a), choiceValue);
}

// Récupérer la meilleurs action à faire :
Action bestAction 	= null;
Double bestValue	= Double.MIN_VALUE; // Attention 
for (Map.Entry<Action, Double> entry : bd_value.entrySet()) {
	if (entry.getValue() > bestValue) {
		bestAction = entry.getKey();
		bestValue  = entry.getValue();
	}
}

// Dispose of model and environment
model.dispose();
env.dispose();

return bestAction;
} catch ( GRBException e) {
System.out.println (" Error code : " + e.getErrorCode() + ". " + e.getMessage ());
}*/
