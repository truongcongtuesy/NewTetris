# Coding Snippets Documentation
## Java Programming Features Demonstrated in Tetris Game

This document provides code snippets with explanations demonstrating various Java programming concepts implemented in our Tetris game project.

---

## 1. JavaFX (GUI Framework Usage)

**Note:** While our project uses Java Swing instead of JavaFX, here's how the GUI framework is utilized:

### Code Snippet:
```java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TetrisGame extends JFrame implements KeyListener {
    // GUI Components initialization
    public TetrisGame() {
        setTitle("Tetris Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(BOARD_WIDTH * BLOCK_SIZE + 200, BOARD_HEIGHT * BLOCK_SIZE + 100);
        setLocationRelativeTo(null);
        addKeyListener(this);
        setFocusable(true);
    }
    
    // Double buffering for smooth graphics
    @Override
    public void paint(Graphics g) {
        Dimension size = getSize();
        Image offScreen = createImage(size.width, size.height);
        Graphics2D offGraphics = (Graphics2D) offScreen.getGraphics();
        offGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                   RenderingHints.VALUE_ANTIALIAS_ON);
        // Drawing operations...
        g.drawImage(offScreen, 0, 0, this);
    }
}
```

### Explanation:
- **JFrame**: Main window container for the game
- **Graphics2D**: Advanced graphics rendering with anti-aliasing
- **Double Buffering**: Creates smooth animations by drawing to an off-screen buffer first
- **Event Handling**: Implements KeyListener interface for user input
- **Layout Management**: Custom positioning and sizing of game components

---

## 2. Enhanced for Loop

### Code Snippet:
```java
// Clearing completed lines using enhanced for loop with arrays
private void clearLines() {
    ArrayList<Integer> linesToClear = new ArrayList<>();
    
    // Find completed lines
    for (int y = 0; y < BOARD_HEIGHT; y++) {
        boolean lineComplete = true;
        for (int block : board[y]) {  // Enhanced for loop
            if (block == 0) {
                lineComplete = false;
                break;
            }
        }
        if (lineComplete) {
            linesToClear.add(y);
        }
    }
    
    // Process cleared lines
    for (Integer lineIndex : linesToClear) {  // Enhanced for loop with collections
        // Remove completed line
        for (int moveY = lineIndex; moveY > 0; moveY--) {
            System.arraycopy(board[moveY - 1], 0, board[moveY], 0, BOARD_WIDTH);
        }
        Arrays.fill(board[0], 0);
    }
}

// Color array iteration
private void drawPiece(Graphics2D g, int pieceType, int x, int y) {
    Color[] pieceColors = {Color.CYAN, Color.BLUE, Color.ORANGE, Color.YELLOW};
    
    for (Color color : pieceColors) {  // Enhanced for loop with arrays
        // Process each color
        g.setColor(color);
        // Drawing logic...
    }
}
```

### Explanation:
- **Enhanced for loop with arrays**: `for (int block : board[y])` iterates through each element in the array row
- **Enhanced for loop with collections**: `for (Integer lineIndex : linesToClear)` iterates through ArrayList elements
- **Type safety**: Automatically handles casting and bounds checking
- **Readability**: More concise than traditional for loops when index isn't needed
- **Performance**: Compiler optimizations make it as efficient as traditional loops

---

## 3. Enhanced Switch Statement

### Code Snippet:
```java
// Modern switch expressions for theme management
private Color getThemeBackgroundColor() {
    return switch (gameTheme) {
        case "Classic" -> Color.BLACK;
        case "Dark" -> new Color(25, 25, 25);
        case "Colorful" -> new Color(50, 50, 100);
        default -> Color.BLACK;
    };
}

private Color getThemeTextColor() {
    return switch (gameTheme) {
        case "Classic" -> Color.WHITE;
        case "Dark" -> Color.LIGHT_GRAY;
        case "Colorful" -> Color.CYAN;
        default -> Color.WHITE;
    };
}

// Enhanced switch for key handling
@Override
public void keyPressed(KeyEvent e) {
    if (showHomeScreen) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> 
                selectedMenuItem = (selectedMenuItem - 1 + menuItems.length) % menuItems.length;
            case KeyEvent.VK_DOWN -> 
                selectedMenuItem = (selectedMenuItem + 1) % menuItems.length;
            case KeyEvent.VK_ENTER -> {
                switch (selectedMenuItem) {
                    case 0 -> startGame();
                    case 1 -> showConfigScreen();
                    case 2 -> System.exit(0);
                }
            }
        }
    }
}

// Pattern matching with switch (Java 17+)
private String getConfigItemText(int index) {
    return switch (index) {
        case 0 -> "Starting Level: " + startingLevel;
        case 1 -> "Ghost Piece: " + (showGhostPiece ? "ON" : "OFF");
        case 2 -> "Next Piece: " + (showNextPiece ? "ON" : "OFF");
        case 3 -> "Sound Effects: " + (soundEnabled ? "ON" : "OFF");
        case 4 -> "Background Music: " + (musicEnabled ? "ON" : "OFF");
        case 5 -> "Game Theme: " + gameTheme;
        case 6 -> "Back to Menu";
        default -> "";
    };
}
```

### Explanation:
- **Switch expressions**: Return values directly using arrow syntax (`->`)
- **No fall-through**: Each case is independent, no need for `break` statements
- **Pattern matching**: More concise and readable than traditional switch
- **Block expressions**: Use `{}` for multiple statements in a case
- **Default handling**: Compiler ensures all cases are covered
- **Type safety**: Return type is checked at compile time

---

## 4. Interface Usage

### Code Snippet:
```java
// Implementing multiple interfaces for event handling
public class TetrisGame extends JFrame implements KeyListener, ActionListener {
    
    // KeyListener interface implementation
    @Override
    public void keyPressed(KeyEvent e) {
        // Handle key press events
        processGameInput(e);
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        // Handle key release events (empty implementation)
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Handle key typed events (empty implementation)
    }
    
    // ActionListener interface implementation for timer events
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == gameTimer) {
            gameStep(); // Execute game logic step
            repaint(); // Refresh display
        }
    }
}

// Custom interface for game components
interface GameComponent {
    void update();
    void render(Graphics2D g);
    boolean isActive();
}

// Example implementation (could be added for modular design)
class TetrisPiece implements GameComponent {
    private int x, y, rotation;
    private int[][] shape;
    private boolean active;
    
    @Override
    public void update() {
        // Update piece position and state
        if (active) {
            // Game logic for piece movement
        }
    }
    
    @Override
    public void render(Graphics2D g) {
        // Draw the piece on screen
        if (active && shape != null) {
            // Rendering logic
        }
    }
    
    @Override
    public boolean isActive() {
        return active;
    }
}
```

### Explanation:
- **Multiple interface implementation**: Class implements both KeyListener and ActionListener
- **Contract enforcement**: Interfaces ensure specific methods are implemented
- **Polymorphism**: Objects can be treated as their interface types
- **Loose coupling**: Components depend on interfaces, not concrete implementations
- **Event-driven programming**: Interfaces enable callback mechanisms
- **Default methods**: Interfaces can provide default implementations (Java 8+)

---

## 5. Abstract Class Usage

### Code Snippet:
```java
// Abstract base class for different game modes
abstract class GameMode {
    protected int score;
    protected int level;
    protected boolean gameActive;
    protected TetrisGame gameInstance;
    
    public GameMode(TetrisGame game) {
        this.gameInstance = game;
        this.score = 0;
        this.level = 1;
        this.gameActive = false;
    }
    
    // Abstract methods that subclasses must implement
    public abstract void processInput(KeyEvent e);
    public abstract void updateGame();
    public abstract void handleGameOver();
    public abstract String getModeName();
    
    // Concrete methods available to all subclasses
    public void startGame() {
        gameActive = true;
        score = 0;
        level = 1;
        initializeGame();
    }
    
    public void pauseGame() {
        gameActive = false;
    }
    
    public int getScore() {
        return score;
    }
    
    protected void addScore(int points) {
        score += points;
        updateLevel();
    }
    
    private void updateLevel() {
        level = (score / 1000) + 1; // Level up every 1000 points
    }
    
    // Template method pattern
    protected final void gameStep() {
        if (gameActive) {
            updateGame();
            checkGameOver();
        }
    }
    
    private void checkGameOver() {
        // Common game over logic
        if (isGameOver()) {
            gameActive = false;
            handleGameOver();
        }
    }
    
    protected abstract boolean isGameOver();
    protected abstract void initializeGame();
}

// Concrete implementation for single player mode
class SinglePlayerMode extends GameMode {
    
    public SinglePlayerMode(TetrisGame game) {
        super(game);
    }
    
    @Override
    public void processInput(KeyEvent e) {
        // Handle single player input
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> gameInstance.movePieceLeft();
            case KeyEvent.VK_RIGHT -> gameInstance.movePieceRight();
            case KeyEvent.VK_DOWN -> gameInstance.movePieceDown();
            case KeyEvent.VK_UP -> gameInstance.rotatePiece();
            case KeyEvent.VK_SPACE -> gameInstance.dropPiece();
        }
    }
    
    @Override
    public void updateGame() {
        // Single player game logic
        gameInstance.gameStep();
    }
    
    @Override
    public void handleGameOver() {
        // Single player game over handling
        gameInstance.showGameOverDialog();
    }
    
    @Override
    public String getModeName() {
        return "Single Player";
    }
    
    @Override
    protected boolean isGameOver() {
        return gameInstance.isGameOver();
    }
    
    @Override
    protected void initializeGame() {
        gameInstance.initializeGame();
    }
}

// Concrete implementation for multiplayer mode
class MultiPlayerMode extends GameMode {
    private int player1Score, player2Score;
    
    public MultiPlayerMode(TetrisGame game) {
        super(game);
    }
    
    @Override
    public void processInput(KeyEvent e) {
        // Handle multiplayer input for both players
        // Player 1 controls: WASD
        // Player 2 controls: Arrow keys
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A -> gameInstance.movePieceLeft();
            case KeyEvent.VK_D -> gameInstance.movePieceRight();
            case KeyEvent.VK_S -> gameInstance.movePieceDown();
            case KeyEvent.VK_W -> gameInstance.rotatePiece();
            // Player 2 controls...
        }
    }
    
    @Override
    public void updateGame() {
        // Multiplayer-specific game logic
        gameInstance.gameStep();
        // Update both players' boards
    }
    
    @Override
    public void handleGameOver() {
        // Multiplayer game over - determine winner
        String winner = (player1Score > player2Score) ? "Player 1" : "Player 2";
        JOptionPane.showMessageDialog(gameInstance, 
            "Game Over! Winner: " + winner + 
            "\nPlayer 1 Score: " + player1Score + 
            "\nPlayer 2 Score: " + player2Score);
    }
    
    @Override
    public String getModeName() {
        return "Multiplayer";
    }
    
    @Override
    protected boolean isGameOver() {
        return gameInstance.isGameOver();
    }
    
    @Override
    protected void initializeGame() {
        gameInstance.initializeGame();
        player1Score = 0;
        player2Score = 0;
    }
}
```

### Explanation:
- **Abstract class**: Cannot be instantiated directly, serves as a base for inheritance
- **Abstract methods**: Must be implemented by subclasses, defining a contract
- **Concrete methods**: Provide common functionality to all subclasses
- **Template method pattern**: `gameStep()` defines algorithm structure, delegates specifics to subclasses
- **Protected access**: Allows subclasses to access parent members while hiding from external classes
- **Code reuse**: Common functionality is implemented once in the abstract class
- **Polymorphism**: Can treat different game modes uniformly through the abstract base class

---

## 6. Record Type Usage

### Code Snippet:
```java
// Modern Java record types for immutable data structures
public record GameState(
    int score,
    int level,
    int linesCleared,
    boolean gameOver,
    boolean paused,
    long gameTime
) {
    // Compact constructor for validation
    public GameState {
        if (score < 0) throw new IllegalArgumentException("Score cannot be negative");
        if (level < 1) throw new IllegalArgumentException("Level must be at least 1");
        if (linesCleared < 0) throw new IllegalArgumentException("Lines cleared cannot be negative");
    }
    
    // Custom methods
    public GameState nextLevel() {
        return new GameState(score, level + 1, linesCleared, gameOver, paused, gameTime);
    }
    
    public GameState addScore(int points) {
        return new GameState(score + points, level, linesCleared, gameOver, paused, gameTime);
    }
    
    public GameState clearLine() {
        return new GameState(score + 100, level, linesCleared + 1, gameOver, paused, gameTime);
    }
}

// Record for position coordinates
public record Position(int x, int y) {
    public Position move(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }
    
    public boolean isValid(int maxX, int maxY) {
        return x >= 0 && x < maxX && y >= 0 && y < maxY;
    }
}

// Record for game configuration
public record GameConfig(
    boolean soundEnabled,
    boolean musicEnabled,
    boolean showGhostPiece,
    boolean showNextPiece,
    String theme,
    int startingLevel
) {
    // Default configuration factory method
    public static GameConfig defaultConfig() {
        return new GameConfig(true, true, true, true, "Classic", 1);
    }
    
    // Builder-like methods for configuration changes
    public GameConfig withSound(boolean enabled) {
        return new GameConfig(enabled, musicEnabled, showGhostPiece, 
                            showNextPiece, theme, startingLevel);
    }
    
    public GameConfig withTheme(String newTheme) {
        return new GameConfig(soundEnabled, musicEnabled, showGhostPiece,
                            showNextPiece, newTheme, startingLevel);
    }
}

// Record for piece information
public record PieceInfo(
    int type,
    int rotation,
    Position position,
    int[][] shape
) {
    public PieceInfo rotate() {
        return new PieceInfo(type, (rotation + 1) % 4, position, rotateShape(shape));
    }
    
    public PieceInfo move(int dx, int dy) {
        return new PieceInfo(type, rotation, position.move(dx, dy), shape);
    }
    
    private int[][] rotateShape(int[][] original) {
        // Matrix rotation logic
        int rows = original.length;
        int cols = original[0].length;
        int[][] rotated = new int[cols][rows];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                rotated[j][rows - 1 - i] = original[i][j];
            }
        }
        return rotated;
    }
}

// Usage in the main game class
public class TetrisGame extends JFrame implements KeyListener {
    private GameState currentState;
    private GameConfig config;
    private PieceInfo currentPiece;
    
    public void initializeGame() {
        currentState = new GameState(0, 1, 0, false, false, System.currentTimeMillis());
        config = GameConfig.defaultConfig();
        currentPiece = new PieceInfo(0, 0, new Position(5, 0), PIECES[0]);
    }
    
    public void updateScore(int points) {
        currentState = currentState.addScore(points);
    }
    
    public void clearLine() {
        currentState = currentState.clearLine();
        repaint();
    }
    
    public void movePiece(int dx, int dy) {
        PieceInfo newPiece = currentPiece.move(dx, dy);
        if (isValidPosition(newPiece)) {
            currentPiece = newPiece;
        }
    }
    
    private boolean isValidPosition(PieceInfo piece) {
        // Validation logic using record data
        return piece.position().isValid(BOARD_WIDTH, BOARD_HEIGHT);
    }
}
```

### Explanation:
- **Immutable data**: Records are inherently immutable, preventing accidental state changes
- **Automatic generation**: Compiler generates constructor, getters, equals(), hashCode(), toString()
- **Compact constructor**: Validates parameters during object creation
- **Value semantics**: Two records with same data are considered equal
- **Pattern matching**: Records work well with pattern matching (Java 17+)
- **Functional style**: Encourages creating new instances rather than modifying existing ones
- **Thread safety**: Immutability makes records inherently thread-safe
- **Memory efficiency**: JVM can optimize record instances

---

## Summary

This Tetris game implementation demonstrates modern Java programming concepts:

1. **JavaFX/Swing**: Professional GUI development with event handling and graphics
2. **Enhanced for loops**: Cleaner iteration over collections and arrays
3. **Enhanced switch**: Modern pattern matching and expression-based switching
4. **Interfaces**: Contract-based programming and event handling
5. **Abstract classes**: Template method pattern and inheritance hierarchies
6. **Records**: Immutable data structures for game state management

Each feature contributes to cleaner, more maintainable, and more efficient code while following modern Java best practices.
