package model;

import javafx.beans.property.*;

public class HistoricoProduto {

    private final IntegerProperty id;
    private final StringProperty acao;
    private final IntegerProperty produtoId;
    private final StringProperty produtoNome;
    private final StringProperty usuario;
    private final StringProperty detalhes;
    private final StringProperty dataHora;

    public HistoricoProduto(int id, String acao, int produtoId, String produtoNome, String usuario, String detalhes, String dataHora) {
        this.id = new SimpleIntegerProperty(id);
        this.acao = new SimpleStringProperty(acao);
        this.produtoId = new SimpleIntegerProperty(produtoId);
        this.produtoNome = new SimpleStringProperty(produtoNome);
        this.usuario = new SimpleStringProperty(usuario);
        this.detalhes = new SimpleStringProperty(detalhes);
        this.dataHora = new SimpleStringProperty(dataHora);
    }

    // Construtor sem ID (para inserção)
    public HistoricoProduto(String acao, int produtoId, String produtoNome, String usuario, String detalhes) {
        this(0, acao, produtoId, produtoNome, usuario, detalhes, null);
    }

    // --- Getters & Setters ---
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    public String getAcao() { return acao.get(); }
    public StringProperty acaoProperty() { return acao; }

    public int getProdutoId() { return produtoId.get(); }
    public IntegerProperty produtoIdProperty() { return produtoId; }

    public String getProdutoNome() { return produtoNome.get(); }
    public StringProperty produtoNomeProperty() { return produtoNome; }

    public String getUsuario() { return usuario.get(); }
    public StringProperty usuarioProperty() { return usuario; }

    public String getDetalhes() { return detalhes.get(); }
    public StringProperty detalhesProperty() { return detalhes; }

    public String getDataHora() { return dataHora.get(); }
    public StringProperty dataHoraProperty() { return dataHora; }
}
