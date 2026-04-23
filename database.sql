-- =============================================
-- Cadastro de Produtos - Script de Banco de Dados
-- =============================================

CREATE DATABASE IF NOT EXISTS cadastro_produtos
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE cadastro_produtos;

CREATE TABLE IF NOT EXISTS produtos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    quantidade INT NOT NULL DEFAULT 0,
    categoria VARCHAR(100),
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Dados de exemplo
INSERT INTO produtos (nome, descricao, preco, quantidade, categoria) VALUES
('Notebook Gamer', 'Notebook com RTX 4060, 16GB RAM, SSD 512GB', 5499.90, 15, 'Eletrônicos'),
('Mouse Wireless', 'Mouse sem fio ergonômico com DPI ajustável', 89.90, 120, 'Periféricos'),
('Teclado Mecânico', 'Teclado mecânico RGB switch blue', 299.90, 45, 'Periféricos'),
('Monitor 27"', 'Monitor IPS 27 polegadas 144Hz', 1899.90, 22, 'Eletrônicos'),
('Cadeira Gamer', 'Cadeira ergonômica com apoio lombar', 1299.00, 8, 'Móveis');
