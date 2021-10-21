package model;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListCell;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.Callback;

public class TableListCell extends CheckBoxListCell<Table> {
    public TableListCell(Callback<Table, ObservableValue<Boolean>> callback) {
        super(callback);
    }

    @Override
    public void updateItem(Table table, boolean empty) {
        super.updateItem(table, empty);

        if (empty || table == null) {
            setText(null);
        } else {
            setText(table.getInfo());
        }
    }
}
