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
 *
 * Các method partner cần gọi:
 *   KeyPair kp  = AlgorithmRSA.generateKey(512);
 *   KeyPair kp2 = AlgorithmRSA.generateKey(p, q);
 *   String  hex = AlgorithmRSA.hash(bytes);
 *   BigInteger sig = AlgorithmRSA.sign(bytes, kp.privateKey);
 *
 * Partner tự xác minh bằng:
 *   BigInteger m = sig.modPow(kp.publicKey.e, kp.publicKey.n);
 *   boolean ok   = m.equals(new BigInteger(1, sha256bytes));
 */
public class AlgorithmRSA {

    // ── 1. TẠO KHÓA NGẪU NHIÊN ──────────────────────────────────────────

    /**
     * Sinh P, Q ngẫu nhiên rồi tạo cặp khóa RSA.
     *
     * @param bitLength độ dài bit của mỗi số nguyên tố (tối thiểu 256,
     *                  khuyến nghị 512 cho demo, 2048 cho thực tế)
     * @return KeyPair chứa PublicKey(N,E) và PrivateKey(N,D)
     */
    public static KeyPair generateKey(int bitLength) {
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
     * Tạo cặp khóa RSA từ P, Q do người dùng nhập.
     *
     * @param p số nguyên tố thứ nhất
     * @param q số nguyên tố thứ hai
     * @return KeyPair chứa PublicKey(N,E) và PrivateKey(N,D)
     * @throws IllegalArgumentException nếu P hoặc Q không hợp lệ
     */
    public static KeyPair generateKey(BigInteger p, BigInteger q) {
        validatePQ(p, q);
        return buildKeyPair(p, q);
    }

    // ── 3. TÍNH SHA-256 ──────────────────────────────────────────────────

    /**
     * Tính SHA-256 của mảng byte, trả về chuỗi hex 64 ký tự.
     *
     * @param data dữ liệu cần băm (nội dung văn bản hoặc file)
     * @return chuỗi hex SHA-256
     */
    public static String hash(byte[] data) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(data);
            return bytesToHex(h);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("JVM không hỗ trợ SHA-256", e);
        }
    }

    /** Overload tiện lợi cho chuỗi UTF-8 */
    public static String hash(String text) {
        return hash(text.getBytes(StandardCharsets.UTF_8));
    }

    // ── 4. KÝ VĂN BẢN ───────────────────────────────────────────────────

    /**
     * Ký dữ liệu bằng khóa riêng tư.
     * Quy trình: data → SHA-256 → sig = hash^D mod N
     *
     * @param data dữ liệu cần ký (bytes của file hoặc văn bản)
     * @param key  khóa riêng tư PrivateKey(N, D)
     * @return chữ ký số BigInteger
     * @throws IllegalArgumentException nếu N quá nhỏ
     */
    public static BigInteger sign(byte[] data, PrivateKey key) {
        byte[] hashBytes = sha256Bytes(data);
        BigInteger hashInt = new BigInteger(1, hashBytes); // unsigned

        if (hashInt.compareTo(key.n) >= 0)
            throw new IllegalArgumentException(
                    "N quá nhỏ so với SHA-256 (256-bit). Dùng P, Q >= 256-bit mỗi số.");

        return hashInt.modPow(key.d, key.n);
    }

    /** Overload cho chuỗi UTF-8 */
    public static BigInteger sign(String text, PrivateKey key) {
        return sign(text.getBytes(StandardCharsets.UTF_8), key);
    }

    // ── INTERNAL: xây KeyPair từ P, Q ───────────────────────────────────

    private static KeyPair buildKeyPair(BigInteger p, BigInteger q) {
        BigInteger n   = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE)
                .multiply(q.subtract(BigInteger.ONE));
        BigInteger e   = chooseE(phi);
        BigInteger d   = e.modInverse(phi);

        return new KeyPair(
                new PublicKey(n, e),
                new PrivateKey(n, d)
        );
    }

    /**
     * Chọn E thỏa: 1 < E < phi và gcd(E, phi) = 1.
     * Ưu tiên 65537 (số Fermat F4 — chuẩn công nghiệp).
     */
    private static BigInteger chooseE(BigInteger phi) {
        BigInteger e = BigInteger.valueOf(65537);
        if (phi.gcd(e).equals(BigInteger.ONE)) return e;
        // fallback: duyệt số lẻ từ 3
        e = BigInteger.valueOf(3);
        while (!phi.gcd(e).equals(BigInteger.ONE))
            e = e.add(BigInteger.TWO);
        return e;
    }

    /** Kiểm tra đầu vào P, Q */
    private static void validatePQ(BigInteger p, BigInteger q) {
        if (p == null || q == null)
            throw new IllegalArgumentException("P và Q không được null.");
        if (!p.isProbablePrime(50))
            throw new IllegalArgumentException("P không phải số nguyên tố.");
        if (!q.isProbablePrime(50))
            throw new IllegalArgumentException("Q không phải số nguyên tố.");
        if (p.equals(q))
            throw new IllegalArgumentException("P và Q phải khác nhau.");
    }

    // ── INTERNAL: SHA-256 trả về byte[] ─────────────────────────────────

    private static byte[] sha256Bytes(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("JVM không hỗ trợ SHA-256", e);
        }
    }

    // ── INTERNAL: byte[] → hex string ───────────────────────────────────

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
