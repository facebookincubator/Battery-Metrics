/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.facebook.battery.metrics.core;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.CharBuffer;
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
  public void testWord() throws Exception {
    String contents = "some";
    String testPath = createFile(contents);

    ProcFileReader reader = new ProcFileReader(testPath).start();

    CharBuffer buffer = CharBuffer.allocate(20);
    assertThat(reader.readWord(buffer).toString()).isEqualTo("some");
    assertThat(reader.hasReachedEOF()).isTrue();
  }

  @Test
  public void testMultipleWords() throws Exception {
    String contents = "many many words";
    String testPath = createFile(contents);

    ProcFileReader reader = new ProcFileReader(testPath).start();

    assertThat(reader.readWord(CharBuffer.allocate(20)).toString()).isEqualTo("many");

    reader.skipSpaces();

    assertThat(reader.readWord(CharBuffer.allocate(20)).toString()).isEqualTo("many");

    reader.skipSpaces();

    assertThat(reader.readWord(CharBuffer.allocate(20)).toString()).isEqualTo("words");
    assertThat(reader.hasReachedEOF()).isTrue();
  }

  @Test(expected = ProcFileReader.ParseException.class)
  public void testEmptyNumber() throws Exception {
    String contents = "";
    ProcFileReader reader = new ProcFileReader(createFile(contents)).start();
    reader.readNumber();
  }

  @Test(expected = ProcFileReader.ParseException.class)
  public void testEmptyString() throws Exception {
    ProcFileReader reader = new ProcFileReader(createFile("")).start();
    reader.readWord(CharBuffer.allocate(100));
  }

  @Test
  public void testSkipWord() throws Exception {
    String contents = "skippable word";
    String testPath = createFile(contents);

    ProcFileReader reader = new ProcFileReader(testPath).start();

    reader.skipSpaces();
    assertThat(reader.readWord(CharBuffer.allocate(20)).toString()).isEqualTo("word");
    assertThat(reader.hasReachedEOF()).isTrue();
  }

  @Test(expected = ProcFileReader.ParseException.class)
  public void testInvalidNumber() throws Exception {
    String contents = "notanumber";
    String testPath = createFile(contents);

    ProcFileReader reader = new ProcFileReader(testPath).start();
    reader.readNumber();
  }

  @Test
  public void testReset() throws Exception {
    String contents = "notanumber";
    String testPath = createFile(contents);

    ProcFileReader reader = new ProcFileReader(testPath).start();

    assertThat(reader.readWord(CharBuffer.allocate(20)).toString()).isEqualTo(contents);
    assertThat(reader.hasReachedEOF()).isTrue();

    reader.reset();

    assertThat(reader.readWord(CharBuffer.allocate(20)).toString()).isEqualTo(contents);
    assertThat(reader.hasReachedEOF()).isTrue();
  }

  @Test
  public void testSmallBuffer() throws Exception {
    String contents = "some";
    String testPath = createFile(contents);

    ProcFileReader reader = new ProcFileReader(testPath).start();

    assertThat(reader.readWord(CharBuffer.allocate(1)).toString()).isEqualTo(contents);
    assertThat(reader.hasReachedEOF()).isTrue();
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
  public void testNegativeNumber() throws Exception {
    String contents = "-979200";
    String testPath = createFile(contents);
    ProcFileReader reader = new ProcFileReader(testPath).start();

    assertThat(reader.readNumber()).isEqualTo(-979200);
    assertThat(reader.hasReachedEOF()).isTrue();

    assertThat(reader.reset().readNumber()).isEqualTo(-979200);
    assertThat(reader.hasReachedEOF()).isTrue();
  }

  @Test
  public void testDoubleNumbersMixed() throws Exception {
    String testPath = createFile("-123 456");
    ProcFileReader reader = new ProcFileReader(testPath).start();

    assertThat(reader.readNumber()).isEqualTo(-123);
    reader.skipSpaces();
    assertThat(reader.readNumber()).isEqualTo(456);
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
      reader.skipLine();
    }

    assertThat(numbers).isEqualTo(new long[] {123, 456, 789, 1000});
  }

  @Test
  public void testMultipleLinesWithNegativeNumbers() throws Exception {
    String testPath = createFile("123 -456\n-789 1000");
    ProcFileReader reader = new ProcFileReader(testPath).start();

    long numbers[] = new long[4];
    int position = 0;
    while (reader.hasNext()) {
      numbers[position++] = reader.readNumber();
      reader.skipSpaces();
      numbers[position++] = reader.readNumber();
      reader.skipLine();
    }

    assertThat(numbers).isEqualTo(new long[] {123, -456, -789, 1000});
  }

  private String createFile(String contents) throws IOException {
    File file = mFolder.newFile();
    FileOutputStream os = new FileOutputStream(file, false);
    os.write(contents.getBytes());
    return file.getAbsolutePath();
  }
}
