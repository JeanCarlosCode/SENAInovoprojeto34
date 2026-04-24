package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.ItemVenda;
import model.Venda;

public class VendaDAO {

    public VendaDAO() {
        criarTabelasSeNaoExistirem();
    }

    private void criarTabelasSeNaoExistirem() {
        String sqlVendas = "CREATE TABLE IF NOT EXISTS vendas ("
                         + "id INT AUTO_INCREMENT PRIMARY KEY, "
                         + "cliente_id INT, "
                         + "data_hora DATETIME DEFAULT CURRENT_TIMESTAMP, "
                         + "total DECIMAL(10,2) NOT NULL, "
                         + "desconto DECIMAL(10,2) DEFAULT 0, "
                         + "total_pago DECIMAL(10,2) NOT NULL, "
                         + "troco DECIMAL(10,2) DEFAULT 0, "
                         + "forma_pagamento VARCHAR(50) NOT NULL, "
                         + "FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE SET NULL"
                         + ")";
                         
        String sqlItens = "CREATE TABLE IF NOT EXISTS vendas_itens ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "venda_id INT NOT NULL, "
                        + "produto_id INT, "
                        + "quantidade INT NOT NULL, "
                        + "preco_unitario DECIMAL(10,2) NOT NULL, "
                        + "subtotal DECIMAL(10,2) NOT NULL, "
                        + "FOREIGN KEY (venda_id) REFERENCES vendas(id) ON DELETE CASCADE, "
                        + "FOREIGN KEY (produto_id) REFERENCES produtos(id) ON DELETE SET NULL"
                        + ")";
                        
        try (Connection conn = ConexaoDB.getConexao();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlVendas);
            stmt.execute(sqlItens);
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabelas de vendas: " + e.getMessage());
        }
    }

    /**
     * RN01 e RN02 - Grava a venda, grava os itens, dá baixa no estoque e gera histórico, tudo via Transação.
     */
    public boolean gravarVenda(Venda venda, List<ItemVenda> itens, String usuarioLogado) throws Exception {
        Connection conn = null;
        try {
            conn = ConexaoDB.getConexao();
            conn.setAutoCommit(false); // Inicia transação

            // 1. Inserir Venda (cabecalho)
            String sqlVenda = "INSERT INTO vendas (cliente_id, total, desconto, total_pago, troco, forma_pagamento) VALUES (?, ?, ?, ?, ?, ?)";
            int vendaId = 0;
            try (PreparedStatement stmt = conn.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                if (venda.getClienteId() > 0) stmt.setInt(1, venda.getClienteId());
                else stmt.setNull(1, java.sql.Types.INTEGER);
                
                stmt.setDouble(2, venda.getTotal());
                stmt.setDouble(3, venda.getDesconto());
                stmt.setDouble(4, venda.getTotalPago());
                stmt.setDouble(5, venda.getTroco());
                stmt.setString(6, venda.getFormaPagamento());
                
                stmt.executeUpdate();
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    vendaId = rs.getInt(1);
                    venda.setId(vendaId); // atualiza na ram
                } else {
                    throw new SQLException("Falha ao obter o ID da nova venda.");
                }
            }

            // 2. Processar cada Item
            String sqlItem = "INSERT INTO vendas_itens (venda_id, produto_id, quantidade, preco_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
            String sqlCheckEstoque = "SELECT quantidade FROM produtos WHERE id = ? FOR UPDATE"; // Row lock
            String sqlUpdateEstoque = "UPDATE produtos SET quantidade = quantidade - ? WHERE id = ?";
            String sqlHistorico = "INSERT INTO historico_produtos (produto_id, data_hora, tipo_movimentacao, quantidade, detalhes, usuario) VALUES (?, NOW(), 'SAÍDA', ?, ?, ?)";

            try (PreparedStatement stmtItem = conn.prepareStatement(sqlItem);
                 PreparedStatement stmtCheck = conn.prepareStatement(sqlCheckEstoque);
                 PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdateEstoque);
                 PreparedStatement stmtHist = conn.prepareStatement(sqlHistorico)) {
                 
                for (ItemVenda item : itens) {
                    // a. Verifica estoque (RN02)
                    stmtCheck.setInt(1, item.getProdutoId());
                    ResultSet rsCheck = stmtCheck.executeQuery();
                    if (rsCheck.next()) {
                        int estqAtual = rsCheck.getInt("quantidade");
                        if (estqAtual < item.getQuantidade()) {
                            throw new Exception("Estoque insuficiente para o produto: " + item.getNomeProduto());
                        }
                    } else {
                        throw new Exception("Produto não encontrado no banco: " + item.getNomeProduto());
                    }
                    
                    // b. Inserir Item
                    stmtItem.setInt(1, vendaId);
                    stmtItem.setInt(2, item.getProdutoId());
                    stmtItem.setInt(3, item.getQuantidade());
                    stmtItem.setDouble(4, item.getPrecoUnitario());
                    stmtItem.setDouble(5, item.getSubtotal());
                    stmtItem.executeUpdate();
                    
                    // c. Baixa no Estoque (RN01)
                    stmtUpdate.setInt(1, item.getQuantidade());
                    stmtUpdate.setInt(2, item.getProdutoId());
                    stmtUpdate.executeUpdate();
                    
                    // d. Inserir Log Histórico Automático
                    stmtHist.setInt(1, item.getProdutoId());
                    stmtHist.setInt(2, item.getQuantidade());
                    stmtHist.setString(3, "Venda #" + vendaId);
                    stmtHist.setString(4, usuarioLogado != null ? usuarioLogado : "Sistema");
                    stmtHist.executeUpdate();
                }
            }
            
            conn.commit(); // Efetiva transação
            return true;
            
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e; // Repassa a msg para o Controller
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
    
    /**
     * RN03 - Estorna a venda devolvendo o estoque.
     */
    public boolean estornarVenda(int vendaId, String usuarioLogado) throws Exception {
        Connection conn = null;
        try {
            conn = ConexaoDB.getConexao();
            conn.setAutoCommit(false);
            
            // Puxa os itens da venda
            String sqlBuscaItens = "SELECT produto_id, quantidade FROM vendas_itens WHERE venda_id = ?";
            String sqlUpdateEstoque = "UPDATE produtos SET quantidade = quantidade + ? WHERE id = ?";
            String sqlHistorico = "INSERT INTO historico_produtos (produto_id, data_hora, tipo_movimentacao, quantidade, detalhes, usuario) VALUES (?, NOW(), 'ENTRADA', ?, ?, ?)";
            String sqlDeleteVenda = "DELETE FROM vendas WHERE id = ?"; // A cascade deletará os itens automaticamente!
            
            try (PreparedStatement stmtBusca = conn.prepareStatement(sqlBuscaItens);
                 PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdateEstoque);
                 PreparedStatement stmtHist = conn.prepareStatement(sqlHistorico);
                 PreparedStatement stmtDelete = conn.prepareStatement(sqlDeleteVenda)) {
                 
                stmtBusca.setInt(1, vendaId);
                ResultSet rs = stmtBusca.executeQuery();
                while (rs.next()) {
                    int prodId = rs.getInt("produto_id");
                    int qtd = rs.getInt("quantidade");
                    
                    // Restaura estoque
                    stmtUpdate.setInt(1, qtd);
                    stmtUpdate.setInt(2, prodId);
                    stmtUpdate.executeUpdate();
                    
                    // Registra Histórico (Estorno)
                    stmtHist.setInt(1, prodId);
                    stmtHist.setInt(2, qtd);
                    stmtHist.setString(3, "Estorno / Cancela. Venda #" + vendaId);
                    stmtHist.setString(4, usuarioLogado != null ? usuarioLogado : "Sistema");
                    stmtHist.executeUpdate();
                }
                
                stmtDelete.setInt(1, vendaId);
                stmtDelete.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public List<Venda> buscarUltimasDoCliente(int clienteId) {
        List<Venda> vendas = new ArrayList<>();
        String sql = "SELECT * FROM vendas WHERE cliente_id = ? ORDER BY data_hora DESC LIMIT 5";
        
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setInt(1, clienteId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Venda v = new Venda();
                v.setId(rs.getInt("id"));
                v.setDataHora(rs.getString("data_hora"));
                v.setTotal(rs.getDouble("total"));
                v.setFormaPagamento(rs.getString("forma_pagamento"));
                vendas.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vendas;
    }
}
