import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class TetrisGame extends JFrame implements KeyListener {
    // Game configuration
    private static final boolean MULTIPLAYER = false; // Set to true for 2-player mode
    
    // Sound and music settings
    private static boolean soundEnabled = true;
    private static boolean musicEnabled = true;
    
    // Game dimensions
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
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
    private int selectedMenuItem = 0; // 0 = Play Game, 1 = Multiplayer, 2 = Settings, 3 = Exit
    private final String[] menuItems = {"Play Game", "Multiplayer", "Settings", "Exit"};
    
    // Config screen state
    private int selectedConfigItem = 0;
    private final String[] configItems = {
        "Starting Level", "Ghost Piece", "Next Piece", "Sound Effects", 
        "Background Music", "Game Theme", "Back to Menu"
    };
    
    // Configuration settings
    private static int startingLevel = 1;
    private static boolean showGhostPiece = true;
    private static boolean showNextPiece = true;
    private static String gameTheme = "Classic";
    private static final String[] themes = {"Classic", "Dark", "Colorful"};
    private static int aiWinScore = AI_WIN_SCORE; // Configurable AI win score
    
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
        
        // Initialize game
        initializeGame();
        
        // Set up the display - adjust size for multiplayer
        if (isMultiplayerMode) {
            // For multiplayer: 2 boards + 2 info panels + spacing
            int multiWidth = (2 * BOARD_WIDTH * BLOCK_SIZE) + (2 * 160) + 100; // 100 for spacing
            int multiHeight = BOARD_HEIGHT * BLOCK_SIZE + 150; // Extra height for title and panels
            setSize(multiWidth, multiHeight);
            setMinimumSize(new Dimension(multiWidth, multiHeight));
        } else {
            // Single player size
            setSize(BOARD_WIDTH * BLOCK_SIZE + 200, BOARD_HEIGHT * BLOCK_SIZE + 100);
            setMinimumSize(new Dimension(BOARD_WIDTH * BLOCK_SIZE + 200, BOARD_HEIGHT * BLOCK_SIZE + 100));
        }
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
            if (player1Type == 1 && aiPlayer1 != null) { // AI player
                aiPlayer1.makeMove();
            }
            
            if (canMove(currentX, currentY + 1, currentRotation)) {
                currentY++;
            } else {
                placePiece();
                clearLines();
                
                // Check if AI Player 1 reached win score
                if (player1Type == 1 && score >= aiWinScore) { // AI reached target points
                    gameOver = true;
                    gameOver2 = true; // End both games
                    gameTimer.stop();
                    showMultiplayerAIWinDialog(1);
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
            if (player2Type == 1 && aiPlayer2 != null) { // AI player
                aiPlayer2.makeMove();
            }
            
            if (canMove2(currentX2, currentY2 + 1, currentRotation2)) {
                currentY2++;
            } else {
                placePiece2();
                clearLines2();
                
                // Check if AI Player 2 reached win score
                if (player2Type == 1 && score2 >= aiWinScore) { // AI reached target points
                    gameOver = true;
                    gameOver2 = true; // End both games
                    gameTimer.stop();
                    showMultiplayerAIWinDialog(2);
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
        } else if (showConfigScreen) {
            drawConfigScreen(offGraphics);
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
                
                // Draw next piece if enabled in config
                if (showNextPiece) {
                    drawNextPiece(offGraphics);
                }
                
                // Draw UI only for single player
                drawUI(offGraphics);
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
            // Highlight selected item
            if (i == selectedMenuItem) {
                g.setColor(Color.YELLOW);
                g.drawString("> " + menuItems[i] + " <", 
                           (getWidth() - fm.stringWidth("> " + menuItems[i] + " <")) / 2, 
                           280 + i * 60);
            } else {
                g.setColor(Color.WHITE);
                g.drawString(menuItems[i], 
                           (getWidth() - fm.stringWidth(menuItems[i])) / 2, 
                           280 + i * 60);
            }
        }
        
        // Draw instructions
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(Color.LIGHT_GRAY);
        fm = g.getFontMetrics();
        String[] instructions = {
            "Use UP/DOWN arrows to navigate",
            "Press ENTER to select",
            "Controls: ,/. (move), L (rotate), Space (drop)",
            "M (music), Ctrl+S (sound), P (pause)"
        };
        
        for (int i = 0; i < instructions.length; i++) {
            g.drawString(instructions[i], 
                       (getWidth() - fm.stringWidth(instructions[i])) / 2, 
                       450 + i * 20);
        }
    }
    
    private void drawConfigScreen(Graphics2D g) {
        // Clear background
        g.setColor(getThemeBackgroundColor());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw title
        g.setColor(getThemeTextColor());
        g.setFont(new Font("Arial", Font.BOLD, 36));
        FontMetrics fm = g.getFontMetrics();
        String title = "SETTINGS";
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        g.drawString(title, titleX, 120);
        
        // Draw config items
        g.setFont(new Font("Arial", Font.BOLD, 20));
        fm = g.getFontMetrics();
        
        for (int i = 0; i < configItems.length; i++) {
            int yPos = 200 + i * 50;
            
            // Highlight selected item
            if (i == selectedConfigItem) {
                g.setColor(Color.YELLOW);
                g.drawString("> " + getConfigItemText(i) + " <", 
                           (getWidth() - fm.stringWidth("> " + getConfigItemText(i) + " <")) / 2, 
                           yPos);
            } else {
                g.setColor(getThemeTextColor());
                g.drawString(getConfigItemText(i), 
                           (getWidth() - fm.stringWidth(getConfigItemText(i))) / 2, 
                           yPos);
            }
        }
        
        // Draw instructions
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(Color.LIGHT_GRAY);
        fm = g.getFontMetrics();
        String[] instructions = {
            "Use UP/DOWN arrows to navigate",
            "Use LEFT/RIGHT arrows to change values",
            "Press ENTER to select, ESC to go back"
        };
        
        for (int i = 0; i < instructions.length; i++) {
            g.drawString(instructions[i], 
                       (getWidth() - fm.stringWidth(instructions[i])) / 2, 
                       500 + i * 20);
        }
    }
    
    private String getConfigItemText(int index) {
        switch (index) {
            case 0: return "Starting Level: " + startingLevel;
            case 1: return "Ghost Piece: " + (showGhostPiece ? "ON" : "OFF");
            case 2: return "Next Piece: " + (showNextPiece ? "ON" : "OFF");
            case 3: return "Sound Effects: " + (soundEnabled ? "ON" : "OFF");
            case 4: return "Background Music: " + (musicEnabled ? "ON" : "OFF");
            case 5: return "Theme: " + gameTheme;
            case 6: return "Back to Menu";
            default: return "";
        }
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
        FontMetrics fm = g.getFontMetrics();
        
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
                g.drawString("Controls:", x + 10, textY);
                textY += 15;
                g.drawString("A/←: Left", x + 10, textY);
                textY += 12;
                g.drawString("D/→: Right", x + 10, textY);
                textY += 12;
                g.drawString("S/↓: Down", x + 10, textY);
                textY += 12;
                g.drawString("W/↑: Rotate", x + 10, textY);
            } else {
                g.drawString("Controls:", x + 10, textY);
                textY += 15;
                g.drawString("J: Left", x + 10, textY);
                textY += 12;
                g.drawString("L: Right", x + 10, textY);
                textY += 12;
                g.drawString("K: Down", x + 10, textY);
                textY += 12;
                g.drawString("I: Rotate", x + 10, textY);
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
    
    private void drawGameOverAtPosition(Graphics2D g, int x, int y, int width) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(x, y - 20, width, 40);
        
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        String text = "GAME OVER";
        g.drawString(text, x + (width - fm.stringWidth(text)) / 2, y);
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
        int startX = BOARD_WIDTH * BLOCK_SIZE + 30;
        int startY = 100;
        
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Next:", startX, startY - 10);
        
        int[][] piece = PIECES[nextPiece];
        g.setColor(COLORS[nextPiece % COLORS.length]);
        
        for (int py = 0; py < piece.length; py++) {
            for (int px = 0; px < piece[py].length; px++) {
                if (piece[py][px] == 1) {
                    g.fillRect(startX + px * 20, startY + py * 20, 20, 20);
                    g.setColor(Color.BLACK);
                    g.drawRect(startX + px * 20, startY + py * 20, 20, 20);
                    g.setColor(COLORS[nextPiece % COLORS.length]);
                }
            }
        }
    }
    
    private void drawUI(Graphics2D g) {
        int startX = BOARD_WIDTH * BLOCK_SIZE + 30;
        int startY = 200;
        
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        
        g.drawString("Score: " + score, startX, startY);
        g.drawString("Level: " + level, startX, startY + 25);
        g.drawString("Lines: " + linesCleared, startX, startY + 50);
        
        if (paused) {
            g.drawString("PAUSED", startX, startY + 100);
        }
        
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.setColor(Color.RED);
            g.drawString("GAME OVER", 50, BOARD_HEIGHT * BLOCK_SIZE / 2 + 50);
        }
        
        // Controls
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Controls:", startX, startY + 150);
        g.drawString("Move: ,/. or A/D", startX, startY + 170);
        g.drawString("Down: Space/↓", startX, startY + 190);
        g.drawString("Rotate: L or W/↑", startX, startY + 210);
        g.drawString("Pause: P", startX, startY + 230);
        g.drawString("Sound: Ctrl+S", startX, startY + 250);
        g.drawString("Music: M", startX, startY + 270);
        g.drawString("ESC: Home Menu", startX, startY + 290);
        
        // Sound/Music status
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Sound: " + (soundEnabled ? "ON" : "OFF"), startX, startY + 320);
        g.drawString("Music: " + (musicEnabled ? "ON" : "OFF"), startX, startY + 340);
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
                    if (selectedMenuItem == 0) { // Play Game
                        startGame();
                    } else if (selectedMenuItem == 1) { // Multiplayer
                        showPlayerSelection = true;
                        showHomeScreen = false;
                        playerSelectionIndex = 0;
                        // Reset names and types
                        player1Name = "";
                        player2Name = "";
                        player1Type = 0;
                        player2Type = 0;
                    } else if (selectedMenuItem == 2) { // Settings
                        showConfigScreen();
                    } else if (selectedMenuItem == 3) { // Exit
                        System.exit(0);
                    }
                    break;
            }
            repaint();
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
        
        repaint();
        
        switch (e.getKeyCode()) {
            // Single Player Controls
            case KeyEvent.VK_COMMA:      // Move left (,)
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                if (canMove(currentX - 1, currentY, currentRotation)) {
                    currentX--;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_PERIOD:     // Move right (.)
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                if (canMove(currentX + 1, currentY, currentRotation)) {
                    currentX++;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_SPACE:      // Move down (Space)
            case KeyEvent.VK_DOWN:
                if (canMove(currentX, currentY + 1, currentRotation)) {
                    currentY++;
                    score++;
                } else {
                    // Hard drop with Space
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        while (canMove(currentX, currentY + 1, currentRotation)) {
                            currentY++;
                            score += 2;
                        }
                    }
                }
                playSound("drop");
                break;
            case KeyEvent.VK_L:          // Rotate (L)
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                int newRotation = (currentRotation + 1) % 4;
                if (canMove(currentX, currentY, newRotation)) {
                    currentRotation = newRotation;
                    playSound("rotate");
                }
                break;
            
            // Additional Controls
            case KeyEvent.VK_S:          // Toggle Sound
                if (e.isControlDown()) { // Ctrl+S to avoid conflict with soft drop
                    soundEnabled = !soundEnabled;
                    showMessage("Sound: " + (soundEnabled ? "ON" : "OFF"));
                }
                break;
            case KeyEvent.VK_M:          // Toggle Music
                musicEnabled = !musicEnabled;
                showMessage("Music: " + (musicEnabled ? "ON" : "OFF"));
                break;
            case KeyEvent.VK_P:          // Pause/Resume
                paused = !paused;
                if (paused) {
                    gameTimer.stop();
                    playSound("pause");
                } else {
                    gameTimer.start();
                    playSound("resume");
                }
                break;
            case KeyEvent.VK_R:          // Restart
                restartGame();
                break;
            case KeyEvent.VK_ESCAPE:     // Return to Home Screen
                if (!gameOver && !showHomeScreen) {
                    // In-game, ask for confirmation before quitting
                    int result = JOptionPane.showConfirmDialog(
                            this,
                            "Quit current game and return to the menu?",
                            "Confirm Quit",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (result == JOptionPane.YES_OPTION) {
                        returnToHomeScreen();
                    }
                } else {
                    // If game over or already in home screen, just return directly
                    returnToHomeScreen();
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
            // Player 1 Controls (WASD + Arrow Keys)
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                if (!gameOver && canMove(currentX - 1, currentY, currentRotation)) {
                    currentX--;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                if (!gameOver && canMove(currentX + 1, currentY, currentRotation)) {
                    currentX++;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                if (!gameOver && canMove(currentX, currentY + 1, currentRotation)) {
                    currentY++;
                    score += 1;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                if (!gameOver) {
                    int newRotation = (currentRotation + 1) % 4;
                    if (canMove(currentX, currentY, newRotation)) {
                        currentRotation = newRotation;
                        playSound("rotate");
                    }
                }
                break;
                
            // Player 2 Controls (IJKL)
            case KeyEvent.VK_J:
                if (!gameOver2 && canMove2(currentX2 - 1, currentY2, currentRotation2)) {
                    currentX2--;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_L:
                if (!gameOver2 && canMove2(currentX2 + 1, currentY2, currentRotation2)) {
                    currentX2++;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_K:
                if (!gameOver2 && canMove2(currentX2, currentY2 + 1, currentRotation2)) {
                    currentY2++;
                    score2 += 1;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_I:
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
            // Movement controls
            case KeyEvent.VK_COMMA:      // Move left (,)
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                if (canMove(currentX - 1, currentY, currentRotation)) {
                    currentX--;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_PERIOD:     // Move right (.)
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                if (canMove(currentX + 1, currentY, currentRotation)) {
                    currentX++;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_K:          // Soft drop (K)
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                if (canMove(currentX, currentY + 1, currentRotation)) {
                    currentY++;
                    score += 1;
                    playSound("move");
                }
                break;
            case KeyEvent.VK_L:          // Rotate (L)
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                int newRotation = (currentRotation + 1) % 4;
                if (canMove(currentX, currentY, newRotation)) {
                    currentRotation = newRotation;
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
                    int result = JOptionPane.showConfirmDialog(
                            this,
                            "Quit current game and return to menu?",
                            "Confirm Quit",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );
                    if (result == JOptionPane.YES_OPTION) {
                        returnToHomeScreen();
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
        
        // Resize window for multiplayer
        int multiWidth = (2 * BOARD_WIDTH * BLOCK_SIZE) + (2 * 160) + 100; // 100 for spacing
        int multiHeight = BOARD_HEIGHT * BLOCK_SIZE + 150; // Extra height for title and panels
        setSize(multiWidth, multiHeight);
        setLocationRelativeTo(null);
        
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
    
    private void returnToHomeScreen() {
        showHomeScreen = true;
        showConfigScreen = false;
        showPlayerSelection = false;
        showNameEntry = false;
        isMultiplayerMode = false;
        gameOver = false;
        gameOver2 = false;
        paused = false;
        if (gameTimer != null) {
            gameTimer.stop();
        }
        selectedMenuItem = 0; // Reset to "Play Game"
        
        // Resize window back to single player size
        int singleWidth = BOARD_WIDTH * BLOCK_SIZE + 200;
        int singleHeight = BOARD_HEIGHT * BLOCK_SIZE + 100;
        setSize(singleWidth, singleHeight);
        setLocationRelativeTo(null);
        
        // Start menu music
        if (musicEnabled && soundManager != null) {
            soundManager.stopBackgroundMusic();
            soundManager.playBackgroundMusic("menu", musicVolume);
        }
        
        repaint();
    }
    
    private void showConfigScreen() {
        showHomeScreen = false;
        showConfigScreen = true;
        selectedConfigItem = 0;
        repaint();
    }
    
    private void changeConfigValue(int direction) {
        switch (selectedConfigItem) {
            case 0: // Starting Level
                startingLevel = Math.max(1, Math.min(20, startingLevel + direction));
                break;
            case 1: // Ghost Piece
                showGhostPiece = !showGhostPiece;
                break;
            case 2: // Next Piece
                showNextPiece = !showNextPiece;
                break;
            case 3: // Sound Effects
                soundEnabled = !soundEnabled;
                showMessage("Sound Effects: " + (soundEnabled ? "ON" : "OFF"));
                break;
            case 4: // Background Music
                musicEnabled = !musicEnabled;
                showMessage("Background Music: " + (musicEnabled ? "ON" : "OFF"));
                break;
            case 5: // Theme
                int currentThemeIndex = 0;
                for (int i = 0; i < themes.length; i++) {
                    if (themes[i].equals(gameTheme)) {
                        currentThemeIndex = i;
                        break;
                    }
                }
                currentThemeIndex = (currentThemeIndex + direction + themes.length) % themes.length;
                gameTheme = themes[currentThemeIndex];
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
        int result = JOptionPane.showOptionDialog(
            this,
            "Game Over!\n\nScore: " + score + "\nLevel: " + level + "\nLines: " + linesCleared + 
            "\n\nWhat would you like to do?",
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
        int result = JOptionPane.showOptionDialog(
            this,
            "🤖 AI WINS! 🤖\n\n" +
            "AI reached " + aiWinScore + " points!\n" +
            "Final Score: " + score + "\n" +
            "Level: " + level + "\n" +
            "Lines: " + linesCleared + 
            "\n\nBetter luck next time, human! 😎\n\nWhat would you like to do?",
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
        // For now, just print to console
        // In a full implementation, you could show a temporary overlay on screen
        System.out.println(message);
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
                                bestMove = new AIMove(x, y, rotation, score);
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
        int x, y, rotation;
        double score;
        
        public AIMove(int x, int y, int rotation, double score) {
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            this.score = score;
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
            // No look and feel setting - use default
            
            if (MULTIPLAYER) {
                // Create two game windows for multiplayer
                TetrisGame game1 = new TetrisGame();
                game1.setTitle("Tetris - Player 1");
                game1.setLocation(100, 100);
                game1.setVisible(true);
                
                TetrisGame game2 = new TetrisGame();
                game2.setTitle("Tetris - Player 2");
                game2.setLocation(500, 100);
                game2.setVisible(true);
            } else {
                // Single player mode
                TetrisGame game = new TetrisGame();
                game.setVisible(true);
            }
        });
    }
}
