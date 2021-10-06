package model;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListCell;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.text.Font;
import javafx.util.Callback;

public class CatalogueListCell extends CheckBoxListCell<String> {
    public CatalogueListCell(Callback<String, ObservableValue<Boolean>> callBack) {
        super(callBack);
    }

    @Override
    public void updateItem(String name, boolean b) {
        super.updateItem(name, b);

        if (b || name == null) {
            setText(null);
        } else {
            setText(name);
            setFont(new Font("System Regular", 16));
        }
    }
}
