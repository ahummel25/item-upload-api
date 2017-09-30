var frisby = require('frisby'),
    config = require('./apiTestConfig');

module.exports = function () {
    frisby.globalSetup({ // globalSetup is for ALL requests
        request: {
            headers: {
                'REMOTE_USER': config.userName,
                'user-agent': 'Java'
            },
            inspectOnFailure: true
        }
    });
};