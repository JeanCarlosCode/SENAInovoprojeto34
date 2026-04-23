-- Script Inicial de Configuração do Banco de Dados
-- EletroTech Distribuidora - SENAI

CREATE DATABASE IF NOT EXISTS cadastro_produtos;
USE cadastro_produtos;

-- NOTA: As tabelas do sistema (usuarios, produtos, historico_produtos, clientes, etc)
-- são criadas e estruturadas automaticamente pelas classes DAO (Data Access Object) 
-- utilizando a cláusula "CREATE TABLE IF NOT EXISTS" na inicialização da aplicação.
-- Portanto, apenas este script inicial é necessário para subir o projeto do zero!
