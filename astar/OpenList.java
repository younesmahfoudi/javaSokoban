package astar;


import java.util.ArrayList;

public class OpenList {
	
	private ArrayList<Point> liste;
	
	public OpenList(Point depart) {
		ArrayList<Point> openList = new ArrayList<Point>();
		openList.add(depart);
		this.liste = openList;
	}
	
	public ArrayList<Point> getListe() {
		return liste;
	}
	
	public void setListe(ArrayList<Point> liste) {
		this.liste = liste;
	}
	
	public void add(Point point) {
		//si le point est deja dans la liste, on regarde si le nouveau chemin est plus rapide que l'ancien
		if (this.liste.contains(point)) {
			//si le nouveau chemin est plus rapide, alors on change la liste des parents
			if(this.liste.get(this.liste.indexOf(point)).getF() < point.getF()) {
				this.liste.get(this.liste.indexOf(point)).setParents(point.getParents());
			}
		// si le point n'est pas dans la liste, on l'ajoute
		} else { 
			this.liste.add(point);
		}
	}
	
	public int meilleur() {
		// initialisation de l'indice du point avec le meilleur potentiel
		int i=0;
		// parcours des points de la liste
		for (int j=1; j<this.liste.size(); j++) {
			// si ce point a un meilleur potentiel, on change l'indice i
			if (this.liste.get(i).getF()>this.liste.get(j).getF()) {
				i=j;
			}
		}
		return i;
	}
}