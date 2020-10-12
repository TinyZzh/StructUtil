///*
// *
// *
// *          Copyright (c) 2020. - TinyZ.
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *         http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//
//package org.struct.util;
//
//
//import org.junit.jupiter.api.Test;
//
//import javax.crypto.Cipher;
//import javax.crypto.KeyGenerator;
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.security.SecureRandom;
//
///**
// * @author TinyZ.
// * @version 2020.03.06
// */
//public class SupportUtilTest {
//
//    @Test
//    public void test() {
//        SupportUtil.supportExcel2Xml(
//                "E:\\C\\LocalUnity3D\\Game-Mafa\\Assets\\Scripts\\GuHuoZai\\Docs\\GameHUD.xlsx",
//                "E:\\C\\LocalUnity3D\\Game-Mafa\\Assets\\Resources\\Xml");
//    }
//
//    @Test
//    public void decryptRes() {
//        decrypt("E:\\DOWNLOAD\\全民挂机\\Code_Core\\2.140.0\\Utility\\1.png", null);
//    }
//
//    public void decrypt(String filePath, String key) {
//        try (FileInputStream fis = new FileInputStream(new File(filePath))) {
//            int available = fis.available();
////            fis.skip(4);
//            byte[] data = new byte[available - 8];
//            int readed = fis.read(data);
//            //实例化
//            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//
//            //使用密钥初始化，设置为解密模式
//            KeyGenerator kg = KeyGenerator.getInstance("AES");
//            //AES 要求密钥长度为 128
//            kg.init(128, new SecureRandom("nc315XHW^".getBytes()));
//            //生成一个密钥
//            SecretKey secretKey = kg.generateKey();
//
//            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
//            cipher.init(Cipher.DECRYPT_MODE, keySpec);
//
//            //执行操作
//            try (FileOutputStream fos = new FileOutputStream("x.png")) {
//                byte[] result = cipher.doFinal(data);
//                fos.write(result);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//}