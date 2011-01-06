#!/usr/bin/env bash

# Build and install the Passify application
ant debug
adb uninstall net.jbrowning.passify
adb install bin/Passify-debug.apk
# ant reinstall 
adb shell am start -a android.intent.action.MAIN -n net.jbrowning.passify/net.jbrowning.passify.GenerateActivity
