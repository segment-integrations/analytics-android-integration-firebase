package com.segment.analytics.android.integration.firebase;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.segment.analytics.Properties;
import com.segment.analytics.android.integrations.firebase.FirebaseIntegration;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.ScreenPayload;
import com.segment.analytics.integrations.TrackPayload;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.segment.analytics.Analytics.LogLevel.VERBOSE;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@PowerMockIgnore({ "org.mockito.*", "org.roboelectric.*", "android.*" })
@PrepareForTest(FirebaseAnalytics.class)
public class FirebaseTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();
    private FirebaseAnalytics firebase;
    private FirebaseIntegration integration;

    @Before
    public void setUp() {
        firebase = PowerMockito.mock(FirebaseAnalytics.class);
        Context context = PowerMockito.mock(Context.class);

        PowerMockito.mockStatic(FirebaseAnalytics.class);
        Mockito.when(FirebaseAnalytics.getInstance(context)).thenReturn(firebase);

        integration = new FirebaseIntegration(context, Logger.with(VERBOSE));
    }

    @Test
    public void identify() {
        integration.identify(new IdentifyPayload.Builder().userId("foo").traits(new HashMap<String, Object>()).build());
        verify(firebase).setUserId("foo");
    }

    @Test
    public void identifyWithTraits() {
        Map<String, Object> traits = new HashMap<>();
        traits.put("age", 20);
        traits.put("firstName", "bar");
        traits.put("lastName", "baz");
        traits.put("Sign Up Date", new Date(117, 6, 14));
        traits.put("  extra spaces        ", "bar");

        integration.identify(new IdentifyPayload.Builder().userId("foo").anonymousId("123").traits(traits).build());

        verify(firebase).setUserId("foo");
        verify(firebase).setUserProperty("firstName", "bar");
        verify(firebase).setUserProperty("lastName", "baz");
        verify(firebase).setUserProperty("Sign_Up_Date", String.valueOf(new Date(117, 6, 14)));
        verify(firebase).setUserProperty("extra_spaces", "bar");
        verify(firebase).setUserProperty("age", "20");
    }

    @Test
    public void track() {
        integration.track(new TrackPayload.Builder().anonymousId("12345").event("foo").build());
        verify(firebase).logEvent(eq("foo"), bundleEq(new Bundle()));
    }

    @Test
    public void trackWithProperties() {
        Properties properties = new Properties()
                .putValue("integer", 1)
                .putValue("double", 1.0)
                .putValue("string", "foo")
                .putValue("date", new Date(117, 0, 1))
                .putValue("key with spaces", "bar")
                .putValue("key.with.periods", "test")
                .putValue("total", 100.0)
                .putValue("  extra spaces   ", "baz");

        integration.track(new TrackPayload.Builder().anonymousId("1234").properties(properties).event("foo").build());

        Bundle expected = new Bundle();
        expected.putInt("integer", 1);
        expected.putDouble("double", 1.0);
        expected.putString("string", "foo");
        expected.putString("date", String.valueOf(new Date(117, 0, 1)));
        expected.putString("key_with_spaces", "bar");
        expected.putString("key_with_periods", "test");
        expected.putDouble("value", 100.0);
        expected.putString("currency", "USD");
        expected.putString("extra_spaces", "baz");

        verify(firebase).logEvent(eq("foo"), bundleEq(expected));
    }

    @Test
    public void trackWithEventNameTransformation() {
        Properties properties = new Properties()
                .putValue("integer", 1)
                .putValue("double", 1.0)
                .putValue("string", "foo")
                .putValue("date", new Date(117, 0, 1))
                .putValue("key with spaces", "bar")
                .putValue("key.with.periods", "test")
                .putValue("total", 100.0)
                .putValue("  extra spaces   ", "baz");

        integration.track(new TrackPayload.Builder().anonymousId("1234").properties(properties).event("foo.bar").build());

        Bundle expected = new Bundle();
        expected.putInt("integer", 1);
        expected.putDouble("double", 1.0);
        expected.putString("string", "foo");
        expected.putString("date", String.valueOf(new Date(117, 0, 1)));
        expected.putString("key_with_spaces", "bar");
        expected.putString("key_with_periods", "test");
        expected.putDouble("value", 100.0);
        expected.putString("currency", "USD");
        expected.putString("extra_spaces", "baz");

        verify(firebase).logEvent(eq("foo_bar"), bundleEq(expected));
    }

    @Test
    public void trackScreenWithName() {
        final Activity activity = PowerMockito.mock(Activity.class);
        integration.onActivityStarted(activity);

        integration.screen(new ScreenPayload.Builder().anonymousId("1234").name("home_screen").build());

        verify(firebase).setCurrentScreen(any(Activity.class), eq("home_screen"), (String) isNull());
    }

    @Test
    public void makeKeyWithDash() {
        integration.track(new TrackPayload.Builder().anonymousId("12345").event("test-event-dashed").build());
        verify(firebase).logEvent(eq("test_event_dashed"), bundleEq(new Bundle()));
    }

    @Test
    public void makeKeyWithDot() {
        integration.track(new TrackPayload.Builder().anonymousId("12345").event("test.event").build());
        verify(firebase).logEvent(eq("test_event"), bundleEq(new Bundle()));
    }

    /**
     * Uses the string representation of the object. Useful for JSON objects.
     * @param expected Expected object
     * @return Argument matcher.
     */
    private JSONObject jsonEq(JSONObject expected) {
        return argThat(new JSONMatcher(expected));
    }

    class JSONMatcher implements ArgumentMatcher<JSONObject> {
        JSONObject expected;

        JSONMatcher(JSONObject expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(JSONObject argument) {
            try {
                JSONAssert.assertEquals(expected, argument, JSONCompareMode.STRICT);
                return true;
            } catch (JSONException e) {
                return false;
            }
        }
    }

    public static Bundle bundleEq(Bundle expected) {
        return argThat(new BundleObjectMatcher(expected));
    }

    private static class BundleObjectMatcher implements ArgumentMatcher<Bundle> {
        Bundle expected;

        private BundleObjectMatcher(Bundle expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Bundle bundle) {
            return expected.toString().equals(bundle.toString());
        }

    }
}
