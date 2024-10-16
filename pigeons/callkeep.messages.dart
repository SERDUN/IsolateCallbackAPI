// ignore_for_file: avoid_positional_boolean_parameters, one_member_abstracts

import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(
  PigeonOptions(
    dartOut: 'lib/src/common/callkeep.pigeon.dart',
    kotlinOut: 'android/app/src/main/kotlin/com/serdun/online/isolate_tester/Generated.kt',
    kotlinOptions: KotlinOptions(
      package: 'com.serdun.online.isolate_tester',
      errorClassName: 'HostCallsDartPigeonFlutterError',
    ),
  ),
)
@HostApi()
abstract class PHostIsolateApi {
  @async
  void registerBackgroundMessageHandler(
    int userCallbackHandle,
    int bgCallbackHandle,
  );

  @async
  void wakeUpBackgroundHandler();

  @async
  void tearDownBackgroundHandler();

  @async
  void requestPermissions();
}

@FlutterApi()
abstract class PDelegateBackgroundRegisterFlutterApi {
  @async
  void onWakeUpBackgroundHandler(int userCallbackHandle);
}
