
from flask import Flask , request, jsonify, send_from_directory
import json
import sys
import os
import zlib
import cv2 as cv
import dbconnect as db  # db 전반적인 기능 처리
from werkzeug.utils import secure_filename
from predict_folder2 import predict
import base64
import shutil
#from flask_mail import Mail, Message

app = Flask(__name__)

from flask import request,  jsonify
app.secret_key = ''

@app.route('/')    
def homepage():
    return  jsonify({'success': False})


@app.route('/insert', methods=['POST'])
def insert_post():
    """
    새로운 게시물을 삽입하는 POST 요청 처리 메소드.
    """
    try:
        # 클라이언트에서 데이터 가져오기
        data = request.form
        title = data.get('title')
        content = data.get('content')
        password = data.get('password')
        image_file = request.files.get('image')  # 이미지 파일 가져오기

        if not title or not content or not password:
            return jsonify({"error": "All fields (title, content, password) are required"}), 400

        # 데이터베이스에서 next_post_id 생성
        max_post_id = db.select_max_post_id() or 0
        next_post_id = max_post_id + 1

        # 이미지 저장 경로 생성
        image_path = f"E:/android_project/dfdc_deepfake_challenge/community_data/{next_post_id}.png"
        if image_file:
            image_file.save(image_path)

        # 데이터베이스에 데이터 삽입
        db.insert_data(title, content, password, image_path)

        return jsonify({
            "message": "Post created successfully",
            "post_id": next_post_id,
            "image_path": image_path
        }), 201

    except Exception as e:
        # 예외 처리
        print("Error during post creation:", e)
        return jsonify({"error": str(e)}), 500






# 가장 큰 post_id 조회 API
@app.route('/max_post_id', methods=['GET'])
def get_max_post_id():
    """
    데이터베이스에서 가장 큰 post_id를 반환.
    """
    try:
        max_post_id = db.select_max_post_id()
        return jsonify({"max_post_id": max_post_id}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500


# 특정 ID의 모든 행 데이터 조회 API
@app.route('/post/<int:post_id>', methods=['GET'])
def get_post(post_id):
    """
    post_id를 통해 해당 포스트의 모든 데이터를 조회하여 반환.
    """
    try:
        post_data = db.select_table_row_all(post_id)
        if not post_data:
            return jsonify({"error": "Post not found"}), 404
        return jsonify({
            "video_path": post_data[0],
            "title": post_data[1],
            "content": post_data[2],
            "password": post_data[3],
            "created_date": post_data[4],
        }), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500


# 특정 ID의 날짜와 제목 조회 API
@app.route('/post/<int:post_id>/summary', methods=['GET'])
def get_post_summary(post_id):
    """
    post_id를 통해 해당 포스트의 created_date와 title을 반환.
    """
    try:
        summary_data = db.select_date_and_title(post_id)
        if not summary_data:
            return jsonify({"error": "Post not found"}), 404

        # 데이터 반환 형식 확인
        created_date = summary_data[0]  # created_date
        title = summary_data[1]  # title

        return jsonify({
            "created_date": created_date,
            "title": title,
        }), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500



@app.route('/post/<int:post_id>/details', methods=['GET'])
def get_post_details(post_id):
    """
    post_id를 통해 해당 포스트의 video_path, title, content를 반환.
    video_path 이미지를 150x150 픽셀로 크기 조정 후 Base64로 인코딩하여 반환.
    """
    try:
        # 데이터베이스에서 post_id로 행을 조회
        details_data = db.select_table_row_post(post_id)
        if not details_data:
            return jsonify({"error": "Post not found"}), 404

        # 데이터베이스에서 가져온 정보
        video_path = details_data[0]
        title = details_data[1]
        content = details_data[2]

        # 이미지 리사이즈 및 Base64 인코딩
        encoded_image = None
        if os.path.exists(video_path):  # 파일이 존재하는지 확인
            # OpenCV로 이미지 읽기
            img = cv.imread(video_path)

            # 이미지 크기 가져오기
            height, width = img.shape[:2]

            # 가로와 세로 중 더 큰 크기를 기준으로 스케일 계산
            if width > height:
                scale = 150 / width  # 가로 기준 스케일 계산
            else:
                scale = 150 / height  # 세로 기준 스케일 계산

            # 새로운 크기 계산 (비율 유지)
            new_width = int(width * scale)
            new_height = int(height * scale)

            # 이미지 크기 변경
            resized_img = cv.resize(img, (new_width, new_height))

            # 이미지를 메모리에 임시로 저장한 후 Base64로 인코딩
            _, buffer = cv.imencode('.png', resized_img)
            encoded_image = base64.b64encode(buffer).decode('utf-8')

        # 결과를 JSON으로 반환
        return jsonify({
            "video_path": video_path,
            "title": title,
            "content": content,
            "image_base64": encoded_image  # Base64로 인코딩된 이미지 데이터
        }), 200

    except Exception as e:
        # 예외 발생 시 오류 메시지 반환
        return jsonify({"error": str(e)}), 500
    

#게시물 수정용 엔드포인트임!!!!!!!혹시 제가 잘못 한거면 수정해 주세용-도현- 
@app.route('/update/<int:post_id>', methods=['GET', 'PATCH'])
def update_post(post_id):
    if request.method == 'GET':
        # GET 요청 처리
        post_data = db.select_table_row_all(post_id)
        if not post_data:
            return jsonify({"error": "Post not found"}), 404
        return jsonify({
            "video_path": post_data[0],
            "title": post_data[1],
            "content": post_data[2],
            "password": post_data[3],
            "created_date": post_data[4],
        }), 200

    elif request.method == 'PATCH':
        # PATCH 요청 처리
        data = request.form
        title = data.get('title')
        content = data.get('content')
        password = data.get('password')
        image_file = request.files.get('image')

        # 기존 게시물 데이터 조회
        post_data = db.select_table_row_all(post_id)
        if not post_data:
            return jsonify({"error": "Post not found"}), 404

        if post_data[3] != password:
            return jsonify({"error": "Invalid password"}), 403

        # 이미지 처리
        video_path = post_data[0]
        if image_file:
            if os.path.exists(video_path):
                os.remove(video_path)
            image_file.save(video_path)

        # 데이터베이스 업데이트
        db.update_post(
            post_id,
            title if title else post_data[1],
            content if content else post_data[2],
            video_path,
            password
        )
        return jsonify({"message": "Post updated successfully"}), 200




    
# 게시물 삭제용 엔드포인트!~!!!!!!!!!!!!!!-도현-x
@app.route('/delete/<int:post_id>', methods=['DELETE'])
def delete_post(post_id):
    """
    게시글 삭제
    """
    try:
        data = request.json
        password = data.get('password')

        # 게시글 확인 및 비밀번호 검증
        post_data = db.select_table_row_all(post_id)
        if not post_data:
            return jsonify({"success": False, "error": "Post not found"}), 404

        if post_data[3] != password:  # post_data[3] = DB에 저장된 비밀번호
            return jsonify({"success": False, "error": "Invalid password"}), 403

        # 게시글 삭제
        db.delete_post(post_id)
        return jsonify({"success": True, "message": "Post deleted successfully"}), 200
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500


@app.route('/uploader_simple', methods=['POST'])
def uploader_simple():
    if request.method == 'POST':
        f = request.files['video']
        # 파일 저장 경로
        f.save(os.path.join('data', secure_filename(f.filename)))

        d = predict() 
        if d[0] > 0.3:
            resultm = "DEEPFAKE"
        else:

            resultm = "ORIGINAL"
        print('결과는', d)
        #try:
        #    os.remove(name)
        #    print(f"{name}를 성공적으로 삭제했습니다.")
        #except OSError as e:
        #    print(f"파일을 삭제하는 도중 오류가 발생했습니다: {e}")
        shutil.rmtree('E:\\android_project\\dfdc_deepfake_challenge\\data')
        os.makedirs('E:\\android_project\\dfdc_deepfake_challenge\\data')
        return jsonify({'existResult': 'true', 'value':resultm})



@app.route('/uploader', methods=['GET', 'POST'])
def uploader_file():
    if request.method == 'POST':
        f = request.files['video']
        start_time = float(request.form.get('start_time'))
        end_time = float(request.form.get('end_time'))

        # 파일 저장 경로
        f.save(os.path.join('data_org', secure_filename(f.filename)))
        name = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'data_org', secure_filename(f.filename))
        output_path = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'data', secure_filename(f.filename))

        try:
            # 동영상 파일 열기
            video = cv.VideoCapture(name)
            if not video.isOpened():
                print("Error: Unable to open video file.")
                return

            # 동영상 정보 가져오기
            fps = video.get(cv.CAP_PROP_FPS)
            total_frames = video.get(cv.CAP_PROP_FRAME_COUNT)
            duration = total_frames / fps

            # FPS 값 검증
            if fps == 0:
                print("Error: FPS value is 0. Unable to process video.")
                return

            # 시작 및 종료 프레임 계산
            start_frame = int(start_time * fps)
            end_frame = int(end_time * fps)

            if start_time < 0 or end_time > duration or start_frame >= end_frame:
                print("Error: Invalid start_time or end_time.")
                return

            # 비디오 코덱 및 출력 파일 설정
            fourcc = cv.VideoWriter_fourcc(*'mp4v')
            width = int(video.get(cv.CAP_PROP_FRAME_WIDTH))
            height = int(video.get(cv.CAP_PROP_FRAME_HEIGHT))
            out = cv.VideoWriter(output_path, fourcc, fps, (width, height))

            # 디버깅 출력
            print(f"Processing video: FPS={fps}, Width={width}, Height={height}")

            # 지정된 범위의 프레임만 추출 및 저장
            current_frame = 0
            while video.isOpened():
                ret, frame = video.read()
                if not ret:
                    break

                if start_frame <= current_frame < end_frame:
                    out.write(frame)
                elif current_frame >= end_frame:
                    break

                current_frame += 1

            # 리소스 해제
            video.release()
            out.release()
            print(f"Trimmed video saved to: {output_path}")

        except Exception as e:
            print(f"Error: {e}")

        d = predict() 
        if d[0] > 0.3:
            resultm = "DEEPFAKE"
        else:

            resultm = "ORIGINAL"
        print('결과는', d)
        #try:
        #    os.remove(name)
        #    print(f"{name}를 성공적으로 삭제했습니다.")
        #except OSError as e:
        #    print(f"파일을 삭제하는 도중 오류가 발생했습니다: {e}")
        shutil.rmtree('E:\\android_project\\dfdc_deepfake_challenge\\data')
        os.makedirs('E:\\android_project\\dfdc_deepfake_challenge\\data')
        return jsonify({'existResult': 'true', 'value':resultm})

if __name__ == '__main__':
    app.debug = True
    app.run(host="0.0.0.0", port=2201)