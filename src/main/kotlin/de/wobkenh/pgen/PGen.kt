package de.wobkenh.pgen

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import kotlin.system.exitProcess

class PGen : CliktCommand() {

    // CLI Arguments
    private val packagePath: String by option(help = "The package to analyze (OPTIONAL)").default("")
    private val directory: File? by option(help = "Sources root directory (REQUIRED)").file()
    private val outputFile: File by option(help = "Output file for PUML Class Diagram. Default output.puml").file().default(
        File("output.puml")
    )
    private val methodVisibility: Visibility by option(help = "Which methods to show. Default NONE").choice(
        "NONE" to Visibility.NONE,
        "PRIVATE" to Visibility.PRIVATE,
        "PACKAGE" to Visibility.PACKAGE,
        "PROTECTED" to Visibility.PROTECTED,
        "PUBLIC" to Visibility.PUBLIC
    ).default(Visibility.NONE)
    private val attributeVisibility: Visibility by option(help = "Which attributes/fields to show. Default NONE").choice(
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
    private val showPackage: Boolean by option(help = "Show packages").flag(default = false)


    override fun run() {
        if (directory == null) {
            println("No sources root directory provided.")
            exitProcess(1)
        }

        printSettings()

        if (outputFile.exists()) {
            if (outputFile.isFile) {
                println("Output file already exists. It will be overwritten")
            } else {
                println("Output file already exists and is not a file.")
                exitProcess(1)
            }
        }

        var classDescriptors =
            ClassDescriptorGenerator(directory, packagePath, methodVisibility, attributeVisibility).generateClassDescriptors()

        if (baseClass.isNotEmpty()) {
            println("Filtering for base class $baseClass")
            classDescriptors = BaseClassTreeFilter.filterForBaseClass(classDescriptors, baseClass)
        }
        if (showPackage) {
            classDescriptors = PackageEnricher.enrichPackageNames(classDescriptors)
        }

        val pumlBody = PumlBodyGenerator(showPackage).generatePumlBody(classDescriptors)
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

    private fun printSettings() {
        println("Generating PUML Class Diagram for classes in package path $packagePath")
        println("Using ${outputFile.absolutePath} as output path")

        println("Using Options:")
        println("- Package Path $packagePath")
        println("- Directory $packagePath")
        println("- OutputFile ${outputFile.absolutePath}")
        println("- Method Visibility $methodVisibility")
        println("- Attribute Visibility $attributeVisibility")
        if (this.baseClass.isNotEmpty()) {
            println("- Base Class $baseClass")
        }
        if (this.title != null) {
            println("- Title $title")
        }
        if (this.caption != null) {
            println("- Caption $caption")
        }
        if (this.scale != null) {
            println("- Scale $scale")
        }
        if (leftToRightDirection) println("- Direction left to right") else println("- Direction top to bottom")
        if (this.showPackage) {
            println("- Showing Packages")
        }
    }

}

fun main(args: Array<String>) = PGen().main(args)