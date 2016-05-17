package ru.mipt.physics.survey

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * Created by darksnake on 15-May-16.
 */
class PrepReport(val name: String) {

    class Summary() {
        val comments = ArrayList<Pair<LocalDate, String>>();
        var entries = 0;
            private set;
        val ratings = HashMap<String, Int>();

        fun addRating(time: LocalDate, rating: Map<String, Int>, comment: String? = null) {
            entries++;
            rating.forEach<String, Int> { entry ->
                if (ratings.containsKey(entry.key)) {
                    ratings.put(entry.key, ratings.get(entry.key)?.plus(entry.value)!!);
                } else {
                    ratings.put(entry.key, entry.value);
                }
            }
            if (comment != null && !comment.isBlank()) {
                comments.add(Pair(time, comment));
            }
        }

        fun getRatingKeys() : Collection<String>{
            return ratings.keys
        }

        fun getRating(key:String): Double{
            return ratings.getOrDefault(key,0).toDouble()/entries;
        }
    }

    val lecturesSummary = Summary();
    val seminarsSummary = Summary();
    val labSummary = Summary();

    fun addLectureRating(time: LocalDate, rating: Map<String, Int>, comment: String? = "") {
        lecturesSummary.addRating(time, rating, comment);
    }

    fun addSeminarRating(time: LocalDate, rating: Map<String, Int>, comment: String? = "") {
        seminarsSummary.addRating(time, rating, comment);
    }

    fun addLabRating(time: LocalDate, rating: Map<String, Int>, comment: String? = "") {
        labSummary.addRating(time, rating, comment);
    }

}

fun Cell.getRawStringValue(): String{
    if(this.cellType== Cell.CELL_TYPE_NUMERIC){
        return numericCellValue.toString();
    } else {
        return stringCellValue;
    }
}

fun fillPreps(book: Workbook, from: LocalDate = LocalDate.MIN, to: LocalDate = LocalDate.now()): Map<String, PrepReport> {
    val sheet = book.getSheetAt(0);
    val prepMap = HashMap<String, PrepReport>();
    fun getPrep(prepName: String): PrepReport {
        return prepMap.getOrPut(prepName) { -> PrepReport(prepName) }
    }

    val titleRow = sheet.first();
    for (r: Row in sheet.drop(1)) {
        val time = LocalDate.from(LocalDateTime.ofInstant(r.getCell(0).dateCellValue.toInstant(), ZoneId.systemDefault()));
        val groupNum = r.getCell(1)?.numericCellValue ?: 0;

        val lectorName = r.getCell(2)?.stringCellValue ?: "default";
        val lectureRatings = HashMap<String, Int>(2);
        for (i in 3..4) {
            lectureRatings.put(titleRow.getCell(i).stringCellValue, r.getCell(i).numericCellValue.toInt());
        }
        val lectureComment = r.getCell(5)?.getRawStringValue();


        val semName = r.getCell(6)?.stringCellValue ?: "default";
        val semRatings = HashMap<String, Int>(3);
        for (i in 7..9) {
            semRatings.put(titleRow.getCell(i).stringCellValue, r.getCell(i).numericCellValue.toInt());
        }
        val semComment = r.getCell(10)?.getRawStringValue();


        val labName = r.getCell(11)?.stringCellValue ?: "default";
        val labRatings = HashMap<String, Int>(3);
        for (i in 12..14) {
            labRatings.put(titleRow.getCell(i).stringCellValue, r.getCell(i).numericCellValue.toInt());
        }
        val labComment = r.getCell(15)?.getRawStringValue();

        if (time.isAfter(from) && time.isBefore(to)) {
            getPrep(lectorName).addLectureRating(time, lectureRatings, lectureComment);


            getPrep(semName).addSeminarRating(time, semRatings, semComment);


            getPrep(labName).addLabRating(time, labRatings, labComment);

        }
    }
    return prepMap;
}