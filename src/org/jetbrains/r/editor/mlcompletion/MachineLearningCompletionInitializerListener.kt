package org.jetbrains.r.editor.mlcompletion

import com.intellij.ide.AppLifecycleListener
import org.jetbrains.r.editor.mlcompletion.update.MachineLearningCompletionDownloadModelService
import org.jetbrains.r.editor.mlcompletion.update.MachineLearningCompletionNotifications
import org.jetbrains.r.editor.mlcompletion.update.MachineLearningCompletionRemoteArtifact
import org.jetbrains.r.editor.mlcompletion.update.MachineLearningCompletionUpdateAction
import org.jetbrains.r.settings.MachineLearningCompletionSettings
import org.jetbrains.r.settings.MachineLearningCompletionSettingsChangeListener

class MachineLearningCompletionInitializerListener : AppLifecycleListener {
  override fun appStarted() {
    tryLaunchServer()
    registerOnEnabledListener()
  }

  private fun tryLaunchServer() {
    if (MachineLearningCompletionSettings.getInstance().state.isEnabled) {
      MachineLearningCompletionServerService.getInstance().tryRelaunchServer()
    }
  }

  private fun registerOnEnabledListener() = MachineLearningCompletionSettingsChangeListener { beforeState, afterState ->
    val becameEnabled = !beforeState.isEnabled && afterState.isEnabled
    if (!becameEnabled || !MachineLearningCompletionUpdateAction.canInitiateUpdateAction.get()) {
      return@MachineLearningCompletionSettingsChangeListener
    }

    MachineLearningCompletionDownloadModelService.getInstance()
      .initiateUpdateCycle(isModal = false, reportIgnored = false) { (artifacts, size) ->
        if (artifacts.any(MachineLearningCompletionRemoteArtifact::localIsMissing)) {
          MachineLearningCompletionNotifications.showPopup(null, artifacts, size)
        }
      }
  }.subscribe()
}