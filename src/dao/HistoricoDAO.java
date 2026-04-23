package dao;

import model.HistoricoProduto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoricoDAO {

    // ==================== AUTO-CRIAR TABELA ====================
    static {
        String sql = "CREATE TABLE IF NOT EXISTS historico_produtos ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "acao VARCHAR(30) NOT NULL, "
                + "produto_id INT, "
                + "produto_nome VARCHAR(255) NOT NULL, "
                + "usuario VARCHAR(100), "
                + "detalhes TEXT, "
                + "data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        Connection conn = ConexaoDB.getConexao();
        if (conn != null) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                System.err.println("Erro ao criar tabela historico_produtos: " + e.getMessage());
            } finally {
                ConexaoDB.fecharConexao(conn);
            }
        }
    }

    // ==================== REGISTRAR ====================
    public boolean registrar(HistoricoProduto h) {
        String sql = "INSERT INTO historico_produtos (acao, produto_id, produto_nome, usuario, detalhes) VALUES (?, ?, ?, ?, ?)";
        Connection conn = ConexaoDB.getConexao();

        if (conn == null) return false;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, h.getAcao());
            stmt.setInt(2, h.getProdutoId());
            stmt.setString(3, h.getProdutoNome());
            stmt.setString(4, h.getUsuario());
            stmt.setString(5, h.getDetalhes());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao registrar histórico: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            ConexaoDB.fecharConexao(conn);
        }
    }

    // ==================== LISTAR TODOS ====================
    public List<HistoricoProduto> listarTodos() {
        List<HistoricoProduto> lista = new ArrayList<>();
        String sql = "SELECT * FROM historico_produtos ORDER BY data_hora DESC";
        Connection conn = ConexaoDB.getConexao();

        if (conn == null) return lista;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                HistoricoProduto h = new HistoricoProduto(
                    rs.getInt("id"),
                    rs.getString("acao"),
                    rs.getInt("produto_id"),
                    rs.getString("produto_nome"),
                    rs.getString("usuario"),
                    rs.getString("detalhes"),
                    rs.getString("data_hora")
                );
                lista.add(h);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar histórico: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConexaoDB.fecharConexao(conn);
        }

        return lista;
    }

    // ==================== LISTAR POR PRODUTO ====================
    public List<HistoricoProduto> listarPorProduto(int produtoId) {
        List<HistoricoProduto> lista = new ArrayList<>();
        String sql = "SELECT * FROM historico_produtos WHERE produto_id = ? ORDER BY data_hora DESC";
        Connection conn = ConexaoDB.getConexao();

        if (conn == null) return lista;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, produtoId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HistoricoProduto h = new HistoricoProduto(
                    rs.getInt("id"),
                    rs.getString("acao"),
                    rs.getInt("produto_id"),
                    rs.getString("produto_nome"),
                    rs.getString("usuario"),
                    rs.getString("detalhes"),
                    rs.getString("data_hora")
                );
                lista.add(h);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar histórico por produto: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConexaoDB.fecharConexao(conn);
        }

        return lista;
    }
}
