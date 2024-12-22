package brs.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.jcajce.provider.digest.MD5;

import signumj.crypto.SignumCrypto;
import signumj.entity.SignumID;

public final class Crypto {
  static final SignumCrypto signumCrypto = SignumCrypto.getInstance();

  private Crypto() {
  } //never

  public static MessageDigest sha256() {
    return signumCrypto.getSha256();
  }

  public static MessageDigest shabal256() {
    return signumCrypto.getShabal256();
  }

  public static MessageDigest ripemd160() {
    return signumCrypto.getRipeMD160();
  }

  public static MessageDigest md5() {// TODO unit test
    try {
      return MessageDigest.getInstance("MD5"); // TODO burstkit4j integration
    } catch (NoSuchAlgorithmException e) {
      return new MD5.Digest();
    }
  }

  public static byte[] getPublicKey(String secretPhrase) {
    return signumCrypto.getPublicKey(secretPhrase);
  }

  public static byte[] getPrivateKey(String secretPhrase) {
    return signumCrypto.getPrivateKey(secretPhrase);
  }

  public static byte[] sign(byte[] message, String secretPhrase) {
      return signumCrypto.sign(message, secretPhrase);
  }

  public static boolean verify(byte[] signature, byte[] message, byte[] publicKey, boolean enforceCanonical) {
      return signumCrypto.verify(signature, message, publicKey, enforceCanonical);
  }

  public static byte[] aesEncrypt(byte[] plaintext, byte[] myPrivateKey, byte[] theirPublicKey) {
    return signumCrypto.aesSharedEncrypt(plaintext, myPrivateKey, theirPublicKey);
  }

  public static byte[] aesEncrypt(byte[] plaintext, byte[] myPrivateKey, byte[] theirPublicKey, byte[] nonce) {
    return signumCrypto.aesSharedEncrypt(plaintext, myPrivateKey, theirPublicKey, nonce);
  }

  public static byte[] aesDecrypt(byte[] ivCiphertext, byte[] myPrivateKey, byte[] theirPublicKey) {
    return signumCrypto.aesSharedDecrypt(ivCiphertext, myPrivateKey, theirPublicKey);
  }

  public static byte[] aesDecrypt(byte[] ivCiphertext, byte[] myPrivateKey, byte[] theirPublicKey, byte[] nonce) {
    return signumCrypto.aesSharedDecrypt(ivCiphertext, myPrivateKey, theirPublicKey, nonce);
  }

  public static byte[] getSharedSecret(byte[] myPrivateKey, byte[] theirPublicKey) {
    return signumCrypto.getSharedSecret(myPrivateKey, theirPublicKey);
  }

  public static String rsEncode(long id) {
    return signumCrypto.rsEncode(SignumID.fromLong(id));
  }

  public static long rsDecode(String rsString) {
    return signumCrypto.rsDecode(rsString).getSignedLongId();
  }
}
