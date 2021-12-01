package tiled.mapeditor.actions;

import tiled.core.Map;
import tiled.mapeditor.MapEditor;
import tiled.mapeditor.Resources;
import tiled.mapeditor.dialogs.NewMapDialog;
import tiled.mapeditor.dialogs.NewZXMapDialog;

import javax.swing.*;

public class NewZXMapAction  extends AbstractFileAction {
    public NewZXMapAction(MapEditor editor, SaveAction saveAction) {
        super(editor,
              saveAction,
              Resources.getString("action.map.new.name"),
              Resources.getString("action.map.new.tooltip"));

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
    }

    protected void doPerformAction() {
        NewZXMapDialog newZXMapDialog = new NewZXMapDialog((JFrame) editor.getAppFrame());
        Map newMap = newZXMapDialog.create();
        if (newMap != null) {
            editor.setCurrentMap(newMap);
        }
    }
}
