package `in`.eswarm.narada.log

actual fun console(level: LogLevel, tag: String, msg: String) {
    print("${level.name}/$tag: $msg")
}