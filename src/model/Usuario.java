package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Usuario {
    
    private final IntegerProperty id;
    private final StringProperty nome;
    private final StringProperty username;
    private final StringProperty senha;
    private final StringProperty perfil;
    private final StringProperty fotoPath;

    public Usuario(int id, String nome, String username, String senha, String perfil) {
        this.id = new SimpleIntegerProperty(id);
        this.nome = new SimpleStringProperty(nome);
        this.username = new SimpleStringProperty(username);
        this.senha = new SimpleStringProperty(senha);
        this.perfil = new SimpleStringProperty(perfil);
        this.fotoPath = new SimpleStringProperty(null);
    }
    
    // Construtor sem ID (para criação)
    public Usuario(String nome, String username, String senha, String perfil) {
        this(0, nome, username, senha, perfil);
    }

    // --- GETTERS & SETTERS NATURAIS ---
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }

    public String getNome() { return nome.get(); }
    public void setNome(String value) { nome.set(value); }

    public String getUsername() { return username.get(); }
    public void setUsername(String value) { username.set(value); }

    public String getSenha() { return senha.get(); }
    public void setSenha(String value) { senha.set(value); }

    public String getPerfil() { return perfil.get(); }
    public void setPerfil(String value) { perfil.set(value); }

    public String getFotoPath() { return fotoPath.get(); }
    public void setFotoPath(String value) { fotoPath.set(value); }

    // --- GETTERS DE PROPRIEDADES (Foco JavaFX) ---
    public IntegerProperty idProperty() { return id; }
    public StringProperty nomeProperty() { return nome; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty senhaProperty() { return senha; }
    public StringProperty perfilProperty() { return perfil; }
    public StringProperty fotoPathProperty() { return fotoPath; }
}
