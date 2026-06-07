
package test;

import core.AlgorithmRSA;
import model.KeyPair;
import model.PrivateKey;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * Điểm khởi chạy ứng dụng + test nhanh backend.
 * Partner thay thế phần test bằng code khởi động UI.
 */
public class TestRSA {

    public static void main(String[] args) {

        // ── TEST 1: Sinh khóa ngẫu nhiên ─────────────────────────────────
        System.out.println("=== TEST 1: Sinh khóa ngẫu nhiên (512-bit) ===");
        KeyPair kp = AlgorithmRSA.generateKey(512);
        System.out.println("Public  key: " + kp.publicKey);
        System.out.println("Private key: " + kp.privateKey);

        // ── TEST 2: Sinh khóa từ P, Q nhập tay ───────────────────────────
        System.out.println("\n=== TEST 2: Nhập thủ công P=61, Q=53 ===");
        KeyPair kp2 = AlgorithmRSA.generateKey(
                new BigInteger("61"), new BigInteger("53"));
        System.out.println("N = " + kp2.publicKey.n);   // 3233
        System.out.println("E = " + kp2.publicKey.e);
        System.out.println("D = " + kp2.privateKey.d);  // 2753

        // ── TEST 3: Hash SHA-256 ──────────────────────────────────────────
        System.out.println("\n=== TEST 3: SHA-256 ===");
        String van_ban = "Xin chào, đây là văn bản cần ký!";
        String hashHex = AlgorithmRSA.hash(van_ban);
        System.out.println("SHA-256 = " + hashHex);

        // ── TEST 4: Ký văn bản ────────────────────────────────────────────
        System.out.println("\n=== TEST 4: Ký văn bản ===");
        PrivateKey priv = kp.privateKey;
        BigInteger sig = AlgorithmRSA.sign(van_ban, priv);
        System.out.println("Chữ ký = " + sig.toString(16).substring(0, 32) + "...");

        // ── TEST 5: Kiểm tra tính đúng đắn ───────────────────────────────
        System.out.println("\n=== TEST 5: Xác minh (partner làm phần UI) ===");
        byte[] rawHash = hexToBytes(hashHex);
        BigInteger hashInt  = new BigInteger(1, rawHash);
        BigInteger recovered = sig.modPow(kp.publicKey.e, kp.publicKey.n);
        System.out.println("Giải mã sig == hash gốc? " + (hashInt.equals(recovered) ? "✓ ĐÚNG" : "✗ SAI"));

        // ── TEST 6: Lưu / đọc khóa dạng string ──────────────────────────
        System.out.println("\n=== TEST 6: Format lưu file ===");
        System.out.println("--- .pub ---\n" + kp.publicKey.toFileString());
        System.out.println("--- .priv ---\n" + kp.privateKey.toFileString());
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        return data;
    }
}