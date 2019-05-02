package eu.su.mas.dedaleEtu.mas.behaviours.yours.Satisfaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction.SignalInterblocage;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction.Satisfaction.EtatAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException; 

/**
 * La classe permet de receptionner tout les messages de type signaux, de faire une purge
 * des signaux anciens et de tenir une base de données des signaux les plus récent.
 * Ainsi que de mettre à jour la satisfaction personnelle et la satisfaction interactionnelle,
 * pour permettre l'aigulage de l'état de l'agent en un comportement égoiste ou altruiste.
 * @author sarah
 */

public class CheckSignauxBoiteLettreBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7842243981203541311L;

	private AgentAbstrait				agent;
	private Integer 						numTransition;
	private ArrayList<SignalInterblocage> 	signals;
	
	/**
	 * Constructeur de l'objet ExploSoloBehaviour
	 * @param agentAbstrait
	 */
	public CheckSignauxBoiteLettreBehaviour(final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent 				=   agentAbstrait;
		this.signals			= 	new ArrayList<SignalInterblocage>();
		this.numTransition		= 	null;
	}	

	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + "<---- est dans CheckSignauxBoiteLettre");	
		this.agent.setStrategie(Strategie.SignalSatisfaction);

		// 0) Re-initialisation des données :
		this.signals.clear();
		
		// 1) Verification si il y a des messages de type Signaux dans la boite aux lettres :
		MessageTemplate msgTemplateEcho = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchProtocol("SIGNAL"));
			
		ACLMessage msgSignalRecu = null;
		do {
			msgSignalRecu = this.agent.receive(msgTemplateEcho);
			if (msgSignalRecu != null) {

				// Maj des signaux Interactifs de la satisfaction de l'agent :
				try {
					SignalInterblocage newSignal = (SignalInterblocage) msgSignalRecu.getContentObject();
					// Affichage :
					//System.out.println(this.agent.getLocalName() + " <---- Reçoit le protocole =  " +
					//		" avec le protocole : " + msgSignalRecu.getProtocol() + " de " +
					//		msgSignalRecu.getSender().getLocalName() + " ,content= \n "+ newSignal);
					// 1er étape : Vérifie si le message n'est pas ancien + MaJ de la base de donnée :
					if (this.agent.getSatisfaction().isValideBdSignal(msgSignalRecu.getSender(), newSignal)) {
						// 2ème étape : Vérifie si l'agent est bien a une distance de 1 arc de l'expéditeur :
						// Verifie si l'agent émétteur de la repulsion est son voisin :
						if (this.isVoisin(newSignal)) {
							// 3ème étape : Vérifie que l'agent se trouve bien sur le chemin du géneur :
							if (newSignal.getCheminBut().contains(this.agent.getCurrentPosition()) == true) {
								// 4 ème étape : Vérifier qu'il ne prenne pas en compte de l'agent déjà pris en compte dans le signal actuel :
								if (this.agent.getSatisfaction().getSignal() == null) {
									this.signals.add(newSignal);
									//System.out.println("-------> Signal == null \n");
								} else {
									String expediteurNew = newSignal.getIdentifiant().getExpediteur();
									String expediteurAncien = this.agent.getSatisfaction().getSignal().getIdentifiant().getExpediteur();
									if (expediteurNew.equals(expediteurAncien) == false) {
										this.signals.add(newSignal);
										//System.out.println("------->signal non en cours\n");
									} else {
										//System.out.println("------->expediteurNew déja pris en compte par expediteurAncien\n");
									}
								}
							} else {
								//System.out.println("-------> Mauvaise direction \n");
							}
						} else {
							//System.out.println("-------> Pas voisin\n");
						}
					} else {
						//System.out.println("-------> Pas isValideBdSignal\n");
					}
				} catch (UnreadableException e) {
					//System.out.println("Le contenu du message ne contient pas un objet SignalInterbocage");
					e.printStackTrace();
				}
			}
		}while(msgSignalRecu != null);
		

		// 2) Mise à jour du signalExt de l'agent + Mise à jour du comportement:
		if (this.signals.isEmpty() == false) {
			// Message(s) reçu(s) :
			// 3) Recupere le Signal le plus prioritaire donc plus intense en valeur non absolu:
			Collections.sort(this.signals, SignalInterblocage.ComparatorSignaux);
			// MaJ du signalI de l'agent :
			this.agent.getSatisfaction().updateSignalInteractif(this.signals.get(0));	
		}
		
		// 3) MaJ de la satisfaction personnelle ayant aucun rapport avec les signaux reçus:
		this.agent.getSatisfaction().run();

				
		EtatAgent	etat	= this.agent.getSatisfaction().getEtatSociabilite();
		if (etat == EtatAgent.altruiste) {
			this.numTransition = AgentExplorateur.T_ALTRUISTE;	
		} else {
			this.numTransition = AgentExplorateur.T_EGOISTE;	
		}
		
		System.out.println("\n1)" + this.agent.getLocalName() + " : \n" + this.agent.getSatisfaction());
	}
	
	/**
	 * L'agent (this) regarde si l'agentX est son voisin :
	 * @return
	 */
	public boolean isVoisin (SignalInterblocage agentX) {
		boolean isVoisin = false;
		//Liste des observables à partir de la position actuelle de l'agent
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs= this.agent.observe();
		//Parcours les observables pour voir si l'agentX est son voisin :
		for (Couple<String,List<Couple<Observation,Integer>>> obs: lobs) {
			if (obs.getLeft().equals(agentX.getPositionCurrent())) {
				isVoisin = true;
			}
		}
		return isVoisin;
	}
	
	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + "----> sort de CheckSignauxBoiteLettre\n");
		return this.numTransition;
	}

}
