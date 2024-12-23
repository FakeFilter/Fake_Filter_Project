import 'package:flutter/material.dart';
import 'package:flutter_xlider/flutter_xlider.dart';
import 'package:screen_recorder/video.dart';
import 'package:video_player/video_player.dart';
import 'dart:io';

class TrimmerPage extends StatefulWidget {
  final File videoFile;
  final Function(double startTime, double endTime) onTrimComplete;

  const TrimmerPage({Key? key, required this.videoFile, required this.onTrimComplete}) : super(key: key);

  @override
  _TrimmerPageState createState() => _TrimmerPageState();
}

class _TrimmerPageState extends State<TrimmerPage> {
  late VideoPlayerController _controller;
  double _videoDuration = 0;
  double _startTime = 0;
  double _endTime = 0;

  @override
  void initState() {
    super.initState();
    _controller = VideoPlayerController.file(widget.videoFile)
      ..initialize().then((_) {
        setState(() {
          _videoDuration = _controller.value.duration.inSeconds.toDouble();
          _endTime = _videoDuration;
        });
        _controller.play();
      });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(
          '동영상 길이 조정',
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
              MaterialPageRoute(builder: (context) => VideoPage()),
                  (route) => false,
            );
          },
        ),
      ),
      body: Container(
        color: Colors.black, // 배경색 검정
        child: Column(
          children: [
            if (_controller.value.isInitialized)
              Expanded(
                flex: 2, // 동영상이 2/3를 차지하도록 설정
                child: Center(
                  child: AspectRatio(
                    aspectRatio: _controller.value.aspectRatio, // 동영상 비율 유지
                    child: VideoPlayer(_controller),
                  ),
                ),
              ),
            Expanded(
              flex: 1, // 슬라이더와 버튼이 나머지 1/3 차지
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  FlutterSlider(
                    values: [_startTime, _endTime],
                    rangeSlider: true,
                    max: _videoDuration,
                    min: 0,
                    onDragging: (handlerIndex, lowerValue, upperValue) {
                      setState(() {
                        _startTime = lowerValue;
                        _endTime = upperValue;
                      });
                      if (handlerIndex == 0) {
                        _controller.seekTo(Duration(seconds: _startTime.toInt()));
                      }
                    },
                  ),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          '시작: ${_startTime.toStringAsFixed(1)} 초',
                          style: TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        Text(
                          '종료: ${_endTime.toStringAsFixed(1)} 초',
                          style: TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                  ),
                  ElevatedButton(
                    onPressed: () {
                      widget.onTrimComplete(_startTime, _endTime);
                      Navigator.pop(context);
                    },
                    child: Text(
                      '검사 시작',
                      style: TextStyle(
                        color: Colors.black,
                        fontWeight: FontWeight.bold,
                        fontSize: 18,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
