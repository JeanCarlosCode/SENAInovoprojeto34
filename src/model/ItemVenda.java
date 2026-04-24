package model;

import javafx.beans.property.*;

public class ItemVenda {
    private final IntegerProperty id;
    private final IntegerProperty vendaId;
    private final IntegerProperty produtoId;
    private final StringProperty nomeProduto; // util para listagem
    private final StringProperty codigoBarras; // util para listagem
    private final IntegerProperty quantidade;
    private final DoubleProperty precoUnitario;
    private final DoubleProperty subtotal;

    public ItemVenda() {
        this.id = new SimpleIntegerProperty(0);
        this.vendaId = new SimpleIntegerProperty(0);
        this.produtoId = new SimpleIntegerProperty(0);
        this.nomeProduto = new SimpleStringProperty("");
        this.codigoBarras = new SimpleStringProperty("");
        this.quantidade = new SimpleIntegerProperty(1);
        this.precoUnitario = new SimpleDoubleProperty(0.0);
        this.subtotal = new SimpleDoubleProperty(0.0);
    }
    
    public ItemVenda(Produto p, int qtdOrigem) {
        this();
        this.produtoId.set(p.getId());
        this.nomeProduto.set(p.getNome());
        this.codigoBarras.set(p.getCodigoBarras());
        this.quantidade.set(qtdOrigem);
        this.precoUnitario.set(p.getPrecoVenda());
        calcularSubtotal();
    }

    public void calcularSubtotal() {
        this.subtotal.set(this.quantidade.get() * this.precoUnitario.get());
    }

    // Getters and Setters
    public int getId() { return id.get(); }
    public void setId(int v) { this.id.set(v); }

    public int getVendaId() { return vendaId.get(); }
    public void setVendaId(int v) { this.vendaId.set(v); }

    public int getProdutoId() { return produtoId.get(); }
    public void setProdutoId(int v) { this.produtoId.set(v); }

    public String getNomeProduto() { return nomeProduto.get(); }
    public void setNomeProduto(String v) { this.nomeProduto.set(v); }
    public StringProperty nomeProdutoProperty() { return nomeProduto; }
    
    public String getCodigoBarras() { return codigoBarras.get(); }
    public void setCodigoBarras(String v) { this.codigoBarras.set(v); }
    public StringProperty codigoBarrasProperty() { return codigoBarras; }

    public int getQuantidade() { return quantidade.get(); }
    public void setQuantidade(int v) { 
        this.quantidade.set(v); 
        calcularSubtotal();
    }
    public IntegerProperty quantidadeProperty() { return quantidade; }

    public double getPrecoUnitario() { return precoUnitario.get(); }
    public void setPrecoUnitario(double v) { 
        this.precoUnitario.set(v); 
        calcularSubtotal();
    }
    public DoubleProperty precoUnitarioProperty() { return precoUnitario; }

    public double getSubtotal() { return subtotal.get(); }
    public DoubleProperty subtotalProperty() { return subtotal; }
}
