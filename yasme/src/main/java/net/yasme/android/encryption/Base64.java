package net.yasme.android.encryption;

import java.io.UnsupportedEncodingException;

/**
 * Created by Marco Eberl on 27.07.2014.
 */
public class Base64 {

    public String base64Encode (byte[] input){
        return android.util.Base64.encodeToString(input, android.util.Base64.DEFAULT);
    }

    public byte[] base64Decode (String input, String coding) throws UnsupportedEncodingException {
        return android.util.Base64.decode(input.getBytes(coding), android.util.Base64.DEFAULT);
    }

    public byte[] base64Decode (String input){
        return android.util.Base64.decode(input.getBytes(), android.util.Base64.DEFAULT);
    }
}
