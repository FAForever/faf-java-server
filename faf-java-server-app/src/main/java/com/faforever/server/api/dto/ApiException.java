package com.faforever.server.api.dto;


import com.github.jasminb.jsonapi.models.errors.Error;

import java.util.List;
import java.util.stream.Collectors;

public class ApiException extends RuntimeException {

  public ApiException(List<? extends Error> errors) {
    super(errors.stream()
      .map(Error::getDetail)
      .collect(Collectors.joining("\n")));
  }
}
