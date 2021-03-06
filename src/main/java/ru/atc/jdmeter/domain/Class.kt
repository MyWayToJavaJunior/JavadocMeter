package ru.atc.jdmeter.domain

/**
 * Created by vkoba on 26.10.2016.
 */


class Class(val name: String, val linesOfCode: List<String>) {
    fun hasCommentAboveClass(): Boolean {
        return linesOfCode.subList(0, linesOfCode.indexOfFirst { s -> s.contains("class $name") })
                .reduceRight { s1, s2 -> s1 + s2 }
                .contains("/**")
    }

    fun isInterface(): Boolean {
        return linesOfCode.find { s -> s.contains(" interface ") } != null;
    }

    fun isMybatisInterface(): Boolean {
        return linesOfCode.find { s -> s.contains("import org.apache.ibatis") } != null
    }

    fun countOfPublicMethod(): Int {
        return publicMethodLines().count()
    }

    fun notCommentedClass(): String? {
        val publicMethodLines = publicMethodLines();
        for (i in linesOfCode.indices) {
            if (publicMethodLines.contains(linesOfCode[i])) {
                val potentialCommentArea = findPotencialCommentArea(i, publicMethodLines)
                if (potentialCommentArea
                        .filter { line -> LineOfCode(line).hasCommentSymbols() || LineOfCode(line).hasOverrideAnnotation() }
                        .count() == 0) {
                    return this.name;
                }
            }
        }
        return null;
    }

    fun countOfCommentedPublicMethod(): Int {
        val publicMethodLines = publicMethodLines();

        var commentedPublicMethods = 0
        for (i in linesOfCode.indices) {
            if (publicMethodLines.contains(linesOfCode[i])) {
                val potentialCommentArea = findPotencialCommentArea(i, publicMethodLines);
                if (potentialCommentArea
                        .filter { line -> LineOfCode(line).hasCommentSymbols() || LineOfCode(line).hasOverrideAnnotation() }
                        .count() > 0) {
                    commentedPublicMethods++
                }
            }
        }
        return commentedPublicMethods
    }

    private fun findPotencialCommentArea(publicMethodLineNumber: Int, publicMethodLines: List<String>): List<String> {
        return linesOfCode.subList(getPreviousMethodEndNumber(publicMethodLines, publicMethodLineNumber), publicMethodLineNumber);
    }

    private fun getPreviousMethodEndNumber(publicMethodLines: List<String>, publicMethodLineNumber: Int): Int {
        for (i in (publicMethodLineNumber - 1) downTo 1) {
            val currentLine = linesOfCode[i];
            if (publicMethodLines.contains(currentLine) || currentLine.trim() == "}" || currentLine.contains(" class ") || currentLine.contains(" interface ") || currentLine.contains(" enum ")) {
                return i;
            }
        }
        return 0;
    }

    fun countOfWtf(): Int {
        return linesOfCode.filter { line -> line.toUpperCase().contains("WTF") }.count()
    }

    private fun publicMethodLines(): List<String> {
        return linesOfCode
                .filter { line ->
                    (Method(line).isPublicMethod(name) && Method(line).methodIsNotSetter())
                            || (isInterface() && !isMybatisInterface() &&
                            (Method(line).isItInterfaceDefaultMethodLine() || Method(line).isInterfaceMethod()) && Method(line).methodIsNotSetter())
                };
    }
}

