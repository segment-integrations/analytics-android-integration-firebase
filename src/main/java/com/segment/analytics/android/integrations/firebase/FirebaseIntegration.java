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
import com.segment.analytics.integrations.ScreenPayload;
import java.util.ArrayList;
import java.util.HashMap;
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
  private final Logger logger;
  private final FirebaseAnalytics firebaseAnalytics;
  private static final Map<String, String> EVENT_MAPPER = createEventMap();
  private Activity currentActivity;

  private static Map<String, String> createEventMap() {
    Map<String, String> EVENT_MAPPER = new HashMap<>();
    EVENT_MAPPER.put("Product Added", Event.ADD_TO_CART);
    EVENT_MAPPER.put("Checkout Started", Event.BEGIN_CHECKOUT);
    EVENT_MAPPER.put("Order Completed", Event.PURCHASE);
    EVENT_MAPPER.put("Order Refunded", Event.REFUND);
    EVENT_MAPPER.put("Product Viewed", Event.VIEW_ITEM);
    EVENT_MAPPER.put("Product List Viewed", Event.VIEW_ITEM_LIST);
    EVENT_MAPPER.put("Payment Info Entered", Event.ADD_PAYMENT_INFO);
    EVENT_MAPPER.put("Promotion Viewed", Event.VIEW_PROMOTION);
    EVENT_MAPPER.put("Product Added to Wishlist", Event.ADD_TO_WISHLIST);
    EVENT_MAPPER.put("Product Shared", Event.SHARE);
    EVENT_MAPPER.put("Product Clicked", Event.SELECT_CONTENT);
    EVENT_MAPPER.put("Products Searched", Event.SEARCH);
    return EVENT_MAPPER;
  }

  private static final Map<String, String> PROPERTY_MAPPER = createPropertyMap();

  private static Map<String, String> createPropertyMap() {
    Map<String, String> PROPERTY_MAPPER = new HashMap<>();
    PROPERTY_MAPPER.put("category", Param.ITEM_CATEGORY);
    PROPERTY_MAPPER.put("product_id", Param.ITEM_ID);
    PROPERTY_MAPPER.put("name", Param.ITEM_NAME);
    PROPERTY_MAPPER.put("price", Param.PRICE);
    PROPERTY_MAPPER.put("quantity", Param.QUANTITY);
    PROPERTY_MAPPER.put("query", Param.SEARCH_TERM);
    PROPERTY_MAPPER.put("shipping", Param.SHIPPING);
    PROPERTY_MAPPER.put("tax", Param.TAX);
    PROPERTY_MAPPER.put("total", Param.VALUE);
    PROPERTY_MAPPER.put("revenue", Param.VALUE);
    PROPERTY_MAPPER.put("order_id", Param.TRANSACTION_ID);
    PROPERTY_MAPPER.put("currency", Param.CURRENCY);
    PROPERTY_MAPPER.put("products", Param.ITEMS);
    return PROPERTY_MAPPER;
  }

  private static final Map<String, String> PRODUCT_MAPPER = createProductMap();

  private static Map<String, String> createProductMap() {
    Map<String, String> MAPPER = new HashMap<>();
    MAPPER.put("category", Param.ITEM_CATEGORY);
    MAPPER.put("product_id", Param.ITEM_ID);
    MAPPER.put("id", Param.ITEM_ID);
    MAPPER.put("name", Param.ITEM_NAME);
    MAPPER.put("price", Param.PRICE);
    MAPPER.put("quantity", Param.QUANTITY);
    return MAPPER;
  }

  public FirebaseIntegration(Context context, Logger logger) {
    this.logger = logger;
    this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
  }

  @Override
  public void onActivityResumed(Activity activity) {
    super.onActivityResumed(activity);

    PackageManager packageManager = activity.getPackageManager();
    try {
      ActivityInfo info =
          packageManager.getActivityInfo(activity.getComponentName(), PackageManager.GET_META_DATA);
      String activityLabel = info.loadLabel(packageManager).toString();
      firebaseAnalytics.setCurrentScreen(activity, activityLabel, null);
      logger.verbose("firebaseAnalytics.setCurrentScreen(activity, %s, null);", activityLabel);
    } catch (PackageManager.NameNotFoundException e) {
      throw new AssertionError("Activity Not Found: " + e.toString());
    }
  }

  @Override
  public void onActivityStarted(Activity activity) {
    super.onActivityStarted(activity);

    this.currentActivity = activity;
  }

  @Override
  public void onActivityStopped(Activity activity) {
    super.onActivityStopped(activity);

    this.currentActivity = null;
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
      String value = String.valueOf(entry.getValue());
      trait = makeKey(trait);
      firebaseAnalytics.setUserProperty(trait, value);
      logger.verbose("firebaseAnalytics.setUserProperty(%s, %s);", trait, value);
    }
  }

  @Override
  public void track(TrackPayload track) {
    super.track(track);

    String event = track.event();
    String eventName;
    if (EVENT_MAPPER.containsKey(event)) {
      eventName = EVENT_MAPPER.get(event);
    } else {
      eventName = makeKey(event);
    }
    Properties properties = track.properties();
    Bundle formattedProperties = formatProperties(properties);
    firebaseAnalytics.logEvent(eventName, formattedProperties);
    logger.verbose("firebaseAnalytics.logEvent(%s, %s);", eventName, formattedProperties);
  }

  @Override
  public void screen(ScreenPayload screen) {
    super.screen(screen);

    Bundle propertiesBundle = new Bundle();
    for (Map.Entry<String, Object> entry : screen.entrySet()) {
      propertiesBundle.putString(entry.getKey(), entry.getValue().toString());
    }
    propertiesBundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screen.name());
    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, propertiesBundle);
    logger.verbose("firebaseAnalytics.screen(activity, %s, null);", screen.name());
  }

  private static Bundle formatProperties(Properties properties) {
    Bundle bundle = new Bundle();
    if ((properties.revenue() != 0 || properties.total() != 0)
        && isNullOrEmpty(properties.currency())) {
      bundle.putString(Param.CURRENCY, "USD");
    }
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      Object value = entry.getValue();
      String property = entry.getKey();
      if (PROPERTY_MAPPER.containsKey(property)) {
        property = PROPERTY_MAPPER.get(property);
      } else {
        property = makeKey(property);
      }
      if (property.equals(Param.ITEMS) && value != null) {
        List<ValueMap> products = properties.getList("products", ValueMap.class);
        ArrayList<Bundle> mappedProducts = formatProducts(products);
        bundle.putParcelableArrayList(property, mappedProducts);
      } else {
        putValue(bundle, property, value);
      }
    }
    return bundle;
  }

  private static ArrayList<Bundle> formatProducts(List<ValueMap> products) {
    ArrayList<Bundle> mappedProducts = new ArrayList<>();
    if (products == null) return mappedProducts;

    for (ValueMap product : products) {
      Bundle mappedProduct = new Bundle();
      for (Map.Entry<String, Object> innerEntry : product.entrySet()) {
        String key = innerEntry.getKey();
        Object value = innerEntry.getValue();
        if (PRODUCT_MAPPER.containsKey(key)) {
          key = PRODUCT_MAPPER.get(key);
        } else {
          key = makeKey(key);
        }
        putValue(mappedProduct, key, value);
      }
      mappedProducts.add(mappedProduct);
    }
    return mappedProducts;
  }

  private static void putValue(Bundle bundle, String key, Object value) {
    if (value instanceof Integer) {
      int intValue = (int) value;
      bundle.putInt(key, intValue);
    } else if (value instanceof Double) {
      double doubleValue = (double) value;
      bundle.putDouble(key, doubleValue);
    } else if (value instanceof Long) {
      long longValue = (long) value;
      bundle.putLong(key, longValue);
    } else {
      String stringValue = String.valueOf(value);
      bundle.putString(key, stringValue);
    }
  }

  public static String makeKey(String key) {
    String[] forbiddenChars = {".", "-", " ", ":"};
    for (String forbidden : forbiddenChars) {
      if (key.contains(forbidden)) {
        key = key.trim().replace(forbidden, "_");
      }
    }

    return key.substring(0, Math.min(key.length(), 40));
  }
}
