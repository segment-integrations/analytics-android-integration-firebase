analytics-android-integration-firebase
=======================================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.segment.analytics.android.integrations/firebase/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.segment.analytics.android.integrations/firebase)
[![Javadocs](http://javadoc-badge.appspot.com/com.segment.analytics.android.integrations/firebase.svg?label=javadoc)](http://javadoc-badge.appspot.com/com.segment.analytics.android.integrations/firebase)

Firebase Analytics integration for [analytics-android](https://github.com/segmentio/analytics-android).

## Installation

To install the Segment-Firebase integration, simply add this line to your gradle file:

```
implementation 'com.segment.analytics.android.integrations:firebase:+@aar'
```

**Note:** The Firebase SDK requires Android resources. To avoid issues with app crashes please implement using the `aar` package. 

### Note
To use firebase-integrations 1.4.+ :
* `Update your app to use (AndroidX) or Android 29`
* `Upgrade com.android.tools.build:gradle to v3.2.1 or later`

For Android 28 or Lower: [here](https://search.maven.org/artifact/com.segment.analytics.android.integrations/firebase/1.2.0/aar)

Don't forget to add the following dependencies to make sure [Firebase works](https://firebase.google.com/docs/android/setup/):
```
apply plugin: 'com.android.application'

android {
  // ...
}

// Add the following line to the bottom of the file:
apply plugin: 'com.google.gms.google-services'  // Google Play services Gradle plugin
```

## Usage

After adding the dependency, you must register the integration with our SDK.  To do this, import the Firebase integration:


```
import com.segment.analytics.android.integrations.firebase.FirebaseIntegration;

```

And add the following line:

```
analytics = new Analytics.Builder(this, "write_key")
                .use(FirebaseIntegration.FACTORY)
                .build();
```

Please see [our documentation](https://segment.com/docs/integrations/firebase-analytics/) for more information.


## License

```
WWWWWW||WWWWWW
 W W W||W W W
      ||
    ( OO )__________
     /  |           \
    /o o|    MIT     \
    \___/||_||__||_|| *
         || ||  || ||
        _||_|| _||_||
       (__|__|(__|__|

The MIT License (MIT)

Copyright (c) 2014 Segment, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
