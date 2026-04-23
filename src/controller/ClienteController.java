package controller;

import dao.ClienteDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Cliente;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ClienteController {

    @FXML private Label lblUserName;
    @FXML private Button btnTheme;
    @FXML private MenuButton btnConfig;
    @FXML private MenuItem menuItemSair;

    // Form Ficha Cliente
    @FXML private TextField txtNome;
    @FXML private TextField txtDocumento;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefone;
    @FXML private ComboBox<String> cmbStatus;
    
    // Tabela
    @FXML private TableView<Cliente> tabelaClientes;
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colDocumento;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private TableColumn<Cliente, String> colTelefone;
    @FXML private TableColumn<Cliente, String> colStatus;
    @FXML private TextField txtBusca;
    @FXML private Label lblTotal;

    // View de Compras
    @FXML private ListView<String> listCompras;

    private ClienteDAO clienteDAO;
    private ObservableList<Cliente> clientesList;
    private Cliente clienteSelecionado;
    
    private boolean isDarkTheme = true;
    private String currentUser = "Visitante";

    @FXML
    public void initialize() {
        clienteDAO = new ClienteDAO();
        clientesList = FXCollections.observableArrayList();
        
        cmbStatus.setItems(FXCollections.observableArrayList("Ativo", "Inativo"));
        cmbStatus.getSelectionModel().select("Ativo");

        configurarTabela();
        carregarDados();
        
        // Listener de Seleção na Tabela (Passo 2 do Professor validado para clientes)
        tabelaClientes.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    mostrarDetalhesCliente(newSelection);
                }
            }
        );

        // Live Search (Busca Automática ao digitar)
        txtBusca.textProperty().addListener((obs, oldText, newText) -> {
            buscar();
        });
    }
    
    public void setUserData(String username, boolean darkTheme) {
        this.currentUser = username;
        this.lblUserName.setText(username != null && !username.isEmpty() ? username : "Visitante");
        this.isDarkTheme = darkTheme;
        atualizarTextoBotaoTema();
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        // Formatar ID para 6 dígitos
        colId.setCellFactory(tc -> new TableCell<Cliente, Integer>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                } else {
                    setText(String.format("%06d", id));
                }
            }
        });

        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDocumento.setCellValueFactory(new PropertyValueFactory<>("documento"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        
        // Formatar Ativo/Inativo na tabela
        colStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isAtivo() ? "Ativo" : "Inativo")
        );
    }

    private void carregarDados() {
        clientesList.setAll(clienteDAO.listarTodos());
        tabelaClientes.setItems(clientesList);
        lblTotal.setText("Clientes: " + clientesList.size());
    }

    private void mostrarDetalhesCliente(Cliente c) {
        clienteSelecionado = c;
        txtNome.setText(c.getNome());
        txtDocumento.setText(c.getDocumento());
        txtEmail.setText(c.getEmail());
        txtTelefone.setText(c.getTelefone());
        cmbStatus.getSelectionModel().select(c.isAtivo() ? "Ativo" : "Inativo");
        
        // Stub para Últimas 5 Compras
        // Simulando que vamos implementar via VendaDAO futuramente
        listCompras.setItems(FXCollections.observableArrayList(
            "(Vendas ainda não implementadas)"
        ));
    }

    @FXML
    private void salvar() {
        String nome = txtNome.getText();
        String documento = txtDocumento.getText();
        
        if (nome == null || nome.trim().isEmpty() || documento == null || documento.trim().isEmpty()) {
            mostrarAlerta("Validação", "Nome e CPF/CNPJ são obrigatórios.", Alert.AlertType.WARNING);
            return;
        }
        
        String email = txtEmail.getText();
        String telefone = txtTelefone.getText();
        boolean ativo = "Ativo".equals(cmbStatus.getValue());

        if (clienteSelecionado == null) {
            Cliente novoCliente = new Cliente(0, nome, documento, email, telefone, ativo);
            try {
                if(clienteDAO.inserir(novoCliente)) {
                    mostrarAlerta("Sucesso", "Cliente cadastrado com sucesso!", Alert.AlertType.INFORMATION);
                    limpar();
                    carregarDados();
                }
            } catch (SQLException e) {
                if(e.getMessage().contains("Duplicate") || e.getMessage().contains("UNIQUE")) {
                    mostrarAlerta("Erro", "O CPF/CNPJ já está cadastrado no sistema.", Alert.AlertType.ERROR);
                } else {
                    mostrarAlerta("Erro", "Erro ao inserir cliente: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } else {
            clienteSelecionado.setNome(nome);
            clienteSelecionado.setDocumento(documento);
            clienteSelecionado.setEmail(email);
            clienteSelecionado.setTelefone(telefone);
            clienteSelecionado.setAtivo(ativo);
            
            try {
                if(clienteDAO.atualizar(clienteSelecionado)) {
                    mostrarAlerta("Sucesso", "Cliente atualizado com sucesso!", Alert.AlertType.INFORMATION);
                    limpar();
                    carregarDados();
                }
            } catch (SQLException e) {
                mostrarAlerta("Erro", "Erro ao atualizar cliente: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void excluir() {
        if (clienteSelecionado == null) {
            mostrarAlerta("Aviso", "Selecione um cliente na tabela para excluir.", Alert.AlertType.WARNING);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Você tem certeza que deseja excluir " + clienteSelecionado.getNome() + "?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (clienteDAO.excluir(clienteSelecionado.getId())) {
                mostrarAlerta("Sucesso", "Cliente excluído.", Alert.AlertType.INFORMATION);
                limpar();
                carregarDados();
            } else {
                mostrarAlerta("Erro", "Não foi possível excluir o cliente.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void limpar() {
        clienteSelecionado = null;
        txtNome.clear();
        txtDocumento.clear();
        txtEmail.clear();
        txtTelefone.clear();
        cmbStatus.getSelectionModel().select("Ativo");
        tabelaClientes.getSelectionModel().clearSelection();
        listCompras.setItems(FXCollections.observableArrayList());
    }

    @FXML
    private void buscar() {
        String termo = txtBusca.getText();
        if (termo == null || termo.trim().isEmpty()) {
            carregarDados();
        } else {
            List<Cliente> resultados = clienteDAO.buscar(termo);
            clientesList.setAll(resultados);
            lblTotal.setText("Clientes Encontrados: " + resultados.size());
        }
    }
    
    // ============================================
    // MENU e NAVEGAÇÃO
    // ============================================

    @FXML
    public void alternarTema(ActionEvent event) {
        isDarkTheme = !isDarkTheme;
        
        Scene scene = btnTheme.getScene();
        if (scene != null) {
            scene.getStylesheets().clear();
            if (isDarkTheme) {
                scene.getStylesheets().add(getClass().getResource("/view/styles.css").toExternalForm());
            } else {
                scene.getStylesheets().add(getClass().getResource("/view/styles-light.css").toExternalForm());
            }
        }
        atualizarTextoBotaoTema();
    }

    private void atualizarTextoBotaoTema() {
        if (btnTheme != null) {
            btnTheme.setText(isDarkTheme ? "\u2600 Light" : "\uD83C\uDF18 Dark");
        }
    }
    
    @FXML
    private void abrirProdutos() {
        abrirTela("/view/ProdutoView.fxml", "Cadastro de Produtos");
    }

    @FXML
    private void abrirVendas() {
        mostrarAlerta("Aviso", "Módulo de Vendas será implementado em breve.", Alert.AlertType.INFORMATION);
        // abrirTela("/view/VendasView.fxml", "PDV / Frente de Caixa");
    }

    @FXML
    private void fazerLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnConfig.getScene().getWindow();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource(isDarkTheme ? "/view/login-styles.css" : "/view/login-styles-light.css").toExternalForm());
            
            stage.setTitle("EletroTech - Acesso Restrito");
            stage.setScene(scene);
        } catch (IOException e) {
            mostrarAlerta("Erro", "Erro ao carregar a tela de Login.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void sairApp() {
        Platform.exit();
    }

    private void abrirTela(String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Tenta passar o estado
            Object controller = loader.getController();
            if (controller instanceof controller.ProdutoController) {
                // ((controller.ProdutoController) controller).setUserData(currentUser, isDarkTheme);
            }
            
            Stage stage = (Stage) btnConfig.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.getStylesheets().add(getClass().getResource(isDarkTheme ? "/view/styles.css" : "/view/styles-light.css").toExternalForm());
            
            stage.setTitle("EletroTech - " + titulo);
            stage.setScene(scene);
        } catch (IOException e) {
            mostrarAlerta("Erro", "Falha ao abrir a tela: " + fxmlPath, Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
