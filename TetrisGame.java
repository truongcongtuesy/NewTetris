import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

@SuppressWarnings("unused")
public class TetrisGame extends JFrame implements KeyListener {
    // Game configuration
    
    // Sound and music settings
    private static boolean soundEnabled = true;
    private static boolean musicEnabled = true;
    
    // Game dimensions
    private static int BOARD_WIDTH = 10;
    private static int BOARD_HEIGHT = 20;
    private static final int BLOCK_SIZE = 30;
    
    // AI Settings
    private static final int AI_WIN_SCORE = 500; // AI wins when reaching this score
    
    // Colors
    private static final Color[] COLORS = {
        Color.CYAN, Color.BLUE, Color.ORANGE, Color.YELLOW,
        Color.GREEN, Color.MAGENTA, Color.RED
    };
    
    // Tetris pieces (7 standard pieces) - simplified 2D arrays
    private static final int[][][] PIECES = {
        // I-piece
        {
            {1,1,1,1}
        },
        // O-piece  
        {
            {1,1},
            {1,1}
        },
        // T-piece
        {
            {0,1,0},
            {1,1,1}
        },
        // S-piece
        {
            {0,1,1},
            {1,1,0}
        },
        // Z-piece
        {
            {1,1,0},
            {0,1,1}
        },
        // J-piece
        {
            {1,0,0},
            {1,1,1}
        },
        // L-piece
        {
            {0,0,1},
            {1,1,1}
        }
    };
    
    // Game state
    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private int currentPiece = 0;
    private int currentX = BOARD_WIDTH / 2 - 1;
    private int currentY = 0;
    private int currentRotation = 0;
    private int nextPiece = 0;
    private int score = 0;
    private int level = 1;
    private int linesCleared = 0;
    private boolean gameOver = false;
    private boolean paused = false;
    
    // Home screen state
    private boolean showSplashScreen = true;
    private boolean showHomeScreen = false;
    private boolean showConfigScreen = false;
    private boolean showHighscoreScreen = false;
    private int selectedMenuItem = 0; // 0 = New Game, 1 = Load Game, 2 = Multiplayer, 3 = Online Mode, 4 = Highscore, 5 = Settings, 6 = Exit
    private final String[] menuItems = {"New Game", "Load Game", "Multiplayer", "Online Mode", "Highscore", "Settings", "Exit"};
    
    // Config screen state
    private int selectedConfigItem = 0;
    private final String[] configItems = {
        "Field Width", "Field Height", "Game Level", "Music", 
        "Sound Effect", "Extend Mode", "Back"
    };
    
    // Configuration settings
    private static int startingLevel = 1;
    private static boolean showGhostPiece = true;
    private static boolean showNextPiece = true;
    private static String gameTheme = "Classic";
    private static int aiWinScore = AI_WIN_SCORE; // Configurable AI win score
    
    // Save/Load Game system
    private static boolean showLoadGameScreen = false;
    private static int selectedSaveSlot = 0;
    private static final int MAX_SAVE_SLOTS = 3;
    private static boolean[] saveSlotExists = new boolean[MAX_SAVE_SLOTS];
    
    // Online mode system
    private static boolean isOnlineMode = false;
    private static boolean showOnlineSetup = false;
    private TetrisServer tetrisServer;
    private boolean serverConnected = false;
    private long lastServerMoveTime = 0;
    private int serverMoveDelay = 500; // Delay between server requests
    
    // Multiplayer system
    private static boolean isMultiplayerMode = false;
    private static boolean showPlayerSelection = false;
    private static boolean showNameEntry = false;
    private static int playerSelectionIndex = 0; // 0 = Player 1 Type, 1 = Player 2 Type, 2 = Continue, 3 = Back
    private static String[] playerTypes = {"Human", "AI", "External"};
    private static int player1Type = 0; // 0 = Human, 1 = AI, 2 = External
    private static int player2Type = 0; // 0 = Human, 1 = AI, 2 = External
    private static String player1Name = "";
    private static String player2Name = "";
    private static int nameEntryPlayer = 1; // 1 or 2
    
    // Player 2 game state (for split screen)
    private int[][] board2 = new int[BOARD_HEIGHT][BOARD_WIDTH];
    private int currentPiece2 = 0;
    private int currentX2 = BOARD_WIDTH / 2 - 1;
    private int currentY2 = 0;
    private int currentRotation2 = 0;
    private int nextPiece2 = 0;
    private int score2 = 0;
    private int level2 = 1;
    private int linesCleared2 = 0;
    private boolean gameOver2 = false;
    
    // Sound system
    private SoundManager soundManager;
    private static float musicVolume = 0.7f; // 0.0 to 1.0
    private static float effectsVolume = 0.8f; // 0.0 to 1.0
    
    // Timer for game loop
    private javax.swing.Timer gameTimer;
    private int fallSpeed = 500; // milliseconds
    
    public TetrisGame() {
        setTitle("Tetris Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Load saved configuration
        loadConfiguration();
        
        // Initialize save slots
        checkSaveSlots();
        
        // Initialize game
        initializeGame();
        
        // Set up the display with consistent large size
        Dimension fixedSize = calculateOptimalWindowSize();
        setSize(fixedSize);
        setMinimumSize(fixedSize);
        setLocationRelativeTo(null);
        
        // Add key listener
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        
        // Initialize sound system
        try {
            soundManager = new SoundManager();
            soundManager.setMusicVolume(musicVolume);
            soundManager.setEffectsVolume(effectsVolume);
        } catch (Exception e) {
            System.out.println("Could not initialize sound system: " + e.getMessage());
            soundManager = null;
        }
        
        // Setup splash screen timer (3 seconds)
        javax.swing.Timer splashTimer = new javax.swing.Timer(3000, e -> {
            showSplashScreen = false;
            showHomeScreen = true;
            repaint();
            
            // Start menu music
            if (musicEnabled && soundManager != null) {
                soundManager.playBackgroundMusic("menu", musicVolume);
            }
        });
        splashTimer.setRepeats(false);
        splashTimer.start();
        
        // Don't start game timer immediately - wait for user to select "Play Game"
        fallSpeed = 500;
        
        // Initialize AI players
        aiPlayer1 = new AIPlayer(1);
        aiPlayer2 = new AIPlayer(2);
        
        // Initialize TetrisServer for online mode
        tetrisServer = new TetrisServer();
    }
    
    private void initializeGame() {
        // Clear board
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            Arrays.fill(board[i], 0);
        }
        
        // Initialize first pieces
        currentPiece = (int)(Math.random() * PIECES.length);
        nextPiece = (int)(Math.random() * PIECES.length);
        
        // Reset position
        currentX = BOARD_WIDTH / 2 - 1;
        currentY = 0;
        currentRotation = 0;
        
        // Reset game state
        score = 0;
        level = startingLevel; // Use config setting
        linesCleared = 0;
        gameOver = false;
        paused = false;
    }
    
    private void initializeMultiplayerGame() {
        // Clear both boards
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            Arrays.fill(board[i], 0);
            Arrays.fill(board2[i], 0);
        }
        
        // Initialize pieces for Player 1
        currentPiece = (int)(Math.random() * PIECES.length);
        nextPiece = (int)(Math.random() * PIECES.length);
        currentX = BOARD_WIDTH / 2 - 1;
        currentY = 0;
        currentRotation = 0;
        
        // Initialize pieces for Player 2
        currentPiece2 = (int)(Math.random() * PIECES.length);
        nextPiece2 = (int)(Math.random() * PIECES.length);
        currentX2 = BOARD_WIDTH / 2 - 1;
        currentY2 = 0;
        currentRotation2 = 0;
        
        // Reset game state for both players
        score = 0;
        score2 = 0;
        level = startingLevel;
        level2 = startingLevel;
        linesCleared = 0;
        linesCleared2 = 0;
        gameOver = false;
        gameOver2 = false;
        paused = false;
    }
    
    private void resetGameBoardsWithNewDimensions() {
        // Recreate boards with new dimensions
        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        board2 = new int[BOARD_HEIGHT][BOARD_WIDTH];
        
        // Reset positions to center of new board
        currentX = BOARD_WIDTH / 2 - 1;
        currentX2 = BOARD_WIDTH / 2 - 1;
        currentY = 0;
        currentY2 = 0;
        
        // Update window size to accommodate new board dimensions
        SwingUtilities.invokeLater(() -> {
            centerWindow();
            repaint();
        });
    }
    
    private void gameStep() {
        if (showHomeScreen || showConfigScreen || gameOver || paused) return;
        
        // AI move for single player if AI is selected
        if (player1Type == 1 && aiPlayer1 != null) { // AI player
            aiPlayer1.makeMove();
        }
        
        // Move piece down
        if (canMove(currentX, currentY + 1, currentRotation)) {
            currentY++;
        } else {
            // Place piece on board
            placePiece();
            
            // Check for completed lines
            clearLines();
            
            // Check if AI reached win score
            if (player1Type == 1 && score >= aiWinScore) { // AI reached target points
                gameOver = true;
                gameTimer.stop();
                showAIWinDialog();
                return;
            }
            
            // Spawn next piece
            spawnNextPiece();
            
            // Check game over
            if (!canMove(currentX, currentY, currentRotation)) {
                gameOver = true;
                gameTimer.stop();
                showGameOverDialog();
            }
        }
        
        // Only repaint once per game step
        repaint();
    }
    
    private void multiplayerGameStep() {
        if (showHomeScreen || showConfigScreen || paused) return;
        if (gameOver && gameOver2) return;

        // Player 1 step
        if (!gameOver) {
            // AI move for player 1 if AI is selected
            if (player1Type == 1 && aiPlayer1 != null) {
                aiPlayer1.makeMove();
            }

            if (canMove(currentX, currentY + 1, currentRotation)) {
                currentY++;
            } else {
                placePiece();
                clearLines();

                // One unified win check (human or AI)
                if (score >= aiWinScore) {
                    endMultiplayerOnScoreWin(1, player1Type == 1);
                    return;
                }

                spawnNextPiece();
                if (!canMove(currentX, currentY, currentRotation)) {
                    gameOver = true;
                }
            }
        }

        // Player 2 step
        if (!gameOver2) {
            // AI move for player 2 if AI is selected
            if (player2Type == 1 && aiPlayer2 != null) {
                aiPlayer2.makeMove();
            }

            if (canMove2(currentX2, currentY2 + 1, currentRotation2)) {
                currentY2++;
            } else {
                placePiece2();
                clearLines2();

                // One unified win check (human or AI)
                if (score2 >= aiWinScore) {
                    endMultiplayerOnScoreWin(2, player2Type == 1);
                    return;
                }

                spawnNextPiece2();
                if (!canMove2(currentX2, currentY2, currentRotation2)) {
                    gameOver2 = true;
                }
            }
        }
        
        // Check if both players are finished
        if (gameOver && gameOver2) {
            gameTimer.stop();
            showMultiplayerGameOverDialog();
        }
        
        repaint();
    }
    
    private boolean canMove(int x, int y, int rotation) {
        int[][] piece = rotatePiece(PIECES[currentPiece], rotation);
        
        for (int py = 0; py < piece.length; py++) {
            for (int px = 0; px < piece[py].length; px++) {
                if (piece[py][px] == 1) {
                    int newX = x + px;
                    int newY = y + py;
                    
                    // Check boundaries
                    if (newX < 0 || newX >= BOARD_WIDTH || newY >= BOARD_HEIGHT) {
                        return false;
                    }
                    
                    // Check collision with placed pieces
                    if (newY >= 0 && board[newY][newX] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private void placePiece() {
        int[][] piece = rotatePiece(PIECES[currentPiece], currentRotation);
        
        for (int py = 0; py < piece.length; py++) {
            for (int px = 0; px < piece[py].length; px++) {
                if (piece[py][px] == 1) {
                    int newX = currentX + px;
                    int newY = currentY + py;
                    
                    if (newY >= 0 && newY < BOARD_HEIGHT && newX >= 0 && newX < BOARD_WIDTH) {
                        board[newY][newX] = currentPiece + 1;
                    }
                }
            }
        }
    }
    
    private void clearLines() {
        int linesRemoved = 0;
        
        for (int y = BOARD_HEIGHT - 1; y >= 0; y--) {
            boolean fullLine = true;
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] == 0) {
                    fullLine = false;
                    break;
                }
            }
            
            if (fullLine) {
                // Remove the line
                for (int moveY = y; moveY > 0; moveY--) {
                    System.arraycopy(board[moveY - 1], 0, board[moveY], 0, BOARD_WIDTH);
                }
                Arrays.fill(board[0], 0);
                
                linesRemoved++;
                y++; // Check same line again
            }
        }
        
        if (linesRemoved > 0) {
            // Update score and level
            linesCleared += linesRemoved;
            score += calculateScore(linesRemoved);
            level = Math.min(linesCleared / 10 + 1, 20);
            
            // Play sound effect for line clear
            playSound("clear");
            
            // Increase speed
            fallSpeed = Math.max(50, 500 - (level - 1) * 25);
            gameTimer.setDelay(fallSpeed);
        }
    }
    
    private int calculateScore(int lines) {
        int[] lineScores = {0, 40, 100, 300, 1200};
        return lineScores[lines] * level;
    }
    
    private void spawnNextPiece() {
        currentPiece = nextPiece;
        nextPiece = (int)(Math.random() * PIECES.length);
        currentX = BOARD_WIDTH / 2 - 1;
        currentY = 0;
        currentRotation = 0;
    }
    
    // Player 2 methods
    private boolean canMove2(int x, int y, int rotation) {
        int[][] piece = rotatePiece(PIECES[currentPiece2], rotation);
        
        for (int py = 0; py < piece.length; py++) {
            for (int px = 0; px < piece[py].length; px++) {
                if (piece[py][px] == 1) {
                    int newX = x + px;
                    int newY = y + py;
                    
                    // Check boundaries
                    if (newX < 0 || newX >= BOARD_WIDTH || newY >= BOARD_HEIGHT) {
                        return false;
                    }
                    
                    // Check collision with existing pieces
                    if (newY >= 0 && board2[newY][newX] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private void placePiece2() {
        int[][] piece = rotatePiece(PIECES[currentPiece2], currentRotation2);
        
        for (int py = 0; py < piece.length; py++) {
            for (int px = 0; px < piece[py].length; px++) {
                if (piece[py][px] == 1) {
                    int newX = currentX2 + px;
                    int newY = currentY2 + py;
                    
                    if (newY >= 0 && newY < BOARD_HEIGHT && newX >= 0 && newX < BOARD_WIDTH) {
                        board2[newY][newX] = currentPiece2 + 1;
                    }
                }
            }
        }
    }
    
    private void clearLines2() {
        int linesRemoved = 0;
        
        for (int y = BOARD_HEIGHT - 1; y >= 0; y--) {
            boolean fullLine = true;
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board2[y][x] == 0) {
                    fullLine = false;
                    break;
                }
            }
            
            if (fullLine) {
                // Remove the line
                for (int moveY = y; moveY > 0; moveY--) {
                    System.arraycopy(board2[moveY - 1], 0, board2[moveY], 0, BOARD_WIDTH);
                }
                Arrays.fill(board2[0], 0);
                
                linesRemoved++;
                y++; // Check same line again
            }
        }
        
        // Update score and level for Player 2
        if (linesRemoved > 0) {
            int[] linePoints = {0, 100, 300, 500, 800};
            score2 += linePoints[linesRemoved] * level2;
            linesCleared2 += linesRemoved;
            
            // Level up every 10 lines
            if (linesCleared2 / 10 >= level2) {
                level2++;
            }
            
            playSound("clear");
        }
    }
    
    private void spawnNextPiece2() {
        currentPiece2 = nextPiece2;
        nextPiece2 = (int)(Math.random() * PIECES.length);
        currentX2 = BOARD_WIDTH / 2 - 1;
        currentY2 = 0;
        currentRotation2 = 0;
    }
    
    private void showMultiplayerGameOverDialog() {
        // Determine winner
        String winner;
        if (!gameOver && gameOver2) {
            winner = player1Name + " Wins!";
        } else if (gameOver && !gameOver2) {
            winner = player2Name + " Wins!";
        } else {
            // Both game over, compare scores
            if (score > score2) {
                winner = player1Name + " Wins!";
            } else if (score2 > score) {
                winner = player2Name + " Wins!";
            } else {
                winner = "It's a Tie!";
            }
        }
        
        String message = winner + "\n\n" +
                        player1Name + " - Score: " + score + ", Lines: " + linesCleared + "\n" +
                        player2Name + " - Score: " + score2 + ", Lines: " + linesCleared2 + "\n\n" +
                        "Press R to play again or ESC to return to menu";
        
        // For now, just print to console (can be replaced with actual dialog)
        System.out.println(message);
    }
    
    private int[][] rotatePiece(int[][] piece, int rotation) {
        int[][] result = piece;
        
        for (int i = 0; i < rotation % 4; i++) {
            int rows = result.length;
            int cols = result[0].length;
            int[][] temp = new int[cols][rows];
            
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    temp[x][rows - 1 - y] = result[y][x];
                }
            }
            result = temp;
        }
        
        return result;
    }
    
    // Override update to prevent automatic clearing and reduce flicker
    @Override
    public void update(Graphics g) {
        paint(g);
    }
    
    @Override
    public void paint(Graphics g) {
        // Create off-screen image for double buffering
        Dimension size = getSize();
        Image offScreen = createImage(size.width, size.height);
        Graphics2D offGraphics = (Graphics2D) offScreen.getGraphics();
        
        // Clear the off-screen buffer
        offGraphics.setColor(getBackground());
        offGraphics.fillRect(0, 0, size.width, size.height);
        
        offGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (showSplashScreen) {
            drawSplashScreen(offGraphics);
        } else if (showHomeScreen) {
            drawHomeScreen(offGraphics);
        } else if (showLoadGameScreen) {
            drawLoadGameScreen(offGraphics);
        } else if (showConfigScreen) {
            drawConfigScreen(offGraphics);
        } else if (showHighscoreScreen) {
            drawHighscoreScreen(offGraphics);
        } else if (showPlayerSelection) {
            drawPlayerSelectionScreen(offGraphics);
        } else if (showNameEntry) {
            drawNameEntryScreen(offGraphics);
        } else {
            if (isMultiplayerMode) {
                // Draw split-screen multiplayer
                drawMultiplayerGame(offGraphics);
            } else {
                // Draw board
                drawBoard(offGraphics);
                
                // Draw current piece
                if (!gameOver) {
                    drawCurrentPiece(offGraphics);
                    
                    // Draw ghost piece if enabled in config
                    if (showGhostPiece) {
                        drawGhostPiece(offGraphics);
                    }
                }
                
                // Draw UI for single player and online mode
                if (isOnlineMode) {
                    drawOnlineUI(offGraphics);
                } else {
                    drawUI(offGraphics);
                }
            }
        }
        
        // Draw the off-screen image to the main graphics
        g.drawImage(offScreen, 0, 0, this);
        offGraphics.dispose();
    }
    
    private void drawSplashScreen(Graphics2D g) {
        // Clear background with gradient
        GradientPaint gradient = new GradientPaint(0, 0, new Color(25, 25, 112), 
                                                  0, getHeight(), new Color(0, 0, 139));
        g.setPaint(gradient);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw main title
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 56));
        FontMetrics fm = g.getFontMetrics();
        String title = "TETRIS GAME";
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g.drawString(title, titleX, 120);
        
        // Draw group information
        g.setFont(new Font("Arial", Font.BOLD, 24));
        fm = g.getFontMetrics();
        String groupInfo = "Group Information";
        int groupX = (getWidth() - fm.stringWidth(groupInfo)) / 2;
        g.drawString(groupInfo, groupX, 200);
        
        // Draw student information - Group members
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        fm = g.getFontMetrics();
        
        String[] studentInfo = {
            "GROUP MEMBERS:",
            "",
            "Name: Cong Tue Sy Truong",
            "Student ID: s53997930",
            "",
            "Name: Jay Hasovic", 
            "Student ID: s5176151",
            "",
            "Name: Nicholas Sialepis",
            "Student ID: s5304788",
            "",
            "Course: 2006ICT - Object Oriented Software Development",
            "Assignment: Tetris Game Project"
        };
        
        int startY = 220;
        for (int i = 0; i < studentInfo.length; i++) {
            if (studentInfo[i].equals("GROUP MEMBERS:")) {
                // Make group header bold and larger
                g.setFont(new Font("Arial", Font.BOLD, 18));
                g.setColor(Color.YELLOW);
            } else if (studentInfo[i].startsWith("Name:")) {
                // Make names bold and white
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.setColor(Color.WHITE);
            } else if (studentInfo[i].startsWith("Student ID:")) {
                // Make student IDs normal and light blue
                g.setFont(new Font("Arial", Font.PLAIN, 14));
                g.setColor(Color.CYAN);
            } else if (studentInfo[i].startsWith("Course:") || studentInfo[i].startsWith("Assignment:")) {
                // Make course info italic and light gray
                g.setFont(new Font("Arial", Font.ITALIC, 14));
                g.setColor(Color.LIGHT_GRAY);
            } else {
                // Default formatting for empty lines
                g.setFont(new Font("Arial", Font.PLAIN, 16));
                g.setColor(Color.WHITE);
            }
            
            if (!studentInfo[i].isEmpty()) {
                fm = g.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(studentInfo[i])) / 2;
                g.drawString(studentInfo[i], textX, startY + (i * 22));
            }
        }
        
        // Draw university/institution name
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        fm = g.getFontMetrics();
        String university = "Griffith University";
        int uniX = (getWidth() - fm.stringWidth(university)) / 2;
        g.drawString(university, uniX, 520);
        
        // Draw loading indicator
        g.setFont(new Font("Arial", Font.ITALIC, 14));
        g.setColor(Color.LIGHT_GRAY);
        fm = g.getFontMetrics();
        String loading = "Loading...";
        int loadingX = (getWidth() - fm.stringWidth(loading)) / 2;
        g.drawString(loading, loadingX, 550);
        
        // Draw copyright
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(Color.GRAY);
        fm = g.getFontMetrics();
        String copyright = "© 2025 - Java Programming Assignment";
        int copyrightX = (getWidth() - fm.stringWidth(copyright)) / 2;
        g.drawString(copyright, copyrightX, 580);
    }
    
    private void drawHomeScreen(Graphics2D g) {
        // Clear background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw title
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g.getFontMetrics();
        String title = "TETRIS";
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g.drawString(title, titleX, 150);
        
        // Draw subtitle
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        fm = g.getFontMetrics();
        String subtitle = "Classic Block Puzzle Game";
        int subtitleX = (getWidth() - fm.stringWidth(subtitle)) / 2;
        g.drawString(subtitle, subtitleX, 180);
        
        // Draw menu items
        g.setFont(new Font("Arial", Font.BOLD, 24));
        fm = g.getFontMetrics();
        
        for (int i = 0; i < menuItems.length; i++) {
            // Check if Load Game should be disabled
            boolean isLoadDisabled = (i == 1) && !hasAnySaveFiles();
            
            // Highlight selected item
            if (i == selectedMenuItem) {
                g.setColor(isLoadDisabled ? Color.GRAY : Color.YELLOW);
                g.drawString("> " + menuItems[i] + " <", 
                           (getWidth() - fm.stringWidth("> " + menuItems[i] + " <")) / 2, 
                           280 + i * 60);
            } else {
                g.setColor(isLoadDisabled ? Color.DARK_GRAY : Color.WHITE);
                g.drawString(menuItems[i], 
                           (getWidth() - fm.stringWidth(menuItems[i])) / 2, 
                           280 + i * 60);
            }
        }
        
        // Draw instructions (positioned below menu items with proper spacing)
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(Color.LIGHT_GRAY);
        fm = g.getFontMetrics();
        
        // Calculate starting position based on menu items
        int menuEndY = 280 + (menuItems.length - 1) * 60; // Last menu item position
        int instructionStartY = menuEndY + 80; // Add space after menu
        
        String[] instructions = {
            "Use UP/DOWN arrows to navigate",
            "Press ENTER to select",
            "Single Player: WASD controls + SPACE (hard drop)",
            "Multiplayer: P1(WASD) vs P2(Arrow Keys) | M(music) P(pause)"
        };
        
        for (int i = 0; i < instructions.length; i++) {
            g.drawString(instructions[i], 
                       (getWidth() - fm.stringWidth(instructions[i])) / 2, 
                       instructionStartY + i * 18);
        }
    }
    
    private void drawConfigScreen(Graphics2D g) {
        // Enhanced config screen layout for large window
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Apply modern styling
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw main title with background
        g.setColor(new Color(70, 130, 180));
        g.fillRoundRect(50, 20, getWidth() - 100, 60, 15, 15);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        String title = "⚙️ Configuration";
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g.drawString(title, titleX, 55);
        
        // Create two-column layout
        int col1X = 80;
        int col2X = getWidth()/2 + 40;
        int startY = 140;
        int itemHeight = 70;
        int panelWidth = getWidth()/2 - 120;
        
        // Left column panel
        g.setColor(new Color(255, 255, 255, 220));
        g.fillRoundRect(col1X - 20, startY - 30, panelWidth, 350, 15, 15);
        g.setColor(new Color(100, 100, 100));
        g.drawRoundRect(col1X - 20, startY - 30, panelWidth, 350, 15, 15);
        
        // Right column panel
        g.setColor(new Color(255, 255, 255, 220));
        g.fillRoundRect(col2X - 20, startY - 30, panelWidth, 350, 15, 15);
        g.setColor(new Color(100, 100, 100));
        g.drawRoundRect(col2X - 20, startY - 30, panelWidth, 350, 15, 15);
        
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.BLACK);
        
        // Left column - Game Settings
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("🎮 Game Settings", col1X, startY - 5);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Field Width
        g.drawString("Field Width (cells):", col1X, startY + 35);
        drawSlider(g, col1X + 20, startY + 40, BOARD_WIDTH, 5, 15, 0 == selectedConfigItem);
        g.drawString(String.valueOf(BOARD_WIDTH), col1X + 200, startY + 55);
        
        // Field Height
        g.drawString("Field Height (cells):", col1X, startY + itemHeight + 10);
        drawSlider(g, col1X + 20, startY + itemHeight + 15, BOARD_HEIGHT, 15, 30, 1 == selectedConfigItem);
        g.drawString(String.valueOf(BOARD_HEIGHT), col1X + 200, startY + itemHeight + 30);
        
        // Game Level
        g.drawString("Starting Level:", col1X, startY + itemHeight * 2 - 15);
        drawSlider(g, col1X + 20, startY + itemHeight * 2 - 10, startingLevel, 1, 10, 2 == selectedConfigItem);
        g.drawString(String.valueOf(startingLevel), col1X + 200, startY + itemHeight * 2 + 5);
        
        // Right column - Audio & Display Settings
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("🎵 Audio & Display", col2X, startY - 5);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Music checkbox
        g.drawString("Background Music:", col2X, startY + 35);
        drawCheckbox(g, col2X + 20, startY + 40, musicEnabled, 3 == selectedConfigItem);
        g.drawString(musicEnabled ? "ON" : "OFF", col2X + 60, startY + 55);
        
        // Sound Effects checkbox
        g.drawString("Sound Effects:", col2X, startY + itemHeight + 10);
        drawCheckbox(g, col2X + 20, startY + itemHeight + 15, soundEnabled, 4 == selectedConfigItem);
        g.drawString(soundEnabled ? "ON" : "OFF", col2X + 60, startY + itemHeight + 30);
        
        // Extend Mode checkbox
        g.drawString("Extend Mode (Ghost Piece):", col2X, startY + itemHeight * 2 - 15);
        drawCheckbox(g, col2X + 20, startY + itemHeight * 2 - 10, showGhostPiece, 5 == selectedConfigItem);
        g.drawString(showGhostPiece ? "ON" : "OFF", col2X + 60, startY + itemHeight * 2 + 5);
        
        // Control instructions panel
        int instructY = startY + 340;
        g.setColor(new Color(245, 245, 245));
        g.fillRoundRect(50, instructY, getWidth() - 100, 80, 15, 15);
        g.setColor(new Color(100, 100, 100));
        g.drawRoundRect(50, instructY, getWidth() - 100, 80, 15, 15);
        
        // Instructions
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.setColor(new Color(60, 60, 60));
        int instY = instructY + 25;
        g.drawString("🎮 Navigation: UP/DOWN (navigate), LEFT/RIGHT (change), ENTER (select)", 70, instY);
        g.drawString("🔍 Extend Mode: Shows ghost piece preview at drop position", 70, instY + 20);
        
        // Back button - centered
        int backY = instructY + 100;
        int buttonWidth = 120;
        int buttonX = (getWidth() - buttonWidth) / 2;
        drawButton(g, buttonX, backY, buttonWidth, 35, "🏠 Back to Menu", 6 == selectedConfigItem);
        
        // Reset antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    
    // Helper methods for drawing UI components
    private void drawSlider(Graphics2D g, int x, int y, int value, int min, int max, boolean selected) {
        int sliderWidth = 200;
        int sliderHeight = 20;
        
        // Draw slider track
        g.setColor(selected ? new Color(100, 150, 255) : Color.LIGHT_GRAY);
        g.fillRoundRect(x, y, sliderWidth, sliderHeight, 10, 10);
        g.setColor(Color.GRAY);
        g.drawRoundRect(x, y, sliderWidth, sliderHeight, 10, 10);
        
        // Calculate thumb position
        float ratio = (float)(value - min) / (max - min);
        int thumbX = x + (int)(ratio * (sliderWidth - 20));
        
        // Draw thumb
        g.setColor(selected ? new Color(50, 100, 200) : Color.DARK_GRAY);
        g.fillOval(thumbX, y - 2, 20, sliderHeight + 4);
        g.setColor(Color.BLACK);
        g.drawOval(thumbX, y - 2, 20, sliderHeight + 4);
    }
    
    private void drawCheckbox(Graphics2D g, int x, int y, boolean checked, boolean selected) {
        int size = 20;
        
        // Draw checkbox border
        g.setColor(selected ? new Color(100, 150, 255) : Color.GRAY);
        g.fillRect(x, y, size, size);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, size, size);
        
        // Draw checkmark if checked
        if (checked) {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(3));
            g.drawLine(x + 4, y + 10, x + 8, y + 14);
            g.drawLine(x + 8, y + 14, x + 16, y + 6);
            g.setStroke(new BasicStroke(1));
        }
    }
    
    private void drawButton(Graphics2D g, int x, int y, int width, int height, String text, boolean selected) {
        // Draw button background
        g.setColor(selected ? new Color(100, 150, 255) : new Color(220, 220, 220));
        g.fillRoundRect(x, y, width, height, 10, 10);
        g.setColor(Color.BLACK); // Strong black border
        g.drawRoundRect(x, y, width, height, 10, 10);
        
        // Draw button text
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.BLACK); // Strong black text
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        int textY = y + (height + fm.getAscent()) / 2 - 2;
        g.drawString(text, textX, textY);
    }
    
    private void drawPlayerSelectionScreen(Graphics2D g) {
        g.setColor(getThemeBackgroundColor());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(getThemeTextColor());
        g.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g.getFontMetrics();
        
        // Title
        String title = "Multiplayer Setup";
        g.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, 100);
        
        // Player setup
        g.setFont(new Font("Arial", Font.BOLD, 24));
        fm = g.getFontMetrics();
        
        String[] options = {
            "Player 1 Type: < " + playerTypes[player1Type] + " >",
            "Player 2 Type: < " + playerTypes[player2Type] + " >",
            "Continue",
            "Back to Home"
        };
        
        for (int i = 0; i < options.length; i++) {
            if (i == playerSelectionIndex) {
                g.setColor(Color.YELLOW);
                g.fillRect(50, 200 + i * 60 - 5, getWidth() - 100, 50);
                g.setColor(Color.BLACK);
            } else {
                g.setColor(getThemeTextColor());
            }
            g.drawString(options[i], (getWidth() - fm.stringWidth(options[i])) / 2, 230 + i * 60);
        }
        
        // Instructions
        g.setColor(getThemeTextColor());
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        fm = g.getFontMetrics();
        String[] instructions = {
            "Use UP/DOWN to navigate, LEFT/RIGHT to change types",
            "ENTER to continue, ESC to go back"
        };
        
        for (int i = 0; i < instructions.length; i++) {
            g.drawString(instructions[i], 
                       (getWidth() - fm.stringWidth(instructions[i])) / 2, 
                       500 + i * 20);
        }
    }
    
    private void drawNameEntryScreen(Graphics2D g) {
        g.setColor(getThemeBackgroundColor());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(getThemeTextColor());
        g.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g.getFontMetrics();
        
        // Title
        String title = "Enter Names";
        g.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, 100);
        
        // Player info
        g.setFont(new Font("Arial", Font.BOLD, 24));
        fm = g.getFontMetrics();
        
        String prompt = "Enter name for Player " + nameEntryPlayer + " (" + playerTypes[nameEntryPlayer == 1 ? player1Type : player2Type] + "):";
        g.drawString(prompt, (getWidth() - fm.stringWidth(prompt)) / 2, 200);
        
        // Name input box
        String currentName = (nameEntryPlayer == 1) ? player1Name : player2Name;
        g.setColor(Color.WHITE);
        g.fillRect(100, 250, getWidth() - 200, 50);
        g.setColor(Color.BLACK);
        g.drawRect(100, 250, getWidth() - 200, 50);
        
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        fm = g.getFontMetrics();
        g.drawString(currentName + "_", 110, 280);
        
        // Progress indicator
        g.setColor(getThemeTextColor());
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        fm = g.getFontMetrics();
        String progress = "Step " + nameEntryPlayer + " of 2";
        g.drawString(progress, (getWidth() - fm.stringWidth(progress)) / 2, 350);
        
        // Already entered name
        if (nameEntryPlayer == 2 && !player1Name.isEmpty()) {
            String player1Info = "Player 1: " + player1Name + " (" + playerTypes[player1Type] + ")";
            g.drawString(player1Info, (getWidth() - fm.stringWidth(player1Info)) / 2, 380);
        }
        
        // Instructions
        String[] instructions = {
            "Type the player name (max 15 characters)",
            "ENTER to continue, ESC to go back, BACKSPACE to delete"
        };
        
        for (int i = 0; i < instructions.length; i++) {
            g.drawString(instructions[i], 
                       (getWidth() - fm.stringWidth(instructions[i])) / 2, 
                       450 + i * 20);
        }
    }
    
    private void drawMultiplayerGame(Graphics2D g) {
        int width = getWidth();
        int height = getHeight();
        
        // Define layout dimensions - more conservative
        int infoWidth = 140;  // Reduce info panel width
        int boardWidth = BOARD_WIDTH * BLOCK_SIZE; // 300 pixels
        int boardHeight = BOARD_HEIGHT * BLOCK_SIZE; // 600 pixels
        int minSpacing = 10; // Minimum spacing
        
        // Calculate if we have enough space for 2 boards + 2 info panels
        int requiredWidth = (2 * infoWidth) + (2 * boardWidth) + (4 * minSpacing);
        
        if (width < requiredWidth) {
            // If window too small, reduce info width further
            infoWidth = Math.max(100, (width - (2 * boardWidth) - (4 * minSpacing)) / 2);
        }
        
        // Calculate positions with guaranteed fit
        int spacing = Math.max(5, (width - (2 * infoWidth) - (2 * boardWidth)) / 4);
        int player1BoardX = infoWidth + spacing;
        int player2BoardX = player1BoardX + boardWidth + spacing;
        int boardY = Math.max(80, (height - boardHeight) / 4);
        
        // Debug - log positions to console
        System.out.println("Window: " + width + "x" + height);
        System.out.println("Board1 X: " + player1BoardX + ", Board2 X: " + player2BoardX);
        System.out.println("Board width: " + boardWidth + ", Required width: " + requiredWidth);
        
        // Draw background with gradient
        GradientPaint gradient = new GradientPaint(0, 0, getThemeBackgroundColor(), 
                                                  width, height, getThemeAccentColor());
        g.setPaint(gradient);
        g.fillRect(0, 0, width, height);
        
        // Draw title first
        g.setColor(getThemeTextColor());
        g.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics titleFm = g.getFontMetrics();
        String title = "MULTIPLAYER MODE";
        g.drawString(title, (width - titleFm.stringWidth(title)) / 2, 40);
        
        // Draw Player 1 info panel (left side)
        drawPlayerInfoPanel(g, spacing, 60, infoWidth - spacing, height - 80, 
                           player1Name, playerTypes[player1Type], score, level, linesCleared, 1, !gameOver);
        
        // Draw Player 2 info panel (right side) - ensure it fits
        int panel2X = Math.min(width - infoWidth, player2BoardX + boardWidth + spacing);
        drawPlayerInfoPanel(g, panel2X, 60, infoWidth - spacing, height - 80, 
                           player2Name, playerTypes[player2Type], score2, level2, linesCleared2, 2, !gameOver2);
        
        // Draw board backgrounds with borders - ensure Player 2 board is visible
        g.setColor(Color.BLACK);
        g.fillRect(player1BoardX - 2, boardY - 2, boardWidth + 4, boardHeight + 4);
        
        // Only draw Player 2 board if it fits in window
        if (player2BoardX + boardWidth > width - 10) {
            // Move Player 2 board to fit
            player2BoardX = width - boardWidth - 10;
            System.out.println("Adjusted Board2 X to: " + player2BoardX);
        }
        g.fillRect(player2BoardX - 2, boardY - 2, boardWidth + 4, boardHeight + 4);
        
        // Draw board borders
        g.setColor(getThemeTextColor());
        g.setStroke(new BasicStroke(2));
        g.drawRect(player1BoardX - 2, boardY - 2, boardWidth + 4, boardHeight + 4);
        g.drawRect(player2BoardX - 2, boardY - 2, boardWidth + 4, boardHeight + 4);
        
        // Draw Player 1 board
        drawBoardAtPosition(g, player1BoardX, boardY, board, false);
        if (!gameOver) {
            if (showGhostPiece) {
                drawGhostPieceAtPosition(g, player1BoardX, boardY, currentPiece, currentX, currentY, currentRotation, false);
            }
            drawCurrentPieceAtPosition(g, player1BoardX, boardY, currentPiece, currentX, currentY, currentRotation, false);
        }
        
        // Draw Player 2 board  
        drawBoardAtPosition(g, player2BoardX, boardY, board2, true);
        if (!gameOver2) {
            if (showGhostPiece) {
                drawGhostPieceAtPosition(g, player2BoardX, boardY, currentPiece2, currentX2, currentY2, currentRotation2, true);
            }
            drawCurrentPieceAtPosition(g, player2BoardX, boardY, currentPiece2, currentX2, currentY2, currentRotation2, true);
        }
        
        // Draw center divider line (optional, more subtle)
        int centerX = width / 2;
        g.setColor(new Color(getThemeTextColor().getRed(), getThemeTextColor().getGreen(), 
                            getThemeTextColor().getBlue(), 60)); // Semi-transparent
        g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5}, 0));
        g.drawLine(centerX, 70, centerX, height - 20);
        
        // Draw winner message if both games are over
        if (gameOver && gameOver2) {
            drawMultiplayerWinner(g);
        }
    }
    
    private void drawPlayerInfoPanel(Graphics2D g, int x, int y, int width, int height, 
                                    String playerName, String playerType, int playerScore, 
                                    int playerLevel, int playerLines, int playerNum, boolean isActive) {
        // Draw panel border
        g.setColor(isActive ? Color.GREEN : Color.RED);
        g.setStroke(new BasicStroke(3));
        g.drawRect(x, y, width, height);
        
        // Draw panel background
        g.setColor(isActive ? new Color(0, 40, 0, 100) : new Color(40, 0, 0, 100));
        g.fillRect(x + 2, y + 2, width - 4, height - 4);
        
        // Draw player info
        g.setColor(getThemeTextColor());
        g.setFont(new Font("Arial", Font.BOLD, 16));
        
        int textY = y + 25;
        int lineHeight = 25;
        
        // Player name and number
        g.drawString("PLAYER " + playerNum, x + 10, textY);
        textY += lineHeight;
        
        // Player name
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Name: " + playerName, x + 10, textY);
        textY += lineHeight;
        
        // Player type
        g.drawString("Type: " + playerType, x + 10, textY);
        textY += lineHeight + 10;
        
        // Game stats
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Score: " + playerScore, x + 10, textY);
        textY += lineHeight;
        
        // AI Score Warning - if AI player and close to winning
        if (playerType.equals("AI") && playerScore >= aiWinScore * 0.8) { // 80% of win score
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 11));
            int remaining = aiWinScore - playerScore;
            g.drawString("⚠️ AI NEEDS " + remaining + " TO WIN!", x + 10, textY);
            textY += lineHeight;
            
            // Progress bar
            int barWidth = width - 30;
            int barHeight = 8;
            g.setColor(Color.DARK_GRAY);
            g.fillRect(x + 10, textY, barWidth, barHeight);
            
            int progress = (int)((double)playerScore / aiWinScore * barWidth);
            g.setColor(playerScore >= aiWinScore * 0.9 ? Color.RED : Color.ORANGE);
            g.fillRect(x + 10, textY, progress, barHeight);
            
            g.setColor(Color.WHITE);
            g.drawRect(x + 10, textY, barWidth, barHeight);
            textY += lineHeight;
            
            g.setColor(getThemeTextColor()); // Reset color
            g.setFont(new Font("Arial", Font.PLAIN, 12)); // Reset font
        }
        
        g.drawString("Level: " + playerLevel, x + 10, textY);
        textY += lineHeight;
        
        g.drawString("Lines: " + playerLines, x + 10, textY);
        textY += lineHeight + 15;
        
        // Status
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.setColor(isActive ? Color.GREEN : Color.RED);
        g.drawString("Status: " + (isActive ? "PLAYING" : "GAME OVER"), x + 10, textY);
        textY += lineHeight + 20;
        
        // Controls (only show for active player)
        if (isActive) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            
            if (playerNum == 1) {
                g.drawString("Player 1 Controls:", x + 10, textY);
                textY += 15;
                g.drawString("A: Left", x + 10, textY);
                textY += 12;
                g.drawString("D: Right", x + 10, textY);
                textY += 12;
                g.drawString("S: Down", x + 10, textY);
                textY += 12;
                g.drawString("W: Rotate", x + 10, textY);
                textY += 15; // Extra spacing
            } else {
                g.drawString("Player 2 Controls:", x + 10, textY);
                textY += 15;
                g.drawString("←: Left", x + 10, textY);
                textY += 12;
                g.drawString("→: Right", x + 10, textY);
                textY += 12;
                g.drawString("↓: Down", x + 10, textY);
                textY += 12;
                g.drawString("↑: Rotate", x + 10, textY);
                textY += 15; // Extra spacing
            }
        }
        
        // Next piece preview (if there's space)
        if (isActive && y + height > textY + 80) {
            g.setColor(getThemeTextColor());
            g.setFont(new Font("Arial", Font.BOLD, 10));
            textY += 20;
            g.drawString("Next Piece:", x + 10, textY);
            
            // Draw mini next piece
            int nextPiece = (playerNum == 1) ? TetrisGame.this.nextPiece : nextPiece2;
            drawMiniPiece(g, x + 10, textY + 5, nextPiece);
        }
    }
    
    private void drawMiniPiece(Graphics2D g, int x, int y, int pieceType) {
        int[][] piece = PIECES[pieceType];
        Color color = getPieceColor(pieceType + 1);
        g.setColor(color);
        
        int miniBlockSize = 8;
        for (int py = 0; py < piece.length; py++) {
            for (int px = 0; px < piece[py].length; px++) {
                if (piece[py][px] == 1) {
                    g.fillRect(x + px * miniBlockSize, y + py * miniBlockSize, 
                              miniBlockSize - 1, miniBlockSize - 1);
                }
            }
        }
    }
    
    private void drawGhostPieceAtPosition(Graphics2D g, int offsetX, int offsetY, 
                                        int pieceType, int pieceX, int pieceY, int rotation, boolean isPlayer2) {
        // Find ghost position (lowest possible position)
        int ghostY = pieceY;
        int[][] gameBoard = isPlayer2 ? board2 : board;
        
        while (canMoveGeneral(pieceX, ghostY + 1, rotation, pieceType, gameBoard)) {
            ghostY++;
        }
        
        // Draw ghost piece
        int[][] piece = rotatePiece(PIECES[pieceType], rotation);
        g.setColor(new Color(200, 200, 200, 100)); // Transparent gray
        
        for (int py = 0; py < piece.length; py++) {
            for (int px = 0; px < piece[py].length; px++) {
                if (piece[py][px] == 1) {
                    int drawX = offsetX + (pieceX + px) * BLOCK_SIZE;
                    int drawY = offsetY + (ghostY + py) * BLOCK_SIZE;
                    g.fillRect(drawX + 1, drawY + 1, BLOCK_SIZE - 2, BLOCK_SIZE - 2);
                    g.drawRect(drawX, drawY, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
                }
            }
        }
    }
    
    private boolean canMoveGeneral(int x, int y, int rotation, int pieceType, int[][] gameBoard) {
        int[][] piece = rotatePiece(PIECES[pieceType], rotation);
        
        for (int py = 0; py < piece.length; py++) {
            for (int px = 0; px < piece[py].length; px++) {
                if (piece[py][px] == 1) {
                    int newX = x + px;
                    int newY = y + py;
                    
                    // Check boundaries
                    if (newX < 0 || newX >= BOARD_WIDTH || newY >= BOARD_HEIGHT) {
                        return false;
                    }
                    
                    // Check collision with existing pieces
                    if (newY >= 0 && gameBoard[newY][newX] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Try to rotate with simple left/right kicks. Works for both players.
    private boolean tryRotateWithKick(boolean isPlayer2) {
        int curRot = isPlayer2 ? currentRotation2 : currentRotation;
        int newRot = (curRot + 1) % 4;
        int x      = isPlayer2 ? currentX2 : currentX;
        int y      = isPlayer2 ? currentY2 : currentY;
        int pieceIndex = isPlayer2 ? currentPiece2 : currentPiece;

        // If it already works in place, do it.
        boolean can = isPlayer2 ? canMove2(x, y, newRot) : canMove(x, y, newRot);
        if (can) {
            if (isPlayer2) currentRotation2 = newRot; else currentRotation = newRot;
            return true;
        }

        // We'll try a range of kicks and auto-clamp them to the board if needed.
        int[][] rotated = rotatePiece(PIECES[pieceIndex], newRot);
        int pieceW = rotated[0].length;

        // Try offsets in a sensible order: small left, small right, then bigger.
        int[] baseKicks = { -1, +1, -2, +2, -3, +3 };

        // Keep track to avoid retrying the same dx after clamping
        java.util.HashSet<Integer> tried = new java.util.HashSet<>();

        for (int baseDx : baseKicks) {
            int dx = baseDx;

            // Clamp against left wall
            if (x + dx < 0) {
                dx = -x; // shift just enough to stay inside
            }
            // Clamp against right wall
            if (x + dx + pieceW > BOARD_WIDTH) {
                dx = BOARD_WIDTH - pieceW - x; // pull left just enough
            }

            // Skip duplicates created by clamping
            if (!tried.add(dx)) continue;

            can = isPlayer2 ? canMove2(x + dx, y, newRot) : canMove(x + dx, y, newRot);
            if (can) {
                if (isPlayer2) { currentX2 += dx; currentRotation2 = newRot; }
                else           { currentX  += dx; currentRotation  = newRot; }
                return true;
            }
        }

        return false; // still blocked
    }
    
    private void drawBoardAtPosition(Graphics2D g, int offsetX, int offsetY, int[][] gameBoard, boolean isPlayer2) {
        // Enable antialiasing for smoother rendering
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw board background with subtle gradient
        GradientPaint bgGradient = new GradientPaint(offsetX, offsetY, Color.BLACK, 
                                                   offsetX + BOARD_WIDTH * BLOCK_SIZE, 
                                                   offsetY + BOARD_HEIGHT * BLOCK_SIZE, 
                                                   new Color(20, 20, 20));
        g.setPaint(bgGradient);
        g.fillRect(offsetX, offsetY, BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE);
        
        // Draw subtle grid
        g.setColor(new Color(40, 40, 40));
        g.setStroke(new BasicStroke(0.5f));
        for (int x = 0; x <= BOARD_WIDTH; x++) {
            g.drawLine(offsetX + x * BLOCK_SIZE, offsetY, 
                      offsetX + x * BLOCK_SIZE, offsetY + BOARD_HEIGHT * BLOCK_SIZE);
        }
        for (int y = 0; y <= BOARD_HEIGHT; y++) {
            g.drawLine(offsetX, offsetY + y * BLOCK_SIZE, 
                      offsetX + BOARD_WIDTH * BLOCK_SIZE, offsetY + y * BLOCK_SIZE);
        }
        
        // Reset stroke
        g.setStroke(new BasicStroke(1.0f));
        
        // Draw placed pieces with 3D effect
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (gameBoard[y][x] != 0) {
                    Color pieceColor = getPieceColor(gameBoard[y][x] - 1);
                    int drawX = offsetX + x * BLOCK_SIZE;
                    int drawY = offsetY + y * BLOCK_SIZE;
                    
                    // Fill main block
                    g.setColor(pieceColor);
                    g.fillRect(drawX + 1, drawY + 1, BLOCK_SIZE - 2, BLOCK_SIZE - 2);
                    
                    // Draw highlight on top and left
                    g.setColor(pieceColor.brighter());
                    g.drawLine(drawX + 1, drawY + 1, drawX + BLOCK_SIZE - 2, drawY + 1); // Top
                    g.drawLine(drawX + 1, drawY + 1, drawX + 1, drawY + BLOCK_SIZE - 2); // Left
                    
                    // Draw shadow on bottom and right
                    g.setColor(pieceColor.darker());
                    g.drawLine(drawX + BLOCK_SIZE - 2, drawY + 1, drawX + BLOCK_SIZE - 2, drawY + BLOCK_SIZE - 2); // Right
                    g.drawLine(drawX + 1, drawY + BLOCK_SIZE - 2, drawX + BLOCK_SIZE - 2, drawY + BLOCK_SIZE - 2); // Bottom
                    
                    // Draw border
                    g.setColor(Color.BLACK);
                    g.drawRect(drawX, drawY, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
                }
            }
        }
        
        // Reset antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    
    private void drawCurrentPieceAtPosition(Graphics2D g, int offsetX, int offsetY, 
                                          int piece, int x, int y, int rotation, boolean isPlayer2) {
        if (isPlayer2 ? gameOver2 : gameOver) return;
        
        int[][] pieceShape = rotatePiece(PIECES[piece], rotation);
        
        // Enable antialiasing for smoother rendering
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        for (int py = 0; py < pieceShape.length; py++) {
            for (int px = 0; px < pieceShape[py].length; px++) {
                if (pieceShape[py][px] == 1) {
                    int drawX = offsetX + (x + px) * BLOCK_SIZE;
                    int drawY = offsetY + (y + py) * BLOCK_SIZE;
                    
                    // Draw piece block with gradient effect
                    Color pieceColor = getPieceColor(piece);
                    
                    // Fill main block
                    g.setColor(pieceColor);
                    g.fillRect(drawX + 1, drawY + 1, BLOCK_SIZE - 2, BLOCK_SIZE - 2);
                    
                    // Draw highlight on top and left
                    g.setColor(pieceColor.brighter());
                    g.drawLine(drawX + 1, drawY + 1, drawX + BLOCK_SIZE - 2, drawY + 1); // Top
                    g.drawLine(drawX + 1, drawY + 1, drawX + 1, drawY + BLOCK_SIZE - 2); // Left
                    
                    // Draw shadow on bottom and right
                    g.setColor(pieceColor.darker());
                    g.drawLine(drawX + BLOCK_SIZE - 2, drawY + 1, drawX + BLOCK_SIZE - 2, drawY + BLOCK_SIZE - 2); // Right
                    g.drawLine(drawX + 1, drawY + BLOCK_SIZE - 2, drawX + BLOCK_SIZE - 2, drawY + BLOCK_SIZE - 2); // Bottom
                    
                    // Draw border
                    g.setColor(Color.BLACK);
                    g.drawRect(drawX, drawY, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
                }
            }
        }
        
        // Reset antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    
    private void drawPlayerInfo(Graphics2D g, int x, int y, String name, 
                               int playerScore, int playerLevel, int playerLines, int playerNum) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        
        g.drawString(name + " (P" + playerNum + ")", x, y + 15);
        g.drawString("Score: " + playerScore, x, y + 30);
        g.drawString("Level: " + playerLevel, x, y + 45);  
        g.drawString("Lines: " + playerLines, x, y + 60);
    }
    
    private void drawMultiplayerWinner(Graphics2D g) {
        // Draw semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Determine winner
        String winnerText;
        if (!gameOver && gameOver2) {
            winnerText = player1Name + " WINS!";
            g.setColor(Color.GREEN);
        } else if (gameOver && !gameOver2) {
            winnerText = player2Name + " WINS!";
            g.setColor(Color.GREEN);
        } else {
            if (score > score2) {
                winnerText = player1Name + " WINS!";
                g.setColor(Color.GREEN);
            } else if (score2 > score) {
                winnerText = player2Name + " WINS!";
                g.setColor(Color.GREEN);
            } else {
                winnerText = "TIE GAME!";
                g.setColor(Color.YELLOW);
            }
        }
        
        // Draw winner text
        g.setFont(new Font("Arial", Font.BOLD, 32));
        FontMetrics fm = g.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(winnerText)) / 2;
        int textY = getHeight() / 2 - 20;
        g.drawString(winnerText, textX, textY);
        
        // Draw instructions
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        fm = g.getFontMetrics();
        String instruction = "Press R to restart or ESC to return to menu";
        int instX = (getWidth() - fm.stringWidth(instruction)) / 2;
        g.drawString(instruction, instX, textY + 40);
    }
    
    private Color getPieceColor(int pieceIndex) {
        return COLORS[pieceIndex % COLORS.length];
    }
    
    private Color getThemeBackgroundColor() {
        switch (gameTheme) {
            case "Dark": return Color.DARK_GRAY;
            case "Colorful": return new Color(30, 30, 60);
            default: return Color.BLACK; // Classic
        }
    }
    
    private Color getThemeTextColor() {
        switch (gameTheme) {
            case "Dark": return Color.LIGHT_GRAY;
            case "Colorful": return Color.CYAN;
            default: return Color.WHITE; // Classic
        }
    }
    
    private Color getThemeAccentColor() {
        switch (gameTheme) {
            case "Dark": return new Color(50, 50, 70);
            case "Colorful": return new Color(60, 30, 90);
            default: return new Color(30, 30, 30); // Classic
        }
    }
    
    private void drawBoard(Graphics2D g) {
        // Draw placed pieces
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] != 0) {
                    g.setColor(COLORS[(board[y][x] - 1) % COLORS.length]);
                    g.fillRect(x * BLOCK_SIZE + 10, y * BLOCK_SIZE + 50, BLOCK_SIZE, BLOCK_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(x * BLOCK_SIZE + 10, y * BLOCK_SIZE + 50, BLOCK_SIZE, BLOCK_SIZE);
                }
            }
        }
        
        // Draw grid
        g.setColor(Color.GRAY);
        for (int x = 0; x <= BOARD_WIDTH; x++) {
            g.drawLine(x * BLOCK_SIZE + 10, 50, x * BLOCK_SIZE + 10, BOARD_HEIGHT * BLOCK_SIZE + 50);
        }
        for (int y = 0; y <= BOARD_HEIGHT; y++) {
            g.drawLine(10, y * BLOCK_SIZE + 50, BOARD_WIDTH * BLOCK_SIZE + 10, y * BLOCK_SIZE + 50);
        }
    }
    
    private void drawCurrentPiece(Graphics2D g) {
        int[][] piece = rotatePiece(PIECES[currentPiece], currentRotation);
        g.setColor(COLORS[currentPiece % COLORS.length]);
        
        for (int py = 0; py < piece.length; py++) {
            for (int px = 0; px < piece[py].length; px++) {
                if (piece[py][px] == 1) {
                    int drawX = (currentX + px) * BLOCK_SIZE + 10;
                    int drawY = (currentY + py) * BLOCK_SIZE + 50;
                    g.fillRect(drawX, drawY, BLOCK_SIZE, BLOCK_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(drawX, drawY, BLOCK_SIZE, BLOCK_SIZE);
                    g.setColor(COLORS[currentPiece % COLORS.length]);
                }
            }
        }
    }
    
    private void drawGhostPiece(Graphics2D g) {
        int ghostY = currentY;
        while (canMove(currentX, ghostY + 1, currentRotation)) {
            ghostY++;
        }
        
        if (ghostY != currentY) {
            int[][] piece = rotatePiece(PIECES[currentPiece], currentRotation);
            g.setColor(new Color(COLORS[currentPiece % COLORS.length].getRed(),
                                COLORS[currentPiece % COLORS.length].getGreen(),
                                COLORS[currentPiece % COLORS.length].getBlue(), 100));
            
            for (int py = 0; py < piece.length; py++) {
                for (int px = 0; px < piece[py].length; px++) {
                    if (piece[py][px] == 1) {
                        int drawX = (currentX + px) * BLOCK_SIZE + 10;
                        int drawY = (ghostY + py) * BLOCK_SIZE + 50;
                        g.fillRect(drawX, drawY, BLOCK_SIZE, BLOCK_SIZE);
                    }
                }
            }
        }
    }
    
    private void drawNextPiece(Graphics2D g) {
        // This method is now integrated into drawUI() for better layout
        // Keeping empty to avoid breaking references
    }
    
    private void drawUI(Graphics2D g) {
        // Enhanced single player UI layout for large window
        int boardEndX = BOARD_WIDTH * BLOCK_SIZE + 20;
        int rightPanelX = boardEndX + 20;
        int rightPanelWidth = getWidth() - rightPanelX - 20;
        
        // Main game stats panel with background
        g.setColor(new Color(240, 240, 240, 200));
        g.fillRoundRect(rightPanelX, 50, rightPanelWidth, 120, 15, 15);
        g.setColor(Color.BLACK);
        g.drawRoundRect(rightPanelX, 50, rightPanelWidth, 120, 15, 15);
        
        // Game statistics
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("GAME STATS", rightPanelX + 10, 75);
        
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Score: " + String.format("%,d", score), rightPanelX + 10, 105);
        g.drawString("Level: " + level, rightPanelX + 10, 125);
        g.drawString("Lines: " + linesCleared, rightPanelX + 10, 145);
        
        // Next piece preview panel
        int nextPieceY = 190;
        g.setColor(new Color(240, 240, 240, 200));
        g.fillRoundRect(rightPanelX, nextPieceY, rightPanelWidth, 100, 15, 15);
        g.setColor(Color.BLACK);
        g.drawRoundRect(rightPanelX, nextPieceY, rightPanelWidth, 100, 15, 15);
        
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("NEXT PIECE", rightPanelX + 10, nextPieceY + 25);
        
        // Draw next piece preview (centered in panel)
        if (showNextPiece) {
            int[][] piece = PIECES[nextPiece];
            Color pieceColor = COLORS[nextPiece % COLORS.length];
            
            int previewX = rightPanelX + rightPanelWidth/2 - 40;
            int previewY = nextPieceY + 40;
            
            g.setColor(pieceColor);
            for (int py = 0; py < piece.length; py++) {
                for (int px = 0; px < piece[py].length; px++) {
                    if (piece[py][px] == 1) {
                        g.fillRect(previewX + px * 20, previewY + py * 20, 20, 20);
                        g.setColor(Color.BLACK);
                        g.drawRect(previewX + px * 20, previewY + py * 20, 20, 20);
                        g.setColor(pieceColor);
                    }
                }
            }
        }
        
        // Controls panel
        int controlsY = nextPieceY + 120;
        int controlsHeight = 160;
        g.setColor(new Color(240, 240, 240, 200));
        g.fillRoundRect(rightPanelX, controlsY, rightPanelWidth, controlsHeight, 15, 15);
        g.setColor(Color.BLACK);
        g.drawRoundRect(rightPanelX, controlsY, rightPanelWidth, controlsHeight, 15, 15);
        
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("CONTROLS", rightPanelX + 10, controlsY + 25);
        
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("🎮 Movement: WASD", rightPanelX + 10, controlsY + 50);
        g.drawString("⬇️ Hard Drop: SPACE", rightPanelX + 10, controlsY + 70);
        g.drawString("⏸️ Pause: P", rightPanelX + 10, controlsY + 90);
        g.drawString("🎵 Music: M", rightPanelX + 10, controlsY + 110);
        g.drawString("🏠 Menu: ESC", rightPanelX + 10, controlsY + 130);
        
        // Status panel
        int statusY = controlsY + controlsHeight + 20;
        g.setColor(new Color(240, 240, 240, 200));
        g.fillRoundRect(rightPanelX, statusY, rightPanelWidth, 80, 15, 15);
        g.setColor(Color.BLACK);
        g.drawRoundRect(rightPanelX, statusY, rightPanelWidth, 80, 15, 15);
        
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("STATUS", rightPanelX + 10, statusY + 25);
        
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(soundEnabled ? new Color(0, 150, 0) : Color.RED);
        g.drawString("🔊 Sound: " + (soundEnabled ? "ON" : "OFF"), rightPanelX + 10, statusY + 45);
        g.setColor(musicEnabled ? new Color(0, 150, 0) : Color.RED);
        g.drawString("🎵 Music: " + (musicEnabled ? "ON" : "OFF"), rightPanelX + 10, statusY + 65);
        
        // Game state overlays
        if (paused) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            FontMetrics fm = g.getFontMetrics();
            String pauseText = "PAUSED";
            int pauseX = (BOARD_WIDTH * BLOCK_SIZE - fm.stringWidth(pauseText)) / 2;
            g.drawString(pauseText, pauseX, BOARD_HEIGHT * BLOCK_SIZE / 2);
        }
        
        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE);
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            FontMetrics fm = g.getFontMetrics();
            String gameOverText = "GAME OVER";
            int gameOverX = (BOARD_WIDTH * BLOCK_SIZE - fm.stringWidth(gameOverText)) / 2;
            g.drawString(gameOverText, gameOverX, BOARD_HEIGHT * BLOCK_SIZE / 2);
        }
    }
    
    private void drawOnlineUI(Graphics2D g) {
        // Enhanced online mode UI layout
        int boardEndX = BOARD_WIDTH * BLOCK_SIZE + 20;
        int rightPanelX = boardEndX + 20;
        int rightPanelWidth = getWidth() - rightPanelX - 20;
        
        // Online Mode indicator panel
        g.setColor(new Color(70, 130, 180, 200));
        g.fillRoundRect(rightPanelX, 50, rightPanelWidth, 60, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(rightPanelX, 50, rightPanelWidth, 60, 15, 15);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("🌐 ONLINE MODE", rightPanelX + 10, 75);
        
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        String status = serverConnected ? "✅ Connected to TetrisServer" : "❌ Disconnected";
        g.setColor(serverConnected ? Color.GREEN : Color.RED);
        g.drawString(status, rightPanelX + 10, 95);
        
        // Main game stats panel
        g.setColor(new Color(240, 240, 240, 200));
        g.fillRoundRect(rightPanelX, 130, rightPanelWidth, 120, 15, 15);
        g.setColor(Color.BLACK);
        g.drawRoundRect(rightPanelX, 130, rightPanelWidth, 120, 15, 15);
        
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("GAME STATS", rightPanelX + 10, 155);
        
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Score: " + String.format("%,d", score), rightPanelX + 10, 185);
        g.drawString("Level: " + level, rightPanelX + 10, 205);
        g.drawString("Lines: " + linesCleared, rightPanelX + 10, 225);
        
        // Next piece preview panel
        int nextPieceY = 270;
        g.setColor(new Color(240, 240, 240, 200));
        g.fillRoundRect(rightPanelX, nextPieceY, rightPanelWidth, 100, 15, 15);
        g.setColor(Color.BLACK);
        g.drawRoundRect(rightPanelX, nextPieceY, rightPanelWidth, 100, 15, 15);
        
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("NEXT PIECE", rightPanelX + 10, nextPieceY + 25);
        
        // Draw next piece preview
        if (showNextPiece) {
            int[][] piece = PIECES[nextPiece];
            Color pieceColor = COLORS[nextPiece % COLORS.length];
            
            int previewX = rightPanelX + rightPanelWidth/2 - 40;
            int previewY = nextPieceY + 40;
            
            g.setColor(pieceColor);
            for (int py = 0; py < piece.length; py++) {
                for (int px = 0; px < piece[py].length; px++) {
                    if (piece[py][px] == 1) {
                        g.fillRect(previewX + px * 20, previewY + py * 20, 20, 20);
                        g.setColor(Color.BLACK);
                        g.drawRect(previewX + px * 20, previewY + py * 20, 20, 20);
                        g.setColor(pieceColor);
                    }
                }
            }
        }
        
        // Server assistance panel
        int serverY = nextPieceY + 120;
        g.setColor(new Color(255, 255, 220, 200));
        g.fillRoundRect(rightPanelX, serverY, rightPanelWidth, 100, 15, 15);
        g.setColor(Color.BLACK);
        g.drawRoundRect(rightPanelX, serverY, rightPanelWidth, 100, 15, 15);
        
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("🤖 AI ASSISTANCE", rightPanelX + 10, serverY + 25);
        
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Server provides optimal moves", rightPanelX + 10, serverY + 50);
        g.drawString("Delay: " + serverMoveDelay + "ms", rightPanelX + 10, serverY + 70);
        
        // Controls panel
        int controlsY = serverY + 120;
        int controlsHeight = 120;
        g.setColor(new Color(240, 240, 240, 200));
        g.fillRoundRect(rightPanelX, controlsY, rightPanelWidth, controlsHeight, 15, 15);
        g.setColor(Color.BLACK);
        g.drawRoundRect(rightPanelX, controlsY, rightPanelWidth, controlsHeight, 15, 15);
        
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("CONTROLS", rightPanelX + 10, controlsY + 25);
        
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("🎮 Movement: WASD", rightPanelX + 10, controlsY + 50);
        g.drawString("⏸️ Pause: P", rightPanelX + 10, controlsY + 70);
        g.drawString("🎵 Music: M", rightPanelX + 10, controlsY + 90);
        g.drawString("🔌 Disconnect: ESC", rightPanelX + 10, controlsY + 110);
        
        // Game state overlays
        if (paused) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            FontMetrics fm = g.getFontMetrics();
            String pauseText = "PAUSED";
            int pauseX = (BOARD_WIDTH * BLOCK_SIZE - fm.stringWidth(pauseText)) / 2;
            g.drawString(pauseText, pauseX, BOARD_HEIGHT * BLOCK_SIZE / 2);
        }
        
        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, BOARD_WIDTH * BLOCK_SIZE, BOARD_HEIGHT * BLOCK_SIZE);
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            FontMetrics fm = g.getFontMetrics();
            String gameOverText = "GAME OVER";
            int gameOverX = (BOARD_WIDTH * BLOCK_SIZE - fm.stringWidth(gameOverText)) / 2;
            g.drawString(gameOverText, gameOverX, BOARD_HEIGHT * BLOCK_SIZE / 2);
        }
    }
    
    // Key controls
    @Override
    public void keyPressed(KeyEvent e) {
        // Handle splash screen - any key skips to home screen
        if (showSplashScreen) {
            showSplashScreen = false;
            showHomeScreen = true;
            repaint();
            return;
        }
        
        // Handle home screen navigation
        if (showHomeScreen) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    selectedMenuItem = (selectedMenuItem - 1 + menuItems.length) % menuItems.length;
                    break;
                case KeyEvent.VK_DOWN:
                    selectedMenuItem = (selectedMenuItem + 1) % menuItems.length;
                    break;
                case KeyEvent.VK_ENTER:
                    if (selectedMenuItem == 0) { // New Game
                        startGame();
                    } else if (selectedMenuItem == 1) { // Load Game
                        if (hasAnySaveFiles()) {
                            showLoadGameScreen();
                        }
                    } else if (selectedMenuItem == 2) { // Multiplayer
                        showPlayerSelection = true;
                        showHomeScreen = false;
                        playerSelectionIndex = 0;
                        // Reset names and types
                        player1Name = "";
                        player2Name = "";
                        player1Type = 0;
                        player2Type = 0;
                    } else if (selectedMenuItem == 3) { // Online Mode
                        startOnlineMode();
                    } else if (selectedMenuItem == 4) { // Highscore
                        showHighscoreScreen();
                    } else if (selectedMenuItem == 5) { // Settings
                        showConfigScreen();
                    } else if (selectedMenuItem == 6) { // Exit
                        System.exit(0);
                    }
                    break;
            }
            repaint();
            return;
        }
        
        // Handle load game screen navigation
        if (showLoadGameScreen) {
            handleLoadGameInput(e);
            return;
        }
        
        // Handle config screen navigation
        if (showConfigScreen) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    selectedConfigItem = (selectedConfigItem - 1 + configItems.length) % configItems.length;
                    break;
                case KeyEvent.VK_DOWN:
                    selectedConfigItem = (selectedConfigItem + 1) % configItems.length;
                    break;
                case KeyEvent.VK_LEFT:
                    changeConfigValue(-1);
                    break;
                case KeyEvent.VK_RIGHT:
                    changeConfigValue(1);
                    break;
                case KeyEvent.VK_ENTER:
                    if (selectedConfigItem == configItems.length - 1) { // Back to Menu
                        returnToHomeScreen();
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    returnToHomeScreen();
                    break;
            }
            repaint();
            return;
        }
        
        // Handle highscore screen navigation
        if (showHighscoreScreen) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ESCAPE:
                case KeyEvent.VK_ENTER:
                    returnToHomeScreen();
                    break;
            }
            repaint();
            return;
        }
        
        // Handle player selection screen
        if (showPlayerSelection) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    if (playerSelectionIndex > 0) {
                        playerSelectionIndex--;
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (playerSelectionIndex < 3) { // 0=P1 Type, 1=P2 Type, 2=Continue, 3=Back
                        playerSelectionIndex++;
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    if (playerSelectionIndex == 0) { // Player 1 Type
                        player1Type = (player1Type - 1 + playerTypes.length) % playerTypes.length;
                    } else if (playerSelectionIndex == 1) { // Player 2 Type
                        player2Type = (player2Type - 1 + playerTypes.length) % playerTypes.length;
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (playerSelectionIndex == 0) { // Player 1 Type
                        player1Type = (player1Type + 1) % playerTypes.length;
                    } else if (playerSelectionIndex == 1) { // Player 2 Type
                        player2Type = (player2Type + 1) % playerTypes.length;
                    }
                    break;
                case KeyEvent.VK_ENTER:
                    if (playerSelectionIndex == 2) { // Continue
                        showNameEntry = true;
                        showPlayerSelection = false;
                        nameEntryPlayer = 1;
                    } else if (playerSelectionIndex == 3) { // Back to Home
                        showPlayerSelection = false;
                        returnToHomeScreen();
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    showPlayerSelection = false;
                    returnToHomeScreen();
                    break;
            }
            repaint();
            return;
        }
        
        // Handle name entry screen
        if (showNameEntry) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (nameEntryPlayer == 1) {
                    if (player1Name.trim().isEmpty()) {
                        player1Name = "Player 1";
                    }
                    nameEntryPlayer = 2;
                } else {
                    if (player2Name.trim().isEmpty()) {
                        player2Name = "Player 2";
                    }
                    // Start multiplayer game (placeholder for now)
                    isMultiplayerMode = true;
                    // Keep consistent large window size
                    showNameEntry = false;
                    startMultiplayerGame(); // Will be replaced with startMultiplayerGame later
                }
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                showNameEntry = false;
                showPlayerSelection = true;
            } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (nameEntryPlayer == 1 && player1Name.length() > 0) {
                    player1Name = player1Name.substring(0, player1Name.length() - 1);
                } else if (nameEntryPlayer == 2 && player2Name.length() > 0) {
                    player2Name = player2Name.substring(0, player2Name.length() - 1);
                }
            } else {
                char c = e.getKeyChar();
                if (Character.isLetterOrDigit(c) || c == ' ') {
                    if (nameEntryPlayer == 1 && player1Name.length() < 15) {
                        player1Name += c;
                    } else if (nameEntryPlayer == 2 && player2Name.length() < 15) {
                        player2Name += c;
                    }
                }
            }
            repaint();
            return;
        }
        
        // Handle game over state
        if (gameOver) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_R:
                    restartGame();
                    break;
                case KeyEvent.VK_ESCAPE:
                    returnToHomeScreen();
                    break;
            }
            return;
        }
        
        // Handle in-game controls
        if (gameOver && (!isMultiplayerMode || gameOver2)) return;
        
        if (isMultiplayerMode) {
            handleMultiplayerControls(e);
        } else {
            handleSinglePlayerControls(e);
        }
        
        // Global game controls (music, pause, etc.)
        switch (e.getKeyCode()) {
            case KeyEvent.VK_M:          // Toggle Music
                musicEnabled = !musicEnabled;
                if (soundManager != null) {
                    if (musicEnabled) {
                        // Resume music based on current state
                        if (showHomeScreen) {
                            soundManager.playBackgroundMusic("menu", musicVolume);
                        } else if (gameOver) {
                            soundManager.playBackgroundMusic("gameover", musicVolume);
                        } else if (paused) {
                            soundManager.playBackgroundMusic("pause", musicVolume);
                        } else {
                            soundManager.playBackgroundMusic("background", musicVolume);
                        }
                    } else {
                        // Stop all music
                        soundManager.stopBackgroundMusic();
                    }
                }
                break;
            case KeyEvent.VK_P:          // Pause/Resume
                paused = !paused;
                if (paused) {
                    gameTimer.stop();
                    playSound("pause");
                    // Switch to pause music
                    if (musicEnabled && soundManager != null) {
                        soundManager.stopBackgroundMusic();
                        soundManager.playBackgroundMusic("pause", musicVolume);
                    }
                } else {
                    gameTimer.start();
                    playSound("resume");
                    // Resume game music
                    if (musicEnabled && soundManager != null) {
                        soundManager.stopBackgroundMusic();
                        soundManager.playBackgroundMusic("background", musicVolume);
                    }
                }
                break;
        }
        
        repaint();
    }
    
    private void restartGame() {
        gameTimer.stop();
        initializeGame();
        fallSpeed = 500;
        gameTimer = new javax.swing.Timer(fallSpeed, e -> gameStep());
        gameTimer.start();
        repaint();
    }
    
    private void handleMultiplayerControls(KeyEvent e) {
        switch (e.getKeyCode()) {
            // Player 1 Controls (WASD only)
            case KeyEvent.VK_A:
                if (!gameOver && canMove(currentX - 1, currentY, currentRotation)) {
                    currentX--;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_D:
                if (!gameOver && canMove(currentX + 1, currentY, currentRotation)) {
                    currentX++;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_S:
                if (!gameOver && canMove(currentX, currentY + 1, currentRotation)) {
                    currentY++;
                    score += 1;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_W:
                if (!gameOver && tryRotateWithKick(false)) {
                    playSound("rotate");
                }
                break;
                
            // Player 2 Controls (Arrow Keys)
            case KeyEvent.VK_LEFT:
                if (!gameOver2 && canMove2(currentX2 - 1, currentY2, currentRotation2)) {
                    currentX2--;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (!gameOver2 && canMove2(currentX2 + 1, currentY2, currentRotation2)) {
                    currentX2++;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_DOWN:
                if (!gameOver2 && canMove2(currentX2, currentY2 + 1, currentRotation2)) {
                    currentY2++;
                    score2 += 1;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_UP:
                if (!gameOver2) {
                    int newRotation2 = (currentRotation2 + 1) % 4;
                    if (canMove2(currentX2, currentY2, newRotation2)) {
                        currentRotation2 = newRotation2;
                        playSound("rotate");
                    }
                }
                break;
                
            // Game controls
            case KeyEvent.VK_ESCAPE:
                if (gameOver && gameOver2) {
                    returnToHomeScreen();
                } else {
                    int result = JOptionPane.showConfirmDialog(
                            this,
                            "Quit multiplayer game and return to menu?",
                            "Confirm Quit",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (result == JOptionPane.YES_OPTION) {
                        returnToHomeScreen();
                    }
                }
                break;
                
            case KeyEvent.VK_R:
                if (gameOver && gameOver2) {
                    restartMultiplayerGame();
                }
                break;
        }
    }
    
    private void handleSinglePlayerControls(KeyEvent e) {
        switch (e.getKeyCode()) {
            // Movement controls (WASD only)
            case KeyEvent.VK_A:
                if (canMove(currentX - 1, currentY, currentRotation)) {
                    currentX--;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_D:
                if (canMove(currentX + 1, currentY, currentRotation)) {
                    currentX++;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_S:
                if (canMove(currentX, currentY + 1, currentRotation)) {
                    currentY++;
                    score += 1;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_W:
                if (tryRotateWithKick(false)) {
                    playSound("rotate");
                }
                break;
            case KeyEvent.VK_SPACE:      // Hard drop (Space)
                while (canMove(currentX, currentY + 1, currentRotation)) {
                    currentY++;
                    score += 2;
                }
                playSound("drop");
                break;
                
            // Game controls
            case KeyEvent.VK_ESCAPE:
                if (!gameOver) {
                    if (isOnlineMode) {
                        // Online mode - ask if user wants to disconnect
                        int result = JOptionPane.showConfirmDialog(this,
                            "Disconnect from TetrisServer and return to menu?",
                            "Disconnect", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            if (serverConnected) {
                                tetrisServer.disconnect();
                                serverConnected = false;
                            }
                            returnToHomeScreen();
                        }
                    } else {
                        // Regular single player - ask if player wants to save
                        String[] options = {"Save & Quit", "Quit without Saving", "Cancel"};
                        int choice = JOptionPane.showOptionDialog(this,
                            "Would you like to save your current game before returning to menu?",
                            "Save Game?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                            null, options, options[0]);
                        
                        if (choice == 0) { // Save & Quit
                            saveCurrentGame();
                            returnToHomeScreen();
                        } else if (choice == 1) { // Quit without Saving
                            returnToHomeScreen();
                        }
                        // If choice == 2 (Cancel), do nothing - continue playing
                    }
                } else {
                    returnToHomeScreen();
                }
                break;
                
            case KeyEvent.VK_R:
                if (gameOver) {
                    restartGame();
                }
                break;
        }
    }
    
    private void restartMultiplayerGame() {
        gameTimer.stop();
        initializeMultiplayerGame();
        fallSpeed = 500;
        gameTimer = new javax.swing.Timer(fallSpeed, e -> multiplayerGameStep());
        gameTimer.start();
        repaint();
    }
    
    private void startGame() {
        showHomeScreen = false;
        isMultiplayerMode = false; // Ensure single player mode
        
        // Reset player types for single player (always Human)
        player1Type = 0; // Force Human player for single player mode
        player2Type = 0; // Reset player 2 type as well
        
        // Keep consistent window size - no resize needed
        initializeGame();
        fallSpeed = 500;
        if (gameTimer != null) {
            gameTimer.stop();
        }
        gameTimer = new javax.swing.Timer(fallSpeed, e -> gameStep());
        gameTimer.start();
        
        // Start game background music
        if (musicEnabled && soundManager != null) {
            soundManager.stopBackgroundMusic();
            soundManager.playBackgroundMusic("background", musicVolume);
        }
        
        repaint();
    }
    
    private void startMultiplayerGame() {
        showHomeScreen = false;
        initializeMultiplayerGame();
        
        // Keep consistent large window size - no resize needed
        centerWindow(); // Just center, don't resize
        
        fallSpeed = 500;
        if (gameTimer != null) {
            gameTimer.stop();
        }
        gameTimer = new javax.swing.Timer(fallSpeed, e -> multiplayerGameStep());
        gameTimer.start();
        
        // Start game background music
        if (musicEnabled && soundManager != null) {
            soundManager.stopBackgroundMusic();
            soundManager.playBackgroundMusic("background", musicVolume);
        }
        
        repaint();
    }
    
    private void startOnlineMode() {
        // Try to connect to TetrisServer
        if (tetrisServer.connect()) {
            serverConnected = true;
            showHomeScreen = false;
            isOnlineMode = true;
            isMultiplayerMode = false;
            
            // Initialize game for online mode
            initializeGame();
            
            // Keep consistent large window size
            centerWindow();
            
            fallSpeed = 500;
            if (gameTimer != null) {
                gameTimer.stop();
            }
            gameTimer = new javax.swing.Timer(fallSpeed, e -> onlineGameStep());
            gameTimer.start();
            
            // Start game background music
            if (musicEnabled && soundManager != null) {
                soundManager.stopBackgroundMusic();
                soundManager.playBackgroundMusic("background", musicVolume);
            }
            
            JOptionPane.showMessageDialog(this,
                "✅ Connected to TetrisServer!\n\nThe AI server will now help you play optimally.\n\nControls: ESC to quit, P to pause",
                "Online Mode Started", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "❌ Failed to connect to TetrisServer!\n\nPlease make sure the server is running on localhost:3000\n\nTry starting the server first, then select Online Mode again.",
                "Connection Failed", JOptionPane.ERROR_MESSAGE);
        }
        
        repaint();
    }
    
    private void onlineGameStep() {
        if (showHomeScreen || showConfigScreen || gameOver || paused) return;
        
        // Get server recommendation if connected and enough time has passed
        long currentTime = System.currentTimeMillis();
        if (serverConnected && currentTime - lastServerMoveTime > serverMoveDelay) {
            requestServerMove();
            lastServerMoveTime = currentTime;
        }
        
        // Normal game step
        if (canMove(currentX, currentY + 1, currentRotation)) {
            currentY++;
        } else {
            // Place piece on board
            placePiece();
            
            // Check for completed lines
            clearLines();
            
            // Spawn next piece
            spawnNextPiece();
            
            // Check game over
            if (!canMove(currentX, currentY, currentRotation)) {
                gameOver = true;
                gameTimer.stop();
                if (serverConnected) {
                    tetrisServer.disconnect();
                    serverConnected = false;
                }
                showOnlineGameOverDialog();
            }
        }
        
        repaint();
    }
    
    private void requestServerMove() {
        if (!serverConnected) return;
        
        try {
            // Create PurGame object with current game state
            TetrisServer.PurGame gameState = new TetrisServer.PurGame(
                BOARD_WIDTH,
                BOARD_HEIGHT, 
                board,
                currentPiece,
                nextPiece
            );
            
            // Get optimal move from server
            TetrisServer.OpMove optimalMove = tetrisServer.getOptimalMove(gameState);
            
            if (optimalMove != null) {
                // Apply server's recommendation
                applyServerMove(optimalMove);
            }
        } catch (Exception e) {
            System.err.println("❌ Error requesting server move: " + e.getMessage());
            serverConnected = false;
        }
    }
    
    private void applyServerMove(TetrisServer.OpMove move) {
        // Apply optimal rotation
        int targetRotation = move.opRotate % 4;
        while (currentRotation != targetRotation) {
            if (canMove(currentX, currentY, (currentRotation + 1) % 4)) {
                currentRotation = (currentRotation + 1) % 4;
            } else {
                break; // Can't rotate due to collision
            }
        }
        
        // Move to optimal X position (with boundary checks)
        int targetX = Math.max(0, Math.min(BOARD_WIDTH - 1, move.opX));
        
        if (currentX < targetX) {
            // Move right
            while (currentX < targetX && canMove(currentX + 1, currentY, currentRotation)) {
                currentX++;
            }
        } else if (currentX > targetX) {
            // Move left  
            while (currentX > targetX && canMove(currentX - 1, currentY, currentRotation)) {
                currentX--;
            }
        }
        
        System.out.println("🎯 Applied server move: x=" + currentX + ", rotation=" + currentRotation);
    }
    
    private void showOnlineGameOverDialog() {
        playSound("gameOver");
        
        // Record the score
        addCurrentScore("Online Mode", "Server-Assisted");
        
        int result = JOptionPane.showOptionDialog(
            this,
            "🌐 Online Game Over!\n\nScore: " + score + "\nLevel: " + level + "\nLines: " + linesCleared + 
            "\n\n✅ Score saved to high scores!\n\nWhat would you like to do?",
            "Online Game Over",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            new String[]{"Play Again", "Main Menu", "Exit"},
            "Play Again"
        );
        
        switch (result) {
            case 0: // Play Again
                startOnlineMode();
                break;
            case 1: // Main Menu
                if (serverConnected) {
                    tetrisServer.disconnect();
                    serverConnected = false;
                }
                returnToHomeScreen();
                break;
            case 2: // Exit
            default:
                System.exit(0);
                break;
        }
    }
    
    private void returnToHomeScreen() {
        // Disconnect from server if connected
        if (serverConnected) {
            tetrisServer.disconnect();
            serverConnected = false;
        }
        
        showHomeScreen = true;
        showConfigScreen = false;
        showHighscoreScreen = false;
        showPlayerSelection = false;
        showNameEntry = false;
        isMultiplayerMode = false;
        isOnlineMode = false;
        gameOver = false;
        gameOver2 = false;
        paused = false;
        
        // Reset player types to default (Human)
        player1Type = 0; // Human
        player2Type = 0; // Human
        
        if (gameTimer != null) {
            gameTimer.stop();
        }
        selectedMenuItem = 0; // Reset to "Play Game"
        
        // Keep consistent large window size
        centerWindow(); // Just center, don't resize
        
        // Start menu music
        if (musicEnabled && soundManager != null) {
            soundManager.stopBackgroundMusic();
            soundManager.playBackgroundMusic("menu", musicVolume);
        }
        
        repaint();
    }
    
    private void showHighscoreScreen() {
        showHomeScreen = false;
        showHighscoreScreen = true;
        showConfigScreen = false;
        showPlayerSelection = false;
        showNameEntry = false;
        repaint();
    }
    
    private void showConfigScreen() {
        showHomeScreen = false;
        showConfigScreen = true;
        selectedConfigItem = 0;
        // Keep consistent large window size
        repaint();
    }
    
    private void changeConfigValue(int direction) {
        switch (selectedConfigItem) {
            case 0: // Field Width
                int newWidth = Math.max(5, Math.min(15, BOARD_WIDTH + direction));
                if (newWidth != BOARD_WIDTH) {
                    BOARD_WIDTH = newWidth;
                    resetGameBoardsWithNewDimensions();
                }
                break;
            case 1: // Field Height
                int newHeight = Math.max(15, Math.min(30, BOARD_HEIGHT + direction));
                if (newHeight != BOARD_HEIGHT) {
                    BOARD_HEIGHT = newHeight;
                    resetGameBoardsWithNewDimensions();
                }
                break;
            case 2: // Game Level
                startingLevel = Math.max(1, Math.min(10, startingLevel + direction));
                break;
            case 3: // Music
                musicEnabled = !musicEnabled;
                if (soundManager != null) {
                    if (musicEnabled) {
                        if (showHomeScreen) {
                            soundManager.playBackgroundMusic("menu", musicVolume);
                        } else {
                            soundManager.playBackgroundMusic("background", musicVolume);
                        }
                    } else {
                        soundManager.stopBackgroundMusic();
                    }
                }
                break;
            case 4: // Sound Effect
                soundEnabled = !soundEnabled;
                break;
            case 5: // Extend Mode (Ghost Piece)
                showGhostPiece = !showGhostPiece;
                break;
            case 6: // Back
                if (direction != 0) returnToHomeScreen();
                break;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    // Sound system (placeholder - can be extended with actual sound files)
    private void playSound(String soundType) {
        if (!soundEnabled) return;
        
        if (soundManager != null) {
            soundManager.playSound(soundType);
        } else {
            // Fallback to system beep
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }
    
    // Game Over dialog
    private void showGameOverDialog() {
        playSound("gameOver");
        
        // Record the high score
        String currentGameMode = isMultiplayerMode ? "Multiplayer" : 
                               (player1Type == 1) ? "AI Mode" : "Single Player";
        addCurrentScore(currentGameMode, "Player");
        
        int result = JOptionPane.showOptionDialog(
            this,
            "Game Over!\n\nScore: " + score + "\nLevel: " + level + "\nLines: " + linesCleared + 
            "\n\n✅ Score saved to high scores!\n\nWhat would you like to do?",
            "Game Over",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            new String[]{"Play Again", "Main Menu", "Exit"},
            "Play Again"
        );
        
        switch (result) {
            case 0: // Play Again
                restartGame();
                break;
            case 1: // Main Menu
                returnToHomeScreen();
                break;
            case 2: // Exit
            default:
                System.exit(0);
                break;
        }
    }
    
    // AI Win Dialog for single player
    private void showAIWinDialog() {
        playSound("gameOver");
        
        // Record the AI's high score
        addCurrentScore("AI Mode", "AI");
        
        int result = JOptionPane.showOptionDialog(
            this,
            "🤖 AI WINS! 🤖\n\n" +
            "AI reached " + aiWinScore + " points!\n" +
            "Final Score: " + score + "\n" +
            "Level: " + level + "\n" +
            "Lines: " + linesCleared + 
            "\n\n✅ AI score saved to high scores!\n" +
            "Better luck next time, human! 😎\n\nWhat would you like to do?",
            "AI Victory!",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            new String[]{"Try Again", "Main Menu", "Exit"},
            "Try Again"
        );
        
        switch (result) {
            case 0: // Try Again
                restartGame();
                break;
            case 1: // Main Menu
                returnToHomeScreen();
                break;
            case 2: // Exit
            default:
                System.exit(0);
                break;
        }
    }
    
    // AI Win Dialog for multiplayer
    private void showMultiplayerAIWinDialog(int aiPlayerNum) {
        playSound("gameOver");
        String aiPlayerName = (aiPlayerNum == 1) ? player1Name : player2Name;
        int aiScore = (aiPlayerNum == 1) ? score : score2;
        int aiLevel = (aiPlayerNum == 1) ? level : level2;
        int aiLines = (aiPlayerNum == 1) ? linesCleared : linesCleared2;
        
        String message = "🤖 " + aiPlayerName + " (AI) WINS! 🤖\n\n" +
                        "AI Player " + aiPlayerNum + " reached " + aiWinScore + " points!\n\n" +
                        "Final Scores:\n" +
                        player1Name + " (P1): " + score + " points\n" +
                        player2Name + " (P2): " + score2 + " points\n\n" +
                        "AI Stats:\n" +
                        "Score: " + aiScore + "\n" +
                        "Level: " + aiLevel + "\n" +
                        "Lines: " + aiLines + 
                        "\n\nThe machines are taking over! 🤖⚡\n\nWhat would you like to do?";
        
        int result = JOptionPane.showOptionDialog(
            this,
            message,
            "AI Victory!",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            new String[]{"Play Again", "Main Menu", "Exit"},
            "Play Again"
        );
        
        switch (result) {
            case 0: // Play Again
                restartMultiplayerGame();
                break;
            case 1: // Main Menu
                returnToHomeScreen();
                break;
            case 2: // Exit
            default:
                System.exit(0);
                break;
        }
    }
    
    // Show temporary message to user
    private void showMessage(String message) {
        // Just print to console for debugging
        System.out.println(message);
    }

    private void showMultiplayerPlayerWinDialog(int playerNum) {
        playSound("gameOver");

        String winnerName = (playerNum == 1) ? player1Name : player2Name;
        int winnerScore   = (playerNum == 1) ? score : score2;
        int winnerLevel   = (playerNum == 1) ? level : level2;
        int winnerLines   = (playerNum == 1) ? linesCleared : linesCleared2;

        String message =
                "🎉 " + winnerName + " WINS! 🎉\n\n" +
                        "Reached " + aiWinScore + " points first.\n\n" +
                        "Final Scores:\n" +
                        player1Name + " (P1): " + score  + " points\n" +
                        player2Name + " (P2): " + score2 + " points\n\n" +
                        "Winner Stats:\n" +
                        "Score: " + winnerScore + "\n" +
                        "Level: " + winnerLevel + "\n" +
                        "Lines: " + winnerLines + "\n\n" +
                        "What would you like to do?";

        int result = JOptionPane.showOptionDialog(
                this, message, "Victory!",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Play Again", "Main Menu", "Exit"},
                "Play Again"
        );

        switch (result) {
            case 0: restartMultiplayerGame(); break;
            case 1: returnToHomeScreen();     break;
            default: System.exit(0);          break;
        }
    }

    // Ends multiplayer immediately when someone hits the target score.
    // Forces a repaint so the UI shows the final score (e.g., 500) before the dialog.
    private void endMultiplayerOnScoreWin(int winnerNum, boolean isAI) {
        gameOver  = true;
        gameOver2 = true;

        // Make sure the last score update is visible
        repaint();

        if (gameTimer != null) gameTimer.stop();

        if (isAI) {
            showMultiplayerAIWinDialog(winnerNum);
        } else {
            showMultiplayerPlayerWinDialog(winnerNum);
        }
    }
    
    // Save/Load Configuration functionality
    private void saveConfiguration() {
        GameData.Config config = new GameData.Config(
            startingLevel, showGhostPiece, showNextPiece, gameTheme,
            soundEnabled, musicEnabled, (int)musicVolume, (int)effectsVolume, aiWinScore,
            BOARD_WIDTH, BOARD_HEIGHT
        );
        
        if (GameData.saveConfig(config)) {
            showMessage("✅ Configuration saved successfully!");
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Configuration saved successfully!\n\nSettings will be restored when you restart the game.",
                "Save Complete", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } else {
            showMessage("❌ Failed to save configuration");
            javax.swing.JOptionPane.showMessageDialog(this, 
                "Failed to save configuration. Please check file permissions.",
                "Save Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadConfiguration() {
        GameData.Config config = GameData.loadConfig();
        
        // Apply loaded configuration
        startingLevel = config.startingLevel;
        showGhostPiece = config.showGhostPiece;
        showNextPiece = config.showNextPiece;
        gameTheme = config.gameTheme;
        soundEnabled = config.soundEnabled;
        musicEnabled = config.musicEnabled;
        musicVolume = (float)config.musicVolume;
        effectsVolume = (float)config.effectsVolume;
        aiWinScore = config.aiWinScore;
        
        // Apply field dimensions if they're different
        if (BOARD_WIDTH != config.fieldWidth || BOARD_HEIGHT != config.fieldHeight) {
            BOARD_WIDTH = config.fieldWidth;
            BOARD_HEIGHT = config.fieldHeight;
            resetGameBoardsWithNewDimensions();
        }
        
        // Update sound system with loaded settings
        if (soundManager != null) {
            soundManager.setMusicVolume(musicVolume);
            soundManager.setEffectsVolume(effectsVolume);
        }
        
        // Re-center window after loading new settings
        centerWindow();
        showMessage("✅ Configuration loaded successfully!");
    }
    
    private void showHighScores() {
        java.util.List<GameData.HighScore> scores = GameData.loadHighScores();
        
        if (scores.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "No high scores yet!\n\nPlay some games to set records.",
                "High Scores", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        StringBuilder message = new StringBuilder("🏆 HIGH SCORES 🏆\\n\\n");
        message.append("Rank | Player | Score | Level | Lines | Mode | Type | Date\\n");
        message.append("-----|--------|-------|-------|-------|------|------|-----\\n");
        
        for (int i = 0; i < Math.min(scores.size(), 10); i++) {
            GameData.HighScore score = scores.get(i);
            message.append(String.format("%2d   | %-6s | %5d | %5d | %5d | %-4s | %-4s | %s\\n",
                i + 1, score.playerName, score.score, score.level, score.lines,
                score.gameMode, score.playerType, score.date.substring(0, 10)));
        }
        
        javax.swing.JOptionPane.showMessageDialog(this, message.toString(),
            "High Scores", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void resetAllData() {
        int result = javax.swing.JOptionPane.showConfirmDialog(this,
            "⚠️ WARNING ⚠️\\n\\n" +
            "This will permanently delete:\\n" +
            "• All saved configuration settings\\n" +
            "• All high scores\\n" +
            "• Game progress data\\n\\n" +
            "This action cannot be undone!\\n\\n" +
            "Are you sure you want to continue?",
            "Reset All Data",
            javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.WARNING_MESSAGE);
        
        if (result == javax.swing.JOptionPane.YES_OPTION) {
            boolean configDeleted = GameData.deleteConfig();
            boolean scoresDeleted = GameData.deleteScores();
            
            if (configDeleted || scoresDeleted) {
                // Reset to defaults
                startingLevel = 1;
                showGhostPiece = true;
                showNextPiece = true;
                gameTheme = "Classic";
                soundEnabled = true;
                musicEnabled = true;
                musicVolume = 70;
                effectsVolume = 80;
                aiWinScore = 500;
                
                javax.swing.JOptionPane.showMessageDialog(this,
                    "✅ All data has been reset!\\n\\n" +
                    "Configuration and high scores have been deleted.\\n" +
                    "Game has been restored to default settings.",
                    "Reset Complete", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    
                showMessage("🔄 All data reset to defaults");
            } else {
                javax.swing.JOptionPane.showMessageDialog(this,
                    "❌ No data files found to delete.\\n\\n" +
                    "Settings are already at defaults.",
                    "Reset Info", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            }
            
            repaint();
        }
    }
    
    // Add high score when game ends
    private void addCurrentScore(String gameMode, String playerType) {
        if (score > 0) { // Only add if score is meaningful
            String playerName = isMultiplayerMode ? 
                (playerType.equals("Player1") ? player1Name : player2Name) : "Player";
            
            if (playerName == null || playerName.trim().isEmpty()) {
                playerName = "Anonymous";
            }
            
            GameData.addHighScore(playerName, score, level, linesCleared, gameMode, playerType);
            showMessage("📊 Score recorded: " + score + " points");
        }
    }
    
    // AI Player class
    private class AIPlayer {
        private int player;
        private long lastMoveTime = 0;
        private int moveDelay = 200; // Delay between AI moves (milliseconds)
        
        public AIPlayer(int playerNumber) {
            this.player = playerNumber;
        }
        
        public void makeMove() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMoveTime < moveDelay) {
                return; // Not time for next move yet
            }
            lastMoveTime = currentTime;
            
            if (player == 1) {
                makePlayer1Move();
            } else {
                makePlayer2Move();
            }
        }
        
        private void makePlayer1Move() {
            if (gameOver) return;
            
            // Get best position for current piece
            AIMove bestMove = calculateBestMove(board, PIECES[currentPiece], currentPiece);
            
            if (bestMove != null) {
                // Rotate to target rotation
                while (currentRotation != bestMove.rotation) {
                    if (canMove(currentX, currentY, (currentRotation + 1) % 4)) {
                        currentRotation = (currentRotation + 1) % 4;
                    } else {
                        break;
                    }
                }
                
                // Move to target X position
                if (currentX < bestMove.x && canMove(currentX + 1, currentY, currentRotation)) {
                    currentX++;
                } else if (currentX > bestMove.x && canMove(currentX - 1, currentY, currentRotation)) {
                    currentX--;
                } else if (currentX == bestMove.x) {
                    // Drop piece faster when in correct position
                    if (canMove(currentX, currentY + 1, currentRotation)) {
                        currentY++;
                    }
                }
            }
        }
        
        private void makePlayer2Move() {
            if (gameOver2) return;
            
            // Get best position for current piece
            AIMove bestMove = calculateBestMove(board2, PIECES[currentPiece2], currentPiece2);
            
            if (bestMove != null) {
                // Rotate to target rotation
                while (currentRotation2 != bestMove.rotation) {
                    if (canMove2(currentX2, currentY2, (currentRotation2 + 1) % 4)) {
                        currentRotation2 = (currentRotation2 + 1) % 4;
                    } else {
                        break;
                    }
                }
                
                // Move to target X position
                if (currentX2 < bestMove.x && canMove2(currentX2 + 1, currentY2, currentRotation2)) {
                    currentX2++;
                } else if (currentX2 > bestMove.x && canMove2(currentX2 - 1, currentY2, currentRotation2)) {
                    currentX2--;
                } else if (currentX2 == bestMove.x) {
                    // Drop piece faster when in correct position
                    if (canMove2(currentX2, currentY2 + 1, currentRotation2)) {
                        currentY2++;
                    }
                }
            }
        }
        
        private AIMove calculateBestMove(int[][] gameBoard, int[][] basePiece, int pieceType) {
            AIMove bestMove = null;
            double bestScore = Double.NEGATIVE_INFINITY;
            
            // Try all rotations
            for (int rotation = 0; rotation < 4; rotation++) {
                // Get rotated piece
                int[][] rotatedPiece = rotatePiece(basePiece, rotation);
                
                // Try all horizontal positions
                for (int x = 0; x < BOARD_WIDTH; x++) {
                    // Find the lowest valid Y position for this X and rotation
                    int y = findLowestPosition(gameBoard, rotatedPiece, x);
                    
                    if (y >= 0) { // Valid position found
                        // Create a copy of the board and simulate placing the piece
                        int[][] testBoard = copyBoard(gameBoard);
                        if (simulatePlacePiece(testBoard, rotatedPiece, x, y)) {
                            double score = evaluateBoard(testBoard);
                            if (score > bestScore) {
                                bestScore = score;
                                bestMove = new AIMove(x, rotation);
                            }
                        }
                    }
                }
            }
            
            return bestMove;
        }
        
        private int findLowestPosition(int[][] gameBoard, int[][] piece, int x) {
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                if (!isValidPosition(gameBoard, piece, x, y)) {
                    return y - 1; // Return the last valid position
                }
            }
            return BOARD_HEIGHT - 1; // If piece can go all the way down
        }
        
        private boolean isValidPosition(int[][] gameBoard, int[][] piece, int x, int y) {
            for (int py = 0; py < piece.length; py++) {
                for (int px = 0; px < piece[py].length; px++) {
                    if (piece[py][px] == 1) {
                        int boardX = x + px;
                        int boardY = y + py;
                        
                        if (boardX < 0 || boardX >= BOARD_WIDTH || 
                            boardY < 0 || boardY >= BOARD_HEIGHT ||
                            gameBoard[boardY][boardX] != 0) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        
        private boolean simulatePlacePiece(int[][] testBoard, int[][] piece, int x, int y) {
            // Place the piece on test board
            for (int py = 0; py < piece.length; py++) {
                for (int px = 0; px < piece[py].length; px++) {
                    if (piece[py][px] == 1) {
                        int boardX = x + px;
                        int boardY = y + py;
                        if (boardX >= 0 && boardX < BOARD_WIDTH && boardY >= 0 && boardY < BOARD_HEIGHT) {
                            testBoard[boardY][boardX] = 1;
                        }
                    }
                }
            }
            return true;
        }
        
        private int[][] copyBoard(int[][] original) {
            int[][] copy = new int[BOARD_HEIGHT][BOARD_WIDTH];
            for (int i = 0; i < BOARD_HEIGHT; i++) {
                System.arraycopy(original[i], 0, copy[i], 0, BOARD_WIDTH);
            }
            return copy;
        }
        
        private double evaluateBoard(int[][] testBoard) {
            double score = 0;
            
            // Calculate height of each column
            int[] heights = new int[BOARD_WIDTH];
            for (int x = 0; x < BOARD_WIDTH; x++) {
                for (int y = 0; y < BOARD_HEIGHT; y++) {
                    if (testBoard[y][x] != 0) {
                        heights[x] = BOARD_HEIGHT - y;
                        break;
                    }
                }
            }
            
            // Penalize height
            for (int height : heights) {
                score -= height * 0.5;
            }
            
            // Penalize height differences (bumpiness)
            for (int x = 0; x < BOARD_WIDTH - 1; x++) {
                score -= Math.abs(heights[x] - heights[x + 1]) * 0.5;
            }
            
            // Count holes and penalize them
            int holes = 0;
            for (int x = 0; x < BOARD_WIDTH; x++) {
                boolean foundBlock = false;
                for (int y = 0; y < BOARD_HEIGHT; y++) {
                    if (testBoard[y][x] != 0) {
                        foundBlock = true;
                    } else if (foundBlock) {
                        holes++;
                    }
                }
            }
            score -= holes * 2;
            
            // Reward clearing lines
            int linesCleared = countClearedLines(testBoard);
            score += linesCleared * linesCleared * 10; // Quadratic bonus for multiple lines
            
            return score;
        }
        
        private int countClearedLines(int[][] testBoard) {
            int cleared = 0;
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                boolean fullLine = true;
                for (int x = 0; x < BOARD_WIDTH; x++) {
                    if (testBoard[y][x] == 0) {
                        fullLine = false;
                        break;
                    }
                }
                if (fullLine) cleared++;
            }
            return cleared;
        }
    }
    
    // AI Move class to store move information
    private class AIMove {
        int x, rotation;
        
        public AIMove(int x, int rotation) {
            this.x = x;
            this.rotation = rotation;
        }
    }
    
    // AI Players
    private AIPlayer aiPlayer1;
    private AIPlayer aiPlayer2;
    
    // Getters for external access
    public int getScore() { return score; }
    public int getLevel() { return level; }
    public int getLinesCleared() { return linesCleared; }
    public boolean isGameOver() { return gameOver; }
    public boolean isPaused() { return paused; }
    public int[][] getBoard() { return board.clone(); }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create single game window with multiplayer support built-in
            TetrisGame game = new TetrisGame();
            game.setVisible(true);
        });
    }
    
    // Dynamic window sizing methods
    private void adjustWindowSize() {
        Dimension newSize = calculateOptimalWindowSize();
        setSize(newSize);
        setMinimumSize(newSize);
        centerWindow();
        revalidate();
        repaint();
    }
    
    private Dimension calculateOptimalWindowSize() {
        // Always use large window size for consistent UI experience
        // Calculate the largest possible size needed for multiplayer mode
        int multiWidth = (2 * BOARD_WIDTH * BLOCK_SIZE) + (2 * 160) + 100; // ~820px
        int multiHeight = BOARD_HEIGHT * BLOCK_SIZE + 200; // ~800px (extra height for UI)
        
        // Use the large size for all modes to maintain consistency
        int fixedWidth = Math.max(multiWidth, 850);  // Minimum 850px width
        int fixedHeight = Math.max(multiHeight, 800); // Minimum 800px height
        
        return new Dimension(fixedWidth, fixedHeight);
    }
    
    private void centerWindow() {
        setLocationRelativeTo(null); // Center on screen
    }
    
    private void drawHighscoreScreen(Graphics2D g) {
        // Set background
        g.setColor(getThemeBackgroundColor());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Apply modern styling
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Title
        g.setColor(getThemeTextColor());
        g.setFont(new Font("Arial", Font.BOLD, 36));
        FontMetrics titleFm = g.getFontMetrics();
        String title = "🏆 HIGH SCORES";
        int titleX = (getWidth() - titleFm.stringWidth(title)) / 2;
        g.drawString(title, titleX, 80);
        
        // Subtitle
        g.setFont(new Font("Arial", Font.ITALIC, 16));
        FontMetrics subtitleFm = g.getFontMetrics();
        String subtitle = "Single Player Mode";
        int subtitleX = (getWidth() - subtitleFm.stringWidth(subtitle)) / 2;
        g.setColor(new Color(100, 100, 100));
        g.drawString(subtitle, subtitleX, 110);
        
        // Load and display high scores
        java.util.List<GameData.HighScore> highScores = GameData.loadHighScores();
        
        if (highScores == null || highScores.isEmpty()) {
            // No high scores found
            g.setColor(getThemeTextColor());
            g.setFont(new Font("Arial", Font.BOLD, 18));
            FontMetrics noScoreFm = g.getFontMetrics();
            String noScoreText = "No high scores yet!";
            String playText = "Play some games to set records!";
            
            int noScoreX = (getWidth() - noScoreFm.stringWidth(noScoreText)) / 2;
            int playX = (getWidth() - noScoreFm.stringWidth(playText)) / 2;
            
            g.drawString(noScoreText, noScoreX, 200);
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.setColor(new Color(120, 120, 120));
            g.drawString(playText, playX, 230);
        } else {
            // Display high scores table
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.setColor(getThemeTextColor());
            
            // Table headers
            int startY = 160;
            int lineHeight = 35;
            
            g.drawString("RANK", 100, startY);
            g.drawString("SCORE", 200, startY);
            g.drawString("LEVEL", 320, startY);
            g.drawString("LINES", 420, startY);
            g.drawString("DATE", 520, startY);
            
            // Draw separator line
            g.setColor(new Color(100, 100, 100));
            g.drawLine(80, startY + 10, getWidth() - 80, startY + 10);
            
            // Display top 10 scores
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            for (int i = 0; i < Math.min(10, highScores.size()); i++) {
                GameData.HighScore score = highScores.get(i);
                int y = startY + 30 + (i * lineHeight);
                
                // Highlight top 3
                if (i < 3) {
                    Color[] medalColors = {
                        new Color(255, 215, 0),   // Gold
                        new Color(192, 192, 192), // Silver  
                        new Color(205, 127, 50)   // Bronze
                    };
                    g.setColor(medalColors[i]);
                    g.setFont(new Font("Arial", Font.BOLD, 14));
                } else {
                    g.setColor(getThemeTextColor());
                    g.setFont(new Font("Arial", Font.PLAIN, 14));
                }
                
                // Medal symbols for top 3
                String rank = (i < 3) ? 
                    new String[]{"🥇", "🥈", "🥉"}[i] + " #" + (i + 1) :
                    "#" + (i + 1);
                
                g.drawString(rank, 100, y);
                g.drawString(String.format("%,d", score.score), 200, y);
                g.drawString(String.valueOf(score.level), 320, y);
                g.drawString(String.valueOf(score.lines), 420, y);
                g.drawString(score.date, 520, y);
            }
        }
        
        // Instructions
        g.setColor(new Color(100, 100, 100));
        g.setFont(new Font("Arial", Font.ITALIC, 12));
        FontMetrics instructFm = g.getFontMetrics();
        String instruction = "Press ESCAPE or ENTER to return to menu";
        int instructX = (getWidth() - instructFm.stringWidth(instruction)) / 2;
        g.drawString(instruction, instructX, getHeight() - 50);
        
        // Reset antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    
    private void resetToDefaultSize() {
        // Just center the window, maintain consistent large size
        centerWindow();
        showMessage("🏠 Window centered!");
    }
    
    // Save/Load Game System Methods
    private void checkSaveSlots() {
        for (int i = 0; i < MAX_SAVE_SLOTS; i++) {
            saveSlotExists[i] = GameData.saveSlotExists(i);
        }
    }
    
    private boolean hasAnySaveFiles() {
        for (boolean exists : saveSlotExists) {
            if (exists) return true;
        }
        return false;
    }
    
    private void showLoadGameScreen() {
        showLoadGameScreen = true;
        showHomeScreen = false;
        selectedSaveSlot = 0;
        checkSaveSlots(); // Refresh save slot status
    }
    
    private void drawLoadGameScreen(Graphics2D g) {
        // Clear background
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Apply modern styling
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw title
        g.setColor(new Color(70, 130, 180));
        g.fillRoundRect(50, 20, getWidth() - 100, 60, 15, 15);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        String title = "📁 Load Game";
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g.drawString(title, titleX, 55);
        
        // Draw save slots
        int startY = 140;
        int slotHeight = 120;
        int slotWidth = getWidth() - 120;
        
        for (int i = 0; i < MAX_SAVE_SLOTS; i++) {
            int slotY = startY + i * (slotHeight + 20);
            
            // Slot background
            if (i == selectedSaveSlot) {
                g.setColor(new Color(100, 150, 255, 180));
            } else {
                g.setColor(new Color(255, 255, 255, 220));
            }
            g.fillRoundRect(60, slotY, slotWidth, slotHeight, 15, 15);
            
            // Slot border
            g.setColor(i == selectedSaveSlot ? new Color(70, 130, 180) : new Color(100, 100, 100));
            g.setStroke(new BasicStroke(i == selectedSaveSlot ? 3 : 1));
            g.drawRoundRect(60, slotY, slotWidth, slotHeight, 15, 15);
            g.setStroke(new BasicStroke(1));
            
            // Slot content
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Slot " + (i + 1), 80, slotY + 30);
            
            if (saveSlotExists[i]) {
                // Load save info to display
                GameData.GameSave save = GameData.loadGame(i);
                if (save != null) {
                    g.setFont(new Font("Arial", Font.PLAIN, 14));
                    g.drawString("Player: " + save.playerName, 80, slotY + 55);
                    g.drawString("Score: " + String.format("%,d", save.score), 80, slotY + 75);
                    g.drawString("Level: " + save.level + " | Lines: " + save.linesCleared, 80, slotY + 95);
                    g.setColor(new Color(100, 100, 100));
                    g.setFont(new Font("Arial", Font.ITALIC, 12));
                    g.drawString("Saved: " + save.saveDate, 320, slotY + 55);
                }
            } else {
                g.setColor(new Color(150, 150, 150));
                g.setFont(new Font("Arial", Font.ITALIC, 16));
                g.drawString("Empty Save", 80, slotY + 75);
            }
        }
        
        // Instructions
        int instructY = startY + MAX_SAVE_SLOTS * (slotHeight + 20) + 20;
        g.setColor(new Color(245, 245, 245));
        g.fillRoundRect(50, instructY, getWidth() - 100, 80, 15, 15);
        g.setColor(new Color(100, 100, 100));
        g.drawRoundRect(50, instructY, getWidth() - 100, 80, 15, 15);
        
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.setColor(new Color(60, 60, 60));
        g.drawString("🎮 UP/DOWN: Select slot | ENTER: Load game | DELETE: Delete save | ESC: Back", 70, instructY + 30);
        g.drawString("💡 Only saved games can be loaded", 70, instructY + 50);
        
        // Reset antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    
    private void handleLoadGameInput(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                selectedSaveSlot = (selectedSaveSlot - 1 + MAX_SAVE_SLOTS) % MAX_SAVE_SLOTS;
                break;
            case KeyEvent.VK_DOWN:
                selectedSaveSlot = (selectedSaveSlot + 1) % MAX_SAVE_SLOTS;
                break;
            case KeyEvent.VK_ENTER:
                if (saveSlotExists[selectedSaveSlot]) {
                    loadGameFromSlot(selectedSaveSlot);
                }
                break;
            case KeyEvent.VK_DELETE:
                if (saveSlotExists[selectedSaveSlot]) {
                    deleteGameSave(selectedSaveSlot);
                }
                break;
            case KeyEvent.VK_ESCAPE:
                showLoadGameScreen = false;
                showHomeScreen = true;
                break;
        }
        repaint();
    }
    
    private void loadGameFromSlot(int slot) {
        GameData.GameSave save = GameData.loadGame(slot);
        if (save != null) {
            // Restore game state
            board = save.board;
            currentPiece = save.currentPiece;
            currentX = save.currentX;
            currentY = save.currentY;
            currentRotation = save.currentRotation;
            nextPiece = save.nextPiece;
            score = save.score;
            level = save.level;
            linesCleared = save.linesCleared;
            paused = save.paused;
            gameOver = false;
            
            // Start game
            showLoadGameScreen = false;
            isMultiplayerMode = false;
            
            // Start game timer
            if (gameTimer != null) {
                gameTimer.stop();
            }
            gameTimer = new javax.swing.Timer(Math.max(50, 500 - (level - 1) * 50), e -> gameStep());
            gameTimer.start();
            
            // Play background music
            if (musicEnabled && soundManager != null) {
                soundManager.playBackgroundMusic("background", musicVolume);
            }
            
            JOptionPane.showMessageDialog(this,
                "Game loaded successfully!\n\nWelcome back, " + save.playerName + "!",
                "Load Complete", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to load game from slot " + (slot + 1) + ".\nSave file may be corrupted.",
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteGameSave(int slot) {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete save slot " + (slot + 1) + "?\nThis action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            if (GameData.deleteSaveSlot(slot)) {
                saveSlotExists[slot] = false;
                JOptionPane.showMessageDialog(this,
                    "Save slot " + (slot + 1) + " deleted successfully!",
                    "Delete Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to delete save slot " + (slot + 1) + ".",
                    "Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveCurrentGame() {
        // Find an empty slot or ask user to choose
        int emptySlot = -1;
        for (int i = 0; i < MAX_SAVE_SLOTS; i++) {
            if (!saveSlotExists[i]) {
                emptySlot = i;
                break;
            }
        }
        
        if (emptySlot == -1) {
            // All slots full, ask user which to overwrite
            String[] options = new String[MAX_SAVE_SLOTS];
            for (int i = 0; i < MAX_SAVE_SLOTS; i++) {
                options[i] = "Slot " + (i + 1);
            }
            
            String choice = (String) JOptionPane.showInputDialog(this,
                "All save slots are full. Which slot would you like to overwrite?",
                "Choose Save Slot", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            
            if (choice != null) {
                for (int i = 0; i < options.length; i++) {
                    if (options[i].equals(choice)) {
                        emptySlot = i;
                        break;
                    }
                }
            } else {
                return; // User cancelled
            }
        }
        
        // Save game
        String playerName = JOptionPane.showInputDialog(this, 
            "Enter your name for this save:", 
            "Save Game", JOptionPane.QUESTION_MESSAGE);
        
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player";
        }
        
        GameData.GameSave save = new GameData.GameSave(
            board, currentPiece, currentX, currentY, currentRotation,
            nextPiece, score, level, linesCleared, paused, playerName.trim()
        );
        
        if (GameData.saveGame(save, emptySlot)) {
            saveSlotExists[emptySlot] = true;
            JOptionPane.showMessageDialog(this,
                "Game saved successfully to slot " + (emptySlot + 1) + "!\n\nYou can continue playing or return to menu.",
                "Save Complete", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to save game. Please try again.",
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
