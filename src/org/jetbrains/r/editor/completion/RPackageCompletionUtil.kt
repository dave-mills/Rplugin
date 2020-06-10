/*
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.editor.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import org.jetbrains.r.console.runtimeInfo
import org.jetbrains.r.interpreter.RInterpreterManager
import org.jetbrains.r.psi.stubs.RAssignmentCompletionIndex
import org.jetbrains.r.psi.stubs.RInternalAssignmentCompletionIndex

object RPackageCompletionUtil {

  fun addPackageCompletion(position: PsiElement, result: CompletionResultSet) {
    val interpreter = RInterpreterManager.getInterpreterOrNull(position.project) ?: return
    val installedPackages = interpreter.installedPackages
    installedPackages.forEach { result.consume(RLookupElementFactory().createPackageLookupElement(it.packageName, false)) }
  }

  fun addNamespaceCompletion(namespaceName: String,
                             isInternalAccess: Boolean,
                             parameters: CompletionParameters,
                             result: CompletionResultSet,
                             elementFactory: RLookupElementFactory) {
    val project = parameters.position.project
    val interpreter = RInterpreterManager.getInterpreterOrNull(project) ?: return
    val packageFile = interpreter.getSkeletonFileByPackageName(namespaceName) ?: return
    val scope = GlobalSearchScope.fileScope(packageFile)
    addCompletionFromIndices(project, scope, parameters.originalFile, "", HashSet(), result, elementFactory, isInternalAccess)
  }

  fun addCompletionFromIndices(project: Project,
                               scope: GlobalSearchScope,
                               originFile: PsiFile,
                               prefix: String,
                               shownNames: HashSet<String>,
                               result: CompletionResultSet,
                               elementFactory: RLookupElementFactory,
                               isInternalAccess: Boolean = false) {
    val runtimeInfo = originFile.runtimeInfo
    val interpreter = RInterpreterManager.getInterpreterOrNull(originFile.project)
    var hasElementsWithPrefix = false
    if (runtimeInfo != null && interpreter != null) {
      val loadedPackages = runtimeInfo.loadedPackages
        .mapNotNull { interpreter.getSkeletonFileByPackageName(it.key)?.virtualFile?.to(it.value) }
        .toMap()
      val runtimeScope = GlobalSearchScope.filesScope(originFile.project, loadedPackages.keys).intersectWith(scope)
      val lookupElements = mutableMapOf<String, Pair<LookupElement, Int?>>()
      processElementsFromIndex(project, runtimeScope, isInternalAccess, elementFactory) { element, file ->
        if (shownNames.contains(element.lookupString)) return@processElementsFromIndex
        if (element.lookupString.startsWith(prefix)) {
          hasElementsWithPrefix = true
        }
        val previousPriority = lookupElements[element.lookupString]?.second
        val currentPriority = loadedPackages[file]
        if (previousPriority == null || (currentPriority != null && currentPriority < previousPriority)) {
          lookupElements[element.lookupString] = element to currentPriority
        }
      }
      result.addAllElements(lookupElements.values.map { it.first })
      shownNames.addAll(lookupElements.keys)
    }
    if (!hasElementsWithPrefix) {
      processElementsFromIndex(project, scope, isInternalAccess, elementFactory) { it, _ ->
        if (shownNames.add(it.lookupString)) result.consume(it)
      }
    }
  }

  private fun processElementsFromIndex(project: Project,
                                       scope: GlobalSearchScope,
                                       isInternalAccess: Boolean,
                                       elementFactory: RLookupElementFactory,
                                       consumer: (LookupElement, VirtualFile) -> Unit) {
    val indexAccessor = if (isInternalAccess) RInternalAssignmentCompletionIndex else RAssignmentCompletionIndex
    indexAccessor.process("", project, scope, Processor { assignment ->
      consumer(elementFactory.createGlobalLookupElement(assignment), assignment.containingFile.virtualFile)
      return@Processor true
    })
  }
}