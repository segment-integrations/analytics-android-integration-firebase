package com.segment.analytics.android.integration.firebase;

import static com.segment.analytics.Analytics.LogLevel.VERBOSE;
import static com.segment.analytics.Utils.createTraits;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import android.content.Context;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.segment.analytics.Traits;
import com.segment.analytics.android.integrations.firebase.FirebaseIntegration;
import com.segment.analytics.core.tests.BuildConfig;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.test.IdentifyPayloadBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest(FirebaseAnalytics.class)
public class FirebaseTest {

  @Rule public PowerMockRule rule = new PowerMockRule();
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
}
