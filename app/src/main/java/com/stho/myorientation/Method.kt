package com.stho.myorientation

enum class Method(private val description: String) {
    AccelerometerMagnetometer("Accelerometer & Magnetometer"),
    RotationVector("Rotation Vector"),
    ComplementaryFilter("Complementary Fusion Filter"),
    KalmanFilter("Kalman Filter"),
    MadgwickFilter("Madgwick Filter"),
    SeparatedCorrectionFilter("Separated Correction Filter"),
    ExtendedComplementaryFilter("Extended Complementary Filter"),
    Composition("Composition Filter"),
    Damped("(Damped)");

    override fun toString(): String =
        description
}

