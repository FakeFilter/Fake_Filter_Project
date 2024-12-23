import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'api.dart'; // insertPost 메서드가 정의된 파일 import

class RegisterPage extends StatefulWidget {
  @override
  _RegisterPageState createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {
  XFile? imageFile;
  final _titleController = TextEditingController(); // 제목 입력 필드 컨트롤러
  final _contentController = TextEditingController(); // 내용 입력 필드 컨트롤러
  final _passwordController = TextEditingController(); // 비밀번호 입력 필드 컨트롤러
  final ScrollController _scrollController = ScrollController(); // 스크롤 컨트롤러

  // 사진 선택 함수
  Future<void> _pickImage() async {
    final picker = ImagePicker();
    final pickedFile = await picker.pickImage(source: ImageSource.gallery);

    if (pickedFile != null) {
      setState(() {
        imageFile = pickedFile;
      });
    }
  }

  // 게시물 등록 함수
  Future<void> _submitPost() async {
    final title = _titleController.text; // 제목 입력값 가져오기
    final content = _contentController.text; // 내용 입력값 가져오기
    final password = _passwordController.text; // 비밀번호 입력값 가져오기

    // 입력 값 검증
    if (title.isEmpty || content.isEmpty || password.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('모든 필드를 입력해 주세요!')),
      );
      return;
    }

    try {
      // API 호출
      await insertPost(title, content, password, imageFile);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('게시물이 성공적으로 등록되었습니다.')),
      );
      Navigator.pop(context, true); // 성공적으로 등록 후 이전 화면으로 돌아가기
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('게시물 등록 중 오류가 발생했습니다: $e')),
      );
    }
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      resizeToAvoidBottomInset: true,
      appBar: AppBar(
        title: Text(
          '게시물 작성',
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
        controller: _scrollController, // 스크롤 컨트롤러 연결
        thumbColor: Colors.grey, // 스크롤바 색상
        radius: Radius.circular(10), // 스크롤바 끝부분 라운드 처리
        thickness: 6, // 스크롤바 두께
        thumbVisibility: true, // 항상 스크롤바 표시
        child: SingleChildScrollView(
          controller: _scrollController, // 스크롤 컨트롤러 연결
          child: Padding(
            padding: const EdgeInsets.all(20.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                GestureDetector(
                  onTap: _pickImage,
                  child: Container(
                    height: 200,
                    decoration: BoxDecoration(
                      border: Border.all(color: Colors.white, width: 2),
                      borderRadius: BorderRadius.circular(10),
                      color: Colors.grey[800],
                    ),
                    child: Stack(
                      children: [
                        Center(
                          child: imageFile == null
                              ? Column(
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
                          )
                              : Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Icon(Icons.check_circle,
                                  size: 50, color: Colors.green),
                              SizedBox(height: 10),
                              Text(
                                '사진이 업로드되었습니다.',
                                style: TextStyle(
                                  color: Colors.white,
                                  fontWeight: FontWeight.bold,
                                  fontSize: 16,
                                ),
                                textAlign: TextAlign.center,
                              ),
                            ],
                          ),
                        ),
                        if (imageFile != null) // X 버튼 추가
                          Positioned(
                            top: 8,
                            right: 8,
                            child: GestureDetector(
                              onTap: () {
                                setState(() {
                                  imageFile = null; // 이미지 초기화
                                });
                              },
                              child: Container(
                                decoration: BoxDecoration(
                                  shape: BoxShape.circle,
                                  color: Colors.red,
                                ),
                                padding: EdgeInsets.all(4),
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
                ),
                SizedBox(height: 20),
                TextField(
                  controller: _titleController, // 제목 컨트롤러 연결
                  decoration: InputDecoration(
                    labelText: '제목',
                    labelStyle: TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                    ),
                    border: OutlineInputBorder(),
                    enabledBorder: OutlineInputBorder(
                      borderSide: BorderSide(color: Colors.white),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderSide: BorderSide(
                        color: Color(0xFF8B0000),
                        width: 2.0,
                      ),
                    ),
                  ),
                  cursorColor: Color(0xFF8B0000),
                  style: TextStyle(color: Colors.white),
                ),
                SizedBox(height: 20),
                TextField(
                  controller: _contentController, // 내용 컨트롤러 연결
                  maxLines: 5,
                  decoration: InputDecoration(
                    labelText: '글쓰기',
                    labelStyle: TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                    ),
                    border: OutlineInputBorder(),
                    enabledBorder: OutlineInputBorder(
                      borderSide: BorderSide(color: Colors.white),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderSide: BorderSide(
                        color: Color(0xFF8B0000),
                        width: 2.0,
                      ),
                    ),
                  ),
                  cursorColor: Color(0xFF8B0000),
                  style: TextStyle(color: Colors.white),
                ),
                SizedBox(height: 20),
                TextField(
                  controller: _passwordController, // 비밀번호 컨트롤러 연결
                  decoration: InputDecoration(
                    labelText: '암호',
                    labelStyle: TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                    ),
                    border: OutlineInputBorder(),
                    enabledBorder: OutlineInputBorder(
                      borderSide: BorderSide(color: Colors.white),
                    ),
                    focusedBorder: OutlineInputBorder(
                      borderSide: BorderSide(
                        color: Color(0xFF8B0000),
                        width: 2.0,
                      ),
                    ),
                  ),
                  cursorColor: Color(0xFF8B0000),
                  style: TextStyle(color: Colors.white),
                  obscureText: true,
                ),
                SizedBox(height: 20),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    ElevatedButton(
                      onPressed: _submitPost, // 등록 버튼 클릭 시 호출
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.white,
                        foregroundColor: Colors.black,
                      ),
                      child: Text(
                        '등록',
                        style: TextStyle(fontWeight: FontWeight.bold),
                      ),
                    ),
                    ElevatedButton(
                      onPressed: () {
                        Navigator.pop(context);
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.white,
                        foregroundColor: Colors.black,
                      ),
                      child: Text(
                        '취소',
                        style: TextStyle(fontWeight: FontWeight.bold),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
