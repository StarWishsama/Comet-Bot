package io.github.starwishsama.namelessbot.utils;

import io.github.starwishsama.namelessbot.BotMain;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AESUtils {
    private static final String KEY_ALGORITHM = "AES";

    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";//默认的加密算法

    /**
     * AES 加密操作
     *
     * @param content 待加密内容
     * @param password 加密密码
     * @return 返回Base64转码后的加密数据
     */

    public static String encrypt(String content, String password) {

        try {

            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);// 创建密码器

            byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);

            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password));// 初始化为加密模式的密码器

            byte[] result = cipher.doFinal(byteContent);// 加密

            return Base64.encodeBase64String(result);//通过Base64转码返回

        } catch (Exception ex) {
            BotMain.getLogger().warning("[AES] " + ex);
        }

        return null;

    }


    /**
     * AES 解密操作
     * @param content
     * @param password
     * @return
     */

    public static String decrypt(String content, String password) {
        try {
            //实例化
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            //使用密钥初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password));
            //执行操作
            byte[] result = cipher.doFinal(Base64.decodeBase64(content));

            return new String(result, StandardCharsets.UTF_8);

        } catch (Exception ex) {
            BotMain.getLogger().warning("[AES] " + ex);
        }

        return null;

    }

    /**

     * 生成加密秘钥

     * @return 密钥

     */

    private static SecretKeySpec getSecretKey(final String password) {

        //返回生成指定算法密钥生成器的 KeyGenerator 对象

        KeyGenerator kg = null;

        try {

            kg = KeyGenerator.getInstance(KEY_ALGORITHM);

            //AES 要求密钥长度为 128

            kg.init(128, new SecureRandom(password.getBytes()));

            //生成一个密钥

            SecretKey secretKey = kg.generateKey();

            return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);// 转换为AES专用密钥

        } catch (NoSuchAlgorithmException ex) {
            BotMain.getLogger().warning("[AES] " + ex);
        }

        return null;

    }
}
