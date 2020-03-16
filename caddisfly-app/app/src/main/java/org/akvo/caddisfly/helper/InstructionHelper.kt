/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */
package org.akvo.caddisfly.helper

import org.akvo.caddisfly.model.Instruction
import org.akvo.caddisfly.model.PageIndex
import java.util.*
import java.util.regex.Pattern

/**
 * Installation related utility methods.
 */
object InstructionHelper {
    @JvmStatic
    fun setupInstructions(testInstructions: List<Instruction>,
                          instructions: ArrayList<Instruction>,
                          pageIndex: PageIndex, skip: Boolean): Int {
        var instructionIndex = 1
        var subSequenceIndex: Int
        instructions.clear()
        pageIndex.clear()
        var index = 0
        val subSequenceNumbers = arrayOf("i", "ii", "iii")
        var alphaSequence = false
        for (i in testInstructions.indices) {
            var instruction: Instruction
            try {
                instruction = testInstructions[i].clone()
                val section = instruction.section!!
                var indent = false
                subSequenceIndex = 0
                var leaveOut = false
                if (skip) {
                    for (i1 in section.indices) {
                        val item = section[i1]
                        if (item.contains("~skippable~")) {
                            leaveOut = true
                        }
                    }
                }
                if (leaveOut) {
                    continue
                }
                instruction.index = instructionIndex
                for (i1 in section.indices) {
                    val item = section[i1]
                    if (item.contains("~photo~")) {
                        pageIndex.setPhotoIndex(index)
                        if (pageIndex.skipToIndex < 0) {
                            pageIndex.skipToIndex = index
                        } else if (pageIndex.skipToIndex2 < 0) {
                            pageIndex.skipToIndex2 = index
                        }
                    } else if (item.contains("~input~")) {
                        pageIndex.setInputIndex(index)
                        if (pageIndex.skipToIndex < 0) {
                            pageIndex.skipToIndex = index
                        }
                    } else if (item.contains("~result~")) {
                        pageIndex.setResultIndex(index)
                    }
                    val m = Pattern.compile("^(\\d+?\\.\\s*)(.*)").matcher(item)
                    val m1 = Pattern.compile("^([a-zA-Z]\\.\\s*)(.*)").matcher(item)
                    if (subSequenceIndex > 0 || item.startsWith("i.")) {
                        section[i1] = subSequenceNumbers[subSequenceIndex] + ". " +
                                item.replace("i.", "")
                        subSequenceIndex++
                        indent = true
                    } else if (m1.find()) {
                        section[i1] = instructionIndex.toString() + item
                        alphaSequence = true
                        indent = true
                    } else {
                        if (alphaSequence) {
                            instructionIndex++
                            alphaSequence = false
                        } else if (m.find()) {
                            section[i1] = item
                        } else if (item.startsWith("stage")) {
                            section[i1] = item
                            indent = true
                        } else if (item.startsWith("~")) {
                            section[i1] = item
                        } else if (item.startsWith("/")) {
                            when {
                                item.startsWith("/-") -> {
                                    section[i1] = item.substring(2)
                                }
                                indent -> {
                                    section[i1] = "." + item.substring(1)
                                }
                                else -> {
                                    section[i1] = item.substring(1)
                                }
                            }
                        } else if (!item.startsWith(".") && !item.startsWith("image:")) {
                            section[i1] = "$instructionIndex. $item"
                            instructionIndex++
                            indent = true
                        }
                    }
                }
                index++
                instructions.add(instruction)
            } catch (e: CloneNotSupportedException) {
                e.printStackTrace()
            }
        }
        if (pageIndex.skipToIndex < 0) {
            pageIndex.skipToIndex = testInstructions.size
        }
        return instructionIndex
    }
}