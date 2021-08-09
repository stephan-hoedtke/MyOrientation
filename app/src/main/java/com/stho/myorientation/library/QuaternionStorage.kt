package com.stho.myorientation.library

import com.stho.myorientation.library.algebra.Quaternion

class QuaternionStorage : IQuaternionStorage {

    override var position: Quaternion = Quaternion.zero
        private set

    override fun setTargetPosition(targetPosition: Quaternion) {
        position = targetPosition
    }
}

