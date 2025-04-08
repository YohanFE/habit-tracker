import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DashboardController {

    // FXML Annotated UI Elements
    @FXML private Button dailybtn;
    @FXML private Button meditationBtn;
    @FXML private Button scoutingMissionBtn;
    @FXML private Button trainingGroundsBtn;
    
    @FXML private Label loginExpLabel;
    @FXML private Label meditationExpLabel;
    @FXML private Label scoutExpLabel;
    @FXML private Label trainingExpLabel;
    @FXML private Label dialogueLabel;
    @FXML private Label rankTextLabel; // Add this if you need a label for rank text
    
    @FXML private ImageView receptionistimage;
    @FXML private ImageView rankviewlabel; // This will be used instead of rankImage
    
    // Constants
    private static final String DATA_FILE = "progression_data.json";
    private static final String IMG_PATH = "resources/img/";
    
    // Color Constants
    private static final String COLOR_AVAILABLE = "#4CAF50"; // Green
    private static final String COLOR_DARK_GREY = "#616161"; // Dark Grey
    private static final String COLOR_LIGHT_GREY = "#9E9E9E"; // Light Grey
    
    // XP Values
    private static final int LOGIN_XP = 25;
    private static final int MEDITATION_XP = 50;
    private static final int SCOUTING_XP = 75;
    private static final int TRAINING_XP = 100;
    
    // Timer delays
    private static final int MEDITATION_TIMER = 5 * 60; // 5 minutes in seconds
    private static final int SCOUTING_TIMER = 13 * 60; // 13 minutes in seconds  
    private static final int TRAINING_TIMER = 20 * 60; // 20 minutes in seconds
    
    // User progression data
    private int currentXp = 0;
    private int currentLevel = 1;
    private String currentRank = "Rookie";
    private int xpForNextLevel = 100;
    private LocalDateTime lastLoginTime;
    private boolean loginDone = false;
    private boolean meditationDone = false;
    private boolean scoutingDone = false;
    private boolean trainingDone = false;
    
    // Countdown timers
    private int meditationCountdown = 0;
    private int scoutingCountdown = 0;
    private int trainingCountdown = 0;
    
    // Timers
    private Timer autoSaveTimer;
    private Timer meditationTimer;
    private Timer scoutingTimer;
    private Timer trainingTimer;
    
    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Debug info
        System.out.println("---- FXML Components Initialization Status ----");
        System.out.println("dailybtn: " + (dailybtn != null));
        System.out.println("meditationBtn: " + (meditationBtn != null));
        System.out.println("scoutingMissionBtn: " + (scoutingMissionBtn != null));
        System.out.println("trainingGroundsBtn: " + (trainingGroundsBtn != null));
        System.out.println("receptionistimage: " + (receptionistimage != null));
        System.out.println("rankviewlabel: " + (rankviewlabel != null));
        System.out.println("dialogueLabel: " + (dialogueLabel != null));
        System.out.println("rankTextLabel: " + (rankTextLabel != null));
        System.out.println("----------------------------------------");
        
        loadUserData();
        setupAutoSave();
        setupButtonHoverEffects();
        
        // Check all UI elements before updating UI
        if (checkUiComponents()) {
            updateUIState();
        } else {
            System.err.println("ERROR: Some UI components are missing. UI update skipped to avoid crashes.");
        }
        
        // Check if a new day has passed since last login
        if (lastLoginTime != null && 
            ChronoUnit.HOURS.between(lastLoginTime, LocalDateTime.now()) >= 24) {
            resetDailyTasks();
        }
    }
    
    /**
     * Check if all essential UI components are not null
     */
    private boolean checkUiComponents() {
        boolean buttonsOk = dailybtn != null && meditationBtn != null && 
                          scoutingMissionBtn != null && trainingGroundsBtn != null;
        
        boolean labelsOk = dialogueLabel != null;
        
        boolean imagesOk = receptionistimage != null && rankviewlabel != null;
        
        return buttonsOk && labelsOk && imagesOk;
    }
    
    /**
     * Set up automatic data saving
     */
    private void setupAutoSave() {
        autoSaveTimer = new Timer(true);
        autoSaveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                saveUserData();
            }
        }, 5000, 5000); // Every 5 seconds
    }
    
    /**
     * Reset all daily tasks
     */
    private void resetDailyTasks() {
        loginDone = false;
        meditationDone = false;
        scoutingDone = false;
        trainingDone = false;
        
        lastLoginTime = LocalDateTime.now();
        saveUserData();
        updateUIState();
    }
    
    /**
     * Set up hover effects for buttons
     */
    private void setupButtonHoverEffects() {
        setupButtonHover(dailybtn);
        setupButtonHover(meditationBtn);
        setupButtonHover(scoutingMissionBtn);
        setupButtonHover(trainingGroundsBtn);
    }
    
    /**
     * Add hover and click effects to a button
     */
    private void setupButtonHover(Button button) {
        if (button == null) {
            System.err.println("Warning: Attempted to setup hover effects on a null button.");
            return;
        }
        
        button.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            if (!button.isDisabled()) {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
                scaleTransition.setToX(1.05);
                scaleTransition.setToY(1.05);
                scaleTransition.play();
            }
        });
        
        button.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });
        
        button.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (!button.isDisabled()) {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), button);
                scaleTransition.setToX(0.95);
                scaleTransition.setToY(0.95);
                scaleTransition.play();
            }
        });
        
        button.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (!button.isDisabled()) {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), button);
                scaleTransition.setToX(1.0);
                scaleTransition.setToY(1.0);
                scaleTransition.play();
            }
        });
    }
    
    /**
     * Handle daily login button action
     */
    @FXML
    public void checkAct(ActionEvent event) {
        if (!loginDone) {
            addExp(LOGIN_XP);
            loginDone = true;
            lastLoginTime = LocalDateTime.now();
            updateDialogue("Daily login complete! You earned " + LOGIN_XP + " XP!");
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> dialogueLabel.setText(""));
              pause.play();
            updateUIState();
            saveUserData();
            
            // Start meditation timer immediately after login
            startMeditationTimer();
        }
    }
    
    /**
     * Handle meditation button action
     */
    @FXML
    public void meditationAct(ActionEvent event) {
        if (loginDone && !meditationDone && meditationCountdown == 0) {
            addExp(MEDITATION_XP);
            meditationDone = true;
            updateDialogue("Meditation complete! Your mind is sharper. You earned " + MEDITATION_XP + " XP!");
             PauseTransition pause = new PauseTransition(Duration.seconds(5));
               pause.setOnFinished(e -> dialogueLabel.setText(""));
                 pause.play();
            updateUIState();
            saveUserData();
            
            // Start scouting timer immediately after meditation is completed
            startScoutingTimer();
        }
    }
    
    /**
     * Handle scouting mission button action
     */
    @FXML
    public void scoutingAct(ActionEvent event) {
        if (loginDone && meditationDone && !scoutingDone && scoutingCountdown == 0) {
            addExp(SCOUTING_XP);
            scoutingDone = true;
            updateDialogue("Scouting mission complete! You discovered new areas. You earned " + SCOUTING_XP + " XP!");
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> dialogueLabel.setText(""));
              pause.play();
            updateUIState();
            saveUserData();
            
            // Start training timer immediately after scouting is completed
            startTrainingTimer();
        }
    }
    
    /**
     * Handle training grounds button action
     */
    @FXML
    public void trainingAct(ActionEvent event) {
        if (loginDone && meditationDone && scoutingDone && !trainingDone && trainingCountdown == 0) {
            addExp(TRAINING_XP);
            trainingDone = true;
            updateDialogue("Training complete! Your skills improved. You earned " + TRAINING_XP + " XP!");
            PauseTransition pause = new PauseTransition(Duration.seconds(5));
            pause.setOnFinished(e -> dialogueLabel.setText(""));
              pause.play();
            updateUIState();
            saveUserData();
        }
    }
    
    /**
     * Add experience points and check for level up
     */
    private void addExp(int exp) {
        int previousLevel = currentLevel;
        currentXp += exp;
        
        // Check for level up
        while (currentXp >= xpForNextLevel) {
            currentXp -= xpForNextLevel;
            currentLevel++;
            
            // Update rank and XP threshold based on level
            updateRankAndXpThreshold();
        }
        
        // If level changed, update rank image and play animation
        if (currentLevel != previousLevel) {
            updateRankImage();
            showRankUpAnimation();
            updateDialogue("Congratulations for ranking up! You did great, keep up with the discipline!");
        }
    }
    
    /**
     * Update rank and XP threshold based on current level
     */
    private void updateRankAndXpThreshold() {
        if (currentLevel >= 1 && currentLevel <= 4) {
            currentRank = "Rookie";
            xpForNextLevel = 100;
        } else if (currentLevel >= 5 && currentLevel <= 9) {
            currentRank = "Novice";
            xpForNextLevel = 200;
        } else if (currentLevel >= 10 && currentLevel <= 14) {
            currentRank = "Veteran";
            xpForNextLevel = 300;
        } else if (currentLevel >= 15 && currentLevel <= 19) {
            currentRank = "Elite";
            xpForNextLevel = 400;
        } else if (currentLevel >= 20 && currentLevel <= 25) {
            currentRank = "Master";
            xpForNextLevel = 500;
        }
    }
    
    /**
     * Update UI elements based on current state
     */
    private void updateUIState() {
        // Update rank view and progress
        // Update the rank text if we have a rankTextLabel
        if (rankTextLabel != null) {
            rankTextLabel.setText(currentRank + " Level " + currentLevel + " - XP: " + currentXp + "/" + xpForNextLevel);
        }
        
        // Update login button
        if (dailybtn != null) {
            if (loginDone) {
                dailybtn.setStyle("-fx-background-color: " + COLOR_DARK_GREY);
                dailybtn.setDisable(true);
                if (loginExpLabel != null) {
                    loginExpLabel.setVisible(false);
                }
            } else {
                dailybtn.setStyle("-fx-background-color: " + COLOR_AVAILABLE);
                dailybtn.setDisable(false);
                if (loginExpLabel != null) {
                    loginExpLabel.setText("+" + LOGIN_XP + " XP");
                    loginExpLabel.setVisible(true);
                }
            }
        }
        
        // Update meditation button
        if (meditationBtn != null) {
            if (!loginDone) {
                meditationBtn.setStyle("-fx-background-color: " + COLOR_DARK_GREY);
                meditationBtn.setDisable(true);
                if (meditationExpLabel != null) {
                    meditationExpLabel.setVisible(false);
                }
            } else if (meditationDone) {
                meditationBtn.setStyle("-fx-background-color: " + COLOR_DARK_GREY);
                meditationBtn.setDisable(true);
                if (meditationExpLabel != null) {
                    meditationExpLabel.setVisible(false);
                }
            } else if (meditationCountdown > 0) {
                meditationBtn.setStyle("-fx-background-color: " + COLOR_LIGHT_GREY);
                meditationBtn.setDisable(true);
                int minutes = meditationCountdown / 60;
                int seconds = meditationCountdown % 60;
                meditationBtn.setText("Honing the mind (" + String.format("%d:%02d", minutes, seconds) + ")");
                if (meditationExpLabel != null) {
                    meditationExpLabel.setText("+" + MEDITATION_XP + " XP");
                    meditationExpLabel.setVisible(true);
                }
            } else {
                meditationBtn.setStyle("-fx-background-color: " + COLOR_AVAILABLE);
                meditationBtn.setDisable(false);
                meditationBtn.setText("Honing the mind (Meditate for 5 minutes)");
                if (meditationExpLabel != null) {
                    meditationExpLabel.setText("+" + MEDITATION_XP + " XP");
                    meditationExpLabel.setVisible(true);
                }
            }
        }
        
        // Update scouting button
        if (scoutingMissionBtn != null) {
            if (!meditationDone) {
                scoutingMissionBtn.setStyle("-fx-background-color: " + COLOR_DARK_GREY);
                scoutingMissionBtn.setDisable(true);
                if (scoutExpLabel != null) {
                    scoutExpLabel.setVisible(false);
                }
            } else if (scoutingDone) {
                scoutingMissionBtn.setStyle("-fx-background-color: " + COLOR_DARK_GREY);
                scoutingMissionBtn.setDisable(true);
                if (scoutExpLabel != null) {
                    scoutExpLabel.setVisible(false);
                }
            } else if (scoutingCountdown > 0) {
                scoutingMissionBtn.setStyle("-fx-background-color: " + COLOR_LIGHT_GREY);
                scoutingMissionBtn.setDisable(true);
                int minutes = scoutingCountdown / 60;
                int seconds = scoutingCountdown % 60;
                scoutingMissionBtn.setText("Scouting Mission (" + String.format("%d:%02d", minutes, seconds) + ")");
                if (scoutExpLabel != null) {
                    scoutExpLabel.setText("+" + SCOUTING_XP + " XP");
                    scoutExpLabel.setVisible(true);
                }
            } else {
                scoutingMissionBtn.setStyle("-fx-background-color: " + COLOR_AVAILABLE);
                scoutingMissionBtn.setDisable(false);
                scoutingMissionBtn.setText("Scouting Mission (Take a walk for 1km)");
                if (scoutExpLabel != null) {
                    scoutExpLabel.setText("+" + SCOUTING_XP + " XP");
                    scoutExpLabel.setVisible(true);
                }
            }
        }
        
        // Update training button
        if (trainingGroundsBtn != null) {
            if (!scoutingDone) {
                trainingGroundsBtn.setStyle("-fx-background-color: " + COLOR_DARK_GREY);
                trainingGroundsBtn.setDisable(true);
                if (trainingExpLabel != null) {
                    trainingExpLabel.setVisible(false);
                }
            } else if (trainingDone) {
                trainingGroundsBtn.setStyle("-fx-background-color: " + COLOR_DARK_GREY);
                trainingGroundsBtn.setDisable(true);
                if (trainingExpLabel != null) {
                    trainingExpLabel.setVisible(false);
                }
            } else if (trainingCountdown > 0) {
                trainingGroundsBtn.setStyle("-fx-background-color: " + COLOR_LIGHT_GREY);
                trainingGroundsBtn.setDisable(true);
                int minutes = trainingCountdown / 60;
                int seconds = trainingCountdown % 60;
                trainingGroundsBtn.setText("Training Grounds (" + String.format("%d:%02d", minutes, seconds) + ")");
                if (trainingExpLabel != null) {
                    trainingExpLabel.setText("+" + TRAINING_XP + " XP");
                    trainingExpLabel.setVisible(true);
                }
            } else {
                trainingGroundsBtn.setStyle("-fx-background-color: " + COLOR_AVAILABLE);
                trainingGroundsBtn.setDisable(false);
                trainingGroundsBtn.setText("Training Grounds (Do any forms of exercise)");
                if (trainingExpLabel != null) {
                    trainingExpLabel.setText("+" + TRAINING_XP + " XP");
                    trainingExpLabel.setVisible(true);
                }
            }
        }
        
        // Update rank image
        updateRankImage();
    }
    
    /**
     * Update the rank image based on current level
     */
    private void updateRankImage() {
        if (rankviewlabel == null) {
            System.err.println("WARNING: rankviewlabel is null in updateRankImage()");
            return;
        }
        
        String rankImageFile = "";
        
        if (currentLevel >= 1 && currentLevel <= 4) {
            rankImageFile = "RookieRank.png";
        } else if (currentLevel >= 5 && currentLevel <= 9) {
            rankImageFile = "NoviceRank.png";
        } else if (currentLevel >= 10 && currentLevel <= 14) {
            rankImageFile = "VeteranRank.png";
        } else if (currentLevel >= 15 && currentLevel <= 19) {
            rankImageFile = "EliteRank.png";
        } else if (currentLevel >= 20) {
            rankImageFile = "MasterRank.png";
        }
        
        try {
            File imageFile = new File(IMG_PATH + rankImageFile);
            if (imageFile.exists()) {
                rankviewlabel.setImage(new Image(imageFile.toURI().toString()));
            } else {
                System.err.println("WARNING: Rank image file not found: " + imageFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("ERROR loading rank image: " + e.getMessage());
        }
    }
    
    /**
     * Show animation for ranking up
     */
    private void showRankUpAnimation() {
        if (receptionistimage == null) {
            System.err.println("WARNING: receptionistimage is null in showRankUpAnimation()");
            return;
        }
        
        try {
            // Change receptionist image to happy
            File happyFile = new File(IMG_PATH + "Receptionist_Happy.png");
            if (happyFile.exists()) {
                receptionistimage.setImage(new Image(happyFile.toURI().toString()));
            
                // Create jump animation
                TranslateTransition jump = new TranslateTransition(Duration.millis(300), receptionistimage);
                jump.setByY(-20);
                jump.setCycleCount(2);
                jump.setAutoReverse(true);
                jump.play();
                
                // Reset to idle after animation
                jump.setOnFinished(event -> {
                    // Schedule returning to idle after 3 seconds
                    Timer idleTimer = new Timer(true);
                    idleTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                File idleFile = new File(IMG_PATH + "Receptionist_Idle.png");
                                if (idleFile.exists()) {
                                    receptionistimage.setImage(new Image(idleFile.toURI().toString()));
                                }
                            });
                        }
                    }, 3000);
                });
            } else {
                System.err.println("WARNING: Happy receptionist image not found: " + happyFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("ERROR in showRankUpAnimation: " + e.getMessage());
        }
    }
    
    /**
     * Update dialogue text
     */
    private void updateDialogue(String message) {
        if (dialogueLabel != null) {
            dialogueLabel.setText(message);
        }
    }
    
    /**
     * Start meditation timer
     */
    private void startMeditationTimer() {
        meditationCountdown = MEDITATION_TIMER;
        
        if (meditationTimer != null) {
            meditationTimer.cancel();
        }
        
        meditationTimer = new Timer(true);
        meditationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (meditationCountdown > 0) {
                        meditationCountdown--;
                        updateUIState();
                    } else {
                        // When timer reaches zero, update the UI to make button clickable
                        meditationTimer.cancel();
                        meditationTimer = null;
                        updateDialogue("Meditation time is over. Click the meditation button to claim your XP!");
                        updateUIState();
                    }
                });
            }
        }, 1000, 1000); // Every second
    }
    
    /**
     * Start scouting timer
     */
    private void startScoutingTimer() {
        scoutingCountdown = SCOUTING_TIMER;
        
        if (scoutingTimer != null) {
            scoutingTimer.cancel();
        }
        
        scoutingTimer = new Timer(true);
        scoutingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (scoutingCountdown > 0) {
                        scoutingCountdown--;
                        updateUIState();
                    } else {
                        // When timer reaches zero, update the UI to make button clickable
                        scoutingTimer.cancel();
                        scoutingTimer = null;
                        updateDialogue("Scouting time is over. Click the scouting button to claim your XP!");
                        updateUIState();
                    }
                });
            }
        }, 1000, 1000); // Every second
    }
    
    /**
     * Start training timer
     */
    private void startTrainingTimer() {
        trainingCountdown = TRAINING_TIMER;
        
        if (trainingTimer != null) {
            trainingTimer.cancel();
        }
        
        trainingTimer = new Timer(true);
        trainingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (trainingCountdown > 0) {
                        trainingCountdown--;
                        updateUIState();
                    } else {
                        // When timer reaches zero, update the UI to make button clickable
                        trainingTimer.cancel();
                        trainingTimer = null;
                        updateDialogue("Training time is over. Click the training button to claim your XP!");
                        updateUIState();
                    }
                });
            }
        }, 1000, 1000); // Every second
    }
    
    /**
     * Save user data to JSON file
     */
    private void saveUserData() {
        JSONObject userData = new JSONObject();
        userData.put("currentXp", currentXp);
        userData.put("currentLevel", currentLevel);
        userData.put("currentRank", currentRank);
        userData.put("xpForNextLevel", xpForNextLevel);
        userData.put("lastLoginTime", lastLoginTime != null ? lastLoginTime.toString() : null);
        userData.put("loginDone", loginDone);
        userData.put("meditationDone", meditationDone);
        userData.put("scoutingDone", scoutingDone);
        userData.put("trainingDone", trainingDone);
        
        try (FileWriter file = new FileWriter(DATA_FILE)) {
            file.write(userData.toString());
            file.flush();
        } catch (Exception e) {
            System.err.println("ERROR saving user data: " + e.getMessage());
            updateDialogue("Failed to save progress: " + e.getMessage());
        }
    }
    
    /**
     * Load user data from JSON file
     */
    private void loadUserData() {
        File dataFile = new File(DATA_FILE);
        if (!dataFile.exists()) {
            // First-time user, set up with defaults
            updateRankAndXpThreshold();
            updateDialogue("Welcome, new adventurer! Complete daily tasks to earn XP and ranks.");
            if (receptionistimage != null) {
                try {
                    File idleFile = new File(IMG_PATH + "Receptionist_Idle.png");
                    if (idleFile.exists()) {
                        receptionistimage.setImage(new Image(idleFile.toURI().toString()));
                    } else {
                        System.err.println("WARNING: Idle receptionist image not found: " + idleFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    System.err.println("ERROR loading receptionist image: " + e.getMessage());
                }
            }
            return;
        }
        
        try {
            String content = new String(Files.readAllBytes(Paths.get(DATA_FILE)));
            JSONObject userData = new JSONObject(content);
            
            currentXp = userData.getInt("currentXp");
            currentLevel = userData.getInt("currentLevel");
            currentRank = userData.getString("currentRank");
            xpForNextLevel = userData.getInt("xpForNextLevel");
            
            String lastLoginTimeStr = userData.has("lastLoginTime") ? userData.getString("lastLoginTime") : null;
            lastLoginTime = lastLoginTimeStr != null ? LocalDateTime.parse(lastLoginTimeStr) : null;
            
            loginDone = userData.getBoolean("loginDone");
            meditationDone = userData.getBoolean("meditationDone");
            scoutingDone = userData.getBoolean("scoutingDone");
            trainingDone = userData.getBoolean("trainingDone");
            
            updateDialogue("Welcome back, adventurer! Continue your journey.");
            if (receptionistimage != null) {
                try {
                    File idleFile = new File(IMG_PATH + "Receptionist_Idle.png");
                    if (idleFile.exists()) {
                        receptionistimage.setImage(new Image(idleFile.toURI().toString()));
                    } else {
                        System.err.println("WARNING: Idle receptionist image not found: " + idleFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    System.err.println("ERROR loading receptionist image: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR loading user data: " + e.getMessage());
            updateDialogue("Failed to load progress: " + e.getMessage());
        }
    }
    
    /**
     * Clean up resources when application closes
     */
    public void shutdown() {
        if (autoSaveTimer != null) {
            autoSaveTimer.cancel();
        }
        if (meditationTimer != null) {
            meditationTimer.cancel();
        }
        if (scoutingTimer != null) {
            scoutingTimer.cancel();
        }
        if (trainingTimer != null) {
            trainingTimer.cancel();
        }
        
        // Save final state
        saveUserData();
    }
}