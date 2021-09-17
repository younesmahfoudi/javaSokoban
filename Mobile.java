package com.sokoban;

/**
 * 
 */
public abstract class Mobile extends Element {
    /**
     * 
     */
    private Configuration config;
    /**
     * 
     */
    private Position position;

    /**
     * @param Type 
     * @param Configuration 
     * @param Position
     */
    public Mobile(Type type,Configuration conf,Position pos) {
    	super(type);
    	this.config = conf;
    	this.position = pos;
    }

    /**
     * @param Position 
     * @return
     */
    public Boolean setPosition(Position pos) {
    	Boolean res = false;
    	if (this.config.get(pos).getType().equals(Type.CASE)){
    		this.position = pos;
    		res = true;
    	}
        return res;
    }

    /**
     * @return
     */
    public Position getPosition() {
        return this.position;
    }
    
    /**
	 * @return the config
	 */
	public Configuration getConfig() {
		return config;
	}

	public Boolean bougerVers(Direction dir) {
    	Boolean res = false;
    	Position nPos = this.position.add(dir);
    	if (this.config.get(nPos).getType().equals(Type.CASE)){
    		res = true;
    	}
    	return res;
    }
    
}