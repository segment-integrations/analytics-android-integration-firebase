package com.segment.analytics.android.integrations.firebase;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.analytics.FirebaseAnalytics.Event;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.TrackPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static com.segment.analytics.internal.Utils.hasPermission;
import static com.segment.analytics.internal.Utils.isNullOrEmpty;
import static com.segment.analytics.internal.Utils.toISO8601Date;

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

  static final Pattern ISO_DATE =
          Pattern.compile("(^([0-9]{4})-(1[0-2]|0[1-9])-(0[1-9]|1[1-9]|2[0-9]|3[0-1]).*)", CASE_INSENSITIVE);
  private static final String FIREBASE_ANALYTICS_KEY = "Firebase";
  final Logger logger;
  final FirebaseAnalytics firebaseAnalytics;
  private static final Map<String, String> eventMapper = createEventMap();
  private static Map<String, String> createEventMap()
  {
    Map<String,String> eventMapper = new HashMap<>();
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
    return eventMapper;
  }
  private static final Map<String, String> propertyMapper = createPropertyMap();
  private static Map<String, String> createPropertyMap()
  {
    Map<String,String> propertyMapper = new HashMap<>();
    propertyMapper.put("category", Param.ITEM_CATEGORY);
    propertyMapper.put("product_id", Param.ITEM_ID);
    propertyMapper.put("name", Param.ITEM_NAME);
    propertyMapper.put("price", Param.PRICE);
    propertyMapper.put("quantity", Param.QUANTITY);
    propertyMapper.put("query", Param.SEARCH_TERM);
    propertyMapper.put("shipping", Param.SHIPPING);
    propertyMapper.put("tax", Param.TAX);
    propertyMapper.put("total", Param.VALUE);
    propertyMapper.put("revenue", Param.VALUE);
    propertyMapper.put("order_id", Param.TRANSACTION_ID);
    propertyMapper.put("currency", Param.CURRENCY);
    return propertyMapper;
  }

  public FirebaseIntegration(Context context, Logger logger) {
    this.logger = logger;
    firebaseAnalytics = FirebaseAnalytics.getInstance(context);
  }

  @Override
  public void onActivityResumed(Activity activity) {
    super.onActivityResumed(activity);

    PackageManager packageManager = activity.getPackageManager();
    try {
      ActivityInfo info =
              packageManager.getActivityInfo(activity.getComponentName(), PackageManager.GET_META_DATA);
      String activityLabel = info.loadLabel(packageManager).toString();
      firebaseAnalytics.setCurrentScreen(
              activity, activityLabel, null);
      logger.verbose(
              "firebaseAnalytics.setCurrentScreen(activity, %s, null /* class override */);",
              activityLabel);

    } catch (PackageManager.NameNotFoundException e) {
      throw new AssertionError("Activity Not Found: " + e.toString());
    }
  }

  @Override
  public void identify(IdentifyPayload identify) {
    super.identify(identify);

    if (!isNullOrEmpty(identify.userId())) {
      firebaseAnalytics.setUserId(identify.userId());
    }
    Map<String, Object> traits = identify.traits();
    for (Map.Entry<String, Object> entry : traits.entrySet()) {
      String trait = entry.getKey();
      trait = trait.trim().replaceAll(" ", "_");
      if (trait.length() > 40) {
        trait = trimKey(trait);
      }
      String formattedValue;
      if (entry.getValue() instanceof Date) {
        Date value = (Date) entry.getValue();
        formattedValue = formatDate(value);
      } else if (entry.getValue() instanceof String && ISO_DATE.matcher(String.valueOf(entry.getValue())).matches()) {
        formattedValue = String.valueOf(entry.getValue()).substring(0, 10);
      } else {
        formattedValue = String.valueOf(entry.getValue());
      }
      firebaseAnalytics.setUserProperty(trait, formattedValue);
      logger.verbose("firebaseAnalytics.setUserProperty(%s, %s);", trait, formattedValue);
    }
  }

  @Override
  public void track(TrackPayload track) {
    super.track(track);

    String event = track.event();
    String eventName = mapEvent(event);
    Properties properties = track.properties();
    Bundle formattedProperties = formatProperties(properties);
    firebaseAnalytics.logEvent(eventName, formattedProperties);
    logger.verbose("firebaseAnalytics.logEvent(%s, %s);", eventName, formattedProperties);
  }

  private String mapEvent(String event) {
    String eventName = event;
    if (eventMapper.containsKey(eventName)) {
      eventName = eventMapper.get(eventName);
    }
    eventName = eventName.trim().replaceAll(" ", "_");
    if (eventName.length() > 40) {
      eventName = trimKey(eventName);
    }
    return eventName;
  }

  private Bundle formatProperties(Properties properties) {
    Bundle bundle = new Bundle();
    if ((properties.revenue() != 0 || properties.total() != 0) && isNullOrEmpty(properties.currency())) {
      bundle.putString(Param.CURRENCY, "USD");
    }
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      String property = entry.getKey();
      property  = mapProperty(property);
      if (entry.getValue() instanceof Integer) {
        int value = (int) entry.getValue();
        bundle.putInt(property, value);
        logger.verbose("bundle.putInt(%s, %s);", property, value);
        continue;
      }
      if (entry.getValue() instanceof Double) {
        double value = (double) entry.getValue();
        bundle.putDouble(property, value);
        logger.verbose("bundle.putDouble(%s, %s);", property, value);
        continue;
      }
      if (entry.getValue() instanceof Long) {
        long value = (long) entry.getValue();
        bundle.putLong(property, value);
        logger.verbose("bundle.putLong(%s, %s);", property, value);
        continue;
      }
      if (entry.getValue() instanceof String) {
        String value = String.valueOf(entry.getValue());
        if (ISO_DATE.matcher(value).matches()) {
          value = value.substring(0, 10);
        }
        bundle.putString(property, value);
        logger.verbose("bundle.putString(%s, %s);", property, value);
        continue;
      }
      if (entry.getValue() instanceof Date) {
        Date value = (Date) entry.getValue();
        String formattedDate = formatDate(value);
        bundle.putString(property, formattedDate);
        logger.verbose("bundle.putString(%s, %s);", property, formattedDate);
        continue;
      }
    }
    return bundle;
  }

  private String mapProperty(String property) {
    if (propertyMapper.containsKey(property)) {
      property = propertyMapper.get(property);
    }
    property = property.trim().replaceAll(" ", "_");
    if (property.length() > 40) {
      property = trimKey(property);
    }
    return property;
  }

  private String trimKey(String string) {
    return string.substring(0, Math.min(string.length(), 40));
  }

  private String formatDate(Date date) {
    String stringifiedValue = toISO8601Date(date);
    String truncatedValue = stringifiedValue.substring(0, 10);
    return truncatedValue;
  }
}
