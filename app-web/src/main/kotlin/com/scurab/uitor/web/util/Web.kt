package com.scurab.uitor.web.util

import com.scurab.uitor.common.util.npe
import org.w3c.dom.Document

fun Document.requireElementById(id: String) = getElementById(id) ?: npe(id)
