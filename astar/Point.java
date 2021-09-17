package astar;

import com.sokoban.Position;

import java.util.ArrayList;
import java.lang.Math;

public class Point {
	
	private int valeur;
	private Position pos;
	private int cout;
	private int f;
	private int h;
	private ArrayList<Position> parents;
	
	public Point(int valeur,int x,int y) {
		this.valeur = valeur;
		this.pos = new Position(x,y);
		this.cout = -1;
		this.f = -1;
		this.h = -1;
		this.parents = new ArrayList<Position>();
	}
	
	public void calculeValeur(Point point,Point arrive) {
		//calcule de la distance en vol d'oiseau entre le point et l'arrive
		this.heuristique(arrive);
		//calcule du cout de deplacement du depart à ce point
		this.cout(point);
		//calcule des parents de ce point
		this.parents(point);
		//calcule de F, qui permet de savoir si le point est le prochain utilisé
		this.setF(this.getCout()+this.getH());
	}
	
	public int getValeur() {
		return valeur;
	}
	
	public Position getPos() {
		return this.pos;
	}
	
	public int getCout() {
		return cout;
	}
	
	public int getF() {
		return f;
	}
	
	public int getH() {
		return h;
	}
	
	public ArrayList<Position> getParents() {
		return parents;
	}
	
	public void setValeur(int valeur) {
		this.valeur = valeur;
	}
	
	public void setCout(int cout) {
		this.cout = cout;
	}
	
	public void setF(int f) {
		this.f = f;
	}
	
	public void heuristique(Point arrive) {
		//calcule de la distance en nombre de case à vol d'oiseau entre cette case et celle d'arrive
		this.h = (Math.abs(this.pos.getX()-arrive.getPos().getX())+Math.abs(this.pos.getY()-arrive.getPos().getY()));
	}
	
	public void cout(Point point) {
		//calcule du cout de déplacement du point de dépar à celui-ci
		this.cout = point.cout+1;
	}

	
	public void parents(Point point) {
		//copie de la liste des parents
		ArrayList<Position> newParents = new ArrayList<>(point.getParents());
		//ajout de la pos du point en argument
		newParents.add(point.getPos());
		//renvoie de la liste de parents
		this.parents = newParents;
	}
	
	public void setParents(ArrayList<Position> parents) {
		this.parents = parents;
	}
	
	public boolean equal(Point point) {
		//si les points on les mêmes coordonnées, ce sont les mêmes
		if(this.pos.equals(point.getPos())) {
			return true;
		} else {
			return false;
		}	
	}	
}