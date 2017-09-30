package com.wwt.itemuploadapi

import grails.converters.JSON
import org.springframework.web.multipart.MultipartHttpServletRequest
import com.wwt.security.SecurityService

class UploadController {

    SecurityService securityService

    static responseFormats = ['json']

    def creationService

    def upload() {

        def outPut = ['code': '', 'message': '']

        def userName = securityService.loggedInUserName

        def file = request instanceof MultipartHttpServletRequest ? request.getFile('file') : null

        if(file) {
            outPut = creationService.create(file.getInputStream(), userName)
            render ((outPut ? outPut : [:]) as JSON)
        } else {
            render view: '/fileNotFound'
        }
    }
}
