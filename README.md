# 경북대 통합 일정 관리 시스템

경북대학교 학사일정 실시간 스크래핑 연동 및 개인 일정 관리가 가능한 순수 Java 기반 데스크톱 캘린더 애플리케이션

## 프로젝트 개요
* **개발 언어:** Java (JDK 22 이상 권장)
* **UI 프레임워크:** Java Swing & AWT
* **아키텍처:** MVC (Model-View-Controller) 패턴 적용
* **특징:** 순수 Java 내장 클래스(`java.net.http.HttpClient`, `java.io`, `java.util.regex`)만을 이용하여 웹 스크래핑 엔진과 파일 입출력 시스템을 자체 구현.

## 핵심 기능
1. **실시간 학사일정 스크래핑 (웹 크롤링)**
    * 프로그램 실행 시 경북대학교 학사일정 웹페이지의 HTML을 읽어와 날짜와 일정 내용을 정밀하게 파싱.
    * 멀티스레드(Background Thread)를 활용해 웹페이지로부터 정보를 불러오는 도중 메인 화면(UI)이 멈추지 않도록 Non-blocking I/O를 구현.
2. **달력형 GUI 및 직관적인 UI**
    * 현재 월 기준 달력 형태로 일정을 시각화.
    * **오늘 날짜 강조** 및 일별 일정 요약 텍스트를 달력 칸 내부에 직접 표시하여 한눈에 파악할 수 있음.
3. **일정 필터링 및 D-Day 시스템**
    * 학사일정(검은색), 공휴일(빨간색), 개인일정(파란색)을 시각적으로 구분하여 렌더링.
    * 콤보박스를 통한 일정 타입별(전체/학사/대학원/개인) 실시간 필터링을 지원함.
    * `java.time.LocalDate`를 활용한 실시간 D-Day 계산 기능(예: `D-15`, `D+3`)이 포함되어 있음.
4. **개인 일정 관리 (CRUD)**
    * 달력 하단의 '퀵-추가' 기능을 통해 빠르게 일정을 등록할 수 있음.
    * 특정 날짜 클릭 시 나타나는 상세 팝업에서 일정 추가, 수정, 삭제가 자유롭게 가능함.
5. **무중단 로컬 데이터 영속성 (File I/O 자동 저장)**
    * 사용자가 별도로 '저장' 버튼을 누르지 않아도, 프로그램을 종료하는 시점에 사용자가 등록한 개인 일정이 로컬 텍스트 파일(`schedules.txt`)에 자동 저장되고 다음 실행 시 완벽하게 복원됨.

## 패키지 구조 (MVC 패턴)
```text
src/kr/ac/knu/calendar/
 ├── model/                  # 데이터 구조 및 비즈니스 로직
 │   ├── Schedule.java             (일정 추상 부모 클래스 - 다형성 적용)
 │   ├── AcademicSchedule.java     (학사일정 자식 클래스)
 │   ├── PersonalSchedule.java     (개인일정 자식 클래스)
 │   ├── ScheduleFilter.java       (조건별 일정 필터링)
 │   ├── ScheduleLoaderThread.java (학사일정 로딩 및 파싱 스레드)
 │   └── ScheduleManager.java      (일정 컬렉션 관리 및 File I/O 자동 저장 처리)
 │
 ├── util/                   # 유틸리티 및 네트워크 통신 엔진
 │   └── DataLoader.java       (HttpClient 기반 HTTP 스크래퍼)
 │
 └── view/                   # 사용자 인터페이스(GUI)
     ├── CalendarApp.java      (Swing 메인 화면 렌더링 및 이벤트 처리)
     ├── DetailDialog.java     (상세 일정 팝업 화면 및 CRUD 이벤트 처리)
     ├── ItemList.java         (상세 일정 화면의 일정 커스텀 목록)
     └── ItemPanel.java        (코드 재사용성을 고려한 커스텀 UI 패널)
```

## 실행 방법
1. 본 레포지토리를 Clone 합니다.

2. IntelliJ IDEA 또는 Eclipse에서 프로젝트를 오픈합니다. (Java 22+ 환경 세팅 필요)

3. src/kr/ac/knu/calendar/view/CalendarApp.java 파일의 main 메서드를 실행합니다.

- Note: 프로그램을 실행하고 개인 일정을 추가한 뒤 종료하면, 프로젝트 루트 디렉토리에 개인 일정 데이터가 담긴 schedules.txt 파일이 자동 생성됩니다.

## 개발 포인트
1. **다형성 활용:** ScheduleManager는 AcademicSchedule과 PersonalSchedule을 추상 부모 타입인 Schedule 하나로 묶어(List<Schedule>) 유연하게 관리

2. **커스텀 정규식 파싱:** 무거운 DOM 파서 대신 정규 표현식(Pattern, Matcher)을 직접 작성하여 웹페이지 구조를 분석하고 연속된 날짜(12.25.~1.1.)를 개별 LocalDate로 변환하는 알고리즘을 적용

3. **비동기 멀티스레딩:** 네트워크 딜레이로 인한 메인 스레드(EDT) 병목을 막기 위해 작업 스레드를 분리하였고, 완료 시 Callback을 통해 안전하게 화면을 갱신

4. **Shutdown Hook 기반 파일 입출력:** java.io 패키지의 BufferedReader/Writer를 활용하였으며, Runtime.getRuntime().addShutdownHook()을 적용해 프로그램 정상 종료 이벤트를 감지하여 데이터를 안전하게 직렬화하도록 설계 (메모리 낭비를 막기 위해 학교 서버에서 다시 불러올 수 있는 학사일정은 제외하고, 개인 일정만 선택적으로 저장)