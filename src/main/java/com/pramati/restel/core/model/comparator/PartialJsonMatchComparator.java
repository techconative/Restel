package com.pramati.restel.core.model.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.pramati.restel.core.http.RESTResponse;
import com.pramati.restel.exception.RestelException;
import com.pramati.restel.utils.ObjectMapperUtils;
import com.pramati.restel.utils.Utils;
import io.qameta.allure.Allure;
import org.springframework.stereotype.Component;
import ro.skyah.comparator.JSONCompare;

import javax.ws.rs.core.MediaType;

@Component(value = "PARTIAL_MATCHER")
public class PartialJsonMatchComparator implements ResponseComparator {

    @Override
    public void compareResponse(RESTResponse restResponse, Object expectedOutput) {
        String media = Utils.getMediaType(restResponse);
        switch (Utils.getMediaType(restResponse)) {
            case MediaType.APPLICATION_JSON:
                evalJson(restResponse.getResponse().getBody(), expectedOutput);
                break;
            case MediaType.APPLICATION_XML:
                break;
            default:
                throw new RestelException("MEDIA_NOT_APP_JSON", media);
        }
    }

    @Override
    public void compareHeader(Object headers, Object expectedHeaders) {
        evalJson(headers, expectedHeaders);
    }

    private void evalJson(Object restResponse, Object expectedOutput) {
        JsonNode expectedOutputnode = ObjectMapperUtils.convertToJsonNode(expectedOutput);
        JsonNode actualOutputNode = ObjectMapperUtils.convertToJsonNode(restResponse);

        Allure.step("Evaluating with PartialJsonMatcher:: Actual :- " + actualOutputNode.toPrettyString() + " Expected :- " + expectedOutputnode.toPrettyString());
        // First pass the expectedOutput(which is partial response) then actual in JsonCompare.
        JSONCompare.assertEquals(expectedOutputnode, actualOutputNode);
    }
}
