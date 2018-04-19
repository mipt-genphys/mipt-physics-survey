package ru.mipt.physics.survey

import com.sun.rowset.internal.Row
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


val DEFAULT_NAME = "#Не указано"

/**
 * A report on specific prep
 * Created by darksnake on 15-May-16.
 */
class PrepReport(val name: String, val minDate: LocalDate = LocalDate.MIN, val maxDate: LocalDate = LocalDate.MAX) {
    class Summary() {
        /**
         * all comments
         */
        val comments = TreeSet<Pair<LocalDate, String>>(Comparator { first, second -> -first.first.compareTo(second.first) });
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
        val ratings = HashMap<String, Int>();
        /**
         * ratings in range
         */
        val rangeRatings = HashMap<String, Int>();

        fun getComments(): List<Pair<LocalDate, String>>{
            return comments.toList();
        }

        fun addRating(time: LocalDate, rating: Map<String, Int>, comment: String? = null, inRange: Boolean = false) {
            entries++;
            rating.forEach<String, Int> { entry ->
                if (ratings.containsKey(entry.key)) {
                    ratings.put(entry.key, ratings.get(entry.key)?.plus(entry.value)!!);
                } else {
                    ratings.put(entry.key, entry.value);
                }
            }
            if (inRange) {
                rangeEntries++;
                rating.forEach<String, Int> { entry ->
                    if (rangeRatings.containsKey(entry.key)) {
                        rangeRatings.put(entry.key, rangeRatings.get(entry.key)?.plus(entry.value)!!);
                    } else {
                        rangeRatings.put(entry.key, entry.value);
                    }
                }
            }


            if (comment != null && !comment.isBlank()) {
                comments.add(Pair(time, comment));
            }
        }

        fun getRatingKeys(): Collection<String> {
            return ratings.keys
        }

        fun getRating(key: String): Double {
            if (entries > 0) {
                return ratings.getOrDefault(key, 0).toDouble() / entries;
            } else {
                return 0.0;
            }
        }

        fun getRangeRating(key: String): Double {
            return if (rangeEntries > 0) {
                rangeRatings.getOrDefault(key, 0).toDouble() / rangeEntries;
            } else {
                0.0;
            }
        }
    }

    val lecturesSummary = Summary();
    val seminarsSummary = Summary();
    val labSummary = Summary();

    fun hasRange(): Boolean {
        return minDate != LocalDate.MIN || maxDate != LocalDate.MAX;
    }

    fun isInRange(date: LocalDate): Boolean {
        return hasRange() && date.isBefore(maxDate) && date.isAfter(minDate);
    }

    fun addLectureRating(time: LocalDate, rating: Map<String, Int>, comment: String? = "") {
        lecturesSummary.addRating(time, rating, comment, isInRange(time));

    }

    fun addSeminarRating(time: LocalDate, rating: Map<String, Int>, comment: String? = "") {
        seminarsSummary.addRating(time, rating, comment, isInRange(time));
    }

    fun addLabRating(time: LocalDate, rating: Map<String, Int>, comment: String? = "") {
        labSummary.addRating(time, rating, comment, isInRange(time));
    }

}

/**
 * Extension of excell cell to produce raw string
 */
fun Cell.getRawStringValue(): String {
    return if (this.cellType == Cell.CELL_TYPE_NUMERIC) {
        numericCellValue.toString();
    } else {
        stringCellValue;
    }
}

/**
 * Fill preps from excell table and return a map
 */
fun fillPreps(book: Workbook, from: LocalDate = LocalDate.MIN, to: LocalDate = LocalDate.MAX): Map<String, PrepReport> {
    val sheet = book.getSheetAt(0);
    val prepMap = HashMap<String, PrepReport>();
    fun getPrep(prepName: String): PrepReport {
        return prepMap.getOrPut(prepName) { -> PrepReport(prepName, from, to) }
    }

    val titleRow = sheet.first();
    for (r: Row in sheet.drop(1)) {
        val time = LocalDate.from(LocalDateTime.ofInstant(r.getCell(0).dateCellValue.toInstant(), ZoneId.systemDefault()));
//        val groupNum = r.getCell(1)?.numericCellValue ?: 0;

        val lectorName = r.getCell(2)?.stringCellValue ?: DEFAULT_NAME;
        val lectureRatings = HashMap<String, Int>(2);
        for (i in 3..4) {
            lectureRatings.put(titleRow.getCell(i).stringCellValue, r.getCell(i).numericCellValue.toInt());
        }
        val lectureComment = r.getCell(5)?.getRawStringValue();


        val semName = r.getCell(6)?.stringCellValue ?: DEFAULT_NAME;
        val semRatings = HashMap<String, Int>(3);
        for (i in 7..9) {
            semRatings.put(titleRow.getCell(i).stringCellValue, r.getCell(i).numericCellValue.toInt());
        }
        val semComment = r.getCell(10)?.getRawStringValue();


        val labName = r.getCell(11)?.stringCellValue ?: DEFAULT_NAME;
        val labRatings = HashMap<String, Int>(3);
        for (i in 12..14) {
            labRatings.put(titleRow.getCell(i).stringCellValue, r.getCell(i).numericCellValue.toInt());
        }
        val labComment = r.getCell(15)?.getRawStringValue();

        getPrep(lectorName).addLectureRating(time, lectureRatings, lectureComment);


        getPrep(semName).addSeminarRating(time, semRatings, semComment);


        getPrep(labName).addLabRating(time, labRatings, labComment);
    }
    return prepMap;
}