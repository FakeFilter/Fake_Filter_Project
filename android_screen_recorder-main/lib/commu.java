import 'package:flutter/material.dart';
import 'menu.dart';
import 'register.dart';
import 'api.dart';
import 'change.dart'; // ChangePage import
import 'dart:convert';
import 'dart:typed_data';

class CommunityPage extends StatefulWidget {
  @override
  _CommunityPageState createState() => _CommunityPageState();
}

class _CommunityPageState extends State<CommunityPage> {
  List<Map<String, String>> posts = [];
  int currentPage = 1;
  int itemsPerPage = 10;
  Set<int> expandedPostIds = {};
  Map<int, String?> expandedPostContents = {};
  final ScrollController _scrollController = ScrollController();

  Future<void> loadPosts() async {
    try {
      List<Map<String, String>> loadedPosts = await fetchPosts();
      setState(() {
        posts = loadedPosts;
      });
    } catch (e) {
      print('Error loading posts: $e');
    }
  }

  Future<void> showPasswordDialog(int postId) async {
    TextEditingController passwordController = TextEditingController();

    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text('암호 입력'),
          content: TextField(
            controller: passwordController,
            obscureText: true,
            decoration: InputDecoration(labelText: '암호를 입력하세요'),
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.pop(context);
              },
              child: Text('취소'),
            ),
            TextButton(
              onPressed: () async {
                String inputPassword = passwordController.text;
                Navigator.pop(context);

                try {
                  // API 호출
                  bool isDeleted = await deletePost(postId, inputPassword);

                  if (isDeleted) {
                    // 삭제 성공: UI 상태 갱신
                    setState(() {
                      posts.removeWhere((post) => int.parse(post['id']!) == postId);
                    });
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('게시글이 삭제되었습니다.')),
                    );
                  } else {
                    // 암호 불일치
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('암호가 올바르지 않습니다.')),
                    );
                  }
                } catch (e) {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text('삭제 중 오류가 발생했습니다: $e')),
                  );
                }
              },
              child: Text('확인'),
            ),
          ],
        );
      },
    );
  }

  Future<void> togglePostExpansion(int postId) async {
    if (expandedPostIds.contains(postId)) {
      setState(() {
        expandedPostIds.remove(postId);
        expandedPostContents.remove(postId);
      });
    } else {
      try {
        Map<String, String?> postDetails = await fetchPostDetails(postId);
        setState(() {
          expandedPostIds.add(postId);
          expandedPostContents[postId] = postDetails['content'];
        });
      } catch (e) {
        print('Error loading post details: $e');
      }
    }
  }

  @override
  void initState() {
    super.initState();
    loadPosts();
  }

  @override
  Widget build(BuildContext context) {
    int totalPages = (posts.length / itemsPerPage).ceil();
    List<Map<String, String>> currentPosts = posts
        .skip((currentPage - 1) * itemsPerPage)
        .take(itemsPerPage)
        .toList();

    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        title: Text(
          '커뮤니티',
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
      body: Column(
        children: [
          Expanded(
            child: posts.isEmpty
                ? Center(
              child: Text(
                '등록된 내용이 없습니다.',
                style: TextStyle(
                  fontSize: 18,
                  color: Colors.grey,
                  fontWeight: FontWeight.bold,
                ),
              ),
            )
                : RawScrollbar(
              controller: _scrollController,
              thumbVisibility: true,
              thickness: 6,
              radius: Radius.circular(10),
              thumbColor: Colors.grey, // 스크롤바 색상을 회색으로 설정
              child: ListView.builder(
                controller: _scrollController,
                itemCount: currentPosts.length,
                itemBuilder: (context, index) {
                  final post = currentPosts[index];
                  final postId = int.parse(post['id']!);
                  final isExpanded = expandedPostIds.contains(postId);

                  return Padding(
                    padding: const EdgeInsets.symmetric(vertical: 5.0, horizontal: 15.0),
                    child: GestureDetector(
                      onTap: () => togglePostExpansion(postId),
                      child: Container(
                        decoration: BoxDecoration(
                          border: Border.all(color: Colors.white),
                          borderRadius: BorderRadius.circular(8),
                          color: Colors.grey[900],
                        ),
                        padding: const EdgeInsets.all(10.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Text(
                                  post['title']!,
                                  style: TextStyle(
                                    color: Colors.white,
                                    fontWeight: FontWeight.bold,
                                    fontSize: 18,
                                  ),
                                ),
                                Text(
                                  '${post['date']}',
                                  style: TextStyle(
                                    color: Colors.white,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ],
                            ),
                            if (isExpanded) ...[
                              SizedBox(height: 10),
                              FutureBuilder<Map<String, dynamic>?>(
                                future: getPostDetails(postId),
                                builder: (context, snapshot) {
                                  if (snapshot.connectionState == ConnectionState.waiting) {
                                    return Center(child: CircularProgressIndicator());
                                  }
                                  if (snapshot.hasError) {
                                    return Text(
                                      '이미지를 불러오는 중 오류가 발생했습니다.',
                                      style: TextStyle(color: Colors.red),
                                    );
                                  }
                                  if (snapshot.hasData && snapshot.data != null) {
                                    final String? base64Image = snapshot.data!['image_base64'];
                                    if (base64Image != null) {
                                      Uint8List imageBytes = base64Decode(base64Image);
                                      return Image.memory(imageBytes, height: 150, width: 150);
                                    } else {
                                      return Text(
                                        '이미지가 없습니다.',
                                        style: TextStyle(color: Colors.grey),
                                      );
                                    }
                                  }
                                  return Container();
                                },
                              ),
                              SizedBox(height: 10),
                              if (expandedPostContents[postId] != null)
                                Text(
                                  expandedPostContents[postId]!,
                                  style: TextStyle(
                                    color: Colors.white,
                                    fontWeight: FontWeight.normal,
                                  ),
                                ),
                              SizedBox(height: 10),
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                                children: [
                                  ElevatedButton(
                                    onPressed: () async {
                                      Map<String, dynamic>? postDetails = await getPostDetails(postId);
                                      if (postDetails != null) {
                                        Navigator.push(
                                          context,
                                          MaterialPageRoute(
                                            builder: (context) => ChangePage(
                                              postId: postId,
                                              title: postDetails['title'] ?? '',
                                              content: postDetails['content'] ?? '',
                                              password: postDetails['password'] ?? '',
                                            ),
                                          ),
                                        ).then((result) {
                                          if (result == true) {
                                            loadPosts(); // Reload posts after returning
                                          }
                                        });
                                      } else {
                                        ScaffoldMessenger.of(context).showSnackBar(
                                          SnackBar(content: Text('게시글 정보를 불러올 수 없습니다.')),
                                        );
                                      }
                                    },
                                    style: ElevatedButton.styleFrom(
                                      backgroundColor: Colors.blue,
                                      foregroundColor: Colors.white,
                                    ),
                                    child: Text('수정'),
                                  ),
                                  ElevatedButton(
                                    onPressed: () => showPasswordDialog(postId),
                                    style: ElevatedButton.styleFrom(
                                      backgroundColor: Colors.red,
                                      foregroundColor: Colors.white,
                                    ),
                                    child: Text('삭제'),
                                  ),
                                ],
                              ),
                            ],
                          ],
                        ),
                      ),
                    ),
                  );
                },
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(10.0),
            child: Column(
              children: [
                ElevatedButton(
                  onPressed: () async {
                    final result = await Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => RegisterPage(),
                      ),
                    );

                    if (result == true) {
                      await loadPosts();
                    }
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.white,
                    foregroundColor: Colors.black,
                    textStyle: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                    minimumSize: Size(150, 50),
                  ),
                  child: Text('등록'),
                ),
                SizedBox(height: 10),
                if (posts.isNotEmpty)
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: List.generate(totalPages, (index) {
                      return GestureDetector(
                        onTap: () {
                          setState(() {
                            currentPage = index + 1;
                          });
                        },
                        child: Container(
                          margin: EdgeInsets.symmetric(horizontal: 5),
                          padding: EdgeInsets.all(8),
                          decoration: BoxDecoration(
                            color: currentPage == index + 1
                                ? Colors.grey[400]
                                : Colors.grey[200],
                            borderRadius: BorderRadius.circular(4),
                            border: Border.all(color: Colors.black),
                          ),
                          child: Text(
                            '${index + 1}',
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              color: currentPage == index + 1
                                  ? Colors.black
                                  : Colors.black54,
                            ),
                          ),
                        ),
                      );
                    }),
                  ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
