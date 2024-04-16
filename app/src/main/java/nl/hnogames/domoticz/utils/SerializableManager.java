/*
 * Copyright (C) 2015 Domoticz - Mark Heinis
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package nl.hnogames.domoticz.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import nl.hnogames.domoticz.cache.JsonCacheManager;

public class SerializableManager {
    private static JsonCacheManager jsonCacheManager;
    private static final Executor executor = Executors.newSingleThreadExecutor();

    public interface InitializationCallback {
        void onInitialized();
    }

    public static void initialize(Context context, InitializationCallback callback) {
        executor.execute(() -> {
            jsonCacheManager = new JsonCacheManager(context);
            new Handler(Looper.getMainLooper()).post(callback::onInitialized);
        });
    }

    public static void cleanAllSerializableObjects(Context context) {
        executor.execute(() -> {
            if (jsonCacheManager == null) {
                initialize(context, () -> executor.execute(() -> jsonCacheManager.clearAllCache()));
            } else {
                executor.execute(() -> jsonCacheManager.clearAllCache());
            }
        });
    }

    public static <T> void saveSerializable(Context context, T objectToSave, String fileName) {
        executor.execute(() -> {
            Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
            String json = gson.toJson(objectToSave);
            if (jsonCacheManager == null) {
                initialize(context, () -> executor.execute(() -> jsonCacheManager.saveJson(fileName, json)));
            } else {
                executor.execute(() -> jsonCacheManager.saveJson(fileName, json));
            }
        });
    }

    public static <T> void readSerializedObject(Context context, String fileName, Type typeOfT, JsonCacheCallback<T> callback) {
        executor.execute(() -> {
            if (jsonCacheManager == null) {
                initialize(context, () -> executor.execute(() -> {
                    try {
                        T result = new Gson().fromJson(jsonCacheManager.getJson(fileName), typeOfT);
                        new Handler(Looper.getMainLooper()).post(() -> callback.onObjectLoaded(result));
                    } catch (Exception e) {
                        // If deserialization fails, remove the cache object
                        removeSerializable(context, fileName);
                    }
                }));
            } else {
                executor.execute(() -> {
                    try {
                        T result = new Gson().fromJson(jsonCacheManager.getJson(fileName), typeOfT);
                        new Handler(Looper.getMainLooper()).post(() -> callback.onObjectLoaded(result));
                    } catch (Exception e) {
                        // If deserialization fails, remove the cache object
                        removeSerializable(context, fileName);
                    }
                });
            }
        });
    }

    public static <T> void readSerializedObject(Context context, String fileName, Class<T> classOfT, JsonCacheCallback<T> callback) {
        executor.execute(() -> {
            if (jsonCacheManager == null) {
                initialize(context, () -> executor.execute(() -> {
                    try {
                        T result = new Gson().fromJson(jsonCacheManager.getJson(fileName), classOfT);
                        new Handler(Looper.getMainLooper()).post(() -> callback.onObjectLoaded(result));
                    } catch (Exception e) {
                        // If deserialization fails, remove the cache object
                        removeSerializable(context, fileName);
                    }
                }));
            } else {
                executor.execute(() -> {
                    try {
                        T result = new Gson().fromJson(jsonCacheManager.getJson(fileName), classOfT);
                        new Handler(Looper.getMainLooper()).post(() -> callback.onObjectLoaded(result));
                    } catch (Exception e) {
                        // If deserialization fails, remove the cache object
                        removeSerializable(context, fileName);
                    }
                });
            }
        });
    }

    public static void removeSerializable(Context context, String filename) {
        executor.execute(() -> {
            if (jsonCacheManager == null) {
                initialize(context, () -> executor.execute(() -> jsonCacheManager.removeCache(filename)));
            } else {
                executor.execute(() -> jsonCacheManager.removeCache(filename));
            }
        });
    }

    public interface JsonCacheCallback<T> {
        void onObjectLoaded(T object);
    }
}