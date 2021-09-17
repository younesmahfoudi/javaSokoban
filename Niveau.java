package com.sokoban;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import astar.Matrice;

/**
 * 
 */
public class Niveau {
    /**
     * 
     */
    private Immobile[][] grille;
    /**
     * 
     */
    private Matrice matrice;
    
    private ArrayList<Position> cibles;
 
    
    public void setMatrice() {
		this.matrice = new Matrice(this.grille);
	}

    @SuppressWarnings("exports")
	public Matrice getMatrice() {
		return matrice;
	}

    /**
     * @return
     * @param n numero du niveau a charger
     * @throws IOException 
     */
    public Niveau(int n) throws IOException {
        BufferedReader lecteur = null;		//Lecteur du fichier
        String ligne;		//Variable d'une ligne			
        Integer cmpLigne = 0;		//Compteur de ligne lu
        this.cibles = new ArrayList<Position>();		//Creation de la liste de cible
        int x = 0;	//Variable de la longeur de la grille
        int y = 0;	//Largeur de la grille
        int i = 0;		//Indice de la ligne traite
        try {
        	//Essaye de lire le fichier du niveau
			lecteur = new BufferedReader(new FileReader("src/ressources/niveaux/"+n+".txt"));
		} catch (FileNotFoundException e) {
			//Si erreur, affiche une Erreur d'ouverture
			System.out.println("Erreur d'ouverture");
			e.printStackTrace();
		}
        //Tant qu'il y a une ligne a lire
        while ((ligne = lecteur.readLine()) != null) {
        	switch (cmpLigne) {
        	//S'il sagit de la premiere ligne
        	case 0:
        		//Stock la longeur de la grille
        		x = Integer.parseInt(ligne);
        		//Incremente le nombre de ligne lu
        		cmpLigne++;
        		break;
        	//S'il sagit de la seconde ligne
        	case 1:
        		//Stock la largeur de la grille
        		y = Integer.parseInt(ligne);
        		//Initialise la grille
        		this.grille = new Immobile[x][y];
        		//Increment le nombre de ligne lu
        		cmpLigne++;
        		break;
        	//S'il sagit de la troisieme ligne (ligne de la position de depart du perso)
        	case 2:
        		//Incremente le nombre de ligne lu
        		cmpLigne++;
        		break;
        	//Dans tous les autres cas
        	default:
        		//Pour chaque caractere de la ligne
        		for (int j=0;j<ligne.length();j++) {
        			//Stock le caractere
        			char verif = ligne.charAt(j);
        			//S'il sagit d'un 1, creer un mur
        			if (Character.getNumericValue(verif) == 1) {
        				this.grille[i][j] = new Mur();
        			//Sinon, s'il sagit d'un 0,2,3 ou 4, cree une case
        			} else if ((Character.getNumericValue(verif) == 0) || (Character.getNumericValue(verif) == 2) || (Character.getNumericValue(verif) == 3) || (Character.getNumericValue(verif) == 4)){
        				this.grille[i][j] = new Case();
        				//s'il sagit d'un 2, stock la position comme cible
        				if (Character.getNumericValue(verif) == 2) {
        					this.cibles.add(new Position(i,j));
        				}
        			}
        		}
        		//Incremente le nombre de ligne de la grille traite
        		i++;
        		//Incremente le nombre de ligne lu
        		cmpLigne++;
        	}
        }
        //Ferme le fichier
        lecteur.close();
    }
    
    /**
	 * @return the grid
	 */
	public Immobile[][] getGrille() {
		return grille;
	}
	/**
     * @return length of the grid
     */
    public int getX() {
        return this.getGrille().length;
    }

    /**
     * @return width of the grid
     */
    public int getY() {
        return this.getGrille()[0].length;
    }

    /**
     * @param pos Position a verifier
     * @return Vrai si la position est une cible, Faux sinon
     */
    public boolean estCible(Position pos) {
        boolean res = false;
        int i = 0;
        while (!res && (i<this.getCibles().size())) {
        	if (this.getCibles().get(i).equals(pos)) {
        		res = true;
        	}
        	i++;
        }
        return res;
    }

    //Affiche la grille de jeu avec les murs et les cases
    public void affGrille() {
    	//Recupere la taille de la grille
    	int x = this.getX();
    	int y = this.getY();
    	//Indice de deplacement sur la grille
    	int i;
    	int j;
    	//Pour chaque ligne
    	for(i=0;i<x;i++) {
    		//Pour chaque colonne
    		for(j=0;j<y;j++) {
    			//Affiche le Type de la case
    			System.out.print(this.grille[i][j].getType());
    		}
    		//Retour a la ligne
    		System.out.println("");
    	}
    }
	public ArrayList<Position> getCibles() {
		return cibles;
	}

}