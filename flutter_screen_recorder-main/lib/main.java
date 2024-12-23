import 'dart:async'; // Timer를 사용하기 위해 필요
import 'package:flutter/material.dart';
import 'menu.dart'; // menu.dart 파일을 import

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        debugShowCheckedModeBanner: false,
      home: SplashScreen(), // 처음 실행할 때 SplashScreen이 보이도록 설정
    );
  }
}

class SplashScreen extends StatefulWidget {
  @override
  _SplashScreenState createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  @override
  void initState() {
    super.initState();
    // 3초 후에 menu.dart로 화면 이동
    Timer(Duration(seconds: 3), () {
      Navigator.pushReplacement(
        context,
        MaterialPageRoute(builder: (context) => MenuPage()),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black, // 배경색을 검정색으로 설정
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Padding(
              padding: EdgeInsets.symmetric(horizontal: 20.0), // 좌우에 20픽셀 여백 추가
              child: Image.asset(
                'assets/icon_main.png',
                width: 400, // 이미지의 너비를 250으로 설정 (더 크게)
                //height: 250, // 필요에 따라 높이도 설정할 수 있음
              ),
            ),
            SizedBox(height: 90), // 이미지와 로딩 애니메이션 사이의 공간을 늘림
            CircularProgressIndicator(
              valueColor: AlwaysStoppedAnimation<Color>(Colors.red), // 로딩 애니메이션 색을 빨간색으로 설정
              strokeWidth: 6.0, // 로딩 애니메이션의 두께 조정
            ),
            SizedBox(height: 20), // 로딩 애니메이션 아래에 추가 공간 설정
          ],
        ),
      ),
    );
  }
}
