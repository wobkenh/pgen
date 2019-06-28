package de.wobkenh.pgen

import com.github.javaparser.ast.AccessSpecifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class ClassDescriptorGeneratorTest {
    private val directory = getResourcesDir()

    @Test
    fun `test generation of class descriptors`() {
        val generator = ClassDescriptorGenerator(directory, "testcase1", Visibility.NONE, Visibility.NONE, DependencyLevel.NONE, false)
        println(directory.absolutePath)
        val classDescriptors = generator.generateClassDescriptors().toList()
        assertEquals(4, classDescriptors.size)
        val testAbstract = classDescriptors.find { it.className == "TestAbstract" }
        requireNotNull(testAbstract)
        assertEquals(listOf<FieldDescriptor>(), testAbstract.attributes)
        assertEquals(listOf<MethodDescriptor>(), testAbstract.methods)
        assertEquals(listOf<DependecyDescriptor>(), testAbstract.dependencies)
        assertEquals(ExtendsDescriptor("", ""), testAbstract.extendedClass)
        assertEquals(listOf<ImplementsDescriptor>(), testAbstract.implementedClasses)
        assertEquals("abstract class", testAbstract.type)
        val testClass = classDescriptors.find { it.className == "TestClass" }
        requireNotNull(testClass)
        assertEquals(listOf<FieldDescriptor>(), testClass.attributes)
        assertEquals(listOf<MethodDescriptor>(), testClass.methods)
        assertEquals(listOf<DependecyDescriptor>(), testClass.dependencies)
        assertEquals(ExtendsDescriptor("", "TestAbstract"), testClass.extendedClass)
        assertEquals(listOf(ImplementsDescriptor("", "TestInterface")), testClass.implementedClasses)
        assertEquals("class", testClass.type)
        val testInferface = classDescriptors.find { it.className == "TestInterface" }
        requireNotNull(testInferface)
        assertEquals(listOf<FieldDescriptor>(), testInferface.attributes)
        assertEquals(listOf<MethodDescriptor>(), testInferface.methods)
        assertEquals(listOf<DependecyDescriptor>(), testInferface.dependencies)
        assertEquals(ExtendsDescriptor("", ""), testInferface.extendedClass)
        assertEquals(listOf<ImplementsDescriptor>(), testInferface.implementedClasses)
        assertEquals("interface", testInferface.type)
        val testUnrelated = classDescriptors.find { it.className == "TestUnrelated" }
        requireNotNull(testUnrelated)
        assertEquals(listOf<FieldDescriptor>(), testUnrelated.attributes)
        assertEquals(listOf<MethodDescriptor>(), testUnrelated.methods)
        assertEquals(listOf<DependecyDescriptor>(), testUnrelated.dependencies)
        assertEquals(ExtendsDescriptor("", ""), testUnrelated.extendedClass)
        assertEquals(listOf<ImplementsDescriptor>(), testUnrelated.implementedClasses)
        assertEquals("class", testUnrelated.type)
    }

    @Test
    fun `test generation of class descriptors with everything turned on`() {
        val generator = ClassDescriptorGenerator(directory, "testcase1", Visibility.PRIVATE, Visibility.PRIVATE, DependencyLevel.ALL, true)
        println(directory.absolutePath)
        val classDescriptors = generator.generateClassDescriptors().toList()
        assertEquals(4, classDescriptors.size)
        val testAbstract = classDescriptors.find { it.className == "TestAbstract" }
        requireNotNull(testAbstract)
        assertEquals(listOf<FieldDescriptor>(), testAbstract.attributes)
        assertEquals(listOf(MethodDescriptor("testAbstract(String)", AccessSpecifier.PUBLIC)), testAbstract.methods)
        assertEquals(listOf(DependecyDescriptor("java.lang", "String")), testAbstract.dependencies)
        assertEquals(ExtendsDescriptor("", ""), testAbstract.extendedClass)
        assertEquals(listOf<ImplementsDescriptor>(), testAbstract.implementedClasses)
        assertEquals("testcase1", testAbstract.packageName)
        assertEquals("abstract class", testAbstract.type)
        val testClass = classDescriptors.find { it.className == "TestClass" }
        requireNotNull(testClass)
        assertEquals(listOf<FieldDescriptor>(), testClass.attributes)
        assertEquals(listOf<MethodDescriptor>(), testClass.methods)
        assertEquals(listOf<DependecyDescriptor>(), testClass.dependencies)
        assertEquals(ExtendsDescriptor("", "TestAbstract"), testClass.extendedClass)
        assertEquals(listOf(ImplementsDescriptor("", "TestInterface")), testClass.implementedClasses)
        assertEquals("testcase1", testClass.packageName)
        assertEquals("class", testClass.type)
        val testInterface = classDescriptors.find { it.className == "TestInterface" }
        requireNotNull(testInterface)
        assertEquals(listOf<FieldDescriptor>(), testInterface.attributes)
        assertEquals(listOf<MethodDescriptor>(), testInterface.methods)
        assertEquals(listOf<DependecyDescriptor>(), testInterface.dependencies)
        assertEquals(ExtendsDescriptor("", ""), testInterface.extendedClass)
        assertEquals(listOf<ImplementsDescriptor>(), testInterface.implementedClasses)
        assertEquals("testcase1", testInterface.packageName)
        assertEquals("interface", testInterface.type)
        val testUnrelated = classDescriptors.find { it.className == "TestUnrelated" }
        requireNotNull(testUnrelated)
        assertEquals(listOf<FieldDescriptor>(), testUnrelated.attributes)
        assertEquals(listOf<MethodDescriptor>(), testUnrelated.methods)
        assertEquals(listOf<DependecyDescriptor>(), testUnrelated.dependencies)
        assertEquals(ExtendsDescriptor("", ""), testUnrelated.extendedClass)
        assertEquals(listOf<ImplementsDescriptor>(), testUnrelated.implementedClasses)
        assertEquals("testcase1", testUnrelated.packageName)
        assertEquals("class", testUnrelated.type)
    }

    private fun getResourcesDir(): File {
        return File("src/test/resources")
    }
}