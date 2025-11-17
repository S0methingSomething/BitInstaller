# 1.0.0 (2025-11-17)


### Bug Fixes

* add ProGuard rules for Gson TypeToken ([6dbd5b1](https://github.com/S0methingSomething/BitInstaller/commit/6dbd5b1d26f7d5959c07b6c536b6280f04b8c23b))
* add write permissions for semantic-release ([f122bcf](https://github.com/S0methingSomething/BitInstaller/commit/f122bcf9553e965816aaa9cc99969068e8232d58))


### Features

* add semantic-release automation and AdGuard compatibility ([ec8ede1](https://github.com/S0methingSomething/BitInstaller/commit/ec8ede150f7494e35fc529f9ddbebd79adf1f34c))
* Complete app transformation - Security, Architecture, Testing (48/74 tasks) ([f978961](https://github.com/S0methingSomething/BitInstaller/commit/f978961f0e40c94652f8cc0a70d235e784331fd9))


### BREAKING CHANGES

* Certificate validation now uses composite trust manager

Features:
- Semantic-release automation for version management
- Auto-generates changelog and release notes
- Creates GitHub releases with APK attached
- CompositeX509TrustManager for AdGuard compatibility
- System certificates tried first, then pinned certs

Commit message format:
- feat: new feature (minor version bump)
- fix: bug fix (patch version bump)
- BREAKING CHANGE: breaking change (major version bump)
- chore: maintenance (no version bump)
