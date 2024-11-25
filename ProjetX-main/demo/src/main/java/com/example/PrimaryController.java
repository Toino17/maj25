package com.example;

import java.io.IOException;
import java.sql.SQLException;

import com.example.DAO.DataProduits;
import com.example.DAO.DataSupplement;
import com.example.DAO.DataUser;
import com.example.model.Categorie;
import com.example.model.Produit;
import com.example.model.SousCategorie;
import com.example.model.Supplement;
import com.example.model.UserSession;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PrimaryController {

    private Stage stage;
    
    public void setStage(Stage stage) {
    this.stage = stage;
    }

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    @FXML
    private void switchToTroisiemary() throws IOException {
        App.setRoot("troisiemary");
    }
    @FXML
    private Label idUtilisateur;

    @FXML
    public void initialize() {
        updateMenu();
        UserSession session = UserSession.getInstance();
        String nom = session.getNom();
        String message = nom != null ? "Bienvenue, " + nom : "Utilisateur non connecté";
        idUtilisateur.setText(message);
    }


        //CREATION D UNE NOUVELLE TABLE//
@FXML
private void nouvelleTable(ActionEvent event) {
    Dialog<String> dialog = new Dialog<>();
    Stage currentStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
    dialog.initOwner(currentStage);

    dialog.setTitle("Créer une nouvelle commande");
    dialog.setHeaderText("Entrez le numéro de la table (ex : 4)");

    Label label = new Label("Numéro de table");
    TextField textField = new TextField();

    ButtonType validerButtonType = new ButtonType("Valider");
    dialog.getDialogPane().getButtonTypes().addAll(validerButtonType, ButtonType.CANCEL);

    VBox vbox = new VBox(10, label, textField);
    dialog.getDialogPane().setContent(vbox);

    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == validerButtonType) {
            return textField.getText();
        }
        return null;
    });

    dialog.showAndWait().ifPresent(response -> {
        try {
            int tableId = Integer.parseInt(response);
            int userId = UserSession.getInstance().getIdUser();
            boolean success = DataUser.createTableWithSpecificId(tableId, userId);

            if (success) {
                System.out.println("Table " + tableId + " associée avec succès !");
            } else {
                System.out.println("Table " + tableId + " déjà occupée ou inexistante.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Veuillez entrer un numéro de table valide.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    });
}

    //CREATION D UNE NOUVELLE TABLE//

    //GESTION DES CATEGORIES//


    @FXML
    private FlowPane categoriesContainer;
    @FXML
    private VBox detailsContainer;

    private final ObservableList<Categorie> categories = FXCollections.observableArrayList();
    private final ObservableList<SousCategorie> sousCategories = FXCollections.observableArrayList();
    private final ObservableList<Produit> produits = FXCollections.observableArrayList();
    private final ObservableList<Supplement> supplements = FXCollections.observableArrayList();
    private final ObservableList<Produit> productsSelection = FXCollections.observableArrayList();

    //CHARGE LES MENUS STATIQUE//

    public void updateMenu() {
        categoriesContainer.getChildren().clear();

        try {
            categories.setAll(DataProduits.getCategories());

            for (Categorie category : categories) {
                Button button = new Button(category.getName());
                button.setMinWidth(100);
                button.setMinHeight(70);

                String hexColor = category.getCouleur();
                button.setStyle("-fx-base: " + hexColor + ";");

                button.setOnAction(e -> updateDetailsForCategory(category.getId()));

                categoriesContainer.getChildren().add(button);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //CHARGE LES MENUS STATIQUE//

    //CHARGE LES SOUS CATEGORIE//

    public void updateDetailsForCategory(int categoryId) {
        detailsContainer.getChildren().clear();
    
        try {
            // Vérifie si la catégorie a des sous-catégories
            sousCategories.setAll(DataProduits.getSousCategories(categoryId));
    
            if (!sousCategories.isEmpty()) {
                // Affiche les sous-catégories si elles existent
                FlowPane pane = new FlowPane();
    
                pane.setHgap(10); // Espacement horizontal entre les éléments
                pane.setVgap(10); // Espacement vertical entre les éléments
                pane.setAlignment(Pos.CENTER); // Alignement global du contenu
                pane.setPadding(new Insets(10)); // Ajoute une marge autour du FlowPane
    
                for (SousCategorie sousCategorie : sousCategories) {
                    Button button = new Button(sousCategorie.getNom());
                    button.setMinWidth(100);
                    button.setMinHeight(100);
    
                    // Charge les produits de la sous-catégorie
                    button.setOnAction(e -> updateDetailsForSousCategory(sousCategorie.getId()));
    
                    pane.getChildren().add(button);
    
                }
    
                detailsContainer.getChildren().add(pane);
            } else {
                // Pas de sous-catégories : affiche les produits de la catégorie directement
                updateDetailsForNoSub(categoryId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //CHARGE LES SOUS CATEGORIE//

    //CHARGE LES PRODUITS SANS SOUS CATEGORIES//

    public void updateDetailsForNoSub(int categoryId) {
        detailsContainer.getChildren().clear();
    
        try {
            // Charge uniquement les produits de la catégorie ou sous-catégorie donnée
            produits.setAll(DataProduits.getProductsByCategorie(categoryId));
    
            GridPane grid = createGrid();
    
            int row = 0;
            int col = 0;
    
            for (Produit produit : produits) {
                Button button = new Button(produit.getName());
                button.setMinWidth(100);
                button.setMinHeight(100);
    
                // Gère la sélection du produit
                button.setOnAction(e -> {
                    try {
                        handleProductSelection(produit, produit.getId());
                    } catch (SQLException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                });
                grid.add(button, col, row);
                col++;
                if (col == 3) {
                    col = 0;
                    row++;
                }
            }
            detailsContainer.getChildren().add(grid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //CHARGE LES PRODUITS SANS SOUS CATEGORIES//
    
    //CHARGE LES PRODUITS AVEC SOUS CATEGORIE//

    public void updateDetailsForSousCategory(int sousCategorieId) {
        detailsContainer.getChildren().clear();
    
        try {
            // Charge uniquement les produits de la catégorie ou sous-catégorie donnée
            produits.setAll(DataProduits.getProductsBySousCategorie(sousCategorieId));
    
            GridPane grid = createGrid();
    
            int row = 0;
            int col = 0;
    
            for (Produit produit : produits) {
                Button button = new Button(produit.getName());
                button.setMinWidth(100);
                button.setMinHeight(100);
    
                // Gère la sélection du produit
                button.setOnAction(e -> {
                    try {
                        handleProductSelection(produit, produit.getId());
                    } catch (SQLException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                });
                grid.add(button, col, row);
                
                col++;
                if (col == 3) {
                    col = 0;
                    row++;
                }
            }
            detailsContainer.getChildren().add(grid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //CHARGE LES PRODUITS AVEC SOUS CATEGORIE//
    
    //GESTIONS DU CLIQUE DU PRODUITS//
    private void handleProductSelection(Produit produit, int idproduit) throws SQLException {

        supplements.setAll(DataSupplement.getSupplementByProduit(idproduit));
        if (!supplements.isEmpty()) {
            showSupplement(supplements, produit.getName());
        }
        else{
            insertLocalProduct(idproduit);
            System.out.println(productsSelection);
        }

    }
    //GESTIONS DU CLIQUE DU PRODUITS//


    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        return grid;
    }



    //GESTION DES SUPPLEMENTS//

   @FXML
    private void showSupplement(ObservableList<Supplement> supplements, String produit) {
    Dialog<Void> dialog = new Dialog<>();
    dialog.initOwner(stage);

    dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    dialog.getDialogPane().getButtonTypes().add(ButtonType.APPLY);

    Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
    cancelButton.setText("Annuler");
    cancelButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-size: 8px;");
    cancelButton.setOnAction(e -> {
        System.out.println("Aucun supplément sélectionné");
        dialog.close();
    });

        Label produitLabel = new Label("Produit : " + produit);
        produitLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
    
        TableView<Supplement> tableView = new TableView<>();
        tableView.setEditable(false);
    
        TableColumn<Supplement, String> nomColonne = new TableColumn<>("Supplément");
        nomColonne.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
    
        tableView.getColumns().add(nomColonne);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    
        tableView.setMaxHeight(200);
        tableView.setPrefHeight(200);
        tableView.setMaxWidth(300);
        tableView.setPrefWidth(300);

    ObservableList<Supplement> supplementsSelectionnes = FXCollections.observableArrayList();
    tableView.setItems(supplementsSelectionnes);

    Button validateButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY);
    validateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 8px;");
    validateButton.setOnAction(e -> {
        System.out.println("Validation des suppléments sélectionnés :");
        supplementsSelectionnes.forEach(supplement -> 
            System.out.println("- " + supplement.getNom())
        );
        dialog.close();
    });

    Label label = new Label("Ajoutez des suppléments :");
    label.getStyleClass().add("labelNewTable");

    HBox hbox = new HBox(10);
    hbox.setStyle("-fx-alignment: center;");

    for (Supplement supplement : supplements) {
        String nomSupplement = supplement.getNom();
        Button buttonSupplement = new Button(nomSupplement);
        hbox.getChildren().add(buttonSupplement);

        buttonSupplement.setOnAction(e -> {
            System.out.println("Supplément ajouté : " + nomSupplement);
            supplementsSelectionnes.add(supplement);
        });
    }

    VBox vbox = new VBox(10, produitLabel, tableView, label, hbox);
    vbox.setPadding(new Insets(10));
    vbox.setStyle("-fx-alignment: center;");

    dialog.getDialogPane().setContent(vbox);
    dialog.showAndWait();
}

    //GESTION DES SUPPLEMENTS//

    //AJOUT DES PRODUITS EN LOCALE//
    

    private void insertLocalProduct(int idProduit) {

        for (Produit produit : produits) {
            if (produit.getId() == idProduit) {
                productsSelection.add(produit);
                System.out.println("Produit ajouté : " + produit.getName());
                return;
            }
            System.out.println("- ID : " + produit.getId() + ", Nom : " + produit.getName());
        }
        System.out.println("Produit avec ID " + idProduit + " introuvable.");
    }

}
