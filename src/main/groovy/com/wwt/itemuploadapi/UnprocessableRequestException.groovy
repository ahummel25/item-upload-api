package com.wwt.itemuploadapi

class UnprocessableRequestException extends ApplicationException {
    UnprocessableRequestException(String message) {
        super(message)
        statusCode = 422
    }
}
