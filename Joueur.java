package com.sokoban;

import java.util.ArrayList;

/**
 * 
 */
public class Joueur extends Mobile {
	
    private ArrayList<Direction> histo;
    private Direction regard = Direction.DROITE;
    private int balles;
    
    /**
     * @param Configuration 
     * @param Position
     */
    public Joueur(Configuration conf, Position position, int balles) {
    	super(Type.JOUEUR,conf,position);
    	this.balles = balles;
    	this.histo = new ArrayList<Direction>();
    }

    /**
     * @param Configuration 
     * @param Joueur
     */
    public Joueur(Configuration conf, Joueur joueur) {
    	super(Type.JOUEUR,conf,joueur.getPosition());
    	this.histo = joueur.getHisto();
    	this.balles = joueur.balles;
    }

    /**
     * @return histo
     */
    public ArrayList<Direction> getHisto() {
        return this.histo;
    }
    
    /**
     * @return histo
     */
    public int getBalles() {
        return this.balles;
    }
    
    /**
     * @param nbBalles Nombres de balle du joueur
     */
    public void setBalles(int nbBalles) {
        this.balles = nbBalles;
    }
    
    public void addBalle() {
        this.balles++;
    }
    
    /**
     * @param dir Direction du tir
     */
    public void tirer(Direction dir) {
    	//Si le nombre de balle est superieur a 0
    	if (this.balles >= 0) {
    		//Recupere la position du joueur
            Position pos = this.getPosition();
            //Recupere la config
            Configuration config = this.getConfig();
            //Recupere l'element a la position
            Element e = config.get(pos);
            //Tant que l'element n'est ni un mur, ni un policier, ni un diamant
            while (!e.getType().equals(Type.MUR) && (!e.getType().equals(Type.POLICIER)) && (!e.getType().equals(Type.DIAMANT))){
            	//Recupere la position suivante dans la direction du tir
            	pos = pos.add(dir);
            	//Recupere l'element a la position
            	e = config.get(pos);
            }
            //Si le tir s'est arete sur un policier, le retire de la liste des policier
            if(e.getType().equals(Type.POLICIER)) {
            	config.removePolicier(pos);
            }
            //Retire une balle au joueur
            this.balles--;
    	}
    }
    
    public Direction getRegard() {
    	return this.regard;
    }
    
    public void setRegard(Direction regard) {
    	this.regard = regard;
    }
    
//	/**
//     * @return
//     */
//    public Direction getRegard() {
//    	Direction res = Direction.DROITE;
//        if (!this.histo.isEmpty()) {
//        	res = this.histo.get(this.histo.size()-1);
//        }
//        return res;
//    }
    
    public void addHisto(Direction dir) {
    	this.histo.add(dir);
    }
    

}