package com.nailorsh.astralis.core.utils.time

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Duration

fun currentInstant(): Instant {
    return Clock.System.now()
}

fun currentDate(): LocalDate {
    return Clock.System.todayIn(TimeZone.currentSystemDefault())
}

fun currentTime(): LocalTime {
    return currentDateTime().time
}

fun currentDateTime(): LocalDateTime {
    return currentInstant().toLocalDateTime(TimeZone.currentSystemDefault())
}

fun LocalDateTime.plus(value: Int, unit: DateTimeUnit.TimeBased): LocalDateTime {
    return TimeZone.currentSystemDefault().let {
        this.toInstant(it).plus(value, unit).toLocalDateTime(it)
    }
}

fun LocalDateTime.minus(value: Int, unit: DateTimeUnit.TimeBased): LocalDateTime {
    return TimeZone.currentSystemDefault().let {
        this.toInstant(it).minus(value, unit).toLocalDateTime(it)
    }
}

fun LocalDateTime.minus(duration: Duration): LocalDateTime {
    return TimeZone.currentSystemDefault().let {
        this.toInstant(it).minus(duration).toLocalDateTime(it)
    }
}