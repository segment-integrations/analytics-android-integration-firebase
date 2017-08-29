package com.segment.analytics.android.integration.firebase;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.internal.zzcco;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.segment.analytics.Analytics;
import com.segment.analytics.core.tests.BuildConfig;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.android.integrations.firebase.FirebaseIntegration;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import com.segment.analytics.test.TrackPayloadBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static android.R.attr.value;
import static com.segment.analytics.Utils.createTraits;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.segment.analytics.Analytics.LogLevel.VERBOSE;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18, manifest = Config.NONE)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*", "org.json.*" })
@PrepareForTest(FirebaseAnalytics.class)
public class FirebaseTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();
    @Mock
    Analytics analytics;
    @Mock
    Application context;
    @Mock
    FirebaseIntegration integration;
    @Mock
    FirebaseAnalytics firebase;

    @Before
    public void setUp() {
        initMocks(this);
        PowerMockito.mockStatic(FirebaseAnalytics.class);

        when(analytics.getApplication()).thenReturn(context);
        when(firebase.getInstance(context)).thenReturn(firebase);
        integration = new FirebaseIntegration(context, null);

    }


    public void activityResume() {
        Activity activity = mock(Activity.class);
        integration.onActivityStarted(activity);
        verify(firebase).setCurrentScreen(activity, anyString(), null);
    }


    public void identify() {

        Traits traits = createTraits("foo").putAge(20).putName("Chris").putValue("level", 13);
        integration.identify(new IdentifyPayloadBuilder().traits(traits).build());

        verify(firebase).setUserId("foo");
        verify(firebase).setUserProperty("age", "20");
        verify(firebase).setUserProperty("name", "Chris");
        verify(firebase).setUserProperty("level", "13");
    }

    @Test
    public void track() {
        Properties properties =
                new Properties().putValue("label", "bar");

        integration.track(new TrackPayloadBuilder().properties(properties).event("foo").build());

        Bundle bundle = new Bundle();
        bundle.putString("label", "bar");

        verify(firebase).logEvent("foo", bundle);

    }


    public void identifyWithMultiWordUserTraits() {

        Traits traits = createTraits("foo").putValue("hair color", "brown");
        integration.identify(new IdentifyPayloadBuilder().traits(traits).build());

        Bundle bundle = new Bundle();
        bundle.putString("hair_color", "brown");

        verify(firebase).setUserId("foo");

    }


    public void trackWithMultiWordEventProperties() {

        Properties properties =
                new Properties().putValue("foo bar", "baz");

        integration.track(new TrackPayloadBuilder().properties(properties).event("foo").build());

        Bundle bundle = new Bundle();
        bundle.putString("foo_bar", "baz");

        verify(firebase).logEvent("foo_bar", bundle);

    }

}
