import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class TetrisGame extends JFrame implements KeyListener {
    // Game configuration
    private static final boolean MULTIPLAYER = false; // Set to true for 2-player mode
    private static final boolean SHOW_NEXT_PIECE = true;
    private static final boolean SHOW_GHOST_PIECE = true;
    
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
    private boolean showHomeScreen = true;
    private int selectedMenuItem = 0; // 0 = Play Game, 1 = Exit
    private final String[] menuItems = {"Play Game", "Exit"};
    
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
        level = 1;
        linesCleared = 0;
        gameOver = false;
        paused = false;
    }
    
    private void gameStep() {
        if (showHomeScreen || gameOver || paused) return;
        
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
                JOptionPane.showMessageDialog(this, "Game Over! Score: " + score);
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
        
        if (showHomeScreen) {
            drawHomeScreen(offGraphics);
        } else {
            // Draw board
            drawBoard(offGraphics);
            
            // Draw current piece
            if (!gameOver) {
                drawCurrentPiece(offGraphics);
                
                // Draw ghost piece if enabled
                if (SHOW_GHOST_PIECE) {
                    drawGhostPiece(offGraphics);
                }
            }
            
            // Draw next piece if enabled
            if (SHOW_NEXT_PIECE) {
                drawNextPiece(offGraphics);
            }
            
            // Draw UI
            drawUI(offGraphics);
        }
        
        // Draw the off-screen image to the main graphics
        g.drawImage(offScreen, 0, 0, this);
        offGraphics.dispose();
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
            "Press ESC to return to menu"
        };
        
        for (int i = 0; i < instructions.length; i++) {
            g.drawString(instructions[i], 
                       (getWidth() - fm.stringWidth(instructions[i])) / 2, 
                       450 + i * 20);
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
        g.drawString("A/D - Move", startX, startY + 170);
        g.drawString("S - Soft Drop", startX, startY + 190);
        g.drawString("W - Rotate", startX, startY + 210);
        g.drawString("Space - Hard Drop", startX, startY + 230);
        g.drawString("P - Pause", startX, startY + 250);
        g.drawString("R - Restart", startX, startY + 270);
        g.drawString("ESC - Home Menu", startX, startY + 290);
    }
    
    // Key controls
    @Override
    public void keyPressed(KeyEvent e) {
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
                    } else if (selectedMenuItem == 1) { // Exit
                        System.exit(0);
                    }
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
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                if (canMove(currentX - 1, currentY, currentRotation)) {
                    currentX--;
                }
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                if (canMove(currentX + 1, currentY, currentRotation)) {
                    currentX++;
                }
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                if (canMove(currentX, currentY + 1, currentRotation)) {
                    currentY++;
                    score++;
                }
                break;
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                int newRotation = (currentRotation + 1) % 4;
                if (canMove(currentX, currentY, newRotation)) {
                    currentRotation = newRotation;
                }
                break;
            case KeyEvent.VK_SPACE:
                // Hard drop
                while (canMove(currentX, currentY + 1, currentRotation)) {
                    currentY++;
                    score += 2;
                }
                break;
            case KeyEvent.VK_P:
                paused = !paused;
                if (paused) {
                    gameTimer.stop();
                } else {
                    gameTimer.start();
                }
                break;
            case KeyEvent.VK_R:
                restartGame();
                break;
            case KeyEvent.VK_ESCAPE:
                returnToHomeScreen();
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
        gameOver = false;
        paused = false;
        if (gameTimer != null) {
            gameTimer.stop();
        }
        selectedMenuItem = 0; // Reset to "Play Game"
        repaint();
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
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
