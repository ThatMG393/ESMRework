/*******************************************************************************
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2024  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 ******************************************************************************/

package io.github.rosemoe.sora.editor.ts

import com.itsaky.androidide.treesitter.TSNode
import com.itsaky.androidide.treesitter.TSQueryCapture
import com.itsaky.androidide.treesitter.TSQueryCursor
import com.itsaky.androidide.treesitter.TSTree
import com.itsaky.androidide.treesitter.string.UTF16String
import java.util.Stack

/**
 * Class for storing tree-sitter variables. This class tracks the positions and scopes
 * of variables and find definitions.
 *
 * @author Rosemoe
 * @param tree The parsed tree
 * @param text The current text for tree
 * @param spec Language specification, which should the same as highlighter's
 */
class TsScopedVariables(tree: TSTree, text: UTF16String, val spec: TsLanguageSpec) {

    private val rootScope: Scope = Scope(0, tree.rootNode.endByte / 2)

    init {
        if (spec.localsDefinitionIndices.isNotEmpty()) {
            TSQueryCursor.create().use { cursor ->
                cursor.exec(spec.tsQuery, tree.rootNode)
                var match = cursor.nextMatch()
                val captures = mutableListOf<TSQueryCapture>()
                while (match != null) {
                    if (spec.queryPredicator.doPredicate(spec.predicates, text, match)) {
                        captures.addAll(match.captures)
                    }
                    match = cursor.nextMatch()
                }
                captures.sortBy { it.node.startByte }
                val scopeStack = Stack<Scope>()
                var lastAddedVariableNode: TSNode? = null
                scopeStack.push(rootScope)
                for (capture in captures) {
                    val startIndex = capture.node.startByte / 2
                    val endIndex = capture.node.endByte / 2
                    while (startIndex >= scopeStack.peek().endIndex) {
                        scopeStack.pop()
                    }
                    val pattern = capture.index
                    if (pattern in spec.localsScopeIndices) {
                        val newScope = Scope(startIndex, endIndex)
                        scopeStack.peek().childScopes.add(newScope)
                        scopeStack.push(newScope)
                    } else if (pattern in spec.localsMembersScopeIndices) {
                        val newScope = Scope(startIndex, endIndex, true)
                        scopeStack.peek().childScopes.add(newScope)
                        scopeStack.push(newScope)
                    } else if (pattern in spec.localsDefinitionIndices) {
                        val scope = scopeStack.peek()
                        val utf16Name = text.subseqChars(startIndex, endIndex)
                        val name = utf16Name.toString()
                        utf16Name.close()
                        val scopedVar = ScopedVariable(
                            name,
                            if (scope.forMembers) scope.startIndex else startIndex,
                            scope.endIndex
                        )
                        scope.variables.add(scopedVar)
                        lastAddedVariableNode = capture.node
                    } else if (pattern !in spec.localsDefinitionValueIndices && pattern !in spec.localsReferenceIndices && lastAddedVariableNode != null) {
                        val topVariables = scopeStack.peek().variables
                        if (topVariables.isNotEmpty()) {
                            val topVariable = topVariables.last()
                            if (lastAddedVariableNode.startByte / 2 == startIndex && lastAddedVariableNode.endByte / 2 == endIndex && topVariable.matchedHighlightPattern == -1) {
                                topVariable.matchedHighlightPattern = pattern
                            }
                        }
                    }
                }
            }
        }
    }

    data class Scope(
        val startIndex: Int,
        val endIndex: Int,
        val forMembers: Boolean = false,
        val variables: MutableList<ScopedVariable> = mutableListOf(),
        val childScopes: MutableList<Scope> = mutableListOf()
    )

    data class ScopedVariable(
        var name: String,
        var scopeStartIndex: Int,
        var scopeEndIndex: Int,
        var matchedHighlightPattern: Int = -1
    )

    fun findDefinition(startIndex: Int, endIndex: Int, name: String): ScopedVariable? {
        var definition: ScopedVariable? = null
        var currentScope: Scope? = rootScope
        while (currentScope != null) {
            for (variable in currentScope.variables) {
                if (variable.scopeStartIndex > startIndex) {
                    break
                }
                if (variable.scopeStartIndex <= startIndex && variable.scopeEndIndex >= endIndex && variable.name == name) {
                    definition = variable
                    // Do not break here: name can be shadowed in some languages
                }
            }
            currentScope =
                currentScope.childScopes.firstOrNull { scope -> scope.startIndex <= startIndex && scope.endIndex >= endIndex }
        }
        return definition
    }

}