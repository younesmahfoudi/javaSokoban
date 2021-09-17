package com.sokoban.app;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import com.sokoban.Configuration;
import com.sokoban.Direction;
import com.sokoban.Policier;
import com.sokoban.Position;
import com.sokoban.Type;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Niveau extends Application {
	private int nivSelec;
	private Configuration config;
	private Label nbDeplacement;
	private Label nbBalle;
	private GridPane affGrille;
	private Label[][] tmpGrille;
	private Label[][] scoreLabels;
	private GridPane score;
	private Timeline  timeline = new Timeline();
	private Timeline  timelineJoueur = new Timeline();
	private Timeline  timelineJoueur2 = new Timeline();
	private ArrayList<Timeline>  timelineZombie = new ArrayList<Timeline>();
	private ArrayList<Timeline>  timelineZombie2 = new ArrayList<Timeline>();
	
	private Timeline smoothJoueur = new Timeline();
	private String classDirectionJoueur = "";
	
	private boolean bullet = false;
	private int pseudoTemps = 0;
	private boolean spawnBullet = false;
	private int pseudoTempsSpawn = 0;
	private Position spawnPos;

	private int duree = 100;
	private int dureeDeplacementJoueur = 10;
	private int dureeDeplacementZombie = 10;

	@Override
	public void start(Stage primaryStage) throws IOException {
		primaryStage.setTitle("Sokoban - Niveau " + nivSelec);// Change le titre de la fenetre pour Sokoban - Niveau + numero du niveau
		//Creation des conteneurs
		HBox root = new HBox();
		VBox root2 = new VBox();
		VBox scoreBox = new VBox();
		//Creation du label HighScore et mise en place de son id css
		Label hsLabel = new Label("HighScore");
		hsLabel.setId("hsLabel");
		//Ajouts des entetes et de leur class css
		scoreLabels[0][0] = new Label("Pseudo");
		scoreLabels[0][0].getStyleClass().add("entete");
		scoreLabels[0][1] = new Label("Score");
		scoreLabels[0][1].getStyleClass().add("entete");
		//Creations des labels
		for (int i = 1; i < 6; i++) {
			for (int j = 0; j < 2; j++) {
				scoreLabels[i][j] = new Label();
			}
		}
		updatescore();
		
		//Creation du label Titre
		Label titleLabel = new Label("Sokoban");
		titleLabel.setId("gameTitle");
		//Creation du label du niveau
		Label nivLabel = new Label("Niveau : "+this.nivSelec);
		//Actualisation du label de deplacement
		updateLabels();
		//Creation des separateurs vertical
		Separator separator = new Separator();
		Separator separator2 = new Separator();
		//Actualisation de la grille
		updateGrille(true);
		//Creation du label de reset
		Label reset = new Label("Vous etes bloque ? Appuyez sur 'R' pour reset le niveau.");
		//Ajout des elements
		scoreBox.getChildren().addAll(hsLabel,score);
		scoreBox.setId("scoreBox");
		root2.getChildren().addAll(titleLabel,nivLabel,nbDeplacement,nbBalle,separator,affGrille,separator2,reset);
		root2.setId("niveau");
		root.getChildren().addAll(root2,scoreBox);
		
		// Charge la scene a partir du parent
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/ressources/css/Niveau.css").toString());
        
		// Gestion des events.
		//Si l'utilisateur appuie sur une touche
		scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
			//S'il sagit de la touche Z, deplacement vers le haut et actualisation de l'affichage
			if (key.getCode() == KeyCode.Z) {
				this.bougerJoueur(Direction.HAUT);
				//S'il sagit de la touche S, deplacement vers le bas et actualisation de l'affichage
			} else if (key.getCode() == KeyCode.S) {
				this.bougerJoueur(Direction.BAS);
				//S'il sagit de la touche Q, deplacement vers la gauche et actualisation de l'affichage
			} else if (key.getCode() == KeyCode.Q) {
				this.bougerJoueur(Direction.GAUCHE);
				//S'il sagit de la touche D, deplacement vers la droite et actualisation de l'affichage
			} else if (key.getCode() == KeyCode.D) {
				this.bougerJoueur(Direction.DROITE);
			//S'il sagit de la touche fleche du haut, tire vers le haut et actualisation de l'affichage
			} else if (key.getCode() == KeyCode.UP) {
				this.tirer(Direction.HAUT);
			//S'il sagit de la touche fleche du bas, tire vers le bas et actualisation de l'affichage
			} else if (key.getCode() == KeyCode.DOWN) {
				this.tirer(Direction.BAS);
			//S'il sagit de la touche fleche de gauche, tire vers la gauche et actualisation de l'affichage
			} else if (key.getCode() == KeyCode.LEFT) {
				this.tirer(Direction.GAUCHE);
			//S'il sagit de la touche fleche de droite, tire vers la droite et actualisation de l'affichage
			} else if (key.getCode() == KeyCode.RIGHT) {
				this.tirer(Direction.DROITE);
				//S'il sagit de la touche R, reset la configuration du niveau et actualise l'affichage
			} else if (key.getCode() == KeyCode.R) {
				try {
					this.config = new Configuration(this.nivSelec);
				} catch (IOException e) {
					e.printStackTrace();
				}
				updateGrille(true);
			}
			;
		});

		// Affiche la scene dans la nouvelle fenetre
		primaryStage.setScene(scene);
		// On empeche de redimmenssionner la fenetre
		primaryStage.setResizable(false);
		// Affiche la fenetre au centre de l'ecran
		primaryStage.show();
		primaryStage.centerOnScreen();

	}
	
	private void smoothingJoueur() {
//		this.smoothJoueur.stop();
		Position pos = this.config.getJoueur().getPosition();
		Direction dir = this.config.getJoueur().getRegard();
		if(dir.equals(Direction.DROITE)) {
			KeyFrame key = new KeyFrame(Duration.millis(this.duree), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite1"));
			KeyFrame key1 = new KeyFrame(Duration.millis(this.duree*2), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite2"));
			KeyFrame key2 = new KeyFrame(Duration.millis(this.duree*3), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite3"));
			KeyFrame key3 = new KeyFrame(Duration.millis(this.duree*4), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite4"));
			KeyFrame key4 = new KeyFrame(Duration.millis(this.duree*5), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite5"));
			KeyFrame key5 = new KeyFrame(Duration.millis(this.duree*6), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_shoot_droite6"));
			KeyFrame key6 = new KeyFrame(Duration.millis(this.duree*7), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite7"));
			KeyFrame key7 = new KeyFrame(Duration.millis(this.duree*8), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite8"));
			KeyFrame key8 = new KeyFrame(Duration.millis(this.duree*9), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite9"));
			KeyFrame key9 = new KeyFrame(Duration.millis(this.duree*10), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite10"));
			KeyFrame key10 = new KeyFrame(Duration.millis(this.duree*11), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite11"));
			KeyFrame key11 = new KeyFrame(Duration.millis(this.duree*12), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite12"));
			KeyFrame key12 = new KeyFrame(Duration.millis(this.duree*13), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite13"));
			KeyFrame key13 = new KeyFrame(Duration.millis(this.duree*14), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite14"));
			KeyFrame key14 = new KeyFrame(Duration.millis(this.duree*15), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite15"));
			KeyFrame key15 = new KeyFrame(Duration.millis(this.duree*16), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite16"));
			KeyFrame key16 = new KeyFrame(Duration.millis(this.duree*17), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite17"));
			KeyFrame key17 = new KeyFrame(Duration.millis(this.duree*18), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite18"));
			KeyFrame key18 = new KeyFrame(Duration.millis(this.duree*19), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite19"));
			KeyFrame key19 = new KeyFrame(Duration.millis(this.duree*20), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_stand_droite20"));
			KeyFrame key20 = new KeyFrame(Duration.ZERO, e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_stand_droite1"));
			
			this.smoothJoueur.getKeyFrames().setAll(key,key1,key2,key3,key4,key5,key6,key7,key8,key9,key10,key11,key12,key13,key14,key15,key16,key17,key18,key19,key20);
//			this.smoothJoueur.setAutoReverse(true);
			this.smoothJoueur.setCycleCount(Timeline.INDEFINITE);
			this.smoothJoueur.setOnFinished(e -> {
				this.smoothJoueur.getKeyFrames().removeAll(key,key1,key2,key3,key4,key5,key6,key7,key8,key9,key10,key11,key12,key13,key14,key15,key16,key17,key18,key19,key20);
			});
		}else if (dir.equals(Direction.GAUCHE)) {
			KeyFrame key = new KeyFrame(Duration.millis(this.duree), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite1","gauche"));
			KeyFrame key1 = new KeyFrame(Duration.millis(this.duree*2), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite2","gauche"));
			KeyFrame key2 = new KeyFrame(Duration.millis(this.duree*3), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite3","gauche"));
			KeyFrame key3 = new KeyFrame(Duration.millis(this.duree*4), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite4","gauche"));
			KeyFrame key4 = new KeyFrame(Duration.millis(this.duree*5), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite5","gauche"));
			KeyFrame key5 = new KeyFrame(Duration.millis(this.duree*6), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_shoot_droite6","gauche"));
			KeyFrame key6 = new KeyFrame(Duration.millis(this.duree*7), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite7","gauche"));
			KeyFrame key7 = new KeyFrame(Duration.millis(this.duree*8), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite8","gauche"));
			KeyFrame key8 = new KeyFrame(Duration.millis(this.duree*9), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite9","gauche"));
			KeyFrame key9 = new KeyFrame(Duration.millis(this.duree*10), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite10","gauche"));
			KeyFrame key10 = new KeyFrame(Duration.millis(this.duree*11), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite11","gauche"));
			KeyFrame key11 = new KeyFrame(Duration.millis(this.duree*12), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite12","gauche"));
			KeyFrame key12 = new KeyFrame(Duration.millis(this.duree*13), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite13","gauche"));
			KeyFrame key13 = new KeyFrame(Duration.millis(this.duree*14), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite14","gauche"));
			KeyFrame key14 = new KeyFrame(Duration.millis(this.duree*15), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite15","gauche"));
			KeyFrame key15 = new KeyFrame(Duration.millis(this.duree*16), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite16","gauche"));
			KeyFrame key16 = new KeyFrame(Duration.millis(this.duree*17), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite17","gauche"));
			KeyFrame key17 = new KeyFrame(Duration.millis(this.duree*18), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite18","gauche"));
			KeyFrame key18 = new KeyFrame(Duration.millis(this.duree*19), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite19","gauche"));
			KeyFrame key19 = new KeyFrame(Duration.millis(this.duree*20), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite20","gauche"));
			KeyFrame key20 = new KeyFrame(Duration.ZERO, e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_stand_droite1","gauche"));
			
			this.smoothJoueur.getKeyFrames().setAll(key,key1,key2,key3,key4,key5,key6,key7,key8,key9,key10,key11,key12,key13,key14,key15,key16,key17,key18,key19,key20);
//			this.smoothJoueur.setAutoReverse(true);
			this.smoothJoueur.setCycleCount(Timeline.INDEFINITE);
			this.smoothJoueur.setOnFinished(e -> {
				this.smoothJoueur.getKeyFrames().removeAll(key,key1,key2,key3,key4,key5,key6,key7,key8,key9,key10,key11,key12,key13,key14,key15,key16,key17,key18,key19,key20);
			});
		} else if (dir.equals(Direction.BAS)) {
			KeyFrame key = new KeyFrame(Duration.millis(this.duree), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite1","bas"));
			KeyFrame key1 = new KeyFrame(Duration.millis(this.duree*2), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite2","bas"));
			KeyFrame key2 = new KeyFrame(Duration.millis(this.duree*3), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite3","bas"));
			KeyFrame key3 = new KeyFrame(Duration.millis(this.duree*4), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite4","bas"));
			KeyFrame key4 = new KeyFrame(Duration.millis(this.duree*5), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite5","bas"));
			KeyFrame key5 = new KeyFrame(Duration.millis(this.duree*6), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_shoot_droite6","bas"));
			KeyFrame key6 = new KeyFrame(Duration.millis(this.duree*7), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite7","bas"));
			KeyFrame key7 = new KeyFrame(Duration.millis(this.duree*8), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite8","bas"));
			KeyFrame key8 = new KeyFrame(Duration.millis(this.duree*9), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite9","bas"));
			KeyFrame key9 = new KeyFrame(Duration.millis(this.duree*10), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite10","bas"));
			KeyFrame key10 = new KeyFrame(Duration.millis(this.duree*11), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite11","bas"));
			KeyFrame key11 = new KeyFrame(Duration.millis(this.duree*12), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite12","bas"));
			KeyFrame key12 = new KeyFrame(Duration.millis(this.duree*13), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite13","bas"));
			KeyFrame key13 = new KeyFrame(Duration.millis(this.duree*14), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite14","bas"));
			KeyFrame key14 = new KeyFrame(Duration.millis(this.duree*15), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite15","bas"));
			KeyFrame key15 = new KeyFrame(Duration.millis(this.duree*16), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite16","bas"));
			KeyFrame key16 = new KeyFrame(Duration.millis(this.duree*17), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite17","bas"));
			KeyFrame key17 = new KeyFrame(Duration.millis(this.duree*18), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite18","bas"));
			KeyFrame key18 = new KeyFrame(Duration.millis(this.duree*19), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite19","bas"));
			KeyFrame key19 = new KeyFrame(Duration.millis(this.duree*20), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite20","bas"));
			KeyFrame key20 = new KeyFrame(Duration.ZERO, e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_stand_droite1","bas"));
			
			this.smoothJoueur.getKeyFrames().setAll(key,key1,key2,key3,key4,key5,key6,key7,key8,key9,key10,key11,key12,key13,key14,key15,key16,key17,key18,key19,key20);
//			this.smoothJoueur.setAutoReverse(true);
			this.smoothJoueur.setCycleCount(Timeline.INDEFINITE);
			
			this.smoothJoueur.setOnFinished(e -> {
				this.smoothJoueur.getKeyFrames().removeAll(key,key1,key2,key3,key4,key5,key6,key7,key8,key9,key10,key11,key12,key13,key14,key15,key16,key17,key18,key19,key20);
			});
		} else if (dir.equals(Direction.HAUT)) {
			KeyFrame key = new KeyFrame(Duration.millis(this.duree), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite1","haut"));
			KeyFrame key1 = new KeyFrame(Duration.millis(this.duree*2), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite2","haut"));
			KeyFrame key2 = new KeyFrame(Duration.millis(this.duree*3), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite3","haut"));
			KeyFrame key3 = new KeyFrame(Duration.millis(this.duree*4), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite4","haut"));
			KeyFrame key4 = new KeyFrame(Duration.millis(this.duree*5), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite5","haut"));
			KeyFrame key5 = new KeyFrame(Duration.millis(this.duree*6), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_shoot_droite6","haut"));
			KeyFrame key6 = new KeyFrame(Duration.millis(this.duree*7), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite7","haut"));
			KeyFrame key7 = new KeyFrame(Duration.millis(this.duree*8), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite8","haut"));
			KeyFrame key8 = new KeyFrame(Duration.millis(this.duree*9), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite9","haut"));
			KeyFrame key9 = new KeyFrame(Duration.millis(this.duree*10), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite10","haut"));
			KeyFrame key10 = new KeyFrame(Duration.millis(this.duree*11), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite11","haut"));
			KeyFrame key11 = new KeyFrame(Duration.millis(this.duree*12), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite12","haut"));
			KeyFrame key12 = new KeyFrame(Duration.millis(this.duree*13), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite13","haut"));
			KeyFrame key13 = new KeyFrame(Duration.millis(this.duree*14), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite14","haut"));
			KeyFrame key14 = new KeyFrame(Duration.millis(this.duree*15), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite15","haut"));
			KeyFrame key15 = new KeyFrame(Duration.millis(this.duree*16), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite16","haut"));
			KeyFrame key16 = new KeyFrame(Duration.millis(this.duree*17), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite17","haut"));
			KeyFrame key17 = new KeyFrame(Duration.millis(this.duree*18), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite18","haut"));
			KeyFrame key18 = new KeyFrame(Duration.millis(this.duree*19), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite19","haut"));
			KeyFrame key19 = new KeyFrame(Duration.millis(this.duree*20), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().addAll("joueur_stand_droite20","haut"));
			KeyFrame key20 = new KeyFrame(Duration.ZERO, e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_stand_droite1","haut"));
			
			this.smoothJoueur.getKeyFrames().setAll(key,key1,key2,key3,key4,key5,key6,key7,key8,key9,key10,key11,key12,key13,key14,key15,key16,key17,key18,key19,key20);
//			this.smoothJoueur.setAutoReverse(true);
			this.smoothJoueur.setCycleCount(Timeline.INDEFINITE);
			this.smoothJoueur.setOnFinished(e -> {
				this.smoothJoueur.getKeyFrames().removeAll(key,key1,key2,key3,key4,key5,key6,key7,key8,key9,key10,key11,key12,key13,key14,key15,key16,key17,key18,key19,key20);
			});
		} 
		this.smoothJoueur.playFromStart();
	}
	
	private void tirer(Direction dir) {		
		if (config.getJoueur().getBalles() >= 0) {
			this.smoothJoueur.stop();
			this.timeline.stop();

			config.getJoueur().tirer(dir);
			config.getJoueur().setRegard(dir);
			Position pos = this.config.getJoueur().getPosition();
			
			if (dir.equals(Direction.DROITE)) {
				this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().clear();
				KeyFrame k = new KeyFrame(Duration.millis(0), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_droite"));
				KeyFrame key = new KeyFrame(Duration.millis(this.duree), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("shoot_droite1"));
				KeyFrame key1 = new KeyFrame(Duration.millis(this.duree*2), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("shoot_droite2"));
				KeyFrame key2 = new KeyFrame(Duration.millis(this.duree*3), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("shoot_droite3"));
				this.timeline.getKeyFrames().setAll(k,key,key1,key2);
				this.timeline.setOnFinished(e -> {
					updateGrille(false);
					this.timeline.getKeyFrames().removeAll(k,key,key1,key2);
				});
				
			} else if(dir.equals(Direction.GAUCHE)) {
				
				this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().clear();
				KeyFrame k = new KeyFrame(Duration.millis(0), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_gauche"));
				KeyFrame key = new KeyFrame(Duration.millis(this.duree), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("shoot_gauche1"));
				KeyFrame key1 = new KeyFrame(Duration.millis(this.duree*2), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("shoot_gauche2"));
				KeyFrame key2 = new KeyFrame(Duration.millis(this.duree*3), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("shoot_gauche3"));
				this.timeline.getKeyFrames().setAll(k,key,key1,key2);
				this.timeline.setOnFinished(e -> {
					updateGrille(false);
					this.timeline.getKeyFrames().removeAll(k,key,key1,key2);
				});
				
			} else if(dir.equals(Direction.HAUT)) {
				
				this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().clear();
				KeyFrame k = new KeyFrame(Duration.millis(0), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_haut"));
				KeyFrame key = new KeyFrame(Duration.millis(this.duree), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("shoot_haut1"));
				KeyFrame key1 = new KeyFrame(Duration.millis(this.duree*2), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("shoot_haut2"));
				KeyFrame key2 = new KeyFrame(Duration.millis(this.duree*3), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("shoot_haut3"));
				this.timeline.getKeyFrames().setAll(k,key,key1,key2);
				this.timeline.setOnFinished(e -> {
					updateGrille(false);
					this.timeline.getKeyFrames().removeAll(k,key,key1,key2);
				});
				
			} else if(dir.equals(Direction.BAS)) {
				this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().clear();
				KeyFrame k = new KeyFrame(Duration.millis(0), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("joueur_bas"));
				KeyFrame key = new KeyFrame(Duration.millis(this.duree), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("shoot_bas1"));
				KeyFrame key1 = new KeyFrame(Duration.millis(this.duree*2), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("shoot_bas2"));
				KeyFrame key2 = new KeyFrame(Duration.millis(this.duree*3), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().add("shoot_bas3"));
				this.timeline.getKeyFrames().setAll(k,key,key1,key2);
				this.timeline.setOnFinished(e -> {
					updateGrille(false);
					this.timeline.getKeyFrames().removeAll(k,key,key1,key2);
				});
			}
			this.timeline.play();
		}
	}
	
	private void bougerJoueur(Direction dir) {
		//on change le regard du joueur
		config.getJoueur().setRegard(dir);
		//sauvegarde de la derniere position du joueur
		Position pos = this.config.getJoueur().getPosition();
		//le joueur change de position
		config.bougerJoueurVers(dir);
		//sauvegarde de la nouvelle position du joueur
		Position newPos = this.config.getJoueur().getPosition();
		
		if(pos!=newPos) {
			this.smoothJoueur.stop();
			this.timelineJoueur.stop();
			this.timelineJoueur2.stop();
			
			//S'il passe sur une balle, rajoute la balle a son pistolet et reset pseudoTemps et bullet
			if (tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().contains("bullet")) {
				this.config.getJoueur().addBalle();
				if (this.spawnPos.equals(newPos)) {
					this.pseudoTempsSpawn = 0;
					this.spawnBullet = false;
				} else {
					this.bullet = false;
					this.pseudoTemps = 0;
				}
			}
	
			//on défini la variable qui contiendra la class css qui sera utilisé pour la direction
			this.classDirectionJoueur = "";
			if(dir.equals(Direction.GAUCHE)) {
				this.classDirectionJoueur = "gauche";
				
			} else if(dir.equals(Direction.HAUT)) {
				this.classDirectionJoueur = "haut";
				
			} else if(dir.equals(Direction.BAS)) {
				this.classDirectionJoueur = "bas";
			}
			KeyFrame key0 = new KeyFrame(Duration.millis(0), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_0","fond_case", this.classDirectionJoueur));
			KeyFrame key1 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_1","fond_case", this.classDirectionJoueur));
			KeyFrame key2 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*2), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_2","fond_case", this.classDirectionJoueur));
			KeyFrame key3 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*3), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_3","fond_case", this.classDirectionJoueur));
			KeyFrame key4 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*4), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_4","fond_case", this.classDirectionJoueur));
			KeyFrame key5 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*5), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_5","fond_case", this.classDirectionJoueur));
			KeyFrame key6 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*6), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_6","fond_case", this.classDirectionJoueur));
			KeyFrame key7 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*7), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_7","fond_case", this.classDirectionJoueur));
			KeyFrame key8 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*8), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_8","fond_case", this.classDirectionJoueur));
			KeyFrame key9 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*9), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_9","fond_case", this.classDirectionJoueur));
			KeyFrame key10 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*10), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_10","fond_case", this.classDirectionJoueur));
			KeyFrame key11 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*11), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_11","fond_case", this.classDirectionJoueur));
			KeyFrame key12 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*12), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_12","fond_case", this.classDirectionJoueur));
			KeyFrame key13 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*13), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_13","fond_case", this.classDirectionJoueur));
			KeyFrame key14 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*14), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_14","fond_case", this.classDirectionJoueur));
			KeyFrame key15 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*15), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_15","fond_case", this.classDirectionJoueur));
			KeyFrame key16 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*16), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_16","fond_case", this.classDirectionJoueur));
			KeyFrame key17 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*17), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_17","fond_case", this.classDirectionJoueur));
			KeyFrame key18 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*18), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("joueur_move_1_18","fond_case", this.classDirectionJoueur));
			this.timelineJoueur.getKeyFrames().setAll(key0,key1,key2,key3,key4,key5,key6,key7,key8,key9,key10,key11,key12,key13,key14,key15,key16,key17,key18);
			this.timelineJoueur.setOnFinished(e -> {
				this.timelineJoueur.getKeyFrames().removeAll(key0,key1,key2,key3,key4,key5,key6,key7,key8,key9,key10,key11,key12,key13,key14,key15,key16,key17,key18);
			});
			
			KeyFrame newKey0 = new KeyFrame(Duration.millis(0), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_0","fond_case", this.classDirectionJoueur));
			KeyFrame newKey1 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_1","fond_case", this.classDirectionJoueur));
			KeyFrame newKey2 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*2), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_2","fond_case", this.classDirectionJoueur));
			KeyFrame newKey3 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*3), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_3","fond_case", this.classDirectionJoueur));
			KeyFrame newKey4 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*4), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_4","fond_case", this.classDirectionJoueur));
			KeyFrame newKey5 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*5), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_5","fond_case", this.classDirectionJoueur));
			KeyFrame newKey6 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*6), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_6","fond_case", this.classDirectionJoueur));
			KeyFrame newKey7 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*7), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_7","fond_case", this.classDirectionJoueur));
			KeyFrame newKey8 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*8), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_8","fond_case", this.classDirectionJoueur));
			KeyFrame newKey9 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*9), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_9","fond_case", this.classDirectionJoueur));
			KeyFrame newKey10 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*10), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_10","fond_case", this.classDirectionJoueur));
			KeyFrame newKey11 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*11), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_11","fond_case", this.classDirectionJoueur));
			KeyFrame newKey12 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*12), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_12","fond_case", this.classDirectionJoueur));
			KeyFrame newKey13 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*13), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_13","fond_case", this.classDirectionJoueur));
			KeyFrame newKey14 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*14), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_14","fond_case", this.classDirectionJoueur));
			KeyFrame newKey15 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*15), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_15","fond_case", this.classDirectionJoueur));
			KeyFrame newKey16 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*16), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_16","fond_case", this.classDirectionJoueur));
			KeyFrame newKey17 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*17), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_17","fond_case", this.classDirectionJoueur));
			KeyFrame newKey18 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*18), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_18","fond_case", this.classDirectionJoueur));
			KeyFrame newKey19 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*19), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_19","fond_case", this.classDirectionJoueur));
			KeyFrame newKey20 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*20), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_20","fond_case", this.classDirectionJoueur));
			KeyFrame newKey21 = new KeyFrame(Duration.millis(this.dureeDeplacementJoueur*21), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("joueur_move_2_21","fond_case", this.classDirectionJoueur));
			this.timelineJoueur2.getKeyFrames().setAll(newKey0,newKey1,newKey2,newKey3,newKey4,newKey5,newKey6,newKey7,newKey8,newKey9,newKey10,newKey11,newKey12,newKey13,newKey14,newKey15,newKey16,newKey17,newKey18,newKey19,newKey20,newKey21);
			this.timelineJoueur2.setOnFinished(e -> {
				this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().clear();
				this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().addAll("joueur_move_2_21","fond_case", this.classDirectionJoueur);
				this.timelineJoueur2.getKeyFrames().removeAll(newKey0,newKey1,newKey2,newKey3,newKey4,newKey5,newKey6,newKey7,newKey8,newKey9,newKey10,newKey11,newKey12,newKey13,newKey14,newKey15,newKey16,newKey17,newKey18,newKey19,newKey20,newKey21);
				updateGrille(false);
			});
			
			this.timelineJoueur.play();
			this.timelineJoueur2.play();
		}
		
	}
	
	private void attack(Direction dir, Position pos) {
		String directionString = "";
		//on défini la variable qui contiendra la class css qui sera utilisé pour la direction
		if(dir.equals(Direction.GAUCHE)) {
			directionString = "gauche";
		} else if(dir.equals(Direction.HAUT)) {
			directionString = "haut";
		} else if(dir.equals(Direction.BAS)) {
			directionString = "bas";
		}
		final String classDirectionZombie = directionString;
		KeyFrame k = new KeyFrame(Duration.millis(0), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("policier_droite",classDirectionZombie));
		KeyFrame key = new KeyFrame(Duration.millis(this.duree), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("attack_droite1",classDirectionZombie));
		KeyFrame key1 = new KeyFrame(Duration.millis(this.duree*2), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("attack_droite2",classDirectionZombie));
		KeyFrame key2 = new KeyFrame(Duration.millis(this.duree*3), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("attack_droite3",classDirectionZombie));
		KeyFrame key3 = new KeyFrame(Duration.millis(this.duree*4), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("attack_droite4",classDirectionZombie));
		KeyFrame key4 = new KeyFrame(Duration.millis(this.duree*5), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("attack_droite5",classDirectionZombie));
		KeyFrame key5 = new KeyFrame(Duration.millis(this.duree*6), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("attack_droite6",classDirectionZombie));
		KeyFrame key6 = new KeyFrame(Duration.millis(this.duree*7), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("attack_droite7",classDirectionZombie));
		KeyFrame key7 = new KeyFrame(Duration.millis(this.duree*8), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("attack_droite8",classDirectionZombie));
		KeyFrame key8 = new KeyFrame(Duration.millis(this.duree*9), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("attack_droite9",classDirectionZombie));
		this.timeline.getKeyFrames().addAll(k,key,key1,key2,key3,key4,key5,key6,key7,key8);
		this.timeline.setOnFinished(e -> {
			this.timeline.getKeyFrames().removeAll(k,key,key1,key2,key3,key4,key5,key6,key7,key8);
		});
		
		this.timeline.play();
	}
	
	private void updatescore() {
		try {
			//Connection à la BDD des scores
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection con=DriverManager.getConnection("jdbc:mysql://e90653-mysql.services.easyname.eu/u143161db7?autoReconnect=true&useSSL=false","u143161db7","ProjetGL2");
			//Creation de la variable de requete
			Statement stmt=con.createStatement();
			//Cr�ation de la variable de resultat et recuperation du resultat de la requete
			ResultSet rs=stmt.executeQuery("SELECT pseudo,nbDeplacement FROM `HighScore` WHERE niveau = "+this.nivSelec+" ORDER BY nbDeplacement ASC LIMIT 5"); 
			//Creation de la variable de deplacement
			int i = 1;
			//Tant qu'il y a un résultat dans rs
			while (rs.next()) {
				//on modifie les labels de la ligne
				scoreLabels[i][0].setText(rs.getString(1));
				scoreLabels[i][1].setText(rs.getString(2));
				//On incremente le numero de la ligne
				i++;
			}
			//On ferme la connection à la BDD
			con.close();
			score.getChildren().clear();
			for (i = 0; i<6;i++) {
				for(int j = 0; j<2;j++) {
					score.add(scoreLabels[i][j],j,i);
				}
			}
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Sokoban - Niveau "+this.nivSelec);
			alert.setHeaderText("Probleme avec le serveur");
			alert.setContentText("Il semblerait que l'application n'arrive pas � se connecter au serveur des scores. Merci de verifier votre connexion internet ou vous ne pourrez pas sauvegarder vos scores.");
			alert.show();
		}
	}
	

	/**
	 * @param nivSelec
	 */
	public Niveau(int nivSelec) {
		super();
		//Set le numero du niveau
		this.nivSelec = nivSelec;
		//Initialise les labels
		this.nbDeplacement = new Label();
		this.nbBalle = new Label();
		//Initialise la configuration du niveau
		try {
			this.config = new Configuration(nivSelec);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Variables de deplacement
		int i;
		int j;
		//Variable de limitation de boucle
		int x = this.config.getNiveau().getX();
		int y = this.config.getNiveau().getY();
		//Initialise le tableau de label
		this.tmpGrille = new Label[x][y];
		//Initialise tous les labels du tableau
		for (i=0;i<x;i++) {
			for (j=0;j<y;j++) {
				tmpGrille[i][j] = new Label();
			}
		}
		//Initialise la grille d'affichage
		this.affGrille = new GridPane();
//		this.affGrille.getColumnConstraints().add(new ColumnConstraints(64));
//		ColumnConstraints col1 = new ColumnConstraints();
//		col1.setMaxWidth(32);
//		col1.setHgrow(Priority.ALWAYS);
//		this.affGrille.getColumnConstraints().add(col1);
//		
//		this.affGrille.getRowConstraints().add(new RowConstraints(64));
//		RowConstraints lig1 = new RowConstraints();
//		lig1.setMaxHeight(32);
//		lig1.setVgrow(Priority.ALWAYS);
//		this.affGrille.getRowConstraints().add(lig1);
		
		//Creations de la grille d'affichage pour les scores
		this.score = new GridPane();
		this.score.setId("score");
		this.scoreLabels = new Label[6][2];
		this.spawnPos = this.config.getJoueur().getPosition();
	}
	
	private void updateLabels() {
		//Met a jour le nombre de deplacement
		this.nbDeplacement.setText("Vous avez effectue "+this.config.getJoueur().getHisto().size()+" deplacements.");
		//Met la jour le nombre de balles restantes
		if (this.config.getJoueur().getBalles() > 0) {
			this.nbBalle.setText("Vous avez "+this.config.getJoueur().getBalles()+" balles dans votre revolver.");
		} else {
			this.nbBalle.setText("Vous n'avez plus de balles dans votre revolver.");
		}
	}
	
	private void bougerZombies() {
		this.timelineZombie.clear();
		this.timelineZombie2.clear();
		//Pour chaque policiers
		for(Policier zombie : this.config.getPoliciers()) {
			Position pos = zombie.getPosition();
			Direction direction = zombie.deplacementPolicier();
			Position newPos = zombie.getPosition().add(direction);
			if(zombie.setPosition(newPos)) {
				Timeline timelineZomb1 = new Timeline();
				Timeline timelineZomb2 = new Timeline();
				String directionString = "";
				//on défini la variable qui contiendra la class css qui sera utilisé pour la direction
				if(direction.equals(Direction.GAUCHE)) {
					directionString = "gauche";
				} else if(direction.equals(Direction.HAUT)) {
					directionString = "haut";
				} else if(direction.equals(Direction.BAS)) {
					directionString = "bas";
				}
				final String classDirectionZombie = directionString;
				KeyFrame zKey0 = new KeyFrame(Duration.millis(0), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("zombie_move_1_0","fond_case", classDirectionZombie));
				KeyFrame zKey1 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("zombie_move_1_1","fond_case", classDirectionZombie));
				KeyFrame zKey2 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*2), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("zombie_move_1_2","fond_case", classDirectionZombie));
				KeyFrame zKey3 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*3), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("zombie_move_1_3","fond_case", classDirectionZombie));
				KeyFrame zKey4 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*4), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("zombie_move_1_4","fond_case", classDirectionZombie));
				KeyFrame zKey5 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*5), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("zombie_move_1_5","fond_case", classDirectionZombie));
				KeyFrame zKey6 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*6), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("zombie_move_1_6","fond_case", classDirectionZombie));
				KeyFrame zKey7 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*7), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("zombie_move_1_7","fond_case", classDirectionZombie));
				KeyFrame zKey8 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*8), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("zombie_move_1_8","fond_case", classDirectionZombie));
				KeyFrame zKey9 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*9), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("zombie_move_1_9","fond_case", classDirectionZombie));
				KeyFrame zKey10 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*10), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("zombie_move_1_10","fond_case", classDirectionZombie));
				KeyFrame zKey11 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*11), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("zombie_move_1_11","fond_case", classDirectionZombie));
				KeyFrame zKey12 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*12), e -> this.tmpGrille[pos.getX()][pos.getY()].getStyleClass().setAll("zombie_move_1_12","fond_case", classDirectionZombie));
				timelineZomb1.getKeyFrames().setAll(zKey0,zKey1,zKey2,zKey3,zKey4,zKey5,zKey6,zKey7,zKey8,zKey9,zKey10,zKey11,zKey12);
				timelineZomb1.setOnFinished(e -> {
					timelineZomb1.getKeyFrames().removeAll(zKey0,zKey1,zKey2,zKey3,zKey4,zKey5,zKey6,zKey7,zKey8,zKey9,zKey10,zKey11,zKey12);
				});
				KeyFrame newZKey0 = new KeyFrame(Duration.millis(0), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("zombie_move_2_0","fond_case", classDirectionZombie));
				KeyFrame newZKey1 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("zombie_move_2_1","fond_case", classDirectionZombie));
				KeyFrame newZKey2 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*2), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("zombie_move_2_2","fond_case", classDirectionZombie));
				KeyFrame newZKey3 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*3), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("zombie_move_2_3","fond_case", classDirectionZombie));
				KeyFrame newZKey4 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*4), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("zombie_move_2_4","fond_case", classDirectionZombie));
				KeyFrame newZKey5 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*5), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("zombie_move_2_5","fond_case", classDirectionZombie));
				KeyFrame newZKey6 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*6), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("zombie_move_2_6","fond_case", classDirectionZombie));
				KeyFrame newZKey7 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*7), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("zombie_move_2_7","fond_case", classDirectionZombie));
				KeyFrame newZKey8 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*8), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("zombie_move_2_8","fond_case", classDirectionZombie));
				KeyFrame newZKey9 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*9), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("zombie_move_2_9","fond_case", classDirectionZombie));
				KeyFrame newZKey10 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*10), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("zombie_move_2_10","fond_case", classDirectionZombie));
				KeyFrame newZKey11 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*11), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("zombie_move_2_11","fond_case", classDirectionZombie));
				KeyFrame newZKey12 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*12), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("zombie_move_2_12","fond_case", classDirectionZombie));
				KeyFrame newZKey13 = new KeyFrame(Duration.millis(this.dureeDeplacementZombie*13), e -> this.tmpGrille[newPos.getX()][newPos.getY()].getStyleClass().setAll("zombie_move_2_13","fond_case", classDirectionZombie));
				timelineZomb2.getKeyFrames().setAll(newZKey0,newZKey1,newZKey2,newZKey3,newZKey4,newZKey5,newZKey6,newZKey7,newZKey8,newZKey9,newZKey10,newZKey11,newZKey12,newZKey13);
				timelineZomb2.setOnFinished(e -> {
					timelineZomb2.getKeyFrames().removeAll(newZKey0,newZKey1,newZKey2,newZKey3,newZKey4,newZKey5,newZKey6,newZKey7,newZKey8,newZKey9,newZKey10,newZKey11,newZKey12,newZKey13);
				});
				
				timelineZomb1.play();
				timelineZomb2.play();
				this.timelineZombie.add(timelineZomb1);
				this.timelineZombie2.add(timelineZomb2);
			}
		}
	}
	
	private void updateGrille(boolean reset) {
		int randomNum;
		this.smoothJoueur.stop();
		//Variables de deplacement
		int i;
		int j;
		//Variable de limitation de boucle
		int x = this.config.getNiveau().getX();
		int y = this.config.getNiveau().getY();
		//Variable mort du joueur
		boolean mort = false;
		
		Position tmpPos;
		
		//Variable position du policier
		Position posP;
		//Variable direction du policier
		Direction dirP;
		
		if (!reset) {
			//bouger les zombies
			this.bougerZombies();
		}
		//Reset du compteur pour le pop de balle si reset == true
		else{
			this.bullet = false;
			this.pseudoTemps = 0;
			this.spawnBullet = false;
			this.pseudoTempsSpawn = 0;
		}
		
		//Pour chaque label de tmpGrille
		for (i=0;i<x;i++) {
			for (j=0;j<y;j++) {
				tmpPos = new Position(i,j);
				//S'il sagit d'un mur, change sa class en mur
				if (this.config.get(tmpPos).getType().equals(Type.MUR)) {
					tmpGrille[i][j].getStyleClass().clear();
					tmpGrille[i][j].getStyleClass().add(getMur(tmpPos));
				//S'il sagit d'un joueur, change sa class en joueur
				} else if (this.config.get(tmpPos).getType().equals(Type.JOUEUR)) {
					if(reset) {
						tmpGrille[i][j].getStyleClass().clear();
						tmpGrille[i][j].getStyleClass().add(this.getRegard());
					}
					smoothingJoueur();
				//S'il sagit d'un diamant, change sa class en diamant
				} else if (this.config.get(tmpPos).getType().equals(Type.DIAMANT)) {
					//S'il passe sur une balle, rajoute la balle a son pistolet et reset pseudoTemps et bullet
					if (tmpGrille[i][j].getStyleClass().contains("bullet")) {
						this.config.getJoueur().addBalle();
						if (this.spawnPos.equals(tmpPos)) {
							this.pseudoTempsSpawn = 0;
							this.spawnBullet = false;
						} else {
							this.bullet = false;
							this.pseudoTemps = 0;
						}
					}
					tmpGrille[i][j].getStyleClass().clear();
					tmpGrille[i][j].getStyleClass().add("diamant");
				//S'il sagit d'une case, change sa class en case
				} else if (this.config.get(tmpPos).getType().equals(Type.CASE)) {
					//Prends le prochain nombre random entre 1 et 20 compris.
					randomNum = ThreadLocalRandom.current().nextInt(1,21);
					//Si une balle n'est pas d�j� sur la grille, que le nombre random est egal a 1, qut le pseudoTemps est egal � 5 et que la pos n'est pas une cible
					//Ou si la case contenait deja une balle et que ce n'est pas un reset
					if ((!this.bullet && this.pseudoTemps == 5 && randomNum==1 && !this.config.getNiveau().getCibles().contains(tmpPos)) || (this.bullet && tmpGrille[i][j].getStyleClass().contains("bullet") && !reset)) {
						//Met this.bullet sur true
						this.bullet = true;
						//Cha,gement la class pour bullet
						tmpGrille[i][j].getStyleClass().clear();
						tmpGrille[i][j].getStyleClass().add("bullet");
						//Si une balle n'est pas deja sur la case de spawn et que le temps est egal a 10;
					} else if ((!this.spawnBullet && this.pseudoTempsSpawn == 10 && this.spawnPos.equals(tmpPos)) || (tmpGrille[i][j].getStyleClass().contains("bullet") && !reset)) {
						//Met this.bullet sur true
						this.spawnBullet = true;
						//Cha,gement la class pour bullet
						tmpGrille[i][j].getStyleClass().clear();
						tmpGrille[i][j].getStyleClass().add("bullet");
					} else {
						//Sinon, change la class pour case
						tmpGrille[i][j].getStyleClass().clear();
						tmpGrille[i][j].getStyleClass().add("case");
					}
				//S'il sagit d'un policier, change sa class en policier
				} else if (this.config.get(tmpPos).getType().equals(Type.POLICIER)) {
					if (tmpGrille[i][j].getStyleClass().contains("bullet")) {
						this.bullet = false;
					}
					if (reset) {
						tmpGrille[i][j].getStyleClass().clear();
						tmpGrille[i][j].getStyleClass().add(getRegardPolicier(tmpPos));
					}
				}
			}
		}
		//Variables des cibles
		ArrayList<Position> cibles = this.config.getNiveau().getCibles();
		//Compteur de cible remplies
		int cp = 0;
		//Pour chaque cible
		for (Position cible : cibles) {
			//Si il n'y a pas de diamant, ni de joueur dessus, change sa class en entrepot
			if (!this.config.get(cible).getType().equals(Type.DIAMANT) && !this.config.get(cible).getType().equals(Type.JOUEUR)) {
				tmpGrille[cible.getX()][cible.getY()].getStyleClass().clear();
				tmpGrille[cible.getX()][cible.getY()].getStyleClass().add("entrepot");
			//S'il y a un diamant dessus, change sa class en diamantEntrepot et increment le compteur
			} else if (this.config.get(cible).getType().equals(Type.DIAMANT)) {
				tmpGrille[cible.getX()][cible.getY()].getStyleClass().clear();
				tmpGrille[cible.getX()][cible.getY()].getStyleClass().add("diamantEntrepot");
				cp++;
			} else if (this.config.get(cible).getType().equals(Type.JOUEUR)){
				tmpGrille[cible.getX()][cible.getY()].getStyleClass().clear();
				tmpGrille[cible.getX()][cible.getY()].getStyleClass().add(getRegard()+"Entrepot");
			}
		}
		//Augmente le pseudo temps de 1 s'il est inferieur � 5 et qu'il n'y a pas deja une balle sur la grille
		if (!this.bullet && this.pseudoTemps<5) {
			this.pseudoTemps++;
		}
		//Augmente le pseudo temps Spawn de 1 s'il est inferieur a 10 et qu'il n'y a pas deja une balle sur la case
		if (!this.spawnBullet && this.pseudoTempsSpawn<10) {
			this.pseudoTempsSpawn++;
		}
		//Reset la grille d'affichage
		this.affGrille.getChildren().clear();
		//Pour chaque case, ajoute le label correspondant
		for (i=0;i<x;i++) {
			for (j=0;j<y;j++) {
				this.affGrille.add(tmpGrille[i][j], j, i);
			}
		}
		Position savePos;
		//Pour chaque policier
		for(Policier policier : this.config.getPoliciers()) {
			//On recupere la position et la vision du policier
			savePos = policier.getPosition();
			dirP = policier.getRegard();
			//On ajoute la direction à la position actuel
			posP = savePos.add(dirP);
			//On verifie la mort du joueur
			if (!mort) {
				mort = this.config.getJoueur().getPosition().equals(posP);
				if(mort) {
					attack(dirP,savePos);
				}
			}
		}
		//Si le compteur est egal au nombre de diamant du niveau
		if (cp == this.config.getDiamants().size()) {
			//Cree et affiche une alerteBox indiquant la victoire
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Sokoban - Niveau "+this.nivSelec);
			alert.setHeaderText("Victoire");
			alert.setContentText("Bravo, vous avez gagne le niveau "+this.nivSelec+" en "+this.config.getJoueur().getHisto().size()+" coups !");
			alert.show();
			//Sauvegarde le score
			saveScore();
		//Sinon, si on est mort
		} else if (mort) {
			//Cree et affiche une alerteBox indiquant la defaite
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Sokoban - Niveau "+this.nivSelec);
			alert.setHeaderText("Defaite");
			alert.setContentText("Dommage, vous avez perdu le niveau "+this.nivSelec+". Vous avez etes reperer par un policier.");
			alert.show();
			//Lors de la fermeture de l'alerteBox
			alert.setOnCloseRequest(e -> {
				//Reset le niveau
				try {
					this.config = new Configuration(this.nivSelec);
				} catch (IOException error) {
					error.printStackTrace();
				}
				updateGrille(true);
			});
		}
		//Update les labels d'informations
		updateLabels();
	}
	
	private String getRegardPolicier( Position pos) {
        Direction dir = Direction.DROITE;
        for(int k = 0 ; k < this.config.getPoliciers().size(); k++) {
            if ( this.config.getPoliciers().get(k).getPosition().equals(pos)) {
                dir = this.config.getPoliciers().get(k).getRegard();
            }
        }
	    
        String res = "";
		if (dir.equals(Direction.DROITE)) {
			res = "policier_droite";
		}else if(dir.equals(Direction.GAUCHE)) {
			res = "policier_gauche";
		}else if(dir.equals(Direction.HAUT)) {
			res = "policier_haut";
		}else if(dir.equals(Direction.BAS)) {
			res = "policier_bas";
		}
		return res;

	}
	
	private String getRegard() {
		String res = "";
		if (this.config.getJoueur().getRegard().equals(Direction.DROITE)) {
			res = "joueur_droite";
		}else if(this.config.getJoueur().getRegard().equals(Direction.GAUCHE)) {
			res = "joueur_gauche";
		}else if(this.config.getJoueur().getRegard().equals(Direction.HAUT)) {
			res = "joueur_haut";
		}else if(this.config.getJoueur().getRegard().equals(Direction.BAS)) {
			res = "joueur_bas";
		}
		return res;
	}
	
	private boolean inGrille(Position pos) {
		int sizeX = this.config.getNiveau().getX();
		int sizeY = this.config.getNiveau().getY();
		if (((pos.getX() < sizeX)&&(pos.getX() >= 0)) && ((pos.getY() < sizeY)&&(pos.getY() >= 0))){
			return true;
		}else {
			return false;
		}
	}
	
	private String getMur(Position pos) {
		String res = "";
		
		boolean haut = false;
		boolean bas = false;
		boolean gauche = false;
		boolean droite = false;
//		boolean hautDroite = false;
//		boolean hautGauche = false;
//		boolean basGauche = false;
//		boolean basDroite = false;
		
		Position tmpPos = new Position(pos);
		
		//haut
		tmpPos = tmpPos.add(Direction.HAUT);
		if ((inGrille(tmpPos) && (this.config.get(tmpPos).getType().equals(Type.MUR)))){
			haut = true;
		}
		tmpPos = tmpPos.sub(Direction.HAUT);
		
		//bas
		tmpPos = tmpPos.add(Direction.BAS);
		if ((inGrille(tmpPos) && (this.config.get(tmpPos).getType().equals(Type.MUR)))){
			bas = true;
		}
		tmpPos = tmpPos.sub(Direction.BAS);
		
		//droite
		tmpPos = tmpPos.add(Direction.DROITE);
		if ((inGrille(tmpPos) && (this.config.get(tmpPos).getType().equals(Type.MUR)))){
			droite = true;
		}
		tmpPos = tmpPos.sub(Direction.DROITE);
		
		//gauche
		tmpPos = tmpPos.add(Direction.GAUCHE);
		if ((inGrille(tmpPos) && (this.config.get(tmpPos).getType().equals(Type.MUR)))){
			gauche = true;
		}
		tmpPos = tmpPos.sub(Direction.GAUCHE);
		
		//diagonale hautgauche
		tmpPos = tmpPos.add(Direction.HAUT).add(Direction.GAUCHE);
		if ((inGrille(tmpPos) && (this.config.get(tmpPos).getType().equals(Type.MUR)))){
//			hautGauche = true;
		}
		tmpPos = tmpPos.sub(Direction.GAUCHE).sub(Direction.HAUT);
		
		//diagonale hautdroite
		tmpPos = tmpPos.add(Direction.HAUT).add(Direction.DROITE);
		if ((inGrille(tmpPos) && (this.config.get(tmpPos).getType().equals(Type.MUR)))){
//			hautDroite = true;
		}
		tmpPos = tmpPos.sub(Direction.DROITE).sub(Direction.HAUT);
		
		//diagonale basgauche
		tmpPos = tmpPos.add(Direction.BAS).add(Direction.GAUCHE);
		if ((inGrille(tmpPos) && (this.config.get(tmpPos).getType().equals(Type.MUR)))){
//			basGauche = true;
		}
		tmpPos = tmpPos.sub(Direction.GAUCHE).sub(Direction.BAS);
		
		//diagonale basdroite
		tmpPos = tmpPos.add(Direction.BAS).add(Direction.DROITE);
		if ((inGrille(tmpPos) && (this.config.get(tmpPos).getType().equals(Type.MUR)))){
//			basDroite = true;
		}
		tmpPos = tmpPos.sub(Direction.DROITE).sub(Direction.BAS);
		
		if (haut && bas && droite && gauche) {
			res = "inter_4";
		} else if (haut && bas && droite) {
			res = "inter_3_droite";
		} else if (bas && droite && gauche) {
			res = "inter_3_bas";
		} else if (haut && bas && gauche) {
			res = "inter_3_gauche";
		} else if (haut && droite && gauche) {
			res = "inter_3_haut";
		} else if (haut && bas) {
			res = "mur_vertical";
		} else if (gauche && droite) {
			res = "mur_horizontal";
		} else if (haut && droite) {
			res = "coin_bas_gauche";
		} else if (haut && gauche) {
			res = "coin_bas_droite";
		} else if (bas && gauche) {
			res = "coin_haut_droite";
		} else if (bas && droite) {
			res = "coin_haut_gauche";
		} else if (bas) {
			res = "bout_haut";
		} else if (haut) {
			res = "bout_bas";
		} else if (gauche) {
			res = "bout_droite";
		} else if (droite) {
			res = "bout_gauche";
		} else {
			res = "carre";
		}
		
		return res;
	}

	private void saveScore() {
		//Creation du stage de sauvegarde de score 
		Stage save = new Stage();
		//Mise a jour du titre du stage
		save.setTitle("Sokoban - Niveau "+this.nivSelec);
		//Creation du conteneur
		VBox saveVBox = new VBox();
		//Creation du label et du champ de texte pour le pseudo
		Label pseudoLabel = new Label("Pseudo : ");
		TextField pseudoTF = new TextField();
		//Creation de la HBox
		HBox pseudoHBox = new HBox();
		pseudoHBox.getChildren().addAll(pseudoLabel, pseudoTF);
		//Creation des boutons
		Button saveBt = new Button("Sauvegarder le score");
		Button notSaveBt = new Button("Ne pas sauvegarder");
		//Ajouts des fonctions des boutons
		notSaveBt.setOnAction(e -> {
			save.hide();
			//Reset le niveau
			try {
				this.config = new Configuration(this.nivSelec);
			} catch (IOException error) {
				error.printStackTrace();
			}
			//Actualise la grille
			updateGrille(true);
			//Actualise le score
			updatescore();
		});
		saveBt.setOnAction(e -> {
			try {
				//Connection � la BDD des scores
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection con=DriverManager.getConnection("jdbc:mysql://e90653-mysql.services.easyname.eu/u143161db7?autoReconnect=true&useSSL=false","u143161db7","ProjetGL2");
				//Creation de la variable de requete
				Statement stmt=con.createStatement();
				//Cr�ation de la variable de resultat et recuperation du resultat de la requete
				int status =stmt.executeUpdate("INSERT INTO HighScore VALUES('"+pseudoTF.getText()+"',"+this.nivSelec+","+this.config.getJoueur().getHisto().size()+")"); 
				//Creation du label de fin de sauvegarde
				Label saveLabel = new Label();
				if (status == 1) {
					saveLabel.setText("Votre score a bien ete sauvegarde!");
				} else {
					saveLabel.setText("Desole, il semblerait que l'application n'arrive pas a sauvegarder votre score. Merci de verifier votre connexion internet.");
				}
				notSaveBt.setText("Ok");
				//Actualisation des elements 
				saveVBox.getChildren().clear();
				saveVBox.getChildren().addAll(saveLabel,notSaveBt);
				//On ferme la connection � la BDD
				con.close();
		} catch (Exception error) {
			System.out.println(error);
		}
		});
		//Ajouts des elements dans le conteneur
		saveVBox.getChildren().addAll(pseudoHBox,saveBt,notSaveBt);
		//Creation de la scene
		Scene saveScene = new Scene(saveVBox);
		//Set la scene
		save.setScene(saveScene);
		// On empeche de redimmenssionner la fenetre
		save.setResizable(false);
		// Affiche la fenetre au centre de l'ecran
		save.show();
		save.centerOnScreen();
	}

}