# Android architecture (feature-first)

Default API: `https://novora-backend-wiem.onrender.com` (Render).  
Local override: `-Pnovora.apiBaseUrl=http://10.0.2.2:8081`

## Package layout

```
dev.tint_wym.novora_hrms_android/
  app/                 # MainActivity, NovoraApp root composition
  core/
    config/            # ApiConfig
    network/           # NovoraApiClient, PersistentCookieJar, ApiException
    model/             # SessionUser, WorkModels
    data/              # WorkRepository (shared HR self-service APIs)
    ui/theme|brand/    # Material theme + Novora lockup
  feature/
    auth/              # Landing, Login, Register, AuthRepository, AuthViewModel
    home/
    attendance/
    leave/
    approvals/
    profile/           # Account + sign out
    shell/             # MainShell tabs
```

## Old → new

| Old | New |
|-----|-----|
| `MainActivity.kt` | `app/MainActivity.kt` |
| `ui/NovoraApp.kt` | `app/NovoraApp.kt` |
| `data/network/*` | `core/network/*` |
| `data/SessionUser.kt`, `WorkModels.kt` | `core/model/*` |
| `data/WorkRepository.kt` | `core/data/WorkRepository.kt` |
| `data/auth/*`, `ui/AuthViewModel.kt`, `ui/auth/*` | `feature/auth/*` |
| `ui/home|attendance|leave|approvals|shell` | `feature/*` |
| `ui/theme/*`, `ui/brand/*` | `core/ui/*` |
