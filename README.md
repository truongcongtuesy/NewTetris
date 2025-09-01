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
- Java Development Kit (JDK) 8 or higher installed
- Command line terminal

### Compilation and Execution

1. Open a terminal and navigate to the project directory:
   ```powershell
   cd "C:\D\Australia Study\Tetris and Tetris"
   ```

2. Compile the Java file:
   ```powershell
   javac TetrisGame.java
   ```

3. Run the game:
   ```powershell
   java TetrisGame
   ```

## Configuration

You can modify the game behavior by changing the constants at the top of the `TetrisGame.java` file:

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
