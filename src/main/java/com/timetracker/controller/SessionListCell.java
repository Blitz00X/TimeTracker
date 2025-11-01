package com.timetracker.controller;

import com.timetracker.model.SessionViewModel;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;

/**
 * ListCell implementation for displaying sessions with contextual actions.
 */
class SessionListCell extends ListCell<SessionViewModel> {

    private final MainController controller;
    private final MenuItem editItem;
    private final MenuItem deleteItem;
    private final ContextMenu contextMenu;

    SessionListCell(MainController controller) {
        this.controller = controller;
        this.editItem = new MenuItem("Edit Session...");
        this.deleteItem = new MenuItem("Delete Session");
        this.contextMenu = new ContextMenu(editItem, deleteItem);
        wireActions();
    }

    private void wireActions() {
        editItem.setOnAction(event -> {
            SessionViewModel item = getItem();
            if (item != null) {
                controller.promptEditSession(item);
            }
        });
        deleteItem.setOnAction(event -> {
            SessionViewModel item = getItem();
            if (item != null) {
                controller.promptDeleteSession(item);
            }
        });
    }

    @Override
    protected void updateItem(SessionViewModel item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setContextMenu(null);
        } else {
            setText(item.asDisplayString());
            setContextMenu(contextMenu);
        }
    }
}
