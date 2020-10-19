/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.utils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Base64;

public class SimpleAES {

    private String key;
    private String iv;
    private Cipher cipherEnc;
    private Cipher cipherDec;

    public SimpleAES(String password, String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, InvalidAlgorithmParameterException {
        SecretKey secretKey = new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(
                new PBEKeySpec(password.toCharArray(), salt.getBytes("utf-8"), 65536, 128))
                .getEncoded(),
                "AES");
        cipherEnc = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherDec = Cipher.getInstance("AES/CBC/PKCS5Padding");

        cipherEnc.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] ivBytes = cipherEnc.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
        cipherDec.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));

        this.key = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        this.iv = Base64.getEncoder().encodeToString(ivBytes);
    }

    public SimpleAES(String[] keyIv) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        this.key = keyIv[0];
        this.iv = keyIv[1];

        SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(keyIv[0]), "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(Base64.getDecoder().decode(keyIv[1]));
        cipherEnc = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherDec = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherEnc.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        cipherDec.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
    }

    public String encrypt(String data) throws UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        return Base64.getEncoder().encodeToString(cipherEnc.doFinal(data.getBytes("utf-8")));
    }

    public String decrypt(String data) throws BadPaddingException, IllegalBlockSizeException {
        return new String(cipherDec.doFinal(Base64.getDecoder().decode(data)));
    }


}
