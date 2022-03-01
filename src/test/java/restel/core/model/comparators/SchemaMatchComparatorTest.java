package restel.core.model.comparators;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchemaException;
import com.pramati.restel.core.http.RESTResponse;
import com.pramati.restel.core.http.ResponseBody;
import com.pramati.restel.core.model.comparator.SchemaMatchComparator;
import com.pramati.restel.exception.RestelException;
import com.pramati.restel.utils.ObjectMapperUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SchemaMatchComparatorTest {

    @Test
    public void testCompareTest() throws IOException {
        RESTResponse restResponse = new RESTResponse();
        SchemaMatchComparator comparator = new SchemaMatchComparator();
        JsonNode node = getJson().get("valid");
        restResponse.setResponse(ResponseBody.builder().body(node.get("response")).build());
        restResponse.setHeaders(Map.of(HttpHeaders.CONTENT_TYPE.toLowerCase(), MediaType.APPLICATION_JSON));
        Assert.assertTrue(comparator.compare(restResponse, node.get("schema")));
        Assert.assertTrue(comparator.compare(node.get("response"), node.get("schema")));
    }

    @Test(expected = RestelException.class)
    public void testCompareTestInvalidMedia() throws IOException {
        RESTResponse restResponse = new RESTResponse();
        SchemaMatchComparator comparator = new SchemaMatchComparator();
        JsonNode node = getJson().get("valid");
        restResponse.setResponse(ResponseBody.builder().body(node.get("response")).build());
        restResponse.setHeaders(Map.of(HttpHeaders.CONTENT_TYPE.toLowerCase(), MediaType.TEXT_PLAIN));
        comparator.compare(restResponse, node.get("schema"));
    }

    @Test(expected = AssertionError.class)
    public void testCompareTesFailed() throws IOException {
        SchemaMatchComparator comparator = new SchemaMatchComparator();
        JsonNode node = getJson().get("invalid");
        comparator.compare(node.get("response"), node.get("schema"));
    }

    @Test(expected = JsonSchemaException.class)
    public void testCompareTesInvalidSchema() throws IOException {
        SchemaMatchComparator comparator = new SchemaMatchComparator();
        JsonNode node = getJson().get("invalid_schema");
        comparator.compare(node.get("response"), node.get("schema"));
    }

    private JsonNode getJson() throws IOException {
        return ObjectMapperUtils.getMapper().readTree(new File("src/test/resources/schema.json"));
    }
}
