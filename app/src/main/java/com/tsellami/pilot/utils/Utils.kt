package com.tsellami.pilot.utils

class Utils {

    companion object {
        private const val GERMANY_PREFIX = "ED"

        fun checkIcao(entry: String): Boolean {
            return entry.length == 4 && entry.startsWith(GERMANY_PREFIX, ignoreCase = true)
        }
    }
}

val <T> T.exhaustive: T
    get() = this
