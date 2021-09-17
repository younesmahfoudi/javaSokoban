package com.sokoban;

/**
 * 
 */
public class Position {
 
    private int x;
    private int y;

	//GETTER AND SETTER//
    public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	/**
     * @param x 
     * @param y
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @param Position
     */
    public Position(Position pos) {
        this.x = pos.x;
        this.y = pos.y;
    }

    /**
     * @param Direction 
     * @return
     */
    public Position add(Direction dir) {
    	if (dir != null) {
	    	int newX = this.x + dir.getDx();
	    	int newY = this.y + dir.getDy();
	        Position res = new Position(newX,newY);
	        return res;
    	} else {
    		return this;
    	}
    }

    /**
     * @param Direction 
     * @return
     */
    public Position sub(Direction dir) {
    	Position res = new Position(this.x - dir.getDx(), this.y - dir.getDy());
        return res;
   }
    
    /**
     * @param Object 
     * @return
     */
    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
    
    //TOSTRING//
    @Override
	public String toString() {
		return "Position [x=" + x + ", y=" + y + "]";
	}
    
    public Direction directionVers(Position pos) {
    	// on regarde si il faut ce deplacer à droite
    	if (this.x-pos.getX() == 1) {
    		return Direction.HAUT;
    	//on regarde si il faut ce deplacer à gauche
    	} else if (this.x-pos.getX() == -1) {
    		return Direction.BAS;
    	//on regarde si il faut ce deplacer en bas
    	} else if (this.y-pos.getY() == 1) {
    		return Direction.GAUCHE;
    	//il faut donc ce deplacer en haut
    	} else {
    		return Direction.DROITE;
    	}
    }
}