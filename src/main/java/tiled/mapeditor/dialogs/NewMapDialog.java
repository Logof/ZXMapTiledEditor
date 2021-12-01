package tiled.mapeditor.dialogs;

import tiled.core.Map;
import tiled.core.ZXScreen;
import tiled.mapeditor.Resources;
import tiled.mapeditor.widget.IntegerSpinner;
import tiled.mapeditor.widget.VerticalStaticJPanel;
import tiled.util.TiledConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

public class NewMapDialog extends JDialog implements ActionListener {
    private static final String DIALOG_TITLE = Resources.getString("dialog.newmap.title");
    private static final String MAPSIZE_TITLE = Resources.getString("dialog.newmap.mapsize.title");
    private static final String WIDTH_LABEL = Resources.getString("dialog.newmap.width.label");
    private static final String HEIGHT_LABEL = Resources.getString("dialog.newmap.height.label");
    private static final String MAPTYPE_LABEL = Resources.getString("dialog.newmap.maptype.label");
    private static final String OK_BUTTON = Resources.getString("general.button.ok");
    private static final String CANCEL_BUTTON = Resources.getString("general.button.cancel");
    private static final String ISOMETRIC_MAPTYPE = Resources.getString("general.maptype.isometric");
    private static final String HEXAGONAL_MAPTYPE = Resources.getString("general.maptype.hexagonal");
    private static final String SHIFTED_MAPTYPE = Resources.getString("general.maptype.shifted");
    private static final String FTONTAL_MAPTYPE = Resources.getString("general.maptype.frontal");


    private final Preferences prefs = TiledConfiguration.node("dialog/newzxmap");
    private Map newMap;
    private IntegerSpinner mapWidth, mapHeight;
    private JComboBox mapTypeChooser;

    public NewMapDialog(JFrame parent) {
        super(parent, DIALOG_TITLE, true);
        init();
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void init() {
        // Load dialog defaults

        int defaultMapWidth = prefs.getInt("mapWidth", 3);
        int defaultMapHeight = prefs.getInt("mapHeight", 3);

        // Create the primitives
        mapWidth = new IntegerSpinner(defaultMapWidth, 1);
        mapHeight = new IntegerSpinner(defaultMapHeight, 1);

        // Map size fields
        JPanel mapSize = new VerticalStaticJPanel();
        mapSize.setLayout(new GridBagLayout());
        mapSize.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(MAPSIZE_TITLE),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.insets = new Insets(5, 0, 0, 5);
        mapSize.add(new JLabel(WIDTH_LABEL), gridBagConstraints);
        gridBagConstraints.gridy = 1;
        mapSize.add(new JLabel(HEIGHT_LABEL), gridBagConstraints);
        gridBagConstraints.insets = new Insets(5, 0, 0, 0);
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        mapSize.add(mapWidth, gridBagConstraints);
        gridBagConstraints.gridy = 1;
        mapSize.add(mapHeight, gridBagConstraints);

        // OK and Cancel buttons
        JButton okButton = new JButton(OK_BUTTON);
        JButton cancelButton = new JButton(CANCEL_BUTTON);
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);

        JPanel buttons = new VerticalStaticJPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createGlue());
        buttons.add(okButton);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(cancelButton);

        // Map type and name inputs
        mapTypeChooser = new JComboBox();
        mapTypeChooser.addItem(FTONTAL_MAPTYPE);
        //mapTypeChooser.addItem(ISOMETRIC_MAPTYPE);
        //mapTypeChooser.addItem(HEXAGONAL_MAPTYPE);
        // TODO: Enable views when implemented decently
        //mapTypeChooser.addItem(SHIFTED_MAPTYPE);

        JPanel miscPropPanel = new VerticalStaticJPanel();
        miscPropPanel.setLayout(new GridBagLayout());
        miscPropPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.insets = new Insets(5, 0, 0, 5);
        miscPropPanel.add(new JLabel(MAPTYPE_LABEL), gridBagConstraints);
        gridBagConstraints.insets = new Insets(5, 0, 0, 0);
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        miscPropPanel.add(mapTypeChooser, gridBagConstraints);

        // Putting two size panels next to eachother
        JPanel sizePanels = new JPanel();
        sizePanels.setLayout(new BoxLayout(sizePanels, BoxLayout.X_AXIS));
        sizePanels.add(mapSize);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(miscPropPanel);
        mainPanel.add(sizePanels);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(Box.createGlue());
        mainPanel.add(buttons);

        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(okButton);
    }

    public Map create() {
        setVisible(true);
        return newMap;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals(OK_BUTTON)) {
            int width = mapWidth.intValue() * ZXScreen.getWidthInTiles();
            int height = mapHeight.intValue() * ZXScreen.getHeightInTiles();
            int tileWidth = ZXScreen.getTileSize16();
            int tileHeight = ZXScreen.getTileSize16();
            int orientation = Map.MDO_FRONT;
            String mapTypeString = (String) mapTypeChooser.getSelectedItem();

            if (mapTypeString.equals(ISOMETRIC_MAPTYPE)) {
                orientation = Map.MDO_ISO;
            } else if (mapTypeString.equals(HEXAGONAL_MAPTYPE)) {
                orientation = Map.MDO_HEX;
            } else if (mapTypeString.equals(SHIFTED_MAPTYPE)) {
                orientation = Map.MDO_SHIFTED;
            }

            newMap = new Map(width, height);
            newMap.setTileWidth(tileWidth);
            newMap.setTileHeight(tileHeight);
            newMap.addLayer();
            newMap.setOrientation(orientation);

            // Save dialog options

            prefs.putInt("mapWidth", mapWidth.intValue());
            prefs.putInt("mapHeight", mapHeight.intValue());
            prefs.putInt("tileWidth", ZXScreen.getTileSize16());
            prefs.putInt("tileHeight", ZXScreen.getTileSize16());
        }
        dispose();
    }
}
