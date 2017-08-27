package com.segment.analytics.android.integrations.firebase;

import com.google.firebase.analytics.FirebaseAnalytics;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.analytics.FirebaseAnalytics.Event;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.ScreenPayload;
import com.segment.analytics.integrations.TrackPayload;

import java.util.HashMap;
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

          return new FirebaseIntegration(context, logger);
        }

        @Override
        public String key() {
          return FIREBASE_ANALYTICS_KEY;
        }
      };

  private static final String FIREBASE_ANALYTICS_KEY = "Firebase";
  final Logger logger;
  final FirebaseAnalytics mFirebaseAnalytics;

  public FirebaseIntegration(Context context, Logger logger) {
    this.logger = logger;
    mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
  }

  @Override
  public void onActivityStarted(Activity activity) {
    super.onActivityStarted(activity);

    PackageManager packageManager = activity.getPackageManager();
    try {
      ActivityInfo info =
              packageManager.getActivityInfo(activity.getComponentName(), PackageManager.GET_META_DATA);
      CharSequence activityLabel = info.loadLabel(packageManager);

      mFirebaseAnalytics.setCurrentScreen(activity, activityLabel.toString(), null /* class override */);
      logger.verbose("mFirebaseAnalytics.setCurrentScreen(%s, %s, null /* class override */);", activity, activityLabel.toString());

    } catch (PackageManager.NameNotFoundException e) {
      throw new AssertionError("Activity Not Found: " + e.toString());
    }
  }

  @Override
  public void identify(IdentifyPayload identify) {
    super.identify(identify);

    if (!isNullOrEmpty(identify.userId())) {
      mFirebaseAnalytics.setUserId(identify.userId());

      Map<String, Object> traits = identify.traits();

      for (Map.Entry<String, Object> entry : traits.entrySet()) {
        String trait = entry.getKey();
        trait = trait.trim().toLowerCase().replaceAll(" ", "_");
        String value = String.valueOf(entry.getValue());
        mFirebaseAnalytics.setUserProperty(trait, value);
        logger.verbose("mFirebaseAnalytics.setUserProperty(%s, %s);", trait, value);
      }
    }
  }

  @Override
  public void screen(ScreenPayload screen) {
    super.screen(screen);
    logger.verbose("Firebase Analytics does not support manual screen tracking. All screen views"
            + "are collected automatically by their SDK");
  }

  @Override
  public void track(TrackPayload track) {
    super.track(track);

    // format event name
    String event = track.event();
    String eventName = mapEvent(event);

    // format properties
    Properties properties = track.properties();
    Bundle mappedProperties = mapProperties(properties);

    mFirebaseAnalytics.logEvent(eventName, mappedProperties);
    logger.verbose("mFirebaseAnalytics.logEvent(%s, %s);", eventName, mappedProperties);
  }

  private String mapEvent(String event) {

    String eventName = event;

    final Map<String, String> eventMapper = new HashMap<>();
    eventMapper.put("Product Added", Event.ADD_TO_CART);
    eventMapper.put("Checkout Started", Event.BEGIN_CHECKOUT);
    eventMapper.put("Order Completed", Event.ECOMMERCE_PURCHASE);
    eventMapper.put("Order Refunded", Event.PURCHASE_REFUND);
    eventMapper.put("Product Viewed", Event.VIEW_ITEM);
    eventMapper.put("Product List Viewed", Event.VIEW_ITEM_LIST);
    eventMapper.put("Payment Info Entered", Event.ADD_PAYMENT_INFO);
    eventMapper.put("Promotion Viewed", Event.PRESENT_OFFER);
    eventMapper.put("Product Added to Wishlist", Event.ADD_TO_WISHLIST);
    eventMapper.put("Product Shared", Event.SHARE);
    eventMapper.put("Product Clicked", Event.SELECT_CONTENT);
    eventMapper.put("Product Searched", Event.SEARCH);
    eventMapper.put("Promotion Viewed", Event.PRESENT_OFFER);

    if (eventMapper.containsKey(eventName)) {
      eventName = eventMapper.get(eventName);
    }

    if (eventName.contains(" ")) {
      eventName = eventName.trim().toLowerCase().replaceAll(" ", "_");
    }

    return eventName;
  }

  private Bundle mapProperties(Properties properties) {

    if (properties.value() != 0 && isNullOrEmpty(properties.currency())) {
      logger.verbose("You must set `currency` in your event's property object to accurately pass 'value' to Firebase.");
    }

    Bundle bundle = new Bundle();

    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      String property = entry.getKey();

      if (entry.getValue() instanceof Integer) {
        int value = (int) entry.getValue();
        bundle.putInt(property, value);
        logger.verbose("bundle.putInt(%s, %s);", property, value);
      }

      if (entry.getValue() instanceof Double) {
        double value = (double) entry.getValue();
        bundle.putDouble(property, value);
        logger.verbose("bundle.putInt(%s, %s);", property, value);
      }

      if (entry.getValue() instanceof Long) {
        long value = (long) entry.getValue();
        bundle.putLong(property, value);
        logger.verbose("bundle.putInt(%s, %s);", property, value);
      }

      if (entry.getValue() instanceof String) {
        String value = String.valueOf(entry.getValue());
        bundle.putString(property, value);
        logger.verbose("bundle.putInt(%s, %s);", property, value);
      }
    }

    return bundle;
  }
}
