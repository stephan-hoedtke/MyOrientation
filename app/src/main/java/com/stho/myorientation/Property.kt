package com.stho.myorientation

enum class Property(private val description: String) {
    Azimuth("Azimuth"),
    Pitch("Pitch"),
    Roll("Roll"),
    CenterAzimuth("Center Azimuth"),
    CenterAltitude("Center Altitude");

    override fun toString(): String =
        description
}

