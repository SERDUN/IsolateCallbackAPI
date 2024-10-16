import 'dart:isolate';

import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';

@pragma('vm:entry-point')
Future<void> syncCallStatus() async {
  logIsolateInfo('syncCallStatus');
  print('syncCallStatus');
}

@pragma('vm:entry-point')
Future<void> userCallbackHandle() async {
  logIsolateInfo('updateApplicationStatus');
}

void logIsolateInfo(String message) {
  print('[$message] Isolate ID: ${Isolate.current.hashCode ?? 'main isolate'}');
}
