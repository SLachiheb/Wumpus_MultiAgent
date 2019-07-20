package eu.su.mas.dedaleEtu.mas.knowledge.yours.EchoFlowding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import eu.su.mas.dedaleEtu.mas.agents.yours.AgentAbstrait;
import jade.core.AID;

public class EchoFlowding implements Serializable {

	private static final long serialVersionUID = -6532395082603157743L;
	
	private AgentAbstrait 			agent;
	private boolean 				racine;
	private Integer 				compteurEcho;
	
	private AID 			  		dad;
	private ProtocoleEcho			protocolEcho;
	private ArrayList<AID> 			fils;
	private boolean					isSecondeChance;	
	
	private HashMap<AID, Integer> 	iterationEcho;
	private List<AidProtocole> 		dadPotentiel;	
	/**
	 * Contient tout les pères potentiels refusés et le père potentiel prioritaire, 
	 * pour eviter de leur envoyer des echos inutiles.
	 */
	private ArrayList<AID>			noEcho;
	
	/**
	 * Constructeur
	 */
	public EchoFlowding (final AgentAbstrait agentAbstrait) {
		this.agent 			= agentAbstrait;
		this.compteurEcho	= 0;
		
		this.dad			= null;
		this.protocolEcho	= null;
		this.racine			= false;
		this.fils			= new ArrayList<AID>(); 
		this.isSecondeChance = false;
		
		this.iterationEcho 	= new HashMap<AID, Integer>();
		this.dadPotentiel	= new ArrayList<AidProtocole>();
		this.noEcho			= new ArrayList<AID>();
	}
	
	/**
	 * Methode utilisée dans attente de confirmation des fils, si l'agent se dit racine :
	 * @param dad
	 * @param protocole
	 */
	public void addDad (AID dad, ProtocoleEcho protocole) {
		if (this.dad == null) {
			this.dad = dad;
			this.insertProtocol(protocole, this.dad);
			this.racine = false;
		} else {
			if (this.protocolEcho.getID() > this.protocolEcho.getID()) {
				this.dad = dad;
				this.insertProtocol(protocole, this.dad);
			}
		}
	}
	
	/**
	 * Valide l'ajout d'un père potentiel si son echo n'est pas obsolète :
	 * @param dadPotentiel
	 */
	public void addDadPotentiel (AID dadPotentiel, ProtocoleEcho protocole) {
		if (this.iterationEcho.containsKey(dadPotentiel) == false  || 
				protocole.getNumEcho() >= this.iterationEcho.get(dadPotentiel)) {
			// Construction d'une struture de trie de dad potentiel : 
			AidProtocole newDadP = new AidProtocole(dadPotentiel, protocole);
			// Si le père n'est pas contenu déjà dans liste des pères potentiel et qu'elle ne contient pas le fils :
			if (this.dadPotentiel.contains(newDadP) == false && this.fils.contains(dadPotentiel) == false) {
				// Ajoute les dad potentiels :
				this.dadPotentiel.add(newDadP);
				// Pas besoin de contacter cette agent par la suite:
				this.noEcho.add(dadPotentiel);
				// Maj de la racine
				this.racine = false;
				// Affichage :
				System.out.println(this.agent.getLocalName() + " <---- Reçoit un echo de " +
						dadPotentiel.getLocalName() + " avec le protocole : " + protocole);
			}
			if (this.iterationEcho.containsKey(dadPotentiel) == false) {
				// Rajouter dans sa base de donnée, si il ne contient pas numéro d'echo de cette agent :
				this.iterationEcho.put(dadPotentiel, protocole.getNumEcho());
			} else {
				// Mettre a jour les iterations des echos :
				this.iterationEcho.put(dadPotentiel, 
						(protocole.getNumEcho() > this.iterationEcho.get(dadPotentiel)) ? protocole.getNumEcho():this.iterationEcho.get(dadPotentiel));
			}
			
		}
	}
	
	/**
	 * Ajoute les Agents à ne pas contacter :
	 * @param agent
	 */
	public void addNoEcho (AID agent) {
		if (this.noEcho.contains(agent) == false) {
			this.noEcho.add(agent);
		}
	}
	
	/**
	 * Trie la liste potentiel de père en fonction de la priorité de la racine du protocole :
	 */
	public void sortDadPotentiel () {
		Collections.sort(this.dadPotentiel, new ComparatorAidProtocole());
	}
	
	/**
	 * Si aucun message dans CheckEchoBoiteLettre, il se définit comme étant Racine :
	 * @param agentRacine
	 */
	public void initNewProtocolEcho () {
		// Declaration en tant que Racine :
		this.dad 			= null;	
		// Creation du protocoleEcho :
		ProtocoleEcho newProtocole = this.newProtocolEcho();
		// Insertion de la numerotation de l'echo :
		this.insertProtocol(newProtocole, this.agent.getAID());
		// Mettre en racine :
		this.racine = true;
	}
	
	/**
	 * Creation d'un protocole à partir de (IdAgent, NumIterationEcho, AIDAgent) :
	 * @return
	 */
	private  ProtocoleEcho newProtocolEcho () {
		ProtocoleEcho newProto =  new ProtocoleEcho(this.agent.getIdAgent(), this.compteurEcho);
		this.compteurEcho++;
		return newProto;
	}
	
	/**
	 * MaJ de la base de donnée des protocoles reçus et du protocoles actuels:
	 * @param protocolEcho
	 */
	private void insertProtocol (ProtocoleEcho protocolEcho, AID expediteur) {
		// Si le l'expédieur du echo existe :
		if (this.iterationEcho.containsKey(expediteur)) {
			// Recupere le compteur du echo actuel de la BD:
			int oldEcho = this.iterationEcho.get(expediteur);
			// MaJ si oldEcho est plus ancien :
			if (oldEcho < protocolEcho.getNumEcho())
				this.iterationEcho.put(expediteur, protocolEcho.getNumEcho());
		} else {
			// Si il n'existe de pas, creation de la base pour le sender :
			this.iterationEcho.put(expediteur, protocolEcho.getNumEcho());
		}
		// Maj du protocolEcho :
		this.protocolEcho = protocolEcho;
	}
	
	/**
	 * Supprime le père potentiel qui n'est pas encore confirmer :
	 */
	public void supprimerDadPotentiel () {
		// Supprime le père
		this.dad = null;
		// Initialise un protocole personnel :
		this.initNewProtocolEcho();
		// 
		this.racine = true;
		// Rajouter dans les non echo ;
		this.noEcho.add(this.dad);
	}

	/**
	 * Methode de réponse boolean :
	 * @return
	 */
	public boolean isSons () {
		return !this.fils.isEmpty();
	}
	
	public boolean isDad () {
		return this.dad != null;
	}
	
	public boolean isDadPotentiel () {
		return this.dadPotentiel.isEmpty();
	}
	
	public boolean isRacine () {
		return this.racine;
	}
	
	public boolean isSecondeChance () {
		return this.isSecondeChance;
	}
	
	/**
	 * Ajoute un fils que si celui qu'on veut ajouter n'est pas NoAck ou Racine
	 * @param destinataireEcho
	 * @return
	 */
	public void addSon (AID myFils) {
		if (!this.fils.contains(myFils)) {
			if (this.dad == null) {
				this.fils.add(myFils);
			} else {
				if (!myFils.getLocalName().equals(this.dad.getLocalName())) {
					this.fils.add(myFils);
				}
			}
		}
	}
	
	/**
	 * Nettoie l'ensemble des données après chaque communication :
	 */
	public void purgeData () {
		// Reboot de la liste des fils :
		this.fils.clear();
		// Reboot du protocole de communication utilisé :
		this.protocolEcho = null;
		//Reboot du père de l'agent :
		this.dad = null;
		// 
		this.racine = false;
		//
		this.isSecondeChance = false;
	}
	
	/**
	 * Retourne la taille de la liste des pères potentiels :
	 * @return
	 */
	public Integer sizeDadPotentiel () {
		return this.dadPotentiel.size();
	}
	
	public String toString () {
		String s = "";
		if (this.getDad() == null) {
			s += "Père : aucun.\n";
		} else {
			s += "Père : " + this.dad.getLocalName() + "\n";
		}
		if (this.fils.isEmpty()) {
			s += "Fils : aucun.";
		} else {
			s += "Fils : ";
			for (AID son : this.fils) {
				s+= son.getLocalName() + ", ";
			}
		}
		s += "\n";
		return s;
	}
	
	/**
	 * Getter
	 */
	public Integer getNumEcho () {
		return this.compteurEcho;
	}
	
	public ProtocoleEcho getProtocolEcho () {
		return this.protocolEcho;
	}
	
	public AID getDad () {
		return this.dad;
	}
	
	public ArrayList<AID> getSons() {
		return this.fils;
	}
	
	public Integer getIterationEcho(AID agent) {
		if (this.iterationEcho.containsKey(agent)) {
			return this.iterationEcho.get(agent);
		} else {
			return -1;
		}
	}
	
	public ArrayList<AID> getNoEcho () {
		return this.noEcho;
	}
	

	public AidProtocole getRemove () {
		if (this.dadPotentiel.size() == 0)
			return null;
		return this.dadPotentiel.remove(0);
	}
	
	public List<AidProtocole>  getAddDadPotentiel () {
		return this.dadPotentiel;
	}
	
	/**
	 * SETTER
	 */
	public void setSecondeChance (boolean value) {
		this.isSecondeChance = value;
	}
	
	public void setProtocolEcho (ProtocoleEcho pEcho) {
		this.protocolEcho = pEcho;
	}
	
	public void setRacine (boolean b) {
		this.racine = b;
	}
	
	/**
	 * Classe utilisée pour trier les pères potentiels en fonction de la racine du protocole.
	 * @author sarah
	 *
	 */
	public class AidProtocole {
		private AID dad;
		private ProtocoleEcho protocole;
		
		public AidProtocole (AID dad, ProtocoleEcho protocole) {
			this.dad = dad;
			this.protocole = protocole;
		}
		
		@Override
		public boolean equals(Object o) {
	        if (this == o) {
	            return true;
	        }
	        if (o == null) { 
	        	return false;
	        }
	        if (!(o instanceof AidProtocole)) {
	            return false;
	        }
	        AidProtocole p = (AidProtocole)o;
	        return this.dad.getLocalName().equals(p.getDad().getLocalName())  && 
	        		this.protocole.getID() == p.getProtocole().getID() &&
	        		this.protocole.getNumEcho() == p.getProtocole().getNumEcho();
	    }
		
		/*GETTER*/
		public AID getDad() {
			return this.dad;
		}		
		public ProtocoleEcho getProtocole () {
			return this.protocole;
		}
	}
	
	/**
	 * Classe permettant de comparer les classes AidProtocole.
	 * @author cassandre
	 *
	 */
	public class ComparatorAidProtocole implements Comparator<AidProtocole> {
		@Override
		public int compare(AidProtocole o1, AidProtocole o2) {
			return o1.getProtocole().getID().compareTo(o2.getProtocole().getID());
		}
	}
}