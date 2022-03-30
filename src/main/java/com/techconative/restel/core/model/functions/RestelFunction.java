package com.techconative.restel.core.model.functions;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class RestelFunction {
  @NonNull private FunctionOps operation;
  @NonNull private String data;
  private List<String> args;
}
