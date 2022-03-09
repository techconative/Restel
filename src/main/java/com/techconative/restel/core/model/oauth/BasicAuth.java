package com.techconative.restel.core.model.oauth;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class BasicAuth {
    @NonNull
    private String username;
    @NonNull
    private String password;
}
