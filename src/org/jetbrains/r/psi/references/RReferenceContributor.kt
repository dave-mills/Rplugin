/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.psi.references

import com.intellij.openapi.paths.WebReference
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.ProcessingContext
import com.intellij.util.io.URLUtil
import org.jetbrains.r.RLanguage
import org.jetbrains.r.console.runtimeInfo
import org.jetbrains.r.psi.RElementFilters
import org.jetbrains.r.psi.api.RFile
import org.jetbrains.r.psi.api.RStringLiteralExpression
import java.io.File
import java.nio.file.Path


class RReferenceContributor : PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    val filePathReferenceProvider = object : PsiReferenceProvider() {
      override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val stringLiteral = element as? RStringLiteralExpression ?: return PsiReference.EMPTY_ARRAY
        val text = stringLiteral.name ?: return PsiReference.EMPTY_ARRAY
        if (URLUtil.URL_PATTERN.matcher(text).matches()) return arrayOf(WebReference(element))

        val file = element.containingFile
        val project = element.project
        val set = object : FileReferenceSet(text, element, 1, this, true, true) {
          override fun getDefaultContexts(): MutableCollection<PsiFileSystemItem> {
            val result = super.getDefaultContexts()
            val workingDir = (file as? RFile)?.runtimeInfo?.workingDir
            if (workingDir != null) {
              val dir = LocalFileSystem.getInstance().findFileByPath(workingDir)?.let { PsiUtilCore.findFileSystemItem(project, it)}
              if (dir != null) {
                return result.plus(dir).toMutableList()
              }
            }
            return result
          }

          override fun findSeparatorLength(sequence: CharSequence, atOffset: Int): Int {
            return super.findSeparatorLength(sequence.replace(SEPARATOR_REGEX, separatorString), atOffset)
          }

          override fun findSeparatorOffset(sequence: CharSequence, startingFrom: Int): Int {
            return super.findSeparatorOffset(sequence.replace(SEPARATOR_REGEX, separatorString), startingFrom)
          }
        }

        val lastReference = set.lastReference ?: return emptyArray()
        return arrayOf(toSourceFileReference(lastReference) ?: return emptyArray())
      }
    }

    registrar.registerReferenceProvider(
      PlatformPatterns.psiElement().withLanguage(RLanguage.INSTANCE).and(RElementFilters.STRING_FILTER),
      filePathReferenceProvider, PsiReferenceRegistrar.LOWER_PRIORITY
    )
  }

  companion object {
    private fun toSourceFileReference(fileReference: FileReference): SourceFileReference? {
      val extendedRange = extendedRange(fileReference.rangeInElement) ?: return null
      val path = Path.of("", *fileReference.fileReferenceSet.allReferences.map { it.text }.toTypedArray()).toString()
      return SourceFileReference(fileReference, extendedRange, path)
    }

    private fun extendedRange(range: TextRange): TextRange? {
      val endOffset = range.endOffset
      if (endOffset < 1) return null
      return TextRange(1, endOffset)
    }

    private class SourceFileReference(private val origin: FileReference, extendedRange: TextRange, path: String)
      : FileReference(origin.fileReferenceSet, extendedRange, origin.index, path) {
      override fun resolve(): PsiFileSystemItem? {
        return origin.resolve()
      }
    }

    private val SEPARATOR_REGEX = File.separator.replace("\\", "\\\\").toRegex()
  }
}