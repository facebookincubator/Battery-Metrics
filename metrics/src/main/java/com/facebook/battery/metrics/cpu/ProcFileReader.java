/**
 * Copyright (c) 2017-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the BSD-style license found in the LICENSE file in the root
 * directory of this source tree. An additional grant of patent rights can be found in the PATENTS
 * file in the same directory.
 */
package com.facebook.battery.metrics.cpu;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;

/**
 * A utility class to cheaply read procfiles repeatedly with minimal memory allocations.
 *
 * <p>It internally wraps RandomAccessFile with a buffer to make sure it doesn't constantly do I/O
 * for every byte read.
 *
 * <p>This file is not threadsafe: access to it must be synchronized.
 */
class ProcFileReader {

  private final String mPath;
  private final byte[] mBuffer;
  @Nullable private RandomAccessFile mFile;

  private int mPosition = -1;
  private int mBufferSize;

  private char mChar;
  private char mPrev;

  private boolean mIsValid = true;
  private boolean mRewound = false;

  public ProcFileReader(String path) {
    this(path, 512);
  }

  public ProcFileReader(String path, int bufferSize) {
    mPath = path;
    mBuffer = new byte[bufferSize];
  }

  public ProcFileReader start() {
    return reset();
  }

  public ProcFileReader reset() {
    // Be optimistic
    mIsValid = true;

    // First, try to move the pointer if a file exists
    if (mFile != null) {
      try {
        mFile.seek(0);
      } catch (IOException ioe) {
        close();
      }
    }

    // Otherwise try to open/reopen the file and fail
    if (mFile == null) {
      try {
        mFile = new RandomAccessFile(mPath, "r");
      } catch (IOException ioe) {
        mIsValid = false;
        close();
      }
    }

    if (mIsValid) {
      mPosition = -1;
      mBufferSize = 0;

      mChar = 0;
      mPrev = 0;

      mRewound = false;
    }

    return this;
  }

  public boolean isValid() {
    return mIsValid;
  }

  public boolean hasNext() {
    if (!mIsValid || mFile == null || mPosition > mBufferSize - 1) {
      return false;
    }

    if (mPosition < mBufferSize - 1) {
      return true;
    }

    try {
      mBufferSize = mFile.read(mBuffer);
      mPosition = -1;
    } catch (IOException ioe) {
      mIsValid = false;
      close();
    }

    return hasNext();
  }

  public boolean hasReachedEOF() {
    return mBufferSize == -1;
  }

  private void next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    mPosition++;
    mPrev = mChar;
    mChar = (char) mBuffer[mPosition];

    mRewound = false;
  }

  private void rewind() {
    if (mRewound) {
      throw new ParseException("Can only rewind one step!");
    }

    mPosition--;
    mChar = mPrev;
    mRewound = true;
  }

  public long readNumber() {
    long result = 0;
    boolean isFirstRun = true;

    while (hasNext()) {
      next();
      if (Character.isDigit(mChar)) {
        result = result * 10 + (mChar - '0');
      } else if (isFirstRun) {
        throw new ParseException("Couldn't read number!");
      } else {
        rewind();
        break;
      }

      isFirstRun = false;
    }

    if (isFirstRun) {
      throw new ParseException("Couldn't read number because the file ended!");
    }

    return result;
  }

  public void skipSpaces() {
    skipPast(' ');
  }

  public void skipLines() {
    skipPast('\n');
  }

  public void skipPast(char skipPast) {
    boolean found = false;
    while (hasNext()) {
      next();

      if (mChar == skipPast) {
        found = true;
      } else if (found) {
        rewind();
        break;
      }
    }
  }

  public void close() {
    if (mFile != null) {
      try {
        mFile.close();
      } catch (IOException ioe) {
        // Ignored
      } finally {
        mFile = null;
      }
    }
  }

  public static class ParseException extends RuntimeException {

    public ParseException(String message) {
      super(message);
    }
  }
}
