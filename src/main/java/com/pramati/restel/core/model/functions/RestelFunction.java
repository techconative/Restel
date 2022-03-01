package com.pramati.restel.core.model.functions;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class RestelFunction {
    @NonNull
    private FunctionOps operation;
    @NonNull
    private String data;
    private List<String> args;
}
