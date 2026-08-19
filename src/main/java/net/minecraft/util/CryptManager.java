package net.minecraft.util;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CryptManager
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static SecretKey createNewSharedKey()
    {
        try
        {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            return keyGenerator.generateKey();
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException)
        {
            throw new Error(noSuchAlgorithmException);
        }
    }

    public static KeyPair generateKeyPair()
    {
        try
        {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            return keyPairGenerator.generateKeyPair();
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException)
        {
            net.minecraft.src.Config.warn(noSuchAlgorithmException.getClass().getName() + ": " + noSuchAlgorithmException.getMessage(), noSuchAlgorithmException);
            LOGGER.error("Key pair generation failed!");
            return null;
        }
    }

    public static byte[] getServerIdHash(String serverId, PublicKey publicKey, SecretKey secretKey)
    {
        try
        {
            return digestOperation("SHA-1", new byte[][] {serverId.getBytes("ISO_8859_1"), secretKey.getEncoded(), publicKey.getEncoded()});
        }
        catch (UnsupportedEncodingException unsupportedEncodingException)
        {
            net.minecraft.src.Config.warn(unsupportedEncodingException.getClass().getName() + ": " + unsupportedEncodingException.getMessage(), unsupportedEncodingException);
            return null;
        }
    }

    private static byte[] digestOperation(String algorithm, byte[]... data)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);

            for (byte[] abyte : data)
            {
                messageDigest.update(abyte);
            }

            return messageDigest.digest();
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException)
        {
            net.minecraft.src.Config.warn(noSuchAlgorithmException.getClass().getName() + ": " + noSuchAlgorithmException.getMessage(), noSuchAlgorithmException);
            return null;
        }
    }

    public static PublicKey decodePublicKey(byte[] encodedKey)
    {
        try
        {
            EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(encodedKeySpec);
        }
        catch (NoSuchAlgorithmException caughtNoSuchAlgorithmException)
        {
            ;
        }
        catch (InvalidKeySpecException caughtInvalidKeySpecException)
        {
            ;
        }

        LOGGER.error("Public key reconstitute failed!");
        return null;
    }

    public static SecretKey decryptSharedKey(PrivateKey key, byte[] secretKeyEncrypted)
    {
        return new SecretKeySpec(decryptData(key, secretKeyEncrypted), "AES");
    }

    public static byte[] encryptData(Key key, byte[] data)
    {
        return cipherOperation(1, key, data);
    }

    public static byte[] decryptData(Key key, byte[] data)
    {
        return cipherOperation(2, key, data);
    }

    private static byte[] cipherOperation(int opMode, Key key, byte[] data)
    {
        try
        {
            return createTheCipherInstance(opMode, key.getAlgorithm(), key).doFinal(data);
        }
        catch (IllegalBlockSizeException illegalBlockSizeException)
        {
            net.minecraft.src.Config.warn(illegalBlockSizeException.getClass().getName() + ": " + illegalBlockSizeException.getMessage(), illegalBlockSizeException);
        }
        catch (BadPaddingException badPaddingException)
        {
            net.minecraft.src.Config.warn(badPaddingException.getClass().getName() + ": " + badPaddingException.getMessage(), badPaddingException);
        }

        LOGGER.error("Cipher data failed!");
        return null;
    }

    private static Cipher createTheCipherInstance(int opMode, String transformation, Key key)
    {
        try
        {
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(opMode, key);
            return cipher;
        }
        catch (InvalidKeyException invalidKeyException)
        {
            net.minecraft.src.Config.warn(invalidKeyException.getClass().getName() + ": " + invalidKeyException.getMessage(), invalidKeyException);
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException)
        {
            net.minecraft.src.Config.warn(noSuchAlgorithmException.getClass().getName() + ": " + noSuchAlgorithmException.getMessage(), noSuchAlgorithmException);
        }
        catch (NoSuchPaddingException noSuchPaddingException)
        {
            net.minecraft.src.Config.warn(noSuchPaddingException.getClass().getName() + ": " + noSuchPaddingException.getMessage(), noSuchPaddingException);
        }

        LOGGER.error("Cipher creation failed!");
        return null;
    }

    public static Cipher createNetCipherInstance(int opMode, Key key)
    {
        try
        {
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher.init(opMode, (Key)key, (AlgorithmParameterSpec)(new IvParameterSpec(key.getEncoded())));
            return cipher;
        }
        catch (GeneralSecurityException generalsecurityexception)
        {
            throw new RuntimeException(generalsecurityexception);
        }
    }
}
