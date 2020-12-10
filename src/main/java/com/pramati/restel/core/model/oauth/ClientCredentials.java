package com.pramati.restel.core.model.oauth;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ClientCredentials {
    @NonNull
    private String authUrl;

    @NonNull
    private String clientId;

    @NonNull
    private String clientSecret;

    private String scope;
}
