package com.hlima.erp.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Violação de regra de negócio (ex: estoque insuficiente pra iniciar uma
 * ordem de produção, tentar concluir uma OP que não está em produção).
 */
public class BusinessException extends AppException {

    public BusinessException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
