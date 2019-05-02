package eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;

public class CarteTresors implements Serializable {

	private static final long serialVersionUID = 2907275599046344638L;
	
	/**
	 * Contient la position du Tresor en String et le Tresor avec la classe Tresor
	 */
	private Date date;
	private HashMap<String, Tresor> carteTresor;
	private 	List<String>		tresorSupp;			


	/**
	 * Constructeur
	 */
	public CarteTresors (/*AgentAbstrait agent*/) {	
		//this.agent	= agent;
		date = new Date();
		carteTresor = new HashMap<String, Tresor> ();
    	this.tresorSupp					= new ArrayList<String>(); 
	}
	
	/**
	 * Ajoute un tresor avec une liste d'observation en cette position :
	 * @param position
	 * @param lObservations
	 * @return boolean
	 */
	public boolean addTresors(String position, List<Couple<Observation,Integer>> lObs) {
		if (this.getTresorSupprime().contains(position) == false) {
			if (this.carteTresor.containsKey(position)) {
				// Recupere le Tresor existant
				Tresor oldTresor = this.carteTresor.get(position);
				// Creation d'un nouveau Tresor potentiel
				Tresor newTresor = new Tresor(lObs);
				// Si il n'y a pas de ressource pour le nouveau trésor :
				/*if (newTresor.isSansTresor() == true) {
					System.out.println("Sans ressources");
					if (newTresor.getDate().after(oldTresor.getDate())) {
						System.out.println("le newTresor est plus recent");
						System.out.println(newTresor.getDate());
						System.out.println(oldTresor.getDate());

						//this.carteTresor.remove(position);
						System.out.println("*******************> \n"  + this.getCarteTresors());
						this.getTresorSupprime().add(position);
						System.out.println("**REMOVE \n"  + this.carteTresor.remove(position));
					} else {
						System.out.println("le newTresor est moins recent");
						System.out.println(newTresor.getDate());
						System.out.println(oldTresor.getDate());
						// Mise a jour
						oldTresor.updateObservations(newTresor);
					}
				} else {
					// Mise a jour
					oldTresor.updateObservations(newTresor);
				}*/
				oldTresor.updateObservations(newTresor);
				if (oldTresor.isSansTresor() == true) {
					System.out.println("*******************> \n"  + this.getCarteTresors());
					this.getTresorSupprime().add(position);
					System.out.println("**REMOVE \n"  + this.carteTresor.remove(position));
					//this.carteTresor.remove(position);
				}
				//this.date = new Date(); //
			}else {
				// Creation d'un Tresor
				Tresor newTresor = new Tresor (lObs);
				// Si le trésor n'est pas vide :
				if (newTresor.isSansTresor() == false) {
					this.carteTresor.put(position, newTresor);
					return true;
				}
				//this.date = new Date(); //
			}
		}
		return false;
	}
	
	/**
	 * Mise a jour de la carte au tresor via la carteUpdate
	 * @param carteUpdate
	 */
	public void updateTresors (CarteTresors carteUpdate) {
		for (Map.Entry<String, Tresor> entry : carteUpdate.getCarteTresors().entrySet()) {
			this.addTresors(entry.getKey(), entry.getValue());
		}
		this.date = new Date();
	}
	
	/**
	 * Ajout d'un tresor par une classe Tresor existante à la HashMap
	 * @param position
	 * @param tresor
	 */
	private void addTresors(String position, Tresor tresor) {
		if (this.getTresorSupprime().contains(position) == false) {
			if (this.carteTresor.containsKey(position)) {
				// Recupere le Tresor existant
				Tresor oldTresor = this.carteTresor.get(position);
				// Mise a jour
				oldTresor.updateObservations(tresor);
				// Test si le tresor est vide :
				if (oldTresor.isSansTresor() == true) {
					//this.carteTresor.remove(position);
					System.out.println("*******************> \n"  + this.getCarteTresors());
					this.getTresorSupprime().add(position);
					System.out.println("**REMOVE \n"  + this.carteTresor.remove(position));
					
				}
			}else {
				// Si la date de rafrichisement de la carte est antérieur à la date du tresor :
				//if (this.date.before(tresor.getDate())) {
					if (tresor.isSansTresor() == false) {
						// Verifie si le tresor n'est pas vide :
						// Insertion du tresor qui n'est pas dans la HashMap
						this.carteTresor.put(position, tresor);			
					}
				//}
			}
		}
		
	}
	
	public void pickTresor (String position, Integer pick) {
		if (this.carteTresor.containsKey(position) == true) {
			this.carteTresor.get(position).setPick(pick);
			if (this.carteTresor.get(position).getRessource() == 0) {
				this.carteTresor.remove(position);
			}
		}
	}
	
	public HashMap<Tresor, String> convertHashMap () {
		HashMap<Tresor, String> convert = new HashMap<Tresor, String>();
		
		for (Map.Entry<String, Tresor> entry : this.carteTresor.entrySet()) {
			convert.put(entry.getValue(), entry.getKey());
		}
		
		return convert;
	}
	
	/**
	 * Affichage console
	 * @param nameAgent
	 */
	public String toString() {
		String s = "";
		for (Map.Entry<String, Tresor> entry : this.carteTresor.entrySet()) {
			s += "\nPosition du Trésor : " + entry.getKey() + "\n";
			s += entry.getValue();
		}
		return s;
	}
	
	/**
	 * GETTER
	 */
	public HashMap<String, Tresor>  getCarteTresors() {
		return this.carteTresor;
	}
	public Date getRafrichissement () {return this.date;} 
	public List<String>			getTresorSupprime()				{return this.tresorSupp;}

	
	/**
	 * Classe interne Tresor
	 * @author sarah
	 *
	 */
	public class Tresor implements Serializable {
	
		/**
		 * 
		 */
		private static final long serialVersionUID = 2176758874803992004L;
		
		private Date	date;
		private Integer diamond;
		private Integer gold;
		
		private Integer	lockIsOpen;
		private Integer strengh;
		private Integer lockpicking;
				
		public Tresor (List<Couple<Observation,Integer>> lobs) {
			this.diamond 		= 0;
			this.gold	 		= 0;
			
			this.lockIsOpen		= 0;
			this.strengh		= 0;
			this.lockpicking 	= 0;
			this.date	 		= new Date();
			this.initAddObservations(lobs);
		}
		
		/**
		 * Verifie si le tresor contient encore des richesses :
		 * @return
		 */
		public boolean isSansTresor() {
			return (this.getRessource() <= 0);
		}
		
		/**
		 * Initialise les attributs de la classe en fonction des ressources observées
		 * @param lobs
		 */
		private void initAddObservations (List<Couple<Observation,Integer>> lobs) {
			for (Couple<Observation,Integer> obs : lobs) {
				if (obs.getLeft() == Observation.DIAMOND) {
					this.diamond 		= obs.getRight();
				}
				if (obs.getLeft() == Observation.GOLD) {
					this.gold 			= obs.getRight();
				}
				if (obs.getLeft() == Observation.LOCKSTATUS) {
					this.lockIsOpen 	= obs.getRight();
				}
				if (obs.getLeft() == Observation.STRENGH) {
					this.strengh 		= obs.getRight();
				}
				if (obs.getLeft() == Observation.LOCKPICKING) {
					this.lockpicking 	= obs.getRight();
				}
			}
		}
		
		/**
		 * Mise a jour du danger courant avec un danger potentielle si la date est anterieur
		 * @param danger
		 */
		public void updateObservations (Tresor newTresor) {
			// Si la date du danger courant est anterieur :
			if (this.date.before(newTresor.getDate())) {
				// Mise a jour avec l objet Danger local:
				if (this.getTypeTresor() == Observation.GOLD)
					this.gold 		= newTresor.getRessource();
				if (this.getTypeTresor() == Observation.DIAMOND)
					this.diamond 		= newTresor.getRessource();
				this.lockIsOpen = newTresor.getLockStatus();
				this.date		= newTresor.getDate();
			}
		}
		
		
		public String toString () {
			String name = "";
			name += "Date: " + this.date.toString() + "\n";
			if (this.getTypeTresor() == Observation.DIAMOND) {
				name += "Diamond : " + Integer.toString(this.diamond) + "\n";
			} else {
				name += "Gold: " + Integer.toString(this.gold) + "\n";
			}
			if (this.getLockStatus().equals(0)) {
				name += "LockStatus : Verrouiller \n";
			} else {
				name += "LockStatus : Ouvert \n";
			}
			name += "Strengh : " + this.strengh + "\n";
			name += "Lockpicking : " + this.lockpicking + "\n";
			return name;
		}
		
		/**
		 * Getter
		 */
		public Integer getRessource () {
			return this.gold + this.diamond;
			/*if (this.getTypeTresor() == Observation.GOLD)
				return this.gold;
			else 
				return this.diamond;*/
		}
		public Date    getDate() {return this.date;}
		public Integer getLockStatus () {return this.lockIsOpen;}
		public Observation getTypeTresor () {
			if (this.diamond > 0)
				return Observation.DIAMOND;
			return Observation.GOLD;
		}
		public Integer getStrengh() {return this.strengh;}
		public Integer getLockpicking() {return this.lockpicking;}
		public void    setOpenLock(Integer value) {this.lockIsOpen = value;}
		public void    setPick(Integer value) {
			if (this.getTypeTresor() == Observation.GOLD)
				this.gold -= value;
			else 
				this.diamond-= value;
		}
	}
}
