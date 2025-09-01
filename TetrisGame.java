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
    private int selectedMenuItem = 0; // 0 = Play Game, 1 = Settings, 2 = Exit
    private final String[] menuItems = {"Play Game", "Settings", "Exit"};
    
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
    
    // Timer for game loop
    private javax.swing.Timer gameTimer;
    private int fallSpeed = 500; // milliseconds
    
    public TetrisGame() {
        setTitle("Tetris Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Initialize game
        initializeGame();
        
        // Set up the display
        setSize(BOARD_WIDTH * BLOCK_SIZE + 200, BOARD_HEIGHT * BLOCK_SIZE + 100);
        setLocationRelativeTo(null);
        
        // Add key listener
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        
        // Setup splash screen timer (3 seconds)
        javax.swing.Timer splashTimer = new javax.swing.Timer(3000, e -> {
            showSplashScreen = false;
            showHomeScreen = true;
            repaint();
        });
        splashTimer.setRepeats(false);
        splashTimer.start();
        
        // Don't start game timer immediately - wait for user to select "Play Game"
        fallSpeed = 500;
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
    
    private void gameStep() {
        if (showHomeScreen || showConfigScreen || gameOver || paused) return;
        
        // Move piece down
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
                showGameOverDialog();
            }
        }
        
        // Only repaint once per game step
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
            
            // Draw UI
            drawUI(offGraphics);
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
                    } else if (selectedMenuItem == 1) { // Settings
                        showConfigScreen();
                    } else if (selectedMenuItem == 2) { // Exit
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
        if (gameOver) return;
        
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
    
    private void startGame() {
        showHomeScreen = false;
        initializeGame();
        fallSpeed = 500;
        if (gameTimer != null) {
            gameTimer.stop();
        }
        gameTimer = new javax.swing.Timer(fallSpeed, e -> gameStep());
        gameTimer.start();
        repaint();
    }
    
    private void returnToHomeScreen() {
        showHomeScreen = true;
        showConfigScreen = false;
        gameOver = false;
        paused = false;
        if (gameTimer != null) {
            gameTimer.stop();
        }
        selectedMenuItem = 0; // Reset to "Play Game"
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
        
        // Placeholder for sound effects
        // In a full implementation, you would load and play actual sound files
        switch (soundType) {
            case "move":
                // Play move sound
                java.awt.Toolkit.getDefaultToolkit().beep();
                break;
            case "rotate":
                // Play rotate sound  
                break;
            case "drop":
                // Play drop sound
                break;
            case "clear":
                // Play line clear sound
                java.awt.Toolkit.getDefaultToolkit().beep();
                break;
            case "pause":
            case "resume":
                // Play pause/resume sound
                break;
            case "gameOver":
                // Play game over sound
                java.awt.Toolkit.getDefaultToolkit().beep();
                break;
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
    
    // Show temporary message to user
    private void showMessage(String message) {
        // For now, just print to console
        // In a full implementation, you could show a temporary overlay on screen
        System.out.println(message);
    }
    
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
