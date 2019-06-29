package de.wobkenh.pgen

import ch.qos.logback.classic.Level
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess

class PGen : CliktCommand() {

    private val logger = LoggerFactory.getLogger("")

    // CLI Arguments
    private val directory: File by option(help = "Sources root directory (REQUIRED)").file().required()
    private val outputFile: File by option(help = "Output file for PUML Class Diagram. Default output.puml").file().default(
        File("output.puml")
    )
    private val packagePath: String by option(help = "The package to analyze (OPTIONAL)").default("")
    private val methodVisibility: Visibility by option(help = "Which methods to show. Default NONE").convert { it.toUpperCase() }.choice(
        "NONE" to Visibility.NONE,
        "PRIVATE" to Visibility.PRIVATE,
        "PACKAGE" to Visibility.PACKAGE,
        "PROTECTED" to Visibility.PROTECTED,
        "PUBLIC" to Visibility.PUBLIC
    ).default(Visibility.NONE)
    private val attributeVisibility: Visibility by option(help = "Which attributes/fields to show. Default NONE").convert { it.toUpperCase() }.choice(
        "NONE" to Visibility.NONE,
        "PRIVATE" to Visibility.PRIVATE,
        "PACKAGE" to Visibility.PACKAGE,
        "PROTECTED" to Visibility.PROTECTED,
        "PUBLIC" to Visibility.PUBLIC
    ).default(Visibility.NONE)
    private val baseClass: String by option(help = "If given, will only generate the class diagram for the hierarchie of this base class (OPTIONAL)").default(
        ""
    )
    private val scale: String? by option(help = "Scale of the diagram, e.g. '1.5', '200*100' or 'max 1024 height' (OPTIONAL)")
    private val title: String? by option(help = "Title of the diagram (OPTIONAL)")
    private val caption: String? by option(help = "Caption of the diagram (OPTIONAL)")
    private val leftToRightDirection: Boolean by option(help = "Changes the direction of the diagram to 'left to right'. Default 'top to bottom'").flag(
        default = false
    )
    private val showPackage: Boolean by option(help = "Show packages, Default false").flag(default = false)
    private val dependencyLevel: DependencyLevel by option(help = "Which dependencies to show in the diagram").convert { it.toUpperCase() }.choice(
        "NONE" to DependencyLevel.NONE,
        "INTERNAL" to DependencyLevel.INTERNAL,
        "EXTERNAL" to DependencyLevel.EXTERNAL,
        "ALL" to DependencyLevel.ALL
    ).default(DependencyLevel.NONE)
    private val debug: Boolean by option(help = "Debug log level").flag(default = false)
    private val trace: Boolean by option(help = "Trace log level").flag(default = false)


    override fun run() {
        setLogLevel()
        printSettings()
        if (outputFile.exists()) {
            if (outputFile.isFile) {
                logger.info("Output file already exists. It will be overwritten")
            } else {
                logger.info("Output file already exists and is not a file.")
                exitProcess(1)
            }
        }

        var classDescriptors =
            ClassDescriptorGenerator(
                directory,
                packagePath,
                methodVisibility,
                attributeVisibility,
                dependencyLevel,
                showPackage
            ).generateClassDescriptors()

        if (baseClass.isNotEmpty()) {
            logger.info("Filtering for base class $baseClass")
            classDescriptors = BaseClassTreeFilter.filterForBaseClass(classDescriptors, baseClass)
        }

        val pumlBody = PumlBodyGenerator(showPackage, dependencyLevel).generatePumlBody(classDescriptors)
        PumlWriter.writePuml(
            pumlBody,
            packagePath,
            attributeVisibility,
            methodVisibility,
            scale,
            title,
            caption,
            leftToRightDirection,
            outputFile
        )
    }

    private fun setLogLevel() {
        if (logger is ch.qos.logback.classic.Logger) {
            when {
                trace -> logger.level = Level.TRACE
                debug -> logger.level = Level.DEBUG
                else -> logger.level = Level.INFO
            }
        } else {
            println("[Error] Could not configure log level.")
        }
    }

    private fun printSettings() {
        logger.info("Generating PUML Class Diagram for classes in package path $packagePath")
        logger.info("Using ${outputFile.absolutePath} as output path")

        logger.info("Using Options:")
        logger.info("# Package Path $packagePath")
        logger.info("# Directory $packagePath")
        logger.info("# OutputFile ${outputFile.absolutePath}")
        logger.info("# Method Visibility $methodVisibility")
        logger.info("# Attribute Visibility $attributeVisibility")
        logger.info("# Dependency Level $dependencyLevel")
        if (this.baseClass.isNotEmpty()) {
            logger.info("# Base Class $baseClass")
        }
        if (this.title != null) {
            logger.info("# Title $title")
        }
        if (this.caption != null) {
            logger.info("# Caption $caption")
        }
        if (this.scale != null) {
            logger.info("# Scale $scale")
        }
        if (leftToRightDirection) logger.info("# Direction left to right") else logger.info("# Direction top to bottom")
        if (this.showPackage) {
            logger.info("# Showing Packages")
        }
    }

}

fun main(args: Array<String>) = PGen().main(args)