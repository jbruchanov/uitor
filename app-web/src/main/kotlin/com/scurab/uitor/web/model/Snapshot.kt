package com.scurab.uitor.web.model

import kotlin.js.Json

interface Snapshot {
    var name: String
    var version: String
    var taken: String
    var viewHierarchy: Json
    var clientConfiguration: Json
    var screenComponents: Json
    var screenStructure: String
    var screenshot: String
    var viewShots: Array<String?>
    var logCat: String
}