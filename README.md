# P/Gen
PUML Class Diagram Generator for Java

## What is P/Gen?

P/Gen is a command line interface used to generate [Plant UML](http://plantuml.com/) class diagrams.
With a lot of options, it offers you the freedom to choose exactly which packages/classes you want to include in your diagram. 
The generated diagram will be in PUML format, which can be used by any Plant UML plugin/generator to create image files. 
P/Gen itself does not posses the ability to generate image files.

## Note

P/Gen is a fairly new project, so it is still under heavy construction.

## Usage

[You can download the pgen jar here.](https://simplex24.de/pgen/pgen.jar) Then simply call `pgen.jar` using `java -jar`. 

Alternatively, you can clone this repository and build/run P/Gen using Gradle or your IDE.

To get a quick overview of all available parameters, use the --help program option.
This will output the help:

```
Usage: pgen [OPTIONS]

Options:
  --directories PATH               Directories with java files to analyze
                                   (REQUIRED)
  --output-file PATH               Output file for PUML Class Diagram. Default
                                   output.puml
  --method-visibility [NONE|PRIVATE|PACKAGE|PROTECTED|PUBLIC]
                                   Which methods to show. Default NONE
  --attribute-visibility [NONE|PRIVATE|PACKAGE|PROTECTED|PUBLIC]
                                   Which attributes/fields to show. Default
                                   NONE
  --base-class TEXT                If given, will only generate the class
                                   diagram for the hierarchie of this base
                                   class (OPTIONAL)
  --scale TEXT                     Scale of the diagram, e.g. '1.5', '200*100'
                                   or 'max 1024 height' (OPTIONAL)
  --title TEXT                     Title of the diagram (OPTIONAL)
  --caption TEXT                   Caption of the diagram (OPTIONAL)
  --left-to-right-direction        Changes the direction of the diagram to
                                   'left to right'. Default 'top to bottom'
  --show-package                   Show packages, Default false
  --show-enum-arguments            Show arguments of enum entries (values for
                                   constructor), Default false
  --dependency-level [NONE|INTERNAL|EXTERNAL|ALL]
                                   Which dependencies to show in the diagram
  --debug                          Debug log level
  --trace                          Trace log level
  -h, --help                       Show this message and exit

```

A few notes here:
- The visibility options for methods and attributes/fields always include the options with higher visibility. 
This means that if you select method visibility "protected", then all methods with public, package-private or protected visibility will be included. 
- The dependency level INTERNAL will include all Files in the current scope which is set by the directory and package path options. 
The EXTERNAL level is not finished yet and right now is behaving like INTERNAL. 
In the future, this will try to resolve dependencies from external libraries. 
The ALL level will try to resolve dependencies from internal and external projects as well as java classes (e.g. List, String).
- When using any dependency level other than NONE, you might want to limit the scope of the class diagram using the output and package path options.
The reasaon for this is that the performance will degrade as the java parser tries to resolve the package names of the dependencies.
Of course, you are always welcome to test out the limits.  
- When using the base class option, the base class does not need to be in the current scope. You can e.g. use the base class "Exception" 
to get all classes in your scope that extend Exception and their subclasses.

## Authors

Henning Wobken ([henning.wobken@simplex24.de](mailto:henning.wobken@simplex24.de))

## License

P/Gen uses the Apache License 2.0