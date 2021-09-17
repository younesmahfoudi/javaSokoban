package com.sokoban;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 */
public class Configuration {
	private ArrayList<Diamant> diamants;
	private ArrayList<Policier> policiers;
    private Joueur joueur;
    private Niveau niveau;

    /**
     * @param niv 
     * @param positionJoueur
     */
    public Configuration(int numNiv) throws IOException{
    	BufferedReader lecteur = null; //Lecteur du fichier
    	String ligne; //Variable d'une ligne			
        Integer cmpLigne = 0; //Compteur de ligne lu
        
        try {
			this.niveau = new Niveau(numNiv);
		} catch (IOException e) {
			e.printStackTrace();
		}
        this.diamants = new ArrayList<Diamant>();
        this.policiers = new ArrayList<Policier>();
        
        //lecture des diamants et policiers
        try {
        	//Essaye de lire le fichier du niveau
			lecteur = new BufferedReader(new FileReader("src/ressources/niveaux/"+numNiv+".txt"));
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
        		//Incremente le nombre de ligne lu
        		cmpLigne++;
        		break;
        	//S'il sagit de la seconde ligne
        	case 1:
        		//Increment le nombre de ligne lu
        		cmpLigne++;
        		break;
        	//S'il sagit de la troisieme ligne (ligne de la position de depart du perso)
        	case 2:
        		//séparer la ligne en deux
        		String position[] = ligne.split(",");
        		//Stock la position x du joueur
        		int x = Integer.parseInt(position[0]);
        		//Stock la position y du joueur
        		int y = Integer.parseInt(position[1]);
        		//Création du joueur
        		this.joueur = new Joueur(this, new Position(x, y) , 0);
        		//Incremente le nombre de ligne lu
        		cmpLigne++;
        		break;
        	//Dans tous les autres cas
        	default:
        		//Pour chaque caractere de la ligne
        		for (int j=0;j<ligne.length();j++) {
        			//Stock le caractere
        			char verif = ligne.charAt(j);
        			//S'il sagit d'un 3, creer un diamant
        			if (Character.getNumericValue(verif) == 3) {
        				this.getDiamants().add(new Diamant(this, new Position(cmpLigne-3, j)));
        			//Sinon, s'il sagit d'un 4, créer un policier
        			} else if (Character.getNumericValue(verif) == 4){
        				this.getPoliciers().add(new Policier(this, new Position(cmpLigne-3, j)));
        			}
        		}
        		//Incremente le nombre de ligne lu
        		cmpLigne++;
        	}
        }
        //Set le nombre de balle du joueur au nombre exacte de policier
        this.joueur.setBalles(1);
        //Ferme le fichier
        lecteur.close();
        this.getNiveau().setMatrice();
    }

    /**
     * @param config
     */
    public Configuration(Configuration config) {
    	this.diamants = config.getDiamants();
    	this.policiers = config.getPoliciers();
    	this.joueur = config.getJoueur();
    	this.niveau = config.getNiveau();
    }

    /**
     * @return
     */
    public Integer getX() {
        // retourner la valeur X de la position du joueur
        return this.getJoueur().getPosition().getX();
    }

    /**
     * @return
     */
    public Integer getY() {
    	// retourner la valeur Y de la position du joueur
    	return this.getJoueur().getPosition().getY();
    }

    /**
     * @param pos Position a chercher 
     * @return l'element contenu a la position donnee
     */
    public Element get(Position pos) {
        //Pour chaque diamant, on verifie sa position
        for (Diamant diamant : this.getDiamants()) {
        	if (diamant.getPosition().equals(pos)) {
        		//Si la position se trouve a notre emplacement de verification, on le retourne
        		return diamant;
        	}
        }
        
        //Pour chaque diamant, on verifie sa position
        for (Policier policier : this.getPoliciers()) {
        	if (policier.getPosition().equals(pos)) {
        		//Si la position se trouve a notre emplacement de verification, on le retourne
        		return policier;
        	}
        }
        
        //Si le joueur se trouve a la position verifie, on le retourne
        if (this.getJoueur().getPosition().equals(pos)) {
        	return this.getJoueur();
        }
        
        //si aucun "mobile" n'est à la position, on renvoie l'imobille de la grille du niveau, donc "mur" ou "case"
        return this.getNiveau().getGrille()[pos.getX()][pos.getY()];
    }

    /**
     * @param pos Position de la case a verifier 
     * @return Vrai si la position contient un diamant et est une cible
     */
    public boolean estVide(Position pos) {
    	for (Diamant diamant : this.getDiamants()) {
        	if (diamant.getPosition().equals(pos)) {
        		//Si la position se trouve a notre emplacement de verification, on le retourne
        		return false;
        	}
        }
        return (this.getNiveau().getCibles().contains(pos) /**&& this.getDiamants().contains(pos)**/);
    }

    /**
     * @param Position 
     * @return
     */
    public boolean estCible(Position pos) {
        return this.getNiveau().estCible(pos);
    }

    /**
     * @param Direction 
     * @return
     */
    public boolean bougerJoueurVers(Direction dir) {
    	boolean res = this.joueur.bougerVers(dir);
    	Position newPos = joueur.getPosition().add(dir);
    	if (res) {
    		res = this.joueur.setPosition(newPos);
        	this.joueur.addHisto(dir);
    	}else if (this.get(newPos).getType().equals(Type.DIAMANT)) {
    		if (this.get(newPos).bougerVers(dir)) {
    			Position newPos1 = newPos.add(dir);

    		    
    			this.diamants.remove(this.diamants.get(diamants.indexOf(this.get(newPos))));
    			this.diamants.add(new Diamant(this,newPos1));
    			
    			res = this.joueur.setPosition(newPos);
    	    	this.joueur.addHisto(dir);
    		}
    	}
    	return res;
    }

    /**
     * @return
     */
    public boolean victoire() {
    	//on parcours tous les diamants
    	for(Diamant diamant : this.getDiamants()) {
    		//si un diament n'est pas sur une cible
    		if(!this.estCible(diamant.getPosition())){
    			//on retourne false
    			return false;
    		}
    	}
    	//si tous les diamant sont sur des cibles on retourne true
        return true;
    }

	public ArrayList<Diamant> getDiamants() {
		return diamants;
	}

	public Joueur getJoueur() {
		return joueur;
	}

	public Niveau getNiveau() {
		return niveau;
	}

	public ArrayList<Policier> getPoliciers() {
		return policiers;
	}
	
	public void removePolicier(Position pos) {
		Policier remove = null;
		for (Policier policier : this.policiers) {
			if (policier.getPosition().equals(pos)) {
				remove = policier;
			}
		}
		this.policiers.remove(remove);
	}

}