package ru.mipt.physics.survey

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.geometry.Side
import javafx.scene.control.Alert
import javafx.scene.control.ListView
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File
import java.io.StringWriter
import java.time.LocalDate
import java.util.*

/**
 * Created by darksnake on 15-May-16.
 */
class ReportView : View() {
    private val prepMap = FXCollections.observableHashMap<String, PrepReport>();

    private val fromDateProperty = SimpleObjectProperty<LocalDate>()
    private val toDateProperty = SimpleObjectProperty<LocalDate>()
    private lateinit var summaryWebView: WebView
    private lateinit var prepList: ListView<String>
    private lateinit var prepWebView: WebView



    override val root = borderpane {
        top {
            toolbar {
                prefHeight = 40.0
                button("Обновить") {  }
                progressbar {  }
                separator(Orientation.VERTICAL)
                pane{hgrow = Priority.ALWAYS}
                separator(Orientation.VERTICAL)
                button ("Экспорт"){  }
                button("Информация") {  }
            }
        }
        center {
            tabpane {
                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                side = Side.LEFT
                tab("Общий отчет") {
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
                            }
                            separator(Orientation.VERTICAL)
                        }
                        center {
                            summaryWebView = webview()
                        }

                    }
                }
                tab("Преподаватели") {
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
        this.title = "Генератор отчетов"
        inputFilePropery.addListener { observableValue, oldValue, newValue -> load(); }
        val defaultFile = File(defaultDataFile);
        if (defaultFile.exists()) {
            inputFilePropery.set(defaultFile);
        }

        fromField.valueProperty().addListener({ observableValue, oldValue, newValue -> showSummary() })
        toField.valueProperty().addListener({ observableValue, oldValue, newValue -> showSummary() })
        loadDataButton.setOnAction { event ->
            val fileChooser = FileChooser();
            fileChooser.title = "Открыть файл данных";
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("xlsx", "*.xlsx"));
            val inputFile = fileChooser.showOpenDialog(primaryStage);
            if (inputFile != null) {
                reset();
                inputFilePropery.set(inputFile);
            }
        };
        fromField.value = getSemesterStart()
    }

    fun buildPrepMap(from: LocalDate = LocalDate.MIN, to: LocalDate = LocalDate.MAX): Map<String, PrepReport> {
        return fillPreps(getBook(), from, to);
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

    fun reset() {
        fileName.text = "";
        prepMap.clear()
        prepList.items.clear()
        summaryBox.engine.loadContent("");
        prepResultBox.engine.loadContent("");
    }

    fun buildSummary(from: LocalDate = LocalDate.MIN, to: LocalDate = LocalDate.MAX, embed: Boolean): String {
        val preps = buildPrepMap(from, to).values.toSortedSet(Comparator { first, second -> first.name.compareTo(second.name) });
        if (!preps.isEmpty()) {
            val range = from != LocalDate.MIN || to != LocalDate.MAX;
            val prep = preps.first();
            val dataMap = HashMap<String, Any>();
            dataMap.put("preps", preps);
            dataMap.put("range", range);
            dataMap.put("lectureRatingKeys", prep.lecturesSummary.getRatingKeys().toList())
            dataMap.put("lectureRatingNum", prep.lecturesSummary.getRatingKeys().size + 1)
            dataMap.put("seminarRatingKeys", prep.seminarsSummary.getRatingKeys().toList())
            dataMap.put("seminarRatingNum", prep.seminarsSummary.getRatingKeys().size + 1)
            dataMap.put("labRatingKeys", prep.labSummary.getRatingKeys().toList())
            dataMap.put("labRatingNum", prep.labSummary.getRatingKeys().size + 1)
            dataMap.put("startDate", from)
            dataMap.put("endDate", to)
            dataMap.put("embed", embed)
            val template = cfg.getTemplate("SummaryReport.ftl");
            val out = StringWriter();
            template.process(dataMap, out);
            return out.toString();
        } else {
            return "";
        }
    }

    fun export() {
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
                fileChooser.showSaveDialog(primaryStage)?.writeText(buildSummary(fromField.value
                        ?: LocalDate.MIN, toField.value ?: LocalDate.now(), false))
            }
        }
    }

    fun showPrep(report: PrepReport) {
        Platform.runLater { -> prepResultBox.engine.loadContent(buildPrepReport(report, true)) };
    }

    fun showSummary() {
        if (inputFilePropery.isNotNull.get()) {
            Platform.runLater { ->
                summaryBox.engine.loadContent(buildSummary(fromField.value ?: LocalDate.MIN, toField.value
                        ?: LocalDate.MAX, true))
            }
        }
    }

    fun getSemesterStart(): LocalDate {
        val now = LocalDate.now();
        if (now.monthValue < 2) {
            return now.minusYears(1).withMonth(9).withDayOfMonth(1);
        } else if (now.monthValue < 9) {
            return now.withMonth(2).withDayOfMonth(10);
        } else {
            return now.withMonth(9).withDayOfMonth(1)
        }
    }


    /**
     * Reload statistics from data file
     */
    private fun reload(){

    }

    fun load() {
        if (inputFilePropery.isNotNull.get()) {
            try {
                reset()
                fileName.text = inputFilePropery.get()?.path;
                prepMap.putAll(buildPrepMap());
                prepList.items.addAll(prepMap.keys.sorted())
                prepList.selectionModel.selectedItemProperty().addListener { observableValue, oldValue, newValue ->
                    if (newValue != null) {
                        showPrep(prepMap[newValue]!!)
                    }
                }
                showSummary();
            } catch (ex: Exception) {
                val alert = Alert(Alert.AlertType.ERROR);
                alert.title = "Ошибка!"
                alert.headerText = "Невозможно прочитать файл данных"
                alert.contentText = "Произошла ошибка при чтении файла данных.\nПроверьте, что вы читаете правильный файл.";
                alert.show();
                reset()
            }
        }
    }
}

