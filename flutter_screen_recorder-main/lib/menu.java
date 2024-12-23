import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'video.dart';
import 'commu.dart';
import 'gallery.dart';

class MenuPage extends StatefulWidget {
  @override
  _MenuPageState createState() => _MenuPageState();
}

class _MenuPageState extends State<MenuPage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black, // 배경색을 검정색으로 설정
      appBar: AppBar(
        title: Text(
          '메뉴',
          style: TextStyle(
            color: Colors.white, // 흰색
            fontWeight: FontWeight.bold, // 볼드체
          ),
        ),
        backgroundColor: Colors.black, // AppBar 배경색도 검정색으로 설정
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Image.asset(
              'assets/icon_main.png',
              width: 330, // 이미지의 너비를 더 크게 설정
            ),
            SizedBox(height: 30), // 이미지 아래에 공간 추가
            // 첫 번째 버튼: 화면 녹화
            ElevatedButton.icon(
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.white, // 버튼 배경색 흰색
                foregroundColor: Colors.black, // 버튼 글씨색 검정
                minimumSize: Size(250, 60), // 버튼 최소 크기 설정 (더 크게 설정)
                padding: EdgeInsets.symmetric(horizontal: 20), // 버튼 내부 여백 설정
              ),
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => VideoPage()), // VideoPage로 이동
                );
              },
              icon: Icon(Icons.videocam, size: 24), // 아이콘 추가 및 크기 설정
              label: Text(
                '화면 녹화',
                style: TextStyle(
                  fontSize: 20, // 글자 크기 설정
                  fontWeight: FontWeight.bold, // 볼드체 설정
                ),
              ),
            ),
            SizedBox(height: 20), // 버튼 사이 공간 추가
            // 두 번째 버튼: 갤러리
            ElevatedButton.icon(
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.white, // 버튼 배경색 흰색
                foregroundColor: Colors.black, // 버튼 글씨색 검정
                minimumSize: Size(250, 60), // 버튼 최소 크기 설정 (더 크게 설정)
                padding: EdgeInsets.symmetric(horizontal: 20), // 버튼 내부 여백 설정
              ),
              onPressed: () {
                // 갤러리 페이지로 이동
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => GalleryPage(videos: [])), // 빈 이미지 목록과 함께 GalleryPage로 이동
                );
              },
              icon: Icon(Icons.photo, size: 24), // 아이콘 추가 및 크기 설정
              label: Text(
                '업로드',
                style: TextStyle(
                  fontSize: 20, // 글자 크기 설정
                  fontWeight: FontWeight.bold, // 볼드체 설정
                ),
              ),
            ),
            SizedBox(height: 20), // 버튼 사이 공간 추가

            // 세 번째 버튼: 커뮤니티
            ElevatedButton.icon(
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.white, // 버튼 배경색 흰색
                foregroundColor: Colors.black, // 버튼 글씨색 검정
                minimumSize: Size(250, 60), // 버튼 최소 크기 설정 (더 크게 설정)
                padding: EdgeInsets.symmetric(horizontal: 20), // 버튼 내부 여백 설정
              ),
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => CommunityPage()), // CommunityPage로 이동
                );
              },
              icon: Icon(Icons.forum, size: 24), // 아이콘 추가 및 크기 설정
              label: Text(
                '커뮤니티',
                style: TextStyle(
                  fontSize: 20, // 글자 크기 설정
                  fontWeight: FontWeight.bold, // 볼드체 설정
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
