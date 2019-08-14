import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:plain_notification_token/plain_notification_token.dart';
typedef Future<dynamic> MessageHandler(Map<String, dynamic> message);

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _pushToken = 'Unknown';
  IosNotificationSettings _settings;
  String _batteryLevel = 'Uknown batery level';
  String _typeMessage = "default";

  StreamSubscription onTokenRefreshSubscription;
  StreamSubscription onIosSubscription;
  var serverUrl = "https://hunter-www.jatytcmg8.at.d2c.io/parse/";
  var appId = "canal10";
  @override
  void initState() {
    super.initState();
    BuildContext context;
    final PlainNotificationToken _plainNotificationToken = PlainNotificationToken();

    onTokenRefreshSubscription = PlainNotificationToken().onTokenRefresh.listen((token) {
      setState(() {
        _pushToken = token;
      });
    });
    onIosSubscription = PlainNotificationToken().onIosSettingsRegistered.listen((settings) {
      setState(() {
        _settings = settings;
      });
    });
    
    
    _plainNotificationToken.configure(
      onMessage: (Map<String, dynamic> message) async {
        print("onMessage example: $message");
        setState(() {
          _typeMessage = "onMessage";
        });
        _navigateToItemDetail(context, message); 
        //app is runinng in foreground
          // _batteryLevel = "onMessage ";
        // return _onMessage(call.arguments.cast<String, dynamic>());
        // _showItemDialog(message);
      },
      onLaunch: (Map<String, dynamic> message) async {
        print("onLaunch example: " + message.toString());
        setState(() {
          _typeMessage = "onLaunch";
        });
        _navigateToItemDetail(context, message); 
        // _onLaunch(message);
        // _navigateToItemDetail(message);
        
      },
      onResume: (Map<String, dynamic> message) async {
        print("onResume example: $message");
        setState(() {
          _typeMessage = "onResume";
        });
        // _navigateToItemDetail(context ,message); 
        // _onResume(message);
      },
    );
    _plainNotificationToken.autoInitParse(serverUrl, appId);
  }

  void _navigateToItemDetail(context,Map<String, dynamic> message) {
    try {
      Item item;
      item = _itemForMessage(message);
      print("log item ***");
      print(item.title);
      print("log item ----");
      setState(() {
        _batteryLevel = item.noteId;
      });
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => FourScreen(item.noteId),
        ),
      );
    } catch (e) {
      print("ocurrio un error al momento del parseo");
      print(e);
    }
  }
  

  @override
  void dispose() {
    onTokenRefreshSubscription.cancel();
    onIosSubscription.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              Text('token: $_pushToken\n'),
              Text("settings: $_settings"),
              Text("tipo de mensaje: -> "+ _typeMessage),
              Text("mensage : -> "+ _batteryLevel),
              Builder(
                builder: (context) => RaisedButton(
                  child: Text("Request permission"),
                  onPressed: () {
                    PlainNotificationToken().requestPermission();
                  },
                ),
              )
            ],
          ),
        ),
        floatingActionButton: Builder(
          builder: (context) => FloatingActionButton(
                child: Icon(Icons.search),
                onPressed: () async {
                  String token;
                  // Platform messages may fail, so we use a try/catch PlatformException.
                  try {
                    token = await PlainNotificationToken().getToken();
                  } on PlatformException {
                    token = 'Failed to get platform version.';
                  }
                  Scaffold.of(context)
                      .showSnackBar(SnackBar(content: Text(token ?? "(no token yet)")));
                },
              ),
        ),
      ),
    );
  }
}

// final Map<String, Item> _items = <String, Item>{};
Item _itemForMessage(Map<String, dynamic> message) {
  print("llego al item for Message");
  // final dynamic data = message['data'] ?? message;

  final String title = message['title'] ?? '';
  final String noteId = message['note'] ?? '';
  // final Item item = _items.putIfAbsent(title, () => Item(title: title,noteId: noteId));
    // ..status = data['status'];
  final Item item = Item(title: title, noteId: noteId);
  print("hola");
  return item;
}

class Item {
  Item({this.title, this.noteId});
  final String title;
  final String noteId;

  StreamController<Item> _controller = StreamController<Item>.broadcast();
  Stream<Item> get onChanged => _controller.stream;

  String _status;
  String get status => _status;
  set status(String value) {
    _status = value;
    _controller.add(this);
  }

  static final Map<String, Route<void>> routes = <String, Route<void>>{};
  // Route<void> get route {
  //   final String routeName = '/detail/$noteId';
  //   return routes.putIfAbsent(
  //     routeName,
  //     () => MaterialPageRoute<void>(
  //       settings: RouteSettings(name: routeName),
  //       builder: (BuildContext context) => FourScreen(noteId),
  //     ),
  //   );
  // }

}

class FourScreen extends StatefulWidget {
  final String payload;
  FourScreen(this.payload);
  @override
  State<StatefulWidget> createState() => FourScreenState();
}

class FourScreenState extends State<FourScreen> {
  String _payload;
  @override
  void initState() {
    super.initState();
    _payload = widget.payload;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Four Screen"),
      ),
      body: Center(
        child: RaisedButton(
          onPressed: () {
            Navigator.pop(context);
          },
          child: Column(
            children: <Widget>[
              Text(_payload),
              Text('Go back!'),
            ],
          ) 
        ),
      ),
    );
  }
}

