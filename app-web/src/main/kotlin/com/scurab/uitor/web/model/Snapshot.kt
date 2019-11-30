package com.scurab.uitor.web.model

import kotlin.js.Json

interface Snapshot {
    var name: String
    var version: String
    var taken: String
    var clientConfiguration: Json
    var resources: Json?
    var screenComponents: Json
    var screenStructure: String
    var screenshot: String
    var viewHierarchy: Json
    var viewShots: Array<String?>
    var logCat: String
}