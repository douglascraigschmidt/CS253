package edu.vanderbilt.imagecrawler.utils

import edu.vanderbilt.imagecrawler.utils.Student.Type.Graduate
import edu.vanderbilt.imagecrawler.utils.Student.Type.Undergraduate

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
    fun `is`(type: Type) = this.type.contains(type)

    @JvmStatic
    fun isGraduate() = `is`(Graduate)

    @JvmStatic
    fun isUndergraduate() = `is`(Undergraduate)
}