import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the LogoScreen.fxml
        AnchorPane root = FXMLLoader.load(getClass().getResource("LogoScreen.fxml"));
        Scene scene = new Scene(root, 400, 900, Color.WHITE); // Make sure to match your design dimensions
        primaryStage.setScene(scene);
        primaryStage.show();

        // Apply the fade-in effect
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(4), root);
        fadeIn.setFromValue(0);  // Start with opacity 0 (invisible)
        fadeIn.setToValue(1);    // End with opacity 1 (fully visible)
        fadeIn.setCycleCount(1);
        fadeIn.setAutoReverse(false);

        // Apply the fade-out effect after the fade-in finishes
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), root);
        fadeOut.setFromValue(1); // Start with opacity 1 (fully visible)
        fadeOut.setToValue(0);   // End with opacity 0 (invisible)
        fadeOut.setCycleCount(1);
        fadeOut.setAutoReverse(false);

        fadeIn.play(); // Play the fade-in effect

        // Transition to GuildRegistration after fade-out
        fadeIn.setOnFinished(event -> {
            fadeOut.play(); // Play the fade-out effect after fade-in
            fadeOut.setOnFinished(e -> {
                // Load the GuildRegistration.fxml after fade-out
                try {
                    AnchorPane registrationRoot = FXMLLoader.load(getClass().getResource("GuildRegistration.fxml"));
                    Scene registrationScene = new Scene(registrationRoot);
                    primaryStage.setScene(registrationScene); // Switch to the next scene
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
