# GitHub Actions 編譯

1. 將此資料夾內的所有檔案上傳到 GitHub Repository 根目錄。
2. 開啟 **Actions** → **Build HC15 Profile Diagnostic APK**。
3. 點 **Run workflow**，或直接 push 到 `main` / `master`。
4. 編譯完成後，在該次執行頁面底部 **Artifacts** 下載 `HC15ProfileDiagnostic-V0.1.1`。

本版已改用 `android-actions/setup-android@v4`，並固定 `ubuntu-24.04`、JDK 17、Gradle 8.9、Android API 35。
