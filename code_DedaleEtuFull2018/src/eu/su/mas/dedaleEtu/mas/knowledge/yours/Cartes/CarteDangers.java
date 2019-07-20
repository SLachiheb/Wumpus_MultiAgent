package eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;

public class CarteDangers implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2326849762662474169L;
	
	/**
	 * Contient la position du danger en String et le dangers avec la classe Danger
	 */
	private HashMap<String, Danger> carteDanger;
	
	/**
	 * Constructeur
	 */
	public CarteDangers () {
		this.carteDanger = new HashMap<String, Danger>();
	}
	
	/**
	 * Ajoute un danger dans la hashMap s'il n'est pas dans la HashMap ou si il l'a, il l'a met à jour
	 * @param position
	 * @param lObs
	 */
	public void addDangers (String position, List<Couple<Observation,Integer>> lObs) {
		if (this.carteDanger.containsKey(position)) {			
			// Recupere le Danger 
			Danger oldDanger = this.carteDanger.get(position);
			// Creation du nouveau Danger potentiel
			Danger newDanger = new Danger(lObs);
			// Mise a jour 
			oldDanger.updateObservations(newDanger);
		} else {
			// Creation d'un danger 
			Danger newDanger = new Danger (lObs);
			// Insertion du danger dans la hashMap
			if (newDanger.isSansDanger() ==  false) {
				this.carteDanger.put(position, newDanger);
			}
		}
	}
	
	/**
	 * Mise a jour de la carte au danger via la carteUpdate
	 * @param carteUpdate
	 */
	public void updateDangers(CarteDangers carteUpdate) {
		for (Map.Entry<String, Danger> entry : carteUpdate.getCarteDanger().entrySet()) {
			this.addDangers(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Ajout d'un danger par une classe Danger existante à la HashMap
	 * @param position
	 * @param tresor
	 */
	private void addDangers(String position, Danger danger) {
		if (this.carteDanger.containsKey(position)) {
			// Recupere le Tresor existant
			Danger oldDanger = this.carteDanger.get(position);
			// Mise a jour
			oldDanger.updateObservations(danger);
			// Test si il y a plus de danger :
			if (oldDanger.isSansDanger() == true) {
				this.carteDanger.remove(position);
			}
		}else {
			if (danger.isSansDanger() == false) {
				// Verifie si le danger n'est pas vide :
				// Insertion du tresor qui n'est pas dans la HashMap
				this.carteDanger.put(position, danger);
			}
			
		}
	}
	
	/**
	 * Affichage console
	 * @param nameAgent
	 */
	public String toString() {
		String s = "";
		for (Map.Entry<String, Danger> entry : this.carteDanger.entrySet()) {
			s += "\nPosition du Danger : " + entry.getKey() + "\n";
			s += entry.getValue();
		}
		return s;
	}

	/**
	 * GETTER
	 */
	public HashMap<String, Danger> getCarteDanger() {
		return this.carteDanger;
	}
	
	
	/**
	 * Classe interne Danger
	 * @author sarah
	 *
	 */
	public class Danger implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2452279603690462011L;
		
		private Date 	date;
		private boolean well;
		private boolean stench;
		
		public Danger (List<Couple<Observation,Integer>> lobs) {
			this.date 	= new Date ();
			this.well 	= false;
			this.stench = false;
			this.initAddObservations(lobs);
		}
		
		/**
		 * Retourne vrai si un danger existe
		 * @return
		 */
		public boolean isSansDanger () {
			return this.well==false && this.stench==false;
		}
		
		/**
		 * Initialise les attributs de la classe en fonction du danger observe
		 * @param lobs
		 */
		private void initAddObservations (List<Couple<Observation,Integer>> lobs) {
			for (Couple<Observation,Integer> obs : lobs) {
				if (obs.getLeft() == Observation.STENCH) {
					this.stench = true;
				}
				if (obs.getLeft() == Observation.WIND) {
					this.well = true;
				}
			}
		}
		
		
		/**
		 * Mise a jour du danger courant avec un danger potentielle si la date est anterieur
		 * @param danger
		 */
		public void updateObservations (Danger newDanger) {
			// Si la date du danger courant anterieur :
			if (this.date.before(newDanger.getDate())) {
				// Mise a jour avec l objet Danger local:
				this.date	= newDanger.getDate();
				this.well	= newDanger.getWell();
				this.stench = newDanger.getStench();
			}
		}
		
		/**
		 * Affichage de l'objet Danger
		 */
		public String toString () {
			String res = "";
			res += "Le Danger contient : \n";
			res += "Date : " + this.date.toString() + "\n";
			res += "Well : " + this.well + "\n";
			res += "Stench : " + this.stench + "\n";
			return res;
		}
		
		/**
		 * GETTER
		 */
		
		public Date getDate() {return this.date;}
		public Boolean getStench () {return this.stench;}
		public Boolean getWell () {return this.well;}
	}
}