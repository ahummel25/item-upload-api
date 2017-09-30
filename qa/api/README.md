# API Testing

Tests API directly using [Frisby.js](http://frisbyjs.com/).

There is an entry in `package.json` to simplify running these tests. You can execute `npm run test-api`, or
inspect the `package.json` file to get the specifics and create your own command.
Frisby uses [jasmine-node](https://github.com/mhevery/jasmine-node) under the hood, which has plenty of fun options
for things like auto-watching, various outputs, etc.


### Options
We use environment variables to set options like which environment to use. Available options are,

* **TEST_ENV**. Direct the tests against the appropriate server. Valid values are 'dev' and 'local'.
Defaults to 'dev'. Note that when targeting DEV, we're using the a2ws network to bypass NAM.
* **PORT**. Only used when `TEST_ENV` is `local`. Defaults to `8080`.
* **USER_NAME**. Only relevant when `TEST_ENV` is `dev`. Sets the appropriate test user.
Defaults to `loadtestuser1`.

This is early and I'd love input!
