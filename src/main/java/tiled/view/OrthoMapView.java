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

package tiled.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tiled.core.*;
import tiled.mapeditor.selection.SelectionLayer;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Properties;

import static java.lang.Math.min;

/**
 * An orthographic map view.
 */
public class OrthoMapView extends MapView {
    private Logger log = LoggerFactory.getLogger(OrthoMapView.class);

    private final Polygon propPoly;

    /**
     * Creates a new orthographic map view that displays the specified map.
     *
     * @param map the map to be displayed by this map view
     */
    public OrthoMapView(Map map) {
        super(map);

        propPoly = new Polygon();
        propPoly.addPoint(0, 0);
        propPoly.addPoint(12, 0);
        propPoly.addPoint(12, 12);
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        Dimension tileSize = getMapTileSize();

        if (orientation == SwingConstants.VERTICAL) {
            return (visibleRect.height / tileSize.height) * tileSize.height;
        } else {
            return (visibleRect.width / tileSize.width) * tileSize.width;
        }
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        Dimension tileSize = getMapTileSize();
        if (orientation == SwingConstants.VERTICAL) {
            return tileSize.height;
        } else {
            return tileSize.width;
        }
    }

    public Dimension getPreferredSize() {
        Dimension tileSize = getMapTileSize();

        return new Dimension(map.getWidth() * tileSize.width, map.getHeight() * tileSize.height);
    }

    protected void paintLayer(Graphics2D g2d, TileLayer layer) {
        // Determine tile size and offset
        Dimension tileSize = getLayerTileSize(layer);
        if (tileSize.width <= 0 || tileSize.height <= 0) {
            return;
        }

        Polygon gridPoly = createGridPolygon(tileSize, 0, -tileSize.height, 0);

        Point pointOffset = calculateParallaxOffsetZoomed(layer);

        // Determine area to draw from clipping rectangle
        Rectangle clipRect = g2d.getClipBounds();
/*      clipRect.x -= pointOffset.x;
        clipRect.y -= pointOffset.y;
        int startX = clipRect.x / tileSize.width;
        int startY = clipRect.y / tileSize.height;
        int endX = (clipRect.x + clipRect.width) / tileSize.width + 1;
        int endY = (clipRect.y + clipRect.height) / tileSize.height + 3;
        // (endY +2 for high tiles, could be done more properly)
*/
        Point start = this.screenToTileCoords(layer, clipRect.x, clipRect.y);
        Point end = this.screenToTileCoords(layer, (clipRect.x + clipRect.width), (clipRect.y + clipRect.height));
        end.x += 1;
        end.y += 3;

        boolean isSelectionLayer = layer instanceof SelectionLayer;

        // Draw this map layer
        for (int y = start.y, gy = (start.y + 1) * tileSize.height + pointOffset.y;
             y < end.y; y++, gy += tileSize.height) {
            for (int x = start.x, gx = start.x * tileSize.width + pointOffset.x;
                 x < end.x; x++, gx += tileSize.width) {
                Tile tile = layer.getTileAt(x, y);

                if (tile == null)
                    continue;

                if (isSelectionLayer) {
                    gridPoly.translate(gx, gy);
                    g2d.fillPolygon(gridPoly);
                    gridPoly.translate(-gx, -gy);
                    //paintEdge(g, layer, gx, gy);
                } else {
                    tile.draw(g2d, gx, gy, zoom);
                }
            }
        }
    }

    protected void paintObjectGroup(Graphics2D g2d, ObjectGroup og) {
        final Dimension tsize = getLayerTileSize(og);
        assert tsize.width != 0 && tsize.height != 0;
        final Rectangle bounds = og.getBounds();
        Iterator<MapObject> itr = og.getObjects();
        g2d.translate(
                bounds.x * tsize.width,
                bounds.y * tsize.height);

        while (itr.hasNext()) {
            MapObject mo = itr.next();
            double ox = mo.getX() * zoom;
            double oy = mo.getY() * zoom;

            Image objectImage = mo.getImage(zoom);
            if (objectImage != null) {
                g2d.drawImage(objectImage, (int) ox, (int) oy, null);
            }

            if (mo.getWidth() == 0 || mo.getHeight() == 0) {
                g2d.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.black);
                g2d.fillOval((int) ox + 1, (int) oy + 1,
                        (int) (10 * zoom), (int) (10 * zoom));
                g2d.setColor(Color.orange);
                g2d.fillOval((int) ox, (int) oy,
                        (int) (10 * zoom), (int) (10 * zoom));
                g2d.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_OFF);
            } else {
                g2d.setColor(Color.black);
                g2d.drawRect((int) ox + 1, (int) oy + 1,
                        (int) (mo.getWidth() * zoom),
                        (int) (mo.getHeight() * zoom));
                g2d.setColor(Color.orange);
                g2d.drawRect((int) ox, (int) oy,
                        (int) (mo.getWidth() * zoom),
                        (int) (mo.getHeight() * zoom));
            }
            if (zoom > 0.0625) {
                final String s = mo.getName() != null ? mo.getName() : "(null)";
                g2d.setColor(Color.black);
                g2d.drawString(s, (int) (ox - 5) + 1, (int) (oy - 5) + 1);
                g2d.setColor(Color.white);
                g2d.drawString(s, (int) (ox - 5), (int) (oy - 5));
            }
        }

        g2d.translate(
                -bounds.x * tsize.width,
                -bounds.y * tsize.height);
    }

    protected void paintGrid(Graphics2D g2d) {
        MapLayer currentLayer = getCurrentLayer();
        // the grid size is dependent on the current layer - no current layer, no grid.
        if (currentLayer == null)
            return;

        // Determine tile size
        Dimension tsize = getLayerTileSize(currentLayer);
        if (tsize.width <= 0 || tsize.height <= 0) {
            return;
        }
        Point offset = calculateParallaxOffsetZoomed(currentLayer);

        // Determine lines to draw from clipping rectangle
        Rectangle clipRect = g2d.getClipBounds();

        // transforming coordinates back and forth between screen and tile 
        // coordinates to quantise the given screen rectangle to coordinates bla 
        // that match the grid lines
        Point startTile = screenToTileCoords(currentLayer, clipRect.x, clipRect.y);

        Point start = tileToScreenCoords(offset, tsize, startTile.x, startTile.y);
        Point end = new Point(clipRect.x + clipRect.width, clipRect.y + clipRect.height);

        for (int x = start.x; x < end.x; x += tsize.width) {
            g2d.drawLine(x, clipRect.y, x, clipRect.y + clipRect.height - 1);
        }
        for (int y = start.y; y < end.y; y += tsize.height) {
            g2d.drawLine(clipRect.x, y, clipRect.x + clipRect.width - 1, y);
        }
    }

    @Override
    protected void paintZXScreenGrid(Graphics2D g2d) {
        MapLayer currentLayer = getCurrentLayer();
        // the grid size is dependent on the current layer - no current layer, no grid.
        if (currentLayer == null)
            return;

        // Determine tile size
        Dimension tileSize = getLayerTileSize(currentLayer);
        Dimension zxScreenSize = getLayerZXScreenSize(currentLayer, tileSize);
        if (zxScreenSize.width <= 0 || zxScreenSize.height <= 0) {
            return;
        }
        Point offset = calculateParallaxOffsetZoomed(currentLayer);

        // Determine lines to draw from clipping rectangle
        Rectangle clipRect = g2d.getClipBounds();

        // transforming coordinates back and forth between screen and tile
        // coordinates to quantise the given screen rectangle to coordinates bla
        // that match the grid lines
        Point startTile = screenToTileCoords(currentLayer, clipRect.x, clipRect.y);

        Point start = tileToScreenCoords(offset, zxScreenSize, startTile.x, startTile.y);
        Point end = new Point(clipRect.x + clipRect.width, clipRect.y + clipRect.height);

        for (int x = start.x; x < end.x; x += zxScreenSize.width) {
            g2d.drawLine(x, clipRect.y, x, clipRect.y + clipRect.height - 1);
        }
        for (int y = start.y; y < end.y; y += zxScreenSize.height) {
            g2d.drawLine(clipRect.x, y, clipRect.x + clipRect.width - 1, y);
        }
    }

    protected void paintCoordinates(Graphics2D g2d) {
        // like the grid, the coordinates are dependent on the current layer
        // (since the tile size can be different from layer to layer
        MapLayer currentLayer = getCurrentLayer();
        if (currentLayer == null)
            return;

        Dimension tileSize = getLayerTileSize(currentLayer);
        if (tileSize.width <= 0 || tileSize.height <= 0) {
            return;
        }
        Point offset = calculateParallaxOffsetZoomed(currentLayer);

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Determine tile size and offset
        int minTileExtents = min(tileSize.width, tileSize.height);
        Font font = new Font("SansSerif", Font.PLAIN, minTileExtents / 4);
        g2d.setFont(font);
        FontRenderContext fontRenderContext = g2d.getFontRenderContext();

        // Determine area to draw from clipping rectangle
        Rectangle clipRect = g2d.getClipBounds();
        Point start = screenToTileCoords(currentLayer, clipRect.x, clipRect.y);
        Point end = screenToTileCoords(currentLayer, clipRect.x + clipRect.width, clipRect.y + clipRect.height);
        end.x += 1;
        end.y += 1;

        // Draw the coordinates
        for (int y = start.y; y < end.y; y++) {
            Point g = tileToScreenCoords(offset, tileSize, start.x, y);
            for (int x = start.x; x < end.x; x++) {
                String coords = "(" + x + "," + y + ")";
                Rectangle2D textSize =
                        font.getStringBounds(coords, fontRenderContext);

                int fx = g.x + (int) ((tileSize.width - textSize.getWidth()) / 2);
                int fy = g.y + (int) ((tileSize.height + textSize.getHeight()) / 2);

                g2d.drawString(coords, fx, fy);
                g.x += tileSize.width;
            }
        }
    }

    protected void paintZXScreenNumbers(Graphics2D g2d) {
        // like the grid, the coordinates are dependent on the current layer
        // (since the tile size can be different from layer to layer

        MapLayer currentLayer = getCurrentLayer();
        if (currentLayer == null)
            return;

        Dimension tileSize = getLayerTileSize(currentLayer);
        Dimension zxScreenSize = getLayerZXScreenSize(currentLayer, tileSize);

        if (zxScreenSize.width <= 0 || zxScreenSize.height <= 0) {
            return;
        }
        Point offset = calculateParallaxOffsetZoomed(currentLayer);

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Determine tile size and offset
        int minTileExtents = min(zxScreenSize.width, zxScreenSize.height);
        Font font = new Font("SansSerif", Font.PLAIN, minTileExtents / 4);
        g2d.setFont(font);
        FontRenderContext fontRenderContext = g2d.getFontRenderContext();

        // Determine area to draw from clipping rectangle
        Rectangle clipRect = g2d.getClipBounds();
        Point start = screenToZXScreenCoords(currentLayer, clipRect.x, clipRect.y);
        Point end = screenToZXScreenCoords(currentLayer, clipRect.x + clipRect.width, clipRect.y + clipRect.height);

        int number = 0;
        // Draw the coordinates
        for (int y = start.y; y < end.y; y++) {
            Point g = tileToScreenCoords(offset, zxScreenSize, start.x, y);
            for (int x = start.x; x < end.x; x++) {
                String screenNumber = String.valueOf(number);//"(" + x + "," + y + ")";

                Rectangle2D textSize = font.getStringBounds(screenNumber, fontRenderContext);
                int fx = g.x + (int) ((zxScreenSize.width - textSize.getWidth()) / 2);
                int fy = g.y + (int) ((zxScreenSize.height + textSize.getHeight()) / 2);

                g2d.drawString(screenNumber, fx, fy);
                g.x += zxScreenSize.width;
                number+=1;
            }
        }
    }

    protected void paintPropertyFlags(Graphics2D g2d, TileLayer layer) {
        Dimension tsize = getLayerTileSize(layer);
        if (tsize.width <= 0 || tsize.height <= 0) {
            return;
        }
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setComposite(AlphaComposite.SrcAtop);

        //g2d.setColor(new Color(0.1f, 0.1f, 0.5f, 0.5f));
        g2d.setXORMode(new Color(0.9f, 0.9f, 0.9f, 0.5f));

        // Determine tile size and offset

        // Determine area to draw from clipping rectangle
        Rectangle clipRect = g2d.getClipBounds();
        int startX = clipRect.x / tsize.width;
        int startY = clipRect.y / tsize.height;
        int endX = (clipRect.x + clipRect.width) / tsize.width + 1;
        int endY = (clipRect.y + clipRect.height) / tsize.height + 1;

        int y = startY * tsize.height;

        for (int j = startY; j <= endY; j++) {
            int x = startX * tsize.width;

            for (int i = startX; i <= endX; i++) {
                try {
                    Properties p = layer.getTileInstancePropertiesAt(i, j);
                    if (p != null && !p.isEmpty()) {
                        //g2d.drawString( "PROP", x, y );
                        //g2d.drawImage(MapView.propertyFlagImage, x + (tsize.width - 12), y, null);
                        g2d.translate(x + (tsize.width - 13), y + 1);
                        g2d.drawPolygon(propPoly);
                        g2d.translate(-(x + (tsize.width - 13)), -(y + 1));
                    }
                } catch (Exception e) {
                    log.info("Exception\n{}", e.getMessage());
                    e.printStackTrace();
                }

                x += tsize.width;
            }
            y += tsize.height;
        }
    }

    public void repaintRegion(MapLayer layer, Rectangle region) {
        Dimension tsize = getLayerTileSize(layer);
        if (tsize.width <= 0 || tsize.height <= 0) {
            return;
        }
        int maxExtraHeight =  (int) (map.getTileHeightMax() * zoom - tsize.height);

        // Calculate the visible corners of the region
        Point start = tileToScreenCoords(layer, region.x, region.y);
        Point end = tileToScreenCoords(layer, (region.x + region.width), (region.y + region.height));

        start.x -= maxExtraHeight;

        Rectangle dirty =
                new Rectangle(start.x, start.y, end.x - start.x, end.y - start.y);

        repaint(dirty);
    }

    public Point screenToTileCoords(MapLayer layer, int x, int y) {
        Dimension tsize = getLayerTileSize(layer);
        Point poffset = calculateParallaxOffsetZoomed(layer);
        return new Point((x - poffset.x) / tsize.width, (y - poffset.y) / tsize.height);
    }

    public Point screenToZXScreenCoords(MapLayer layer, int x, int y) {
        Dimension tileSize = getLayerTileSize(layer);
        Dimension zxScreenSize = getLayerZXScreenSize(layer, tileSize);

        Point poffset = calculateParallaxOffsetZoomed(layer);
        return new Point((x - poffset.x) / zxScreenSize.width, (y - poffset.y) / zxScreenSize.height);
    }

    public Point tileToScreenCoords(Point offset, Dimension tileDimension, int x, int y) {
        return new Point(offset.x + x * tileDimension.width, offset.y + y * tileDimension.height);
    }

    protected Dimension getLayerTileSize(MapLayer layer) {
        return new Dimension(
                (int) (layer.getTileWidth() * zoom),
                (int) (layer.getTileHeight() * zoom));
    }

    protected Dimension getLayerZXScreenSize(MapLayer layer, Dimension tileSize) {
        return new Dimension(
                (int) (ZXScreen.getWidthInPixelsBySizeTile(tileSize.width) * zoom),
                (int) (ZXScreen.getHeightInPixelsBySizeTile(tileSize.height) * zoom));
    }

    protected Dimension getMapTileSize() {
        return new Dimension(
                (int) (map.getTileWidth() * zoom),
                (int) (map.getTileHeight() * zoom));
    }

    protected Polygon createGridPolygon(Dimension tileDimension, int tx, int ty, int border) {
        Polygon poly = new Polygon();
        poly.addPoint(tx - border, ty - border);
        poly.addPoint(tx + tileDimension.width + border, ty - border);
        poly.addPoint(tx + tileDimension.width + border, ty + tileDimension.height + border);
        poly.addPoint(tx - border, ty + tileDimension.height + border);

        return poly;
    }

}
