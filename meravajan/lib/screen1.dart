import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';

class Screen1 extends StatelessWidget {
  const Screen1({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: App1(),
    );
  }
}


class App1 extends StatefulWidget {
  const App1({super.key});

  @override
  State<App1> createState() => _App1State();
}

class _App1State extends State<App1> {
  @override
  void initState() {
    super.initState();
    getdata();
    receive();
  }

  @override
  void dispose() {
    controller.dispose();
    connector("disconnect");
    super.dispose();
  }


  var data1 = "";

  var name ="";

  getdata()async{
    SharedPreferences sharedPref = await SharedPreferences.getInstance();
    name = sharedPref.getString("name")!;
  }


  var channel = MethodChannel("com.example.bluet/blue");

  connector(String a)async{
    await channel.invokeMethod(a,{"value":controller.text});
  }


  TextEditingController controller = TextEditingController(text: "12");

  receive()async{
    var data = await channel.invokeMethod("receive");
    setState(() {
      data1 = data;
    });
  }
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(),
      body: ListView(
        children: [
          ElevatedButton(onPressed: (){
            connector("connect");
          }, child: Text("Connect",style: TextStyle(fontSize: 30)),
            style: ButtonStyle(backgroundColor: WidgetStatePropertyAll(Colors.green)),),
          ElevatedButton(onPressed: (){
            connector("disconnect");
          }, child: Text("Disconnect",style: TextStyle(fontSize: 30,color: Colors.white,fontWeight:FontWeight.bold)),
            style: ButtonStyle(backgroundColor: WidgetStatePropertyAll(Colors.red)),),

          Padding(
            padding: const EdgeInsets.all(8.0),
            child: TextField(
              controller: controller,
              decoration: InputDecoration(
                border: OutlineInputBorder(),
              ),
            ),
          ),
          ElevatedButton(onPressed: (){
            connector("send");
            Timer(Duration(seconds: 5),() => {
              receive()
            },);
          }, child: Text("Get data",style: TextStyle(fontSize: 30),)),
          Text("Weight ",style: TextStyle(fontSize: 30,fontWeight:FontWeight.bold),),
          Text(data1,style: TextStyle(fontSize: 60),textAlign: TextAlign.center,),
        ],
      ),
      floatingActionButton: FloatingActionButton(onPressed: (){
        receive();
      }),
    );
  }
}
