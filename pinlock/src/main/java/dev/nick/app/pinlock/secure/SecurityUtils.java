package dev.nick.app.pinlock.secure;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.util.Locale;

public class SecurityUtils {

    public static PinKey text2PinKey(String text) {
        for (PinKey key : PinKey.values()) {
            if (key.toString().equalsIgnoreCase(text)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Convert a chain of bytes into a {@link String}
     *
     * @param bytes The chain of bytes
     * @return The converted String
     */
    private static String bytes2Hex(byte[] bytes) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < bytes.length; n++) {
            stmp = (Integer.toHexString(bytes[n] & 0XFF));
            if (stmp.length() == 1) {
                hs += "0" + stmp;
            } else {
                hs += stmp;
            }
        }
        return hs.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Allows to get the SHA1 of a {@link String} using {@link MessageDigest}
     */
    public static String getSHA1(String text) {
        String sha1 = null;
        if (TextUtils.isEmpty(text)) {
            return sha1;
        }
        MessageDigest sha1Digest = null;
        try {
            sha1Digest = MessageDigest.getInstance("SHA-1");
        } catch (Exception e) {
            return sha1;
        }
        byte[] textBytes = text.getBytes();
        sha1Digest.update(textBytes, 0, text.length());
        byte[] sha1hash = sha1Digest.digest();
        return bytes2Hex(sha1hash);
    }
}
