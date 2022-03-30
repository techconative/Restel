package com.techconative.restel.core.model.oauth;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ResourceOwnerPassword {
  @NonNull private String username;
  @NonNull private String password;
  @NonNull private String authUrl;
  @NonNull private String clientId;
  @NonNull private String clientSecret;
  private String scope;
}
