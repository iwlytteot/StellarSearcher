package cz.muni.fi.tovarys.model;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.text.Font;
import javafx.util.Callback;

public class TableListCell extends CheckBoxListCell<Table> {
    public TableListCell(Callback<Table, ObservableValue<Boolean>> callback) {
        super(callback);
        setFont(new Font("System Regular", 14));
    }

    @Override
    public void updateItem(Table table, boolean empty) {
        super.updateItem(table, empty);

        if (empty || table == null) {
            setText(null);
        } else {
            setText(table.getName() + "  -  " + table.getInfo());
        }
    }
}
