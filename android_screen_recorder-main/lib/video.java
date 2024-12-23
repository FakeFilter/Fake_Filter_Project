import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:ed_screen_recorder/ed_screen_recorder.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';
import 'TrimmerPage.dart';
import 'menu.dart';
import 'result.dart'; // 결과 페이지 import

class VideoPage extends StatefulWidget {
  const VideoPage({Key? key}) : super(key: key);

  @override
  State<VideoPage> createState() => _VideoPageState();
}

class _VideoPageState extends State<VideoPage> {
  EdScreenRecorder? screenRecorder;
  bool isUploading = false; // 업로드 상태를 관리
  late File selectedVideoFile; // 선택된 동영상 파일을 저장
  late String analysisResult; // 분석 결과를 저장

  @override
  void initState() {
    super.initState();
    screenRecorder = EdScreenRecorder();
  }
  Future<void> startRecord({required String fileName}) async {
    Directory tempDir = await getApplicationDocumentsDirectory();
    String tempPath = tempDir.path;
    print("Temporary Path: $tempPath");
    print("Recording Started");
    try {
      await screenRecorder?.startRecordScreen(
        fileName: fileName,
        dirPathToSave: tempPath,
        audioEnable: true,
      );
    } on PlatformException catch (e) {
      debugPrint("Error starting recording: $e");
    }
  }

  Future<void> stopRecord() async {
    try {
      setState(() {
        isUploading = true; // 로딩 상태 활성화
      });

      var responseRecord = await screenRecorder?.stopRecord();
      var filePath = responseRecord?['file'];
      if (filePath != null) {
        File videoFile = filePath; // 동영상 파일 객체 생성

        // 트리밍 페이지로 이동
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => TrimmerPage(
              videoFile: videoFile,
              onTrimComplete: (startTime, endTime) async {
                await _uploadTrimmedVideo(videoFile, startTime, endTime);
              },
            ),
          ),
        );
      }
    } catch (e) {
      setState(() {
        isUploading = false; // 로딩 상태 비활성화
      });
      print('오류 발생: $e');
    }
  }

  Future<void> _uploadTrimmedVideo(File videoFile, double startTime, double endTime) async {
    try {
      final url = Uri.parse('http://220.68.27.130:2201/uploader');
      final request = http.MultipartRequest('POST', url)
        ..fields['start_time'] = startTime.toString()
        ..fields['end_time'] = endTime.toString()
        ..files.add(
          await http.MultipartFile.fromPath(
            'video',
            videoFile.path,
          ),
        );

      final response = await request.send();
      if (response.statusCode == 200) {
        final responseString = await response.stream.bytesToString();
        final result = jsonDecode(responseString)['value'] ?? '결과 없음';
        setState(() {
          isUploading = false;
        });

        // 결과 페이지로 이동
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => ResultPage(
              videoFile: videoFile,
              result: result,
              fromPage: 'video',
            ),
          ),
        );
      } else {
        setState(() {
          isUploading = false;
        });
        _showDialog('실패', '동영상 업로드 실패');
      }
    } catch (e) {
      setState(() {
        isUploading = false;
      });
      print('오류 발생: $e');
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
          "화면 녹화",
          style: TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
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
      body: Stack(
        children: [
          Column(
            mainAxisSize: MainAxisSize.max,
            mainAxisAlignment: MainAxisAlignment.start,
            children: [
              SizedBox(height: 150),
              Center(
                child: ShaderMask(
                  shaderCallback: (bounds) => LinearGradient(
                    colors: [Colors.black, Colors.red],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ).createShader(bounds),
                  child: Icon(
                    Icons.videocam,
                    size: 140,
                    color: Colors.white,
                  ),
                ),
              ),
              SizedBox(height: 100),
              Center(
                child: ElevatedButton.icon(
                  onPressed: () => startRecord(fileName: "recording"),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.white,
                    foregroundColor: Colors.black,
                    minimumSize: Size(250, 60),
                    padding: EdgeInsets.symmetric(horizontal: 20),
                  ),
                  icon:
                  Icon(Icons.fiber_manual_record, color: Color(0xFF8B0000)),
                  label: Text(
                    '녹화 시작',
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),
              SizedBox(height: 20),
              Center(
                child: ElevatedButton.icon(
                  onPressed: () => stopRecord(),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.white,
                    foregroundColor: Colors.black,
                    minimumSize: Size(250, 60),
                    padding: EdgeInsets.symmetric(horizontal: 20),
                  ),
                  icon: Icon(Icons.stop_circle, color: Color(0xFF8B0000)),
                  label: Text(
                    '녹화 멈춤',
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),
            ],
          ),
          if (isUploading)
            Container(
              color: Colors.black.withOpacity(0.5), // 반투명 배경
              child: Center(
                child: CircularProgressIndicator(
                  color: Colors.red, // 로딩 색상
                ),
              ),
            ),
        ],
      ),
    );
  }
}
