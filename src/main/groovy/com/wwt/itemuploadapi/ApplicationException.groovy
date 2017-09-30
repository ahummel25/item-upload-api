package com.wwt.itemuploadapi

class ApplicationException extends Exception {
    protected String message
    protected String source
    protected List errors
    protected int statusCode = 500

    // Restricting which constructors can be used so that no unexpected behavior occurs in ErrorsController

    ApplicationException(String message) {
        super(message)
        println "Here"
        this.message = message
    }
}
