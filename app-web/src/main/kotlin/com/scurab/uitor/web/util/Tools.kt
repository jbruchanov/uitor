package com.scurab.uitor.web.util

import com.scurab.uitor.web.model.ViewNode

/**
 * Help function to pick a viewNode for notification.
 * If it's same as selected, returns null => turn of selection mode
 * otherwise pick the found as new selection
 */
internal fun pickNodeForNotification(selectedNode: ViewNode?, found: ViewNode?): ViewNode? {
    return when {
        selectedNode != null && found == selectedNode -> null
        else -> found
    }
}