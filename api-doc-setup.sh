# Copy RAML api-console from node_modules to web-app folder

CONSOLE_PATH="node_modules/wwt-raml-api-console/dist"
NEW_PATH="src/main/resources/public"

cp -R "$CONSOLE_PATH/authentication" $NEW_PATH
cp -R "$CONSOLE_PATH/fonts" $NEW_PATH
cp -R "$CONSOLE_PATH/img" $NEW_PATH
cp -R "$CONSOLE_PATH/scripts" $NEW_PATH
cp -R "$CONSOLE_PATH/styles" $NEW_PATH