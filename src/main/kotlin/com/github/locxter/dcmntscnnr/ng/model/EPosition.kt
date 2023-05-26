package com.github.locxter.dcmntscnnr.ng.model

enum class EPosition(val hasMoveLeft: Boolean, val hasMoveRight: Boolean) {
    POSITION_ALONE(false, false),
    POSITION_LEFT(false, true),
    POSITION_MIDDLE(true, true),
    POSITION_RIGHT(true, false)
}
