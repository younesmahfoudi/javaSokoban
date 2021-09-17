package astar;

import java.util.ArrayList;
import com.sokoban.Position;
import com.sokoban.Direction;

public class Astar {
	
	public static Direction aStar(Matrice matrice){
		//création de l'openList
		OpenList openList = new OpenList(matrice.getDepart());
		//initialisation du l'indice du point avec le meilleur potentiel
		int meilleur =0 ;
		// ajout a l'openList du point de depart
		Point pointEtudie = openList.getListe().get(0);
		// ajout des voisins du point de depart
		// on fait un tours avant pour respecter les conditions de fin de la boucle while
		matrice.etudeVoisins(pointEtudie, openList);
		// on continue tant que l'on a pas trouvé le point d'arriver
		// ou tant qu'il reste des points à étudier
		while((!pointEtudie.equal(matrice.getArrive())) && (!openList.getListe().isEmpty())) {
			meilleur = openList.meilleur();
			pointEtudie = openList.getListe().get(meilleur);
			matrice.etudeVoisins(pointEtudie, openList);
		}
		// on verifier si la boucle c'est arrété car tout les points on été étudier
		if ((openList.getListe().isEmpty()) && (!pointEtudie.equal(matrice.getArrive()))) {
			//on renvoie null car il n'y a pas de chemin.
			return null;
		// il y a donc un chemin possible
		} else {
			// on prends la liste des parents du point d'arrivé
			ArrayList<Position> chemin = pointEtudie.getParents();
			if (chemin.size() == 1) {
				Direction direction = chemin.get(0).directionVers(matrice.getArrive().getPos());
				return direction;
			}
			// on calcule la direction à suivre en fonction du point de depart et le suivant dans le chemin
			Direction direction = chemin.get(0).directionVers(chemin.get(1));
			// on renvoie la direction à suivre
			return direction;
		}
	}
	
}
