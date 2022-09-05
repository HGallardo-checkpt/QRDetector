package com.securityandsafetythings.examples.helloworld;
/*
 * Copyright 2019-2020 by Security and Safety Things GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Application;
import android.content.Context;

/**
 * Main Application class
 */
public class TfLiteDetectorApplication extends Application {

    private static Context sAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = getApplicationContext();
    }

    /**
     * Get application context.
     * @throws NullPointerException If application context is null
     * @return application context
     */
    public static Context getAppContext() throws NullPointerException {

        if (sAppContext == null) {
            throw new NullPointerException("Application context is null");
        }

        return sAppContext;
    }

}
