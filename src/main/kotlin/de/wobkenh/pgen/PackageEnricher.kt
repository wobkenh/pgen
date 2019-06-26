package de.wobkenh.pgen

object PackageEnricher {
    fun enrichPackageNames(classDescriptorsSequence: Sequence<ClassDescriptor>): Sequence<ClassDescriptor> {
        val classDescriptorsList = classDescriptorsSequence.toList()
        val classDescriptorsMap = classDescriptorsList.groupBy({ it.className }, { it.packageName })
        return classDescriptorsList.map { classDescriptor ->
            val extendClassName = classDescriptor.extendedClassName.className
            if (extendClassName.isNotEmpty()) {
                val extendClasses = classDescriptorsMap[extendClassName]
                if (extendClasses != null) {
                    val extendClassPackage = extendClasses[0]
                    if (extendClasses.size > 1) {
                        println(
                            "[WARN] Found multiple classes with name $extendClassName. Choosing the first one " +
                                    "(package: '$extendClassPackage') for extension of ${classDescriptor.className}"
                        )
                    }
                    classDescriptor.extendedClassName.packageName = extendClassPackage
                } else {
                    println("[INFO] Could not find class $extendClassName in inspected package")
                }
            }
            classDescriptor.implementedClassNames.filter { it.className.isNotEmpty() }.forEach { implementDesciptor ->
                val implementedClasses = classDescriptorsMap[implementDesciptor.className]
                if (implementedClasses != null) {
                    val implementedClassPackage = implementedClasses[0]
                    if (implementedClasses.size > 1) {
                        println(
                            "[WARN] Found multiple interfaces with name ${implementDesciptor.className}. Choosing the first one " +
                                    "(package: '$implementedClassPackage') for implementation ${classDescriptor.className}"
                        )
                    }
                    implementDesciptor.packageName = implementedClassPackage
                } else {
                    println("[INFO] Could not find interface ${implementDesciptor.className} in inspected package")
                }
            }
            classDescriptor
        }.asSequence()
    }
}