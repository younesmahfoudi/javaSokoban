package com.sokoban;

/**
 * 
 */
public abstract class Immobile extends Element {

    /**
     * @param String
     */
    public Immobile(Type type) {
        super(type);
    }
    
    public Boolean bougerVers(Direction dir) {
    	return false;
    }
    
    public Boolean setPosition(Position pos) {
    	return false;
    }
}