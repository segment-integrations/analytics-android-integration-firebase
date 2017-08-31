package com.segment.analytics.android.integration.firebase;

import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;
import com.segment.analytics.android.integrations.firebase.FirebaseIntegration;
import com.segment.analytics.core.tests.BuildConfig;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.TrackPayload;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import com.segment.analytics.test.TrackPayloadBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static com.segment.analytics.Utils.createTraits;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.segment.analytics.Analytics.LogLevel.VERBOSE;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18, manifest = Config.NONE)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest({FirebaseAnalytics.class, FirebaseIntegration.class})
public class FirebaseTest {
    @Rule public PowerMockRule rule = new PowerMockRule();
    @Mock Application context;
    @Mock Analytics analytics;
    @Mock FirebaseAnalytics firebase;
    FirebaseIntegration integration;


    @Before
    public void setUp() {
        initMocks(this);

        when(context.getApplicationContext()).thenReturn(context);

        PowerMockito.mockStatic(FirebaseAnalytics.class);
        when(FirebaseAnalytics.getInstance(context)).thenReturn(firebase);
        integration = new FirebaseIntegration(context, Logger.with(VERBOSE));
        PowerMockito.mockStatic(FirebaseAnalytics.class);
    }

    @Test
    public void identify() {

        Traits traits = createTraits("foo");

        integration.identify(new IdentifyPayloadBuilder().traits(traits).build());

        verify(firebase).setUserId("foo");

    }

}
