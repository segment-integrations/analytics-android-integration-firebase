2.3.3 / 2022-06-13
==================
* [Fix](https://github.com/segment-integrations/analytics-android-integration-firebase/pull/43): Upgrade Segment SDK version

2.3.2 / 2021-10-05
==================
* [Fix](https://github.com/segment-integrations/analytics-android-integration-firebase/pull/41): add null check on formatProducts

2.3.1 / 2021-09-09
==================
* [Fix](https://github.com/segment-integrations/analytics-android-integration-firebase/pull/39): Catch error when products=null

2.3.0 / 2021-05-03
==================
  * [Fix](https://github.com/segment-integrations/analytics-android-integration-firebase/pull/36): Fix product key mappings
  * [New](https://github.com/segment-integrations/analytics-android-integration-firebase/pull/37): `makeKey` Add restriction for `:`

2.2.0 / 2021-03-23
==================
  * [Fix](https://github.com/segment-integrations/analytics-android-integration-firebase/pull/32): Logic to catch forbidden chars in keys
  * [Fix](https://github.com/segment-integrations/analytics-android-integration-firebase/pull/33): Apply correct transformation for Products Searched
  * [New](https://github.com/segment-integrations/analytics-android-integration-firebase/pull/34): Bump internal firebase SDK to 18.0.0

2.1.1 / 2020-04-21
==================
  * Adds check on `makeKey` to replace `-` to `_` on properties and event names.

2.1.0 / 2020-02-20
==================
  * Add support for explicitly tracking screen calls.

2.0.0 / 2019-11-15
==================
*(Supports Android 29+ and Gradle 3.2.1+)*

  * Bump Firebase to 17.2.1

1.4.0 / 2019-09-09
==================

  * Bump Firebase to 17.2.0

1.3.1 / 2019-07-17
==================

  * Change to `implementation`

1.3.0 / 2019-07-16
==================
*(Supports analytics-android 4.3.1 and Firebase Core 17.0.1)*

  * [DEST-854] Update build and bump Firebase to 17.0.1 (#19)

Version 1.2.0 (3rd October, 2018)
===================================
*(Supports analytics-android 4.2.6 and Firebase Core 16.0.3)*

  * Bumps Firebase Core dependency to 16.0.3.

Version 1.1.0 (11th September, 2017)
===================================
*(Supports analytics-android 4.2.6 and Firebase Core 11.2.0)*

  * Sends dates in default `new Date()` object format
  * No longer auto-formats dates "YYYY-MM-DD"

Version 1.0.0 (7th September, 2017)
===================================
*(Supports analytics-android 4.2.6 and Firebase Core 11.2.0)*

  * Initial Release
