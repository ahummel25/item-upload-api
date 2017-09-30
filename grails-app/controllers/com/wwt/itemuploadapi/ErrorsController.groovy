package com.wwt.itemuploadapi

class ErrorsController {

    static responseFormats = ['json']

    def notFoundHandler() {

        def message = [message: "The requested resource was not found.", source: "item-upload-api"]

        response.status = 404
        respond message
    }

    def exceptionHandler() {
        def message = [message: request.exception?.message ?: "An unexpected error occured on the server.", source: "item-upload-api"]
        response.status = 500
        respond message
    }
}
