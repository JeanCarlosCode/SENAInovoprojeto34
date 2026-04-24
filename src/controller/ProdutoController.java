package controller;

import dao.ProdutoDAO;
import dao.HistoricoDAO;
import model.Produto;
import model.HistoricoProduto;
import model.Usuario;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.List;
import java.util.prefs.Preferences;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class ProdutoController implements Initializable {

    // ==================== PAINÉIS PARA ANIMAÇÃO ====================
    @FXML private BorderPane headerPane;
    @FXML private VBox formPane;
    @FXML private VBox tabelaPane;

    // ==================== CAMPOS DO FORMULÁRIO ====================
    @FXML private TextField txtNome;
    @FXML private TextArea txtDescricao;
    @FXML private TextField txtPrecoCusto;
    @FXML private TextField txtMargemLucro;
    @FXML private TextField txtPrecoVenda;
    @FXML private TextField txtEstoqueMinimo;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtCodigoBarras;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> cmbFiltroCategoria;

    // ==================== BOTÕES PARA ANIMAÇÃO ====================
    @FXML private Button btnSalvar;
    @FXML private Button btnExcluir;
    @FXML private Button btnLimpar;
    @FXML private Button btnBuscar;
    
    // ==================== LABEL LOGO ====================
    @FXML private Label lblLogo;

    // ==================== USUÁRIO LOGADO ====================
    @FXML private StackPane avatarNavContainer;
    @FXML private Label lblUserName;
    
    // ==================== TOGGLE TEMA ====================
    @FXML private Button btnTheme;
    @FXML private MenuButton btnConfig;
    @FXML private MenuItem menuItemSair;
    private boolean isDarkMode = true;
    private Preferences prefs = Preferences.userNodeForPackage(LoginController.class);

    // ==================== TABELA ====================
    @FXML private TableView<Produto> tabelaProdutos;
    @FXML private TableColumn<Produto, Integer> colId;
    @FXML private TableColumn<Produto, String> colCodigoBarras;
    @FXML private TableColumn<Produto, String> colNome;
    @FXML private TableColumn<Produto, String> colDescricao;
    @FXML private TableColumn<Produto, Double> colPreco;
    @FXML private TableColumn<Produto, Integer> colQuantidade;
    @FXML private TableColumn<Produto, String> colCategoria;

    // ==================== LABELS DE STATUS ====================
    @FXML private Label lblTotal;

    // ==================== ATRIBUTOS ====================
    private ProdutoDAO dao = new ProdutoDAO();
    private HistoricoDAO historicoDAO = new HistoricoDAO();
    private ObservableList<Produto> listaProdutos;
    private Produto produtoSelecionado = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar colunas da tabela
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigoBarras.setCellValueFactory(new PropertyValueFactory<>("codigoBarras"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("precoVenda"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        // Formatar coluna de ID (6 dígitos)
        colId.setCellFactory(column -> new TableCell<Produto, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%06d", item));
                }
            }
        });

        // Formatar coluna de preço (Locale pt-BR)
        colPreco.setCellFactory(column -> new TableCell<Produto, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format(new java.util.Locale("pt", "BR"), "R$ %,.2f", item));
                }
            }
        });

        // ==================== ALERTA DE ESTOQUE BAIXO (ROW FACTORY) ====================
        tabelaProdutos.setRowFactory(tv -> new TableRow<Produto>() {
            @Override
            protected void updateItem(Produto item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                    getStyleClass().remove("linha-estoque-baixo");
                } else {
                    if (item.getQuantidade() < item.getEstoqueMinimo()) {
                        if (!getStyleClass().contains("linha-estoque-baixo")) {
                            getStyleClass().add("linha-estoque-baixo");
                        }
                    } else {
                        getStyleClass().remove("linha-estoque-baixo");
                    }
                }
            }
        });

        // Configurar categorias (Formulário e Filtro)
        ObservableList<String> categorias = FXCollections.observableArrayList(
            "Eletrônicos", "Periféricos", "Móveis", "Acessórios",
            "Software", "Roupas", "Alimentos", "Outros"
        );
        cmbCategoria.setItems(categorias);

        ObservableList<String> categoriasFiltro = FXCollections.observableArrayList("Todas Categorias");
        categoriasFiltro.addAll(categorias);
        cmbFiltroCategoria.setItems(categoriasFiltro);
        cmbFiltroCategoria.getSelectionModel().selectFirst();

        // Listener para seleção na tabela
        tabelaProdutos.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    produtoSelecionado = newSelection;
                    preencherFormulario(newSelection);
                }
            }
        );

        // Listeners para Precificação Automática
        txtPrecoCusto.textProperty().addListener((obs, oldVal, newVal) -> calcularPrecificacaoAutomatica());
        txtMargemLucro.textProperty().addListener((obs, oldVal, newVal) -> calcularPrecificacaoAutomatica());

        // Listener para Pequisa Automática (Live Search)
        txtBusca.textProperty().addListener((obs, oldText, newText) -> {
            buscar();
        });

        // Listener para mudança imediata de Categoria
        cmbFiltroCategoria.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Verifica se a interface já terminou de carregar o valor inicial
            if (newVal != null) buscar();
        });

        // Carregar dados
        carregarTabela();
        
        // Sincronizar Preferências de Tema
        isDarkMode = prefs.getBoolean("temaDark", true);
        javafx.application.Platform.runLater(() -> {
            aplicarTemaAtual();
            estilizarMenuSair();
        });

        // Animação inicial de 1.5s
        iniciarAnimacoes();
    }

    /** Garante a estilização vermelha do item Sair, independente do CSS */
    private void estilizarMenuSair() {
        if (menuItemSair != null) {
            menuItemSair.setStyle("-fx-text-fill: " + (isDarkMode ? "#F87171" : "#DC2626") + "; -fx-font-weight: 700;");
        }
    }

    // ==================== ANIMAÇÕES WOW ====================
    private void iniciarAnimacoes() {
        // 1. Animar Entrada dos Painéis (Slide + Fade)
        if (headerPane != null) animarEntrada(headerPane, 100, "DOWN");
        if (formPane != null) animarEntrada(formPane, 300, "LEFT");
        if (tabelaPane != null) animarEntrada(tabelaPane, 500, "RIGHT");

        // 2. Aplicar efeito emborrachado de Hover nos botões
        if (btnSalvar != null) aplicarEfeitoHover(btnSalvar, 1.05);
        if (btnExcluir != null) aplicarEfeitoHover(btnExcluir, 1.05);
        if (btnLimpar != null) aplicarEfeitoHover(btnLimpar, 1.05);
        if (btnBuscar != null) aplicarEfeitoHover(btnBuscar, 1.05);
        
        // 3. Efeito de Respiração (Glow/Pulse) no Logo SENAI
        if (lblLogo != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(1500), lblLogo);
            ft.setFromValue(1.0);
            ft.setToValue(0.6);
            ft.setCycleCount(javafx.animation.Animation.INDEFINITE);
            ft.setAutoReverse(true);
            ft.play();
        }
    }

    private void animarEntrada(Node no, double delayMs, String direcao) {
        no.setOpacity(0);
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(600), no);
        if (direcao.equals("LEFT")) {
            no.setTranslateX(-50);
            tt.setToX(0);
        } else if (direcao.equals("RIGHT")) {
            no.setTranslateX(50);
            tt.setToX(0);
        } else if (direcao.equals("DOWN")) {
            no.setTranslateY(-30);
            tt.setToY(0);
        }
        
        FadeTransition ft = new FadeTransition(Duration.millis(600), no);
        ft.setFromValue(0);
        ft.setToValue(1);

        tt.setDelay(Duration.millis(delayMs));
        ft.setDelay(Duration.millis(delayMs));

        tt.play();
        ft.play();
    }

    private void aplicarEfeitoHover(Node no, double scalaMapeada) {
        ScaleTransition stIn = new ScaleTransition(Duration.millis(150), no);
        stIn.setToX(scalaMapeada);
        stIn.setToY(scalaMapeada);

        ScaleTransition stOut = new ScaleTransition(Duration.millis(150), no);
        stOut.setToX(1.0);
        stOut.setToY(1.0);

        no.setOnMouseEntered(e -> stIn.playFromStart());
        no.setOnMouseExited(e -> stOut.playFromStart());
    }

    // ==================== DADOS DO USUÁRIO ====================
    public void setUsuarioLogado(Usuario paramUsuario) {
        if (paramUsuario == null) return;
        
        String[] nomes = paramUsuario.getNome().split(" ");
        lblUserName.setText(nomes[0] + (nomes.length > 1 ? " " + nomes[nomes.length - 1] : ""));

        if (paramUsuario.getFotoPath() != null && !paramUsuario.getFotoPath().isEmpty()) {
            try {
                java.io.File file = new java.io.File(paramUsuario.getFotoPath());
                if (file.exists()) {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(file.toURI().toString(), 64, 64, true, true);
                    javafx.scene.shape.Circle fotoCircle = new javafx.scene.shape.Circle(30);
                    fotoCircle.setFill(new javafx.scene.paint.ImagePattern(img));
                    avatarNavContainer.getChildren().clear();
                    avatarNavContainer.getChildren().add(fotoCircle);
                }
            } catch (Exception e) {}
        }
    }

    // ==================== ALTERNÂNCIA DE TEMA ====================
    @FXML
    private void alternarTema() {
        isDarkMode = !isDarkMode;
        prefs.putBoolean("temaDark", isDarkMode);
        aplicarTemaAtual();
        estilizarMenuSair();
    }

    private void aplicarTemaAtual() {
        Scene scene = btnTheme.getScene();
        if (scene == null) return;
        
        scene.getStylesheets().clear();
        
        if (isDarkMode) {
            scene.getStylesheets().add(getClass().getResource("/view/styles.css").toExternalForm());
            btnTheme.setText("\u2600 Light");
        } else {
            scene.getStylesheets().add(getClass().getResource("/view/styles-light.css").toExternalForm());
            btnTheme.setText("\uD83C\uDF19 Dark");
        }
    }
    
    // ==================== CONFIGURAÇÕES DO USUÁRIO ====================
    @FXML
    private void abrirPerfil() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(btnTheme.getScene().getWindow());
        alert.setTitle("Meu Perfil");
        alert.setHeaderText("Funcionalidade Reservada");
        alert.setContentText("A integração com fotos e edição de perfil será liberada de acordo com o plano do banco de dados.");
        alert.show();
    }

    @FXML
    private void fazerLogout() {
        try {
            // Fechar a janela atual (maximizada) completamente
            Stage currentStage = (Stage) btnTheme.getScene().getWindow();
            currentStage.close();

            // Abrir uma janela limpa para o login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            Scene scene = new Scene(root, 1100, 650);
            loginStage.setScene(scene);
            loginStage.setTitle("SENAI — Login");
            loginStage.setResizable(false);
            loginStage.show();
            loginStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erro de Rotas");
            alert.setContentText("Não foi possível carregar a tela de Login.");
            alert.show();
        }
    }
    
    @FXML
    private void sairApp() {
        javafx.application.Platform.exit();
    }

    @FXML
    private void abrirClientes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ClienteView.fxml"));
            Parent root = loader.load();

            // Tenta passar o estado
            Object controller = loader.getController();
            if (controller instanceof controller.ClienteController) {
                ((controller.ClienteController) controller).setUserData(lblUserName.getText(), isDarkMode);
            }

            Stage stage = (Stage) btnTheme.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            if (isDarkMode) {
                scene.getStylesheets().add(getClass().getResource("/view/styles.css").toExternalForm());
            } else {
                scene.getStylesheets().add(getClass().getResource("/view/styles-light.css").toExternalForm());
            }
            stage.setTitle("EletroTech - Cadastro de Clientes");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erro");
            alert.setContentText("Falha ao abrir a tela.");
            alert.show();
        }
    }

    @FXML
    private void abrirVendas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/VendasView.fxml"));
            Parent root = loader.load();

            // Tenta passar o estado
            Object controller = loader.getController();
            if (controller instanceof controller.VendasController) {
                ((controller.VendasController) controller).setUserData(lblUserName.getText(), isDarkMode);
            }

            Stage stage = (Stage) btnTheme.getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            if (isDarkMode) {
                scene.getStylesheets().add(getClass().getResource("/view/styles.css").toExternalForm());
            } else {
                scene.getStylesheets().add(getClass().getResource("/view/styles-light.css").toExternalForm());
            }
            stage.setTitle("EletroTech - Frente de Caixa / PDV");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erro");
            alert.setContentText("Falha ao abrir a tela.");
            alert.show();
        }
    }


    // ==================== CARREGAR TABELA E TOTAL ====================
    private void carregarTabela() {
        listaProdutos = FXCollections.observableArrayList(dao.listarTodos());
        tabelaProdutos.setItems(listaProdutos);
        atualizarTotal();
    }

    private void atualizarTotal() {
        if (listaProdutos == null) return;
        
        int totalItens = listaProdutos.size();
        double valorTotal = 0.0;
        
        for (Produto p : listaProdutos) {
            valorTotal += (p.getPrecoVenda() * p.getQuantidade());
        }
        
        lblTotal.setText(String.format("Itens visíveis: %d   \u2022   Valor em Estoque: R$ %,.2f", totalItens, valorTotal));
    }

    // ==================== PREENCHER FORMULÁRIO ====================
    private void preencherFormulario(Produto p) {
        txtNome.setText(p.getNome());
        txtDescricao.setText(p.getDescricao());
        txtPrecoCusto.setText(String.format(new java.util.Locale("pt", "BR"), "%.2f", p.getPrecoCusto()));
        txtPrecoVenda.setText(String.format(new java.util.Locale("pt", "BR"), "%.2f", p.getPrecoVenda()));
        txtEstoqueMinimo.setText(String.valueOf(p.getEstoqueMinimo()));
        txtMargemLucro.setText(""); // Resetar na carga
        txtQuantidade.setText(String.valueOf(p.getQuantidade()));
        cmbCategoria.setValue(p.getCategoria());
        txtCodigoBarras.setText(p.getCodigoBarras());
    }

    // ==================== HELPER: DETALHES DE EDIÇÃO ====================
    private String gerarDetalhesEdicao(Produto antes, Produto depois) {
        StringBuilder sb = new StringBuilder();
        if (!antes.getNome().equals(depois.getNome()))
            sb.append("Nome: ").append(antes.getNome()).append(" → ").append(depois.getNome()).append("; ");
        if (antes.getPrecoVenda() != depois.getPrecoVenda())
            sb.append("Preço: R$").append(String.format("%.2f", antes.getPrecoVenda())).append(" → R$").append(String.format("%.2f", depois.getPrecoVenda())).append("; ");
        if (antes.getPrecoCusto() != depois.getPrecoCusto())
            sb.append("Custo: R$").append(String.format("%.2f", antes.getPrecoCusto())).append(" → R$").append(String.format("%.2f", depois.getPrecoCusto())).append("; ");
        if (!antes.getCategoria().equals(depois.getCategoria()))
            sb.append("Categoria: ").append(antes.getCategoria()).append(" → ").append(depois.getCategoria()).append("; ");
        if (antes.getEstoqueMinimo() != depois.getEstoqueMinimo())
            sb.append("Est. Mínimo: ").append(antes.getEstoqueMinimo()).append(" → ").append(depois.getEstoqueMinimo()).append("; ");
        if (sb.length() == 0) sb.append("Alterações gerais aplicadas");
        return sb.toString().trim();
    }

    // ==================== ABRIR HISTÓRICO ====================
    @FXML
    private void abrirHistorico() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HistoricoView.fxml"));
            Parent root = loader.load();

            Stage historicoStage = new Stage();
            Scene scene = new Scene(root, 820, 520);

            // Aplicar mesmo tema
            if (isDarkMode) {
                scene.getStylesheets().add(getClass().getResource("/view/styles.css").toExternalForm());
            } else {
                scene.getStylesheets().add(getClass().getResource("/view/styles-light.css").toExternalForm());
            }

            historicoStage.setScene(scene);
            historicoStage.setTitle("Histórico de Atividades");
            historicoStage.initOwner(btnTheme.getScene().getWindow());
            historicoStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            historicoStage.setResizable(true);
            historicoStage.show();
            historicoStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(btnTheme.getScene().getWindow());
            alert.setTitle("Erro");
            alert.setHeaderText("Falha ao Abrir Histórico");
            alert.setContentText("Não foi possível carregar a tela de histórico.");
            alert.show();
        }
    }

    // ==================== AÇÕES DOS BOTÕES ====================

    @FXML
    private void salvar() {
        if (!validarCampos()) return;

        try {
            if (produtoSelecionado == null) {
                // INSERIR novo produto
                Produto novo = criarProdutoDoFormulario();
                if (dao.inserir(novo)) {
                    exibirStatus("✅ Produto adicionado com sucesso!", "sucesso");
                    historicoDAO.registrar(new HistoricoProduto(
                        "CRIAÇÃO", 0, novo.getNome(),
                        lblUserName.getText(),
                        "Produto criado — Categoria: " + novo.getCategoria() + ", Preço: R$" + String.format("%.2f", novo.getPrecoVenda())
                    ));
                } else {
                    exibirStatus("❌ Erro ao adicionar produto!", "erro");
                }
            } else {
                // ATUALIZAR produto existente
                String detalhesEdicao = gerarDetalhesEdicao(produtoSelecionado, criarProdutoDoFormulario());
                Produto atualizado = criarProdutoDoFormulario();
                atualizado.setId(produtoSelecionado.getId());
                if (dao.atualizar(atualizado)) {
                    exibirStatus("✅ Produto atualizado com sucesso!", "sucesso");
                    historicoDAO.registrar(new HistoricoProduto(
                        "EDIÇÃO", atualizado.getId(), atualizado.getNome(),
                        lblUserName.getText(),
                        detalhesEdicao
                    ));
                } else {
                    exibirStatus("❌ Erro ao atualizar produto!", "erro");
                }
            }
            buscar(); // Recarrega aplicando os filtros atuais
            limpar();
        } catch (NumberFormatException e) {
            exibirStatus("❌ Preço ou quantidade inválidos!", "erro");
        }
    }

    @FXML
    private void excluir() {
        if (produtoSelecionado == null) {
            exibirStatus("⚠️ Selecione um produto na tabela para excluir!", "aviso");
            return;
        }

        // Confirmação
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(tabelaProdutos.getScene().getWindow());
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Excluir produto: " + produtoSelecionado.getNome());
        alert.setContentText("Tem certeza que deseja excluir este produto?");

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (dao.deletar(produtoSelecionado.getId())) {
                exibirStatus("🗑️ Produto excluído com sucesso!", "sucesso");
                historicoDAO.registrar(new HistoricoProduto(
                    "EXCLUSÃO", produtoSelecionado.getId(), produtoSelecionado.getNome(),
                    lblUserName.getText(),
                    "Produto removido do sistema"
                ));
                buscar(); // Recarrega aplicando filtros
                limpar();
            } else {
                exibirStatus("❌ Erro ao excluir produto!", "erro");
            }
        }
    }

    @FXML
    private void abrirProcessamentoEstoque() {
        if (produtoSelecionado == null) {
            exibirStatus("⚠️ Selecione um produto na tabela para processar estoque!", "aviso");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(tabelaProdutos.getScene().getWindow());
            alert.setTitle("Aviso");
            alert.setHeaderText("Nenhum Produto Selecionado");
            alert.setContentText("Por favor, selecione um produto na tabela primeiro.");
            alert.showAndWait();
            return;
        }

        TextInputDialog dialog = new TextInputDialog("0");
        dialog.initOwner(tabelaProdutos.getScene().getWindow());
        dialog.setTitle("Processar Estoque");
        dialog.setHeaderText("Produto: " + produtoSelecionado.getNome() + "\nEstoque Atual: " + produtoSelecionado.getQuantidade());
        dialog.setContentText("Digite a quantidade a processar\n(Use positivo para ENTRADA e negativo para SAÍDA):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int qtdProcessar = Integer.parseInt(result.get());
                if (qtdProcessar == 0) return;

                int novoEstoque = produtoSelecionado.getQuantidade() + qtdProcessar;
                if (novoEstoque < 0) {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.initOwner(tabelaProdutos.getScene().getWindow());
                    err.setTitle("Erro");
                    err.setHeaderText("Estoque Insuficiente");
                    err.setContentText("A saída de " + Math.abs(qtdProcessar) + " é maior que o estoque em tela (" + produtoSelecionado.getQuantidade() + ").");
                    err.showAndWait();
                    return;
                }

                int qtdAnterior = produtoSelecionado.getQuantidade();
                produtoSelecionado.setQuantidade(novoEstoque);
                if (dao.atualizar(produtoSelecionado)) {
                    exibirStatus("✅ Estoque processado com sucesso!", "sucesso");
                    String tipoAcao = qtdProcessar > 0 ? "ENTRADA_ESTOQUE" : "SAÍDA_ESTOQUE";
                    historicoDAO.registrar(new HistoricoProduto(
                        tipoAcao, produtoSelecionado.getId(), produtoSelecionado.getNome(),
                        lblUserName.getText(),
                        "Estoque: " + qtdAnterior + " → " + novoEstoque + " (" + (qtdProcessar > 0 ? "+" : "") + qtdProcessar + ")"
                    ));
                    buscar();
                    preencherFormulario(produtoSelecionado); // atualiza na interface
                } else {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.initOwner(tabelaProdutos.getScene().getWindow());
                    err.setTitle("Erro");
                    err.setHeaderText("Falha ao Salvar");
                    err.setContentText("Erro ao atualizar o estoque no banco de dados.");
                    err.showAndWait();
                }
            } catch (NumberFormatException e) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.initOwner(tabelaProdutos.getScene().getWindow());
                err.setTitle("Erro");
                err.setHeaderText("Valor Inválido");
                err.setContentText("Por favor, digite um número inteiro.");
                err.showAndWait();
            }
        }
    }

    @FXML
    private void limpar() {
        txtNome.clear();
        txtDescricao.clear();
        txtPrecoCusto.clear();
        txtPrecoVenda.clear();
        txtMargemLucro.clear();
        txtEstoqueMinimo.setText("5");
        txtQuantidade.setText("0"); // Campo desabilitado
        cmbCategoria.setValue(null);
        txtCodigoBarras.clear();
        produtoSelecionado = null;
        tabelaProdutos.getSelectionModel().clearSelection();
        txtNome.requestFocus();
    }

    @FXML
    private void buscar() {
        String termo = txtBusca.getText().trim();
        String categoria = cmbFiltroCategoria.getValue();
        
        // 1. Busca todos no banco (ou pelo nome se preenchido)
        List<Produto> resultadoBruto;
        if (termo.isEmpty()) {
            resultadoBruto = dao.listarTodos();
        } else {
            resultadoBruto = dao.buscarPorNome(termo);
        }
        
        // 2. Filtra por Categoria em memória usando API Streams
        if (categoria != null && !categoria.equals("Todas Categorias")) {
            resultadoBruto = resultadoBruto.stream()
                .filter(p -> p.getCategoria() != null && p.getCategoria().equals(categoria))
                .collect(java.util.stream.Collectors.toList());
        }
        
        listaProdutos = FXCollections.observableArrayList(resultadoBruto);
        tabelaProdutos.setItems(listaProdutos);
        atualizarTotal();
        
        if (listaProdutos.isEmpty()) {
            exibirStatus("🔍 Nenhum produto encontrado nos filtros.", "aviso");
        } else {
            exibirStatus("🔍 Busca Concluída.", "info");
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void calcularPrecificacaoAutomatica() {
        try {
            if (txtPrecoCusto.getText().trim().isEmpty() || txtMargemLucro.getText().trim().isEmpty()) {
                return;
            }
            double custo = Double.parseDouble(txtPrecoCusto.getText().trim().replace(",", "."));
            double margem = Double.parseDouble(txtMargemLucro.getText().trim().replace("%", "").replace(",", "."));
            double venda = custo + (custo * (margem / 100));
            txtPrecoVenda.setText(String.format(java.util.Locale.US, "%.2f", venda));
        } catch (NumberFormatException e) {
            // Ignorar erro de digitação interativa
        }
    }

    private Produto criarProdutoDoFormulario() {
        Produto p = new Produto();
        p.setNome(txtNome.getText().trim());
        p.setDescricao(txtDescricao.getText().trim());
        p.setPrecoCusto(Double.parseDouble(txtPrecoCusto.getText().trim().replace(",", ".")));
        p.setPrecoVenda(Double.parseDouble(txtPrecoVenda.getText().trim().replace(",", ".")));
        p.setEstoqueMinimo(Integer.parseInt(txtEstoqueMinimo.getText().trim()));
        
        // Quantidade é desativada, puxamos do produtoSelecionado ou é 0 para novos
        if (produtoSelecionado != null) {
            p.setQuantidade(produtoSelecionado.getQuantidade());
        } else {
            p.setQuantidade(0);
        }
        
        p.setCategoria(cmbCategoria.getValue());
        p.setCodigoBarras(txtCodigoBarras.getText().trim());
        return p;
    }

    private boolean validarCampos() {
        StringBuilder erros = new StringBuilder();

        if (txtNome.getText().trim().isEmpty()) {
            erros.append("• Nome é obrigatório\n");
        }
        if (txtPrecoCusto.getText().trim().isEmpty()) {
            erros.append("• Preço de Custo é obrigatório\n");
        } else {
            try {
                double preco = Double.parseDouble(txtPrecoCusto.getText().trim().replace(",", "."));
                if (preco < 0) erros.append("• Preço de Custo não pode ser negativo\n");
            } catch (NumberFormatException e) {
                erros.append("• Preço de Custo inválido (use números)\n");
            }
        }
        if (txtPrecoVenda.getText().trim().isEmpty()) {
            erros.append("• Preço de Venda é obrigatório\n");
        } else {
            try {
                double preco = Double.parseDouble(txtPrecoVenda.getText().trim().replace(",", "."));
                if (preco < 0) erros.append("• Preço de Venda não pode ser negativo\n");
            } catch (NumberFormatException e) {
                erros.append("• Preço de Venda inválido (use números)\n");
            }
        }
        if (txtEstoqueMinimo.getText().trim().isEmpty()) {
            erros.append("• Estoque Mínimo é obrigatório\n");
        } else {
            try {
                int em = Integer.parseInt(txtEstoqueMinimo.getText().trim());
                if (em < 0) erros.append("• Estoque Mínimo não pode ser negativo\n");
            } catch (NumberFormatException e) {
                erros.append("• Estoque Mínimo inválido (use inteiros)\n");
            }
        }
        if (txtQuantidade.getText().trim().isEmpty()) {
            // Em caso do campo estar nulo ou apagado manual, não erraremos, pois é automático.
            txtQuantidade.setText("0");
        } else {
            try {
                int qtd = Integer.parseInt(txtQuantidade.getText().trim());
                if (qtd < 0) erros.append("• Quantidade não pode ser negativa\n");
            } catch (NumberFormatException e) {
                erros.append("• Quantidade inválida (use números inteiros)\n");
            }
        }
        if (cmbCategoria.getValue() == null) {
            erros.append("• Selecione uma categoria\n");
        }

        if (erros.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(txtNome.getScene().getWindow());
            alert.setTitle("Campos Inválidos");
            alert.setHeaderText("Corrija os seguintes campos:");
            alert.setContentText(erros.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private void exibirStatus(String mensagem, String tipo) {
        // Status updates foram removidos da UI conforme solicitado,
        // então esse método agora não faz nada, mantendo as chamadas seguras.
    }
}
