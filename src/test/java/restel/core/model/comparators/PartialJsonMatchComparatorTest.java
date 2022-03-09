package restel.core.model.comparators;

import com.techconative.restel.core.http.RESTResponse;
import com.techconative.restel.core.http.ResponseBody;
import com.techconative.restel.core.model.comparator.PartialJsonMatchComparator;
import com.techconative.restel.exception.RestelException;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.Map;

public class PartialJsonMatchComparatorTest {

    @Test
    public void testCompare() {
        PartialJsonMatchComparator comparator = new PartialJsonMatchComparator();
        RESTResponse restResponse = new RESTResponse();

        restResponse.setHeaders(Map.of(HttpHeaders.CONTENT_TYPE.toLowerCase(), MediaType.APPLICATION_JSON));
        restResponse.setResponse(ResponseBody.builder().body(Map.of("k", "v", "p", "q")).build());

        Assert.assertTrue(comparator.compare(restResponse, Map.of("k", "v")));
        Assert.assertTrue(comparator.compare(Map.of("k", "v", "p", "q"), Map.of("k", "v")));

    }

    @Test(expected = RestelException.class)
    public void testCompareInvalidMedia() {
        PartialJsonMatchComparator comparator = new PartialJsonMatchComparator();
        RESTResponse restResponse = new RESTResponse();

        restResponse.setHeaders(Map.of(HttpHeaders.CONTENT_TYPE.toLowerCase(), MediaType.TEXT_PLAIN));
        restResponse.setResponse(ResponseBody.builder().body(Map.of("k", "v", "p", "q")).build());

        comparator.compare(restResponse, Map.of("k", "v"));
    }

    @Test
    public void testCompareExact() {
        PartialJsonMatchComparator comparator = new PartialJsonMatchComparator();
        RESTResponse restResponse = new RESTResponse();

        restResponse.setHeaders(Map.of(HttpHeaders.CONTENT_TYPE.toLowerCase(), MediaType.APPLICATION_JSON));
        restResponse.setResponse(ResponseBody.builder().body(Map.of("k", "v")).build());

        Assert.assertTrue(comparator.compare(restResponse, Map.of("k", "v")));
    }


    @Test(expected = AssertionError.class)
    public void testCompareMoreContent() {
        PartialJsonMatchComparator comparator = new PartialJsonMatchComparator();
        RESTResponse restResponse = new RESTResponse();

        restResponse.setHeaders(Map.of(HttpHeaders.CONTENT_TYPE.toLowerCase(), MediaType.APPLICATION_JSON));
        restResponse.setResponse(ResponseBody.builder().body(Map.of("k", "v")).build());

        comparator.compare(restResponse, Map.of("k", "v", "p", "q"));
    }
}
