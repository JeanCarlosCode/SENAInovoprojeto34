package model;

import javafx.beans.property.*;

public class Venda {
    private final IntegerProperty id;
    private final IntegerProperty clienteId;
    private final StringProperty nomeCliente; // útil para a tabela
    private final StringProperty dataHora;
    private final DoubleProperty total;
    private final DoubleProperty desconto;
    private final DoubleProperty totalPago;
    private final DoubleProperty troco;
    private final StringProperty formaPagamento;

    public Venda() {
        this.id = new SimpleIntegerProperty(0);
        this.clienteId = new SimpleIntegerProperty(0);
        this.nomeCliente = new SimpleStringProperty("");
        this.dataHora = new SimpleStringProperty("");
        this.total = new SimpleDoubleProperty(0.0);
        this.desconto = new SimpleDoubleProperty(0.0);
        this.totalPago = new SimpleDoubleProperty(0.0);
        this.troco = new SimpleDoubleProperty(0.0);
        this.formaPagamento = new SimpleStringProperty("");
    }

    public Venda(int id, int clienteId, String nomeCliente, String dataHora, double total, 
                 double desconto, double totalPago, double troco, String formaPagamento) {
        this.id = new SimpleIntegerProperty(id);
        this.clienteId = new SimpleIntegerProperty(clienteId);
        this.nomeCliente = new SimpleStringProperty(nomeCliente);
        this.dataHora = new SimpleStringProperty(dataHora);
        this.total = new SimpleDoubleProperty(total);
        this.desconto = new SimpleDoubleProperty(desconto);
        this.totalPago = new SimpleDoubleProperty(totalPago);
        this.troco = new SimpleDoubleProperty(troco);
        this.formaPagamento = new SimpleStringProperty(formaPagamento);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int v) { this.id.set(v); }
    public IntegerProperty idProperty() { return id; }

    // Cliente ID
    public int getClienteId() { return clienteId.get(); }
    public void setClienteId(int v) { this.clienteId.set(v); }
    public IntegerProperty clienteIdProperty() { return clienteId; }

    // Nome Cliente
    public String getNomeCliente() { return nomeCliente.get(); }
    public void setNomeCliente(String v) { this.nomeCliente.set(v); }
    public StringProperty nomeClienteProperty() { return nomeCliente; }

    // DataHora
    public String getDataHora() { return dataHora.get(); }
    public void setDataHora(String v) { this.dataHora.set(v); }
    public StringProperty dataHoraProperty() { return dataHora; }

    // Total
    public double getTotal() { return total.get(); }
    public void setTotal(double v) { this.total.set(v); }
    public DoubleProperty totalProperty() { return total; }

    // Desconto
    public double getDesconto() { return desconto.get(); }
    public void setDesconto(double v) { this.desconto.set(v); }
    public DoubleProperty descontoProperty() { return desconto; }

    // Total Pago
    public double getTotalPago() { return totalPago.get(); }
    public void setTotalPago(double v) { this.totalPago.set(v); }
    public DoubleProperty totalPagoProperty() { return totalPago; }

    // Troco
    public double getTroco() { return troco.get(); }
    public void setTroco(double v) { this.troco.set(v); }
    public DoubleProperty trocoProperty() { return troco; }

    // Forma Pgto
    public String getFormaPagamento() { return formaPagamento.get(); }
    public void setFormaPagamento(String v) { this.formaPagamento.set(v); }
    public StringProperty formaPagamentoProperty() { return formaPagamento; }
}
