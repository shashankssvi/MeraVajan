import 'package:flutter/material.dart';
import 'package:meravajan/screen1.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: const MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  setdata()async{
    SharedPreferences sharedPref = await SharedPreferences.getInstance();
    sharedPref.setString("name", name);

  }

  var data1 = "";

  var name ="";

  @override
  Widget build(BuildContext context) {

    return Scaffold(
      appBar: AppBar(
        title: Text("Mera Vajan"),
      ),
      body: ListView(
        children: [
          TextField(
            onChanged: (value){

            },
          ),
          ElevatedButton(onPressed: ()async{
            await setdata();
            Navigator.push(context, MaterialPageRoute(builder: (context)=>App1()));
          }, child: Text("Next"),
          ),
        ],
      ),
    );
  }
}
