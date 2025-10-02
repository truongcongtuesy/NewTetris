import java.io.*;
import java.net.*;
import com.google.gson.*;

/**
 * TetrisServer client for connecting to external Tetris AI server
 * Implements the specification for connecting Tetris game to TetrisServer
 */
public class TetrisServer {
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private Gson gson;
    private boolean connected = false;
    
    // Server configuration
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3000;
    
    public TetrisServer() {
        this.gson = new Gson();
    }
    
    /**
     * Establish connection to TetrisServer
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            System.out.println("✅ Connected to TetrisServer at " + SERVER_HOST + ":" + SERVER_PORT);
            return true;
        } catch (IOException e) {
            System.err.println("❌ Failed to connect to TetrisServer: " + e.getMessage());
            connected = false;
            return false;
        }
    }
    
    /**
     * Disconnect from TetrisServer
     */
    public void disconnect() {
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
            connected = false;
            System.out.println("🔌 Disconnected from TetrisServer");
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
    
    /**
     * Send game state to server and receive optimal move
     */
    public OpMove getOptimalMove(PurGame gameState) {
        if (!connected) {
            System.err.println("❌ Not connected to server");
            return null;
        }
        
        try {
            // Send game state as JSON
            String gameJson = gson.toJson(gameState);
            writer.write(gameJson);
            writer.newLine();
            writer.flush();
            
            System.out.println("📤 Sent game state to server");
            
            // Receive optimal move response
            String response = reader.readLine();
            if (response != null) {
                OpMove optimalMove = gson.fromJson(response, OpMove.class);
                System.out.println("📥 Received optimal move: x=" + optimalMove.opX + ", rotations=" + optimalMove.opRotate);
                return optimalMove;
            }
            
        } catch (IOException e) {
            System.err.println("❌ Communication error: " + e.getMessage());
            connected = false;
        } catch (JsonSyntaxException e) {
            System.err.println("❌ JSON parsing error: " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean isConnected() {
        return connected && socket != null && socket.isConnected() && !socket.isClosed();
    }
    
    /**
     * PurGame class representing the current game state
     * According to specification: serialized PurGame object including width, height, cells, currentShape, nextShape
     */
    public static class PurGame {
        public int width;           // Width of the Tetris board
        public int height;          // Height of the Tetris board  
        public int[][] cells;       // Current state of the board (2D array)
        public int currentShape;    // The tetromino that is currently falling
        public int nextShape;       // The next tetromino to be played after the current one
        
        public PurGame(int width, int height, int[][] cells, int currentShape, int nextShape) {
            this.width = width;
            this.height = height;
            this.cells = cells;
            this.currentShape = currentShape;
            this.nextShape = nextShape;
        }
    }
    
    /**
     * OpMove class representing the optimal move from server
     * According to specification: OpMove object contains opX and opRotate
     */
    public static class OpMove {
        public int opX;        // The optimal X position where the current tetromino should be placed
        public int opRotate;   // The optimal number of rotations to apply to the current tetromino
        
        public OpMove() {}
        
        public OpMove(int opX, int opRotate) {
            this.opX = opX;
            this.opRotate = opRotate;
        }
    }
}