# Slideshow 프로젝트 진행 상황

작성일: 2026-04-21
브랜치: `claude/android-slideshow-saf-K8lkn`

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
| 테스트 | JUnit 4 + kotlinx-coroutines-test (JVM 단위) |
| 빌드 | Gradle 8.11.1 + AGP 8.7.3 + Kotlin 2.1.0, Compose BOM 2024.12.01 |

## 3. 완료된 단계 (플랜 1~10)

### 1~7단계 (요약)
- 스캐폴딩, 테마·내비게이션, SettingsRepository, SAF 폴더 선택,
  FolderRepository, PlayerScreen/PlayerViewModel(자동 넘김 타이머).

### 8단계 — 설정 화면 연결
- `ui/settings/SettingsViewModel.kt` — DataStore Flow → StateFlow, 개별
  변경 메서드(setIntervalMs/Shuffle/Loop/Recursive).
- `ui/settings/SettingsScreen.kt` — 3·5·8·10·15·30초 FilterChip(FlowRow로
  좁은 화면에서도 줄바꿈), 셔플/반복/하위폴더 Switch.

### 9단계 — 폴리시
- `ui/player/PlayerScreen.kt`
  - 인접 프리로드: `LaunchedEffect(state.index)`에서 ±1 이미지를
    `context.imageLoader.enqueue(ImageRequest)`로 미리 디코드.
  - 에러 화면에 "뒤로" 버튼 추가 → 스와이프 제스처 기기에서도 탈출 가능.
- 회전 시 상태 유지: `viewModel()`이 NavBackStackEntry scope에서 재사용
  되므로 별도 코드 없이 현 인덱스·재생 상태 유지됨.

### 10단계 — 테스트
- 순수 로직 분리:
  - `data/ImageFilter.kt` — `isImage(mime, name)`
  - `util/PlayerIndex.kt` — `nextIndex(index, size, step, loop)`
- `app/src/test/.../ImageFilterTest.kt` — MIME·확장자·대소문자·빈 값.
- `app/src/test/.../PlayerIndexTest.kt` — 전/후진, 루프 wrap, 경계 clamp,
  단일/빈 컬렉션, 점프.
- 의존성: `junit:4.13.2`, `kotlinx-coroutines-test`.

## 4. 실행 환경
- **실기기 사용 예정** (에뮬레이터 블로커 종료):
  1. 폰의 설정 → 휴대전화 정보 → 빌드 번호 7회 터치 → 개발자 옵션 활성화.
  2. 개발자 옵션에서 **USB 디버깅** 켜기.
  3. USB로 PC 연결 → 폰의 "이 컴퓨터를 허용하시겠습니까?" 팝업에서 허용.
  4. Android Studio 상단 기기 드롭다운에서 폰 선택 → ▶ Run.
- 이미지가 들어 있는 폴더는 갤러리/Pictures 하위 아무 곳이나 OK —
  SAF 피커에서 직접 선택 가능하므로 별도 복사 불필요.

## 5. 커밋 이력 (이 브랜치)
1. `Add initial slideshow app design plan`
2. `Scaffold Android slideshow project (Compose, SAF, DataStore)`
3. `Implement slideshow playback (FolderRepository, Player, auto-advance)`
4. `Add progress summary document`
5. `Connect settings, add preloading and unit tests` — 플랜 8~10

## 6. 실행 방법 요약
```bash
git clone https://github.com/nadakjc/slideshow.git
cd slideshow
git checkout claude/android-slideshow-saf-K8lkn
```
Android Studio에서 프로젝트 폴더 열기 → Gradle Sync → 실기기 선택 → ▶ Run.
테스트 실행: `./gradlew :app:testDebugUnitTest`.
