/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.search;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.provider.SearchIndexableResource;
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.android.settings.core.PreferenceController;
import com.android.settings.search2.XmlParserUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A basic SearchIndexProvider that returns no data to index.
 */
public class BaseSearchIndexProvider implements Indexable.SearchIndexProvider {

    private static final String TAG = "BaseSearchIndex";
    private static final List<String> EMPTY_LIST = new ArrayList<>();

    public BaseSearchIndexProvider() {
    }

    @Override
    public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
        return null;
    }

    @Override
    public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
        return null;
    }

    @Override
    @CallSuper
    public List<String> getNonIndexableKeys(Context context) {
        if (!isPageSearchEnabled(context)) {
            // Entire page should be suppressed, mark all keys from this page as non-indexable.
            return getNonIndexableKeysFromXml(context);
        }
        final List<PreferenceController> controllers = getPreferenceControllers(context);
        if (controllers != null && !controllers.isEmpty()) {
            final List<String> nonIndexableKeys = new ArrayList<>();
            for (PreferenceController controller : controllers) {
                controller.updateNonIndexableKeys(nonIndexableKeys);
            }
            return nonIndexableKeys;
        } else {
            return EMPTY_LIST;
        }
    }

    @Override
    public List<PreferenceController> getPreferenceControllers(Context context) {
        return null;
    }

    /**
     * Returns true if the page should be considered in search query. If return false, entire page
     * will be suppressed during search query.
     */
    protected boolean isPageSearchEnabled(Context context) {
        return true;
    }

    private List<String> getNonIndexableKeysFromXml(Context context) {
        final List<SearchIndexableResource> resources = getXmlResourcesToIndex(
                context, true /* not used*/);
        if (resources == null || resources.isEmpty()) {
            return EMPTY_LIST;
        }
        final List<String> nonIndexableKeys = new ArrayList<>();
        for (SearchIndexableResource res : resources) {
            final XmlResourceParser parser = context.getResources().getXml(res.xmlResId);
            final AttributeSet attrs = Xml.asAttributeSet(parser);
            try {
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    final String key = XmlParserUtils.getDataKey(context, attrs);
                    if (!TextUtils.isEmpty(key)) {
                        nonIndexableKeys.add(key);
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                Log.w(TAG, "Error parsing non-indexable from xml " + res.xmlResId);
            }
        }
        return nonIndexableKeys;
    }

}
