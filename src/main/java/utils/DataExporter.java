package utils;

import cds.savot.model.SavotTD;
import cds.savot.model.SavotTR;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.util.ArrayList;

public class DataExporter extends StdSerializer<TableView> {

    public DataExporter() {
        this(null);
    }

    public DataExporter(Class<TableView> data) {
        super(data);
    }

    @Override
    public void serialize(TableView tableView, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        var tableV = (TableView<SavotTR>) tableView;
        var cols = new ArrayList<String>();
        for (var column : tableV.getColumns()) {
            cols.add(column.getText());
        }

        jsonGenerator.writeObjectField("columns", cols);


        var rows = new ArrayList<String>();
        for (var value : tableV.getItems()) {
            for (var f : value.getTDSet().getItems()) {
                rows.add(((SavotTD) f).getContent());
            }
        }
        jsonGenerator.writeObjectField("rows", rows);
    }
}
