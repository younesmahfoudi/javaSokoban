package astar;

import com.sokoban.Position;
import com.sokoban.Immobile;
import com.sokoban.Policier;

import java.util.ArrayList;

import com.sokoban.Configuration;
import com.sokoban.Diamant;


public class Matrice {

	private Point[][] matrice;
	private Point depart;
	private Point arrive;
	
	public Point[][] getMatrice() {
		return matrice;
	}
	
	public Matrice(Matrice map) {
		this.depart = null;
		this.arrive = null;
		int maxX = map.getMatrice().length;
		int maxY = map.getMatrice()[0].length;
		Point [][] newMatrice = new Point[maxX][maxY];
		for (int x=0;x<maxX;x++) {
			for (int y=0;y<maxY;y++) {
				newMatrice[x][y] = new Point(map.getMatrice()[x][y].getValeur(),x,y);
			}
		}
		this.matrice = newMatrice;
	}
	
	public void ajoutDesMobiles(Configuration conf) {
		//création du raccourci pour la liste des diamant
		ArrayList <Diamant> listeDiamant = conf.getDiamants();
		//prise en compte de diamants en tant que mur pour A*
		for (int i=0;i<listeDiamant.size();i++) {
			this.setValeur(1, listeDiamant.get(i).getPosition());
		}
		//création du raccourci pour la liste des policiers
		ArrayList <Policier> listePolicier = conf.getPoliciers();
		//prise en compte des policiers en tant de mur pour A*
		for (int i=0;i<listePolicier.size();i++) {
			this.setValeur(1, listePolicier.get(i).getPosition());
		}
		//on renvoie la carte la carte avec tout les murs initialisés
	}
	
	public Matrice(Immobile [][] m){
		// calcule de la taille de la matrice
		int maxX = m.length;
		int maxY = m[0].length;
		// création de la matrice
		Point[][] matrice = new Point[maxX][maxY];
		//initialisation des valeurs des points de la matrice
		for(int x=0;x<maxX;x++) {
			for(int y=0;y<maxY;y++) {
				//valeurs en fonction du caractère de l'immobile.
				//0 pour une case; 1 pour un mur
				matrice[x][y] = new Point(m[x][y].getType().getiType(),x,y);
			}
		}
		this.depart = null;
		this.arrive = null;
		this.matrice = matrice;
	}
	
	public Point getDepart() {
		return depart;
	}
	
	public void setDepart(Position pos) {
		this.matrice[pos.getX()][pos.getY()].setValeur(0);
		this.depart = this.matrice[pos.getX()][pos.getY()];
		
	}
	
	public Point getArrive() {
		return arrive;
	}
	
	public void setArrive(Position pos) {
		this.arrive = this.matrice[pos.getX()][pos.getY()];
	}
	
	public void setValeur(int valeur,Position pos) {
		this.matrice[pos.getX()][pos.getY()].setValeur(valeur);
	}
	
	public void etudeVoisins(Point point,OpenList openList){
		// création de raccourcis pour rendre plus lisible le code
		int x = point.getPos().getX();
		int y = point.getPos().getY();
		//initialisation de la liste des voisins
		Point voisin = null;
		// on verifie que le point n'est pas sur le bord gauche
		if (x != 0) {
			// on verifie que le voisin de gauche soit une case vide
			if (this.matrice[x-1][y].getValeur() == 0) {
				//on initialise les valeurs du voisin
				voisin=this.matrice[x-1][y];
				//on calcule les bonnes valeurs
				voisin.calculeValeur(point,this.arrive);
				//on l'ajoute à la liste des points à étudier
				openList.add(voisin);
			}
		}
		// on verifie que le point n'est pas sur le bord droit
		if (x != this.matrice.length-1) {
			//on verifie que le voisin de droite soit une case vide
			if (this.matrice[x+1][y].getValeur() == 0) {
				//on initialise les valeurs du voisin
				voisin=this.matrice[x+1][y];
				//on calcule les bonnes valeurs
				voisin.calculeValeur(point,this.arrive);
				//on l'ajoute à la liste des points à étudies
				openList.add(voisin);
			}
		}
		// on verifie que le point n'est pas sur le bord haut
		if (y != 0) {
			//on verifie que le voisin du haut soit une case vide
			if (this.matrice[x][y-1].getValeur() == 0) {
				//on initialise les valeurs du voisin
				voisin=this.matrice[x][y-1];
				//on calcule les bonnes valeurs
				voisin.calculeValeur(point,this.arrive);
				//on l'ajoute à la liste des points à étudier
				openList.add(voisin);
			}
		
		}
		//on verifie que le point n'est pas sur le bord bas
		if (y != this.matrice[0].length-1) {
			//on verifie que le voisin du bas soit une case vide
			if (this.matrice[x][y+1].getValeur() == 0) {
				//on initialise les valeurs du voisin
				voisin=this.matrice[x][y+1];
				//on calcule les bonnes valeurs
				voisin.calculeValeur(point,this.arrive);
				//on l'ajoute à la lsite des points à étudier+
				openList.add(voisin);
			}
		}
		//on change la valeur du point pour ne pas avoir à l'étudier de nouveau
		//permet de remplacer la "closeList"
		point.setValeur(2);
		//on retire ce point de la liste des points à étudier
		openList.getListe().remove(point);
	}
}