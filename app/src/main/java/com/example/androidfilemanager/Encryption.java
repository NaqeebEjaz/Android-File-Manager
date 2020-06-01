package com.example.androidfilemanager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Encryption {


    private String algorithmType;
    private String vaultPath;
    private String decryptedPath;


    public Encryption(String ALGOTHERM_TYPE, String vaultPath, String decryptedPath) {

        this.algorithmType = ALGOTHERM_TYPE;
        this.vaultPath = vaultPath;
        this.decryptedPath = decryptedPath;
    }

    public byte[] getIV() {
        byte[] iv = null;
        // SecureRandom random = new SecureRandom();
        if (algorithmType.equalsIgnoreCase("AES")) {
            iv = "1234567891234567".getBytes();

        } else if (algorithmType.equalsIgnoreCase("BLOWFISH")) {
            iv = "12345678".getBytes();

        }
        return iv;
    }

    public void encrypt(byte[] key, String filepath) throws IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        //Reading file using path.
        final FileInputStream fileInputStream = new FileInputStream(filepath);
        // /storage/emulated/0/cloud.jpg

        //Seperating filename from filepath
        String filename = filepath.substring(filepath.lastIndexOf("/") + 1);
         String vaultPath = "/storage/emulated/0/Vault/";
        //String vaultPath = Environment.getExternalStorageDirectory().getPath() + "/Vault/";

        final FileOutputStream fileOutputStream = new FileOutputStream(vaultPath + filename);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(getIV());


        SecretKeySpec secretKeySpec = new SecretKeySpec(key,
                algorithmType);
        // Create cipher
        Cipher cipher = Cipher.getInstance(algorithmType + "/CBC/PKCS5PADDING");

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

        // Wrap the output stream
        final CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, cipher);
        // Write bytes


//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//
//
//            }
//        }.start();


        int b;
        byte[] d = new byte[1024];
        long progress = 1024;
        try {
            while ((b = fileInputStream.read(d)) != -1) {
                cipherOutputStream.write(d, 0, b);
                // d denotes data
                // 0 offset byte, from where it starts reading or writing
                // b denotes no of bytes to write
                //   timeBased.updateProgress(size, progress);

            }

            // Flush and close streams.
            cipherOutputStream.flush();
            cipherOutputStream.close();
            fileInputStream.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void decrypt(byte[] key, String filepath) throws IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        //  String vaultPath = Environment.getExternalStorageDirectory().getPath() + "/Vault/";
        String filename = filepath.substring(filepath.lastIndexOf("/") + 1);
        String inputPath = vaultPath + filename;
        final FileInputStream fileInputStream = new FileInputStream(inputPath);
        //  fis.getChannel().size();

        String fileoutputpath = decryptedPath + filename;
        final FileOutputStream fileOutputStream = new FileOutputStream(fileoutputpath);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(getIV());


        SecretKeySpec secretKeySpec = new SecretKeySpec(key,
                algorithmType);
        Cipher cipher = Cipher.getInstance(algorithmType + "/CBC/PKCS5PADDING");

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

        final CipherInputStream cipherInputStream = new CipherInputStream(fileInputStream, cipher);


        byte[] d = new byte[1024];
        int b;
        try {
            while ((b = cipherInputStream.read(d)) != -1) {
                fileOutputStream.write(d, 0, b);
                // d denotes data
                // 0 offset byte, from where it starts reading or writing
                // b denotes no of bytes to write
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            cipherInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public byte[] hashGenerator(String pass) throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, UnsupportedEncodingException, InvalidKeyException {

        byte[] keysecret = (pass).getBytes("UTF-8");
        //  MessageDigest sha = MessageDigest.getInstance("SHA-256");
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] key = messageDigest.digest(keysecret);
        return key;
    }

}

