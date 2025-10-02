import java.io.*;
import java.net.*;
import java.util.*;
import com.google.gson.*;

/**
 * Simple TetrisServer implementation for testing Online Mode
 * This is a basic AI server that provides random but valid moves
 */
public class SimpleTetrisServer {
    private static final int PORT = 3000;
    private ServerSocket serverSocket;
    private Gson gson;
    private boolean running = false;
    
    public SimpleTetrisServer() {
        this.gson = new Gson();
    }
    
    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        running = true;
        
        System.out.println("🌐 TetrisServer started on port " + PORT);
        System.out.println("Waiting for Tetris game connections...");
        
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("📱 Client connected: " + clientSocket.getInetAddress());
                
                // Handle client in separate thread
                new Thread(() -> handleClient(clientSocket)).start();
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client: " + e.getMessage());
                }
            }
        }
    }
    
    private void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
            
            // Read game state from client
            String gameStateJson = reader.readLine();
            System.out.println("📥 Received game state: " + gameStateJson.substring(0, Math.min(100, gameStateJson.length())) + "...");
            
            // Parse game state
            PurGame gameState = gson.fromJson(gameStateJson, PurGame.class);
            
            // Calculate optimal move (simple AI)
            OpMove optimalMove = calculateOptimalMove(gameState);
            
            // Send response
            String responseJson = gson.toJson(optimalMove);
            writer.write(responseJson);
            writer.newLine();
            writer.flush();
            
            System.out.println("📤 Sent optimal move: x=" + optimalMove.opX + ", rotations=" + optimalMove.opRotate);
            
        } catch (Exception e) {
            System.err.println("❌ Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
    
    /**
     * Simple AI that calculates optimal move
     * This is a basic implementation - replace with your advanced AI
     */
    private OpMove calculateOptimalMove(PurGame gameState) {
        int width = gameState.width;
        int height = gameState.height;
        int[][] board = gameState.cells;
        
        // Simple strategy: find position with lowest landing height
        int bestX = width / 2;  // Default to center
        int bestRotations = 0;  // Default no rotation
        int minHeight = height;
        
        // Try different rotations (0-3)
        for (int rotations = 0; rotations < 4; rotations++) {
            // Try different X positions
            for (int x = 0; x < width; x++) {
                int landingHeight = calculateLandingHeight(board, x, width, height);
                
                // Prefer lower heights and avoid edges
                int score = landingHeight;
                if (x == 0 || x == width - 1) score += 2; // Penalty for edges
                
                if (score < minHeight) {
                    minHeight = score;
                    bestX = x;
                    bestRotations = rotations;
                }
            }
        }
        
        // Add some randomness to make it interesting
        Random random = new Random();
        if (random.nextDouble() < 0.1) { // 10% chance for random move
            bestX = random.nextInt(width);
            bestRotations = random.nextInt(4);
        }
        
        // Ensure bestX is within bounds
        bestX = Math.max(0, Math.min(width - 1, bestX));
        
        return new OpMove(bestX, bestRotations);
    }
    
    /**
     * Calculate where a piece would land at given X position
     */
    private int calculateLandingHeight(int[][] board, int x, int width, int height) {
        // Find the highest occupied cell in column x
        for (int y = 0; y < height; y++) {
            if (x >= 0 && x < width && board[y][x] != 0) {
                return y;
            }
        }
        return height; // Column is empty
    }
    
    public void stop() throws IOException {
        running = false;
        if (serverSocket != null) {
            serverSocket.close();
        }
        System.out.println("🛑 TetrisServer stopped");
    }
    
    /**
     * PurGame class - matches TetrisServer.java
     */
    public static class PurGame {
        public int width;
        public int height;
        public int[][] cells;
        public int currentShape;
        public int nextShape;
    }
    
    /**
     * OpMove class - matches TetrisServer.java  
     */
    public static class OpMove {
        public int opX;
        public int opRotate;
        
        public OpMove(int opX, int opRotate) {
            this.opX = opX;
            this.opRotate = opRotate;
        }
    }
    
    public static void main(String[] args) {
        SimpleTetrisServer server = new SimpleTetrisServer();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
            } catch (IOException e) {
                System.err.println("Error stopping server: " + e.getMessage());
            }
        }));
        
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("❌ Failed to start server: " + e.getMessage());
            System.err.println("Make sure port 3000 is not already in use");
        }
    }
}