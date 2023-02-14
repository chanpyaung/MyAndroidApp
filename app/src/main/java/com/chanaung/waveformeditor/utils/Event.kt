package com.chanaung.waveformeditor.utils

open class Event<out T>(private val content: T) {

    var emitted = false

    fun getContentIfNotEmitted(): T? {
        return if (emitted) {
            null
        } else {
            emitted = true
            content
        }
    }

    fun peekContent(): T = content
}