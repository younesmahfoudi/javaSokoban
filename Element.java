package com.sokoban;

/**
 * 
 */
public abstract class Element {
    private Type type;

    /**
     * @param type
     */
    public Element(Type type) {
        this.type = type;
    }

    /**
     * @return Type
     */
    public Type getType() {
        return this.type;
    }
    
    abstract Boolean bougerVers(Direction dir);
    abstract Boolean setPosition(Position pos);

}