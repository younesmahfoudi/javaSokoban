package com.sokoban.app;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CreationNiveau extends Application {
	private int numeroNiveau;
	private GridPane affGrille;
	private Label[][] tmpGrille;
	private int largeur;
	private int longueur;
	private VBox root;
	private Scene scene;
	private Label titleLabel;
	private Label nivLabel;
	private TextField largeurTF;
	private TextField longueurTF;
	private boolean joueur = false;
	private int nbEntrepot = 0;
	private int nbDiamant = 0;
	private String posJoueur;

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Sokoban - Creation du niveau " + this.numeroNiveau);// Change le titre de la fenetre pour Sokoban - Niveau + numero du niveau
		//Creation des labels de largeur et longeur
		Label largeur = new Label("Largeur de la grille : ");
		Label longueur = new Label("Longueur de la grille : ");
		//Creation des HBox
		HBox largeurHBox = new HBox();
		HBox longueurHBox = new HBox();
		//Ajout des elements dans les HBOX
		largeurHBox.getChildren().addAll(largeur,largeurTF);
		longueurHBox.getChildren().addAll(longueur,longueurTF);
		//Ajout des spacings
		largeurHBox.setSpacing(10);
		longueurHBox.setSpacing(10);
		//Creation des separateurs vertical
		Separator separator = new Separator();
		//Creation du boutons de validation
		Button validation1 = new Button("Validation");
		//Ajout de la fonction en cas de clique sur le bouton validation1
		validation1.setOnAction(e -> creationGrille());
		
		//Ajout des elements
		root.getChildren().addAll(titleLabel,nivLabel,separator,longueurHBox,largeurHBox,validation1);
		
		// Charge la scene a partir du parent
		scene.getStylesheets().add(getClass().getResource("/ressources/css/CreationNiveau.css").toString());
		
		// Affiche la scene dans la nouvelle fenetre
		primaryStage.setScene(scene);
		// On empeche de redimmenssionner la fenetre
		primaryStage.setResizable(false);
		// Affiche la fenetre au centre de l'ecran
		primaryStage.show();
		primaryStage.centerOnScreen();
	}

	private void creationGrille() {
		//Declaration des variables de déplacement sur la matrice
		int i;
		int j;
		//Variable d'erreur d'entree
		boolean erreur = false;
		//Recupere la largeur et la longueur
		try {
			this.largeur = Integer.parseInt(largeurTF.getText());
		} catch (Exception error) {
			erreur = true;
			erreur(0);
		}
		if (!erreur) {
			try {
				this.longueur = Integer.parseInt(longueurTF.getText());
				if ((this.largeur < 5) || (this.longueur < 5)) {
					erreur(4);
				} else {
					//Initialise la matrice de bouton et mets tous les boutons "mur"
					this.tmpGrille = new Label[this.longueur][this.largeur];
					for(i = 0;i<this.longueur;i++) {
						for (j = 0;j<this.largeur;j++) {
							this.tmpGrille[i][j] = new Label();
							this.tmpGrille[i][j].getStyleClass().clear();
							tmpGrille[i][j].getStyleClass().add("mur");
							if ((i == 0) || (j == 0) || (i == this.longueur-1) || (j == this.largeur-1)) {
								tmpGrille[i][j].setDisable(true);
							} else {
								int emplacementI = i;
								int emplacementJ = j;
								tmpGrille[i][j].setOnMouseClicked(event -> changeClass("mur",emplacementI,emplacementJ));
							}
						}
					}
					//Reset la grille d'affichage
					this.affGrille.getChildren().clear();
					//Pour chaque case, ajoute le label correspondant
					for (i=0;i<this.longueur;i++) {
						for (j=0;j<this.largeur;j++) {
							this.affGrille.add(tmpGrille[i][j], j, i);
						}
					}
					
					//Creation du boutons de validation
					Button validation2 = new Button("Validation");
					validation2.setOnAction(save -> sauvegarde());
					
					//Creation des separateurs vertical
					Separator separator = new Separator();
					Separator separator2 = new Separator();
					//Actualisation de la scene
					root.getChildren().clear();
					root.getChildren().addAll(titleLabel,nivLabel,separator,affGrille,separator2,validation2);
					//Actualisation de la taille de la fenetre
					root.getScene().getWindow().sizeToScene();
				}
			} catch (Exception error) {
				erreur(0);
			}
		}
	}

	private void erreur(int n) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("ATTENTION");
		switch(n) {
			case 0:
				alert.setHeaderText("Probleme d'entre");
				alert.setContentText("Merci de n'entrer que des nombres !");
				break;
			case 1:
				alert.setHeaderText("Probleme de joueur");
				alert.setContentText("Merci de placer un joueur");
				break;
			case 2:
				alert.setHeaderText("Probleme de diamant ou d'entrepot");
				alert.setContentText("Merci de placer autant de diamant que d'entrepot ( Au minimum un de chaque)");
				break;
			case 4:
				alert.setHeaderText("Probleme de dimensions");
				alert.setContentText("Un niveau doit avoir une dimension minimum de 5 par 5");
				break;
			default:
				alert.setHeaderText("Erreur creation de fichier");
				alert.setContentText("Il semblerait que l'application soit dans l'impossibilite de creer le fichier du niveau");
				break;
		}
		alert.show();

		new Thread(() -> {
			try {
				Thread.sleep(2000);
				Platform.runLater(() -> alert.hide());
			} catch (InterruptedException error2) {
				error2.printStackTrace();
			}
		}).start();
	}
	
	private void sauvegarde() {
		//Creation de la liste des lignes
		ArrayList<String> lignes = new ArrayList<String>();
 		if (!this.joueur) {
			erreur(1);
		} else if ((this.nbDiamant != this.nbEntrepot) || (this.nbDiamant == 0)) {
			erreur(2);
		} else {
			File niveau = new File("src/ressources/niveaux/"+this.numeroNiveau+".txt");
			try {
				//Creation du fichier
				niveau.createNewFile();
				//Recuperation du chemin d'acces au fichier
				Path fichier = Paths.get("src/ressources/niveaux/"+this.numeroNiveau+".txt");
				//Ajout des lignes de taille de la matrice et de la position du joueur
				lignes.add(Integer.toString(this.longueur));
				lignes.add(Integer.toString(this.largeur));
				lignes.add(this.posJoueur);
				//Creation du string temporaire
				String tmp = "";
				//Pour chaque ligne
				for(int i=0; i<this.longueur;i++) {
					//Pour chaque colonne
					for(int j=0;j<this.largeur;j++) {
						//s'il sagit d'un mur, on ajoute un 1 à tmp
						if (this.tmpGrille[i][j].getStyleClass().get(0) == "mur") {
							tmp += "1";
						//s'il sagit d'un joueur ou d'une case, on ajoute un 0 à tmp
						} else if ((this.tmpGrille[i][j].getStyleClass().get(0) == "case") || (this.tmpGrille[i][j].getStyleClass().get(0) == "joueur")) {
							tmp += "0";
						//s'il sagit d'un diamant, on ajoute un 3 à tmp
						} else if (this.tmpGrille[i][j].getStyleClass().get(0) == "diamant") {
							tmp += "3";
						//s'il sagit d'un entrepot, on ajoute un 2 à tmp
						} else if (this.tmpGrille[i][j].getStyleClass().get(0) == "entrepot") {
							tmp += "2";
						//s'il sagit d'un policier, on ajoute un 4 à tmp
						} else if (this.tmpGrille[i][j].getStyleClass().get(0) == "policier") {
							tmp += "4";
						}
					}
					//On sauvegarde la ligne dans la liste de lignes
					lignes.add(tmp);
					//On reset tmp
					tmp = "";
				}
				Files.write(fichier,lignes,Charset.forName("UTF-8"));
				Alert alert = new Alert(AlertType.WARNING);
				alert.setHeaderText("Succes");
				alert.setContentText("Le niveau a ete sauvegarde avec succes! A noter que le niveau sera accessible une fois le jeu relance.");
				alert.setTitle("Sokoban - Niveau "+this.numeroNiveau);
				alert.show();
			} catch (IOException e) {
				erreur(3);
			}
		}
	}

	private void changeClass(String styleClass,int i, int j) {
		this.tmpGrille[i][j].getStyleClass().clear();
		if (styleClass.equals("mur")) {
			this.tmpGrille[i][j].getStyleClass().add("case");
			tmpGrille[i][j].setOnMouseClicked(event -> changeClass("case",i,j));
		} else if (styleClass.equals("case")) {
			if (!this.joueur) {
				this.tmpGrille[i][j].getStyleClass().add("joueur");
				tmpGrille[i][j].setOnMouseClicked(event -> changeClass("joueur",i,j));
				this.joueur = true;
				this.posJoueur = Integer.toString(i)+","+Integer.toString(j);
			} else {
				this.tmpGrille[i][j].getStyleClass().add("diamant");
				tmpGrille[i][j].setOnMouseClicked(event -> changeClass("diamant",i,j));
				this.nbDiamant++;
			}
		} else if (styleClass.equals("joueur")) {
			this.joueur = false;
			this.tmpGrille[i][j].getStyleClass().add("diamant");
			tmpGrille[i][j].setOnMouseClicked(event -> changeClass("diamant",i,j));
			this.nbDiamant++;
		} else if (styleClass.equals("diamant")) {
			this.nbDiamant--;
			this.tmpGrille[i][j].getStyleClass().add("policier");
			tmpGrille[i][j].setOnMouseClicked(event -> changeClass("policier",i,j));
		} else if (styleClass.equals("policier")) {
			this.tmpGrille[i][j].getStyleClass().add("entrepot");
			tmpGrille[i][j].setOnMouseClicked(event -> changeClass("entrepot",i,j));
			this.nbEntrepot++;
		} else if (styleClass.equals("entrepot")) {
			this.nbEntrepot--;
			this.tmpGrille[i][j].getStyleClass().add("mur");
			tmpGrille[i][j].setOnMouseClicked(event -> changeClass("mur",i,j));
		}
	}

	/**
	 * @param numeroNiveau
	 */
	public CreationNiveau(int numeroNiveau) {
		super();
		//Set le numero du niveau
		this.numeroNiveau = numeroNiveau;
		//Initialise la grille d'affichage
		this.affGrille = new GridPane();
		//Creation du conteneur
		this.root = new VBox();
		//Creation de la scene
		this.scene = new Scene(root);
		//Creation du label Titre
		titleLabel = new Label("Sokoban");
		titleLabel.setId("gameTitle");
		//Creation du label du niveau
		nivLabel = new Label("Creation du niveau "+this.numeroNiveau);
		//Creation des inputs de largeur et longeur
		largeurTF = new TextField();
		longueurTF = new TextField();
	}

}
