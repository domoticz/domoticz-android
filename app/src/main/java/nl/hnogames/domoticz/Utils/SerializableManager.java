package nl.hnogames.domoticz.Utils;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Copyright (C) 2015 Domoticz
 * <p/>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

public class SerializableManager {

    /**
     * Saves a serializable object.
     *
     * @param context      The application context.
     * @param objectToSave The object to save.
     * @param fileName     The name of the file.
     */
    public static void saveSerializable(Context context, Object objectToSave, String fileName) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(objectToSave);

            objectOutputStream.close();
            fileOutputStream.close();
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

        try {
            FileInputStream fis = context.openFileInput(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            objectToReturn = ois.readObject();

            ois.close();
            fis.close();
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
        context.deleteFile(filename);
    }

}