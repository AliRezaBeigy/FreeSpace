<div align="center">
  <img width="96" alt="Free Space" src="assets/logo-rounded.png" />

# Free Space

On-device disk optimizer for Android. Scan large photos and videos, estimate savings, batch-compress what you select, then review before keeping both or replacing the original.

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue.svg?style=for-the-badge)

<br>

<img src="assets/screenshots/01_list.png" alt="Large media list" width="30%"/>
<img src="assets/screenshots/02_screen.png" alt="Compression queue" width="30%"/>
<img src="assets/screenshots/03_screen.png" alt="Compare result" width="30%"/>
</div>

## Features

- Opt-in media library scan (photos + videos) after you grant access
- Multi-select with High / Medium / Low quality modes and estimated reclaim
- Sequential queue using Media3 for video and on-device image compression
- Side-by-side review: keep both or replace original (with confirmation)
- Hide file / folder filters and compression history that survive restarts
- No ads, no tracking, no internet permission

## Privacy

All processing stays on your device. See [PRIVACY.md](PRIVACY.md).

## Build

```bash
./gradlew :app:assembleDebug
```

Requires Android SDK / JDK 17+.

## License

MIT. Derived from [JoshAtticus/Compressor](https://github.com/JoshAtticus/Compressor); this project is a dedicated Free Space app, not a continuation of that product.
