package eu.su.mas.dedaleEtu.mas.behaviours.yours;

import java.util.List;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentCollecteur;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentTanker;
import jade.core.behaviours.OneShotBehaviour;

/***
 * Arbre de décision 
 * author sarah
 */
public class PlanificationBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1665127700523671496L;
	
	AgentAbstrait			agent;
	private Integer				numeroTransition;

	public PlanificationBehaviour(final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent = agentAbstrait;
	}

	@Override
	public void action() {
		
		// Si je suis un explorateur :
		if (this.getAgent().getClass() == AgentExplorateur.class) {
			AgentExplorateur agentExplorer = (AgentExplorateur)this.getAgent();
			
			// Recherche de son etat de comportement avant de rentrer en planification :
			Strategie		etat = agentExplorer.getStrategie();
			switch(etat) {
			case Exploration :
				// Si je suis toujours en état d'exploration :
				if (this.agent.getExploration() == true) {	
					// Si l'agent a pu faire son mouvement :
					if (this.agent.getIsMove() == true) {	
						// Mettre à jour la variable d'interblocage :
						this.agent.rebootInterblocage();
						// Est-ce que l'agent est doit entrer en mode EchoFlowdingMap par compteur :
						if (this.isActiveEchoMap() == true) {
							// Reboot le compteur d'EchoMap :
							this.agent.setRebootEchoMap();
							// Si EchoMap actif :
							//this.agent.setCptEchoMapIncrementer();
							this.numeroTransition = AgentExplorateur.T_CHECK_ECHO_BOITE_LETTRE;
						} else {
							// Si EchoMap non actif :
							// Verifier si on a reçu un EchoMap entre temps :
							if (this.agent.getCheckEcho() == false) {
								// Si on n'a pas encore fait de vérification :
								// On n'incremente pas le compteur d'EchoMap :
								// Mettre la verife d'echoMap en mode actif:
								this.agent.setCheckEcho(true);
								this.numeroTransition = AgentExplorateur.T_CHECK_ECHO_BOITE_LETTRE;
							} else {
								// Si on a déjà une vérification précédemment :
								// Incrementer le compteur de EchoMap :
								this.agent.setCptEchoMapIncrementer();
								// Reboot :
								this.agent.setCheckEcho(false);
								this.numeroTransition = AgentExplorateur.T_CHECK_SIGNAUX_BOITE_LETTRE;
							}
						}
					} else {
						// L'agent n'a pas pu bouger:
						this.numeroTransition = AgentExplorateur.P_SEND_MAP;
					}
				} else {
					// L'exploration est terminé (Fin de mission) :
					this.agent.setDegreeNode(this.agent.getCarteExploration().getGraph());
					this.numeroTransition = AgentExplorateur.T_TRESOR_COLLECTIF_EXPLO;
				}
				break;
			case EchoFlowdingMap :
				if (this.agent.getCheckEcho() == true) {
					this.agent.setCptEchoMapIncrementer();
					this.agent.setCheckEcho(false);
				} else {
					// Reboot le compteur d'EchoMap :
					this.agent.setRebootEchoMap();
				}
				// Reboot le compteur d'EchoMap :
				//this.agent.setRebootEchoMap();
				// Il y a eu des changements dans la carte après agrégation :
				if (this.agent.getCarteExploration().getChangeMap() == true) {
					this.searchNodesBut();
				} else {
					// Il n'y a pas eu de changement :
					this.numeroTransition = AgentExplorateur.T_CHECK_SIGNAUX_BOITE_LETTRE;
				}
				break;
			case SendMap :
				// Il y a eu des changements dans la carte après agrégation :
				if (this.agent.getCarteExploration().getChangeMap() == true) {
					this.searchNodesBut();
				} else {
					// Il n'y a pas eu de changement :
					this.numeroTransition = AgentExplorateur.T_CHECK_SIGNAUX_BOITE_LETTRE;
				}
				break;
			case SignalSatisfaction :
				// Est-ce que je suis en Exploration :
				if (this.agent.getExploration() == true) {
					//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je suis en Exploration");
					// Oui, donc on continue l'Exploration :
					this.numeroTransition = AgentAbstrait.P_EXPLORATION;
				} else {
					//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je ne suis plus en Exploration");
					// Non, je ne suis plus en Exploration :
					// Est-ce que je suis en explorationTresorPerdu :
					if (this.agent.getExplorationTresorPerdu() == false) {
						//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je ne suis pas en Exploration de trésor Perdu");
						// Non, je ne suis pas en Exploration Tresor Perdu :
						// Est-ce que je suis en action Collectif :
						if (this.agent.getSearchTresorCollectif() == true) {
							//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je suis en recherche de Tresor Collectif");
							// Oui, je suis en actionTresorCollectif :
							this.numeroTransition = AgentExplorateur.T_ACTION_TRESOR_COLLECTIF_AFTER_PLANIFICATION;
						} else {
							//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je vais faire un Recherche Tresor Collectif");
							// Non, je ne suis pas en action collectif :
							this.numeroTransition = AgentExplorateur.T_TRESOR_COLLECTIF_EXPLO;
						}
					} else {
						//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je suis en Exploration de trésor Perdu");
						// Oui, Je suis en Exploration de trésor Perdu :
						// Est-ce que je connais assez de tanker sur la Map :
						if (this.agent.getPositionTanker().size() >= this.agent.getNbNodeVisitedSilo()) {
							//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je connais assez de Tanker");
							// Oui, je connais assez de tanker :
							// Est-ce que j'ai atteinds le quota de No à visité :
							if (this.agent.getCptNodeVisited() % this.agent.getIntervalleNodeExploTresor() == 0) {
								//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je suis en intervalle Exploration Tresor Perdu");
								// J'ai atteinds le nombre de node à visiter, aller faire la tournée des tankers:
								// Si j'ai visité tout les tanker :
								if (this.agent.getTourTanker().size() == 0) {
									//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> J'ai fini mon tour");
									this.agent.rebootCptNodeVisited();
									this.agent.setTourTanker(this.agent.getPositionTanker());
									this.numeroTransition = AgentAbstrait.P_P;
								} else {
									//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je continue mon tour");
									this.numeroTransition = AgentAbstrait.T_TOUR_TANKER;
								}
							} else {
								//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je ne suis pas en intervalle Exploration Tresor Perdu ");
								// Si J'ai trouvé au moins un trésor, je vais informer les agent Silo :
								if (/*this.isTresorAOuvrir()*/this.agent.getTresorPerdu() == true) {		
									//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> J'ai trouvé un trésor ");
									// Je ne peux pas ouvrir le tresor :
									if (this.agent.getTourTanker().size() == 0) {
										//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> J'ai fini mon tour ");
										this.agent.rebootCptNodeVisited();
										this.agent.setTourTanker(this.agent.getPositionTanker());
										this.agent.setExplorationTresorPerdu(false);
										this.agent.setTresorPerdu(false);
										this.numeroTransition = AgentAbstrait.P_P;
									} else {
										//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je continue mon tour ");
										this.numeroTransition = AgentAbstrait.T_TOUR_TANKER;
									}
								} else {
									//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je continue mon Exploration Tresor Perdu");
									// J'ai pas trouvé de trésor :
									this.numeroTransition = AgentCollecteur.T_EXPLO_TRESOR_PERDU;
								}
							}
						} else {
							// Je ne connais pas assez de tanker :
							//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je n'ai pas assez de tanker, recherche les Silos ");
							this.numeroTransition = AgentAbstrait.P_RECHERCHE_SILO;
						}
					}
				}
				break;
			default:
				break;
			}
		}
		
		/*********************************************************************************************/
		/*******************************     AGENT COLLECTEUR   **************************************/
		/*********************************************************************************************/
		
		if (this.getAgent().getClass()  == AgentCollecteur.class) {
			AgentCollecteur agentCollecteur = (AgentCollecteur)this.getAgent();
			
			// Recherche de son etat de comportement avant de rentrer en planification :
			Strategie		etat = agentCollecteur.getStrategie();
			switch(etat) {
			case Exploration :
				// Si je suis toujours en état d'exploration :
				if (agentCollecteur.getExploration() == true) {	
					// Tant que l'agent Collecteur est en exploration on peut mettre à jour la capacité de son sac :
					if (agentCollecteur.getCapacityBag() == 0) {
						agentCollecteur.updateCapacitySac(this.agent.getBackPackFreeSpace());
					}

					// Si l'agent a pu faire son mouvement :
					if (agentCollecteur.getIsMove() == true) {	
						// Mettre à jour la variable d'interblocage :
						agentCollecteur.rebootInterblocage();
						// Est-ce que l'agent est doit entrer en mode EchoFlowdingMap par compteur :
						if (this.isActiveEchoMap() == true) {
							// Reboot le compteur d'EchoMap :
							this.agent.setRebootEchoMap();
							// Si EchoMap actif :
							//agentCollecteur.setCptEchoMapIncrementer();

							this.numeroTransition = AgentCollecteur.T_CHECK_ECHO_BOITE_LETTRE;
						} else {
							// Si EchoMap non actif :
							// Verifier si on a reçu un EchoMap entre temps :
							if (agentCollecteur.getCheckEcho() == false) {
								// Si on n'a pas encore fait de vérification :
								// On n'incremente pas le compteur d'EchoMap :
								// Mettre la verife d'echoMap en mode actif:
								agentCollecteur.setCheckEcho(true);
								this.numeroTransition = AgentCollecteur.T_CHECK_ECHO_BOITE_LETTRE;
							} else {
								// Si on a déjà une vérification précédemment :
								// Incrementer le compteur de EchoMap :
								agentCollecteur.setCptEchoMapIncrementer();
								// Reboot :
								agentCollecteur.setCheckEcho(false);
								this.numeroTransition = AgentCollecteur.T_CHECK_SIGNAUX_BOITE_LETTRE;
							}
						}
					} else {
						// L'agent n'a pas pu bouger:
						this.numeroTransition = AgentCollecteur.P_SEND_MAP;
					}
				} else {
					// L'exploration est terminé (Fin de mission) :
					this.agent.setDegreeNode(this.agent.getCarteExploration().getGraph());
					this.numeroTransition = AgentCollecteur.T_PDM;
				}
				break;
			case EchoFlowdingMap :
				if (this.agent.getCheckEcho() == true) {
					this.agent.setCptEchoMapIncrementer();
					this.agent.setCheckEcho(false);
				} else {
					// Reboot le compteur d'EchoMap :
					this.agent.setRebootEchoMap();
				}
				// Reboot le compteur d'EchoMap :
				//agentCollecteur.setRebootEchoMap();
				// Il y a eu des changements dans la carte après agrégation :
				if (agentCollecteur.getCarteExploration().getChangeMap() == true) {
					this.searchNodesBut();
				} else {
					// Il n'y a pas eu de changement :
					this.numeroTransition = AgentCollecteur.T_CHECK_SIGNAUX_BOITE_LETTRE;
				}
				break;
			case SendMap :
				// Il y a eu des changements dans la carte après agrégation :
				if (agentCollecteur.getCarteExploration().getChangeMap() == true) {
					this.searchNodesBut();
				} else {
					// Il n'y a pas eu de changement :
					this.numeroTransition = AgentCollecteur.T_CHECK_SIGNAUX_BOITE_LETTRE;
				}
				break;
			case SignalSatisfaction :
				// Est-ce que je suis en Exploration :
				if (this.agent.getExploration() == true) {
					//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je suis en Exploration");
					// Oui, donc on continue l'Exploration :
					this.numeroTransition = AgentCollecteur.P_EXPLORATION;
				} else {
					//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je ne suis plus en Exploration");
					// Non, je ne suis plus en Exploration :
					// Est-ce que je suis en explorationTresorPerdu :
					if (this.agent.getExplorationTresorPerdu() == false) {
						//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je ne suis pas en Exploration de trésor Perdu");
						// Non, je ne suis pas en Exploration Tresor Perdu :
						// Est-ce que je suis en action Collectif :
						if (this.agent.getSearchTresorCollectif() == true) {
							//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je suis en recherche de Tresor Collectif ");
							// Oui, je suis en actionTresorCollectif :
							this.numeroTransition = AgentCollecteur.T_ACTION_TRESOR_COLLECTIF_AFTER_PLANIFICATION;
						} else {
							//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je vais faire un PDM");
							// Non, je ne suis pas en action collectif :
							this.numeroTransition = AgentCollecteur.T_PDM;
						}
					} else {
						//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je suis en Exploration de trésor Perdu");
						// Oui, Je suis en Exploration de trésor Perdu :
						// Est-ce que je connais assez de tanker sur la Map :
						if (this.agent.getPositionTanker().size() >= this.agent.getNbNodeVisitedSilo()) {
							//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je connais assez de Tanker");
							// Oui, je connais assez de tanker :
							// Est-ce que j'ai atteinds le quota de No à visité :
							if (this.agent.getCptNodeVisited() % this.agent.getIntervalleNodeExploTresor() == 0) {
								//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je suis en intervalle Exploration Tresor Perdu");
								// J'ai atteinds le nombre de node à visiter, aller faire la tournée des tankers:
								// Si j'ai visité tout les tanker :
								if (this.agent.getTourTanker().size() == 0) {
									//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> J'ai fini mon tour");
									this.agent.rebootCptNodeVisited();
									this.agent.setTourTanker(this.agent.getPositionTanker());
									this.numeroTransition = AgentAbstrait.P_P;
								} else {
									//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je continue mon tour");
									this.numeroTransition = AgentAbstrait.T_TOUR_TANKER;
								}
							} else {
								//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je ne suis pas en intervalle Exploration Tresor Perdu ");
								// Si J'ai trouvé au moins un trésor, je vais informer les agent Silo :
								if (/*this.isTresorAOuvrir()*/this.agent.getTresorPerdu() == true) {		
									//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> J'ai trouvé un trésor ");
									// Je ne peux pas ouvrir le tresor :
									if (this.agent.getTourTanker().size() == 0) {
										//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> J'ai fini mon tour ");
										this.agent.rebootCptNodeVisited();
										this.agent.setTourTanker(this.agent.getPositionTanker());
										this.agent.setExplorationTresorPerdu(false);
										this.agent.setTresorPerdu(false);
										this.numeroTransition = AgentAbstrait.P_P;
									} else {
										//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je continue mon tour ");
										this.numeroTransition = AgentAbstrait.T_TOUR_TANKER;
									}
								} else {
									//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je continue mon Exploration Tresor Perdu");
									// J'ai pas trouvé de trésor :
									this.numeroTransition = AgentCollecteur.T_EXPLO_TRESOR_PERDU;
								}
								/*} else {
									//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je continue mon Exploration Tresor Perdu");
									// J'ai pas trouvé de trésor :
									this.numeroTransition = AgentCollecteur.T_EXPLO_TRESOR_PERDU;*/
								//}
							}
						} else {
							// Je ne connais pas assez de tanker :
							//System.out.println("\n*******************> " + this.agent.getLocalName() + " ----> Je recherche les Silos ");
							this.numeroTransition = AgentAbstrait.P_RECHERCHE_SILO;
						}
					}
				}
				break;
			default:
				break;
			}
		}
		
		/*********************************************************************************************/
		/*******************************         AGENT TANKER       **********************************/
		/*********************************************************************************************/
		if (this.getAgent().getClass() == AgentTanker.class) {
			AgentTanker agentTanker = (AgentTanker)this.getAgent();
						
			// Recherche de son etat de comportement avant de rentrer en planification :
			Strategie		etat = agentTanker.getStrategie();
			switch(etat) {
			case Exploration :
				// Si je suis toujours en état d'exploration :
				if (this.agent.getExploration() == true) {	
					// Si l'agent a pu faire son mouvement :
					if (this.agent.getIsMove() == true) {	
						// Mettre à jour la variable d'interblocage :
						this.agent.rebootInterblocage();
						// Est-ce que l'agent est doit entrer en mode EchoFlowdingMap par compteur :
						if (this.isActiveEchoMap() == true) {
							// Reboot le compteur d'EchoMap :
							this.agent.setRebootEchoMap();
							// Si EchoMap actif :
							//this.agent.setCptEchoMapIncrementer();

							this.numeroTransition = AgentTanker.T_CHECK_ECHO_BOITE_LETTRE;
						} else {
							// Si EchoMap non actif :
							// Verifier si on a reçu un EchoMap entre temps :
							if (this.agent.getCheckEcho() == false) {
								// Si on n'a pas encore fait de vérification :
								// On n'incremente pas le compteur d'EchoMap :
								// Mettre la verife d'echoMap en mode actif:
								this.agent.setCheckEcho(true);
								this.numeroTransition = AgentTanker.T_CHECK_ECHO_BOITE_LETTRE;
							} else {
								// Si on a déjà une vérification précédemment :
								// Incrementer le compteur de EchoMap :
								this.agent.setCptEchoMapIncrementer();
								// Reboot :
								this.agent.setCheckEcho(false);
								this.numeroTransition = AgentTanker.T_CHECK_SIGNAUX_BOITE_LETTRE;
							}
						}
					} else {
						// L'agent n'a pas pu bouger:
						this.agent.setDegreeNode(this.agent.getCarteExploration().getGraph());
						this.numeroTransition = AgentTanker.P_SEND_MAP;
					}
				} else {
					// L'exploration est terminé (Fin de mission) :
					// Recherche d'une position Silo pour le Tanker + modification du chemin but :
					this.searchPositionSilo(agentTanker);
					this.numeroTransition = AgentTanker.T_ITINERAIRE_SILO;
				}
				break;
			case EchoFlowdingMap :
				if (this.agent.getCheckEcho() == true) {
					this.agent.setCptEchoMapIncrementer();
					this.agent.setCheckEcho(false);
				} else {
					// Reboot le compteur d'EchoMap :
					this.agent.setRebootEchoMap();
				}
				// Reboot le compteur d'EchoMap :
				//this.agent.setRebootEchoMap();
				// Il y a eu des changements dans la carte après agrégation :
				if (this.agent.getCarteExploration().getChangeMap() == true) {
					this.searchNodesBut();
				} else {
					// Il n'y a pas eu de changement :
					this.numeroTransition = AgentTanker.T_CHECK_SIGNAUX_BOITE_LETTRE;
				}
				break;
			case SendMap :
				// Il y a eu des changements dans la carte après agrégation :
				if (this.agent.getCarteExploration().getChangeMap() == true) {
					this.searchNodesBut();
				} else {
					// Il n'y a pas eu de changement :
					this.numeroTransition = AgentTanker.T_CHECK_SIGNAUX_BOITE_LETTRE;
				}
				break;
			case SignalSatisfaction :
				if (this.agent.getExploration() == true) {
					this.numeroTransition = AgentTanker.P_EXPLORATION;
				} else {
					this.numeroTransition = AgentTanker.T_ITINERAIRE_SILO;
				}
				break;
			case IntinerairePositionSilo :
				this.numeroTransition = AgentTanker.T_CHECK_MAP_ENV;
				break;
			case CheckConfirmationPositionSilo :
				if (agentTanker.getCompteurSendPosition() % agentTanker.getIntervalleSendPositionSilo() == 0) {
					this.numeroTransition = AgentTanker.T_SEND_POSITION_SILO;
					agentTanker.setResetCompteurSilo();
				} else {
					this.numeroTransition = AgentTanker.T_CHECK_SIGNAUX_BOITE_LETTRE;
					agentTanker.incrementeCompteurSilo();
				}
				break;
			case SendPositionSilo :
				this.numeroTransition = AgentTanker.T_RECEIVE_CONFIRMATION_POSITION;
				break;
			case ReceiveConfirmationPositionSilo :
				this.numeroTransition = AgentTanker.T_CHECK_SIGNAUX_BOITE_LETTRE;
				break;
			case CheckReceiveMapEnv :
				this.numeroTransition = AgentTanker.T_CHECK_CONFIRMATION_SILO;
				break;
			default:
				break;
			}
		}
		
	}
	
	/*Utiliser que par les agents Silo :*/
	private void searchPositionSilo (AgentTanker agentTanker) {
		// Recherche dans le graphe les noeuds de plus haut degré :
		//List<Node> degreeNode = Toolkit.degreeMap(this.agent.getCarteExploration().getGraph());
		// Recupérer le noeud du plus au dégrée pour le tanker (this) en fonction de son identifiant :
		//Node positionTanker =  degreeNode.get(agentTanker.getIdTanker());
		// Mettre à jour la position Tanker :
		agentTanker.setPositionSilo(this.agent.getDegreeNode().positionTanker(agentTanker.getIdTanker()));//positionTanker.getId());
		// Calculer le chemin pour aller de la position current à la position Tanker :
		List<String> cheminBut = this.agent.getCarteExploration().getShortestPath(this.agent.getCurrentPosition(), agentTanker.getPositionSilo());
		// Mettre à jour le nouveau cheminBut :
		this.agent.setCheminPlanification(cheminBut);
	}
	
	private void searchNodesBut () {
		// Tester si l'exploration est terminé :
		if (this.agent.getCarteExploration().getNodesOpen().isEmpty() == true) {
			// L'exploration est terminé (Fin de mission) :
			this.agent.setIsExploration(false);
			this.numeroTransition = AgentExplorateur.P_P;
		} else {
			if (this.nodeButIsClose() == true) {
				// Le noeud but est contenu dans la liste des noeuds closes :
				// Rechercher un nouveau Goal :
				if (this.isNodesGoals() == true) {
					// On a encore des noeuds but a explorer :
					// Selectionner un node goal :
					this.selectNodeGoal();
					// Reset variable EchoMap + shareMap :
					this.agent.resetShareMap(); // A voir !!!!!
					this.numeroTransition = AgentExplorateur.T_CHECK_SIGNAUX_BOITE_LETTRE;
				} else {
					// Plus de noeud but a explorer :
					// Reset variable EchoMap + shareMap :
					this.agent.resetShareMap(); // A voir !!!!!
					// L'exploration est terminé (Fin de mission) :
					this.agent.setIsExploration(false);
					this.numeroTransition = AgentExplorateur.P_P; 
				}
			}
		} 
	}
	
	private boolean isActiveEchoMap () {
		boolean active = false;
		if (this.agent.getCptEchoFlowdingMap() % this.agent.getIntervalleEchoMap() == 0) {
			// Plus le critère d'attente après reception d'une carte : !!!!!!!!!!!!!!!!!!!!!!!
			active = true;
		}
		return active;
	}
	
	private boolean nodeButIsClose () {
		boolean isContenu = false;
		if (this.agent.getNodesBut().isEmpty() == false) {
			Integer indexNodeGoal = this.agent.getNodesBut().size() - 1;
			if (this.agent.getCarteExploration().getNodesClose().contains(indexNodeGoal) == true) {
				isContenu = true;
			}
		}
		return isContenu;
	}
	
	private boolean isNodesGoals () {
		boolean isPresent = false;
		if (this.agent.getCarteExploration().getNodesOpen().isEmpty() == false) {
			isPresent = true;
		}
		return isPresent;
	}
	
	private void selectNodeGoal () {
		// Recherche les chemins but :
		List<List<String>> list_chemin = this.agent.getCarteExploration().getShortestPathNodes(this.agent.getCurrentPosition(),
				this.agent.getCarteExploration().getNodesOpen(), 2);
		// Verifier si l'agent (this) est prioritaire :
		if (this.agent.getIdAgent() < this.agent.getIdentifiantShareMap().getId()) {
			// Je suis  prioritaire donc je prends le noeud but le plus proche :
			this.agent.setCheminPlanification(list_chemin.get(0));
		} else {
			// L'agent le moins prioritaire prendra le noeud but le plus loin :
			int taille_list = list_chemin.size();
			this.agent.setCheminPlanification(list_chemin.get(taille_list-1));
		}
	}
	
	@Override
	public int onEnd(){
		return this.numeroTransition ;
	}

}
