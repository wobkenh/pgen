package de.wobkenh.pgen

import com.github.javaparser.ast.AccessSpecifier

enum class Visibility(val value: Int) {
    PUBLIC(4), PACKAGE(3), PROTECTED(2), PRIVATE(1), NONE(0);

    fun isLowerOrEqual(accessSpecifier: AccessSpecifier): Boolean = getValueForAccessSpecifier(accessSpecifier) >= value

    private fun getValueForAccessSpecifier(accessSpecifier: AccessSpecifier): Int = when (accessSpecifier) {
        AccessSpecifier.PUBLIC -> 4
        AccessSpecifier.PACKAGE_PRIVATE -> 3
        AccessSpecifier.PROTECTED -> 2
        AccessSpecifier.PRIVATE -> 1
    }
}