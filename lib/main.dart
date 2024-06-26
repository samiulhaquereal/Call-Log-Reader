import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: CallLogScreen(),
    );
  }
}

class CallLogScreen extends StatefulWidget {
  @override
  _CallLogScreenState createState() => _CallLogScreenState();
}

class _CallLogScreenState extends State<CallLogScreen> {
  static const platform = MethodChannel('call_log');
  List<Map<dynamic, dynamic>> callLogs = [];

  @override
  void initState() {
    super.initState();
    getCallLogs();
  }

  Future<void> getCallLogs() async {
    try {
      final List<dynamic> result = await platform.invokeMethod('getCallLogs');
      setState(() {
        callLogs = List<Map<dynamic, dynamic>>.from(result);
      });
    } on PlatformException catch (e) {
      print("Failed to get call logs: '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Call Logs'),
      ),
      body: ListView.builder(
        itemCount: callLogs.length,
        itemBuilder: (context, index) {
          return ListTile(
            title: Text(callLogs[index]['number']),
            subtitle: Text(
              'Type: ${callLogs[index]['type']}, Date: ${callLogs[index]['date']}, Duration: ${callLogs[index]['duration']}',
            ),
          );
        },
      ),
    );
  }
}
