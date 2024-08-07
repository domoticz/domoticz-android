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

package nl.hnogames.domoticzapi.Utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializableManager {

    public static void cleanAllSerializableObjects(Context context) {
        removeSerializable(context, "Dashboard");
        removeSerializable(context, "Switches");
        removeSerializable(context, "Weathers");
        removeSerializable(context, "Plans");
        removeSerializable(context, "Cameras");
        removeSerializable(context, "Temperatures");
        removeSerializable(context, "Scenes");
        removeSerializable(context, "Events");
        removeSerializable(context, "UserVariables");
        removeSerializable(context, "Logs");
        removeSerializable(context, "Utilities");
    }

    /**
     * Saves a serializable object.
     *
     * @param context      The application context.
     * @param objectToSave The object to save.
     * @param fileName     The name of the file.
     */
    public static void saveSerializable(Context context, Object objectToSave, String fileName) {
        try {
            File appSpecificInternalStorageDirectory = context.getFilesDir();
            File file = new File(appSpecificInternalStorageDirectory, fileName);
            file.createNewFile();

            ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
            output.writeObject(objectToSave);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a serializable object.
     *
     * @param context  The application context.
     * @param fileName The filename.
     * @return the serializable object.
     */
    public static Object readSerializedObject(Context context, String fileName) {
        Object objectToReturn = null;

        File appSpecificInternalStorageDirectory = context.getFilesDir();
        File file = new File(appSpecificInternalStorageDirectory, fileName);
        if (!file.exists())
            return null;

        try {
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
            objectToReturn = input.readObject();
            input.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return objectToReturn;
    }

    /**
     * Removes a specified file.
     *
     * @param context  The application context.
     * @param filename The name of the file.
     */
    public static void removeSerializable(Context context, String filename) {
        try {
            context.deleteFile(filename);
        } catch (Exception ex) {
        }
    }
}