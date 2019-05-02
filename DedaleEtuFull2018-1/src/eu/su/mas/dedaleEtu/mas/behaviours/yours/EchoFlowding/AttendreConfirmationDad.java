package eu.su.mas.dedaleEtu.mas.behaviours.yours.EchoFlowding;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import eu.su.mas.dedaleEtu.mas.agents.yours.AgentExplorateur;
import eu.su.mas.dedaleEtu.mas.knowledge.yours.EchoFlowding.ProtocoleEcho;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class AttendreConfirmationDad extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2812760244814719637L;
	private AgentAbstrait agent;
	private boolean			 confirmation;

	
	public AttendreConfirmationDad (final AgentAbstrait agentAbstrait) {
		super(agentAbstrait);
		this.agent = agentAbstrait;
		this.confirmation = false;
	}

	@Override
	public void action() {
		//System.out.println("**** " + this.agent.getLocalName() + " <---- entre dans AttendreConfirmationDad\n");
		//System.out.println("**** " + this.agent.getLocalName() + " data EchoFlowing :\n" + this.agent.getEchoFlowding());
		//System.out.println("Ma position " + this.agent.getCurrentPosition());
		// Check Son dans la boite au lettre : 
		
		MessageTemplate messageTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.CONFIRM), MessageTemplate.MatchProtocol("DAD"));
				
		ACLMessage msg = this.agent.blockingReceive(messageTemplate, 2000);			
		
		while (msg != null) {
			if (msg.getPerformative() == ACLMessage.CONFIRM && msg.getProtocol().equals("DAD")) {
				// Ouverture du message contenant le protocole :
				ProtocoleEcho msgContent = null;
				try {
					msgContent = (ProtocoleEcho) msg.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				
				// Si l'agent qui m'accepte comme père à le meme protocole alors je l'accepte comme fils : A VOIR !!!!
				if (this.agent.getEchoFlowding().getProtocolEcho().getNumEcho().equals(msgContent.getNumEcho()) &&
						this.agent.getEchoFlowding().getProtocolEcho().getID().equals(msgContent.getID())) {
				
					// Verifie que la demande de confirmation correspond bien au père potentiel que j'ai retenu et qu'il ne soit pas ancien:
					if (msg.getSender().getLocalName().equals(this.agent.getEchoFlowding().getDad().getLocalName()) &&
							msgContent.getNumEcho() >= this.agent.getEchoFlowding().getIterationEcho(msg.getSender())) {
						//System.out.println("---------------------------------------------------------------------------");
						// Message de confirmation de père :
						//System.out.println(this.agent.getLocalName() + " confirme pour père " + this.agent.getEchoFlowding().getDad().getLocalName()
						//		+ " avec le protocole " + this.agent.getEchoFlowding().getProtocolEcho());
						// Mettre l'agent sender dans la liste à ne pas envoyer d'echo :
						this.agent.getEchoFlowding().addNoEcho(msg.getSender());
						// Message de confirmation reçu :
						this.confirmation = true;
					} else {
						//System.out.print("J'ai reçu la confirmation de mon père mais je l'ai refusé chit !!");
					}
				} else {
					//System.out.println("\n*************** " + this.agent.getLocalName() + " AUCUNE CORRESPONDANCE DAD -----------------\n");
				}
			}
			msg = this.agent.receive(messageTemplate);
		}
		if (this.confirmation == false) {
			// Affichage :
			//System.out.println(this.agent.getLocalName() + " supprime comme père potentiel " + this.agent.getEchoFlowding().getDad().getLocalName()
			//		+ " avec le protocole " + this.agent.getEchoFlowding().getProtocolEcho());
			// Supprimer le père potentiel + Creation d'un protocole :
			this.agent.getEchoFlowding().supprimerDadPotentiel();
		}
	}

	@Override
	public int onEnd(){
		// Affichage :
		//System.out.println("**** " + this.agent.getLocalName() + " ----> sort de AttendreConfirmationDad\n");
		// Ré-initialiser la confirmation à false :
		this.confirmation = false;
		if (this.agent.getEchoFlowding().isSecondeChance() == true) {
			return AgentExplorateur.T_ATTENDRE_CONFIRMATION_FILS_AFTER_CONFIRMATION_DAD;
		}
		return AgentExplorateur.T_SEND_ECHO_AFTER_CONFIRMATION_DAD;
	}
}
