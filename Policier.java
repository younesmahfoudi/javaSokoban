package com.sokoban;

import java.util.ArrayList;

import astar.Astar;
import astar.Matrice;

/**
 * 
 */
public class Policier extends Mobile {
    /**
     * 
     */
    public ArrayList<Direction> histo;
    public Direction regard = Direction.DROITE;
    public boolean etatAlerte;
    /**
     * @param Configuration 
     * @param Position
     */
    public Policier(Configuration conf, Position position) {
        super(Type.POLICIER,conf,position);
        this.histo = new ArrayList<Direction>();
    }

    public Direction getRegard() {
        return this.regard;
    }
    
    public void setRegard(Direction regard) {
		this.regard = regard;
	}
    
    public Boolean getEtatAlerte(){
    	return this.etatAlerte;
    }
    
    public Direction deplacementPolicier() {
    	//si le policier n'est pas en alerte, on regarde s'il le deviens ce tour
    	Direction direction = null;
    	if (!this.etatAlerte) {
    		int xPolicier=this.getPosition().getX();
    		int yPolicier=this.getPosition().getY();
    		int xJoueur=this.getConfig().getJoueur().getPosition().getX();
    		int yJoueur=this.getConfig().getJoueur().getPosition().getY();
    		//si le joueur se trouve à 3 cases du policier, il entre en alerte
    		if (Math.abs(xPolicier-xJoueur)+Math.abs(yPolicier-yJoueur) <= 3) {
    			etatAlerte = true;
    		}
    	}
    	//si le policier est en alerte, il poursuit le joueur
    	if (this.etatAlerte) {
    		// ajout des mobiles sur la map pour l'application de A*
    		Matrice newMap= new Matrice(this.getConfig().getNiveau().getMatrice());
    		newMap.ajoutDesMobiles(this.getConfig());
    		//ajout du point de depart
    		newMap.setDepart(this.getPosition());
    		//ajout du point d'arrive
    		newMap.setArrive(this.getConfig().getJoueur().getPosition());
    		//application du code A*
    		direction = Astar.aStar(newMap);
    		//changement du regard du policier
    		if (direction != null) {
    			this.regard = direction;
    		}
    		//renvoie de la direction à suivre
    		if (this.getConfig().getJoueur().getPosition().equals(this.getPosition().add(direction))) {
    			return null;
    		}
    		return direction;
    	//le policier n'est pas en alerte
    	} else {
    		// le policier ce deplace en fonction de son regard
    		//si le policier peut se deplacer dans cette direction, il le fait
    		if ((this.bougerVers(this.regard)) && (!this.getConfig().estCible(this.getPosition().add(this.regard)))) {
    			//renvoie de la direction à suivre
    			return (this.regard);
    		//sinon il fait demi-tour
    		} else {
    			// inversion de la direction
    			this.setRegard(this.regard.oppose());
    			//renvoie la direction à suivre
    			return (this.regard);
    		}
    	}
    }

}