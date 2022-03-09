package com.techconative.restel.core.model.assertion;

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
    private AssertType assertType;
    @NonNull
    private String actual;
    private String expected;
    private String message;
}
