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
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger


const val lectureFunKey = "Увлекательность подачи материала";
const val lectureCompKey = "Доступность изложения"

const val semProblemsKey = "Помогают научиться решать задачи";
const val semCompKey = "Объяснения преподавателя понятны";
const val semQuestKey = "Преподаватель готов отвечать на вопросы и давать дополнительные разъяснения";

const val labCompKey = "Помогают лучше понять изучаемый курс";
const val labIndividualKey = "Преподаватель работает с Вами  индивидуально при сдаче";
const val labReportKey = "Преподаватель разумно требователен к оформлению отчета";

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
        val labComment: String?
) : Serializable

object SurveyData {
    val entries: MutableSet<SurveyEntry> = TreeSet(compareBy { it.date })
    var lastUpdated: LocalDate = LocalDate.MIN

    /**
     * Update data from server
     */
    fun update() {
        synchronized(this) {
            entries.addAll(Connection.load())
        }
    }

    /**
     * Load data from file. Return true if read is successful
     */
    @Synchronized
    fun load(): Boolean {
        val file = File(STORE_FILE_NAME)
        try {
            ObjectInputStream(file.inputStream()).use {
                lastUpdated = it.readObject() as LocalDate
                entries.addAll(it.readObject() as Collection<SurveyEntry>)
                return true
            }
        } catch (ex: Exception) {
            Logger.getLogger("SurveyData").log(Level.WARNING, "Failed to laod data from file", ex)
            return false
        }
    }

    @Synchronized
    fun save() {
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

private object Connection {
    /** Application name.  */
    private const val APPLICATION_NAME = "Survey client"

    /** Directory to store user credentials for this application.  */
    private val DATA_STORE_DIR = File("credentials.dat")

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
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build()
        val credential = AuthorizationCodeInstalledApp(
                flow, LocalServerReceiver()).authorize("user")
        logger.log(Level.INFO, "Credentials saved to " + DATA_STORE_DIR.absolutePath)
        return credential
    }

    fun load(): List<SurveyEntry> {
        // Build a new authorized API client service.
        val service = sheetsService

        // Prints the names and majors of students in a sample spreadsheet:
        // https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
        val spreadsheetId = "1ztF9KdELyv333trk5raI2javne8HpEnQMbQtEnw8pno"
        val range = "Form Responses 1!A2:P"
        val response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute()
        val values = response.getValues()

        return if (values == null || values.size == 0) {
            logger.log(Level.INFO, "No data found on server")
            emptyList()
        } else {
            logger.log(Level.INFO, "Found ${values.size} entries on server")
            values.map {
                SurveyEntry(
                        date = it[0] as LocalDate,
                        group = it[1] as String?,
                        lecturerName = (it[2] as String?) ?: DEFAULT_NAME,
                        lectureComprehend = it[3] as Byte,
                        lectureFun = it[4] as Byte,
                        lectureComment = it[5] as String?,
                        seminarName = (it[6] as String?) ?: DEFAULT_NAME,
                        seminarProblems = it[7] as Byte,
                        seminarComprehend = it[8] as Byte,
                        seminarQuest = it[9] as Byte,
                        seminarComment = it[10] as String?,
                        labName = (it[11] as String?) ?: DEFAULT_NAME,
                        labComprehend = it[12] as Byte,
                        labIndividual = it[13] as Byte,
                        labReport = it[14] as Byte,
                        labComment = it[15] as String?
                )
            }
        }

    }


}