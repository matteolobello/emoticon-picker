package io.github.ohmylob.emojipicker.debug;

/*
 * Copyright (C) 2016 Matteo Lobello
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Logger {
    private static final boolean DEBUG = io.github.ohmylob.emojipicker.BuildConfig.DEBUG;
    private static final String TAG = "EmojiFromDroid";

    public static void print(Object what) {
        if (DEBUG) {
            android.util.Log.d(TAG, what != null ? String.valueOf(what) : "null");
        }
    }
}
