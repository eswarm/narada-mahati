package `in`.eswarm.mahati.mqtt.common

// MQTT topic filter matching for + and # wildcards.
fun mqttTopicMatches(filter: String, topic: String): Boolean {
    if (filter == "#") return true

    val filterLevels = filter.split('/')
    val topicLevels = topic.split('/')

    var index = 0
    while (index < filterLevels.size) {
        val filterLevel = filterLevels[index]

        if (filterLevel == "#") {
            return index == filterLevels.lastIndex
        }

        if (index >= topicLevels.size) return false

        val topicLevel = topicLevels[index]
        if (filterLevel != "+" && filterLevel != topicLevel) return false

        index++
    }

    return index == topicLevels.size
}

