/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.util.date

/**
 * Create new gmt date from the [timestamp].
 * @param timestamp is a number of epoch milliseconds (it is `now` by default).
 */
public actual fun GMTDate(timestamp: Long?): GMTDate {
    TODO("not supported yet")
}

/**
 * Create an instance of [GMTDate] from the specified date/time components
 */
public actual fun GMTDate(seconds: Int, minutes: Int, hours: Int, dayOfMonth: Int, month: Month, year: Int): GMTDate {
    TODO("not supported yet")
}

/**
 * Invalid exception: possible overflow or underflow
 */
public class InvalidTimestampException(timestamp: Long) : IllegalStateException(
    "Invalid date timestamp exception: $timestamp"
)

/**
 * Gets current system time in milliseconds since certain moment in the past, only delta between two subsequent calls makes sense.
 */
public actual fun getTimeMillis(): Long = TODO("not supported yet")
