// PieceFactory.java
import java.util.Random;
import java.awt.Color;

public final class PieceFactory {
    private static final Random RNG = new Random();
    private final int[][][] pieces;
    private final Color[] colors;

    public PieceFactory(int[][][] piecesRef, Color[] colorsRef) {
        this.pieces = piecesRef;
        this.colors = colorsRef;
    }

    /** Returns the next piece index (0..pieces.length-1). */
    public int nextPieceIndex() {
        return RNG.nextInt(pieces.length);
    }

    // Optional convenience
    public int[][] shapeOf(int idx) { return pieces[idx]; }
    public Color   colorOf(int idx) { return colors[idx % colors.length]; }
}