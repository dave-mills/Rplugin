// Copyright (c) 2017, Holger Brandl, Ekaterina Tuzova
/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.r.run.configuration

import com.intellij.openapi.options.SettingsEditor
import javax.swing.JComponent

class RRunConfigurationEditor : SettingsEditor<RRunConfiguration>() {
  private var myForm: RRunConfigurationForm = RRunConfigurationForm()

  public override fun resetEditorFrom(config: RRunConfiguration) {
    myForm.scriptPath = config.scriptPath
  }

  public override fun applyEditorTo(config: RRunConfiguration) {
    config.scriptPath = myForm.scriptPath
  }

  override fun createEditor(): JComponent {
    return myForm.panel
  }
}
