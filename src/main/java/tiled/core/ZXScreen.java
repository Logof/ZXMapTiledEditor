package tiled.core;

public class ZXScreen {
    private static final int HEIGHT = 10;
    private static final int WIDTH = 15;

    private static final int TILE_SIZE_16 = 16;
    private static final int TILE_SIZE_48 = 48;

    private ZXScreen() {

    }

    public static int getHeight() {
        return HEIGHT;
    }

    public static int getHeightInPixelsBySizeTile(int tileSizeHeight) {
        return HEIGHT * tileSizeHeight;
    }

    public static int getWidth() {
        return WIDTH;
    }

    public static int getWidthInPixelsBySizeTile(int tileSizeWidth) {
        return WIDTH * tileSizeWidth;
    }

    public static int getTileSize16() {
        return TILE_SIZE_16;
    }

    public static int getTileSize48() {
        return TILE_SIZE_48;
    }
}
