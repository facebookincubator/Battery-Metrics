/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import com.facebook.battery.metrics.composite.CompositeMetrics;
import com.facebook.battery.metrics.composite.CompositeMetricsCollector;

/** A magical activity that displays the current snapshot */
public class MainActivity extends Activity {

  private final CompositeMetricsCollector mCollector =
      BatteryApplication.INSTANCE.getMetricsCollector();
  private final CompositeMetrics mMetrics = mCollector.createMetrics();
  private TextView mContent;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ScrollView parent = new ScrollView(this);

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

    updateContent();
    parent.addView(mContent);
    setContentView(parent);
  }

  private void updateContent() {
    mCollector.getSnapshot(mMetrics);
    String text = "Snapshot at " + SystemClock.elapsedRealtime() + ":\n\n" + mMetrics.toString();
    mContent.setText(text);
    Log.d("BatteryMetrics", text);
  }
}
