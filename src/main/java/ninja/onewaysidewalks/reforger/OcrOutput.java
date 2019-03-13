package ninja.onewaysidewalks.reforger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;

import java.util.List;

@Data
public class OcrOutput {

    @JacksonXmlElementWrapper(localName = "page")
    @JsonProperty("block")
    private List<String> lines;
}
