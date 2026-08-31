package com.hlima.erp.shared.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends AppException {

    public NotFoundException(String resource) {
        super(resource + " não encontrado.", HttpStatus.NOT_FOUND);
    }
}
