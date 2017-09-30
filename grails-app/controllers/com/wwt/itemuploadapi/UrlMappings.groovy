package com.wwt.itemuploadapi

class UrlMappings {

    static mappings = {

        group("/api") {
            "/upload"(controller: "upload") { action = [POST: "upload"] }
        }
        "/"(redirect: '/static/index.html')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
