package eu.su.mas.dedaleEtu.mas.behaviours.yours.OuvertureTresorCollectif;


import java.io.IOException;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentCollecteur;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.behaviours.yours.Strategie;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes.Carte;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction.Identifiant;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ActionGestionTresorCollectifBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9077979051655501271L;
	private AgentAbstrait 	agent;
	private Integer			numTransition;
	
	public ActionGestionTresorCollectifBehaviour (final AgentAbstrait agentCollecteur) {
		super(agentCollecteur);
		this.agent 				=   agentCollecteur;
		this.numTransition		= null;
	}
	
	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " est dans ActionGestionTresorCollectifBehaviour");
		this.agent.setStrategie(Strategie.ActionTresorCollecif);
		
		this.agent.attendre();
		this.agent.updateCarte();
		
		// 2) Vérifier si on a pas reçu une validation d'ouverture de trésor :
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchProtocol("OUVERTURE_TRESOR"));

		ACLMessage msgRecuValidation= this.agent.receive(msgTemplate);
		while(msgRecuValidation != null) {
			// Récupérer la nouvelle carte du Trésor mise à jour :
			try {
				Carte carteRecu = (Carte) msgRecuValidation.getContentObject();
				// Mettre à jour la carte de l'agent à partir de la carte reçu :
				carteRecu.updateInsertCartes(this.agent.getCarteTresors(), this.agent.getCarteDangers());
				System.out.println("**** " + this.agent.getLocalName() + " : recoit un message qu'un tresor est ouvert.");


				// MaJ de l'ouverture du trésor :
				if (this.agent.getCarteTresors().getCarteTresors().containsKey(carteRecu.getPositionTresor()) ==  true) {
					this.agent.getCarteTresors().getCarteTresors().get(carteRecu.getPositionTresor()).setOpenLock(1);
				}
				
				// Verifier que le message de validation corresponds bien à mon trésor actuel :
				if (this.agent.getPosistionTresorCollectif().equals(carteRecu.getPositionTresor()) == true) {
					System.out.println("**** " + this.agent.getLocalName() + " : c'est mon trésor donc je propage que le trésor " + carteRecu.getPositionTresor() + " est ouvert");
					
					// Reboot :
					this.agent.setSearchTresorCollectif(false);
					this.agent.setAttenteTresor(false);
					
					// Maj de l'agent Collecteur :
					if (this.agent.getClass() == AgentCollecteur.class) {
						AgentCollecteur agentCollecteur = (AgentCollecteur)this.agent;
						agentCollecteur.setActionPDM(null);
					}

					
					// Faire un broadcast autour de moi :
					// 1) Creation d'un message :
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setProtocol("OUVERTURE_TRESOR");
					// 2) Modifier l'expéditeur du message :
					msg.setSender(this.getAgent().getAID());
					// 3) Ajouter l'expéditeur de type Collecteur / Expediteur:
					DFAgentDescription description = new DFAgentDescription();
				    DFAgentDescription[] resultats;
				    try {
						resultats = DFService.search(this.getAgent(), description);
				        if (resultats.length > 0) {
				            for (DFAgentDescription agent : resultats) {
				            	// Ne communique ni avec l'expéditeur du message, ni avec lui même :
				            	if (agent.getName().equals(this.getAgent().getAID()) == false &&
				            			agent.getName().equals(msgRecuValidation.getSender()) == false) {
				                	// Ajout les expéditeurs du message :
				                	msg.addReceiver(agent.getName());
				            	}
				            }
				        }
					} catch (FIPAException e1) {
						e1.printStackTrace();
					}
				    Identifiant id = new Identifiant(this.getAgent().getLocalName(), this.agent.getIdAgent(), this.agent.getCurrentPosition());
				    // 4) Rajouter la carte du danger et la carte d'exploration pour la mise à jour des données + Position tresor collectif :
					Carte carte = new Carte(id, this.agent.getCarteTresors(), this.agent.getCarteDangers(), this.agent.getPosistionTresorCollectif());
				    try {
						msg.setContentObject(carte);
					} catch (IOException e) {
						e.printStackTrace();
					}
				    // 5) Envoyer le message :
					this.agent.sendMessage(msg);
					
					// Partir en satisfaction pour trouver une autre action :
					this.numTransition = AgentAbstrait.T_CHECK_SIGNAL_AFTER_ACTION_TRESOR_COLLECTIF;
				}
				// On ne propage pas un broadcast qui ne correspond pas à notre trésor.
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			// Next message dans la boite au lettre :
			msgRecuValidation = this.agent.receive(msgTemplate);
		} 
		
		// Verifier qu'on soit bien en recherche de trésor collectif :
		if (this.agent.getSearchTresorCollectif() == true) {
			// 3) Vérifier si je me trouve à la position du trésor que je recherche actuellement : 
			if (this.agent.getPosistionTresorCollectif().equals(this.agent.getCurrentPosition()) == true) {
				
				// Verifie si le trésor existe toujours :
				if (this.agent.getCarteTresors().getCarteTresors().containsKey(this.agent.getPosistionTresorCollectif()) == true) {
					// Est-ce que je peux ouvrir le coffre :
					if (this.agent.getCarteTresors().getCarteTresors().get(this.agent.getPosistionTresorCollectif()).getTypeTresor().equals(Observation.DIAMOND)){
						//System.out.println("Le type du tresor est : " + Observation.DIAMOND);
					} else {
						//System.out.println("Le type du tresor est : " + Observation.GOLD);
					}
					if (this.agent.openLock(this.agent.getCarteTresors().getCarteTresors().get(this.agent.getPosistionTresorCollectif()).getTypeTresor()) == true) {
						// MaJ dans la carte des trésors que j'ai pu ouvrir ce tresor:
						this.agent.getCarteTresors().getCarteTresors().get(this.agent.getPosistionTresorCollectif()).setOpenLock(1);
						// MaJ des variables :
						this.agent.setAttenteTresor(false);
						this.agent.setSearchTresorCollectif(false);
						
						if (this.agent.getClass() == AgentCollecteur.class) {
							// Dire à l'agentCollecteur qu'il n'a plus d'action en cours :
							AgentCollecteur agentC = (AgentCollecteur)this.agent;
							agentC.setActionPDM(null);
							agentC.setActionSatisfaction(false);
						}
						if (this.agent.getClass() == AgentExplorateur.class) {
							// Dire à l'agentCollecteur qu'il n'a plus d'action en cours :
							AgentExplorateur agentE = (AgentExplorateur)this.agent;
							agentE.setActionSatisfaction(false);
						}
						// Affichage : 
						System.out.println("**** " + this.agent.getLocalName() + " : Je suis arrivée au trésor et je peux l'ouvrir.");
						// Informer au agent qui m'ont aidé que j'ai pu ouvrir le coffre :
						this.numTransition = AgentAbstrait.T_SEND_VALIDE_TRESOR_AFTER_ACTION_TRESOR_COLLECTIF;
					} else {
						// Je suis arrvée au trésor, mais je ne peux pas l'ouvrir, je passe en attente de trésor :
						// Affichage :
						System.out.println("**** " + this.agent.getLocalName() + " : Je suis arrivée au trésor mais je ne peux pas l'ouvrir");
						// MaJ des variables :
						this.agent.setAttenteTresor(true); // Je suis en attente d'ouverture car besoin d'aide.
						// Si je n'arrive pas à ouvrir le coffre :
						this.checkPartCommun_AttenteTresor_getMove();
					}
				} else {
					this.agent.setAttenteTresor(false);
					this.agent.setSearchTresorCollectif(false);
					if (this.agent.getClass() == AgentCollecteur.class) {
						// Dire à l'agentCollecteur qu'il n'a plus d'action en cours :
						AgentCollecteur agentC = (AgentCollecteur)this.agent;
						agentC.setActionPDM(null);
						agentC.setActionSatisfaction(false);
					}
					if (this.agent.getClass() == AgentExplorateur.class) {
						// Dire à l'agentCollecteur qu'il n'a plus d'action en cours :
						AgentExplorateur agentE = (AgentExplorateur)this.agent;
						agentE.setActionSatisfaction(false);
					}
				}
				
			} else {
				// Si je ne suis pas à la position du trésor :
				//System.out.println("**** " + this.agent.getLocalName() + " : Je ne me trouve pas à la position du trésor");
				this.checkPartCommun_AttenteTresor_getMove();
			}
		} else {
			this.numTransition = AgentAbstrait.T_CHECK_SIGNAL_AFTER_ACTION_TRESOR_COLLECTIF;
		}
	}
	
	private void checkPartCommun_AttenteTresor_getMove () {
		// 4) Est-ce que je suis en attente d'ouverture :
		if (this.agent.getAttenteTresor() == true) {
			// Vérifier si on a reçu une proposition d'aide pour ouvrir les coffres :
			this.numTransition = AgentAbstrait.T_CHECK_PROPOSAL_HELP_AFTER_ACTION_TRESOR_COLLECTIF;
		} else {
			// MaJ de la carte :
			this.agent.updateCarte();
			// Je ne suis pas en attente de tresor :
			this.agent.setIsMove(this.agent.moveTo(this.agent.getNodesBut().get(0))); 
			// Est-ce que je peux bouger :
			if (this.agent.getIsMove()) {
				// J'ai pu bouger donc je peux enlever ma position actuel du chemin but :
				this.agent.cheminButRemove(0); 
				// Poursuivre avec ma satisfaction :
				this.numTransition = AgentAbstrait.T_CHECK_SIGNAL_AFTER_ACTION_TRESOR_COLLECTIF;
			} else {
				// Je n'ai pas pu bouger, quelqu'un me gène :
				// Tenter de lui envoyer une proposition d'aide pour voir si il patient pour le même trésor que moi :
				this.numTransition = AgentAbstrait.T_SEND_PROPOSAL_HELP_AFTER_ACTION_TRESOR_COLLECTIF;
			}
		}
	}

	@Override
	public int onEnd(){
		//System.out.println("**** " + this.agent.getLocalName() + " sort de ActionGestionTresorCollectifBehaviour");
		return this.numTransition;
	}

}
