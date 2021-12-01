package tiled.mapeditor.actions;

import tiled.core.Map;
import tiled.mapeditor.MapEditor;
import tiled.mapeditor.Resources;
import tiled.mapeditor.dialogs.NewMapDialog;

import javax.swing.*;

public class NewMapAction extends AbstractFileAction {
    public NewMapAction(MapEditor editor, SaveAction saveAction) {
        super(editor,
              saveAction,
              Resources.getString("action.map.new.name"),
              Resources.getString("action.map.new.tooltip"));

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
    }

    protected void doPerformAction() {
        NewMapDialog newMapDialog = new NewMapDialog((JFrame) editor.getAppFrame());
        Map newMap = newMapDialog.create();
        if (newMap != null) {
            editor.setCurrentMap(newMap);
        }
    }
}
