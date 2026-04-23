package model;

import javafx.beans.property.*;

public class Produto {

    private final IntegerProperty id;
    private final StringProperty nome;
    private final StringProperty descricao;
    private final DoubleProperty precoCusto;
    private final DoubleProperty precoVenda;
    private final IntegerProperty quantidade;
    private final IntegerProperty estoqueMinimo;
    private final StringProperty categoria;
    private final StringProperty codigoBarras;

    public Produto() {
        this.id = new SimpleIntegerProperty();
        this.nome = new SimpleStringProperty("");
        this.descricao = new SimpleStringProperty("");
        this.precoCusto = new SimpleDoubleProperty(0.0);
        this.precoVenda = new SimpleDoubleProperty(0.0);
        this.quantidade = new SimpleIntegerProperty(0);
        this.estoqueMinimo = new SimpleIntegerProperty(5);
        this.categoria = new SimpleStringProperty("");
        this.codigoBarras = new SimpleStringProperty("");
    }

    public Produto(int id, String nome, String descricao, double precoCusto, double precoVenda, int quantidade, int estoqueMinimo, String categoria, String codigoBarras) {
        this.id = new SimpleIntegerProperty(id);
        this.nome = new SimpleStringProperty(nome);
        this.descricao = new SimpleStringProperty(descricao);
        this.precoCusto = new SimpleDoubleProperty(precoCusto);
        this.precoVenda = new SimpleDoubleProperty(precoVenda);
        this.quantidade = new SimpleIntegerProperty(quantidade);
        this.estoqueMinimo = new SimpleIntegerProperty(estoqueMinimo);
        this.categoria = new SimpleStringProperty(categoria);
        this.codigoBarras = new SimpleStringProperty(codigoBarras);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Nome
    public String getNome() { return nome.get(); }
    public void setNome(String nome) { this.nome.set(nome); }
    public StringProperty nomeProperty() { return nome; }

    // Descrição
    public String getDescricao() { return descricao.get(); }
    public void setDescricao(String descricao) { this.descricao.set(descricao); }
    public StringProperty descricaoProperty() { return descricao; }

    // Preço Custo
    public double getPrecoCusto() { return precoCusto.get(); }
    public void setPrecoCusto(double precoCusto) { this.precoCusto.set(precoCusto); }
    public DoubleProperty precoCustoProperty() { return precoCusto; }

    // Preço Venda
    public double getPrecoVenda() { return precoVenda.get(); }
    public void setPrecoVenda(double precoVenda) { this.precoVenda.set(precoVenda); }
    public DoubleProperty precoVendaProperty() { return precoVenda; }

    // Quantidade
    public int getQuantidade() { return quantidade.get(); }
    public void setQuantidade(int quantidade) { this.quantidade.set(quantidade); }
    public IntegerProperty quantidadeProperty() { return quantidade; }

    // Categoria
    public String getCategoria() { return categoria.get(); }
    public void setCategoria(String categoria) { this.categoria.set(categoria); }
    public StringProperty categoriaProperty() { return categoria; }

    // Código de Barras
    public String getCodigoBarras() { return codigoBarras.get(); }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras.set(codigoBarras); }
    public StringProperty codigoBarrasProperty() { return codigoBarras; }

    // Estoque Minimo
    public int getEstoqueMinimo() { return estoqueMinimo.get(); }
    public void setEstoqueMinimo(int estoqueMinimo) { this.estoqueMinimo.set(estoqueMinimo); }
    public IntegerProperty estoqueMinimoProperty() { return estoqueMinimo; }

    @Override
    public String toString() {
        return "Produto [id=" + getId() + ", nome=" + getNome() + ", precoVenda=" + getPrecoVenda() + "]";
    }
}
