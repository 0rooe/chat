package com.chatapp.relationship.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RelationshipAlreadyExistsException extends RuntimeException {
    public RelationshipAlreadyExistsException(String message) {
        super(message);
    }
}