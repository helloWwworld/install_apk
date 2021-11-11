import 'dart:async';

import 'package:flutter/services.dart';

class InstallApk {
  static const MethodChannel _channel = MethodChannel('install_apk');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String?> installApk(String filePath, String appId) async {
    Map<String, String> params = {
      "filePath": filePath,
      "appId": appId,
    };
    final String? version = await _channel.invokeMethod('install', params);
    return version;
  }
}
