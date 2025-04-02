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

public class RegisterController {

    @FXML
    private TextField inputfieldlabel;

    @FXML
    private Button signbtn;

    private static final String DATA_FILE = "user_data.json";

    @FXML
    public void initialize() {
        // Check if the user is already registered
        if (isUserRegistered()) {
            goToMainDashboard();
        }
    }

    @FXML
    private void handleSignIn() {
        String username = inputfieldlabel.getText().trim();
        if (!username.isEmpty()) {
            saveUserData(username);
            goToMainDashboard();
        }
    }

    private boolean isUserRegistered() {
        File file = new File(DATA_FILE);
        return file.exists();
    }

    private void saveUserData(String username) {
        JSONObject userData = new JSONObject();
        userData.put("username", username);
        
        try (FileWriter file = new FileWriter(DATA_FILE)) {
            file.write(userData.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goToMainDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainDashboard.fxml"));
            AnchorPane dashboardRoot = loader.load();
            Scene dashboardScene = new Scene(dashboardRoot, 400, 900);
            
            Stage stage = (Stage) signbtn.getScene().getWindow();
            stage.setScene(dashboardScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
