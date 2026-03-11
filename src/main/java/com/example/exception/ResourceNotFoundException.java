package com.example.exception;

import lombok.Getter;

/** Thrown when a requested resource cannot be found. */
@Getter
public class ResourceNotFoundException extends RuntimeException {

  private final String resourceType;
  private final Object resourceId;

  public ResourceNotFoundException(String resourceType, Object resourceId) {
    super(String.format("%s with id '%s' not found", resourceType, resourceId));
    this.resourceType = resourceType;
    this.resourceId = resourceId;
  }

  public ResourceNotFoundException(String message) {
    super(message);
    this.resourceType = "Resource";
    this.resourceId = null;
  }
}
