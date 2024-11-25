package com.example.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.model.Categorie;
import com.example.model.Produit;
import com.example.model.SousCategorie;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DataProduits {

    public static ObservableList<Categorie> getCategories() throws SQLException {
        ObservableList<Categorie> categories = FXCollections.observableArrayList();
        String query = "SELECT * FROM categorie;";
        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categories.add(new Categorie(
                    rs.getInt("id_categorie"),
                    rs.getString("nom"),
                     rs.getString("couleur_css_hexadecimal") 
                ));
            }
        }
        System.out.println(categories);
        return categories;
    }

    public static ObservableList<SousCategorie> getSousCategories(int categorieId) throws SQLException {
        ObservableList<SousCategorie> sousCategories = FXCollections.observableArrayList();
        String query = "SELECT * FROM sous_categorie WHERE id_categorie = ?";

        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, categorieId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sousCategories.add(new SousCategorie(
                    rs.getInt("id_sous_categorie"),
                    rs.getString("nom"),
                    rs.getInt("id_categorie"),
                    rs.getString("couleur_css_hexadecimal") 
                ));
            }
        }
        return sousCategories;
    }

    public static ObservableList<Produit> getProductsBySousCategorie(int sousCategorieId) throws SQLException {
        ObservableList<Produit> produits = FXCollections.observableArrayList();
        String query = "SELECT * FROM produit WHERE id_sous_categorie = ?";

        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, sousCategorieId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                produits.add(new Produit(
                    rs.getInt("id_produit"),
                    rs.getString("nom"),
                    rs.getInt("id_sous_categorie"),
                    rs.getString("couleur_css_hexadecimal") 
                ));
            }
        }
        return produits;
    }

    public static ObservableList<Produit> getProductsByCategorie(int CategorieId) throws SQLException {
        ObservableList<Produit> produits = FXCollections.observableArrayList();
        String query = "SELECT * FROM produit WHERE id_categorie = ?";
    
        try (Connection conn = DataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            stmt.setInt(1, CategorieId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                produits.add(new Produit(
                    rs.getInt("id_produit"),
                    rs.getString("nom"),
                    rs.getInt("id_categorie"),
                    rs.getString("couleur_css_hexadecimal")  // Récupération de la couleur
                ));
            }
        }
        return produits;
    
}
}
