import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import org.json.JSONObject;
import org.json.JSONException;

public class DashboardController implements Initializable {

    // FXML Button Components
    @FXML private Button dailybtn;
    @FXML private Button meditationBtn;
    @FXML private Button scoutingMissionBtn;
    @FXML private Button trainingGroundsBtn;
    
    // FXML Label Components
    @FXML private Label loginExpLabel;
    @FXML private Label meditationExpLabel;
    @FXML private Label scoutExpLabel;
    @FXML private Label trainingExpLabel;
    @FXML private Label dialogueLabel;
    
    
    // FXML ImageView Components
    @FXML private ImageView receptionistimage;
    @FXML private ImageView rankImageView; // Added this field for rank image
    @FXML private ImageView rankviewlabel;
    
    // User Data
    private String username;
    private int currentExp = 0;
    private int level = 1;
    private String rank = "Rookie";
    private boolean dailyLoginClaimed = false;
    private LocalDateTime lastLoginTime;
    private boolean meditationCompleted = false;
    private boolean scoutingCompleted = false;
    private boolean trainingCompleted = false;
    
    // Timers
    private Timeline dailyResetTimer;
    private Timeline meditationTimer;
    private Timeline scoutingTimer;
    private Timeline trainingTimer;
    
    // EXP values for activities
    private final int LOGIN_EXP = 20;
    private final int MEDITATION_EXP = 30;
    private final int SCOUTING_EXP = 40;
    private final int TRAINING_EXP = 50;
    
    // Encouraging messages
    private final List<String> encouragingMessages = Arrays.asList(
        "You've done such a great job completing everything.",
        "I am proud of you, keep up the good work!"
    );
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserData();
        // Initialize components first before using them
        initializeComponents();
        setupButtons();
        checkDailyLoginStatus();
        updateRankView();
    }
    
    // Added this method to ensure components are properly initialized
    private void initializeComponents() {
        // Set default values for labels if they're not null
        if (loginExpLabel != null) loginExpLabel.setText("+" + LOGIN_EXP + " XP");
        if (meditationExpLabel != null) meditationExpLabel.setText("+" + MEDITATION_EXP + " XP");
        if (scoutExpLabel != null) scoutExpLabel.setText("+" + SCOUTING_EXP + " XP");
        if (trainingExpLabel != null) trainingExpLabel.setText("+" + TRAINING_EXP + " XP");
        
        // Initialize buttons with default text if they're not null
        if (meditationBtn != null) meditationBtn.setText("Honing the Mind (Meditate for 5 minutes)");
        if (scoutingMissionBtn != null) scoutingMissionBtn.setText("Scouting Mission (Take a walk for 1km)");
        if (trainingGroundsBtn != null) trainingGroundsBtn.setText("Training Grounds (Do any forms of exercise)");
    }
    
    private void setupButtons() {
        if (dailybtn != null) dailybtn.setOnAction(event -> claimDailyLogin());
        if (meditationBtn != null) meditationBtn.setOnAction(event -> startMeditation());
        if (scoutingMissionBtn != null) scoutingMissionBtn.setOnAction(event -> startScoutingMission());
        if (trainingGroundsBtn != null) trainingGroundsBtn.setOnAction(event -> startTraining());
        
        // Initially disable progression buttons if daily login not claimed
        updateButtonStates();
    }
    
    private void loadUserData() {
        File file = new File("user_data.json");
        
        if (!file.exists()) {
            // If file doesn't exist, create new user data
            System.out.println("No existing user data found. Creating new profile.");
            saveUserData();
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            
            JSONObject userData = new JSONObject(content.toString());
            
            username = userData.has("username") ? userData.getString("username") : "";
            
            // Load progress data if exists
            if (userData.has("exp")) {
                currentExp = userData.getInt("exp");
            }
            if (userData.has("level")) {
                level = userData.getInt("level");
            }
            if (userData.has("rank")) {
                rank = userData.getString("rank");
            }
            if (userData.has("lastLoginTime")) {
                lastLoginTime = LocalDateTime.parse(userData.getString("lastLoginTime"));
                
                // Check if the last login was more than 24 hours ago
                if (ChronoUnit.HOURS.between(lastLoginTime, LocalDateTime.now()) >= 24) {
                    dailyLoginClaimed = false;
                } else {
                    dailyLoginClaimed = true;
                }
            }
            if (userData.has("dailyLoginClaimed")) {
                dailyLoginClaimed = userData.getBoolean("dailyLoginClaimed");
            }
            if (userData.has("meditationCompleted")) {
                meditationCompleted = userData.getBoolean("meditationCompleted");
            }
            if (userData.has("scoutingCompleted")) {
                scoutingCompleted = userData.getBoolean("scoutingCompleted");
            }
            if (userData.has("trainingCompleted")) {
                trainingCompleted = userData.getBoolean("trainingCompleted");
            }
            
        } catch (IOException | JSONException e) {
            System.out.println("Error reading user data: " + e.getMessage());
            saveUserData();
        }
    }
    
    private void saveUserData() {
        JSONObject userData = new JSONObject();
        
        userData.put("username", username != null ? username : "");
        userData.put("exp", currentExp);
        userData.put("level", level);
        userData.put("rank", rank);
        userData.put("lastLoginTime", lastLoginTime != null ? lastLoginTime.toString() : LocalDateTime.now().toString());
        userData.put("dailyLoginClaimed", dailyLoginClaimed);
        userData.put("meditationCompleted", meditationCompleted);
        userData.put("scoutingCompleted", scoutingCompleted);
        userData.put("trainingCompleted", trainingCompleted);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("user_data.json"))) {
            writer.write(userData.toString(4)); // Pretty print with 4-space indent
        } catch (IOException e) {
            System.out.println("Error saving user data: " + e.getMessage());
        }
    }
    
    private void checkDailyLoginStatus() {
        if (lastLoginTime != null) {
            long hoursSinceLastLogin = ChronoUnit.HOURS.between(lastLoginTime, LocalDateTime.now());
            
            if (hoursSinceLastLogin >= 24) {
                // Reset daily tasks
                dailyLoginClaimed = false;
                meditationCompleted = false;
                scoutingCompleted = false;
                trainingCompleted = false;
                saveUserData();
            } else {
                // Set up timer for the next day reset
                long minutesUntilReset = 24 * 60 - ChronoUnit.MINUTES.between(lastLoginTime, LocalDateTime.now());
                
                dailyResetTimer = new Timeline(
                    new KeyFrame(Duration.minutes(minutesUntilReset), e -> {
                        dailyLoginClaimed = false;
                        meditationCompleted = false;
                        scoutingCompleted = false;
                        trainingCompleted = false;
                        updateUI();
                        saveUserData();
                    })
                );
                dailyResetTimer.play();
            }
        }
        
        updateUI();
    }
    
    private void claimDailyLogin() {
        if (!dailyLoginClaimed) {
            dailyLoginClaimed = true;
            lastLoginTime = LocalDateTime.now();
            addExp(LOGIN_EXP);
            if (loginExpLabel != null) loginExpLabel.setVisible(false);
            if (dailybtn != null) dailybtn.setDisable(true);
            
            // Set up daily reset timer
            dailyResetTimer = new Timeline(
                new KeyFrame(Duration.hours(24), e -> {
                    dailyLoginClaimed = false;
                    meditationCompleted = false;
                    scoutingCompleted = false;
                    trainingCompleted = false;
                    updateUI();
                    saveUserData();
                })
            );
            dailyResetTimer.play();
            
            updateButtonStates();
            saveUserData();
        }
    }
    
    private void startMeditation() {
        if (dailyLoginClaimed && !meditationCompleted && meditationBtn != null) {
            meditationBtn.setDisable(true);
            meditationBtn.setStyle("-fx-background-color: lightgray;");
            
            // Start 5-minute meditation timer
            final int[] secondsRemaining = {5 * 60};
            meditationTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    secondsRemaining[0]--;
                    int minutes = secondsRemaining[0] / 60;
                    int seconds = secondsRemaining[0] % 60;
                    meditationBtn.setText(String.format("Meditating... %d:%02d", minutes, seconds));
                    
                    if (secondsRemaining[0] <= 0) {
                        meditationCompleted = true;
                        addExp(MEDITATION_EXP);
                        if (meditationExpLabel != null) meditationExpLabel.setVisible(false);
                        meditationBtn.setText("Meditation Completed");
                        updateButtonStates();
                        saveUserData();
                        meditationTimer.stop();
                    }
                })
            );
            meditationTimer.setCycleCount(5 * 60);
            meditationTimer.play();
        }
    }
    
    private void startScoutingMission() {
        if (dailyLoginClaimed && meditationCompleted && !scoutingCompleted && scoutingMissionBtn != null) {
            scoutingMissionBtn.setDisable(true);
            scoutingMissionBtn.setStyle("-fx-background-color: lightgray;");
            
            // Start 13-minute scouting timer (simulating a 1km walk)
            final int[] secondsRemaining = {13 * 60};
            scoutingTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    secondsRemaining[0]--;
                    int minutes = secondsRemaining[0] / 60;
                    int seconds = secondsRemaining[0] % 60;
                    scoutingMissionBtn.setText(String.format("Walking... %d:%02d", minutes, seconds));
                    
                    if (secondsRemaining[0] <= 0) {
                        scoutingCompleted = true;
                        addExp(SCOUTING_EXP);
                        if (scoutExpLabel != null) scoutExpLabel.setVisible(false);
                        scoutingMissionBtn.setText("Scouting Completed");
                        updateButtonStates();
                        saveUserData();
                        scoutingTimer.stop();
                    }
                })
            );
            scoutingTimer.setCycleCount(13 * 60);
            scoutingTimer.play();
        }
    }
    
    private void startTraining() {
        if (dailyLoginClaimed && meditationCompleted && scoutingCompleted && !trainingCompleted && trainingGroundsBtn != null) {
            trainingGroundsBtn.setDisable(true);
            trainingGroundsBtn.setStyle("-fx-background-color: lightgray;");
            
            // Start 20-minute training timer
            final int[] secondsRemaining = {20 * 60};
            trainingTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    secondsRemaining[0]--;
                    int minutes = secondsRemaining[0] / 60;
                    int seconds = secondsRemaining[0] % 60;
                    trainingGroundsBtn.setText(String.format("Training... %d:%02d", minutes, seconds));
                    
                    if (secondsRemaining[0] <= 0) {
                        trainingCompleted = true;
                        addExp(TRAINING_EXP);
                        if (trainingExpLabel != null) trainingExpLabel.setVisible(false);
                        trainingGroundsBtn.setText("Training Completed");
                        updateButtonStates();
                        checkAllTasksCompleted();
                        saveUserData();
                        trainingTimer.stop();
                    }
                })
            );
            trainingTimer.setCycleCount(20 * 60);
            trainingTimer.play();
        }
    }
    
    private void addExp(int expAmount) {
        currentExp += expAmount;
        checkLevelUp();
        updateRankView();
    }
    
    private void checkLevelUp() {
        int expRequired = getExpRequiredForNextLevel();
        
        if (currentExp >= expRequired) {
            currentExp -= expRequired;
            level++;
            updateRank();
            
            // Check for level up again in case multiple levels gained
            checkLevelUp();
        }
    }
    
    private int getExpRequiredForNextLevel() {
        if (level >= 1 && level <= 4) {
            return 100; // Rookie
        } else if (level >= 5 && level <= 9) {
            return 200; // Novice
        } else if (level >= 10 && level <= 14) {
            return 300; // Veteran
        } else if (level >= 15 && level <= 19) {
            return 400; // Elite
        } else if (level >= 20 && level <= 25) {
            return 500; // Master
        } else {
            return 500; // Default for beyond level 25
        }
    }
    
    private void updateRank() {
        if (level >= 1 && level <= 4) {
            rank = "Rookie";
        } else if (level >= 5 && level <= 9) {
            rank = "Novice";
        } else if (level >= 10 && level <= 14) {
            rank = "Veteran";
        } else if (level >= 15 && level <= 19) {
            rank = "Elite";
        } else if (level >= 20) {
            rank = "Master";
        }
        
        updateRankImage();
    }
    
    private void updateRankImage() {
        if (rankImageView == null) return;
        
        String imagePath = "/img/" + rank + "Rank.png";
        try {
            Image rankImage = new Image(getClass().getResourceAsStream(imagePath));
            rankImageView.setImage(rankImage);
        } catch (Exception e) {
            System.out.println("Error loading rank image: " + e.getMessage());
        }
    }
    
    private void updateRankView() {
        // If you want to update the ImageView for rank information
        if (rankviewlabel != null) {
            // Since rankviewlabel is an ImageView, we need to load an appropriate image
            try {
                String imagePath = "/img/" + rank + "Rank.png";
                Image rankImage = new Image(getClass().getResourceAsStream(imagePath));
                rankviewlabel.setImage(rankImage);
            } catch (Exception e) {
                System.out.println("Error loading rank image for rankviewlabel: " + e.getMessage());
            }
        }
    }
    
    private void updateButtonStates() {
        // Daily Login Button
        if (dailybtn != null) {
            dailybtn.setDisable(dailyLoginClaimed);
        }
        
        if (loginExpLabel != null) {
            loginExpLabel.setVisible(!dailyLoginClaimed);
        }
        
        // Meditation Button
        if (meditationBtn != null) {
            if (!dailyLoginClaimed) {
                meditationBtn.setDisable(true);
                meditationBtn.setStyle("-fx-background-color: darkgray;");
            } else if (!meditationCompleted) {
                meditationBtn.setDisable(false);
                meditationBtn.setStyle("");
                meditationBtn.setText("Honing the Mind (Meditate for 5 minutes)");
            }
        }
        
        if (meditationExpLabel != null) {
            meditationExpLabel.setVisible(dailyLoginClaimed && !meditationCompleted);
        }
        
        // Scouting Button
        if (scoutingMissionBtn != null) {
            if (!dailyLoginClaimed || !meditationCompleted) {
                scoutingMissionBtn.setDisable(true);
                scoutingMissionBtn.setStyle("-fx-background-color: darkgray;");
            } else if (!scoutingCompleted) {
                scoutingMissionBtn.setDisable(false);
                scoutingMissionBtn.setStyle("");
                scoutingMissionBtn.setText("Scouting Mission (Take a walk for 1km)");
            }
        }
        
        if (scoutExpLabel != null) {
            scoutExpLabel.setVisible(dailyLoginClaimed && meditationCompleted && !scoutingCompleted);
        }
        
        // Training Button
        if (trainingGroundsBtn != null) {
            if (!dailyLoginClaimed || !meditationCompleted || !scoutingCompleted) {
                trainingGroundsBtn.setDisable(true);
                trainingGroundsBtn.setStyle("-fx-background-color: darkgray;");
            } else if (!trainingCompleted) {
                trainingGroundsBtn.setDisable(false);
                trainingGroundsBtn.setStyle("");
                trainingGroundsBtn.setText("Training Grounds (Do any forms of exercise)");
            }
        }
        
        if (trainingExpLabel != null) {
            trainingExpLabel.setVisible(dailyLoginClaimed && meditationCompleted && scoutingCompleted && !trainingCompleted);
        }
    }
    
    private void checkAllTasksCompleted() {
        if (!dailyLoginClaimed || !meditationCompleted || !scoutingCompleted || !trainingCompleted) return;
        
        if (dialogueLabel != null) {
            Random random = new Random();
            String message = encouragingMessages.get(random.nextInt(encouragingMessages.size()));
            dialogueLabel.setText(message);
        }
        
        // Change receptionist image to happy
        if (receptionistimage != null) {
            try {
                Image happyReceptionist = new Image(getClass().getResourceAsStream("/img/Receptionist_Happy.png"));
                receptionistimage.setImage(happyReceptionist);
            } catch (Exception e) {
                System.out.println("Error loading receptionist image: " + e.getMessage());
            }
        }
    }
    
    private void updateUI() {
        // Update EXP labels
        if (loginExpLabel != null) loginExpLabel.setText("+" + LOGIN_EXP + " XP");
        if (meditationExpLabel != null) meditationExpLabel.setText("+" + MEDITATION_EXP + " XP");
        if (scoutExpLabel != null) scoutExpLabel.setText("+" + SCOUTING_EXP + " XP");
        if (trainingExpLabel != null) trainingExpLabel.setText("+" + TRAINING_EXP + " XP");
        
        // Update rank view
        updateRankView();
        
        // Update button states
        updateButtonStates();
        
        // Set default receptionist image
        if (receptionistimage != null) {
            try {
                Image idleReceptionist = new Image(getClass().getResourceAsStream("/img/Receptionist_Idle.png"));
                receptionistimage.setImage(idleReceptionist);
            } catch (Exception e) {
                System.out.println("Error loading receptionist image: " + e.getMessage());
            }
        }
        
        // Set rank image
        updateRankImage();
        
        // Check if all tasks completed
        checkAllTasksCompleted();
    }
    
    // Public method to close all timers when application closes
    public void shutdown() {
        if (dailyResetTimer != null) {
            dailyResetTimer.stop();
        }
        if (meditationTimer != null) {
            meditationTimer.stop();
        }
        if (scoutingTimer != null) {
            scoutingTimer.stop();
        }
        if (trainingTimer != null) {
            trainingTimer.stop();
        }
    }
}