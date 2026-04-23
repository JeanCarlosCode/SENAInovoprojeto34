package controller;

import dao.HistoricoDAO;
import model.HistoricoProduto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class HistoricoController implements Initializable {

    @FXML private TableView<HistoricoProduto> tabelaHistorico;
    @FXML private TableColumn<HistoricoProduto, String> colDataHora;
    @FXML private TableColumn<HistoricoProduto, String> colAcao;
    @FXML private TableColumn<HistoricoProduto, String> colProduto;
    @FXML private TableColumn<HistoricoProduto, String> colUsuario;
    @FXML private TableColumn<HistoricoProduto, String> colDetalhes;

    @FXML private ComboBox<String> cmbFiltroAcao;
    @FXML private TextField txtBuscaProduto;
    @FXML private Label lblTotalRegistros;

    private HistoricoDAO dao = new HistoricoDAO();
    private ObservableList<HistoricoProduto> listaCompleta;
    private FilteredList<HistoricoProduto> listaFiltrada;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar colunas
        colDataHora.setCellValueFactory(new PropertyValueFactory<>("dataHora"));
        colAcao.setCellValueFactory(new PropertyValueFactory<>("acao"));
        colProduto.setCellValueFactory(new PropertyValueFactory<>("produtoNome"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colDetalhes.setCellValueFactory(new PropertyValueFactory<>("detalhes"));

        // Badges coloridos para a coluna Ação
        colAcao.setCellFactory(column -> new TableCell<HistoricoProduto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-alignment: center;");
                    switch (item) {
                        case "CRIAÇÃO":
                            setStyle(getStyle() + "-fx-text-fill: #22C55E;");
                            break;
                        case "EDIÇÃO":
                            setStyle(getStyle() + "-fx-text-fill: #3B82F6;");
                            break;
                        case "EXCLUSÃO":
                            setStyle(getStyle() + "-fx-text-fill: #EF4444;");
                            break;
                        case "ENTRADA_ESTOQUE":
                            setStyle(getStyle() + "-fx-text-fill: #10B981;");
                            break;
                        case "SAÍDA_ESTOQUE":
                            setStyle(getStyle() + "-fx-text-fill: #F59E0B;");
                            break;
                        default:
                            setStyle(getStyle() + "-fx-text-fill: #94A3B8;");
                    }
                }
            }
        });

        // Filtros
        ObservableList<String> acoes = FXCollections.observableArrayList(
            "Todas", "CRIAÇÃO", "EDIÇÃO", "EXCLUSÃO", "ENTRADA_ESTOQUE", "SAÍDA_ESTOQUE"
        );
        cmbFiltroAcao.setItems(acoes);
        cmbFiltroAcao.getSelectionModel().selectFirst();

        // Listeners de filtro
        cmbFiltroAcao.valueProperty().addListener((obs, o, n) -> aplicarFiltros());
        txtBuscaProduto.textProperty().addListener((obs, o, n) -> aplicarFiltros());

        // Carregar dados
        carregarDados();
    }

    private void carregarDados() {
        listaCompleta = FXCollections.observableArrayList(dao.listarTodos());
        listaFiltrada = new FilteredList<>(listaCompleta, p -> true);
        tabelaHistorico.setItems(listaFiltrada);
        atualizarTotal();
    }

    private void aplicarFiltros() {
        String filtroAcao = cmbFiltroAcao.getValue();
        String buscaProduto = txtBuscaProduto.getText() == null ? "" : txtBuscaProduto.getText().toLowerCase().trim();

        listaFiltrada.setPredicate(h -> {
            boolean matchAcao = filtroAcao == null || filtroAcao.equals("Todas") || h.getAcao().equals(filtroAcao);
            boolean matchProduto = buscaProduto.isEmpty() || h.getProdutoNome().toLowerCase().contains(buscaProduto);
            return matchAcao && matchProduto;
        });
        atualizarTotal();
    }

    private void atualizarTotal() {
        lblTotalRegistros.setText("Registros: " + listaFiltrada.size());
    }

    @FXML
    private void atualizar() {
        carregarDados();
        aplicarFiltros();
    }
}
