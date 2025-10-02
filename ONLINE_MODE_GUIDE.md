# Tetris Online Mode Setup Guide

## Overview
The Tetris game now supports **Online Mode** that connects to a TetrisServer for AI-assisted gameplay. The server analyzes your current game state and provides optimal move recommendations.

## Prerequisites

### 1. GSON Library
The game requires GSON library for JSON communication:
- **Already included**: `lib/gson-2.10.1.jar`
- **Auto-downloaded**: Run `download-gson.bat` if missing

### 2. TetrisServer
You need a TetrisServer running on `localhost:3000` that implements the specification:

#### Server Requirements:
- **Host**: `localhost`
- **Port**: `3000` 
- **Protocol**: Socket connection with JSON messages
- **Input**: `PurGame` object (game state)
- **Output**: `OpMove` object (optimal move)

## How to Use Online Mode

### Step 1: Start TetrisServer
```bash
# Example - start your TetrisServer implementation
python tetris_server.py
# or
node tetris_server.js
# or
java TetrisServerMain
```

### Step 2: Compile Game with GSON
```bash
javac -cp ".;lib/gson-2.10.1.jar" *.java
```

### Step 3: Run Game with GSON
```bash
java -cp ".;lib/gson-2.10.1.jar" TetrisGame
```

### Step 4: Select Online Mode
1. Launch the game
2. Navigate to **"Online Mode"** in main menu
3. Press ENTER to connect

## Game Features in Online Mode

### 🌐 Server Connection
- **Auto-connect**: Game attempts to connect to `localhost:3000`
- **Connection status**: Displayed in real-time on UI
- **Auto-disconnect**: When returning to menu or game over

### 🤖 AI Assistance
- **Real-time recommendations**: Server provides optimal moves every 500ms
- **Automatic application**: Game applies server suggestions
- **Transparent operation**: You can still manually control pieces

### 🎮 Controls
- **Movement**: WASD keys (same as single player)
- **Pause**: P key
- **Music toggle**: M key  
- **Disconnect**: ESC key (returns to menu)

### 📊 Enhanced UI
- **Connection indicator**: Shows server status
- **AI assistance panel**: Displays server communication info
- **Game statistics**: Score, level, lines cleared
- **Next piece preview**: Shows upcoming piece

## JSON Communication Protocol

### PurGame Object (Sent to Server)
```json
{
  "width": 10,
  "height": 20,
  "cells": [[0,0,0,...], [1,0,1,...],...],
  "currentShape": 0,
  "nextShape": 3
}
```

### OpMove Object (Received from Server)
```json
{
  "opX": 4,
  "opRotate": 2
}
```

### Communication Flow
1. **Client**: Sends current game state as JSON
2. **Server**: Analyzes board and returns optimal move
3. **Client**: Applies recommended position and rotation
4. **Repeat**: Every 500ms during gameplay

## Troubleshooting

### ❌ "Failed to connect to TetrisServer"
- **Check**: Server is running on `localhost:3000`
- **Verify**: No firewall blocking port 3000
- **Restart**: Server and try again

### ❌ "Communication error"
- **Check**: Server responds with valid JSON
- **Verify**: OpMove format is correct
- **Monitor**: Server logs for errors

### ❌ "JSON parsing error" 
- **Check**: Server sends valid JSON format
- **Verify**: No extra characters in response
- **Validate**: JSON structure matches specification

## Server Implementation Tips

### Required Server Features:
1. **Socket listener** on port 3000
2. **JSON parser** for PurGame objects
3. **Game analysis algorithm** (your Tetris AI)
4. **JSON response** with OpMove object
5. **Connection management** (close after each request)

### Example Server Response:
```python
# Python server example
import socket, json

def analyze_game(game_state):
    # Your AI algorithm here
    optimal_x = 3
    optimal_rotations = 1
    return {"opX": optimal_x, "opRotate": optimal_rotations}

server = socket.socket()
server.bind(('localhost', 3000))
server.listen(5)

while True:
    client, addr = server.accept()
    data = client.recv(1024).decode()
    game_state = json.loads(data)
    
    move = analyze_game(game_state)
    response = json.dumps(move)
    
    client.send(response.encode())
    client.close()
```

## Performance Notes

- **Move delay**: 500ms between server requests (configurable)
- **Connection timeout**: Automatic retry on failure
- **Memory usage**: Minimal JSON overhead
- **Network**: Local connections only (localhost)

## File Structure
```
📁 Tetris Game/
├── 📄 TetrisGame.java        # Main game with online mode
├── 📄 TetrisServer.java      # Server communication client
├── 📄 GameData.java          # Save/load functionality  
├── 📄 SoundManager.java      # Audio system
├── 📄 download-gson.bat      # GSON library downloader
├── 📁 lib/
│   └── 📄 gson-2.10.1.jar   # JSON library
└── 📄 ONLINE_MODE_GUIDE.md   # This guide
```

---

**🎯 Ready to play with AI assistance! Start your TetrisServer and enjoy optimal Tetris gameplay!**