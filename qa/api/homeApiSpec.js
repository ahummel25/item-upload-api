/*
 * Example API test which hits the context root and verifies that we get back a 200 response
 */

var config = require('./apiTestConfig');
var frisby = require('frisby');

require('./globalSetup')();

frisby.create('The home page should return a 200')
    .get(config.contextRootUrl)
    .expectStatus(200)
    .toss();
