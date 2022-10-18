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

package com.securityandsafetythings.examples.helloworld.utils;

import android.content.res.AssetManager;
import android.util.Log;
import com.securityandsafetythings.examples.helloworld.TfLiteDetectorApplication;

/**
 * Class that contains helper methods to retrieve information from the assets folder.
 */
public final class AssetsHelper {
    private static AssetsHelper sInstance = null;
    private static final String LOGTAG = AssetsHelper.class.getSimpleName();
    private final AssetManager mAssetManager;

    private AssetsHelper() {
        mAssetManager = TfLiteDetectorApplication.getAppContext().getResources().getAssets();
    }

    /**
     * Gets an instance of this class.
     *
     * @return The Singleton instance of this class.
     */
    public static synchronized AssetsHelper getInstance() {
        // If no instance exists create one. Else return existing instance
        if (sInstance == null) {
            sInstance = new AssetsHelper();
        }
        return sInstance;
    }

    /**
     * Iterates over the assets file path and checks how many files with the given file extension exist in the given folder.
     *
     * @param filePath The path where the files reside.
     * @param fileType The extension of files that we are looking for.
     * @return Number of files that exist with the given extension, or -1 if the method fails to get the file list,
     * or if filePath is null.
     */
    public int getNumberOfFilesInPath(final String filePath, final String fileType) {
        final String[] filesInPath;
        try {
            filesInPath = mAssetManager.list(filePath);
            if (filesInPath == null) {
                Log.e(LOGTAG, "AssetManager#list() returned null for the given file path " + filePath);
                return -1;
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Error occurred while trying to list the assets in " + filePath + ": " + e.getMessage(), e);
            return -1;
        }
        int filesQty = 0;
        for (String file : filesInPath) {
            if (file.endsWith(fileType)) {
                ++filesQty;
            }
        }
        return filesQty;
    }
}
