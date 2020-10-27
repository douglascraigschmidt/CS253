package edu.vanderbilt.imagecrawler.utils

object Assignment {
    enum class Name { 
        Assignment1a,
        Assignment1b,
        Assignment2a,
        Assignment2b,
        Assignment3a,
        Assignment3b,
        Assignment4,
        Assignment5,
        all
    }

    @JvmStatic
    var version = Name.Assignment3a

    @JvmStatic
    fun includes(name: Name): Boolean = name.ordinal <= version.ordinal

    @JvmStatic
    fun `is`(name: Name) = includes(name)

    @JvmStatic
    fun isAssignment(name: Name) = `is`(name)

    @JvmStatic
    fun isUndergraduate(name: Name) = isUndergraduate() && isAssignment(name)

    @JvmStatic
    fun isGraduate(name: Name) = isGraduate() && isAssignment(name)

    @JvmStatic
    fun isUndergraduate() = Student.isUndergraduate()

    @JvmStatic
    fun isGraduate() = Student.isGraduate()
}