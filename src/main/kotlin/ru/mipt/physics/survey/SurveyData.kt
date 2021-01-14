package ru.mipt.physics.survey

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import java.io.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


const val lectureFunKey = "Увлекательность подачи материала"
const val lectureCompKey = "Доступность изложения"

const val semProblemsKey = "Помогают научиться решать задачи"
const val semCompKey = "Объяснения преподавателя понятны"
const val semQuestKey = "Преподаватель готов отвечать на вопросы и давать дополнительные разъяснения"

const val labCompKey = "Помогают лучше понять изучаемый курс"
const val labIndividualKey = "Преподаватель работает с Вами  индивидуально при сдаче"
const val labReportKey = "Преподаватель разумно требователен к оформлению отчета"

const val STORE_FILE_NAME = "surveyData.dat"

const val DEFAULT_NAME = "#Не указано"

private val logger = Logger.getLogger("SurveyData")

data class SurveyEntry(
    val date: LocalDate,
    val group: String?,

    val lecturerName: String,
    val lectureFun: Byte,
    val lectureComprehend: Byte,
    val lectureComment: String?,

    val seminarName: String,
    val seminarProblems: Byte,
    val seminarComprehend: Byte,
    val seminarQuest: Byte,
    val seminarComment: String?,

    val labName: String,
    val labComprehend: Byte,
    val labIndividual: Byte,
    val labReport: Byte,
    val labComment: String?,
) : Serializable

interface UpdateCallback {
    fun notifyUpdateMessage(message: String)
    fun notifyUpdateInProgress(inProgress: Boolean)
}

object SurveyData {
    val entries: MutableList<SurveyEntry> = ArrayList()
    var lastUpdated: LocalDate = LocalDate.MIN

    /**
     * Update data from server
     */
    fun update(callback: UpdateCallback) {
        synchronized(this) {
            callback.notifyUpdateInProgress(true)
            try {
                callback.notifyUpdateMessage("Загрузка данных с сервера")
                val newEntries = Connection.download(entries.size)
                entries.addAll(newEntries)
                callback.notifyUpdateMessage("Загружено ${newEntries.size} новых записи с сервера")
                if (newEntries.isNotEmpty()) {
                    save()
                }
            } finally {
                callback.notifyUpdateInProgress(false)
            }
        }
    }

    /**
     * Load data from file. Return true if read is successful
     */
    fun load(callback: UpdateCallback): Boolean {
        synchronized(this) {
            val file = File(STORE_FILE_NAME)
            if (file.exists()) {
                callback.notifyUpdateInProgress(true)
                try {
                    callback.notifyUpdateMessage("Загрузка данных из локального файла")
                    ObjectInputStream(file.inputStream()).use {
                        lastUpdated = it.readObject() as LocalDate
                        @Suppress("UNCHECKED_CAST")
                        entries.addAll(it.readObject() as Collection<SurveyEntry>)
                        callback.notifyUpdateMessage("Загружено ${entries.size} записи из файла")
                        return true
                    }
                } catch (ex: Exception) {
                    Logger.getLogger("SurveyData").log(Level.INFO, "Failed to load data from file", ex)
                    return false
                } finally {
                    callback.notifyUpdateInProgress(false)
                }
            } else {
                callback.notifyUpdateMessage("Локальная копия данных не найдена")
                return false
            }
        }
    }

    @Synchronized
    private fun save() {
        synchronized(this) {
            if (!entries.isEmpty()) {
                val file = File(STORE_FILE_NAME)
                try {
                    ObjectOutputStream(file.outputStream()).use {
                        it.writeObject(lastUpdated)
                        it.writeObject(entries)
                    }
                } catch (ex: Exception) {
                    logger.log(Level.WARNING, "Failed to save data from file", ex)
                }
            } else {
                logger.log(Level.INFO, "Nothing to save")
            }
        }
    }
}

private object Connection {
    /** Application name.  */
    private const val APPLICATION_NAME = "Survey client"

    /** Directory to store user credentials for this application.  */
    private val DATA_STORE_DIR = File(".credentials")

    /** Global instance of the [FileDataStoreFactory].  */
    private val DATA_STORE_FACTORY: FileDataStoreFactory = FileDataStoreFactory(DATA_STORE_DIR)

    /** Global instance of the JSON factory.  */
    private val JSON_FACTORY = JacksonFactory.getDefaultInstance()

    /** Global instance of the HTTP transport.  */
    private val HTTP_TRANSPORT: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/sheets.googleapis.com-java-quickstart
     */
    private val SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY)

    /**
     * Build and return an authorized Sheets API client service.
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    private val sheetsService: Sheets by lazy {
        val credential = authorize()
        return@lazy Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun authorize(): Credential {
        // Load client secrets.
        val input = Connection::class.java.getResourceAsStream("/client_secret.json")
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(input))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES
        ).setDataStoreFactory(DATA_STORE_FACTORY)
            .setAccessType("offline")
            .build()
        val credential = AuthorizationCodeInstalledApp(
            flow, LocalServerReceiver()
        ).authorize("user")
        logger.log(Level.INFO, "Credentials saved to " + DATA_STORE_DIR.absolutePath)
        return credential
    }

    //6/11/2015 2:14:12
    private val dateFormat = DateTimeFormatter.ofPattern("M'/'d'/'yyyy H':'mm':'ss")

    fun download(startIndex: Int = 0): List<SurveyEntry> {
        // Build a new authorized API client service.
        val service = sheetsService

        // Prints the names and majors of students in a sample spreadsheet:
        // https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
        val spreadsheetId = "1ztF9KdELyv333trk5raI2javne8HpEnQMbQtEnw8pno"
        val range = "Form Responses 1!A${startIndex + 2}:P"
        val response = service.spreadsheets().values()
            .get(spreadsheetId, range)
            .execute()
        val values = response.getValues()

        return if (values == null || values.size == 0) {
            logger.log(Level.INFO, "No data found on server")
            emptyList()
        } else {
            logger.log(Level.INFO, "Found ${values.size} entries on server")
            values.mapNotNull {
                try {
                    SurveyEntry(
                        date = LocalDate.parse(it[0].toString(), dateFormat),
                        group = it[1] as String?,
                        lecturerName = (it.getOrNull(2) as String?) ?: DEFAULT_NAME,
                        lectureComprehend = it[3].toString().toByte(),
                        lectureFun = it[4].toString().toByte(),
                        lectureComment = it.getOrNull(5) as String?,
                        seminarName = (it.getOrNull(6) as String?) ?: DEFAULT_NAME,
                        seminarProblems = it[7].toString().toByte(),
                        seminarComprehend = it[8].toString().toByte(),
                        seminarQuest = it[9].toString().toByte(),
                        seminarComment = it.getOrNull(10) as String?,
                        labName = (it.getOrNull(11) as String?) ?: DEFAULT_NAME,
                        labComprehend = it[12].toString().toByte(),
                        labIndividual = it[13].toString().toByte(),
                        labReport = it[14].toString().toByte(),
                        labComment = it.getOrNull(15) as String?
                    )
                } catch (ex: Exception) {
                    logger.log(Level.SEVERE, "Failed to parse entry: $it")
                    null
                }
            }
        }
    }
}

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