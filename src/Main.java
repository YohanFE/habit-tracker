import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the LogoScreen.fxml
        AnchorPane root = FXMLLoader.load(getClass().getResource("LogoScreen.fxml"));
        Scene scene = new Scene(root, Color.WHITE); // Make sure to match your design dimensions
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
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
                // Check if the user is already registered
                if (isUserRegistered()) {
                    // Load the MainDashboard.fxml if the user is already registered
                    goToMainDashboard(primaryStage);
                } else {
                    // Load the GuildRegistration.fxml if the user isn't registered
                    try {
                        AnchorPane registrationRoot = FXMLLoader.load(getClass().getResource("GuildRegistration.fxml"));
                        Scene registrationScene = new Scene(registrationRoot);
                        primaryStage.setScene(registrationScene); // Switch to the registration scene
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });
    }

    // Check if the user is already registered
    private boolean isUserRegistered() {
        File file = new File("user_data.json");
        return file.exists();
    }

    // Method to switch to the Main Dashboard
    private void goToMainDashboard(Stage stage) {
        try {
            AnchorPane dashboardRoot = FXMLLoader.load(getClass().getResource("MainDashboard.fxml"));
            Scene dashboardScene = new Scene(dashboardRoot);
            stage.setScene(dashboardScene); // Switch to the Main Dashboard
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
