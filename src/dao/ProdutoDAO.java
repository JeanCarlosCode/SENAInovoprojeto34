package dao;

import model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    // ==================== INSERIR ====================
    public boolean inserir(Produto produto) {
        String sql = "INSERT INTO produtos (nome, descricao, preco_custo, preco_venda, quantidade, estoque_minimo, categoria, codigo_barras) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = ConexaoDB.getConexao();

        if (conn == null) return false;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getDescricao());
            stmt.setDouble(3, produto.getPrecoCusto());
            stmt.setDouble(4, produto.getPrecoVenda());
            stmt.setInt(5, produto.getQuantidade());
            stmt.setInt(6, produto.getEstoqueMinimo());
            stmt.setString(7, produto.getCategoria());
            stmt.setString(8, produto.getCodigoBarras());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir produto: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            ConexaoDB.fecharConexao(conn);
        }
    }

    // ==================== LISTAR TODOS ====================
    public List<Produto> listarTodos() {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produtos ORDER BY id DESC";
        Connection conn = ConexaoDB.getConexao();

        if (conn == null) return produtos;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Produto p = new Produto(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("descricao"),
                    rs.getDouble("preco_custo"),
                    rs.getDouble("preco_venda"),
                    rs.getInt("quantidade"),
                    rs.getInt("estoque_minimo"),
                    rs.getString("categoria"),
                    rs.getString("codigo_barras")
                );
                produtos.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar produtos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConexaoDB.fecharConexao(conn);
        }

        return produtos;
    }

    // ==================== ATUALIZAR ====================
    public boolean atualizar(Produto produto) {
        String sql = "UPDATE produtos SET nome=?, descricao=?, preco_custo=?, preco_venda=?, quantidade=?, estoque_minimo=?, categoria=?, codigo_barras=? WHERE id=?";
        Connection conn = ConexaoDB.getConexao();

        if (conn == null) return false;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getDescricao());
            stmt.setDouble(3, produto.getPrecoCusto());
            stmt.setDouble(4, produto.getPrecoVenda());
            stmt.setInt(5, produto.getQuantidade());
            stmt.setInt(6, produto.getEstoqueMinimo());
            stmt.setString(7, produto.getCategoria());
            stmt.setString(8, produto.getCodigoBarras());
            stmt.setInt(9, produto.getId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar produto: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            ConexaoDB.fecharConexao(conn);
        }
    }

    // ==================== DELETAR ====================
    public boolean deletar(int id) {
        String sql = "DELETE FROM produtos WHERE id=?";
        Connection conn = ConexaoDB.getConexao();

        if (conn == null) return false;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao deletar produto: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            ConexaoDB.fecharConexao(conn);
        }
    }

    // ==================== BUSCAR POR NOME OU COD BARRA ====================
    public List<Produto> buscarPorNome(String nomeOuBarra) {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produtos WHERE nome LIKE ? OR codigo_barras = ? ORDER BY id DESC";
        Connection conn = ConexaoDB.getConexao();

        if (conn == null) return produtos;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nomeOuBarra + "%");
            stmt.setString(2, nomeOuBarra);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Produto p = new Produto(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("descricao"),
                    rs.getDouble("preco_custo"),
                    rs.getDouble("preco_venda"),
                    rs.getInt("quantidade"),
                    rs.getInt("estoque_minimo"),
                    rs.getString("categoria"),
                    rs.getString("codigo_barras")
                );
                produtos.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar produtos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            ConexaoDB.fecharConexao(conn);
        }

        return produtos;
    }
}
