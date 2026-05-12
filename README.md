# 경북대 통합 일정 관리 시스템 (KNU Unified Calendar)

경북대학교 학사일정 실시간 연동 및 개인 일정 관리가 가능한 순수 Java 기반 데스크톱 캘린더 애플리케이션입니다.

## 프로젝트 개요
* **개발 언어:** Java (JDK 8 이상 권장)
* **UI 프레임워크:** Java Swing & AWT
* **아키텍처:** MVC (Model-View-Controller) 패턴 적용
* **특징:** **외부 라이브러리(Jsoup 등)를 일절 사용하지 않고** 순수 Java 내장 클래스(`HttpURLConnection`)만을 이용해 웹 스크래핑 엔진을 자체 구현했습니다.

## 핵심 기능
1. **실시간 학사일정 스크래핑 (웹 크롤링)**
    * 프로그램 실행 시 경북대학교 학사일정 웹페이지의 HTML을 읽어와 날짜와 일정 내용을 파싱합니다.
    * 네트워크 단절 시 내장된 더미(Dummy) 데이터를 불러오는 Fallback 시스템이 적용되어 있습니다.
2. **달력형 GUI 및 직관적인 UI**
    * 현재 월 기준 달력 형태로 일정을 시각화합니다.
    * **오늘 날짜 강조** 및 일별 일정 요약 텍스트를 달력 칸 내부에 직접 표시합니다.
3. **일정 필터링 및 D-Day 시스템**
    * 학사일정(파란색)과 개인일정(빨간색)을 시각적으로 구분합니다.
    * 콤보박스를 통한 일정 타입별(전체/학사/개인) 필터링을 지원합니다.
    * `java.time.LocalDate`를 활용한 실시간 D-Day 계산 기능이 포함되어 있습니다.
4. **개인 일정 관리 (CRUD)**
    * 달력 하단의 '퀵-추가' 기능을 통해 빠르게 일정을 등록할 수 있습니다.
    * 특정 날짜 클릭 시 나타나는 상세 팝업에서 일정 추가, 수정, 삭제가 가능합니다.

## 패키지 구조 (MVC 패턴)
```text
src/kr/ac/knu/calendar/
 ├── model/                  # 데이터 구조 및 비즈니스 로직
 │   ├── Schedule.java         (일정 추상 부모 클래스 - 다형성 적용)
 │   ├── AcademicSchedule.java (학사일정 자식 클래스)
 │   ├── PersonalSchedule.java (개인일정 자식 클래스)
 │   └── ScheduleManager.java  (일정 배열 관리 및 필터링/정렬)
 │
 ├── util/                   # 네트워크 통신 및 파싱 엔진
 │   └── DataLoader.java       (순수 Java 기반 웹 스크래퍼)
 │
 └── view/                   # 사용자 인터페이스(GUI)
     └── CalendarApp.java      (Swing 메인 화면 및 이벤트 처리)
```

## 실행 방법
1. 본 레포지토리를 Clone 합니다.

2. IntelliJ IDEA 또는 Eclipse에서 프로젝트를 오픈합니다.

3. src/kr/ac/knu/calendar/view/CalendarApp.java 파일의 main 메서드를 실행합니다.

## 개발 포인트 (어필 요소)
1. 다형성(Polymorphism) 활용: ScheduleManager는 AcademicSchedule과 PersonalSchedule을 부모 타입인 Schedule[] 배열 하나로 묶어서 관리합니다.

2. 커스텀 문자열 파싱: 웹페이지 구조(<li>, <span class="day">)를 분석하고, indexOf와 substring을 조합해 원하는 텍스트만 추출하는 커스텀 파싱 로직을 적용했습니다.

3. 정렬 인터페이스 구현: Schedule 클래스에 Comparable 인터페이스를 구현하여, 날짜 객체(LocalDate)를 기준으로 일정이 항상 시간순으로 자동 정렬되도록 설계했습니다.