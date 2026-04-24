package controller;

import dao.ClienteDAO;
import dao.ProdutoDAO;
import dao.VendaDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Cliente;
import model.ItemVenda;
import model.Produto;
import model.Venda;

import java.io.IOException;
import java.util.List;

public class VendasController {

    @FXML private Label lblUserName;
    @FXML private Button btnTheme;
    @FXML private MenuButton btnConfig;
    
    // Carrinho
    @FXML private TextField txtBuscaProduto;
    @FXML private TextField txtQuantidadeBase;
    @FXML private TableView<ItemVenda> tabelaCarrinho;
    @FXML private TableColumn<ItemVenda, String> colCodBarras;
    @FXML private TableColumn<ItemVenda, String> colNome;
    @FXML private TableColumn<ItemVenda, Integer> colQuantidade;
    @FXML private TableColumn<ItemVenda, Double> colPreco;
    @FXML private TableColumn<ItemVenda, Double> colSubtotal;
    @FXML private TableColumn<ItemVenda, Void> colAcao;

    // Checkout
    @FXML private ComboBox<String> cmbCliente;
    @FXML private Label lblSubtotalVal;
    @FXML private TextField txtDesconto;
    @FXML private Label lblTotalVal;
    
    // Pagamento
    @FXML private RadioButton rbDinheiro;
    @FXML private RadioButton rbCartao;
    @FXML private RadioButton rbPix;
    @FXML private HBox boxValorPago;
    @FXML private TextField txtValorRecebido;
    @FXML private Label lblTrocoVal;
    @FXML private Button btnFinalizar;

    private boolean isDarkTheme = true;
    private String currentUser = "Visitante";
    
    private ProdutoDAO produtoDAO = new ProdutoDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();
    private VendaDAO vendaDAO = new VendaDAO();
    
    private ObservableList<ItemVenda> itensCarrinho = FXCollections.observableArrayList();
    private List<Cliente> clientesAtivos;
    
    private ContextMenu popupBusca;
    
    private double subtotalGeral = 0.0;
    private double totalComDesconto = 0.0;
    private double descontoManual = 0.0;
    private double valorRecebido = 0.0;

    @FXML
    public void initialize() {
        ToggleGroup tgPagamento = new ToggleGroup();
        rbDinheiro.setToggleGroup(tgPagamento);
        rbCartao.setToggleGroup(tgPagamento);
        rbPix.setToggleGroup(tgPagamento);
        
        // Listener Formas Pagar
        tgPagamento.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            boolean isDinheiro = rbDinheiro.isSelected();
            boxValorPago.setVisible(isDinheiro);
            boxValorPago.setManaged(isDinheiro);
            if(!isDinheiro) {
                txtValorRecebido.setText(String.format(java.util.Locale.US, "%.2f", totalComDesconto));
            }
            calcularTotalETroco();
        });

        // Configurar Tabela Carrinho
        colCodBarras.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("precoUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        
        // Formatters de Moeda
        colPreco.setCellFactory(tc -> new TableCell<ItemVenda, Double>() {
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("R$ %.2f", item));
            }
        });
        colSubtotal.setCellFactory(tc -> new TableCell<ItemVenda, Double>() {
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("R$ %.2f", item));
            }
        });

        adicionarBotaoRemoverTabela();
        
        tabelaCarrinho.setItems(itensCarrinho);

        // Enter no campo de busca chama adicionarProduto
        txtBuscaProduto.setOnAction(e -> adicionarProdutoCarrinho());
        
        // Autocomplete/Live Search
        popupBusca = new ContextMenu();
        popupBusca.setMaxHeight(200);
        txtBuscaProduto.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().length() < 2) {
                popupBusca.hide();
                return;
            }
            List<Produto> enc = produtoDAO.buscarPorNome(newText.trim());
            popupBusca.getItems().clear();
            for (Produto p : enc) {
                MenuItem item = new MenuItem(p.getCodigoBarras() + " - " + p.getNome() + " (R$ " + String.format("%.2f", p.getPrecoVenda()) + ") - Est: " + p.getQuantidade());
                item.setOnAction(ev -> {
                    txtBuscaProduto.setText(p.getCodigoBarras());
                    adicionarProdutoCarrinho();
                });
                popupBusca.getItems().add(item);
            }
            
            if (!popupBusca.getItems().isEmpty()) {
                if (!popupBusca.isShowing()) {
                    popupBusca.show(txtBuscaProduto, javafx.geometry.Side.BOTTOM, 0, 0);
                }
            } else {
                popupBusca.hide();
            }
        });
        
        // Desconto mudou
        txtDesconto.textProperty().addListener((o, oldV, newV) -> calcularTotalETroco());
        // Recebido mudou
        txtValorRecebido.textProperty().addListener((o, oldV, newV) -> calcularTotalETroco());

        carregarClientes();
    }
    
    private void adicionarBotaoRemoverTabela() {
        colAcao.setCellFactory(param -> new TableCell<ItemVenda, Void>() {
            private final Button btn = new Button("X");
            {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-cursor: hand;");
                btn.setOnAction(event -> {
                    ItemVenda data = getTableView().getItems().get(getIndex());
                    itensCarrinho.remove(data);
                    calcularTotalETroco();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    public void setUserData(String username, boolean darkTheme) {
        this.currentUser = username;
        this.lblUserName.setText(username != null && !username.isEmpty() ? username : "Visitante");
        this.isDarkTheme = darkTheme;
        if (btnTheme != null) btnTheme.setText(isDarkTheme ? "\u2600 Light" : "\uD83C\uDF19 Dark");
    }

    private void carregarClientes() {
        clientesAtivos = clienteDAO.listarTodos();
        clientesAtivos.removeIf(c -> !c.isAtivo()); // Só mostra ativos
        
        ObservableList<String> nomes = FXCollections.observableArrayList("Consumidor Final");
        for (Cliente c : clientesAtivos) {
            nomes.add(c.getNome() + " (" + c.getDocumento() + ")");
        }
        cmbCliente.setItems(nomes);
        cmbCliente.getSelectionModel().selectFirst();
    }

    @FXML
    private void adicionarProdutoCarrinho() {
        String busca = txtBuscaProduto.getText().trim();
        if (busca.isEmpty()) return;
        
        int qtd = 1;
        try {
            qtd = Integer.parseInt(txtQuantidadeBase.getText().trim());
            if (qtd <= 0) qtd = 1;
        } catch (Exception e) {}

        List<Produto> enc = produtoDAO.buscarPorNome(busca); // No meu DAO, buscarPorNome busca nome ou CodBarras
        if (!enc.isEmpty()) {
            Produto p = enc.get(0);
            
            // RN02 (Bloqueio sem estoque) validado previamente na UI
            if (p.getQuantidade() < qtd) {
                mostrarAviso("Estoque Insuficiente", "O produto " + p.getNome() + " possui apenas " + p.getQuantidade() + " em estoque.");
                return;
            }
            
            // Check if already in cart
            boolean alreadyInCart = false;
            for (ItemVenda i : itensCarrinho) {
                if (i.getProdutoId() == p.getId()) {
                    if (p.getQuantidade() < (i.getQuantidade() + qtd)) {
                        mostrarAviso("Estoque Insuficiente", "Se adicionar mais, passará do limite de estoque!");
                        return;
                    }
                    i.setQuantidade(i.getQuantidade() + qtd);
                    alreadyInCart = true;
                    // Trigger refresh
                    tabelaCarrinho.refresh();
                    break;
                }
            }
            if(!alreadyInCart) {
                ItemVenda iv = new ItemVenda(p, qtd);
                itensCarrinho.add(iv);
            }

            txtBuscaProduto.clear();
            txtQuantidadeBase.setText("1");
            txtBuscaProduto.requestFocus();
            if (popupBusca != null && popupBusca.isShowing()) {
                popupBusca.hide();
            }
            calcularTotalETroco();
        } else {
            mostrarAviso("Não Encontrado", "Produto não encontrado.");
        }
    }

    private void calcularTotalETroco() {
        subtotalGeral = 0;
        for (ItemVenda iv : itensCarrinho) {
            subtotalGeral += iv.getSubtotal();
        }
        
        descontoManual = 0;
        try {
            if(!txtDesconto.getText().isEmpty()) {
                descontoManual = Double.parseDouble(txtDesconto.getText().replace(",","."));
            }
        } catch (Exception e){}
        
        // Regra Desconto Limitado 5% ou Gerencial
        if (descontoManual > 5.0) {
            // Em tese pediria senha de gerente, aqui vamos apenas bloquear se não for "admin" local
            if (!"admin".equalsIgnoreCase(currentUser)) {
                mostrarAviso("Bloqueio de Desconto", "Apenas Gerentes conseguem aplicar desconto maior que 5%. Reduzindo para 5%.");
                txtDesconto.setText("5");
                descontoManual = 5.0;
            }
        }

        double valDesconto = subtotalGeral * (descontoManual / 100.0);
        totalComDesconto = subtotalGeral - valDesconto;
        if (totalComDesconto < 0) totalComDesconto = 0;

        valorRecebido = totalComDesconto;
        if (rbDinheiro.isSelected()) {
            try {
                if (!txtValorRecebido.getText().isEmpty()) {
                    valorRecebido = Double.parseDouble(txtValorRecebido.getText().replace(",","."));
                } else {
                    valorRecebido = 0;
                }
            } catch (Exception e){}
        }

        double troco = valorRecebido - totalComDesconto;

        lblSubtotalVal.setText(String.format("R$ %.2f", subtotalGeral));
        lblTotalVal.setText(String.format("R$ %.2f", totalComDesconto));
        
        if (troco < 0) {
            lblTrocoVal.setText("Falta: " + String.format("R$ %.2f", Math.abs(troco)));
            lblTrocoVal.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #EF4444;");
            btnFinalizar.setDisable(true);
        } else {
            lblTrocoVal.setText(String.format("R$ %.2f", troco));
            lblTrocoVal.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #10B981;");
            btnFinalizar.setDisable(itensCarrinho.isEmpty());
        }
    }

    @FXML
    private void finalizarVenda() {
        if (itensCarrinho.isEmpty()) return;
        if (totalComDesconto > 0 && valorRecebido < totalComDesconto) {
            mostrarAviso("Bloqueio", "O valor pago é menor que o total da venda!");
            return;
        }

        Venda v = new Venda();
        v.setTotal(subtotalGeral);
        v.setDesconto(subtotalGeral * (descontoManual / 100.0));
        v.setTotalPago(valorRecebido);
        v.setTroco(valorRecebido - totalComDesconto);
        v.setFormaPagamento(rbDinheiro.isSelected() ? "Dinheiro" : (rbCartao.isSelected() ? "Cartão" : "PIX"));

        int clienteId = -1;
        int selectedIndex = cmbCliente.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            clienteId = clientesAtivos.get(selectedIndex - 1).getId();
            v.setClienteId(clienteId);
        }

        try {
            boolean success = vendaDAO.gravarVenda(v, itensCarrinho, currentUser);
            if (success) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Sucesso!");
                ok.setHeaderText("VENDA #" + v.getId() + " FINALIZADA!");
                ok.setContentText("Total: R$ " + String.format("%.2f", totalComDesconto) + 
                                  "\nRecebido: R$ " + String.format("%.2f", valorRecebido) + 
                                  "\nTroco: R$ " + String.format("%.2f", v.getTroco()) +
                                  "\n\nGerando cupom...");
                ok.showAndWait();
                limparTudo();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAviso("Erro na Gravação", "Ocorreu um erro ao faturar a venda:\n" + e.getMessage());
        }
    }

    @FXML
    private void limparTudo() {
        itensCarrinho.clear();
        cmbCliente.getSelectionModel().selectFirst();
        txtBuscaProduto.clear();
        txtQuantidadeBase.setText("1");
        txtDesconto.setText("0");
        rbDinheiro.setSelected(true);
        txtValorRecebido.clear();
        calcularTotalETroco();
    }
    
    // NAV
    @FXML
    public void alternarTema(ActionEvent event) {
        isDarkTheme = !isDarkTheme;
        Scene scene = btnTheme.getScene();
        if (scene != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource(isDarkTheme ? "/view/styles.css" : "/view/styles-light.css").toExternalForm());
        }
        btnTheme.setText(isDarkTheme ? "\u2600 Light" : "\uD83C\uDF19 Dark");
    }

    @FXML private void abrirProdutos() { abrirTela("/view/ProdutoView.fxml", "Cadastro de Produtos"); }
    @FXML private void abrirClientes() { abrirTela("/view/ClienteView.fxml", "Cadastro de Clientes"); }
    
    @FXML
    private void fazerLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnConfig.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource(isDarkTheme ? "/view/login-styles.css" : "/view/login-styles-light.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void sairApp() { Platform.exit(); }

    private void abrirTela(String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Object controller = loader.getController();
            
            // Tratamento simplificado
            if (controller instanceof controller.ProdutoController) {
                // ((controller.ProdutoController) controller).setUserData(currentUser, isDarkTheme);
            } else if (controller instanceof controller.ClienteController) {
                // ((controller.ClienteController) controller).setUserData(currentUser, isDarkTheme);
            }

            Stage stage = (Stage) btnConfig.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            scene.getStylesheets().add(getClass().getResource(isDarkTheme ? "/view/styles.css" : "/view/styles-light.css").toExternalForm());
            stage.setTitle("EletroTech - " + titulo);
            stage.setScene(scene);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void mostrarAviso(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
