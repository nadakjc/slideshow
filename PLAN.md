# 안드로이드 슬라이드쇼 앱 — 설계 계획

## 1. 제품 개요
- 사용자가 **기기 내 특정 폴더**를 선택하면 그 폴더의 이미지를 전체 화면으로 자동 재생하는 슬라이드쇼 앱.
- MVP 범위: 폴더 선택 → 자동 넘김(간격 설정) → 페이드 전환 → 셔플/반복 → 제스처로 일시정지·이전·다음.

## 2. 기본 선택값 (추천 기본값)
| 항목 | 선택 |
| --- | --- |
| 언어/UI | Kotlin + Jetpack Compose |
| 아키텍처 | 단일 Activity + MVVM (ViewModel + StateFlow) |
| minSdk / targetSdk | 26 (Android 8.0) / 35 |
| 이미지 소스 | Storage Access Framework(SAF) — 사용자가 폴더 선택, URI 영구 권한 |
| 이미지 로더 | Coil 3 (Compose 통합, 메모리/디스크 캐시) |
| 설정 저장 | Jetpack DataStore (Preferences) |
| 내비게이션 | Navigation-Compose |
| 빌드 시스템 | Gradle (Kotlin DSL) + version catalog (`libs.versions.toml`) |

## 3. MVP 기능
- [F1] **폴더 선택**: `ACTION_OPEN_DOCUMENT_TREE`로 폴더 선택, `takePersistableUriPermission` 으로 재실행 후에도 유지.
- [F2] **이미지 목록**: `DocumentFile.fromTreeUri` 로 재귀 탐색(옵션), MIME `image/*` 필터(jpeg/png/webp/heic/gif).
- [F3] **슬라이드쇼 재생**: 전체 화면, 페이드 전환(기본 400ms), 자동 넘김 간격 기본 5초.
- [F4] **제스처 컨트롤**: 탭 = 일시정지/재개, 좌/우 스와이프 = 이전/다음, 길게 누르기 = 컨트롤바 표시.
- [F5] **옵션**: 셔플 on/off, 반복 on/off, 하위 폴더 포함 on/off, 간격 (2/5/10/30초, 커스텀).
- [F6] **화면 꺼짐 방지**: 재생 중 `keepScreenOn`.
- [F7] **상태 복원**: 최근 폴더 URI, 설정값은 DataStore에 저장.

### 비-MVP(백로그)
- Ken Burns(줌/팬), 슬라이드 간 배경음악, 캐스팅(Chromecast), Android TV 대응, 위젯/Daydream, 클라우드(Google Drive) 소스, 자막·캡션.

## 4. 아키텍처

```
app/
 └─ src/main/java/com/example/slideshow/
    ├─ MainActivity.kt              # 단일 Activity, WindowCompat 설정
    ├─ nav/AppNav.kt                # Home / Settings / Player 라우팅
    ├─ ui/
    │  ├─ theme/                    # Material3 테마 (다크 지원)
    │  ├─ home/HomeScreen.kt        # 폴더 선택·시작·설정 진입
    │  ├─ settings/SettingsScreen.kt
    │  └─ player/
    │     ├─ PlayerScreen.kt        # 전체 화면 뷰어
    │     └─ PlayerViewModel.kt     # 인덱스/재생 상태/타이머
    ├─ data/
    │  ├─ SettingsRepository.kt     # DataStore 래퍼
    │  ├─ FolderRepository.kt       # SAF 트리 URI → 이미지 URI 목록
    │  └─ model/SlideshowSettings.kt
    └─ util/ImmersiveMode.kt
```

- **데이터 흐름**: `FolderRepository.listImages(treeUri, recursive)` → `PlayerViewModel` 가 순서(`shuffled()` 또는 원본) 결정 → Compose가 `currentIndex` 구독 → Coil `AsyncImage` 로 표시.
- **타이머**: `PlayerViewModel` 내부 `viewModelScope`에서 `while(isPlaying) { delay(intervalMs); advance() }`. 일시정지/간격 변경 시 취소-재시작.
- **전환 효과**: `Crossfade(targetState = currentIndex, animationSpec = tween(400))`.

## 5. 데이터 모델

```kotlin
data class SlideshowSettings(
    val folderUri: String? = null,        // persisted tree URI
    val intervalMs: Long = 5_000,
    val shuffle: Boolean = false,
    val loop: Boolean = true,
    val recursive: Boolean = true,
)
```

## 6. 권한 / 매니페스트
- `READ_MEDIA_IMAGES` **사용하지 않음** — SAF 영구 권한만으로 해결 (권한 프롬프트 최소화).
- 매니페스트: 특별한 권한 없음. `MainActivity`에 `configChanges` 로 회전 대응.
- 화면 꺼짐 방지: Player 화면에서만 `keepScreenOn = true`.

## 7. 화면 구성

### 7.1 Home
- 현재 선택된 폴더 경로 표시 (없으면 "폴더를 선택하세요").
- 버튼: **폴더 선택**, **슬라이드쇼 시작**(비활성: 폴더 없을 때), **설정**.

### 7.2 Settings
- 간격(슬라이더/칩 2·5·10·30·60초).
- 셔플 / 반복 / 하위 폴더 포함 토글.

### 7.3 Player
- 전체 화면, 시스템 바 숨김(immersive).
- 중앙 탭 → 하단 컨트롤바(이전/재생·일시정지/다음, 진행 n/m) 토글.
- 뒤로가기 → Home.

## 8. 단계별 구현 순서
1. **프로젝트 스캐폴딩** — AGP + Kotlin + Compose BOM, Material3, 버전 카탈로그.
2. **테마 & Nav 뼈대** — Home/Settings/Player 빈 화면 연결.
3. **SettingsRepository(DataStore)** — 읽기/쓰기, 초기 기본값.
4. **폴더 선택 플로우** — `rememberLauncherForActivityResult`, persistable permission, URI 저장.
5. **FolderRepository** — `DocumentFile` 로 이미지 수집, 재귀 옵션, MIME 필터, 빠른 정렬(이름순).
6. **Player 기본** — Coil `AsyncImage` + `Crossfade`, 수동 좌/우 스와이프.
7. **자동 넘김 타이머** — ViewModel 코루틴, 일시정지/재개.
8. **옵션 반영** — 셔플(시작 시 한 번), 반복, 간격 변경 즉시 반영.
9. **Polish** — immersive 모드, keepScreenOn, 회전 대응, 빈 폴더/로딩 에러 UI.
10. **테스트** — `FolderRepository` 필터/정렬 단위테스트, `PlayerViewModel` 타이머 테스트(`TestDispatcher`).

## 9. 수용 기준(Acceptance)
- 폴더를 선택하면 재시작 후에도 그 폴더로 바로 재생 가능.
- 기본 5초 간격으로 부드럽게 넘어가며, 탭하면 즉시 일시정지.
- 셔플 on일 때 재생 순서가 시드 기반으로 고정(세션 내 일관).
- 빈 폴더면 "이미지가 없습니다" 안내 후 Home 복귀.
- 100장 기준 재생 중 프레임 드랍·OOM 없음(기본 프리로드 ±1장).

## 10. 리스크 / 열린 질문
- **HEIC 재생**: 기기별 지원 편차 → Coil 디코더 확인, 필요 시 분기.
- **대용량 폴더**: 수천 장일 때 초기 목록화 시간 → 증분 로딩 또는 페이징 고려(백로그).
- **GIF/애니메이션**: Coil `GifDecoder` 추가 여부 — MVP는 정지 이미지 우선.
- **보안 이미지 대비**: SAF 권한 회수 시 재선택 유도 UI.

---

다음 단계 제안: 위 1~4단계(스캐폴딩 + 설정 저장 + 폴더 선택)를 먼저 구현해 실제 기기에서 폴더 선택과 URI 영구 권한까지 확인한 뒤, 5~7단계를 진행.
