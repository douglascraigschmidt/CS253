package edu.vanderbilt.imagecrawler.utils

import edu.vanderbilt.imagecrawler.utils.Student.graduateTodo
import edu.vanderbilt.imagecrawler.utils.Student.undergraduateTodo

object Assignment {
    enum class Name { 
                Assignment1a,
        Assignment1b,
        Assignment2a,
        Assignment2b,
        Assignment2c,
        Assignment3a,
        Assignment3b,
        Assignment4,
        Assignmentrx,
        all
    }

    @JvmStatic
    var version = Name.Assignment1b

    @JvmStatic
    fun includes(name: Name): Boolean = name.ordinal <= version.ordinal

    @JvmStatic
    fun `is`(name: Name) = includes(name)

    @JvmStatic
    fun isAssignment(name: Name) = `is`(name)

    @JvmStatic
    fun isUndergraduate(name: Name) = undergraduateTodo() && isAssignment(name)

    @JvmStatic
    fun isGraduate(name: Name) = graduateTodo() && isAssignment(name)
}