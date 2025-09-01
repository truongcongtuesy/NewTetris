# Tetris Game

A complete Tetris implementation in Java using Swing.

## Features

- Classic Tetris gameplay with all 7 standard pieces (I, O, T, S, Z, J, L)
- Ghost piece preview showing where the current piece will land
- Next piece preview
- Score tracking and level progression
- Line clearing with proper scoring (40, 100, 300, 1200 points for 1-4 lines)
- Increasing game speed with higher levels
- Pause functionality
- Game over detection and restart capability
- Configurable features:
  - Multiplayer mode (2 players)
  - AI player mode
  - External control interface
  - Show/hide next piece and ghost piece

## Controls

- **A/D** or **Arrow Keys (Left/Right)** - Move piece horizontally
- **S** or **Arrow Key (Down)** - Soft drop (move piece down faster)
- **W** or **Arrow Key (Up)** - Rotate piece clockwise
- **Space** - Hard drop (instantly drop piece to bottom)
- **P** - Pause/unpause game
- **R** - Restart game (works during game over)

## How to Run

### Prerequisites
- **Java Development Kit (JDK) 8 or higher** installed on your computer
- Command line terminal (Command Prompt, PowerShell, or Terminal)

### Download and Run the Game

#### Method 1: Download from GitHub (Recommended)

1. **Download the project**:
   - Go to: https://github.com/truongcongtuesy/NewTetris
   - Click the green **"Code"** button
   - Select **"Download ZIP"**
   - Extract the ZIP file to your desired location

2. **Or clone with Git** (if you have Git installed):
   ```bash
   git clone https://github.com/truongcongtuesy/NewTetris.git
   cd NewTetris
   ```

3. **Navigate to the project folder**:
   ```bash
   cd path/to/NewTetris
   # Example on Windows: cd "C:\Downloads\NewTetris-main"
   # Example on Mac/Linux: cd ~/Downloads/NewTetris-main
   ```

4. **Compile the Java file**:
   ```bash
   javac TetrisGame.java
   ```

5. **Run the game**:
   ```bash
   java TetrisGame
   ```

#### Method 2: Quick Run (if already compiled)
If the `.class` files are already present, you can skip compilation:
```bash
java TetrisGame
```

### Troubleshooting

**❌ "javac is not recognized" or "java is not recognized"**
- Make sure Java JDK is installed
- Add Java to your system PATH
- Download Java from: https://www.oracle.com/java/technologies/downloads/

**❌ "Could not find or load main class TetrisGame"**
- Make sure you're in the correct directory containing `TetrisGame.java`
- Ensure the file was compiled successfully (check for `TetrisGame.class`)

**❌ Game window doesn't appear**
- Check if your system supports Java Swing GUI
- Try running from a different terminal or as administrator

### Quick Start Guide
1. Download → Extract → Open Terminal in folder
2. Run: `javac TetrisGame.java`
3. Run: `java TetrisGame`
4. Enjoy! 🎮

## Step-by-Step Guide for Beginners

### For Windows Users:

1. **Check if Java is installed**:
   - Press `Win + R`, type `cmd`, press Enter
   - Type: `java -version`
   - If Java is not found, download from: https://www.oracle.com/java/technologies/downloads/

2. **Download the game**:
   - Visit: https://github.com/truongcongtuesy/NewTetris
   - Click green "Code" button → "Download ZIP"
   - Extract to Desktop (you'll get a folder like "NewTetris-main")

3. **Open Command Prompt in the game folder**:
   - Open the extracted folder
   - Hold `Shift` + Right-click in empty space
   - Select "Open PowerShell window here" or "Open command window here"

4. **Run these commands one by one**:
   ```cmd
   javac TetrisGame.java
   java TetrisGame
   ```

### For Mac Users:

1. **Check Java installation**:
   - Open Terminal (Applications → Utilities → Terminal)
   - Type: `java -version`

2. **Download and extract the game** (same as Windows)

3. **Navigate to folder**:
   ```bash
   cd ~/Desktop/NewTetris-main
   ```

4. **Run the game**:
   ```bash
   javac TetrisGame.java
   java TetrisGame
   ```

### For Linux Users:

1. **Install Java** (if not installed):
   ```bash
   sudo apt update
   sudo apt install default-jdk
   ```

2. **Download and run**:
   ```bash
   wget https://github.com/truongcongtuesy/NewTetris/archive/main.zip
   unzip main.zip
   cd NewTetris-main
   javac TetrisGame.java
   java TetrisGame
   ```

## Game Controls

### 🏠 Home Screen:
- **↑↓ (Up/Down arrows)** - Navigate menu
- **Enter** - Select option
- **ESC** - Return to home (from game)

### 🎮 In-Game Controls:

#### Single-Player Mode:
- **,/.** or **A/D** or **←→** - Move piece horizontally 
- **Space** or **↓** - Move down (Space = hard drop, ↓ = soft drop)
- **L** or **W** or **↑** - Rotate piece clockwise

#### Additional Controls:
- **P** - Pause/Resume the game
- **M** - Toggle Music on/off
- **Ctrl+S** - Toggle Sound Effects on/off
- **R** - Restart game (works during game over)
- **ESC** - Return to Home Screen

#### Two-Player Mode (Future Extension):
- **Player 1**: Use ,.SpaceL keys
- **Player 2**: Use arrow keys for corresponding actions

### 🎯 Game Features:
- **Ghost Piece**: Semi-transparent preview showing where piece will land
- **Next Piece**: Preview of the next piece coming
- **Score System**: Points awarded for clearing lines (more lines = more points!)
- **Level Progression**: Game speeds up every 10 lines cleared
- **Pause Function**: Press P to pause and resume
- **Sound Effects**: Audio feedback for moves, rotations, and line clears (toggle with Ctrl+S)
- **Music Control**: Background music toggle (press M)
- **Flexible Controls**: Multiple key options for each action

## Configuration

- `MULTIPLAYER` - Set to `true` for 2-player mode
- `AI_ENABLED` - Set to `true` to enable AI player
- `EXTERNAL_CONTROL` - Set to `true` for external control interface
- `SHOW_NEXT_PIECE` - Show/hide next piece preview
- `SHOW_GHOST_PIECE` - Show/hide ghost piece preview

## Game Mechanics

- **Scoring**: Lines cleared award points based on the number cleared simultaneously:
  - 1 line: 40 × level
  - 2 lines: 100 × level  
  - 3 lines: 300 × level
  - 4 lines (Tetris): 1200 × level

- **Level Progression**: Level increases every 10 lines cleared (maximum level 20)

- **Speed**: Game speed increases with each level, making pieces fall faster

- **Line Clearing**: Complete horizontal lines are removed and all lines above drop down

## Architecture

The game is implemented as a single Java class (`TetrisGame`) that extends `JFrame` and implements `KeyListener`. Key components include:

- **Game Board**: 10×20 grid represented as a 2D integer array
- **Piece System**: 7 standard Tetris pieces with rotation logic
- **Collision Detection**: Boundary and piece overlap checking
- **Rendering**: Custom paint method using Java 2D Graphics
- **Game Loop**: Timer-based game state updates
- **Input Handling**: Keyboard event processing for game controls

## Additional Features

- **AI Player**: Simple AI that attempts to move pieces to the left side
- **External Control**: API methods for programmatic control of pieces
- **Multiplayer**: Support for two simultaneous game windows
- **Visual Effects**: Semi-transparent ghost pieces and smooth graphics

Enjoy playing Tetris!
