/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.core;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ProcFileReaderTest {

  TemporaryFolder mFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    mFolder.create();
  }

  @Test
  public void testSingleNumber() throws Exception {
    String contents = "979200";
    String testPath = createFile(contents);
    ProcFileReader reader = new ProcFileReader(testPath).start();

    assertThat(reader.readNumber()).isEqualTo(979200);
    assertThat(reader.hasReachedEOF()).isTrue();

    assertThat(reader.reset().readNumber()).isEqualTo(979200);
    assertThat(reader.hasReachedEOF()).isTrue();
  }

  @Test
  public void testDoubleNumbers() throws Exception {
    String testPath = createFile("123 456");
    ProcFileReader reader = new ProcFileReader(testPath).start();

    assertThat(reader.readNumber()).isEqualTo(123);
    reader.skipSpaces();
    assertThat(reader.readNumber()).isEqualTo(456);
    assertThat(reader.hasReachedEOF()).isTrue();
  }

  @Test
  public void testMultipleLines() throws Exception {
    String testPath = createFile("123 456\n789 1000");
    ProcFileReader reader = new ProcFileReader(testPath).start();

    long numbers[] = new long[4];
    int position = 0;
    while (reader.hasNext()) {
      numbers[position++] = reader.readNumber();
      reader.skipSpaces();
      numbers[position++] = reader.readNumber();
      reader.skipLines();
    }

    assertThat(numbers).isEqualTo(new long[] {123, 456, 789, 1000});
  }

  private String createFile(String contents) throws IOException {
    File file = mFolder.newFile();
    FileOutputStream os = new FileOutputStream(file, false);
    os.write(contents.getBytes());
    return file.getAbsolutePath();
  }
}
