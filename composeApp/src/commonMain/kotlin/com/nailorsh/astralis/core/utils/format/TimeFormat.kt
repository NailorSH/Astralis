package com.nailorsh.astralis.core.utils.format

import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char

val LocalTime.Formats.HOUR_MINUTE_SECOND: DateTimeFormat<LocalTime>
    get() = run {
        LocalTime.Format {
            hour(); char(':'); minute(); char(':'); second()
        }
    }