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

package tiled.core;

import java.awt.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

/**
 * MultilayerPlane makes up the core functionality of both Maps and Brushes.
 * This class handles the order of layers as a group.
 */
public class MultilayerPlane implements Iterable<MapLayer> {
    protected Rectangle bounds;          //in tiles
    private Vector<MapLayer> layers;

    /**
     * Default constructor.
     */
    public MultilayerPlane() {
        layers = new Vector<MapLayer>();
        bounds = new Rectangle();
    }

    /**
     * Construct a MultilayerPlane to the specified dimensions.
     *
     * @param width
     * @param height
     */
    public MultilayerPlane(int width, int height) {
        this();
        bounds.width = width;
        bounds.height = height;
    }

    /**
     * Returns the total number of layers.
     *
     * @return the size of the layer vector
     */
    public int getTotalLayers() {
        return layers.size();
    }

    /**
     * Changes the bounds of this plane to include all layers completely.
     */
    public void fitBoundsToLayers() {
        int width = 0;
        int height = 0;

        Rectangle layerBounds = new Rectangle();

        for (int i = 0; i < layers.size(); i++) {
            getLayer(i).getBounds(layerBounds);
            if (width < layerBounds.width) width = layerBounds.width;
            if (height < layerBounds.height) height = layerBounds.height;
        }

        bounds.width = width;
        bounds.height = height;
    }

    /**
     * Returns a <code>Rectangle</code> representing the maximum bounds in
     * tiles.
     *
     * @return a new rectangle containing the maximum bounds of this plane
     */
    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    /**
     * Adds a layer to the map.
     *
     * @param layer The {@link MapLayer} to add
     * @return the layer passed to the function
     */
    public MapLayer addLayer(MapLayer layer) {
        insertLayer(layers.size(), layer);
        return layer;
    }

    void insertLayer(int index, MapLayer layer) {
        layers.add(index, layer);
    }

    public void setLayer(int index, MapLayer layer) {
        layers.set(index, layer);
    }

    /**
     * Adds all the layers in a given java.util.Collection.
     *
     * @param layers a collection of layers to add
     */
    public void addAllLayers(Collection<MapLayer> layers) {
        this.layers.addAll(layers);
    }

    /**
     * Removes the layer at the specified index. Layers above this layer will
     * move down to fill the gap.
     *
     * @param index the index of the layer to be removed
     * @return the layer that was removed from the list
     */
    public MapLayer removeLayer(int index) {
        return layers.remove(index);
    }

    /**
     * Removes all layers from the plane.
     */
    public void removeAllLayers() {
        layers.removeAllElements();
    }

    /**
     * Returns the layer vector.
     *
     * @return Vector the layer vector
     */
    public Vector<MapLayer> getLayerVector() {
        return layers;
    }

    /**
     * Sets the layer vector to the given java.util.Vector.
     *
     * @param layers the new set of layers
     */
    public void setLayerVector(Vector<MapLayer> layers) {
        this.layers = layers;
    }

    /**
     * Moves the layer at <code>index</code> up one in the vector.
     *
     * @param index the index of the layer to swap up
     */
    public void swapLayerUp(int index) {
        if (index + 1 == layers.size()) {
            throw new RuntimeException(
                    "Can't swap up when already at the top.");
        }

        MapLayer hold = layers.get(index + 1);
        layers.set(index + 1, getLayer(index));
        layers.set(index, hold);
    }

    /**
     * Moves the layer at <code>index</code> down one in the vector.
     *
     * @param index the index of the layer to swap down
     */
    public void swapLayerDown(int index) {
        if (index - 1 < 0) {
            throw new RuntimeException(
                    "Can't swap down when already at the bottom.");
        }

        MapLayer hold = layers.get(index - 1);
        layers.set(index - 1, getLayer(index));
        layers.set(index, hold);
    }

    /**
     * Merges the layer at <code>index</code> with the layer below it
     *
     * @param index the index of the layer to merge down
     * @see MapLayer#mergeOnto
     */
    public void mergeLayerDown(int index) {
        if (index - 1 < 0) {
            throw new RuntimeException("Can't merge down bottom layer.");
        }

        // TODO: We're not accounting for different types of layers!!!
        TileLayer ntl;
        try {
            ntl = (TileLayer) getLayer(index - 1).clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return;
        }

        getLayer(index).mergeOnto(ntl);
        setLayer(index - 1, ntl);
        removeLayer(index);
    }

    /**
     * Finds the index of the given MapLayer instance. If the given layer is
     * not part of this Map, the function returns -1;
     *
     * @param ml the layer to request the index of.
     * @return the layer index or -1 if the layer could not be found
     */
    protected int findLayerIndex(MapLayer ml) {
        return layers.indexOf(ml);
    }

    /**
     * Returns the layer at the specified vector index.
     *
     * @param i the index of the layer to return
     * @return the layer at the specified index, or null if the index is out of
     * bounds
     */
    public MapLayer getLayer(int i) {
        try {
            return layers.get(i);
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return null;
    }

    /**
     * Gets a listIterator of all layers.
     *
     * @return a listIterator
     */
    public ListIterator<MapLayer> getLayers() {
        return layers.listIterator();
    }

    /**
     * Resizes this plane. The (dx, dy) pair determines where the original
     * plane should be positioned on the new area. Only layers that exactly
     * match the bounds of the map are resized, any other layers are moved by
     * the given shift.
     * This method will resize all layers first (if there are any) and then
     * call <code>resize(width,height)</code>
     *
     * @param width  The new width of the map.
     * @param height The new height of the map.
     * @param dx     The shift in x direction in tiles.
     * @param dy     The shift in y direction in tiles.
     * @see MapLayer#resize
     */
    public void resize(int width, int height, int dx, int dy) {
        ListIterator<MapLayer> itr = getLayers();
        while (itr.hasNext()) {
            MapLayer layer = (MapLayer) itr.next();
            if (layer.bounds.equals(bounds)) {
                layer.resize(width, height, dx, dy);
            } else {
                layer.setOffset(layer.bounds.x + dx, layer.bounds.y + dy);
            }
        }

        resize(width, height);
    }

    /**
     * Resizes this plane. The plane's layers will not be affected.
     *
     * @param width  The plane's new width
     * @param height The plane's new height
     * @see MapLayer#resize
     */
    public void resize(int width, int height) {
        bounds.width = width;
        bounds.height = height;
    }

    /**
     * Determines wether the point (x,y) falls within the plane.
     *
     * @param x
     * @param y
     * @return <code>true</code> if the point is within the plane,
     * <code>false</code> otherwise
     */
    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < bounds.width && y < bounds.height;
    }

    public Iterator<MapLayer> iterator() {
        return layers.iterator();
    }
}
