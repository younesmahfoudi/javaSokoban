package com.sokoban;

public enum Type {
	MUR("Mur",1),
	CASE("Case",0),
	DIAMANT("Diamant",3),
	JOUEUR("Joueur",2),
	POLICIER("Policier",4);
	
	private String sType;
	private int iType;
	
	Type(String sType,int iType){
		this.sType = sType;
		this.iType = iType;
	}

	public String getsType() {
		return sType;
	}

	public void setsType(String sType) {
		this.sType = sType;
	}

	public int getiType() {
		return iType;
	}

	public void setiType(int iType) {
		this.iType = iType;
	}
	
	@Override
	public String toString(){
	    return "Type [Type= "+this.sType+", Indice = "+iType+"]";
	}
}
