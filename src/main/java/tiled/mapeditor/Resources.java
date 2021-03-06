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

package tiled.mapeditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * This class implements static accessors to common editor resources. These
 * currently include icons and internationalized strings.
 *
 * @version $Id$
 */
public final class Resources {
    private static Logger log = LoggerFactory.getLogger(Resources.class);
    // The resource bundle used by this class
    //private static final ResourceBundle resourceBundle = ResourceBundle.getBundle(Resources.class.getPackage().getName() + ".resources.gui");
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("gui");
    private static final ClassLoader classLoader = Resources.class.getClassLoader();

    public static ClassLoader getClassLoader() {
        return classLoader;
    }


    // Prevent instanciation
    private Resources() {
    }

    /**
     * Retrieves a string from the resource bundle in the default locale.
     *
     * @param key the key for the desired string
     * @return the string for the given key
     */
    public static String getString(String key) {
        return resourceBundle.getString(key);
    }

    /**
     * Loads an image from the resources directory. This directory is part of
     * the distribution jar.
     *
     * @param filename the filename relative from the resources directory
     * @return A BufferedImage instance of the image
     * @throws IOException              if an error occurs during reading
     * @throws IllegalArgumentException when the resource could not be found
     */
    public static Image getImage(String filename) throws IOException, IllegalArgumentException {
        return ImageIO.read(classLoader.getResourceAsStream(filename));
    }

    /**
     * Loads the image using {@link #getImage(String)} and uses it to create
     * a new {@link ImageIcon} instance.
     *
     * @param filename the filename of the image relative from the
     *                 <code>resources</code> directory
     * @return the loaded icon, or <code>null</code> when an error occured
     * while loading the image
     */
    public static Icon getIcon(String filename) {
        try {
            return new ImageIcon(getImage(filename));
        } catch (IOException e) {
            log.info("Failed to load as image: {}", filename);
        } catch (IllegalArgumentException e) {
            log.info("Failed to load resource: {}", filename);
        }
        return null;
    }
}
