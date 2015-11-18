package nl.hnogames.domoticz.Utils;

public class UsefulBits {

    public static boolean isStringEmpty(String string) {
        boolean isEmpty = false;

        if (string.equalsIgnoreCase("") || string.isEmpty()) {
            isEmpty = true;
        }
        return isEmpty;
    }

}