# HC15 Profile Diagnostic

作者：Peter

一個從零撰寫的 Android 14+ / Android 15 Health Connect 工作設定檔診斷工具。

## 用途

比較主空間與 Shelter / Work Profile 中：

- Android / SDK 版本
- 目前 UID 與推算 User ID
- `UserManager.isProfile()`
- `UserManager.isManagedProfile()`
- `android.health.connect.HealthConnectManager` 類別是否存在
- `Context.getSystemService(HealthConnectManager.class)` 是否取得實體
- `android.health.connect.action.HEALTH_HOME_SETTINGS` 是否可解析
- `com.google.android.healthconnect.controller` / `com.google.android.apps.healthdata` 套件狀態

## 建議測試方式

1. 將 APK 安裝到主空間與 Shelter 工作設定檔。
2. 主空間開啟一次並按「複製診斷結果」。
3. 工作設定檔再開啟一次並按「複製診斷結果」。
4. 比較兩份結果，尤其是：
   - Profile / Managed Profile
   - HealthConnectManager Service
   - Health Connect Controller

## GitHub Actions 編譯

Repository → Actions → `Build HC15 Profile Diagnostic APK` → Run workflow。

成功後在 Artifacts 下載：

`HC15ProfileDiagnostic-V0.1.0.apk`

## Package

`com.peter.hc15profilediag`

## Version

`0.1.0`
