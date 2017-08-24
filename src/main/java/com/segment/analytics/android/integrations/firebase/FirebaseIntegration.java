package com.segment.analytics.android.integrations.firebase;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.segment.analytics.Analytics;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.ScreenPayload;
import com.segment.analytics.integrations.TrackPayload;

import java.util.List;
import java.util.Map;

import static com.segment.analytics.internal.Utils.hasPermission;
import static com.segment.analytics.internal.Utils.isNullOrEmpty;

/**
 * Google Analytics for Firebase is a free app measurement solution that provides insight on app
 * usage and user engagement.
 *
 * @see <a href="https://firebase.google.com/docs/analytics/">Google Analytics for Firebase</a>
 * @see <a href="#">Google Analytics for Firebase Integration</a>
 * @see <a href="#">Google Analytics for Firebase Android SDK</a>
 */
public class FirebaseIntegration extends Integration<FirebaseAnalytics> {

  public static final Factory FACTORY =
      new Factory() {
        @Override
        public Integration<?> create(ValueMap settings, Analytics analytics) {
          Logger logger = analytics.logger(FIREBASE_ANALYTICS_KEY);
          if (!hasPermission(
              analytics.getApplication(), Manifest.permission.ACCESS_NETWORK_STATE)) {
            logger.debug("ACCESS_NETWORK_STATE is required for Firebase Analytics.");
            return null;
          }
          if (!hasPermission(analytics.getApplication(), Manifest.permission.INTERNET)) {
            logger.debug("INTERNET is required for Firebase Analytics.");
            return null;
          }
          if (!hasPermission(analytics.getApplication(), Manifest.permission.WAKE_LOCK)) {
            logger.debug("WAKE_LOCK is required for Firebase Analytics.");
            return null;
          }

          Context context = analytics.getApplication();

          return new FirebaseIntegration(context);
        }

        @Override
        public String key() {
          return FIREBASE_ANALYTICS_KEY;
        }
      };

  private static final String FIREBASE_ANALYTICS_KEY = "Firebase";

  final FirebaseAnalytics mFirebaseAnalytics;

  public FirebaseIntegration(Context context) {
    mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
  }

  @Override
  public void onActivityStarted(Activity activity) {
    super.onActivityStarted(activity);
  }

  @Override
  public void onActivityStopped(Activity activity) {
    super.onActivityStopped(activity);
  }

  @Override
  public void identify(IdentifyPayload identify) {
    super.identify(identify);

    if (!isNullOrEmpty(identify.userId())) {
      mFirebaseAnalytics.setUserId(identify.userId());

      Map<String, Object> traits = identify.traits();

      for (Map.Entry<String, Object> entry : identify.traits().entrySet()) {
        String trait = entry.getKey();
        String value = String.valueOf(entry.getValue());
        mFirebaseAnalytics.setUserProperty(trait, value);
      }

    }

  }

  @Override
  public void screen(ScreenPayload screen) {
    super.screen(screen);

//    mFirebaseAnalytics.setCurrentScreen(this, screen.name(), null /* class override */);
  }

  @Override
  public void track(TrackPayload track) {
    super.track(track);
  }
}
