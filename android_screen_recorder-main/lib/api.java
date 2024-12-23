import 'dart:io';

import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:image_picker/image_picker.dart';
import 'dart:convert';
import 'package:path/path.dart';

Future<void> insertPost(String title, String content, String password, XFile? imageFile) async {
  final uri = Uri.parse('http://220.68.27.130:2201/insert'); // 서버 URL

  final request = http.MultipartRequest('POST', uri);

  // 필드 추가
  request.fields['title'] = title;
  request.fields['content'] = content;
  request.fields['password'] = password;

  // 이미지 파일 추가
  if (imageFile != null) {
    final file = File(imageFile.path);
    request.files.add(await http.MultipartFile.fromPath('image', file.path));
  }

  // 요청 전송 및 응답 확인
  final response = await request.send();
  if (response.statusCode == 201) {
    print('Post created successfully');
  } else {
    throw Exception('Failed to create post: ${response.reasonPhrase}');
  }
}

Future<int?> getMaxPostId() async {
  final url = Uri.parse('http://220.68.27.130:2201/max_post_id');
  final response = await http.get(url);

  if (response.statusCode == 200) {
    final data = jsonDecode(response.body);
    return data['max_post_id'];
  } else {
    print('Error: ${response.body}');
    return null;
  }
}


Future<Map<String, dynamic>?> getPost(int postId) async {
  final url = Uri.parse('http://220.68.27.130:2201/post/$postId');
  final response = await http.get(url);

  if (response.statusCode == 200) {
    return jsonDecode(response.body);
  } else {
    print('Error: ${response.body}');
    return null;
  }
}


Future<Map<String, dynamic>?> getPostDetails(int postId) async {
  final url = Uri.parse('http://220.68.27.130:2201/post/$postId/details');
  final response = await http.get(url);

  if (response.statusCode == 200) {
    final data = jsonDecode(response.body);
    return data;
  } else {
    print('Error: ${response.body}');
    return null;
  }
}

//-도현- 게시물 삭제
Future<bool> deletePost(int postId, String inputPassword) async {
  final url = Uri.parse('http://220.68.27.130:2201/delete/$postId');
  try {
    final response = await http.delete(
      url,
      headers: {"Content-Type": "application/json"},
      body: jsonEncode({
        'password': inputPassword,
      }),
    );

    if (response.statusCode == 200) {
      final result = jsonDecode(response.body);
      return result['success']; // 서버 응답에서 성공 여부 확인
    } else {
      print('Error: ${response.body}');
      return false;
    }
  } catch (e) {
    print('Error deleting post: $e');
    throw Exception('삭제 중 오류가 발생했습니다.');
  }
}

Future<Map<String, dynamic>> updatePost({
  required int postId,
  required String title,
  required String content,
  required String password,
  File? imageFile,
}) async {
  final url = Uri.parse('http://220.68.27.130:2201/update/$postId');
  final request = http.MultipartRequest('PATCH', url);

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





//-도현- 게시글 수정할때 서버에서 데이터 가져오는 부분
Future<List<Map<String, String>>> fetchPosts() async {
  List<Map<String, String>> posts = [];
  try {
    int? maxPostId = await getMaxPostId();
    if (maxPostId != null) {
      for (int i = 1; i <= maxPostId; i++) {
        Map<String, dynamic>? postDetails = await getPost(i);
        if (postDetails != null) {
          posts.add({
            'id': i.toString(),
            'title': postDetails['title'] ?? '제목 없음',
            'date': postDetails['created_date'] ?? '알 수 없음',
          });
        }
      }
    }
  } catch (e) {
    print('Error loading posts: $e');
  }
  return posts;
}
// -도현- 게시글 수정할 때 특정 게시물의 세부 정보를 가져오는 부분
Future<Map<String, String?>> fetchPostDetails(int postId) async {
  Map<String, String?> postDetails = {};
  try {
    Map<String, dynamic>? details = await getPostDetails(postId);
    if (details != null) {
      postDetails = {
        'content': details['content'] ?? '내용 없음',
        'video_path': details['video']
      };
    } else {
      postDetails = {
        'content': '내용을 불러올 수 없습니다.',
      };
    }
  } catch (e) {
    print('Error loading post details: $e');
    postDetails = {
      'content': '내용을 불러오는 중 오류가 발생했습니다.',
    };
  }
  return postDetails;
}

Future<void> uploadVideo(String filePath) async {
  final url = Uri.parse('http://220.68.27.130:2201/uploader');
  final request = http.MultipartRequest('POST', url);

  request.files.add(await http.MultipartFile.fromPath('video', filePath));

  final response = await request.send();
  if (response.statusCode == 200) {
    final responseData = await response.stream.bytesToString();
    print('Upload successful: $responseData');
  } else {
    print('Error: ${response.statusCode}');
  }
}
