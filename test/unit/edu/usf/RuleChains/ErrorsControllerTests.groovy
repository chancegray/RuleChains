package edu.usf.RuleChains


import grails.converters.*
import grails.test.mixin.*
import org.junit.*

/**
 * Testing ErrorController which handles graceful AJAX errors
 * <p>
 * 
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(ErrorsController)
class ErrorsControllerTests {
    /**
     * Error 403 Test
     */
    void testError403() {
        controller.request.method = "GET"
        controller.request.contentType = "text/json"
        def model = controller.error403()
        assert controller.response.text == ''
    }
    /**
     * Error 404 Test
     */
    void testError404() {
        controller.request.method = "GET"
        controller.request.contentType = "text/json"
        def model = controller.error404()
        assert controller.response.text == ''
    }
    /**
     * Error 500 Test
     */
    void testError500() {
        controller.request.method = "GET"
        controller.request.contentType = "text/json"
        def model = controller.error500()
        assert controller.response.text == ''
    }
}
