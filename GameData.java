import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * GameData class handles saving and loading of game configuration and high scores
 * Implements persistence functionality using JSON-like format
 */
public class GameData {
    private static final String CONFIG_FILE = "tetris_config.json";
    private static final String SCORES_FILE = "tetris_scores.json";
    private static final String SAVE_FILE_PREFIX = "tetris_save_slot_";
    private static final String SAVE_FILE_SUFFIX = ".json";
    
    // Game save data
    public static class GameSave {
        public int[][] board;
        public int currentPiece;
        public int currentX;
        public int currentY;
        public int currentRotation;
        public int nextPiece;
        public int score;
        public int level;
        public int linesCleared;
        public boolean paused;
        public String saveDate;
        public String playerName;
        
        public GameSave() {}
        
        public GameSave(int[][] board, int currentPiece, int currentX, int currentY, 
                       int currentRotation, int nextPiece, int score, int level, 
                       int linesCleared, boolean paused, String playerName) {
            this.board = new int[board.length][];
            for (int i = 0; i < board.length; i++) {
                this.board[i] = board[i].clone();
            }
            this.currentPiece = currentPiece;
            this.currentX = currentX;
            this.currentY = currentY;
            this.currentRotation = currentRotation;
            this.nextPiece = nextPiece;
            this.score = score;
            this.level = level;
            this.linesCleared = linesCleared;
            this.paused = paused;
            this.playerName = playerName;
            this.saveDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        }
    }
    
    // Configuration data
    public static class Config {
        public int startingLevel = 1;
        public boolean showGhostPiece = true;
        public boolean showNextPiece = true;
        public String gameTheme = "Classic";
        public boolean soundEnabled = true;
        public boolean musicEnabled = true;
        public int musicVolume = 70;
        public int effectsVolume = 80;
        public int aiWinScore = 500;
        
        public Config() {}
        
        public Config(int startingLevel, boolean showGhostPiece, boolean showNextPiece, 
                     String gameTheme, boolean soundEnabled, boolean musicEnabled, 
                     int musicVolume, int effectsVolume, int aiWinScore) {
            this.startingLevel = startingLevel;
            this.showGhostPiece = showGhostPiece;
            this.showNextPiece = showNextPiece;
            this.gameTheme = gameTheme;
            this.soundEnabled = soundEnabled;
            this.musicEnabled = musicEnabled;
            this.musicVolume = musicVolume;
            this.effectsVolume = effectsVolume;
            this.aiWinScore = aiWinScore;
        }
    }
    
    // High score entry
    public static class HighScore {
        public String playerName;
        public int score;
        public int level;
        public int lines;
        public String date;
        public String gameMode; // "Single", "Multiplayer", "AI"
        public String playerType; // "Human", "AI"
        
        public HighScore() {}
        
        public HighScore(String playerName, int score, int level, int lines, 
                        String gameMode, String playerType) {
            this.playerName = playerName;
            this.score = score;
            this.level = level;
            this.lines = lines;
            this.gameMode = gameMode;
            this.playerType = playerType;
            this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        }
    }
    
    /**
     * Save configuration to JSON file
     */
    public static boolean saveConfig(Config config) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE))) {
            writer.println("{");
            writer.println("  \"startingLevel\": " + config.startingLevel + ",");
            writer.println("  \"showGhostPiece\": " + config.showGhostPiece + ",");
            writer.println("  \"showNextPiece\": " + config.showNextPiece + ",");
            writer.println("  \"gameTheme\": \"" + config.gameTheme + "\",");
            writer.println("  \"soundEnabled\": " + config.soundEnabled + ",");
            writer.println("  \"musicEnabled\": " + config.musicEnabled + ",");
            writer.println("  \"musicVolume\": " + config.musicVolume + ",");
            writer.println("  \"effectsVolume\": " + config.effectsVolume + ",");
            writer.println("  \"aiWinScore\": " + config.aiWinScore);
            writer.println("}");
            return true;
        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load configuration from JSON file
     */
    public static Config loadConfig() {
        Config config = new Config(); // Default values
        
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains("startingLevel")) {
                    config.startingLevel = extractIntValue(line);
                } else if (line.contains("showGhostPiece")) {
                    config.showGhostPiece = extractBooleanValue(line);
                } else if (line.contains("showNextPiece")) {
                    config.showNextPiece = extractBooleanValue(line);
                } else if (line.contains("gameTheme")) {
                    config.gameTheme = extractStringValue(line);
                } else if (line.contains("soundEnabled")) {
                    config.soundEnabled = extractBooleanValue(line);
                } else if (line.contains("musicEnabled")) {
                    config.musicEnabled = extractBooleanValue(line);
                } else if (line.contains("musicVolume")) {
                    config.musicVolume = extractIntValue(line);
                } else if (line.contains("effectsVolume")) {
                    config.effectsVolume = extractIntValue(line);
                } else if (line.contains("aiWinScore")) {
                    config.aiWinScore = extractIntValue(line);
                }
            }
        } catch (IOException e) {
            System.out.println("No config file found, using defaults");
        }
        
        return config;
    }
    
    /**
     * Save high scores to JSON file
     */
    public static boolean saveHighScores(List<HighScore> scores) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SCORES_FILE))) {
            writer.println("{");
            writer.println("  \"highScores\": [");
            
            for (int i = 0; i < scores.size(); i++) {
                HighScore score = scores.get(i);
                writer.println("    {");
                writer.println("      \"playerName\": \"" + score.playerName + "\",");
                writer.println("      \"score\": " + score.score + ",");
                writer.println("      \"level\": " + score.level + ",");
                writer.println("      \"lines\": " + score.lines + ",");
                writer.println("      \"date\": \"" + score.date + "\",");
                writer.println("      \"gameMode\": \"" + score.gameMode + "\",");
                writer.println("      \"playerType\": \"" + score.playerType + "\"");
                writer.print("    }");
                if (i < scores.size() - 1) {
                    writer.println(",");
                } else {
                    writer.println();
                }
            }
            
            writer.println("  ]");
            writer.println("}");
            return true;
        } catch (IOException e) {
            System.err.println("Error saving high scores: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load high scores from JSON file
     */
    public static List<HighScore> loadHighScores() {
        List<HighScore> scores = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(SCORES_FILE))) {
            String line;
            HighScore currentScore = null;
            boolean inScoreObject = false;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.equals("{") && !inScoreObject && !line.contains("highScores")) {
                    currentScore = new HighScore();
                    inScoreObject = true;
                } else if (line.equals("}") && inScoreObject) {
                    if (currentScore != null) {
                        scores.add(currentScore);
                    }
                    inScoreObject = false;
                } else if (inScoreObject && currentScore != null) {
                    if (line.contains("playerName")) {
                        currentScore.playerName = extractStringValue(line);
                    } else if (line.contains("\"score\"")) {
                        currentScore.score = extractIntValue(line);
                    } else if (line.contains("\"level\"")) {
                        currentScore.level = extractIntValue(line);
                    } else if (line.contains("\"lines\"")) {
                        currentScore.lines = extractIntValue(line);
                    } else if (line.contains("\"date\"")) {
                        currentScore.date = extractStringValue(line);
                    } else if (line.contains("gameMode")) {
                        currentScore.gameMode = extractStringValue(line);
                    } else if (line.contains("playerType")) {
                        currentScore.playerType = extractStringValue(line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("No high scores file found, starting fresh");
        }
        
        return scores;
    }
    
    /**
     * Add new high score and maintain top 10 list
     */
    public static void addHighScore(String playerName, int score, int level, int lines, 
                                   String gameMode, String playerType) {
        List<HighScore> scores = loadHighScores();
        scores.add(new HighScore(playerName, score, level, lines, gameMode, playerType));
        
        // Sort by score (descending)
        scores.sort((a, b) -> Integer.compare(b.score, a.score));
        
        // Keep only top 10
        if (scores.size() > 10) {
            scores = scores.subList(0, 10);
        }
        
        saveHighScores(scores);
    }
    
    // Helper methods for parsing JSON values
    private static int extractIntValue(String line) {
        String[] parts = line.split(":");
        if (parts.length > 1) {
            String value = parts[1].trim().replaceAll("[,}]", "");
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    private static boolean extractBooleanValue(String line) {
        String[] parts = line.split(":");
        if (parts.length > 1) {
            String value = parts[1].trim().replaceAll("[,}]", "");
            return Boolean.parseBoolean(value);
        }
        return false;
    }
    
    private static String extractStringValue(String line) {
        String[] parts = line.split(":");
        if (parts.length > 1) {
            String value = parts[1].trim().replaceAll("[,}]", "");
            // Remove quotes
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return value.substring(1, value.length() - 1);
            }
            return value;
        }
        return "";
    }
    
    /**
     * Check if high score files exist
     */
    public static boolean configExists() {
        return new File(CONFIG_FILE).exists();
    }
    
    public static boolean scoresExist() {
        return new File(SCORES_FILE).exists();
    }
    
    /**
     * Delete save files (for reset functionality)
     */
    public static boolean deleteConfig() {
        return new File(CONFIG_FILE).delete();
    }
    
    public static boolean deleteScores() {
        return new File(SCORES_FILE).delete();
    }
    
    /**
     * Save game state to specified slot
     */
    public static boolean saveGame(GameSave gameSave, int slot) {
        String fileName = SAVE_FILE_PREFIX + slot + SAVE_FILE_SUFFIX;
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("{");
            writer.println("  \"playerName\": \"" + gameSave.playerName + "\",");
            writer.println("  \"score\": " + gameSave.score + ",");
            writer.println("  \"level\": " + gameSave.level + ",");
            writer.println("  \"linesCleared\": " + gameSave.linesCleared + ",");
            writer.println("  \"currentPiece\": " + gameSave.currentPiece + ",");
            writer.println("  \"currentX\": " + gameSave.currentX + ",");
            writer.println("  \"currentY\": " + gameSave.currentY + ",");
            writer.println("  \"currentRotation\": " + gameSave.currentRotation + ",");
            writer.println("  \"nextPiece\": " + gameSave.nextPiece + ",");
            writer.println("  \"paused\": " + gameSave.paused + ",");
            writer.println("  \"saveDate\": \"" + gameSave.saveDate + "\",");
            
            // Save board state
            writer.println("  \"board\": [");
            for (int i = 0; i < gameSave.board.length; i++) {
                writer.print("    [");
                for (int j = 0; j < gameSave.board[i].length; j++) {
                    writer.print(gameSave.board[i][j]);
                    if (j < gameSave.board[i].length - 1) writer.print(", ");
                }
                writer.print("]");
                if (i < gameSave.board.length - 1) writer.println(",");
                else writer.println();
            }
            writer.println("  ]");
            writer.println("}");
            return true;
        } catch (IOException e) {
            System.err.println("Error saving game to slot " + slot + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load game state from specified slot
     */
    public static GameSave loadGame(int slot) {
        String fileName = SAVE_FILE_PREFIX + slot + SAVE_FILE_SUFFIX;
        GameSave gameSave = new GameSave();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            boolean inBoard = false;
            int boardRow = 0;
            gameSave.board = new int[20][10]; // Default board size
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.contains("\"board\"")) {
                    inBoard = true;
                    continue;
                } else if (inBoard && line.equals("]")) {
                    inBoard = false;
                    continue;
                } else if (inBoard && line.startsWith("[")) {
                    // Parse board row
                    String rowData = line.replaceAll("[\\[\\],]", "").trim();
                    String[] values = rowData.split("\\s+");
                    for (int j = 0; j < values.length && j < 10; j++) {
                        gameSave.board[boardRow][j] = Integer.parseInt(values[j]);
                    }
                    boardRow++;
                } else if (line.contains("playerName")) {
                    gameSave.playerName = extractStringValue(line);
                } else if (line.contains("score")) {
                    gameSave.score = extractIntValue(line);
                } else if (line.contains("level")) {
                    gameSave.level = extractIntValue(line);
                } else if (line.contains("linesCleared")) {
                    gameSave.linesCleared = extractIntValue(line);
                } else if (line.contains("currentPiece")) {
                    gameSave.currentPiece = extractIntValue(line);
                } else if (line.contains("currentX")) {
                    gameSave.currentX = extractIntValue(line);
                } else if (line.contains("currentY")) {
                    gameSave.currentY = extractIntValue(line);
                } else if (line.contains("currentRotation")) {
                    gameSave.currentRotation = extractIntValue(line);
                } else if (line.contains("nextPiece")) {
                    gameSave.nextPiece = extractIntValue(line);
                } else if (line.contains("paused")) {
                    gameSave.paused = extractBooleanValue(line);
                } else if (line.contains("saveDate")) {
                    gameSave.saveDate = extractStringValue(line);
                }
            }
            return gameSave;
        } catch (IOException e) {
            System.err.println("Error loading game from slot " + slot + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if save slot exists
     */
    public static boolean saveSlotExists(int slot) {
        String fileName = SAVE_FILE_PREFIX + slot + SAVE_FILE_SUFFIX;
        return new File(fileName).exists();
    }
    
    /**
     * Delete save slot
     */
    public static boolean deleteSaveSlot(int slot) {
        String fileName = SAVE_FILE_PREFIX + slot + SAVE_FILE_SUFFIX;
        return new File(fileName).delete();
    }
}