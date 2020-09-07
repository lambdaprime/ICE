package id.ICE.scanners;

/**
 * Scanner for messages where each message are represented
 * as a single line ending with new line (ex. '\n').
 * 
 * The new line is not considered as part of the message.
 */
public class NewLineMessageScanner extends DelimiterMessageScanner {

    public NewLineMessageScanner() {
        super((byte)'\n');
    }

}
