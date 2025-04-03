import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.net.URL;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Debug: Print current working directory
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        
        // Debug: Try different ways to locate the file
        System.out.println("Method 1: " + getClass().getResource("/LogoScreen.fxml"));
        System.out.println("Method 2: " + getClass().getClassLoader().getResource("LogoScreen.fxml"));
        System.out.println("Method 3: " + getClass().getResource("LogoScreen.fxml"));
        
        // Check if file exists directly
        File file1 = new File("resources/LogoScreen.fxml");
        System.out.println("Direct file exists: " + file1.exists() + " Path: " + file1.getAbsolutePath());
        
        File file2 = new File("src/main/resources/LogoScreen.fxml");
        System.out.println("Maven path exists: " + file2.exists() + " Path: " + file2.getAbsolutePath());
        
        // Try to load with direct file path
        try {
            URL directUrl = file1.exists() ? file1.toURI().toURL() : 
                           (file2.exists() ? file2.toURI().toURL() : null);
            
            if (directUrl != null) {
                System.out.println("Found file at: " + directUrl);
                AnchorPane root = FXMLLoader.load(directUrl);
                Scene scene = new Scene(root, Color.WHITE);
                primaryStage.setScene(scene);
                primaryStage.setResizable(false);
                primaryStage.show();
                
                // Rest of your animation code goes here
                setupAnimations(root, primaryStage);
            } else {
                throw new Exception("Could not locate FXML file by any method");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Failed to load FXML file");
        }
    }
    
    private void setupAnimations(AnchorPane root, Stage primaryStage) {
        // Apply the fade-in effect
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(4), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setCycleCount(1);
        fadeIn.setAutoReverse(false);

        // Apply the fade-out effect
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setCycleCount(1);
        fadeOut.setAutoReverse(false);

        fadeIn.play();

        fadeIn.setOnFinished(event -> {
            fadeOut.play();
            fadeOut.setOnFinished(e -> {
                if (isUserRegistered()) {
                    goToMainDashboard(primaryStage);
                } else {
                    goToRegistration(primaryStage);
                }
            });
        });
    }
    
    private void goToRegistration(Stage stage) {
        try {
            // Try to find the file directly 
            File file = new File("resources/GuildRegistration.fxml");
            if (!file.exists()) {
                file = new File("src/main/resources/GuildRegistration.fxml");
            }
            
            if (file.exists()) {
                URL url = file.toURI().toURL();
                AnchorPane registrationRoot = FXMLLoader.load(url);
                Scene registrationScene = new Scene(registrationRoot);
                stage.setScene(registrationScene);
            } else {
                System.err.println("Cannot find GuildRegistration.fxml");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Check if the user is already registered
    private boolean isUserRegistered() {
        File file = new File("user_data.json");
        return file.exists();
    }

    // Method to switch to the Main Dashboard
    private void goToMainDashboard(Stage stage) {
        try {
            // Try to find the file directly
            File file = new File("resources/MainDashboard.fxml");
            if (!file.exists()) {
                file = new File("src/main/resources/MainDashboard.fxml");
            }
            
            if (file.exists()) {
                URL url = file.toURI().toURL();
                AnchorPane dashboardRoot = FXMLLoader.load(url);
                Scene dashboardScene = new Scene(dashboardRoot);
                stage.setScene(dashboardScene);
            } else {
                System.err.println("Cannot find MainDashboard.fxml");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}