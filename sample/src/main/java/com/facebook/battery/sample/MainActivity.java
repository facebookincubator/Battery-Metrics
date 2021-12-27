/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.battery.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.facebook.battery.metrics.composite.CompositeMetrics;
import com.facebook.battery.metrics.composite.CompositeMetricsCollector;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/** A magical activity that displays the current snapshot */
public class MainActivity extends Activity {

  private final CompositeMetricsCollector mCollector =
      BatteryApplication.INSTANCE.getMetricsCollector();
  private final CompositeMetrics mMetrics = mCollector.createMetrics();
  private TextView mContent;
  private TextView mTriggerNetworkRequest;

  private final HandlerThread mBackgroundHandlerThread = new HandlerThread("background");
  private Handler mBackgroundHandler;

  private int mRequestCount = 0;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mBackgroundHandlerThread.start();
    mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());

    ScrollView parent = new ScrollView(this);
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);

    parent.addView(layout);

    mContent = new TextView(this);
    int padding =
        (int)
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
    mContent.setPadding(padding, padding, padding, padding);
    mContent.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            updateContent();
          }
        });

    mTriggerNetworkRequest = new TextView(this);
    mTriggerNetworkRequest.setText("Trigger a network request");
    mTriggerNetworkRequest.setPadding(padding, padding, padding, padding);
    mTriggerNetworkRequest.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mRequestCount++;
            mTriggerNetworkRequest.setText("Trigger a network request: request #" + mRequestCount);
            mBackgroundHandler.post(
                new Runnable() {
                  @Override
                  public void run() {
                    try {
                      download();
                    } catch (IOException ioe) {
                      Log.e("BatteryMetrics", "Couldn't download", ioe);
                    }
                  }
                });
          }
        });

    updateContent();
    layout.addView(mTriggerNetworkRequest);
    layout.addView(mContent);
    setContentView(parent);
  }

  private void updateContent() {
    mCollector.getSnapshot(mMetrics);
    String text = "Snapshot at " + SystemClock.elapsedRealtime() + ":\n\n" + mMetrics.toString();
    mContent.setText(text);
    Log.d("BatteryMetrics", text);
  }

  private void download() throws IOException {
    HttpsURLConnection connection = null;
    InputStreamReader reader = null;
    URL url = new URL("https://www.facebook.com");
    try {
      connection = (HttpsURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setDoInput(true);
      connection.connect();
      reader = new InputStreamReader(connection.getInputStream());

      char[] buffer = new char[1024];
      int bytes;
      while ((bytes = reader.read(buffer)) != -1) {
        Log.d("BatteryMetrics", "Read " + bytes + " bytes");
      }
    } finally {
      if (reader != null) {
        reader.close();
      }
      if (connection != null) {
        connection.disconnect();
      }
    }
  }
}
