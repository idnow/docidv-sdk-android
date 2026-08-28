package io.idnow.docidv.sample.start

sealed class PendingIdentResult {
    data object Success : PendingIdentResult()
    data object Cancelled : PendingIdentResult()
    data class Error(val message: String) : PendingIdentResult()
}
