package eu.su.mas.dedaleEtu.mas.knowledge.yours.Cartes;

import java.io.Serializable;

import eu.su.mas.dedaleEtu.mas.knowledge.yours.Satisfaction.Identifiant;

public class Carte implements Serializable {
	
	private static final long serialVersionUID = -343539910427469041L;
	
	private SerializableCarteExploration  	carteExploration;
	private CarteTresors	 				carteTresors;
	private CarteDangers					carteDangers;
	private Identifiant						id;
	private String							positionTresorValide;
	
	public Carte (SerializableCarteExploration carteExploration, CarteTresors carteTresors, CarteDangers carteDangers) {
		this.carteExploration 	=	carteExploration;
		this.carteTresors		=	carteTresors;
		this.carteDangers		=	carteDangers;
	}
	
	public Carte (Identifiant id, SerializableCarteExploration carteExploration, CarteTresors carteTresors, CarteDangers carteDangers) {
		this.id = id;
		this.carteExploration 	=	carteExploration;
		this.carteTresors		=	carteTresors;
		this.carteDangers		=	carteDangers;	
	}
	
	public Carte (Identifiant id, CarteTresors carteTresors, CarteDangers carteDangers, String posTresor) {
		this.carteExploration = null;
		this.id = id;
		this.carteTresors		=	carteTresors;
		this.carteDangers		=	carteDangers;
		this.positionTresorValide	= posTresor;
	}
	
	public Carte (CarteTresors carteTresors, CarteDangers carteDangers) {
		this.carteExploration = null;
		this.id = null;
		this.carteTresors		=	carteTresors;
		this.carteDangers		=	carteDangers;
	}
	
	/**
	 * Mise a jour de la carte make_update_carteExploration via carteExploration
	 * @param make_update_carteExploration
	 */
	public void updateCarteExplorations(CarteExploration make_update_carteExploration) {
		this.carteExploration.fusionGrapheNonSerializable(make_update_carteExploration);
	}
	
	/**
	 *  Mise a jour de la carte make_update_carteTresors via carteTresors
	 * @param make_update_carteTresors
	 */
	public void updateCarteTresors(CarteTresors make_update_carteTresors) {
		make_update_carteTresors.updateTresors(this.carteTresors);
	}
	
	/**
	 * Mise a jour de la carte make_update_carteTresors via carteDanger
	 * @param make_update_carteDangers
	 */
	public void updateCarteDanger(CarteDangers make_update_carteDangers) {
		make_update_carteDangers.updateDangers(this.carteDangers);
	}
	
	/**
	 * Update de l'ensemble des cartes 
	 * @param make_update_carteExploration
	 * @param make_update_carteTresors
	 * @param make_update_carteDangers
	 */
	public void updateInsertCartes(CarteExploration make_update_carteExploration,
			CarteTresors make_update_carteTresors,
			CarteDangers make_update_carteDangers) {
		this.updateCarteExplorations(make_update_carteExploration);
		this.updateCarteTresors(make_update_carteTresors);
		this.updateCarteDanger(make_update_carteDangers);
	}
	
	public void updateInsertCartes(
			CarteTresors make_update_carteTresors,
			CarteDangers make_update_carteDangers) {
		this.updateCarteTresors(make_update_carteTresors);
		this.updateCarteDanger(make_update_carteDangers);
	}
	
	public String toString() {
		return "CarteExplorateur + CarteTresors + CarteDangers";
	}
	
	/**
	 * GETTER
	 */
	
	public SerializableCarteExploration getCarteExplorations () {
		return this.carteExploration;
	}
	
	public CarteTresors  getCarteTresors () {
		return this.carteTresors;
	}
	
	public CarteDangers	 getCarteDangers () {
		return this.carteDangers;
	}
	
	public Identifiant 	getIdentifiant () {
		return this.id;
	}
	
	public String	getPositionTresor (){
		return this.positionTresorValide;
	}
}
