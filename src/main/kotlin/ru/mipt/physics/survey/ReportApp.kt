package ru.mipt.physics.survey

import freemarker.template.Configuration
import javafx.application.Application
import tornadofx.App
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by darksnake on 17-May-16.
 */
class ReportApp : App() {
    override val primaryView = ReportView::class
    fun startApp(args: Array<String>) {
        Application.launch(*args);
    }
}

fun configureFTL(): Configuration {
    // Create your Configuration instance, and specify if up to what FreeMarker
    // version (here 2.3.24) do you want to apply the fixes that are not 100%
    // backward-compatible. See the Configuration JavaDoc for details.
    val cfg = Configuration(Configuration.VERSION_2_3_23);

    // Specify the source where the template files come from. Here I set a
    // plain directory for it, but non-file-system sources are possible too:
    cfg.setClassLoaderForTemplateLoading(ClassLoader.getSystemClassLoader(), "/templates")

    // Set the preferred charset template files are stored in. UTF-8 is
    // a good choice in most applications:
    cfg.setDefaultEncoding("UTF-8");

    // Sets how errors will appear.
    // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
    cfg.setTemplateExceptionHandler(freemarker.template.TemplateExceptionHandler.RETHROW_HANDLER);

    // Don't log exceptions inside FreeMarker that it will thrown at you anyway:
    cfg.setLogTemplateExceptions(false);
    return cfg
}

val cfg = configureFTL();

fun main(args: Array<String>) {
    Logger.getGlobal().level = Level.ALL;
    ReportApp().startApp(args)
}