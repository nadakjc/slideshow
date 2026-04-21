# Slideshow 프로젝트 진행 상황

작성일: 2026-04-21
브랜치: `claude/android-slideshow-plan-3n7VU`

## 1. 프로젝트 개요
안드로이드용 슬라이드쇼 앱. 사용자가 기기 내 특정 폴더를 선택하면 그 폴더의
이미지를 전체 화면으로 자동 재생한다. 추가 권한(READ_MEDIA_IMAGES) 없이
Storage Access Framework(SAF)의 영구 URI 권한만 사용한다.

## 2. 기술 스택 (확정)
| 항목 | 선택 |
| --- | --- |
| 언어/UI | Kotlin + Jetpack Compose |
| 아키텍처 | 단일 Activity + MVVM (ViewModel + StateFlow) |
| minSdk / targetSdk | 26 / 35 |
| 이미지 소스 | SAF 트리 URI (재실행 후에도 유지) |
| 이미지 로더 | Coil 3.0.4 |
| 설정 저장 | Jetpack DataStore (Preferences) |
| 내비게이션 | Navigation-Compose |
| 빌드 | Gradle 8.11.1 + AGP 8.7.3 + Kotlin 2.1.0, Compose BOM 2024.12.01 |

## 3. 완료된 단계 (플랜 1~7)

### 1단계 — 프로젝트 스캐폴딩
- 루트 `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`
- `gradle/libs.versions.toml` (버전 카탈로그)
- Gradle 래퍼 (8.11.1)
- 앱 모듈 `app/build.gradle.kts`, `AndroidManifest.xml`, `proguard-rules.pro`

### 2단계 — 테마 & 내비게이션 뼈대
- `SlideshowApp` (Application) + `MainActivity` (edge-to-edge)
- Material3 테마(동적 색상 지원) — `ui/theme/Theme.kt`
- Navigation-Compose 3개 라우트: Home / Settings / Player — `nav/AppNav.kt`

### 3단계 — 설정 저장소
- `data/model/SlideshowSettings.kt` — folderUri, intervalMs(5초 기본),
  shuffle, loop(true), recursive(true)
- `data/SettingsRepository.kt` — DataStore 래퍼, Flow로 노출

### 4단계 — 폴더 선택 (SAF)
- `ui/home/HomeViewModel.kt` — `ACTION_OPEN_DOCUMENT_TREE` 결과 처리,
  `takePersistableUriPermission` 으로 URI 영구 권한 취득, 이전 권한 정리
- `ui/home/HomeScreen.kt` — 폴더 경로 표시, 폴더 선택/시작/설정 버튼

### 5단계 — 이미지 수집
- `data/FolderRepository.kt` — `DocumentFile.fromTreeUri` 재귀 순회,
  MIME `image/*` + 확장자 폴백(jpg/png/webp/heic/heif/gif/bmp), 이름순 정렬

### 6단계 — 슬라이드쇼 재생
- `ui/player/PlayerScreen.kt` — 검정 배경 풀스크린, Coil 3 `AsyncImage`,
  `Crossfade(400ms)` 전환, 탭 = 일시정지 토글, 좌/우 스와이프 = 이전/다음,
  일시정지 시 상단 pause 아이콘
- `util/ScreenEffects.kt` — `KeepScreenOn`, `ImmersiveMode` composable

### 7단계 — 자동 넘김 타이머
- `ui/player/PlayerViewModel.kt` — 설정 로드 → 셔플 적용 → 코루틴 `delay`
  기반 타이머, 반복 on/off, 수동 조작 시 타이머 재시작, 폴더 미선택/빈
  폴더 에러 상태

## 4. 확인된 동작
- 에뮬레이터에서 앱 실행 → 폴더 선택 시스템 피커까지 정상 진입 확인.
- 폴더 URI가 DataStore에 저장되어 재실행 후에도 유지됨.

## 5. 현재 블로커
- **에뮬레이터에 테스트 이미지 배치** — 드래그 & 드롭으로 `/sdcard/Download/`
  까지는 파일이 올라간 것을 Device Explorer에서 확인했으나, SAF 폴더
  피커의 "Downloads" 가상 뷰에서는 보이지 않음.
- 원인: 드래그로 올린 파일은 MediaStore 스캔이 안 되어 있어 SAF 피커의
  "Downloads" 가상 컬렉션에 안 나타남.
- 해결안 (검증 예정):
  1. SAF 피커에서 ⋮ → "Show internal storage" 활성화 후 ☰ → 기기 저장소 →
     Download 로 진입.
  2. Device Explorer 로 `sdcard/Pictures/Slideshow/` 등 Pictures 하위에
     이미지를 옮긴 뒤 그 폴더를 선택.

## 6. 커밋 이력 (이 브랜치)
1. `Add initial slideshow app design plan` — PLAN.md
2. `Scaffold Android slideshow project (Compose, SAF, DataStore)` — 플랜 1~4
3. `Implement slideshow playback (FolderRepository, Player, auto-advance)` — 플랜 5~7

## 7. 남은 단계 (플랜 8~10)
- **8 설정 화면 연결** — 간격 칩/슬라이더, 셔플/반복/하위폴더 토글을
  DataStore에 반영.
- **9 폴리시** — 인접 이미지 프리로드(±1장), 대용량 폴더 증분 로딩,
  회전 시 index 유지, 에러 UI 개선.
- **10 테스트** — `FolderRepository` 필터/정렬 단위 테스트,
  `PlayerViewModel` 타이머 테스트 (`TestDispatcher` / `runTest`).

## 8. 실행 방법 요약
```bash
git clone https://github.com/nadakjc/slideshow.git
cd slideshow
git checkout claude/android-slideshow-plan-3n7VU
```
Android Studio에서 프로젝트 폴더 열기 → Gradle Sync 완료 대기 → 상단의
기기 드롭다운에서 에뮬레이터/실기기 선택 → ▶ Run.
