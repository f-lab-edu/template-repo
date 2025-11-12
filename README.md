## 실행
1. 도커 컴포즈 사용
- 환경 변수 설정(하단 참조)을 포함한 `.env` 파일을 각 모듈의 루트 디렉토리에 배치합니다.
- Docker Desktop을 실행합니다.
- cmd(Windows) 또는 shell에서 `docker compose up` 명령어를 입력합니다.

## 환경 변수

### app
1. DB 설정
  - DB_URL: DB 연결을 위한 jdbc url입니다. (e.g. jdbc:postgresql://localhost:5432/app, 도커 사용시 jdbc:postgresql://postgres/app)
  - DB_USER: DB의 사용자 이름입니다.
  - DB_PASSWORD: DB의 사용자 비밀번호입니다.
  - SQL_INIT_MODE: 어플리케이션 설정 시 DDL인 `schema.sql`의 실행을 설정하는 값입니다. 현재 어플리케이션의 기본값은 `never`입니다.
    - `always`: 항상 실행합니다.
    - `embedded`: 임베디드 데이터베이스를 사용할 경우에만 실행합니다.
    - `never`: 실행하지 않습니다.