
package nl.hnogames.domoticzapi.Parsers;

import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;

public class LogOffParser implements JSONParserInterface {

    private static final String TAG = LogOffParser.class.getSimpleName();

    public LogOffParser() {
        //TODO: extend this parser with status info
    }

    @Override
    public void parseResult(String result) {
    }

    @Override
    public void onError(Exception error) {
    }
}