package ru.mipt.physics.survey

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.geometry.Side
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import javafx.stage.FileChooser
import tornadofx.*
import java.io.StringWriter
import java.time.LocalDate
import java.util.*

/**
 * Created by darksnake on 15-May-16.
 */
class ReportView : View("Генератор отчетов", ImageView(icon)), UpdateCallback {

    private val prepMap = FXCollections.observableHashMap<String, PrepReport>();

    private val fromDateProperty = SimpleObjectProperty<LocalDate>()
    private val toDateProperty = SimpleObjectProperty<LocalDate>()
    private lateinit var summaryWebView: WebView
    private lateinit var prepList: ListView<String>
    private lateinit var prepWebView: WebView

    private lateinit var summaryTab: Tab
    private lateinit var prepsTab: Tab
    private lateinit var progressBar: ProgressIndicator
    private lateinit var messageLabel: Label

    override val root = borderpane {
        top {
            toolbar {
                prefHeight = 40.0
                button("Обновить") {
                    action {
                        runAsync(daemon = true) {
                            SurveyData.update(this@ReportView)
                            update()
                        }
                    }
                }
                progressBar = progressindicator {
                    prefHeight = 30.0
                    progress = -1.0
                    isVisible = false
                }
                separator(Orientation.VERTICAL)
                messageLabel = label()
                pane { hgrow = Priority.ALWAYS }
                separator(Orientation.VERTICAL)
                button("Экспорт") {
                    action {
                        export()
                    }
                }
                //button("Информация") { }
            }
        }
        center {
            tabpane {
                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                side = Side.LEFT
                summaryTab = tab("Общий отчет") {
                    borderpane {
                        top {
                            toolbar {
                                prefHeight = 40.0
                                label("С") {
                                    insets(left = 10, right = 5)
                                }
                                datepicker(fromDateProperty)
                                label("По") {
                                    insets(left = 10, right = 5)
                                }
                                datepicker(toDateProperty)
                                separator(Orientation.VERTICAL)
                            }
                        }
                        center {
                            summaryWebView = webview()
                        }

                    }
                }
                prepsTab = tab("Преподаватели") {
                    splitpane {
                        setDividerPosition(0, 0.2)
                        prepList = listview {
                            prefWidth = 200.0
                        }
                        prepWebView = webview()
                    }
                }
            }
        }
    }


    init {
        fromDateProperty.onChange { showSummary() }
        toDateProperty.onChange { showSummary() }
        fromDateProperty.value = getSemesterStart()

        runAsync(daemon = true) {
            SurveyData.load(this@ReportView)
            update()
        }
    }

    override fun notifyUpdateMessage(message: String) {
        runLater {
            messageLabel.text = message
        }
    }

    override fun notifyUpdateInProgress(inProgress: Boolean) {
        runLater {
            progressBar.isVisible = inProgress
        }
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

    /**
     * Fill preps from data store table and return a map
     */
    fun fillPreps(from: LocalDate = LocalDate.MIN, to: LocalDate = LocalDate.MAX) {
        prepMap.clear()

        fun getPrep(prepName: String): PrepReport {
            return prepMap.getOrPut(prepName) { -> PrepReport(prepName, from, to) }
        }

        SurveyData.entries.forEach {
            getPrep(it.lecturerName).addLectureRating(
                    it.date,
                    mapOf(
                            lectureCompKey to it.lectureComprehend,
                            lectureFunKey to it.lectureFun
                    ),
                    it.lectureComment
            )
            getPrep(it.seminarName).addSeminarRating(
                    it.date,
                    mapOf(
                            semProblemsKey to it.seminarProblems,
                            semCompKey to it.seminarComprehend,
                            semQuestKey to it.seminarQuest
                    ),
                    it.seminarComment
            )
            getPrep(it.labName).addLabRating(
                    it.date,
                    mapOf(
                            labCompKey to it.labComprehend,
                            labIndividualKey to it.labIndividual,
                            labReportKey to it.labReport
                    ),
                    it.labComment
            )
        }

    }

    private fun update() {
        runLater {
            reset()
            fillPreps()
            prepList.items.addAll(prepMap.keys.sorted())
            prepList.selectionModel.selectedItemProperty().addListener { observableValue, oldValue, newValue ->
                if (newValue != null) {
                    showPrep(prepMap[newValue]!!)
                }
            }
            showSummary()
        }
    }

    private fun reset() {
        prepMap.clear()
        prepList.items.clear()
        summaryWebView.engine.loadContent("");
        prepWebView.engine.loadContent("");
    }

    fun buildSummary(from: LocalDate = LocalDate.MIN, to: LocalDate = LocalDate.MAX, embed: Boolean): String {
        fillPreps(from, to)
        val preps = prepMap.values.toSortedSet(compareBy { it.name });
        if (!preps.isEmpty()) {
            val range = from != LocalDate.MIN || to != LocalDate.MAX;
            val prep = preps.first();
            val dataMap = HashMap<String, Any>();
            dataMap["preps"] = preps;
            dataMap["range"] = range;
            dataMap["lectureRatingKeys"] = prep.lecturesSummary.getRatingKeys().toList()
            dataMap["lectureRatingNum"] = prep.lecturesSummary.getRatingKeys().size
            dataMap["seminarRatingKeys"] = prep.seminarsSummary.getRatingKeys().toList()
            dataMap["seminarRatingNum"] = prep.seminarsSummary.getRatingKeys().size
            dataMap["labRatingKeys"] = prep.labSummary.getRatingKeys().toList()
            dataMap["labRatingNum"] = prep.labSummary.getRatingKeys().size
            dataMap["startDate"] = from
            dataMap["endDate"] = to
            dataMap["embed"] = embed
            val template = cfg.getTemplate("SummaryReport.ftl");
            val out = StringWriter();
            template.process(dataMap, out);
            return out.toString();
        } else {
            return "";
        }
    }

    private fun export() {
        if (prepsTab.isSelected) {
            val currentPrepName = prepList.selectionModel.selectedItem;
            if (currentPrepName != null) {
                val fileChooser = FileChooser();
                fileChooser.title = "Сохранить отчет";
                fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("html", "*.html"));
                fileChooser.initialFileName = currentPrepName + ".html";
                fileChooser.showSaveDialog(primaryStage)?.writeText(buildPrepReport(prepMap.get(currentPrepName)!!, false))
            }
        } else if (summaryTab.isSelected) {
            if (!prepMap.isEmpty()) {
                val fileChooser = FileChooser();
                fileChooser.title = "Сохранить отчет";
                fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("html", "*.html"));
                fileChooser.initialFileName = "summary" + ".html";
                fileChooser.showSaveDialog(primaryStage)?.writeText(
                        buildSummary(
                                fromDateProperty.value ?: LocalDate.MIN,
                                toDateProperty.value ?: LocalDate.now(),
                                false
                        )
                )
            }
        }
    }

    fun showPrep(report: PrepReport) {
        Platform.runLater { -> prepWebView.engine.loadContent(buildPrepReport(report, true)) };
    }

    fun showSummary() {
        Platform.runLater { ->
            summaryWebView.engine.loadContent(
                    buildSummary(
                            fromDateProperty.value ?: LocalDate.MIN,
                            toDateProperty.value ?: LocalDate.MAX,
                            true
                    )
            )
        }
    }

    fun getSemesterStart(): LocalDate {
        val now = LocalDate.now();
        return when {
            now.monthValue < 2 -> now.minusYears(1).withMonth(9).withDayOfMonth(1)
            now.monthValue < 9 -> now.withMonth(2).withDayOfMonth(10)
            else -> now.withMonth(9).withDayOfMonth(1)
        }
    }
}

