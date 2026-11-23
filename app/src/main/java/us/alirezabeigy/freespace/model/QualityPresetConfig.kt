package us.alirezabeigy.freespace.model

data class QualityPresetConfig(
    val resolutionShortSide: Int = 0,
    val targetFps: Int = 0,
    val sizeRatio: Float = 0.7f,
    val audioBitrate: Int = 320000,
    val label: String? = null
)
