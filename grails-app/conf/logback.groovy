import com.splunk.logging.HttpEventCollectorLogbackAppender
import ch.qos.logback.classic.PatternLayout
import grails.util.Environment
import com.wwt.customapps.CloudUtilities

def productionLogLevel = WARN

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('stdout', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{MM.dd-HH:mm:ss.SSS} [%thread] %-5level %logger - %m"
    }
}

if (Environment.isDevelopmentMode()) {
    root(INFO, ['stdout'])
}

// add Splunk HTTP appender when deployed to CF
if (CloudUtilities.isCloudDeployed()) {
    def splunkCreds = CloudUtilities.getServiceCredentials('splunk-http-collector')
    appender('splunk', HttpEventCollectorLogbackAppender) {
        url = splunkCreds.url
        token = splunkCreds.token
        disableCertificateValidation = true
        metadata = [
            app            : CloudUtilities.getApplicationName(),
            version        : CloudUtilities.getApplicationVersion(),
            cfInstance     : CloudUtilities.getCfInstanceName(),
            cfInstanceIndex: CloudUtilities.getCfInstanceIndex(),
            cfInstanceId   : CloudUtilities.getCfInstanceId()
        ]
        layout(PatternLayout) {
            pattern = "%d{MM.dd-HH:mm:ss.SSS} [%thread] %-5level %logger - %m"
        }
    }

    root(productionLogLevel, ['splunk'])
}

logger('org', ERROR)
logger('grails.plugin', ERROR)
logger('grails.plugins', ERROR)
logger('grails.util', ERROR)

