package com.techconative.restel.core.http;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseBody {
    private Object body;
}
