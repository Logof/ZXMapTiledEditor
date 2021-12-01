/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.mapeditor.widget;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author upachler
 */
public class TimedStatusLabel extends JLabel {

    private final int displayDuration;    // display text for 10 seconds
    private Color defaultForegroundColor;
    private long timeOut;
    private boolean clockRunning;

    /// Creates a new timed label which will display the text that is set for
    /// a couple of seconds
    /// @param    displayDuration    time duration in seconds indicating how long
    ///        the text should be displayed
    public TimedStatusLabel(int displayDuration) {
        this.displayDuration = displayDuration;
        this.defaultForegroundColor = getForeground();
        Thread t = new Thread("TimedStatusLabel") {
            public void run() {
                synchronized (TimedStatusLabel.this) {
                    while (true) {
                        try {
                            while (clockRunning && System.currentTimeMillis() < timeOut) {
                                long timeToWait = java.lang.Math.max(1, timeOut - System.currentTimeMillis());
                                TimedStatusLabel.this.wait(timeToWait);
                            }

                            if (clockRunning) {    // check if the clock is still running; if it is it means that we have timed out
                                clockRunning = false;    // turn off clock now; clearText() does it as well, but that might take a while to be called as it's done in another thread
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        TimedStatusLabel.this.clearText();
                                    }
                                });
                            } else {
                                TimedStatusLabel.this.wait();
                            }
                        } catch (InterruptedException ex) {
                            Logger.getLogger(TimedStatusLabel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    private void startClock() {
        synchronized (this) {
            clockRunning = true;
            timeOut = System.currentTimeMillis() + displayDuration * 1000;
            notifyAll();
        }
    }

    public void clearText() {
        synchronized (this) {
            clockRunning = false;
            super.setText("");
            notifyAll();
        }
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        startClock();
    }

    public void setErrorText(String s) {
        setForeground(Color.RED);
        setText(s);
    }

    public void setInfoText(String s) {
        setForeground(defaultForegroundColor);
        setText(s);
    }

}
