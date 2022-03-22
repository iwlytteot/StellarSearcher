package utils;

import cds.savot.model.SavotTD;
import cds.savot.model.SavotTR;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import javafx.scene.control.TableView;
import model.OutputData;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Class for mapping between Java class OutputData to JSON object.
 */
@Component
@SuppressWarnings("unchecked")
public class DataExporter extends StdSerializer<OutputData> {

    public DataExporter() {
        this(null);
    }

    public DataExporter(Class<OutputData> data) {
        super(data);
    }

    @Override
    public void serialize(OutputData outputData, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("input", outputData.getInput());
        jsonGenerator.writeStringField("radius", outputData.getRadius());

        var tab = outputData.getTab();

        var tableView = (TableView<SavotTR>) tab.getContent();
        var cols = new ArrayList<String>();
        for (var column : tableView.getColumns()) {
            cols.add(column.getText());
        }

        jsonGenerator.writeStringField("catalogue", tab.getText());
        jsonGenerator.writeObjectField("columns", cols);

        var row = new ArrayList<String>();
        var rows = new ArrayList<ArrayList<String>>();
        for (var value : tableView.getItems()) {
            for (var f : value.getTDSet().getItems()) {
                row.add(((SavotTD) f).getContent());
            }
            rows.add(row);
        }
        jsonGenerator.writeObjectField("rows", rows);

        jsonGenerator.writeEndObject();
    }
}
