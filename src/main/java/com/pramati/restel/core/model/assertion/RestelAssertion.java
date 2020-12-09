package com.pramati.restel.core.model.assertion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class RestelAssertion {
    @NonNull
    private String name;
    @NonNull
    private String expected;
    @NonNull
    private String actual;
}
