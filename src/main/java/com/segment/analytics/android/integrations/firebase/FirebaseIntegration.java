package com.segment.analytics.android.integrations.firebase;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.analytics.FirebaseAnalytics.Event;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.ScreenPayload;
import com.segment.analytics.integrations.TrackPayload;

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.format;
import static com.segment.analytics.integrations.BasePayload.Type.identify;
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

    // format event name
    String event = track.event();
    String eventName = mapEvent(event);

    // format properties
    Properties properties = track.properties();
    Bundle mappedProperties = mapProperties(properties);

    mFirebaseAnalytics.logEvent(eventName, mappedProperties);

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

    // map properties to specced Firebase property names
    // trim whitespace, replace spaces between words with underscores
    final Map<String, String> propertiesMapper = new HashMap<>();
    propertiesMapper.put("Product Added", Event.ADD_TO_CART);

    Bundle bundle = new Bundle();

    // refactor to check data type of each property value
    // map specced Segment properties to specced Firebase Params

    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      String property = entry.getKey();
      String value = String.valueOf(entry.getValue());
      bundle.putString(property, value);
    }

    return bundle;

  }
}
