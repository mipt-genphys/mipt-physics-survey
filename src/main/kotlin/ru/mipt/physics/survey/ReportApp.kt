package ru.mipt.physics.survey

import freemarker.template.Configuration
import javafx.application.Application
import javafx.scene.image.Image
import javafx.stage.Stage
import tornadofx.*

/**
 * Created by darksnake on 17-May-16.
 */
class ReportApp : App(ReportView::class) {
    override fun start(stage: Stage) {
        stage.icons += icon
        super.start(stage)
    }
}

fun configureFTL(): Configuration {
    // Create your Configuration instance, and specify if up to what FreeMarker
    // version (here 2.3.24) do you want to apply the fixes that are not 100%
    // backward-compatible. See the Configuration JavaDoc for details.
    val cfg = Configuration(Configuration.VERSION_2_3_28);

    // Specify the source where the template files come from. Here I set a
    // plain directory for it, but non-file-system sources are possible too:
    cfg.setClassLoaderForTemplateLoading(ClassLoader.getSystemClassLoader(), "/templates")

    // Set the preferred charset template files are stored in. UTF-8 is
    // a good choice in most applications:
    cfg.defaultEncoding = "UTF-8";

    // Sets how errors will appear.
    // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
    cfg.templateExceptionHandler = freemarker.template.TemplateExceptionHandler.RETHROW_HANDLER;

    // Don't log exceptions inside FreeMarker that it will thrown at you anyway:
    cfg.logTemplateExceptions = false;
    return cfg
}

val cfg = configureFTL();

val icon = Image(ReportApp::class.java.getResourceAsStream("/npm-logo-no-text-white.png"))

fun main(args: Array<String>) {
    //Logger.getGlobal().level = Level.ALL;
    Application.launch(ReportApp::class.java, *args)
}