import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:http/http.dart' as http;
import 'dart:io';
import 'result.dart'; // result.dart를 import

class GalleryPage extends StatefulWidget {
  final List<XFile> videos; // 갤러리에서 선택한 동영상 목록

  GalleryPage({Key? key, required this.videos}) : super(key: key);

  @override
  _GalleryPageState createState() => _GalleryPageState();
}

class _GalleryPageState extends State<GalleryPage> {
  final ImagePicker picker = ImagePicker(); // 동영상 선택기 인스턴스
  bool isVideoUploaded = false; // 동영상 업로드 상태 확인
  bool isLoading = false; // 로딩 상태
  late File selectedVideoFile; // 선택된 동영상 파일 저장
  late String analysisResult; // 분석 결과 저장

  Future<void> _pickVideo() async {
    final XFile? pickedVideo = await picker.pickVideo(source: ImageSource.gallery);
    if (pickedVideo != null) {
      setState(() {
        widget.videos.add(pickedVideo);
        isVideoUploaded = true; // 동영상 업로드 상태 변경
      });
    }
  }

  Future<void> _uploadVideo(File videoFile) async {
    final url = Uri.parse('http://220.68.27.130:2201/uploader_simple');
    setState(() {
      isLoading = true; // 로딩 시작
    });

    try {
      final request = http.MultipartRequest('POST', url)
        ..files.add(
          await http.MultipartFile.fromPath(
            'video', // 서버에서 받을 필드 이름
            videoFile.path,
          ),
        );

      final response = await request.send();
      if (response.statusCode == 200) {
        final resString = await response.stream.bytesToString();
        final resCheck = jsonDecode(resString);

        if (resCheck['existResult'] == 'true') {
          var result = resCheck['value'];
          print('결과: $result');

          // 검사 결과 페이지로 이동
          setState(() {
            isLoading = false; // 로딩 종료
          });
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => ResultPage(
                videoFile: videoFile, // 수정된 부분
                result: result,       // 수정된 부분
                fromPage: 'gallery',  // 호출된 페이지 정보 전달
              ),
            ),
          );
        } else {
          setState(() {
            isLoading = false; // 로딩 종료
          });
          print('결과 없음');
          _showDialog('알림', '동영상 분석 결과가 없습니다.');
        }
      } else {
        setState(() {
          isLoading = false; // 로딩 종료
        });
        print('동영상 업로드 실패: ${response.statusCode}');
        _showDialog('실패', '동영상 업로드에 실패했습니다.');
      }
    } catch (e) {
      setState(() {
        isLoading = false; // 로딩 종료
      });
      print('예외 발생: $e');
      _showDialog('오류', '동영상 업로드 중 오류가 발생했습니다.');
    }
  }


  void _showDialog(String title, String content) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text(title),
          content: Text(content),
          actions: [
            TextButton(
              child: Text('확인'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        title: Text(
          '업로드',
          style: TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
          ),
        ),
        backgroundColor: Colors.black,
        leading: IconButton(
          icon: Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () {
            Navigator.pop(context);
          },
        ),
      ),
      body: Stack(
        children: [
          Column(
            children: [
              Container(
                height: MediaQuery.of(context).size.height * 0.4,
                margin: EdgeInsets.all(16.0),
                decoration: BoxDecoration(
                  color: Colors.grey[800],
                  borderRadius: BorderRadius.circular(10.0),
                  border: Border.all(color: Colors.white, width: 2),
                ),
                child: Stack(
                  children: [
                    Center(
                      child: isVideoUploaded
                          ? Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.check_circle,
                              size: 50, color: Colors.green),
                          SizedBox(height: 10),
                          Text(
                            '동영상이 업로드되었습니다.',
                            style: TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              fontSize: 16,
                            ),
                          ),
                        ],
                      )
                          : Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.add, size: 50, color: Colors.white),
                          SizedBox(height: 10),
                          GestureDetector(
                            onTap: _pickVideo,
                            child: Text(
                              '동영상 추가',
                              style: TextStyle(
                                color: Colors.white,
                                fontWeight: FontWeight.bold,
                                fontSize: 16,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                    if (isVideoUploaded)
                      Align(
                        alignment: Alignment.topRight,
                        child: GestureDetector(
                          onTap: () {
                            setState(() {
                              widget.videos.clear();
                              isVideoUploaded = false; // 상태 초기화
                            });
                          },
                          child: Container(
                            margin: EdgeInsets.all(10), // 경계에서 여백 추가
                            decoration: BoxDecoration(
                              shape: BoxShape.circle,
                              color: Colors.red,
                            ),
                            padding: EdgeInsets.all(5),
                            child: Icon(
                              Icons.close,
                              color: Colors.white,
                              size: 20,
                            ),
                          ),
                        ),
                      ),
                  ],
                ),
              ),
              SizedBox(height: 16.0),
              Container(
                margin: EdgeInsets.symmetric(horizontal: 16.0),
                child: ElevatedButton(
                  onPressed: () {
                    if (widget.videos.isEmpty) {
                      _showDialog('경고', '동영상을 업로드 해주세요.');
                    } else {
                      final videoFile = File(widget.videos[0].path);
                      _uploadVideo(videoFile);
                    }
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.white,
                    minimumSize: Size(250, 50),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                  ),
                  child: Text(
                    '검사 시작',
                    style: TextStyle(
                      color: Colors.black,
                      fontWeight: FontWeight.bold,
                      fontSize: 18,
                    ),
                  ),
                ),
              ),
            ],
          ),
          if (isLoading)
            Container(
              color: Colors.black.withOpacity(0.5),
              child: Center(
                child: CircularProgressIndicator(
                  valueColor: AlwaysStoppedAnimation<Color>(Color(0xFF8B0000)),
                ),
              ),
            ),
        ],
      ),
    );
  }
}
