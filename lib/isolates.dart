import 'dart:isolate';

@pragma('vm:entry-point')
Future<void> userCallbackHandle() async {
  logIsolateInfo('updateApplicationStatus');
}

void logIsolateInfo(String message) {
  print('[$message] Isolate ID: ${Isolate.current.hashCode ?? 'main isolate'}');
}
