package net.yasme.android.encryption;

import android.util.Log;

import java.io.UnsupportedEncodingException;

/**
 * Created by Marco Eberl on 27.07.2014.
 */
public class Base64 {

    public String base64Encode (byte[] input){
        String encoded = android.util.Base64.encodeToString(input, android.util.Base64.DEFAULT);
        Log.d(getClass().getSimpleName(), "Base64-encode: " + encoded);
        return encoded;

    }

    public byte[] base64Decode (String input, String coding) throws UnsupportedEncodingException {
        Log.d(getClass().getSimpleName(), "Base64-decode: " + input);
        return android.util.Base64.decode(input.getBytes(coding), android.util.Base64.DEFAULT);
    }

    public byte[] base64Decode (String input){
        Log.d(getClass().getSimpleName(), "Base64-decode: " + input);
        return android.util.Base64.decode(input.getBytes(), android.util.Base64.DEFAULT);
    }
}
