package com.segment.analytics.android.integration.firebase;

import static com.segment.analytics.Analytics.LogLevel.VERBOSE;
import static com.segment.analytics.Utils.createTraits;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.segment.analytics.Traits;
import com.segment.analytics.android.integrations.firebase.FirebaseIntegration;
import com.segment.analytics.Properties;
import com.segment.analytics.core.tests.BuildConfig;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import com.segment.analytics.test.TrackPayloadBuilder;
import java.util.Date;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Matchers.argThat;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import static org.powermock.api.mockito.PowerMockito.mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
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
        Traits traits = createTraits("foo");
        integration.identify(new IdentifyPayloadBuilder().traits(traits).build());
        verify(firebase).setUserId("foo");
    }

    @Test
    public void identifyWithTraits() {
        Traits traits = createTraits("foo")
                .putAge(20)
                .putFirstName("bar")
                .putLastName("baz")
                .putValue("anonymousId", 123)
                .putValue("Sign Up Date", new Date(117, 6, 14))
                .putValue("  extra spaces        ", "bar");


        integration.identify(new IdentifyPayloadBuilder().traits(traits).build());

        verify(firebase).setUserId("foo");
        verify(firebase).setUserProperty("firstName", "bar");
        verify(firebase).setUserProperty("lastName", "baz");
        verify(firebase).setUserProperty("anonymousId", "123");
        verify(firebase).setUserProperty("Sign_Up_Date", "Fri Jul 14 00:00:00 PDT 2017");
        verify(firebase).setUserProperty("extra_spaces", "bar");
    }

    @Test
    public void track() {
        integration.track(new TrackPayloadBuilder().event("foo").build());
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
                .putValue("total", 100.0)
                .putValue("  extra spaces   ", "baz");

        integration.track(new TrackPayloadBuilder().properties(properties).event("foo").build());

        Bundle expected = new Bundle();
        expected.putInt("integer", 1);
        expected.putDouble("double", 1.0);
        expected.putString("string", "foo");
        expected.putString("date", "Sun Jan 01 00:00:00 PST 2017");
        expected.putString("key_with_spaces", "bar");
        expected.putDouble("value", 100.0);
        expected.putString("currency", "USD");
        expected.putString("extra_spaces", "baz");

        verify(firebase).logEvent(eq("foo"), bundleEq(expected));
    }

    public static Bundle bundleEq(Bundle expected) {
        return argThat(new BundleObjectMatcher(expected));
    }

    private static class BundleObjectMatcher extends TypeSafeMatcher<Bundle> {
        private final Bundle expected;

        private BundleObjectMatcher(Bundle expected) {
            this.expected = expected;
        }

        @Override public boolean matchesSafely(Bundle bundleObject) {
            // todo: this relies on having the same order
            return expected.toString().equals(bundleObject.toString());
        }

        @Override public void describeTo(Description description) {
            description.appendText(expected.toString());
        }
    }
}