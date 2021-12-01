package tiled.core;

public class ZXScreen {
    private static final int HEIGHT = 10;
    private static final int WIDTH = 15;

    private static final int TILE_SIZE_16 = 16;
    private static final int TILE_SIZE_48 = 48;

    private ZXScreen() {

    }

    public static int getHeightInTiles() {
        return HEIGHT;
    }

    public static int getHeightInPixels() {
        return HEIGHT * TILE_SIZE_16;
    }

    public static int getWidthInTiles() {
        return WIDTH;
    }

    public static int getWidthInPixels() {
        return WIDTH * TILE_SIZE_16;
    }

    public static int getTileSize16() {
        return TILE_SIZE_16;
    }

    public static int getTileSize48() {
        return TILE_SIZE_48;
    }
}
