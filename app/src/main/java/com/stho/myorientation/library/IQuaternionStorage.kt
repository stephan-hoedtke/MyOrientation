package com.stho.myorientation.library

import com.stho.myorientation.library.algebra.IRotation
import com.stho.myorientation.library.algebra.Quaternion

interface IQuaternionStorage {
    val position: Quaternion
    fun setTargetPosition(targetPosition: Quaternion)
}
