import 'dart:typed_data';
import 'dart:ui' as ui; // RenderRepaintBoundary 관련 기능
import 'dart:io'; // File 관련 기능
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:gallery_saver/gallery_saver.dart';
import 'package:video_player/video_player.dart';
import 'package:url_launcher/url_launcher.dart';
import 'video.dart';
import 'gallery.dart';
import 'menu.dart';
import 'package:path_provider/path_provider.dart'; // 경로 관련 메서드 제공

class ResultPage extends StatefulWidget {
  final File videoFile;
  final String result;
  final String fromPage; // 추가된 매개변수

  ResultPage({
    required this.videoFile,
    required this.result,
    required this.fromPage, // 호출된 페이지 정보
  });

  @override
  _ResultPageState createState() => _ResultPageState();
}

class _ResultPageState extends State<ResultPage> {
  late VideoPlayerController _controller;
  final GlobalKey _repaintBoundaryKey = GlobalKey(); // 캡처 대상 영역의 GlobalKey

  @override
  void initState() {
    super.initState();
    _controller = VideoPlayerController.file(widget.videoFile)
      ..initialize().then((_) {
        setState(() {});
      });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _saveToGallery() async {
    try {
      // RepaintBoundary에서 이미지 캡처
      RenderRepaintBoundary boundary = _repaintBoundaryKey.currentContext!
          .findRenderObject() as RenderRepaintBoundary;
      ui.Image image = await boundary.toImage(pixelRatio: 3.0); // 고해상도 캡처
      ByteData? byteData =
      await image.toByteData(format: ui.ImageByteFormat.png);
      if (byteData != null) {
        Uint8List pngBytes = byteData.buffer.asUint8List();

        // 임시 파일에 저장
        final directory = await getTemporaryDirectory();
        final imagePath = '${directory.path}/captured_image.png';
        final imageFile = File(imagePath);
        await imageFile.writeAsBytes(pngBytes);

        // gallery_saver를 사용하여 갤러리에 저장
        await GallerySaver.saveImage(imageFile.path).then((bool? success) {
          if (success != null && success) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text("이미지가 갤러리에 저장되었습니다.")),
            );
          } else {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text("갤러리에 이미지를 저장하지 못했습니다.")),
            );
          }
        });
      }
    } catch (e) {
      print("저장 오류: $e");
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("이미지 저장에 실패했습니다.")),
      );
    }
  }

  Future<void> _launchURL() async {
    const url = 'https://ecrm.police.go.kr/minwon/main';
    final Uri uri = Uri.parse(url);

    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    } else {
      throw 'Could not launch $url';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(
          'AI 분석 결과',
          style: TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold, // 볼드체
          ),
        ),
        backgroundColor: Colors.black,
        leading: IconButton(
          icon: Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () {
            Navigator.pushAndRemoveUntil(
                context,
                MaterialPageRoute(builder: (context) => MenuPage()),
            (route) => false,
            );
          },
        ),
      ),
      backgroundColor: Colors.black,
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            RepaintBoundary(
              key: _repaintBoundaryKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  if (_controller.value.isInitialized)
                    Stack(
                      alignment: Alignment.bottomRight, // 오른쪽 아래에 버튼 배치
                      children: [
                        Container(
                          height: 500, // 원하는 높이 설정
                          width: double.infinity,
                          child: FittedBox(
                            fit: BoxFit.contain, // 원본 비율 유지
                            child: SizedBox(
                              width: _controller.value.size.width,
                              height: _controller.value.size.height,
                              child: VideoPlayer(_controller),
                            ),
                          ),
                        ),
                        Positioned(
                          bottom: 10,
                          right: 10,
                          child: FloatingActionButton(
                            onPressed: () {
                              setState(() {
                                _controller.value.isPlaying
                                    ? _controller.pause()
                                    : _controller.play();
                              });
                            },
                            backgroundColor: Color(0xFF8B0000), // 색상 변경
                            child: Icon(
                              _controller.value.isPlaying
                                  ? Icons.pause
                                  : Icons.play_arrow,
                              size: 20, // 아이콘 크기
                            ),
                            mini: true, // 버튼 크기 줄이기
                          ),
                        ),
                      ],
                    )
                  else
                    Center(child: CircularProgressIndicator()),
                  SizedBox(height: 20),
                  // 결과 카드
                  SizedBox(
                    width: double.infinity, // 가로를 화면에 꽉 차게 설정
                    child: Card(
                      color: Colors.white, // 흰색 배경
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10),
                        side: BorderSide(color: Colors.black, width: 1), // 검정 테두리
                      ),
                      child: Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Text(
                          widget.result, // 서버에서 받은 결과만 표시
                          style: TextStyle(
                            color: Color(0xFF8B0000), // 사용자 지정 색상
                            fontSize: 20,
                            fontWeight: FontWeight.bold, // 볼드체
                          ),
                          textAlign: TextAlign.center,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            SizedBox(height: 20),
            // 신고 버튼과 저장 버튼
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                // widget.result 값이 'DEEPFAKE'일 때만 신고 버튼 표시
                if (widget.result == 'DEEPFAKE')
                  ElevatedButton.icon(
                    onPressed: _launchURL, // 신고 버튼 클릭 시 URL 열기
                    icon: Icon(Icons.report, color: Colors.white), // 신고 아이콘
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.red, // 버튼 배경색
                      foregroundColor: Colors.white, // 버튼 텍스트 색
                      minimumSize: Size(100, 50), // 버튼 크기
                    ),
                    label: Text(
                      '신고',
                      style: TextStyle(fontWeight: FontWeight.bold),
                    ),
                  ),
                ElevatedButton.icon(
                  onPressed: _saveToGallery,
                  icon: Icon(Icons.save_alt, color: Colors.white), // 저장 아이콘
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.green, // 버튼 배경색
                    foregroundColor: Colors.white, // 버튼 텍스트 색
                    minimumSize: Size(100, 50), // 버튼 크기
                  ),
                  label: Text(
                    '저장',
                    style: TextStyle(fontWeight: FontWeight.bold),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
