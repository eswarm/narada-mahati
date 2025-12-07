package `in`.eswarm.narada.log

internal actual fun log(tag: String, message: String) {
    println("$tag: $message")
}
