/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.example.android.apis

import androidx.multidex.MultiDexApplication

/**
 * This is an example of a [android.app.Application] class which extends [MultiDexApplication] in
 * order for mulitdex to work on androids before SDK 21,
 *
 * This can also be used as a central repository for per-process information about your app;
 * however it is recommended to use singletons for that instead rather than merge
 * all of these globals from across your application into one place here.
 */
@Suppress("unused") // Used in AndroidManifest.xml
class ApiDemosApplication : MultiDexApplication() {
    @Suppress("RedundantOverride")
    override fun onCreate() {
        super.onCreate()
    }
}
