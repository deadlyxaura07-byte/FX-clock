package com.example.data.model

data class LapItem(
    val lapNumber: Int,
    val lapTimeMillis: Long,
    val totalTimeMillis: Long,
    val isFastest: Boolean = false,
    val isSlowest: Boolean = false
) {
    fun formattedLapTime(): String = formatMillis(lapTimeMillis)
    fun formattedTotalTime(): String = formatMillis(totalTimeMillis)

    companion object {
        fun formatMillis(millis: Long): String {
            val minutes = (millis / 60000) % 60
            val seconds = (millis / 1000) % 60
            val hundredths = (millis % 1000) / 10
            return String.format("%02d:%02d.%02d", minutes, seconds, hundredths)
        }
    }
}
