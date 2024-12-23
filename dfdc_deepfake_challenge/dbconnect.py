
import pymysql
from datetime import datetime
import os
from dotenv import load_dotenv  # python-dotenv 패키지에서 제공
import shutil
# .env 파일 로드
load_dotenv()

# 환경 변수 가져오기
ip = os.getenv("IP")
db_user = os.getenv("USER")
db_password = os.getenv("PASSWORD")
db_db = os.getenv("DB")



def dbcon():
    return pymysql.connect(
        host=f"{ip}", 
        user=f"{db_user}", 
        password=f"{db_password}", 
        db=f"{db_db}",charset='utf8'
        )   # DB에 연결 (호스트이름 대신 IP주소 가능)

def select_max_post_id(): 
    ret = None
    db = None
    con = None
    try:
        db = dbcon()
        con = db.cursor()
        # 가장 큰 post_id 찾기
        con.execute("SELECT MAX(post_id) AS max_post_id FROM posts")
        result = con.fetchone()  # 하나의 결과만 가져옴
        if result and result[0]:
            ret = result[0]  # 가장 큰 post_id 값
    except Exception as e:
        print('db error:', e)
    finally:
        if con:
            con.close()
        if db:
            db.close()
    return ret

def insert_data(title, content, password, image_path=None):
    """
    제목, 내용, 비밀번호, 이미지 경로를 데이터베이스에 삽입합니다.
    """
    try:
        db = dbcon()
        con = db.cursor()

        # 가장 큰 post_id를 가져와 다음 ID를 계산
        max_post_id = select_max_post_id() or 0  # 테이블이 비어 있으면 0을 기본값으로 사용
        next_post_id = max_post_id + 1

        # 이미지 경로 설정 (uploaded_images 디렉토리 사용)
        if image_path is None:  # 이미지 경로가 제공되지 않았을 경우 기본 경로 사용
            image_path = f"E:/android_project/dfdc_deepfake_challenge/community_data/{next_post_id}.png"
        else:
            # 업로드된 이미지가 제공된 경우 복사하여 저장
            upload_dir = "E:/android_project/dfdc_deepfake_challenge/community_data/"
            os.makedirs(upload_dir, exist_ok=True)
            final_image_path = os.path.join(upload_dir, f"{next_post_id}.png")
            image_path = final_image_path

        # SQL 쿼리 실행 (매개변수 방식 사용)
        query = """
        INSERT INTO posts (video_path, title, content, password)
        VALUES (%s, %s, %s, %s)
        """
        con.execute(query, (image_path, title, content, password))
        db.commit()  # 변경 사항 적용
        print(f"데이터 삽입이 완료되었습니다. 이미지 경로: {image_path}")

    except Exception as e:
        print('db error:', e)
    finally:
        if con:
            con.close()
        if db:
            db.close()




def select_table_row_all(id):
    """
    데이터베이스에서 지정된 id의 모든 열을 조회한 후, 1차원 리스트로 반환.
    created_date는 '%Y/%m/%d/%H:%M' 형식으로 변환.
    """
    ret = []
    db = None
    con = None
    try:
        db = dbcon()  # DB 연결
        con = db.cursor()  # 커서 생성

        # SQL 쿼리 실행
        con.execute(f"SELECT video_path, title, content, password, created_date FROM posts WHERE post_id='{id}'")
        result = con.fetchall()  # 결과 가져오기

        # 1차원 리스트로 변환
        for row in result:
            ret.append(row[0])  # video_path 추가
            ret.append(row[1])  # title 추가
            ret.append(row[2])  # content 추가
            ret.append(row[3])  # password 추가
            ret.append(row[4].strftime('%Y/%m/%d/%H:%M'))  # created_date 변환 후 추가
    except Exception as e:
        print('db error:', e)
    finally:
        if con:
            con.close()  # 커서 닫기
        if db:
            db.close()  # DB 연결 닫기
    return ret



def select_date_and_title(id):
    """
    데이터베이스에서 지정된 id의 created_date와 title을 반환.
    반환값은 1차원 리스트 형태.
    """
    ret = []
    db = None
    con = None
    try:
        db = dbcon()  # DB 연결
        con = db.cursor()  # 커서 생성

        # SQL 쿼리 실행
        con.execute(f"SELECT created_date, title FROM posts WHERE post_id='{id}'")
        result = con.fetchall()  # 결과 가져오기

        # 1차원 리스트로 변환
        for row in result:
            ret.append(row[0].strftime('%Y/%m/%d/%H:%M'))  # 날짜 변환 후 추가
            ret.append(row[1])  # 제목 추가
    except Exception as e:
        print('db error:', e)
    finally:
        if con:
            con.close()  # 커서 닫기
        if db:
            db.close()  # DB 연결 닫기
    return ret


def select_table_row_post(id):
    """
    데이터베이스에서 지정된 id의 모든 열을 조회한 후, 1차원 리스트로 반환.
    created_date는 '%Y/%m/%d/%H:%M' 형식으로 변환.
    """
    ret = []
    db = None
    con = None
    try:
        db = dbcon()  # DB 연결
        con = db.cursor()  # 커서 생성

        # SQL 쿼리 실행
        con.execute(f"SELECT video_path, title, content FROM posts WHERE post_id='{id}'")
        result = con.fetchall()  # 결과 가져오기

        # 1차원 리스트로 변환
        for row in result:
            ret.append(row[0])  # video_path 추가
            ret.append(row[1])  # title 추가
            ret.append(row[2])  # content 추가

    except Exception as e:
        print('db error:', e)
    finally:
        if con:
            con.close()  # 커서 닫기
        if db:
            db.close()  # DB 연결 닫기
    return ret

def update_post(post_id, title, content, video_path, password):
    """
    특정 게시물의 제목, 내용, 이미지 경로를 수정합니다.
    """
    try:
        db = dbcon()
        con = db.cursor()

        # SQL 쿼리: 지정된 post_id의 내용을 업데이트
        sql = """
        UPDATE posts
        SET title = %s, content = %s, video_path = %s, password = %s
        WHERE post_id = %s
        """
        con.execute(sql, (title, content, video_path, password, post_id))
        db.commit()  # 변경 사항 적용
        print("게시물 업데이트 완료")
    except Exception as e:
        print('db error:', e)
        raise
    finally:
        if con:
            con.close()
        if db:
            db.close()



# 여기도 -도현-
def delete_post(post_id):
    """
    특정 게시물을 삭제합니다.
    """
    try:
        db = dbcon()
        con = db.cursor()
        # 게시물 삭제 쿼리
        sql = "DELETE FROM posts WHERE post_id = %s"
        con.execute(sql, (post_id,))
        db.commit()
        print("게시물 삭제 완료")
    except Exception as e:
        print('db error:', e)
        raise
    finally:
        if con:
            con.close()
        if db:
            db.close()
            