package com.example.exception;

import lombok.AllArgsConstructor;

/** Thrown when a resource creation or update would violate a uniqueness constraint. */
@AllArgsConstructor
public class ConflictException extends RuntimeException {

  public ConflictException(String message) {
    super(message);
  }
}
