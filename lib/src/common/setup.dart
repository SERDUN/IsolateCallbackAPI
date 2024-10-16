import 'dart:ui';

import 'package:flutter/cupertino.dart';

import 'callkeep.pigeon.dart';

bool _bgHandlerInitialized = false;

typedef BackgroundHandler = Future<void> Function();

@pragma('vm:entry-point')
void _callbackDispatcher() {
  // Initialize the Flutter framework necessary for method channels and Pigeon.
  WidgetsFlutterBinding.ensureInitialized();

  // Set up the Pigeon API for the background service.
  PDelegateBackgroundRegisterFlutterApi.setUp(_BackgroundServiceDelegate());
}

class _BackgroundServiceDelegate implements PDelegateBackgroundRegisterFlutterApi {
  @override
  Future<void> onWakeUpBackgroundHandler(int userCallbackHandle) async {
    try {
      final CallbackHandle handle = CallbackHandle.fromRawHandle(userCallbackHandle);
      final closure = PluginUtilities.getCallbackFromHandle(handle)! as Future<void> Function();
      await closure();
    } catch (e) {
      print('Error in onWakeUpBackgroundHandler: $e');
    }
  }
}

class Setup {
  static final hostApi = PHostIsolateApi();

  static Future registerBackgroundMessageHandler(BackgroundHandler userBackgroundHandler) async {
    if (!_bgHandlerInitialized) {
      _bgHandlerInitialized = true;

      final userHandle = PluginUtilities.getCallbackHandle(userBackgroundHandler)!;
      final bgHandle = PluginUtilities.getCallbackHandle(_callbackDispatcher)!;

      await hostApi.registerBackgroundMessageHandler(
        userHandle.toRawHandle(),
        bgHandle.toRawHandle(),
      );
    }
  }

  static void wakeUpBackgroundHandler() {
    hostApi.wakeUpBackgroundHandler();
  }

  static void tearDownBackgroundHandler() {
    hostApi.tearDownBackgroundHandler();
  }

  static void requestPermissions() {
    hostApi.requestPermissions();
  }
}
