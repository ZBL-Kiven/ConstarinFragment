package com.zj.cf.unitive

enum class Lifecycle(val value: Int) {
    NONE(-1), CREATE(-1), CREATED(3), START(9), RESTART(9), RESUME(15), PAUSE(8), STOP(4), DESTROY(2)
}