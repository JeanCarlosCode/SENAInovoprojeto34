package controller;

import dao.UsuarioDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.application.Platform;
import java.util.prefs.Preferences;
import model.Usuario;

import java.io.File;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtSenha;
    @FXML private TextField txtNome;
    @FXML private PasswordField txtConfirmarSenha;
    @FXML private Label lblMensagemErro;
    @FXML private Label lblMensagemSucesso;
    @FXML private Label lblLoginTitle;
    @FXML private Label lblLoginSub;
    @FXML private SVGPath svgAvatarIcon;
    @FXML private Label lblFotoHint;
    @FXML private Button btnAcessar;
    @FXML private Hyperlink linkCadastro;
    @FXML private Button btnVoltar;
    @FXML private Button btnTheme;
    @FXML private VBox boxNome;
    @FXML private VBox boxConfirmarSenha;
    @FXML private StackPane avatarContainer;
    @FXML private Circle clipCircle;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private VBox formInputsBox;
    @FXML private Label lblDuvida;

    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private boolean modoCadastro = false;
    private boolean temaDark = true;
    private boolean isAutenticando = false;
    private File fotoSelecionada = null;
    private Preferences prefs = Preferences.userNodeForPackage(LoginController.class);

    @FXML
    public void initialize() {
        // Carregar preferência salva
        temaDark = prefs.getBoolean("temaDark", true);

        // Enter no username → foca na senha
        txtUsername.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                txtSenha.requestFocus();
            }
        });

        // Aplicar o tema depois que a scene estiver carregada
        Platform.runLater(this::aplicarTemaAtual);
    }

    @FXML
    public void tentarLogar(ActionEvent event) {
        if (isAutenticando) return;
        
        limparMensagens();

        if (modoCadastro) {
            executarCadastro();
            return;
        }

        String username = txtUsername.getText();
        String senha = txtSenha.getText();

        if (username == null || username.trim().isEmpty() || senha == null || senha.trim().isEmpty()) {
            mostrarErro("Preencha todos os campos.");
            return;
        }

        Usuario logado = usuarioDAO.autenticar(username.trim(), senha);

        if (logado != null) {
            isAutenticando = true;
            efeitoCarregamento(logado);
        } else {
            mostrarErro("Usuário ou senha inválidos.");
            txtSenha.clear();
        }
    }

    private void executarCadastro() {
        String nome = txtNome.getText();
        String username = txtUsername.getText();
        String senha = txtSenha.getText();
        String confirmar = txtConfirmarSenha.getText();

        if (nome == null || nome.trim().isEmpty()
                || username == null || username.trim().isEmpty()
                || senha == null || senha.trim().isEmpty()) {
            mostrarErro("Preencha todos os campos obrigatórios.");
            return;
        }

        if (!senha.equals(confirmar)) {
            mostrarErro("As senhas não coincidem.");
            txtConfirmarSenha.clear();
            return;
        }

        if (senha.length() < 4) {
            mostrarErro("A senha deve ter pelo menos 4 caracteres.");
            return;
        }

        String fotoPath = fotoSelecionada != null ? fotoSelecionada.getAbsolutePath() : null;
        Usuario novoUsuario = new Usuario(nome.trim(), username.trim(), senha, "VENDEDOR");
        novoUsuario.setFotoPath(fotoPath);

        boolean sucesso = usuarioDAO.cadastrar(novoUsuario);

        if (sucesso) {
            mostrarSucesso("Conta criada! Faça login agora.");
            toggleCadastro(null);
            txtUsername.setText(username);
            txtSenha.clear();
        } else {
            mostrarErro("Esse nome de usuário já existe.");
        }
    }

    @FXML
    public void selecionarFoto() {
        if (!modoCadastro) return; // Só permite selecionar foto no modo cadastro

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecionar Foto de Perfil");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        Stage stage = (Stage) txtUsername.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);

        if (file != null) {
            fotoSelecionada = file;
            // Mostrar preview da foto no avatar
            try {
                Image img = new Image(file.toURI().toString(), 80, 80, true, true);
                Circle fotoCircle = new Circle(36);
                fotoCircle.setFill(new ImagePattern(img));
                // Esconder o ícone svg e mostrar a foto
                svgAvatarIcon.setVisible(false);
                svgAvatarIcon.setManaged(false);
                // Remover foto anterior se existir e adicionar nova
                avatarContainer.getChildren().removeIf(node -> node instanceof Circle);
                avatarContainer.getChildren().add(fotoCircle);
                lblFotoHint.setText("Foto selecionada ✓");
            } catch (Exception e) {
                mostrarErro("Erro ao carregar a imagem.");
            }
        }
    }

    @FXML
    public void toggleCadastro(ActionEvent event) {
        limparMensagens();
        modoCadastro = !modoCadastro;

        boxNome.setVisible(modoCadastro);
        boxNome.setManaged(modoCadastro);
        boxConfirmarSenha.setVisible(modoCadastro);
        boxConfirmarSenha.setManaged(modoCadastro);
        lblFotoHint.setVisible(modoCadastro);
        lblFotoHint.setManaged(modoCadastro);

        if (modoCadastro) {
            lblLoginTitle.setText("Criar Conta");
            lblLoginSub.setText("Preencha seus dados");
            btnAcessar.setText("Cadastrar");
            lblDuvida.setText("Ja tem conta?");
            linkCadastro.setText("Voltar ao login");
            if (btnVoltar != null) {
                btnVoltar.setVisible(true);
                btnVoltar.setManaged(true);
            }
            if (btnTheme != null) {
                btnTheme.setVisible(false);
                btnTheme.setManaged(false);
            }
        } else {
            lblLoginTitle.setText("Acessar Sistema");
            lblLoginSub.setText("Insira suas credenciais");
            btnAcessar.setText("Entrar");
            lblDuvida.setText("Novo por aqui?");
            linkCadastro.setText("Criar conta");
            if (btnVoltar != null) {
                btnVoltar.setVisible(false);
                btnVoltar.setManaged(false);
            }
            if (btnTheme != null) {
                btnTheme.setVisible(true);
                btnTheme.setManaged(true);
            }
            // Resetar avatar para ícone
            svgAvatarIcon.setVisible(true);
            svgAvatarIcon.setManaged(true);
            avatarContainer.getChildren().removeIf(node -> node instanceof Circle);
            fotoSelecionada = null;
        }
    }

    @FXML
    public void alternarTema(ActionEvent event) {
        temaDark = !temaDark;
        prefs.putBoolean("temaDark", temaDark);
        aplicarTemaAtual();
    }

    private void aplicarTemaAtual() {
        if (txtUsername.getScene() == null) return;
        Scene scene = txtUsername.getScene();
        Parent root = scene.getRoot();
        
        // Limpar das duas fontes possíveis p/ não dar conflito
        root.getStylesheets().clear();
        scene.getStylesheets().clear();

        if (temaDark) {
            root.getStylesheets().add(getClass().getResource("/view/login-styles.css").toExternalForm());
            if (btnTheme != null) btnTheme.setText("\u2600 Light");
        } else {
            root.getStylesheets().add(getClass().getResource("/view/login-styles-light.css").toExternalForm());
            if (btnTheme != null) btnTheme.setText("\uD83C\uDF19 Dark");
        }
    }

    private void efeitoCarregamento(Usuario logado) {
        formInputsBox.setVisible(false);
        lblMensagemErro.setVisible(false);
        lblMensagemSucesso.setVisible(false);

        lblLoginTitle.setText("Bem-vindo(a),");
        lblLoginSub.setText(logado.getNome() + "!");

        if (logado.getFotoPath() != null && !logado.getFotoPath().isEmpty()) {
            try {
                File file = new File(logado.getFotoPath());
                if (file.exists()) {
                    Image img = new Image(file.toURI().toString(), 80, 80, true, true);
                    Circle fotoCircle = new Circle(36);
                    fotoCircle.setFill(new ImagePattern(img));
                    svgAvatarIcon.setVisible(false);
                    avatarContainer.getChildren().removeIf(node -> node instanceof Circle || node instanceof ProgressIndicator);
                    avatarContainer.getChildren().add(fotoCircle);
                }
            } catch (Exception e) {}
        }

        loadingSpinner.setVisible(true);
        loadingSpinner.setManaged(true);
        if (!avatarContainer.getChildren().contains(loadingSpinner)) {
            avatarContainer.getChildren().add(loadingSpinner);
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
        pause.setOnFinished(e -> abrirTelaPrincipal(logado));
        pause.play();
    }

    private void mostrarErro(String msg) {
        lblMensagemSucesso.setVisible(false);
        lblMensagemSucesso.setManaged(false);
        lblMensagemErro.setText(msg);
        lblMensagemErro.setVisible(true);
        lblMensagemErro.setManaged(true);
    }

    private void mostrarSucesso(String msg) {
        lblMensagemErro.setVisible(false);
        lblMensagemErro.setManaged(false);
        lblMensagemSucesso.setText("\u2705 " + msg);
        lblMensagemSucesso.setVisible(true);
        lblMensagemSucesso.setManaged(true);
    }

    private void limparMensagens() {
        lblMensagemErro.setVisible(false);
        lblMensagemErro.setManaged(false);
        lblMensagemSucesso.setVisible(false);
        lblMensagemSucesso.setManaged(false);
    }

    private void abrirTelaPrincipal(Usuario logado) {
        try {
            // Fechar a janela de login atual
            Stage loginStage = (Stage) txtUsername.getScene().getWindow();
            loginStage.close();

            // Abrir nova janela limpa para evitar conflitos de maximização
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ProdutoView.fxml"));
            Parent root = loader.load();

            ProdutoController controller = loader.getController();
            controller.setUsuarioLogado(logado);

            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/view/styles.css").toExternalForm());

            Stage mainStage = new Stage();
            
            scene.setOnKeyPressed(ev -> {
                if (ev.getCode() == javafx.scene.input.KeyCode.F11) {
                    mainStage.setFullScreen(!mainStage.isFullScreen());
                }
            });

            mainStage.setTitle("SENAI — Cadastro de Produtos");
            mainStage.setScene(scene);
            mainStage.setResizable(true);
            mainStage.setMinWidth(1000);
            mainStage.setMinHeight(650);
            
            // Focar e exibir tela cheia de forma segura
            mainStage.show();
            mainStage.setMaximized(true);

        } catch (Exception e) {
            System.err.println("Erro ao carregar a tela principal: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro interno ao mudar de tela.");
        }
    }
}
