/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.highlighting

import com.intellij.codeInsight.daemon.RainbowVisitor
import com.intellij.codeInsight.daemon.impl.HighlightVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.r.annotator.textAttribute
import org.jetbrains.r.psi.api.RControlFlowHolder
import org.jetbrains.r.psi.api.RFile
import org.jetbrains.r.psi.api.RIdentifierExpression
import org.jetbrains.r.psi.findVariableDefinition

class RRainbowVisitor : RainbowVisitor() {
  override fun suitableForFile(file: PsiFile): Boolean = file is RFile

  override fun clone(): HighlightVisitor = RRainbowVisitor()

  override fun visit(element: PsiElement) {
    if (element is RIdentifierExpression) {
      val variableDefinition = element.findVariableDefinition() ?: return
      val firstDefinition = variableDefinition.variableDescription.firstDefinition
      val definitionControlFlowHolder = PsiTreeUtil.getParentOfType(firstDefinition, RControlFlowHolder::class.java) ?: return
      addInfo(getInfo(definitionControlFlowHolder, element, element.name, element.textAttribute))
    }
  }
}