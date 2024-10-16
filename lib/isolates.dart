import 'dart:isolate';

import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';

@pragma('vm:entry-point')
Future<void> syncCallStatus() async {
  logIsolateInfo('syncCallStatus');
  print('syncCallStatus');
}

@pragma('vm:entry-point')
Future<void> userCallbackHandle(bool isBackground) async {
  logIsolateInfo('updateApplicationStatus');
  print('updateApplicationStatus isBackground: $isBackground');
  Fluttertoast.showToast(
      msg: "This is Center Short Toast",
      toastLength: Toast.LENGTH_SHORT,
      gravity: ToastGravity.CENTER,
      timeInSecForIosWeb: 1,
      backgroundColor: Colors.red,
      textColor: Colors.white,
      fontSize: 16.0);
}

void logIsolateInfo(String message) {
  print('[$message] Isolate ID: ${Isolate.current.hashCode ?? 'main isolate'}');
}
