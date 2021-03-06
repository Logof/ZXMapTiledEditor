/*
 *  Tiled Map Editor, (c) 2004-2006
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <bjorn@lindeijer.nl>
 */

package tiled.util;

import tiled.core.*;

import java.awt.*;
import java.util.Iterator;
import java.util.Vector;

/**
 * This class facilitates physically merging tiles.
 */
public class TileMergeHelper {
    private Map myMap;
    private TileSet myTs;
    private Vector<Cell> cells;

    public TileMergeHelper(Map map) {
        myMap = map;
        cells = new Vector();
        myTs = new TileSet();
        myTs.setName("Merged Set");
    }

    public static boolean areTileSizesUniform(Map map) {
        for (MapLayer l : map.getLayerVector()) {
            if (l.getTileWidth() != map.getTileWidth() || l.getTileHeight() != map.getTileHeight()) {
                return false;
            }
        }
        return true;
    }

    public TileLayer merge(int start, int len, boolean all) {
        Rectangle r = myMap.getBounds();
        TileLayer mergedLayer = new TileLayer(r, myMap.getTileWidth(), myMap.getTileHeight());

        // make sure all tile sizes are the same as the map's default tile size, otherwise the result will be a large mess..
        assert areTileSizesUniform(myMap);

        for (int i = 0; i < r.height; i++) {
            for (int j = 0; j < r.width; j++) {
                mergedLayer.setTileAt(j, i, createCell(j, i, start, len, all));
            }
        }

        return mergedLayer;
    }

    public TileSet getSet() {
        return myTs;
    }

    public Tile createCell(int tx, int ty, int start, int len, boolean all) {
        Cell c = new Cell(myMap, tx, ty, start, len, all);
        Iterator<Cell> itr = cells.iterator();
        Tile tile;

        while (itr.hasNext()) {
            Cell check = itr.next();
            if (check.equals(c)) {
                return check.getTile();
            }
        }

        cells.add(c);

        tile = new Tile();
        c.setTile(tile);

        //GENERATE MERGED TILE IMAGE
        //FIXME: although faster, the following doesn't seem to handle alpha on some platforms...
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        Image tileImg = config.createCompatibleImage(c.getWidth(), c.getHeight());
        c.render(tileImg.getGraphics());
        tile.setImage(tileImg);

        myTs.addTile(tile);

        return tile;
    }

    private class Cell {
        private Vector<Tile> sandwich;
        private Tile myTile;

        public Cell(Map map, int posx, int posy, int start, int len, boolean all) {
            sandwich = new Vector<Tile>();
            for (int i = 0; i < len; i++) {
                MapLayer ml = map.getLayer(start + i);
                if (ml instanceof TileLayer) {
                    TileLayer l = (TileLayer) ml;
                    if (l.isVisible() || all) {
                        sandwich.add(l.getTileAt(posx, posy));
                    } else {
                        sandwich.add(null);
                    }
                }
            }
        }

        public Tile getTile() {
            return myTile;
        }

        public void setTile(Tile t) {
            myTile = t;
        }

        public void render(Graphics g) {
            for (Tile tile : sandwich) {
                if (tile != null) tile.draw(g, 0, getHeight(), 1.0f);
            }
        }

        public boolean equals(Cell c) {
            Iterator<Tile> me = sandwich.iterator();
            Iterator<Tile> them = c.sandwich.iterator();
            while (me.hasNext()) {
                Tile m = me.next();
                Tile t = them.next();
                if (m != null && t != null && !m.equals(t)) {
                    return false;
                } else if (m != null && t != null && t != m) {
                    return false;
                } else if ((m != null && t == null) || (m == null && t != null)) {
                    return false;
                }
            }
            return true;
        }

        public int getWidth() {
            int width = 0;
            for (Tile tile : sandwich) {
                if (tile != null) {
                    int w = tile.getWidth();
                    if (w > width) width = w;
                }
            }
            return width;
        }

        public int getHeight() {
            int height = 0;
            for (Tile tile : sandwich) {
                if (tile != null) {
                    int h = tile.getHeight();
                    if (h > height) height = h;
                }
            }
            return height;
        }
    }
}
