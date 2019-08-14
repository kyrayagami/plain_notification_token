#import "PlainNotificationTokenPlugin.h"
#import <UserNotifications/UserNotifications.h>
#import <Parse/Parse.h>

@implementation PlainNotificationTokenPlugin {
    NSString *_lastToken;
    FlutterMethodChannel *_channel;
//    UILocalNotification *launchNotification;
    bool launchingAppFromNotification;
    NSDictionary *_launchNotification;
    bool initialized;
    BOOL _resumingFromBackground;
}
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"plain_notification_token"
            binaryMessenger:[registrar messenger]];
  PlainNotificationTokenPlugin* instance = [[PlainNotificationTokenPlugin alloc] initWithChannel:channel];
  [registrar addApplicationDelegate:instance];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (instancetype)initWithChannel:(FlutterMethodChannel *)channel {
    self = [super init];

    if (self) {
        _channel = channel;
        dispatch_async(dispatch_get_main_queue(), ^() {
            [[UIApplication sharedApplication] registerForRemoteNotifications];
        });
        if (@available(iOS 10.0, *)) {
            [[UNUserNotificationCenter currentNotificationCenter] getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings * _Nonnull settings) {
                NSDictionary *settingsDictionary = @{
                                                     @"sound" : [NSNumber numberWithBool:settings.soundSetting == UNNotificationSettingEnabled],
                                                     @"badge" : [NSNumber numberWithBool:settings.badgeSetting == UNNotificationSettingEnabled],
                                                     @"alert" : [NSNumber numberWithBool:settings.alertSetting == UNNotificationSettingEnabled],
                                                     };
                [self->_channel invokeMethod:@"onIosSettingsRegistered" arguments:settingsDictionary];
            }];
        }
    }
    return self;
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getToken" isEqualToString:call.method]) {
    result([self getToken]);
  } else if ([@"requestPermission" isEqualToString:call.method]) {
      [self requestPermissionWithSettings:[call arguments]];
      result(nil);
  }
  else if([@"configure" isEqualToString:call.method]) {
    if (@available(iOS 10.0, *)) {
        UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
        [center requestAuthorizationWithOptions:(UNAuthorizationOptionSound | UNAuthorizationOptionAlert | UNAuthorizationOptionBadge) completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if (error != nil) {
                NSLog(@"Error during requesting notification permission: %@", error);
            }
            if (granted) {
                dispatch_async(dispatch_get_main_queue(), ^() {
                    [[UIApplication sharedApplication] registerForRemoteNotifications];
                });
                [self->_channel invokeMethod:@"onIosSettingsRegistered" arguments:@"settings"];
            }
            if(self->_launchNotification != nil) {
                [self handleSelectNotification:self->_launchNotification];
//                  [FlutterLocalNotificationsPlugin handleSelectNotification:launchPayload];
                
            }
            result(@(granted));
        }];
//          [center requestAuthorizationWithOptions:(authorizationOptions) completionHandler:^(BOOL granted, NSError * _Nullable error) {
//              if(launchPayload != nil) {
//                  [FlutterLocalNotificationsPlugin handleSelectNotification:launchPayload];
//              }
//              result(@(granted));
//          }];
    } else {
        // Fallback on earlier versions
    }
    if (_launchNotification != nil) {
        [_channel invokeMethod:@"onLaunch" arguments:_launchNotification];
    }
  }
  else if ([@"initParse" isEqualToString:call.method]) {
    /*
    UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeAlert | UIUserNotificationTypeBadge | UIUserNotificationTypeSound categories:nil];
    [application registerUserNotificationSettings:settings];
    [application registerForRemoteNotifications];
    */
    NSString *applicationId = call.arguments[@"applicationId"];
    NSString *serverUrl = call.arguments[@"serverUrl"];
    //      [self initParse:[ appId:applicationId serverUrl:serverUrl ]]
    [self initParse:applicationId serverUrl:serverUrl ];
//      [self runTask:runMyTask arguments:taskArgs launchPath:command];
//      - (void)runTask:(NSTask *)theTask arguments:(NSArray *)arguments launchPath:(NSString *)launchPath;
//      -(void)initParse:(NSString *)appId initParse:(NSString *)serverUrl{
  }
  else {
    result(FlutterMethodNotImplemented);
  }

}

//-(void)initParse:(NSString *)appId (NSString *) serverUrl{
-(void)initParse:(NSString *)appId serverUrl:(NSString *)serverUrl{
  // BOOL isOk = [self swizzled_application:application didFinishLaunchingWithOptions:launchOptions];
//   @try {
//      // test if Parse client has been initialized in the main AppDelegate.m
//      NSLog(@"Custom Parse.Push init already took place. appId: ");
//   } @catch (NSException *exception) {
//      //
      // default Parse Push setup. For custom setup, initialize the Parse client and
      // notification settings yourself in your main AppDelegate.m 's didFinishLaunchingWithOptions
      //
//      ParsePushPlugin* pluginInstance = [self getParsePluginInstance];
      
     [Parse initializeWithConfiguration:[ParseClientConfiguration configurationWithBlock:^(id<ParseMutableClientConfiguration> configuration) {
        configuration.applicationId = appId;
        configuration.server = serverUrl;
         configuration.clientKey = @"";
     }]];

//      if(!autoReg.length || [autoReg caseInsensitiveCompare:@"true"] == 0 || [application isRegisteredForRemoteNotifications]){
//          // if autoReg is true or nonexistent (defaults to true)
//          // or app already registered for PN, do/redo registration
//          //
//          // Note: redo registration because APNS device token can change and Apple
//          // suggests re-registering on each app start. registerForPN() is idempotent so
//          // no worries if it gets called multiple times.
//          [pluginInstance registerForPN];
//      }
//    }
}

// - (void)createInstallationParse:(NSData *deviceToken, )call result:(FlutterResult)result {

-(void)createInstallationParse:(NSData *)deviceToken {
    NSLog(@"creado token en parse");
   PFInstallation *currentInstallation = [PFInstallation currentInstallation];
  [currentInstallation setDeviceTokenFromData: deviceToken];
  [currentInstallation saveInBackground];
}

- (NSString *)getToken {
    return _lastToken;
}
- (void)requestPermissionWithSettings: (NSDictionary<NSString*, NSNumber*> *)settings {
    if (@available(iOS 10.0, *)) {
        UNAuthorizationOptions options = UNAuthorizationOptionNone;
        if ([[settings objectForKey:@"sound"] boolValue]) {
            options |= UNAuthorizationOptionSound;
        }
        if ([[settings objectForKey:@"badge"] boolValue]) {
            options |= UNAuthorizationOptionBadge;
        }
        if ([[settings objectForKey:@"alert"] boolValue]) {
            options |= UNAuthorizationOptionAlert;
        }
        [[UNUserNotificationCenter currentNotificationCenter] requestAuthorizationWithOptions:options completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if (error != nil) {
                NSLog(@"Error during requesting notification permission: %@", error);
            }
            if (granted) {
                dispatch_async(dispatch_get_main_queue(), ^() {
                    [[UIApplication sharedApplication] registerForRemoteNotifications];
                });
                [self->_channel invokeMethod:@"onIosSettingsRegistered" arguments:settings];
            }
        }];
    }
    else {
        UIUserNotificationType types = 0;
        if ([[settings objectForKey:@"sound"] boolValue]) {
            types |= UIUserNotificationTypeSound;
        }
        if ([[settings objectForKey:@"badge"] boolValue]) {
            types |= UIUserNotificationTypeBadge;
        }
        if ([[settings objectForKey:@"alert"] boolValue]) {
            types |= UIUserNotificationTypeAlert;
        }
        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:types categories:nil];
        [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
    }
}

#pragma mark - AppDelegate

- (void)application:(UIApplication *)application didRegisterUserNotificationSettings:(UIUserNotificationSettings *)notificationSettings {
    NSDictionary *settingsDictionary = @{
                                         @"sound" : [NSNumber numberWithBool:notificationSettings.types & UIUserNotificationTypeSound],
                                         @"badge" : [NSNumber numberWithBool:notificationSettings.types & UIUserNotificationTypeBadge],
                                         @"alert" : [NSNumber numberWithBool:notificationSettings.types & UIUserNotificationTypeAlert],
                                         };
    [_channel invokeMethod:@"onIosSettingsRegistered" arguments:settingsDictionary];
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    const char *data = [deviceToken bytes];
    NSMutableString *ret = [NSMutableString string];
    for (NSUInteger i = 0; i < [deviceToken length]; i++) {
        [ret appendFormat:@"%02.2hhx", data[i]];
    }
    _lastToken = [ret copy];
    // createInstallationParse
    NSLog(@"token  " );
    [self createInstallationParse:deviceToken];
//    [self initParse:applicationId serverUrl:serverUrl ];
    [_channel invokeMethod:@"onToken" arguments: deviceToken ];
}

// - (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
//     NSLog(@"llego la notificacion en application: ");
//     [_channel invokeMethod:@"onMessage" arguments:userInfo];
//     [PFPush handlePush:userInfo];
// }

- (bool)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult result))completionHandler {
    NSLog(@"receive remote notification");
    [self didReceiveRemoteNotification:userInfo];
    completionHandler(UIBackgroundFetchResultNoData);
    return YES;
}

- (void)didReceiveRemoteNotification:(NSDictionary *)userInfo {
    if (_resumingFromBackground) {
        [_channel invokeMethod:@"onResume" arguments:userInfo];
    } else {
        [_channel invokeMethod:@"onMessage" arguments:userInfo];
    }
}
- (void)applicationDidEnterBackground:(UIApplication *)application {
    _resumingFromBackground = YES;
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    _resumingFromBackground = NO;
}

/*- (void)swizzled_application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
{
    [self swizzled_application:application didReceiveRemoteNotification:userInfo];
    
    if (application.applicationState != UIApplicationStateActive) {
        // The application was just brought from the background to the foreground,
        // so we consider the app as having been "opened by a push notification."
        [PFAnalytics trackAppOpenedWithRemoteNotificationPayload:userInfo];
    }
    //
    // PN can either be opened by user or received directly by app:
    // PN can only be received directly by app when app is running in foreground, UIApplicationStateActive.
    // PN that arrived when app is not running or in background (UIApplicationStateInactive or UIApplicationStateBackground)
    //    must be opened by user to reach this part of the code
//    ParsePushPlugin* pluginInstance = [self getParsePluginInstance];
    
//    [pluginInstance jsCallback:userInfo withAction:(application.applicationState == UIApplicationStateActive) ? @"RECEIVE" : @"OPEN"];
    NSLog(@"llego la notificacion  en swizzled application: ");
    [PFPush handlePush:userInfo];
    [_channel invokeMethod:@"onMessage" arguments:userInfo];
}*/

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
//    if (launchOptions != nil) {
//        launchNotification = (UILocalNotification *)[launchOptions objectForKey:UIApplicationLaunchOptionsLocalNotificationKey];
//        launchingAppFromNotification = launchNotification != nil;
//    }
    if (launchOptions != nil) {
        _launchNotification = launchOptions[UIApplicationLaunchOptionsRemoteNotificationKey];
    }
    return YES;
}


//- (void)handleSelectNotification:(NSString *)payload {
//    [_channel invokeMethod:@"selectNotification" arguments:payload];
//}

- (void)handleSelectNotification:(NSDictionary *)payload {
    [_channel invokeMethod:@"onLaunch" arguments:payload];
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center
didReceiveNotificationResponse:(UNNotificationResponse *)response
         withCompletionHandler:(void (^)(void))completionHandler NS_AVAILABLE_IOS(10.0) {
    if ([response.actionIdentifier isEqualToString:UNNotificationDefaultActionIdentifier]) {
        NSString *payload = (NSString *) response.notification.request.content.userInfo;
        if(initialized) {
//            [self handleSelectNotification:payload];
            [self handleSelectNotification:response.notification.request.content.userInfo];
        } else {
//            launchPayload = payload;
            _launchNotification = response.notification.request.content.userInfo;
            launchingAppFromNotification = true;
        }
    }
}

@end
