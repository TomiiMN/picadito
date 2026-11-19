package com.tomasmendez.picadito.shared.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super("%s with id %s not found".formatted(resourceName, id));
    }

    public ErrorCode getErrorCode() {
        return ErrorCode.RESOURCE_NOT_FOUND;
    }
}
