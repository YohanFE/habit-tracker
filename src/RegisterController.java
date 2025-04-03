import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONObject;
import java.io.File;
import java.net.URL;

public class RegisterController {
    @FXML
    private TextField inputfieldlabel;
    @FXML
    private Button signbtn;
    private static final String DATA_FILE = "user_data.json";
    
    @FXML
    public void initialize() {
        System.out.println("RegisterController initialized");
        // Check if the user is already registered
        if (isUserRegistered()) {
            System.out.println("User already registered, redirecting to dashboard");
            goToMainDashboard();
        }
    }
    
    @FXML
    private void handleSignIn() {
        String username = inputfieldlabel.getText().trim();
        System.out.println("Sign in attempt with username: " + username);
        if (!username.isEmpty()) {
            saveUserData(username);
            goToMainDashboard();
        } else {
            System.out.println("Username is empty, not proceeding");
        }
    }
    
    private boolean isUserRegistered() {
        File file = new File(DATA_FILE);
        boolean exists = file.exists();
        System.out.println("Checking if user data exists: " + exists + " at path: " + file.getAbsolutePath());
        return exists;
    }
    
    private void saveUserData(String username) {
        JSONObject userData = new JSONObject();
        userData.put("username", username);
        
        try (FileWriter file = new FileWriter(DATA_FILE)) {
            file.write(userData.toString());
            System.out.println("User data saved successfully: " + userData.toString());
        } catch (IOException e) {
            System.err.println("Error saving user data:");
            e.printStackTrace();
        }
    }
    
    private void goToMainDashboard() {
        try {
            // Debug: Try different ways to locate the dashboard FXML file
            System.out.println("Method 1: " + getClass().getResource("/MainDashboard.fxml"));
            System.out.println("Method 2: " + getClass().getClassLoader().getResource("MainDashboard.fxml"));
            System.out.println("Method 3: " + getClass().getResource("MainDashboard.fxml"));
            
            // Check if file exists directly
            File file1 = new File("resources/MainDashboard.fxml");
            System.out.println("Direct file exists: " + file1.exists() + " Path: " + file1.getAbsolutePath());
            
            File file2 = new File("src/main/resources/MainDashboard.fxml");
            System.out.println("Maven path exists: " + file2.exists() + " Path: " + file2.getAbsolutePath());
            
            // Try to load with direct file path if available
            URL dashboardUrl = null;
            if (file1.exists()) {
                dashboardUrl = file1.toURI().toURL();
                System.out.println("Using file1 path: " + dashboardUrl);
            } else if (file2.exists()) {
                dashboardUrl = file2.toURI().toURL();
                System.out.println("Using file2 path: " + dashboardUrl);
            } else {
                // Try with classloader as fallback
                dashboardUrl = getClass().getClassLoader().getResource("MainDashboard.fxml");
                System.out.println("Using classloader resource: " + dashboardUrl);
            }
            
            if (dashboardUrl != null) {
                FXMLLoader loader = new FXMLLoader(dashboardUrl);
                AnchorPane dashboardRoot = loader.load();
                Scene dashboardScene = new Scene(dashboardRoot, 400, 950);
                
                Stage stage = (Stage) signbtn.getScene().getWindow();
                stage.setScene(dashboardScene);
                System.out.println("Successfully loaded MainDashboard.fxml");
            } else {
                throw new IOException("Could not locate MainDashboard.fxml by any method");
            }
        } catch (Exception e) {
            System.err.println("Error loading MainDashboard.fxml:");
            e.printStackTrace();
        }
    }
}