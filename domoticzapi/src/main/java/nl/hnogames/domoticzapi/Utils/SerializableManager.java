
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