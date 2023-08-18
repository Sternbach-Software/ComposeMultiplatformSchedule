package com.danielrampelt.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours


data class Event(
    val name: String,
    val color: Color,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val description: String? = null,
)

inline class SplitType private constructor(val value: Int) {
    companion object {
        val None = SplitType(0)
        val Start = SplitType(1)
        val End = SplitType(2)
        val Both = SplitType(3)
    }
}

data class PositionedEvent(
    val event: Event,
    val splitType: SplitType,
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val col: Int = 0,
    val colSpan: Int = 1,
    val colTotal: Int = 1,
)

val timeZone by lazy { TimeZone.currentSystemDefault() }
val now by lazy { Clock.System.now() }
val nowAsLocalDateTime by lazy { now.toLocalDateTime(timeZone) }
val today by lazy { now.toLocalDateTime(timeZone).date }

/**
 * Formats a [LocalDateTime] in the format h:mm a.
 * */
fun LocalDateTime.timeFormat() =
    "${if (hour == 0) 12 else hour % 12}:${
        minute.toString().padStart(2, '0')
    } ${if (hour < 12) "AM" else "PM"}"

fun LocalDate.dayFormat() = "${dayOfWeek.name}, ${month.name} $dayOfMonth"
fun LocalTime.hourFormat() = "${if (hour == 0) 12 else hour % 12} ${if (hour < 12) "AM" else "PM"}"

@Composable
fun BasicEvent(
    positionedEvent: PositionedEvent,
    modifier: Modifier = Modifier,
) {
    val event = positionedEvent.event
    val topRadius =
        if (positionedEvent.splitType == SplitType.Start || positionedEvent.splitType == SplitType.Both) 0.dp else 4.dp
    val bottomRadius =
        if (positionedEvent.splitType == SplitType.End || positionedEvent.splitType == SplitType.Both) 0.dp else 4.dp
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                end = 2.dp,
                bottom = if (positionedEvent.splitType == SplitType.End) 0.dp else 2.dp
            )
            .clipToBounds()
            .background(
                event.color,
                shape = RoundedCornerShape(
                    topStart = topRadius,
                    topEnd = topRadius,
                    bottomEnd = bottomRadius,
                    bottomStart = bottomRadius,
                )
            )
            .padding(4.dp)
    ) {
        Text(
            text = "${event.start.timeFormat()} - ${event.end.timeFormat()}",
            style = MaterialTheme.typography.caption,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )

        Text(
            text = event.name,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (event.description != null) {
            Text(
                text = event.description,
                style = MaterialTheme.typography.body2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

val sampleEvents by lazy {
    listOf(
        Event(
            name = "Google I/O Keynote",
            color = Color(0xFFAFBBF2),
            start = LocalDateTime.parse("2021-05-18T09:00:00"),
            end = LocalDateTime.parse("2021-05-18T11:00:00"),
            description = "Tune in to find out about how we're furthering our mission to organize the world’s information and make it universally accessible and useful.",
        ),
        Event(
            name = "Developer Keynote",
            color = Color(0xFFAFBBF2),
            start = LocalDateTime.parse("2021-05-18T09:00:00"),
            end = LocalDateTime.parse("2021-05-18T10:00:00"),
            description = "Learn about the latest updates to our developer products and platforms from Google Developers.",
        ),
        Event(
            name = "What's new in Android",
            color = Color(0xFF1B998B),
            start = LocalDateTime.parse("2021-05-18T10:00:00"),
            end = LocalDateTime.parse("2021-05-18T11:00:00"),
            description = "In this Keynote, Chet Haase, Dan Sandler, and Romain Guy discuss the latest Android features and enhancements for developers.",
        ),
        Event(
            name = "What's new in Material Design",
            color = Color(0xFF6DD3CE),
            start = LocalDateTime.parse("2021-05-18T11:00:00"),
            end = LocalDateTime.parse("2021-05-18T11:45:00"),
            description = "Learn about the latest design improvements to help you build personal dynamic experiences with Material Design.",
        ),
        Event(
            name = "What's new in Machine Learning",
            color = Color(0xFFF4BFDB),
            start = LocalDateTime.parse("2021-05-18T10:00:00"),
            end = LocalDateTime.parse("2021-05-18T11:00:00"),
            description = "Learn about the latest and greatest in ML from Google. We’ll cover what’s available to developers when it comes to creating, understanding, and deploying models for a variety of different applications.",
        ),
        Event(
            name = "What's new in Machine Learning",
            color = Color(0xFFF4BFDB),
            start = LocalDateTime.parse("2021-05-18T10:30:00"),
            end = LocalDateTime.parse("2021-05-18T11:30:00"),
            description = "Learn about the latest and greatest in ML from Google. We’ll cover what’s available to developers when it comes to creating, understanding, and deploying models for a variety of different applications.",
        ),
        Event(
            name = "Jetpack Compose Basics",
            color = Color(0xFF1B998B),
            start = LocalDateTime.parse("2021-05-20T12:00:00"),
            end = LocalDateTime.parse("2021-05-20T13:00:00"),
            description = "This Workshop will take you through the basics of building your first app with Jetpack Compose, Android's new modern UI toolkit that simplifies and accelerates UI development on Android.",
        ),
    )
}

private class EventDataModifier(
    val positionedEvent: PositionedEvent,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = positionedEvent
}

private fun Modifier.eventData(positionedEvent: PositionedEvent) =
    this.then(EventDataModifier(positionedEvent))

@Composable
fun BasicDayHeader(
    day: LocalDate,
    modifier: Modifier = Modifier,
) {
    Text(
        text = day.dayFormat(),
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
    )
}

@Composable
fun ScheduleHeader(
    minDate: LocalDate,
    maxDate: LocalDate,
    dayWidth: Dp,
    modifier: Modifier = Modifier,
    dayHeader: @Composable (day: LocalDate) -> Unit = { BasicDayHeader(day = it) },
) {
    Row(modifier = modifier) {
        val numDays = minDate.until(maxDate, DateTimeUnit.DAY) + 1
        repeat(numDays) { i ->
            Box(modifier = Modifier.width(dayWidth)) {
                dayHeader(minDate + DatePeriod(days = i))
            }
        }
    }
}

@Composable
fun BasicSidebarLabel(
    time: LocalTime,
    modifier: Modifier = Modifier,
) {
    Text(
        text = time.hourFormat(),
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp)
    )
}

internal const val NANOS_PER_ONE = 1_000_000_000

internal val LOCAL_TIME_MIN: LocalTime = LocalTime(0, 0, 0, 0)
internal val LOCAL_TIME_MAX: LocalTime = LocalTime(23, 59, 59, NANOS_PER_ONE - 1)
fun LocalTime.toInstant(tz: TimeZone) = atDate(now.toLocalDateTime(tz).date).toInstant(tz)

@Composable
fun ScheduleSidebar(
    hourHeight: Dp,
    modifier: Modifier = Modifier,
    minTime: LocalTime = LOCAL_TIME_MIN,
    maxTime: LocalTime = LOCAL_TIME_MAX,
    label: @Composable (time: LocalTime) -> Unit = { BasicSidebarLabel(time = it) },
) {
    val minTimeToInstant = minTime.toInstant(timeZone)
    val maxTimeInstant = maxTime.toInstant(timeZone)
    val numMinutes = minTimeToInstant.until(maxTimeInstant, DateTimeUnit.MINUTE).toInt() + 1
    val numHours = numMinutes / 60
    val firstHour = LocalTime(minTime.hour, 0, 0, 0)
    val firstHourAsInstant = firstHour.toInstant(timeZone)
    val firstHourPlusOneHour = firstHourAsInstant + 1.hours
    val firstHourOffsetMinutes =
        if (firstHour == minTime) 0
        else minTimeToInstant.until(firstHourPlusOneHour, DateTimeUnit.MINUTE)
    val firstHourOffset = hourHeight * (firstHourOffsetMinutes / 60f)
    val startTime = if (firstHour == minTime) firstHourAsInstant else firstHourPlusOneHour
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(firstHourOffset))
        repeat(numHours) { i ->
            Box(modifier = Modifier.height(hourHeight)) {
                label((startTime + i.hours).toLocalDateTime(timeZone).time)
            }
        }
    }
}

private fun splitEvents(events: List<Event>): List<PositionedEvent> {
    return events
        .map { event ->
            val startDate = event.start
            val endDate = event.end
            if (startDate == endDate) {
                listOf(
                    PositionedEvent(
                        event,
                        SplitType.None,
                        event.start.date,
                        event.start.time,
                        event.end.time
                    )
                )
            } else {
                val startDateAsInstant = startDate.toInstant(timeZone)
                val days = startDateAsInstant.until(
                    endDate.toInstant(timeZone),
                    DateTimeUnit.DAY,
                    timeZone
                )
                val splitEvents = mutableListOf<PositionedEvent>()
                for (i in 0..days) {
                    val date = startDateAsInstant.plus(i.days).toLocalDateTime(timeZone)
                    splitEvents += PositionedEvent(
                        event,
                        splitType = if (date == startDate) SplitType.End else if (date == endDate) SplitType.Start else SplitType.Both,
                        date = date.date,
                        start = if (date == startDate) event.start.time else LOCAL_TIME_MIN,
                        end = if (date == endDate) event.end.time else LOCAL_TIME_MAX,
                    )
                }
                splitEvents
            }
        }
        .flatten()
}

private fun PositionedEvent.overlapsWith(other: PositionedEvent): Boolean {
    return date == other.date && start < other.end && end > other.start
}

private fun List<PositionedEvent>.timesOverlapWith(event: PositionedEvent): Boolean {
    return any { it.overlapsWith(event) }
}

private fun arrangeEvents(events: List<PositionedEvent>): List<PositionedEvent> {
    val positionedEvents = mutableListOf<PositionedEvent>()
    val groupEvents: MutableList<MutableList<PositionedEvent>> = mutableListOf()

    fun resetGroup() {
        groupEvents.forEachIndexed { colIndex, col ->
            col.forEach { e ->
                positionedEvents.add(e.copy(col = colIndex, colTotal = groupEvents.size))
            }
        }
        groupEvents.clear()
    }

    events.forEach { event ->
        var firstFreeCol = -1
        var numFreeCol = 0
        for (i in 0 until groupEvents.size) {
            val col = groupEvents[i]
            if (col.timesOverlapWith(event)) {
                if (firstFreeCol < 0) continue else break
            }
            if (firstFreeCol < 0) firstFreeCol = i
            numFreeCol++
        }

        when {
            // Overlaps with all, add a new column
            firstFreeCol < 0 -> {
                groupEvents += mutableListOf(event)
                // Expand anything that spans into the previous column and doesn't overlap with this event
                for (ci in 0 until groupEvents.size - 1) {
                    val col = groupEvents[ci]
                    col.forEachIndexed { ei, e ->
                        if (ci + e.colSpan == groupEvents.size - 1 && !e.overlapsWith(event)) {
                            col[ei] = e.copy(colSpan = e.colSpan + 1)
                        }
                    }
                }
            }
            // No overlap with any, start a new group
            numFreeCol == groupEvents.size -> {
                resetGroup()
                groupEvents += mutableListOf(event)
            }
            // At least one column free, add to first free column and expand to as many as possible
            else -> {
                groupEvents[firstFreeCol] += event.copy(colSpan = numFreeCol)
            }
        }
    }
    resetGroup()
    return positionedEvents
}

sealed class ScheduleSize {
    class FixedSize(val size: Dp) : ScheduleSize()
    class FixedCount(val count: Float) : ScheduleSize() {
        constructor(count: Int) : this(count.toFloat())
    }

    class Adaptive(val minSize: Dp) : ScheduleSize()
}

@Composable
fun Schedule(
    events: List<Event>,
    modifier: Modifier = Modifier,
    eventContent: @Composable (positionedEvent: PositionedEvent) -> Unit = {
        BasicEvent(
            positionedEvent = it
        )
    },
    dayHeader: @Composable (day: LocalDate) -> Unit = { BasicDayHeader(day = it) },
    timeLabel: @Composable (time: LocalTime) -> Unit = { BasicSidebarLabel(time = it) },
    minDate: LocalDate = events.minByOrNull(Event::start)?.start?.date ?: now.toLocalDateTime(
        timeZone
    ).date,
    maxDate: LocalDate = events.maxByOrNull(Event::end)?.end?.date
        ?: now.toLocalDateTime(timeZone).date,
    minTime: LocalTime = LOCAL_TIME_MIN,
    maxTime: LocalTime = LOCAL_TIME_MAX,
    daySize: ScheduleSize = ScheduleSize.FixedSize(256.dp),
    hourSize: ScheduleSize = ScheduleSize.FixedSize(64.dp),
) {
    val numDays = minDate.atStartOfDayIn(timeZone)
        .until(maxDate.atStartOfDayIn(timeZone), DateTimeUnit.DAY, timeZone).toInt() + 1
    val numMinutes =
        minTime.toInstant(timeZone).until(maxTime.toInstant(timeZone), DateTimeUnit.MINUTE)
            .toInt() + 1
    val numHours = numMinutes.toFloat() / 60f
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    var sidebarWidth by remember { mutableStateOf(0) }
    var headerHeight by remember { mutableStateOf(0) }
    BoxWithConstraints(modifier = modifier) {
        val dayWidth: Dp = when (daySize) {
            is ScheduleSize.FixedSize -> daySize.size
            is ScheduleSize.FixedCount -> with(LocalDensity.current) { ((constraints.maxWidth - sidebarWidth) / daySize.count).toDp() }
            is ScheduleSize.Adaptive -> with(LocalDensity.current) {
                maxOf(
                    ((constraints.maxWidth - sidebarWidth) / numDays).toDp(),
                    daySize.minSize
                )
            }
        }
        val hourHeight: Dp = when (hourSize) {
            is ScheduleSize.FixedSize -> hourSize.size
            is ScheduleSize.FixedCount -> with(LocalDensity.current) { ((constraints.maxHeight - headerHeight) / hourSize.count).toDp() }
            is ScheduleSize.Adaptive -> with(LocalDensity.current) {
                maxOf(
                    ((constraints.maxHeight - headerHeight) / numHours).toDp(),
                    hourSize.minSize
                )
            }
        }
        Column(modifier = modifier) {
            ScheduleHeader(
                minDate = minDate,
                maxDate = maxDate,
                dayWidth = dayWidth,
                dayHeader = dayHeader,
                modifier = Modifier
                    .padding(start = with(LocalDensity.current) { sidebarWidth.toDp() })
                    .horizontalScroll(horizontalScrollState)
                    .onGloballyPositioned { headerHeight = it.size.height }
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Start)
            ) {
                ScheduleSidebar(
                    hourHeight = hourHeight,
                    minTime = minTime,
                    maxTime = maxTime,
                    label = timeLabel,
                    modifier = Modifier
                        .verticalScroll(verticalScrollState)
                        .onGloballyPositioned { sidebarWidth = it.size.width }
                )
                BasicSchedule(
                    events = events,
                    eventContent = eventContent,
                    minDate = minDate,
                    maxDate = maxDate,
                    minTime = minTime,
                    maxTime = maxTime,
                    dayWidth = dayWidth,
                    hourHeight = hourHeight,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState)
                )
            }
        }
    }
}

@Composable
fun BasicSchedule(
    events: List<Event>,
    modifier: Modifier = Modifier,
    eventContent: @Composable (positionedEvent: PositionedEvent) -> Unit = {
        BasicEvent(
            positionedEvent = it
        )
    },
    minDate: LocalDate = events.minByOrNull(Event::start)?.start?.date ?: today,
    maxDate: LocalDate = events.maxByOrNull(Event::end)?.end?.date ?: today,
    minTime: LocalTime = LOCAL_TIME_MIN,
    maxTime: LocalTime = LOCAL_TIME_MAX,
    dayWidth: Dp,
    hourHeight: Dp,
) {
    val numDays = minDate.atStartOfDayIn(timeZone)
        .until(maxDate.atStartOfDayIn(timeZone), DateTimeUnit.DAY, timeZone).toInt() + 1
    val numMinutes =
        minTime.toInstant(timeZone).until(maxTime.toInstant(timeZone), DateTimeUnit.MINUTE)
            .toInt() + 1
    val numHours = numMinutes / 60
    val dividerColor = if (MaterialTheme.colors.isLight) Color.LightGray else Color.DarkGray
    val positionedEvents =
        remember(events) { arrangeEvents(splitEvents(events.sortedBy(Event::start))).filter { it.end > minTime && it.start < maxTime } }
    Layout(
        content = {
            positionedEvents.forEach { positionedEvent ->
                Box(modifier = Modifier.eventData(positionedEvent)) {
                    eventContent(positionedEvent)
                }
            }
        },
        modifier = modifier
            .drawBehind {
                val firstHour = LocalTime(minTime.hour, 0, 0)
                val firstHourOffsetMinutes =
                    if (firstHour == minTime) 0
                    else minTime
                        .toInstant(timeZone)
                        .until(
                            firstHour.toInstant(timeZone) + 1.hours,
                            DateTimeUnit.MINUTE
                        )
                val firstHourOffset = (firstHourOffsetMinutes / 60f) * hourHeight.toPx()
                repeat(numHours) {
                    drawLine(
                        dividerColor,
                        start = Offset(0f, it * hourHeight.toPx() + firstHourOffset),
                        end = Offset(size.width, it * hourHeight.toPx() + firstHourOffset),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                repeat(numDays - 1) {
                    drawLine(
                        dividerColor,
                        start = Offset((it + 1) * dayWidth.toPx(), 0f),
                        end = Offset((it + 1) * dayWidth.toPx(), size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
    ) { measureables, constraints ->
        val height = (hourHeight.toPx() * (numMinutes / 60f)).roundToInt()
        val width = dayWidth.roundToPx() * numDays
        val placeablesWithEvents = measureables.map { measurable ->
            val splitEvent = measurable.parentData as PositionedEvent
            val eventDurationMinutes = splitEvent
                .start
                .toInstant(timeZone)
                .until(
                    minOf(splitEvent.end, maxTime).toInstant(timeZone),
                    DateTimeUnit.MINUTE
                )
            val eventHeight = ((eventDurationMinutes / 60f) * hourHeight.toPx()).roundToInt()
            val eventWidth =
                ((splitEvent.colSpan.toFloat() / splitEvent.colTotal.toFloat()) * dayWidth.toPx()).roundToInt()
            val placeable = measurable.measure(
                constraints.copy(
                    minWidth = eventWidth,
                    maxWidth = eventWidth,
                    minHeight = eventHeight,
                    maxHeight = eventHeight
                )
            )
            Pair(placeable, splitEvent)
        }
        layout(width, height) {
            placeablesWithEvents.forEach { (placeable, splitEvent) ->
                val eventOffsetMinutes =
                    if (splitEvent.start > minTime) minTime
                        .toInstant(timeZone)
                        .until(
                            splitEvent.start.toInstant(timeZone),
                            DateTimeUnit.MINUTE
                        )
                    else 0
                val eventY = ((eventOffsetMinutes / 60f) * hourHeight.toPx()).roundToInt()
                val eventOffsetDays = minDate
                    .atStartOfDayIn(timeZone)
                    .until(
                        splitEvent.date.atStartOfDayIn(timeZone),
                        DateTimeUnit.DAY,
                        timeZone
                    ).toInt()
                val eventX =
                    eventOffsetDays * dayWidth.roundToPx() + (splitEvent.col * (dayWidth.toPx() / splitEvent.colTotal.toFloat())).roundToInt()
                placeable.place(eventX, eventY)
            }
        }
    }
}
