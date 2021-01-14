package ru.mipt.physics.survey

import java.time.LocalDate
import java.util.*
import kotlin.collections.HashMap

/**
 * A report on specific prep
 * Created by darksnake on 15-May-16.
 */
class PrepReport(val name: String, val minDate: LocalDate = LocalDate.MIN, val maxDate: LocalDate = LocalDate.MAX) {
    class Summary {
        /**
         * all comments
         */
        val comments = TreeSet(
            Comparator.comparing<Pair<LocalDate, String>, LocalDate> { it.first }.reversed()
                    then
                    Comparator.comparing { it.second }
        )

        /**
         * total number of entries
         */
        var entries = 0
            private set

        /**
         * A number of entries in time range
         */
        var rangeEntries = 0
            private set

        /**
         * ratings map
         */
        val ratings = HashMap<String, Int>()

        /**
         * ratings in range
         */
        val rangeRatings = HashMap<String, Int>()

        fun addRating(time: LocalDate, rating: Map<String, Byte>, comment: String? = null, inRange: Boolean = false) {
            entries++
            rating.forEach { entry ->
                ratings[entry.key] = (ratings[entry.key] ?: 0) + entry.value
            }
            if (inRange) {
                rangeEntries++
                rating.forEach { entry ->
                    rangeRatings[entry.key] = (rangeRatings[entry.key] ?: 0) + entry.value
                }
            }

            if (comment != null && comment.isNotBlank()) {
                comments.add(Pair(time, comment))
            }
        }

        fun getRatingKeys(): Collection<String> {
            return ratings.keys
        }

        fun getRating(key: String): Double {
            return if (entries > 0) {
                ratings.getOrDefault(key, 0).toDouble() / entries
            } else {
                0.0
            }
        }

        fun getRangeRating(key: String): Double {
            return if (rangeEntries > 0) {
                rangeRatings.getOrDefault(key, 0).toDouble() / rangeEntries
            } else {
                0.0
            }
        }
    }

    val lecturesSummary = Summary()
    val seminarsSummary = Summary()
    val labSummary = Summary()

    fun hasRange(): Boolean {
        return minDate != LocalDate.MIN || maxDate != LocalDate.MAX
    }

    fun isInRange(date: LocalDate): Boolean {
        return hasRange() && date.isBefore(maxDate) && date.isAfter(minDate)
    }

    fun addLectureRating(time: LocalDate, rating: Map<String, Byte>, comment: String? = "") {
        lecturesSummary.addRating(time, rating, comment, isInRange(time))
    }

    fun addSeminarRating(time: LocalDate, rating: Map<String, Byte>, comment: String? = "") {
        seminarsSummary.addRating(time, rating, comment, isInRange(time))
    }

    fun addLabRating(time: LocalDate, rating: Map<String, Byte>, comment: String? = "") {
        labSummary.addRating(time, rating, comment, isInRange(time))
    }

}