/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package icons.org.intellij.datavis.inlays

import com.intellij.ide.ui.UISettings
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.FoldRegion
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.InlayModel
import com.intellij.openapi.editor.colors.EditorColorsListener
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.editor.ex.FoldingListener
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.concurrency.NonUrgentExecutor
import icons.org.intellij.datavis.ui.InlineToolbar
import org.intellij.datavis.inlays.*
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent

/**
 * Manages inlays.
 *
 * On project load subscribes
 *    on editor opening/closing.
 *    on adding/removing notebook cells
 *    on any document changes
 *    on folding actions
 *
 * On editor open checks the PSI structure and restores saved inlays.
 *
 * ToDo should be split into InlaysManager with all basics and NotebookInlaysManager with all specific.
 */
class EditorInlaysManager(val project: Project, private val editor: EditorImpl, val descriptor: InlayElementDescriptor) {

  private val inlays: MutableMap<PsiElement, NotebookInlayComponent> = LinkedHashMap()
  private val inlayElements = LinkedHashSet<PsiElement>()
  private val toolbars: MutableMap<PsiElement, InlineToolbar> = LinkedHashMap()
  @Volatile private var toolbarUpdateScheduled: Boolean = false

  init {
    addResizeListener()
    addCaretListener()
    addFoldingListener()
    addDocumentListener()
    addInlayModelListener()
    editor.settings.isRightMarginShown = false
    UISettings.instance.showEditorToolTip = false
    MouseWheelUtils.wrapEditorMouseWheelListeners(editor)
    restoreOutputs()
    restoreToolbars()
    onCaretPositionChanged()
    onColorSchemeChanged()
    ApplicationManager.getApplication().invokeLater {
      updateInlayComponentsWidth()
    }
  }

  private fun onColorSchemeChanged() {
    project.messageBus.connect(editor.disposable).subscribe(EditorColorsManager.TOPIC, EditorColorsListener { updateHighlighting() })
  }

  fun dispose() {
    inlays.values.forEach { it.disposeInlay() }
    inlays.clear()
    inlayElements.clear()
  }

  fun updateCell(psi: PsiElement) {
    ApplicationManager.getApplication().invokeLater {
      if (ApplicationManager.getApplication().isUnitTestMode) return@invokeLater
      val inlayOutputs = descriptor.getInlayOutputs(psi)
      getInlayComponent(psi)?.let { oldInlay -> removeInlay(oldInlay, cleanup = false) }
      if (inlayOutputs.isEmpty()) return@invokeLater
      addInlayOutputs(addInlayComponent(psi), inlayOutputs)
      ApplicationManager.getApplication().invokeLater {
        updateInlays()
      }
    }
  }

  private fun addInlayOutputs(inlayComponent: NotebookInlayComponent,
                              inlayOutputs: List<InlayOutput>) {
    inlayComponent.addInlayOutputs(inlayOutputs) { removeInlay(inlayComponent) }
  }

  private fun removeInlay(inlayComponent: NotebookInlayComponent, cleanup: Boolean = true) {
    val cell = inlayComponent.cell
    if (cleanup && cell.isValid) descriptor.cleanup(cell)
    inlayComponent.parent?.remove(inlayComponent)
    inlayComponent.disposeInlay()
    inlays.remove(cell)
  }

  private fun addFoldingListener() {
    Disposer.get("ui")?.let { uiParent ->
      data class Region(val textRange: TextRange, val isExpanded: Boolean)
      val listener = object : FoldingListener {
        private val regions = ArrayList<Region>()

        override fun onFoldRegionStateChange(region: FoldRegion) {
          regions.add(Region(TextRange.create(region.startOffset, region.endOffset), region.isExpanded))
        }

        override fun onFoldProcessingEnd() {
          inlays.filter { pair -> regions.filter { !it.isExpanded }.any { pair.key.textRange.intersects(it.textRange) } }.forEach {
            removeInlay(it.value, cleanup = false)
          }
          inlayElements.filter { key -> regions.filter { it.isExpanded }.any { key.textRange.intersects(it.textRange) } }.forEach {
            if (it !in inlays.keys) { updateCell(it) }
          }
          regions.clear()
          updateToolbarPositions()
          updateInlays()
        }
      }
      editor.foldingModel.addListener(listener, uiParent)
    }
  }

  private fun addDocumentListener() {
    editor.document.addDocumentListener(object : DocumentListener {
      override fun documentChanged(event: DocumentEvent) {
        if (!toolbarUpdateScheduled) {
          toolbarUpdateScheduled = true
          PsiDocumentManager.getInstance(project).performForCommittedDocument(editor.document) {
            try {
              removeInvalidToolbars()
              scheduleIntervalUpdate(event.offset, event.newFragment.length)
            } finally {
              toolbarUpdateScheduled = false
            }
          }
        }
        if (!event.oldFragment.contains("\n") && !event.newFragment.contains("\n")) return
        PsiDocumentManager.getInstance(project).performForCommittedDocument(editor.document) {
          updateInlays()
          updateToolbarPositions()
          updateHighlighting()
        }
      }
    })
  }

  private fun updateHighlighting() {
    descriptor.onUpdateHighlighting(toolbars.keys)
  }

  private fun scheduleIntervalUpdate(offset: Int, length: Int) {
    val psiFile = descriptor.psiFile
    var node = psiFile.node.findLeafElementAt(offset)?.psi
    while (node != null && node.parent != psiFile) {
      node = node.parent
    }
    var isAdded = false
    inlayElements.filter { !it.isValid }.forEach { getInlayComponent(it)?.let { inlay -> removeInlay(inlay) } }
    inlayElements.removeIf { !it.isValid }
    while (node != null && node.textRange.startOffset < offset + length) {
      PsiTreeUtil.collectElements(node) { psi -> descriptor.isToolbarActionElement(psi) || descriptor.isInlayElement(psi) }.forEach { psi ->
        if (descriptor.isToolbarActionElement(psi)) {
          if (psi !in toolbars) {
            addToolbar(psi, descriptor.getToolbarActions(psi)!!)
            isAdded = true
          }
        } else {
          inlayElements.add(psi)
        }
      }
      node = node.nextSibling
    }
    updateHighlighting()
    if (isAdded) {
      updateToolbarPositions()
    }
  }

  private fun removeInvalidToolbars() {
    toolbars.entries.toList().forEach { (psi, inlay) ->
      if (!psi.isValid) {
        toolbars.remove(psi)
        editor.contentComponent.remove(inlay)
      }
    }
  }

  /** On editor resize all inlays got width of editor. */
  private fun addResizeListener() {
    editor.component.addComponentListener(object : ComponentAdapter() {
      override fun componentResized(e: ComponentEvent) {
        updateInlayComponentsWidth()
        updateToolbarPositions()
      }
    })
  }

  private fun addInlayModelListener() {
    editor.inlayModel.addListener(object : InlayModel.SimpleAdapter() {
      override fun onUpdated(inlay: Inlay<*>) {
        updateToolbarPositions()
      }
    }, editor.disposable)
  }

  private fun restoreOutputs() {
    ReadAction.nonBlocking {
      PsiTreeUtil.collectElements(descriptor.psiFile) { descriptor.isInlayElement(it) }.forEach { psiCell ->
        val inlayOutputs = descriptor.getInlayOutputs(psiCell).takeIf { it.isNotEmpty() } ?: return@forEach
        ApplicationManager.getApplication().invokeLater {
          if (getInlayComponent(psiCell) == null) {
            addInlayOutputs(addInlayComponent(psiCell), inlayOutputs)
          }
        }
      }
    }.inSmartMode(project).submit(NonUrgentExecutor.getInstance())
  }

  private fun restoreToolbars() {
    val toolbars = ArrayList<Pair<PsiElement, ActionGroup>>()
    val inlaysPsiElements = ArrayList<PsiElement>()
    ReadAction.nonBlocking {
      PsiTreeUtil.processElements(descriptor.psiFile) { element ->
        descriptor.getToolbarActions(element)?.let { toolbars.add(element to it) }
        if (descriptor.isInlayElement(element) == true) inlaysPsiElements.add(element)
        true
      }
    }.finishOnUiThread(ModalityState.NON_MODAL) {
      inlayElements.clear()
      inlayElements.addAll(inlaysPsiElements)
      toolbars.forEach { (psi, action) ->
        addToolbar(psi, action)
      }
      updateHighlighting()
      updateToolbarPositions()
    }.inSmartMode(project).submit(NonUrgentExecutor.getInstance())
  }

  private fun addToolbar(psi: PsiElement, action: ActionGroup) {
    val inlineToolbar = InlineToolbar(psi, editor, action)
    editor.contentComponent.add(inlineToolbar)
    toolbars[psi] = inlineToolbar
  }

  private fun getInlayComponentByOffset(offset: Int): NotebookInlayComponent? {
    return if (offset == editor.document.textLength)
      inlays.entries.firstOrNull { it.key.textRange.containsOffset(offset) }?.value
    else
      inlays.entries.firstOrNull { it.key.textRange.contains(offset) }?.value
  }

  /** Add caret listener for editor to draw highlighted background for psiCell under caret. */
  private fun addCaretListener() {
    editor.caretModel.addCaretListener(object : CaretListener {

      override fun caretPositionChanged(e: CaretEvent) {
        if (editor.caretModel.primaryCaret != e.caret) return
        onCaretPositionChanged()
      }
    }, editor.disposable)
  }

  private fun onCaretPositionChanged() {

    if (editor.isDisposed) {
      return
    }

    val cellUnderCaret = getInlayComponentByOffset(editor.logicalPositionToOffset(editor.caretModel.logicalPosition))
    if (cellUnderCaret == null) {
      inlays.values.forEach { it.selected = false }
    }
    else {
      if (!cellUnderCaret.selected) {
        inlays.values.forEach { it.selected = false }
        cellUnderCaret.selected = true
      }
    }
  }

  /** When we are adding or removing paragraphs, old cells can change their text ranges*/
  private fun updateInlays() {
    inlays.values.forEach { updateInlayPosition(it) }
  }

  private fun setupInlayComponent(inlayComponent: NotebookInlayComponent) {

    fun updateInlaysInEditor(editor: Editor) {

      val end = editor.xyToLogicalPosition(Point(0, Int.MAX_VALUE))
      val offsetEnd = editor.logicalPositionToOffset(end)

      val inlays = editor.inlayModel.getBlockElementsInRange(0, offsetEnd)

      inlays.forEach { inlay ->
        if (inlay.renderer is InlayComponent) {
          (inlay.renderer as InlayComponent).updateComponentBounds(inlay)
        }
      }
    }

    inlayComponent.onHeightChanged = {
      updateToolbarPositions()
      updateInlaysInEditor(editor)
    }
  }

  /** Aligns all editor inlays to fill full width of editor. */
  private fun updateInlayComponentsWidth() {
    val gutterWidth = (editor.gutter as EditorGutterComponentEx).width
    val inlayWidth = editor.component.width - gutterWidth - InlayDimensions.rightBorder
    if (inlayWidth > 0) {
      inlays.values.forEach {
        it.setSize(inlayWidth, it.height)
        it.inlay?.updateSize()
      }
    }
  }

  /** It could be that user started to type below inlay. In this case we will detect new position and perform inlay repositioning. */
  private fun updateInlayPosition(inlayComponent: NotebookInlayComponent) {
    // editedCell here contains old text. This event will be processed by PSI later.
    val offset = getInlayOffset(inlayComponent.cell)
    if (inlayComponent.inlay!!.offset != offset) {
      inlayComponent.disposeInlay()
      val inlay = addBlockElement(offset, inlayComponent)
      inlayComponent.assignInlay(inlay)
    }
  }

  private fun updateToolbarPositions() {
    invokeLater { toolbars.values.forEach { it.updateBounds() } }
  }

  private fun addBlockElement(offset: Int, inlayComponent: NotebookInlayComponent): Inlay<NotebookInlayComponent> {
    return editor.inlayModel.addBlockElement(offset, true, false, 0, inlayComponent)!!
  }

  private fun addInlayComponent(cell: PsiElement): NotebookInlayComponent {

    val existingInlay = inlays[cell]
    if (existingInlay != null) {
      throw Exception("Cell already added.")
    }

    InlayDimensions.init(editor)

    val offset = getInlayOffset(cell)
    val inlayComponent = NotebookInlayComponent(cell, editor)

    // On editor creation it has 0 width
    val gutterWidth = (editor.gutter as EditorGutterComponentEx).width
    var editorWideWidth = editor.component.width - inlayComponent.width - gutterWidth - InlayDimensions.rightBorder
    if (editorWideWidth <= 0) {
      editorWideWidth = InlayDimensions.width
    }

    inlayComponent.setBounds(0, editor.offsetToXY(offset).y + editor.lineHeight, editorWideWidth, InlayDimensions.smallHeight)
    editor.contentComponent.add(inlayComponent)
    val inlay = addBlockElement(offset, inlayComponent)

    inlayComponent.assignInlay(inlay)
    inlays[cell] = inlayComponent

    setupInlayComponent(inlayComponent)

    return inlayComponent
  }

  /** Gets the offset to the last non-whitespace character in psiCell text. */
  private fun getInlayOffset(psiElement: PsiElement): Int {
    return psiElement.textRange.endOffset - 1
  }

  private fun getInlayComponent(cell: PsiElement): NotebookInlayComponent? {
    return inlays[cell]
  }
}