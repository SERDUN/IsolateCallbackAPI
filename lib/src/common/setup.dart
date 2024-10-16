import 'dart:ui';

import 'package:flutter/cupertino.dart';

import 'callkeep.pigeon.dart';

final hostApi = PHostIsolateApi();

bool _bgHandlerInitialized = false;

Future registerBackgroundMessageHandler(Function userCallbackHandle) async {
  if (!_bgHandlerInitialized) {
    _bgHandlerInitialized = true;

    final bgHandle = PluginUtilities.getCallbackHandle(_callbackDispatcher)!;
    final userHandle = PluginUtilities.getCallbackHandle(userCallbackHandle)!;

    await hostApi.registerBackgroundMessageHandler(
      userHandle.toRawHandle(),
      bgHandle.toRawHandle(),
    );
  }
}

void wakeUpBackgroundHandler() {
  hostApi.wakeUpBackgroundHandler();
}

void tearDownBackgroundHandler() {
  hostApi.tearDownBackgroundHandler();
}

void requestPermissions() {
  hostApi.requestPermissions();
}

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
    final CallbackHandle handle = CallbackHandle.fromRawHandle(userCallbackHandle);

    final closure = PluginUtilities.getCallbackFromHandle(handle)! as Future<void> Function();
    try {
      await closure();
    } catch (e) {
      print('FlutterFire Messaging: An error occurred in your background messaging handler:');
      print(e);
    }
  }
}
