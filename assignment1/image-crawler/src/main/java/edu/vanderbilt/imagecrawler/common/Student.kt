package edu.vanderbilt.imagecrawler.common

import edu.vanderbilt.imagecrawler.common.Student.Type.Graduate
import edu.vanderbilt.imagecrawler.common.Student.Type.Undergraduate

object Student {
    enum class Type {
        Graduate,
        Undergraduate
    }

    /**
     * TODO: (Graduate students) - remove "Undergraduate" from the set below.
     * TODO: (Undergraduate students) - remove "Graduate" from the set below.
     */
    @JvmStatic
    private var type = setOf(Graduate, Undergraduate)

    @JvmStatic
    fun `is`(type: Type) = Student.type.contains(type)

    @JvmStatic
    fun isGraduate() = `is`(Graduate)

    @JvmStatic
    fun isUndergraduate() = `is`(Undergraduate)
}