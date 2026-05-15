package io.github.genkidoudou.utils;


import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import java.util.Scanner;

/**
 * Jasypt工具类
 *
 * @author luyanan
 * @since 2026/3/1
 */
public class JasyptUtils {

    public static void main(String[] args) {


        // 从控制台输入密码
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入密钥：");
        String password = scanner.next();
        // 要加密的文本有很多个,可以输入多次,并且输入exit 退出
        while (true) {
            System.out.println("请输入要加密的文本(输入exit退出)：");
            String text = scanner.next();
            if ("exit".equals(text)) {
                break;
            }
            System.out.println("加密后的文本：" + decrypt(text, password));
        }


    }

    public static String decrypt(String text, String password) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm("PBEWithHMACSHA256AndAES_128");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);

        return encryptor.encrypt(text);
    }


}
