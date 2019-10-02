import 'dart:async';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:plain_notification_token/plain_notification_token.dart';
import 'dart:convert';
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
  String _title = "";

  StreamSubscription onTokenRefreshSubscription;
  StreamSubscription onIosSubscription;
  var serverUrl = "https://parseapi.back4app.com/";
  var appId = "xlX7CBM2dolI89gS8KeSymxdXaLPlPh2xrv9vPye";
  var clientKey = "aOgAQ4zg29o9hdYy8SFNFCUcnXWXUbIoijh5qq2R";
  
  // veracidad channel 2da app
  // var appId = "bY3VMZxyuBEqmcqiv29PCjDn7YAfyBitRjug8Alh";
  // var clientKey = "UFHWP0j1ysPxlsjgrMZyq1G9QOciZq0b68IT30qn";
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

        // print(message.containsKey('data'));
        // print(message['data']);
        if (Platform.isAndroid) {
          Map<String, dynamic> newMap = jsonDecode(message['data']);
          print(newMap);
          _navigateToItemDetail(context, newMap);
        }
        if (Platform.isIOS) {
          _navigateToItemDetail(context, message);
        }
        
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
        if (Platform.isAndroid) {
          Map<String, dynamic> newMap = jsonDecode(message['data']);
          print(newMap);
          _navigateToItemDetail(context, newMap);
        }
        if (Platform.isIOS) {
          _navigateToItemDetail(context, message);
        }
        
        // _onLaunch(message);
        // _navigateToItemDetail(message);

      },
      onResume: (Map<String, dynamic> message) async {
        print("onResume example: "+ message.toString());
        setState(() {
          _typeMessage = "onResume";
        });
        if (Platform.isAndroid) {
          Map<String, dynamic> newMap = jsonDecode(message['data']);
          print(newMap);
          _navigateToItemDetail(context, newMap);
        }
        if (Platform.isIOS) {
          _navigateToItemDetail(context, message);
        }

        // _onResume(message);
      },
    );
    _plainNotificationToken.autoInitParse(serverUrl, appId, clientKey);
  }

  void _navigateToItemDetail(context,Map<String, dynamic> message) {
    try {
      print("hola, llego al  _navigateToItemDetail");
      Item item;
      if (Platform.isAndroid) {
        item = _itemForMessage(message);
      } else {
        item = _itemForMessage(message);
      }
      print("log item ***");
      print(item.title);
      print("log item ----");
      setState(() {
        _batteryLevel = item.noteId;
        _title = item.title;
      });
      // Navigator.push(
      //   context,
      //   MaterialPageRoute(
      //     builder: (context) => FourScreen(item.noteId),
      //   ),
      // );
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
              Text("title : -> "+ _title),
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
  String title = "";
  String noteId = "";
  if (Platform.isAndroid) {
    print("is android");
    print(message.toString());
    // Map<String, dynamic> pnData = message["data"];
    // print( pnData.toString());
    // print( pnData["title"].toString());
    // //noteId = data['note'] ?? '';
    // //message.map()
    // //final jsonResponse = json.decode(message.);
    title = message["title"] ?? '';
    noteId = message["note"].toString() ?? '';
  }
  if (Platform.isIOS) {
    print("is ios");
    title = message['title'] ?? '';
    noteId = message['note'] ?? '';
  }
  // final dynamic data = message['data'] ?? message;

  // final Item item = _items.putIfAbsent(title, () => Item(title: title,noteId: noteId));
    // ..status = data['status'];
  final Item item = Item(title: title, noteId: noteId);
  print("hola");
  print("item.noteId => "+ item.noteId);
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
