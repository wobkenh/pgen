package de.wobkenh.pgen

import com.github.javaparser.ast.AccessSpecifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class ClassDescriptorGeneratorTest {
    private val directory = getResourcesDir()

    @Test
    fun `test generation of class descriptors`() {
        val generator =
            ClassDescriptorGenerator(
                listOf(File(directory, "testcase1")),
                Visibility.NONE,
                Visibility.NONE,
                DependencyLevel.NONE,
                false,
                false
            )
        println(directory.absolutePath)
        val classDescriptors = generator.generateClassDescriptors().toList()
        assertEquals(4, classDescriptors.size)
        val testAbstract = classDescriptors.find { it.className == "TestAbstract" }
        requireNotNull(testAbstract)
        if (testAbstract !is ClassDescriptor) throw RuntimeException("wrong type")
        assertEquals(listOf<FieldDescriptor>(), testAbstract.attributes)
        assertEquals(listOf<MethodDescriptor>(), testAbstract.methods)
        assertEquals(listOf<DependencyDescriptor>(), testAbstract.dependencies)
        assertEquals(ExtendsDescriptor("", ""), testAbstract.extendedClass)
        assertEquals(listOf<ImplementsDescriptor>(), testAbstract.implementedClasses)
        assertEquals("abstract class", testAbstract.type)
        val testClass = classDescriptors.find { it.className == "TestClass" }
        requireNotNull(testClass)
        if (testClass !is ClassDescriptor) throw RuntimeException("wrong type")
        assertEquals(listOf<FieldDescriptor>(), testClass.attributes)
        assertEquals(listOf<MethodDescriptor>(), testClass.methods)
        assertEquals(listOf<DependencyDescriptor>(), testClass.dependencies)
        assertEquals(ExtendsDescriptor("", "TestAbstract"), testClass.extendedClass)
        assertEquals(listOf(ImplementsDescriptor("", "TestInterface")), testClass.implementedClasses)
        assertEquals("class", testClass.type)
        val testInferface = classDescriptors.find { it.className == "TestInterface" }
        requireNotNull(testInferface)
        if (testInferface !is ClassDescriptor) throw RuntimeException("wrong type")
        assertEquals(listOf<FieldDescriptor>(), testInferface.attributes)
        assertEquals(listOf<MethodDescriptor>(), testInferface.methods)
        assertEquals(listOf<DependencyDescriptor>(), testInferface.dependencies)
        assertEquals(ExtendsDescriptor("", ""), testInferface.extendedClass)
        assertEquals(listOf<ImplementsDescriptor>(), testInferface.implementedClasses)
        assertEquals("interface", testInferface.type)
        val testUnrelated = classDescriptors.find { it.className == "TestUnrelated" }
        requireNotNull(testUnrelated)
        if (testUnrelated !is ClassDescriptor) throw RuntimeException("wrong type")
        assertEquals(listOf<FieldDescriptor>(), testUnrelated.attributes)
        assertEquals(listOf<MethodDescriptor>(), testUnrelated.methods)
        assertEquals(listOf<DependencyDescriptor>(), testUnrelated.dependencies)
        assertEquals(ExtendsDescriptor("", ""), testUnrelated.extendedClass)
        assertEquals(listOf<ImplementsDescriptor>(), testUnrelated.implementedClasses)
        assertEquals("class", testUnrelated.type)
    }

    @Test
    fun `test generation of class descriptors with everything turned on`() {
        val generator =
            ClassDescriptorGenerator(
                listOf(File(directory, "testcase1")),
                Visibility.PRIVATE,
                Visibility.PRIVATE,
                DependencyLevel.ALL,
                true,
                false
            )
        val classDescriptors = generator.generateClassDescriptors().toList()
        assertEquals(4, classDescriptors.size)
        val testAbstract = classDescriptors.find { it.className == "TestAbstract" }
        requireNotNull(testAbstract)
        if (testAbstract !is ClassDescriptor) throw RuntimeException("wrong type")
        assertEquals(listOf<FieldDescriptor>(), testAbstract.attributes)
        assertEquals(listOf(MethodDescriptor("testAbstract(String)", AccessSpecifier.PUBLIC, "void")), testAbstract.methods)
        assertEquals(listOf(DependencyDescriptor("java.lang", "String")), testAbstract.dependencies)
        assertEquals(ExtendsDescriptor("", ""), testAbstract.extendedClass)
        assertEquals(listOf<ImplementsDescriptor>(), testAbstract.implementedClasses)
        assertEquals("testcase1", testAbstract.packageName)
        assertEquals("abstract class", testAbstract.type)
        val testClass = classDescriptors.find { it.className == "TestClass" }
        requireNotNull(testClass)
        if (testClass !is ClassDescriptor) throw RuntimeException("wrong type")
        assertEquals(listOf<FieldDescriptor>(), testClass.attributes)
        assertEquals(
            listOf(
                MethodDescriptor("testAbstract(String)", AccessSpecifier.PUBLIC, "void"),
                MethodDescriptor("test()", AccessSpecifier.PUBLIC, "int")
            ), testClass.methods
        )
        assertEquals(
            listOf(
                DependencyDescriptor("java.lang", "String"),
                DependencyDescriptor("testcase1", "TestAbstract"),
                DependencyDescriptor("testcase1", "TestInterface")
            ), testClass.dependencies
        )
        assertEquals(ExtendsDescriptor("testcase1", "TestAbstract"), testClass.extendedClass)
        assertEquals(listOf(ImplementsDescriptor("testcase1", "TestInterface")), testClass.implementedClasses)
        assertEquals("testcase1", testClass.packageName)
        assertEquals("class", testClass.type)
        val testInterface = classDescriptors.find { it.className == "TestInterface" }
        requireNotNull(testInterface)
        if (testInterface !is ClassDescriptor) throw RuntimeException("wrong type")
        assertEquals(listOf<FieldDescriptor>(), testInterface.attributes)
        assertEquals(listOf(MethodDescriptor("test()", AccessSpecifier.PUBLIC, "int")), testInterface.methods)
        assertEquals(listOf<DependencyDescriptor>(), testInterface.dependencies)
        assertEquals(ExtendsDescriptor("", ""), testInterface.extendedClass)
        assertEquals(listOf<ImplementsDescriptor>(), testInterface.implementedClasses)
        assertEquals("testcase1", testInterface.packageName)
        assertEquals("interface", testInterface.type)
        val testUnrelated = classDescriptors.find { it.className == "TestUnrelated" }
        requireNotNull(testUnrelated)
        if (testUnrelated !is ClassDescriptor) throw RuntimeException("wrong type")
        assertEquals(listOf(FieldDescriptor("testNumber", "int", AccessSpecifier.PRIVATE)), testUnrelated.attributes)
        assertEquals(listOf(MethodDescriptor("test(String)", AccessSpecifier.PUBLIC, "String")), testUnrelated.methods)
        assertEquals(listOf(DependencyDescriptor("java.lang", "String")), testUnrelated.dependencies)
        assertEquals(ExtendsDescriptor("", ""), testUnrelated.extendedClass)
        assertEquals(listOf<ImplementsDescriptor>(), testUnrelated.implementedClasses)
        assertEquals("testcase1", testUnrelated.packageName)
        assertEquals("class", testUnrelated.type)
    }

    @Test
    fun `test field visibility public`() {
        val generatorPublic =
            ClassDescriptorGenerator(
                listOf(File(directory, "testcase2")),
                Visibility.NONE,
                Visibility.PUBLIC,
                DependencyLevel.NONE,
                false,
                false
            )
        val classDescriptorsPublic = generatorPublic.generateClassDescriptors().toList()
        assertEquals(1, classDescriptorsPublic.size)
        val classPublic = classDescriptorsPublic[0]
        assertEquals("TestFieldVisibility", classPublic.className)
        assertEquals(1, classPublic.attributes.size)
        assertEquals(FieldDescriptor("testPublic", "Boolean", AccessSpecifier.PUBLIC), classPublic.attributes[0])
    }

    @Test
    fun `test field visibility package`() {
        val generatorPublic =
            ClassDescriptorGenerator(
                listOf(File(directory, "testcase2")),
                Visibility.NONE,
                Visibility.PACKAGE,
                DependencyLevel.NONE,
                false,
                false
            )
        val classDescriptorsPublic = generatorPublic.generateClassDescriptors().toList()
        assertEquals(1, classDescriptorsPublic.size)
        val classPublic = classDescriptorsPublic[0]
        assertEquals("TestFieldVisibility", classPublic.className)
        assertEquals(2, classPublic.attributes.size)
        assertEquals(
            FieldDescriptor("testPublic", "Boolean", AccessSpecifier.PUBLIC),
            classPublic.attributes.find { it.name == "testPublic" })
        assertEquals(
            FieldDescriptor("testPackage", "LocalDate", AccessSpecifier.PACKAGE_PRIVATE),
            classPublic.attributes.find { it.name == "testPackage" })
    }

    @Test
    fun `test field visibility protected`() {
        val generatorPublic =
            ClassDescriptorGenerator(
                listOf(File(directory, "testcase2")),
                Visibility.NONE,
                Visibility.PROTECTED,
                DependencyLevel.NONE,
                false,
                false
            )
        val classDescriptorsPublic = generatorPublic.generateClassDescriptors().toList()
        assertEquals(1, classDescriptorsPublic.size)
        val classPublic = classDescriptorsPublic[0]
        assertEquals("TestFieldVisibility", classPublic.className)
        assertEquals(3, classPublic.attributes.size)
        assertEquals(
            FieldDescriptor("testPublic", "Boolean", AccessSpecifier.PUBLIC),
            classPublic.attributes.find { it.name == "testPublic" })
        assertEquals(
            FieldDescriptor("testPackage", "LocalDate", AccessSpecifier.PACKAGE_PRIVATE),
            classPublic.attributes.find { it.name == "testPackage" })
        assertEquals(
            FieldDescriptor("testProtected", "String", AccessSpecifier.PROTECTED),
            classPublic.attributes.find { it.name == "testProtected" })
    }

    @Test
    fun `test field visibility private`() {
        val generatorPublic =
            ClassDescriptorGenerator(
                listOf(File(directory, "testcase2")),
                Visibility.NONE,
                Visibility.PRIVATE,
                DependencyLevel.NONE,
                false,
                false
            )
        val classDescriptorsPublic = generatorPublic.generateClassDescriptors().toList()
        assertEquals(1, classDescriptorsPublic.size)
        val classPublic = classDescriptorsPublic[0]
        assertEquals("TestFieldVisibility", classPublic.className)
        assertEquals(4, classPublic.attributes.size)
        assertEquals(
            FieldDescriptor("testPublic", "Boolean", AccessSpecifier.PUBLIC),
            classPublic.attributes.find { it.name == "testPublic" })
        assertEquals(
            FieldDescriptor("testPackage", "LocalDate", AccessSpecifier.PACKAGE_PRIVATE),
            classPublic.attributes.find { it.name == "testPackage" })
        assertEquals(
            FieldDescriptor("testProtected", "String", AccessSpecifier.PROTECTED),
            classPublic.attributes.find { it.name == "testProtected" })
        assertEquals(
            FieldDescriptor("testPrivate", "int", AccessSpecifier.PRIVATE),
            classPublic.attributes.find { it.name == "testPrivate" })
    }

    @Test
    fun `test method visibility public`() {
        val generatorPublic =
            ClassDescriptorGenerator(
                listOf(File(directory, "testcase3")),
                Visibility.PUBLIC,
                Visibility.NONE,
                DependencyLevel.NONE,
                false,
                false
            )
        val classDescriptorsPublic = generatorPublic.generateClassDescriptors().toList()
        assertEquals(1, classDescriptorsPublic.size)
        val classPublic = classDescriptorsPublic[0]
        assertEquals("TestMethodVisibility", classPublic.className)
        assertEquals(1, classPublic.methods.size)
        assertEquals(
            MethodDescriptor("testPublic(String, LocalDate, boolean)", AccessSpecifier.PUBLIC, "Boolean"),
            classPublic.methods.find { it.signature.contains("testPublic") })
    }


    @Test
    fun `test method visibility package`() {
        val generatorPublic =
            ClassDescriptorGenerator(
                listOf(File(directory, "testcase3")),
                Visibility.PACKAGE,
                Visibility.NONE,
                DependencyLevel.NONE,
                false,
                false
            )
        val classDescriptorsPublic = generatorPublic.generateClassDescriptors().toList()
        assertEquals(1, classDescriptorsPublic.size)
        val classPublic = classDescriptorsPublic[0]
        assertEquals("TestMethodVisibility", classPublic.className)
        assertEquals(2, classPublic.methods.size)
        assertEquals(
            MethodDescriptor("testPublic(String, LocalDate, boolean)", AccessSpecifier.PUBLIC, "Boolean"),
            classPublic.methods.find { it.signature.contains("testPublic") })
        assertEquals(
            MethodDescriptor("testPackage(String, LocalDate)", AccessSpecifier.PACKAGE_PRIVATE, "LocalDate"),
            classPublic.methods.find { it.signature.contains("testPackage") })
    }

    @Test
    fun `test method visibility protected`() {
        val generatorPublic =
            ClassDescriptorGenerator(
                listOf(File(directory, "testcase3")),
                Visibility.PROTECTED,
                Visibility.NONE,
                DependencyLevel.NONE,
                false,
                false
            )
        val classDescriptorsPublic = generatorPublic.generateClassDescriptors().toList()
        assertEquals(1, classDescriptorsPublic.size)
        val classPublic = classDescriptorsPublic[0]
        assertEquals("TestMethodVisibility", classPublic.className)
        assertEquals(3, classPublic.methods.size)
        assertEquals(
            MethodDescriptor("testPublic(String, LocalDate, boolean)", AccessSpecifier.PUBLIC, "Boolean"),
            classPublic.methods.find { it.signature.contains("testPublic") })
        assertEquals(
            MethodDescriptor("testPackage(String, LocalDate)", AccessSpecifier.PACKAGE_PRIVATE, "LocalDate"),
            classPublic.methods.find { it.signature.contains("testPackage") })
        assertEquals(
            MethodDescriptor("testProtected(String)", AccessSpecifier.PROTECTED, "String"),
            classPublic.methods.find { it.signature.contains("testProtected") })
    }

    @Test
    fun `test method visibility private`() {
        val generatorPublic =
            ClassDescriptorGenerator(
                listOf(File(directory, "testcase3")),
                Visibility.PRIVATE,
                Visibility.NONE,
                DependencyLevel.NONE,
                false,
                false
            )
        val classDescriptorsPublic = generatorPublic.generateClassDescriptors().toList()
        assertEquals(1, classDescriptorsPublic.size)
        val classPublic = classDescriptorsPublic[0]
        assertEquals("TestMethodVisibility", classPublic.className)
        assertEquals(4, classPublic.methods.size)
        assertEquals(
            MethodDescriptor("testPublic(String, LocalDate, boolean)", AccessSpecifier.PUBLIC, "Boolean"),
            classPublic.methods.find { it.signature.contains("testPublic") })
        assertEquals(
            MethodDescriptor("testPackage(String, LocalDate)", AccessSpecifier.PACKAGE_PRIVATE, "LocalDate"),
            classPublic.methods.find { it.signature.contains("testPackage") })
        assertEquals(
            MethodDescriptor("testProtected(String)", AccessSpecifier.PROTECTED, "String"),
            classPublic.methods.find { it.signature.contains("testProtected") })
        assertEquals(
            MethodDescriptor("testPrivate()", AccessSpecifier.PRIVATE, "int"),
            classPublic.methods.find { it.signature.contains("testPrivate") })
    }

    @Test
    fun `test enum with arguments`() {
        val generatorPublic =
            ClassDescriptorGenerator(
                listOf(File(directory, "testcase4")),
                Visibility.PRIVATE,
                Visibility.PRIVATE,
                DependencyLevel.NONE,
                false,
                true
            )
        val enumDescriptors = generatorPublic.generateClassDescriptors().toList()
        assertEquals(1, enumDescriptors.size)
        val enumDescriptor = enumDescriptors[0] as? EnumDescriptor ?: throw RuntimeException("wrong type")
        assertEquals("TestEnum", enumDescriptor.className)
        assertEquals(1, enumDescriptor.methods.size)
        assertEquals(MethodDescriptor("getTest()", AccessSpecifier.PUBLIC, "String"), enumDescriptor.methods[0])
        assertEquals(ValueDescriptor("A", "\"TestA\""), enumDescriptor.values[0])
        assertEquals(ValueDescriptor("B", "\"TestB\""), enumDescriptor.values[1])
    }

    @Test
    fun `test enum without arguments`() {
        val generatorPublic =
            ClassDescriptorGenerator(
                listOf(File(directory, "testcase4")),
                Visibility.PRIVATE,
                Visibility.PRIVATE,
                DependencyLevel.NONE,
                false,
                false
            )
        val enumDescriptors = generatorPublic.generateClassDescriptors().toList()
        assertEquals(1, enumDescriptors.size)
        val enumDescriptor = enumDescriptors[0] as? EnumDescriptor ?: throw RuntimeException("wrong type")
        assertEquals("TestEnum", enumDescriptor.className)
        assertEquals(1, enumDescriptor.methods.size)
        assertEquals(MethodDescriptor("getTest()", AccessSpecifier.PUBLIC, "String"), enumDescriptor.methods[0])
        assertEquals(ValueDescriptor("A", ""), enumDescriptor.values[0])
        assertEquals(ValueDescriptor("B", ""), enumDescriptor.values[1])
    }

    @Test
    fun `test inner class`() {
        val innerClassDescriptors =
            ClassDescriptorGenerator(
                listOf(File(directory, "testcase5")),
                Visibility.PRIVATE,
                Visibility.PRIVATE,
                DependencyLevel.NONE,
                false,
                false
            ).generateClassDescriptors().toList()
        assertEquals(3, innerClassDescriptors.size)
        val testFrame = innerClassDescriptors.find { it.className == "InnerClassTest" }
        requireNotNull(testFrame)
        if (testFrame !is ClassDescriptor) throw RuntimeException("wrong type")
        assertEquals("testcase5", testFrame.packageName)
        assertEquals(listOf<FieldDescriptor>(), testFrame.attributes)
        assertEquals(listOf<MethodDescriptor>(), testFrame.methods)
        assertEquals(listOf<DependencyDescriptor>(), testFrame.dependencies)
        assertEquals(ExtendsDescriptor("", ""), testFrame.extendedClass)
        assertEquals(listOf<ImplementsDescriptor>(), testFrame.implementedClasses)
        assertEquals("class", testFrame.type)

        val testBase = innerClassDescriptors.find { it.className == "ExceptionBase" }
        requireNotNull(testBase)
        if (testBase !is ClassDescriptor) throw RuntimeException("wrong type")
        assertEquals("testcase5.InnerClassTest", testBase.packageName)
        assertEquals(listOf<FieldDescriptor>(), testBase.attributes)
        assertEquals(listOf<MethodDescriptor>(), testBase.methods)
        assertEquals(listOf<DependencyDescriptor>(), testBase.dependencies)
        assertEquals(ExtendsDescriptor("", "Exception"), testBase.extendedClass)
        assertEquals(listOf<ImplementsDescriptor>(), testBase.implementedClasses)
        assertEquals("class", testBase.type)

        val testExtend = innerClassDescriptors.find { it.className == "ExceptionExtend" }
        requireNotNull(testExtend)
        if (testExtend !is ClassDescriptor) throw RuntimeException("wrong type")
        assertEquals("testcase5.InnerClassTest", testExtend.packageName)
        assertEquals(listOf(FieldDescriptor("code", "String", AccessSpecifier.PRIVATE)), testExtend.attributes)
        assertEquals(listOf(MethodDescriptor("getCode()", AccessSpecifier.PUBLIC, "String")), testExtend.methods)
        assertEquals(listOf<DependencyDescriptor>(), testExtend.dependencies)
        assertEquals(ExtendsDescriptor("testcase5.InnerClassTest", "ExceptionBase"), testExtend.extendedClass)
        assertEquals(listOf<ImplementsDescriptor>(), testExtend.implementedClasses)
        assertEquals("class", testExtend.type)
    }

    private fun getResourcesDir(): File {
        return File("src/test/resources")
    }


}
