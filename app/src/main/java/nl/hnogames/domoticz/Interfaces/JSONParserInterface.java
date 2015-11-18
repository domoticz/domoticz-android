package nl.hnogames.domoticz.Interfaces;

/**
 * Provides an interface that parses a result from the RequestUtil
 */
public interface JSONParserInterface {

    void parseResult(String result);

    void onError(Exception error);

}
