import 'package:flutter/material.dart';
import 'package:isolate_tester/src/common/setup.dart' as setup;

import 'isolates.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  setup.registerBackgroundMessageHandler(userCallbackHandle);
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      home: MainScreen(),
    );
  }
}

class MainScreen extends StatelessWidget {
  const MainScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        width: MediaQuery.of(context).size.width,
        padding: const EdgeInsets.only(top: 48),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Text("Foreground service API"),
            Divider(),
            SizedBox(height: 16),
            ElevatedButton(
              onPressed: setup.wakeUpBackgroundHandler,
              child: Text("Wake up background handler"),
            ),
            SizedBox(height: 8),
            ElevatedButton(
              onPressed: setup.tearDownBackgroundHandler,
              child: Text("Tear down background handler"),
            ),
            SizedBox(height: 8),
            ElevatedButton(
              onPressed: setup.requestPermissions,
              child: Text("Request permissions"),
            ),
          ],
        ),
      ),
    );
  }
}
