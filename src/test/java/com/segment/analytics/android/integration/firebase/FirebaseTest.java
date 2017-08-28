package com.segment.analytics.android.integration.firebase;

import android.app.Activity;
import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.segment.analytics.Analytics;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.android.integrations.firebase.FirebaseIntegration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.test.IdentifyPayloadBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static android.R.attr.value;
import static com.segment.analytics.Utils.createTraits;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.when;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.segment.analytics.Analytics.LogLevel.VERBOSE;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class FirebaseTest {

    FirebaseIntegration integration;
    FirebaseAnalytics firebase;

    @Mock
    Analytics analytics;
    @Mock
    Application application;

    @Before
    public void setUp() {
        initMocks(this);

        firebase = mock(FirebaseAnalytics.class);

        when(analytics.getApplication()).thenReturn(application);
        integration = new FirebaseIntegration(application, Logger.with(VERBOSE));
    }

    @Test
    public void activityResume() {
        Activity activity = mock(Activity.class);

    }

    @Test
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

    }

    public void identifyWithMultiWordUserProperties() {

    }

    public void trackWithMultiWordEventProperties() {

    }

}
