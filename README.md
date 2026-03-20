# HR Management Console System

## 1. 프로젝트 소개
Oracle DB와 Java를 기반으로 구현한 콘솔형 인사관리 시스템입니다.  
관리자와 사원 권한을 분리하여 인사 정보, 부서/직급, 급여, 근태, 게시판, 통계 기능을 관리할 수 있도록 구현했습니다.

## 2. 개발 환경
- Language: Java
- Database: Oracle
- Tool: Eclipse
- Library: JDBC (ojdbc8)

## 3. 주요 기능
### 공통 기능
- 회원가입
- 로그인

### 관리자 기능
- 사원 조회 및 관리
- 부서 관리
- 직급 관리
- 급여 관리
- 인사발령 관리
- 근태 조회
- 공지사항 관리
- 로그 조회
- 통계 조회

### 사원 기능
- 내 정보 조회 / 수정
- 출퇴근 관리
- 공지사항 조회

## 4. 프로젝트 구조
- `kr.admin` : 관리자 기능
- `kr.employee` : 사원 기능
- `kr.hrsystem.dao` : DAO 계층
- `kr.hrsystem.main` : 메인 메뉴 및 실행
- `kr.util` : DB 연결 유틸

## 5. 실행 방법
1. Oracle DB를 준비합니다.
2. `sql` 폴더 내 스크립트를 실행합니다.
3. `DBUtil.java`에서 DB 접속 정보를 수정합니다.
4. 메인 클래스를 실행합니다.

## Oracle JDBC 드라이버 안내
Oracle JDBC 드라이버(ojdbc8.jar)는 저장소에 포함하지 않았습니다.
실행 전 libs 경로에 별도로 준비한 뒤 Build Path에 추가해야 합니다.

## 6. 향후 개선점
- 웹 기반 프로젝트로 확장
- 예외 처리 보강
- 테스트 코드 작성
