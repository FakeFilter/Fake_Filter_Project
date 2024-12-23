import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'dart:io';

class ChangePage extends StatefulWidget {
  final int postId;
  final String title;
  final String content;
  final String password;
  final String? imagePath; // 서버에서 가져온 기존 이미지 경로

  ChangePage({
    required this.postId,
    required this.title,
    required this.content,
    required this.password,
    this.imagePath,
  });

  @override
  _ChangePageState createState() => _ChangePageState();
}

class _ChangePageState extends State<ChangePage> {
  final _titleController = TextEditingController();
  final _contentController = TextEditingController();
  final _passwordController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  File? _selectedImage; // 새로 선택된 이미지
  String? _currentImagePath; // 기존 이미지 경로

  @override
  void initState() {
    super.initState();
    _titleController.text = widget.title;
    _contentController.text = widget.content;
    _passwordController.text = widget.password;
    _currentImagePath = widget.imagePath; // 서버에서 전달받은 이미지 경로 저장
  }

  Future<void> _pickImage() async {
    final picker = ImagePicker();
    final pickedFile = await picker.pickImage(source: ImageSource.gallery);

    if (pickedFile != null) {
      setState(() {
        _selectedImage = File(pickedFile.path); // 새 이미지를 저장
        _currentImagePath = null; // 새 이미지를 선택했을 때 기존 경로 제거
      });
    }
  }

  Future<void> _updatePost() async {
    final title = _titleController.text;
    final content = _contentController.text;
    final password = _passwordController.text;

    if (title.isEmpty || content.isEmpty || password.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('모든 필드를 입력해 주세요!')),
      );
      return;
    }

    try {
      // API 호출을 통해 게시물 업데이트
      var response = await updatePost(
        postId: widget.postId,
        title: title,
        content: content,
        password: password,
        imageFile: _selectedImage, // 새 이미지가 선택된 경우
      );

      // 서버 응답 처리
      if (response['message'] == 'Post updated successfully') {  // 성공 조건 수정
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('게시물이 성공적으로 수정되었습니다.')),
        );
        Navigator.pop(context, true); // 수정 후 이전 화면으로 돌아가기
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('수정 실패: ${response['message']}')),
        );
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('게시물 수정 중 오류가 발생했습니다: $e')),
      );
      print('Error: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      resizeToAvoidBottomInset: true, // 키보드 올라오면 화면이 올라가게 설정
      appBar: AppBar(
        title: Text(
          '게시물 수정',
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
      backgroundColor: Colors.black,
      body: RawScrollbar(
        controller: _scrollController,
        thumbColor: Colors.grey,
        radius: Radius.circular(10),
        thickness: 6,
        thumbVisibility: true,
        child: SingleChildScrollView(
          controller: _scrollController,
          child: Padding(
            padding: const EdgeInsets.all(20.0),
            child: Column(
              children: [
                // 이미지 업로드 영역
                GestureDetector(
                  onTap: _pickImage,
                  child: Container(
                    height: 200,
                    decoration: BoxDecoration(
                      border: Border.all(color: Colors.white, width: 2),
                      borderRadius: BorderRadius.circular(10),
                      color: Colors.grey[800],
                    ),
                    child: Center(
                      child: _selectedImage != null
                          ? Image.file(
                        _selectedImage!,
                        fit: BoxFit.cover,
                        width: double.infinity,
                      )
                          : _currentImagePath != null
                          ? Image.network(
                        _currentImagePath!,
                        fit: BoxFit.cover,
                        width: double.infinity,
                      )
                          : Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.add_a_photo,
                              size: 50, color: Colors.white),
                          SizedBox(height: 10),
                          Text(
                            '사진 추가',
                            style: TextStyle(
                              color: Colors.white,
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
                SizedBox(height: 20),

                // 제목 입력 필드
                TextField(
                  controller: _titleController,
                  decoration: InputDecoration(
                    labelText: '제목',
                    labelStyle: TextStyle(color: Colors.white),
                    enabledBorder: OutlineInputBorder(
                      borderSide: BorderSide(color: Colors.white),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderSide: BorderSide(color: Color(0xFF8B0000)),
                    ),
                  ),
                  cursorColor: Color(0xFF8B0000),
                  style: TextStyle(color: Colors.white),
                ),
                SizedBox(height: 20),

                // 내용 입력 필드
                TextField(
                  controller: _contentController,
                  maxLines: 5,
                  decoration: InputDecoration(
                    labelText: '내용',
                    labelStyle: TextStyle(color: Colors.white),
                    enabledBorder: OutlineInputBorder(
                      borderSide: BorderSide(color: Colors.white),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderSide: BorderSide(color: Color(0xFF8B0000)),
                    ),
                  ),
                  cursorColor: Color(0xFF8B0000),
                  style: TextStyle(color: Colors.white),
                ),
                SizedBox(height: 20),

                // 암호 입력 필드
                TextField(
                  controller: _passwordController,
                  decoration: InputDecoration(
                    labelText: '암호',
                    labelStyle: TextStyle(color: Colors.white),
                    enabledBorder: OutlineInputBorder(
                      borderSide: BorderSide(color: Colors.white),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderSide: BorderSide(color: Color(0xFF8B0000)),
                    ),
                  ),
                  obscureText: true,
                  cursorColor: Color(0xFF8B0000),
                  style: TextStyle(color: Colors.white),
                ),
                SizedBox(height: 20),

                // 수정 완료 버튼
                ElevatedButton(
                  onPressed: _updatePost,
                  child: Text(
                    '수정 완료',
                    style: TextStyle(
                      color: Colors.black,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.white,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

// API 호출 함수 (PATCH 요청)
Future<Map<String, dynamic>> updatePost({
  required int postId,
  required String title,
  required String content,
  required String password,
  File? imageFile,
}) async {
  // 서버의 엔드포인트 URL
  final url = Uri.parse('http://220.68.27.130:2201/update/$postId');
  final request = http.MultipartRequest('PATCH', url); // PATCH 메서드 사용

  request.fields['title'] = title;
  request.fields['content'] = content;
  request.fields['password'] = password;

  if (imageFile != null) {
    request.files.add(await http.MultipartFile.fromPath('image', imageFile.path));
  }

  final response = await request.send();
  if (response.statusCode == 200) {
    final responseBody = await response.stream.bytesToString();
    return jsonDecode(responseBody);
  } else {
    throw Exception('Failed to update post: ${response.statusCode}');
  }
}
