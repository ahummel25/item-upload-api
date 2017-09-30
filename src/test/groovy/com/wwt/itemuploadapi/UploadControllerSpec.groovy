package com.wwt.itemuploadapi

import com.wwt.itemuploadapi.helper.ItemUploadApiConstants
import com.wwt.itemuploadapi.CreationService
import com.wwt.security.SecurityService
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(UploadController)
class UploadControllerSpec extends Specification{

    def securityService

    def mockOutput = ['code': null, 'message': null]

    def setup() {

        securityService = Mock(SecurityService) {
            getLoggedInUserName() >> ItemUploadApiConstants.LOGGED_IN_USER
        }

        controller.securityService = securityService
    }

    def "upload()"() {

        given:
        FileInputStream mockFileInputStream = new FileInputStream('src/main/resources/public/utilities/fileTest.xlsx')
        MockMultipartFile mockFile = new MockMultipartFile('file', mockFileInputStream)
        def creationService = Mock(CreationService) {
            def mockReturn = [code: 'S', message: 'null']
            return mockReturn
        }
        controller.creationService = creationService

        when: 'called with valid file'
        request.addFile(mockFile)
        mockOutput = controller.upload()

        then: 'should render valid http response and output'
        //1 * (controller.creationService.create(mockFile.getInputStream(), "hummela")) >> mockOutput
        println mockOutput
        response.status == 200
    }
}
