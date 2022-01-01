package utils;

import cds.savot.model.SavotTD;
import cds.savot.model.SavotTR;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.util.ArrayList;

public class DataExporter extends StdSerializer<Tab> {

    public DataExporter() {
        this(null);
    }

    public DataExporter(Class<Tab> data) {
        super(data);
    }

    @Override
    public void serialize(Tab tab, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        var tableView = (TableView<SavotTR>) tab.getContent();
        var cols = new ArrayList<String>();
        for (var column : tableView.getColumns()) {
            cols.add(column.getText());
        }

        jsonGenerator.writeStringField("catalogue", tab.getText());
        jsonGenerator.writeObjectField("columns", cols);

        var rows = new ArrayList<String>();
        for (var value : tableView.getItems()) {
            for (var f : value.getTDSet().getItems()) {
                rows.add(((SavotTD) f).getContent());
            }
        }
        jsonGenerator.writeObjectField("rows", rows);
        jsonGenerator.writeEndObject();
    }
}
