package ru.mipt.physics.survey

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.scene.control.Button
import javafx.scene.control.DatePicker
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane
import javafx.scene.web.WebView
import javafx.stage.FileChooser
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import tornadofx.View
import java.io.File
import java.io.StringWriter
import java.nio.charset.Charset
import java.time.LocalDate
import java.util.*

/**
 * Created by darksnake on 15-May-16.
 */
class ReportView : View() {
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
    private val defaultDataFile = "C:\\Users\\darksnake\\Dropbox\\MIPT\\connect\\responses-current.xlsx";

    override val root: BorderPane by fxml("/fxml/ReportView.fxml");
    val prepList: ListView<String> by fxid();
    val resultBox: WebView by fxid();
    val fileName: Label by fxid();

    val fromField: DatePicker by fxid()
    val toField: DatePicker by fxid()

    val loadDataButton: Button by fxid();
    val prepReportButton: Button by fxid();
    val fullReportButton: Button by fxid();

    private val inputFilePropery = SimpleObjectProperty<File>();
    private val prepMap = FXCollections.observableHashMap<String, PrepReport>();

    init {
        inputFilePropery.addListener { observableValue, oldValue, newValue -> buildList(); }
        inputFilePropery.set(File(defaultDataFile));

        fromField.valueProperty().addListener(ChangeListener { observableValue, oldValue, newValue -> buildList() })
        toField.valueProperty().addListener(ChangeListener { observableValue, oldValue, newValue -> buildList() })
        loadDataButton.setOnAction { event ->
            val fileChooser = FileChooser();
            fileChooser.title = "Открыть файл данных";
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("xlsx", "*.xlsx"));
            val inputFile = fileChooser.showOpenDialog(primaryStage);
            if (inputFile != null) {
                inputFilePropery.set(inputFile);
            }
        };
        prepReportButton.setOnAction { event ->
            val currentPrepName = prepList.selectionModel.selectedItem;
            if (currentPrepName != null) {
                val fileChooser = FileChooser();
                fileChooser.title = "Сохранить отчет";
                fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("html", "*.html"));
                fileChooser.initialFileName = currentPrepName + ".html";
                fileChooser.showSaveDialog(primaryStage)?.writeText(buildPrepReport(prepMap.get(currentPrepName)!!, false))
            }
        };
    }


    fun getBook(): Workbook {
        return WorkbookFactory.create(inputFilePropery.get());
    }

    fun getPrepMap(): Map<String, PrepReport> {
        return fillPreps(getBook(), fromField.value ?: LocalDate.MIN, toField.value ?: LocalDate.now());
    }

    fun buildPrepReport(report: PrepReport, embed: Boolean): String {
        val dataMap = HashMap<String, Any>();
        dataMap.put("prep", report);
//        dataMap.put("resourceDir", this.javaClass.getResource("/assets").toString());
        dataMap.put("embed", embed)
        val template = cfg.getTemplate("PrepReport.ftl");
        val out = StringWriter();
        template.process(dataMap, out);
        return out.toString();
    }

    fun showPrep(report: PrepReport) {
        resultBox.engine.loadContent(buildPrepReport(report, true));
    }


    fun buildList() {
        if (inputFilePropery.isNotNull.get()) {
            fileName.text = inputFilePropery.get()?.path;
            prepList.items.clear();
            prepList.selectionModel.clearSelection()
            prepMap.clear();
            prepMap.putAll(getPrepMap());
            prepList.items.addAll(prepMap.keys.sorted())
            prepList.selectionModel.selectedItemProperty().addListener { observableValue, oldValue, newValue ->
                if (newValue != null) {
                    showPrep(prepMap[newValue]!!)
                }
            }
        }
    }
}

