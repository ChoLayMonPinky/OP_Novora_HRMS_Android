# Novora HRMS Android

Employee self-service app. Default API is production Render:

`https://novora-backend-wiem.onrender.com`

See [ARCHITECTURE.md](ARCHITECTURE.md) for the feature-first package layout (`app` / `core` / `feature`).

## Run

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
./gradlew :app:installDebug
```

Or in Android Studio: open the `android/` folder, wait for Gradle sync, then Run ▶ on an emulator/device.

First request after Render idle can take 30–90s (cold start).

## Optional local backend

```bash
cd ../backend
docker compose up --build
# then:
./gradlew :app:installDebug -Pnovora.apiBaseUrl=http://10.0.2.2:8081
```

## Troubleshooting

**`Unable to locate a Java Runtime`** — set `JAVA_HOME` to JDK 21 (see above) before Gradle or Android Studio.

**`Failed file name validation` / resource name contains a space** — stale build intermediates (often macOS `file 2.png` copies). Fix with:

```bash
./gradlew clean :app:assembleDebug
```

**No device** — start an AVD in Device Manager, or plug in a phone with USB debugging, then `adb devices` should list it before `installDebug`.
