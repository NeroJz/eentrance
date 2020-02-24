/**
 * DecryptQR Service
 * Perform OPENSSL decryption of scanned QR code.
 *
 *
 * Author: JZ
 * Date: 13-02-2020
 * Version: 0.0.1
 */


package hk.com.uatech.eticket.eticket.qrCode;

import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DecryptQR {

    static final String ENCRYPTION_KEY = "abc123";
    static final String ALGORITHM = "AES";
    static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    /**
     * Perform decode of scanned data
     * @param input [String - scanned data]
     * @return String
     */
    public static String decode(String input) {
        try {
            byte[] encrypted = Base64.decode(input, Base64.NO_WRAP);
            byte[] iv = Arrays.copyOfRange(encrypted, 0, 16);
            byte[] hMac = Arrays.copyOfRange(encrypted, 16, 48);
            byte[] chiperVal = Arrays.copyOfRange(encrypted, 48, encrypted.length);

            String mHash = getMD5();

            SecretKeySpec secretKeySpec = new SecretKeySpec(mHash.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            IvParameterSpec ivspec = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);

            byte[] cipherByte = cipher.doFinal(chiperVal);

            return new String(cipherByte);

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Get MD5 encryption of secret key
     * @return
     * @throws NoSuchAlgorithmException
     */
    protected static String getMD5() throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("MD5");

        byte[] hashInBytes = md.digest(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

}
