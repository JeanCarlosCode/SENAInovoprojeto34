package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Cliente;

public class ClienteDAO {

    public ClienteDAO() {
        criarTabelaSeNaoExistir();
    }

    private void criarTabelaSeNaoExistir() {
        String sql = "CREATE TABLE IF NOT EXISTS clientes ("
                   + "id INT AUTO_INCREMENT PRIMARY KEY, "
                   + "nome VARCHAR(255) NOT NULL, "
                   + "documento VARCHAR(20) NOT NULL UNIQUE, "
                   + "email VARCHAR(100), "
                   + "telefone VARCHAR(20), "
                   + "ativo BOOLEAN DEFAULT TRUE"
                   + ")";
        try (Connection conn = ConexaoDB.getConexao();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabela clientes: " + e.getMessage());
        }
    }

    public boolean inserir(Cliente cliente) throws SQLException {
        // Validation for uniqueness is handled by the UNIQUE constraint, but we can catch SQLIntegrityConstraintViolationException
        String sql = "INSERT INTO clientes (nome, documento, email, telefone, ativo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getDocumento());
            stmt.setString(3, cliente.getEmail());
            stmt.setString(4, cliente.getTelefone());
            stmt.setBoolean(5, cliente.isAtivo());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean atualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE clientes SET nome = ?, documento = ?, email = ?, telefone = ?, ativo = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getDocumento());
            stmt.setString(3, cliente.getEmail());
            stmt.setString(4, cliente.getTelefone());
            stmt.setBoolean(5, cliente.isAtivo());
            stmt.setInt(6, cliente.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM clientes WHERE id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir cliente: " + e.getMessage());
            return false;
        }
    }

    public List<Cliente> listarTodos() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clientes ORDER BY nome ASC";
        
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            while (rs.next()) {
                Cliente c = new Cliente(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("documento"),
                    rs.getString("email"),
                    rs.getString("telefone"),
                    rs.getBoolean("ativo")
                );
                clientes.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar clientes: " + e.getMessage());
        }
        return clientes;
    }

    public List<Cliente> buscar(String termo) {
        List<Cliente> clientes = new ArrayList<>();
        // Busca por nome, documento ou email
        String sql = "SELECT * FROM clientes WHERE nome LIKE ? OR documento LIKE ? OR email LIKE ? ORDER BY nome ASC";
        
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            String busca = "%" + termo + "%";
            stmt.setString(1, busca);
            stmt.setString(2, busca);
            stmt.setString(3, busca);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Cliente c = new Cliente(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("documento"),
                        rs.getString("email"),
                        rs.getString("telefone"),
                        rs.getBoolean("ativo")
                    );
                    clientes.add(c);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar clientes: " + e.getMessage());
        }
        return clientes;
    }
}
