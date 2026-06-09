package core;

import model.KeyPair;
import model.PrivateKey;
import model.PublicKey;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * AlgorithmRSA — toàn bộ logic thuật toán RSA + SHA-256.
 */
public class AlgorithmRSA {

    // ── 1. TẠO KHÓA NGẪU NHIÊN ──────────────────────────────────────────

    /**
     * Sinh P, Q ngẫu nhiên rồi tạo cặp khóa RSA.
     * @param bitLength độ dài bit của mỗi số nguyên tố (tối thiểu 256, khuyến nghị 512 cho demo, 2048 cho thực tế)
     */
    public static KeyPair generateKey(int bitLength) {
        if (bitLength < 16) {
            throw new IllegalArgumentException("bitLength quá nhỏ để sinh khóa RSA an toàn.");
        }
        SecureRandom rng = new SecureRandom();
        BigInteger p, q;
        do {
            p = BigInteger.probablePrime(bitLength, rng);
            q = BigInteger.probablePrime(bitLength, rng);
        } while (p.equals(q));
        return buildKeyPair(p, q);
    }

    // ── 2. TẠO KHÓA TỪ P, Q NHẬP TAY ───────────────────────────────────

    /**
     * Tạo cặp khóa RSA từ P, Q do người dùng nhập thủ công.
     */
    public static KeyPair generateKey(BigInteger p, BigInteger q) {
        validatePQ(p, q);
        return buildKeyPair(p, q);
    }

    // ── 3. TÍNH SHA-256 ──────────────────────────────────────────────────

    /**
     * Tính SHA-256 của mảng byte, trả về chuỗi hex 64 ký tự.
     */
    public static String hash(byte[] data) {
        return bytesToHex(sha256Bytes(data));
    }

    /** Overload tiện lợi cho chuỗi UTF-8 */
    public static String hash(String text) {
        return hash(text.getBytes(StandardCharsets.UTF_8));
    }

    // ── 4. KÝ VĂN BẢN ───────────────────────────────────────────────────

    /**
     * Ký dữ liệu bằng khóa riêng tư.
     * Quy trình: data → SHA-256 → sig = hash^D mod N
     */
    public static BigInteger sign(byte[] data, PrivateKey key) {
        byte[] hashBytes = sha256Bytes(data);
        BigInteger hashInt = new BigInteger(1, hashBytes); // Đảm bảo số dương (unsigned)

        if (hashInt.compareTo(key.getN()) >= 0) {
            throw new IllegalArgumentException(
                    "Modulus N quá nhỏ so với SHA-256 (256-bit). Hãy nhập cặp P, Q lớn hơn.");
        }

        return hashInt.modPow(key.getD(), key.getN());
    }

    /** Overload ký cho chuỗi UTF-8 */
    public static BigInteger sign(String text, PrivateKey key) {
        return sign(text.getBytes(StandardCharsets.UTF_8), key);
    }

    // ── 5. XÁC MINH CHỮ KÝ ─────────────────────────────────────────────

    /**
     * Xác minh tính hợp lệ của chữ ký số.
     * Quy trình: m' = sig^E mod N. So sánh m' với SHA-256 của dữ liệu gốc.
     */
    public static boolean verify(byte[] data, BigInteger signature, PublicKey key) {
        if (signature == null || key == null) return false;
        
        if (signature.compareTo(key.getN()) >= 0 || signature.compareTo(BigInteger.ZERO) < 0) {
            return false; 
        }

        BigInteger decryptedHash = signature.modPow(key.getE(), key.getN());
        BigInteger originalHash = new BigInteger(1, sha256Bytes(data));

        return decryptedHash.equals(originalHash);
    }

    /** Overload verify cho chuỗi UTF-8 */
    public static boolean verify(String text, BigInteger signature, PublicKey key) {
        return verify(text.getBytes(StandardCharsets.UTF_8), signature, key);
    }

    // ── INTERNAL UTILS ──────────────────────────────────────────────────

    private static KeyPair buildKeyPair(BigInteger p, BigInteger q) {
        BigInteger n   = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger e   = chooseE(phi);
        BigInteger d   = e.modInverse(phi);

        return new KeyPair(new PublicKey(n, e), new PrivateKey(n, d));
    }

    private static BigInteger chooseE(BigInteger phi) {
        BigInteger e = BigInteger.valueOf(65537);
        if (e.compareTo(phi) < 0 && phi.gcd(e).equals(BigInteger.ONE)) {
            return e;
        }
        
        e = BigInteger.valueOf(3);
        while (e.compareTo(phi) < 0) {
            if (phi.gcd(e).equals(BigInteger.ONE)) return e;
            e = e.add(BigInteger.TWO);
        }
        throw new IllegalArgumentException("Không tìm thấy E hợp lệ cho cặp P, Q này. Hãy chọn P, Q lớn hơn.");
    }

    private static void validatePQ(BigInteger p, BigInteger q) {
        if (p == null || q == null) throw new IllegalArgumentException("P và Q không được null.");
        if (!p.isProbablePrime(50)) throw new IllegalArgumentException("P không phải số nguyên tố.");
        if (!q.isProbablePrime(50)) throw new IllegalArgumentException("Q không phải số nguyên tố.");
        if (p.equals(q)) throw new IllegalArgumentException("P và Q phải khác nhau.");
    }

    private static byte[] sha256Bytes(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hệ thống không hỗ trợ thư viện SHA-256", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
