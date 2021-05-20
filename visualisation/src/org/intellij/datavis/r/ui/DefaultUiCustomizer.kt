package org.intellij.datavis.r.ui

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.UIUtil
import org.intellij.datavis.r.inlays.components.GraphicsPanel
import org.intellij.datavis.r.inlays.components.InlayProgressStatus
import org.intellij.datavis.r.inlays.components.ToolbarPane
import org.intellij.datavis.r.inlays.components.buildProgressStatusComponent
import org.intellij.images.editor.ImageEditor
import org.intellij.images.editor.impl.ImageEditorImpl
import javax.swing.JComponent
import javax.swing.JPanel

class DefaultUiCustomizer : UiCustomizer {
  override fun createImageEditor(project: Project, file: VirtualFile, graphicsPanel: GraphicsPanel): ImageEditor =
    ImageEditorImpl(project, file)

  override fun toolbarPaneProgressComponentChanged(toolbarPane: ToolbarPane, component: JComponent?): Unit = Unit

  override fun toolbarPaneToolbarComponentChanged(toolbarPane: ToolbarPane, component: JComponent?): Unit = Unit

  override fun toolbarPaneMainPanelCreated(toolbarPane: ToolbarPane, panel: JPanel?): Unit = Unit

  override fun getTextOutputBackground(editor: Editor) = UIUtil.getPanelBackground()

  override fun buildInlayProgressStatusComponent(progressStatus: InlayProgressStatus, editor: Editor): JComponent? {
    return buildProgressStatusComponent(progressStatus, editor)
  }

  override val showUpdateCellSeparator: Boolean = true
}