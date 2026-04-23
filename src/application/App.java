package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Carregar o FXML (Agora começa pelo Login)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();

            // Janela ampla e moderna
            Scene scene = new Scene(root, 1100, 650);
            scene.getStylesheets().add(getClass().getResource("/view/login-styles.css").toExternalForm());

            primaryStage.setTitle("EletroTech OS");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao inicializar a aplicação!");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
