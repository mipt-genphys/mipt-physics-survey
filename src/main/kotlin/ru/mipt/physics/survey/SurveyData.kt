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
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.Serializable
import java.util.Arrays
import kotlin.collections.ArrayList
import kotlin.collections.List


//    private val lectureFunKey = "Увлекательность подачи материала";
//    private val lectureCompKey = "Доступность изложения"
//
//    private val semProblemsKey = "Помогают научиться решать задачи";
//    private val semCompKey = "Объяснения преподавателя понятны";
//    private val semQuestKey = "Преподаватель готов отвечать на вопросы и давать дополнительные разъяснения";
//
//    private val labCompKey = "Помогают лучше понять изучаемый курс";
//    private val labIndividualKey = "Преподаватель работает с Вами  индивидуально при сдаче";
//    private val labReportKey = "Преподаватель разумно требователен к оформлению отчета";


data class SurveyEntry(
        val group: String,
        val lectorName: String,
        val lectureFun: Byte,
        val lectureComperhand: Byte,
        val seminarName: String,
        val seminarComperhand: Byte,
        val seminarQuest: Byte,
        val labName: String,
        val labComperhand: Byte,
        val labIndividual: Byte,
        val labReport: Byte
) : Serializable

object SurveyData : Serializable {
    val entries: List<SurveyEntry> = ArrayList()
    //val lastUpdated: LocalDate

    /**
     * Update data from server
     */
    fun update(){

    }


}

object Connection {
    /** Application name.  */
    private val APPLICATION_NAME = "Survey client"

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
    val sheetsService: Sheets by lazy {
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
    fun authorize(): Credential {
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
        println("Credentials saved to " + DATA_STORE_DIR.absolutePath)
        return credential
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // Build a new authorized API client service.
        val service = sheetsService

        // Prints the names and majors of students in a sample spreadsheet:
        // https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
        val spreadsheetId = "1ztF9KdELyv333trk5raI2javne8HpEnQMbQtEnw8pno"
        val range = "Class Data!A2:E"
        val response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute()
        val values = response.getValues()
        if (values == null || values.size == 0) {
            println("No data found.")
        } else {
            println("Name, Major")
            for (row in values) {
                // Print columns A and E, which correspond to indices 0 and 4.
                System.out.printf("%s, %s\n", row[0], row[4])
            }
        }
    }


}