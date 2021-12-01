package tiled.io.bin;

import tiled.core.Map;
import tiled.core.TileSet;
import tiled.io.MapWriter;
import tiled.io.PluginLogger;

import java.io.File;
import java.io.OutputStream;

public class BINMapWriter implements MapWriter {
    @Override
    public void writeMap(Map map, String filename) throws Exception {

    }

    @Override
    public void writeTileset(TileSet set, String filename) throws Exception {

    }

    @Override
    public void writeMap(Map map, OutputStream out) throws Exception {

    }

    @Override
    public void writeTileset(TileSet set, OutputStream out) throws Exception {

    }

    @Override
    public boolean accept(File pathname) {
        return false;
    }

    @Override
    public String getFilter() throws Exception {
        return "*.bin";
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getPluginPackage() {
        return null;
    }

    @Override
    public void setLogger(PluginLogger logger) {

    }
}
