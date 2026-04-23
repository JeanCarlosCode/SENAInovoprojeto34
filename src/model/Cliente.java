package model;

import javafx.beans.property.*;

public class Cliente {

    private final IntegerProperty id;
    private final StringProperty nome;
    private final StringProperty documento; // CPF ou CNPJ
    private final StringProperty email;
    private final StringProperty telefone;
    private final BooleanProperty ativo;

    public Cliente() {
        this.id = new SimpleIntegerProperty(0);
        this.nome = new SimpleStringProperty("");
        this.documento = new SimpleStringProperty("");
        this.email = new SimpleStringProperty("");
        this.telefone = new SimpleStringProperty("");
        this.ativo = new SimpleBooleanProperty(true);
    }

    public Cliente(int id, String nome, String documento, String email, String telefone, boolean ativo) {
        this.id = new SimpleIntegerProperty(id);
        this.nome = new SimpleStringProperty(nome);
        this.documento = new SimpleStringProperty(documento);
        this.email = new SimpleStringProperty(email);
        this.telefone = new SimpleStringProperty(telefone);
        this.ativo = new SimpleBooleanProperty(ativo);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Nome
    public String getNome() { return nome.get(); }
    public void setNome(String nome) { this.nome.set(nome); }
    public StringProperty nomeProperty() { return nome; }

    // Documento (CPF/CNPJ)
    public String getDocumento() { return documento.get(); }
    public void setDocumento(String documento) { this.documento.set(documento); }
    public StringProperty documentoProperty() { return documento; }

    // Email
    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }

    // Telefone
    public String getTelefone() { return telefone.get(); }
    public void setTelefone(String telefone) { this.telefone.set(telefone); }
    public StringProperty telefoneProperty() { return telefone; }

    // Status Ativo
    public boolean isAtivo() { return ativo.get(); }
    public void setAtivo(boolean ativo) { this.ativo.set(ativo); }
    public BooleanProperty ativoProperty() { return ativo; }

    @Override
    public String toString() {
        return "Cliente [id=" + getId() + ", nome=" + getNome() + ", doc=" + getDocumento() + ", ativo=" + isAtivo() + "]";
    }
}
