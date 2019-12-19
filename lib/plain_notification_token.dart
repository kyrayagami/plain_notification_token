import 'dart:async';
import 'dart:io';
import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:platform/platform.dart';
typedef Future<dynamic> MessageHandler(Map<String, dynamic> message);
typedef SelectNotificationCallback = Future<dynamic> Function(String payload);

class PlainNotificationToken {
  static PlainNotificationToken _instance;
  final MethodChannel _channel;
  final Platform _platform;
  MessageHandler _onMessage;
  MessageHandler _onLaunch;
  MessageHandler _onResume;
  SelectNotificationCallback selectNotificationCallback;


  PlainNotificationToken._(MethodChannel channel, Platform platform)
      : _channel = channel,
        _platform = platform {
    // _channel.setMethodCallHandler(_handleMethod);
  }

  factory PlainNotificationToken() =>
      _instance ??
      (_instance = PlainNotificationToken._(
          const MethodChannel('plain_notification_token'),
          const LocalPlatform()));

  final StreamController<String> _tokenStreamController =
      StreamController<String>.broadcast();

  /// Fires when a new token is generated.
  Stream<String> get onTokenRefresh => _tokenStreamController.stream;

  /// Returns the APNs (in iOS)/FCM (in Android) token.
  Future<String> getToken() => _channel.invokeMethod<String>('getToken');

  Future<void> subscribeToTopic(String topic){
    return _channel.invokeMethod<void>('subscribe', {"channel": topic});
  }

  Future<void> unsubscribeFromTopic(String topic){
    return _channel.invokeMethod<void>('unsubscribe', {"channel": topic});
  }

  final StreamController<IosNotificationSettings> _iosSettingsStreamController =
      StreamController<IosNotificationSettings>.broadcast();

  final StreamController<String> _dataNotification = StreamController<String>.broadcast();

  /// Stream that fires when the user changes their notification settings.
  ///
  /// Only fires on iOS.
  Stream<IosNotificationSettings> get onIosSettingsRegistered =>
      _iosSettingsStreamController.stream;

  //
  Stream<String> get onMessage => _dataNotification.stream;

  /// On iOS, prompts the user for notification permissions the first time it is called.
  ///
  /// Does nothing on Android.
  void requestPermission(
      [IosNotificationSettings settings = const IosNotificationSettings()]) {
    if (_platform.isAndroid) return;

    _channel.invokeMethod("requestPermission", settings.toMap());
  }

  Future<dynamic> _handleMethod(MethodCall call) async {
    switch (call.method) {
      case "onToken":
        final String token = call.arguments;
        _tokenStreamController.add(token);
        return null;
      case "onIosSettingsRegistered":
        _iosSettingsStreamController.add(IosNotificationSettings._fromMap(
            call.arguments.cast<String, bool>()));
        return null;
      case "onMessage":
        print("plugin onMessage" + call.arguments.toString());
        try {
          //return _onMessage(call.arguments.cast<String, dynamic>()); 
          return _onMessage(call.arguments.cast<String, dynamic>());
        } catch (e) {
          print("Error" + e);
        }
        return _onMessage(call.arguments);
      case "onLaunch":
        return _onLaunch(call.arguments.cast<String, dynamic>());
      case "onResume":
        print("plugin onResume" + call.arguments.toString());
        return _onResume(call.arguments.cast<String, dynamic>());
      default:
        throw UnsupportedError("Unrecognized JSON message");
    }
  }


  /// Sets up [MessageHandler] for incoming messages.
  void configure({
    MessageHandler onMessage,
    MessageHandler onLaunch,
    MessageHandler onResume,
  }) {
    _onMessage = onMessage;
    _onLaunch = onLaunch;
    _onResume = onResume;
    _channel.setMethodCallHandler(_handleMethod);
    _channel.invokeMethod<void>('configure');
  }

  Future<dynamic> autoInitParse(serverUrl, applicationId, clientKey) async{
    // // if (Platform.isIOS) {
    //   plainNotificationToken.requestPermission();
    //   // If you want to wait until Permission dialog close,
    //   // you need wait changing setting registered.
    //   await plainNotificationToken.onIosSettingsRegistered.first;
    // // }
    // final String token = await plainNotificationToken.getToken();
    // await initParse(serverUrl, applicationId, token);
    print("autoInitParse run");
    await _channel.invokeMethod('initParse',{"serverUrl" :serverUrl, "applicationId": applicationId, "clientKey": clientKey} );

    // return token;
  }
}

/// Representing settings of notify way in iOS.
class IosNotificationSettings {
  final bool alert;
  final bool badge;
  final bool sound;

  const IosNotificationSettings(
      {this.alert = true, this.badge = true, this.sound = true});

  IosNotificationSettings._fromMap(Map<String, bool> settings)
      : sound = settings['sound'],
        alert = settings['alert'],
        badge = settings['badge'];

  @visibleForTesting
  Map<String, dynamic> toMap() {
    return <String, bool>{'sound': sound, 'alert': alert, 'badge': badge};
  }

  @override
  String toString() => 'PushNotificationSettings ${toMap()}';
}

//  class DataNotification{
//    final
//  }
