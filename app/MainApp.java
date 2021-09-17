package com.sokoban.app;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application{

	@Override
	public void start(Stage primaryStage) throws IOException {
		primaryStage.setTitle("Sokoban");	//Change le titre de la fenetre pour Sokoban
		//Charge le fichier Main.xml dans un parent
		Parent root = FXMLLoader.load(getClass().getResource("/ressources/fxml/Main.fxml"));  
		//Charge une scene � partir du parent
		Scene scene = new Scene(root);
		//Affiche la scene dans la fenetre
		primaryStage.setScene(scene);
		//Affiche la fenetre
		primaryStage.show();
		//Centre la fenetre
		primaryStage.centerOnScreen();
	}
	
	public static void main(String[] args) {
		//Lance l'application
		launch(args);
	}

}