var config = {},
    util = require('util'),
    testEnvironment = process.env.TEST_ENV || 'dev',
    port = process.env.PORT || '8080';

/*
 * Build URL based on 'TEST_ENV' and 'PORT' environment variables.
 *
 * Valid values for 'TEST_ENV' are 'dev' and 'local'. Defaults to 'dev'.
 *
 * PORT is only used when 'TEST_ENV' is local. Defaults to '8080'.
 */
testEnvironment = testEnvironment.toLowerCase();

var context = 'item-upload-api';
if (testEnvironment === 'dev') {
    config.contextRootUrl = 'http://a2ws-dev.wwt.com/' + context;
} else if (testEnvironment === 'local') {
    config.contextRootUrl = util.format('http://localhost:%s/' + context, port);
}

config.rootUrl = config.contextRootUrl + '/api';

config.userName = process.env.USER_NAME || 'loadtestuser1';

module.exports = config;