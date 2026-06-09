package test;

import core.AlgorithmRSA;
import model.KeyPair;
import model.PrivateKey;

import java.math.BigInteger;

/**
 * Điểm khởi chạy ứng dụng + test nhanh backend.
 * Partner thay thế phần test bằng code khởi động UI.
 */
public class TestRSA {

    public static void main(String[] args) {

        // ── TEST 1: Sinh khóa ngẫu nhiên ─────────────────────────────────
        System.out.println("=== TEST 1: Sinh khóa ngẫu nhiên (512-bit) ===");
        KeyPair kp = AlgorithmRSA.generateKey(512);
        // SỬA: Dùng getter thay vì gọi trực tiếp thuộc tính private
        System.out.println("Public  key: " + kp.getPublicKey());
        System.out.println("Private key: " + kp.getPrivateKey());

        // ── TEST 2: Sinh khóa từ P, Q nhập tay ───────────────────────────
        System.out.println("\n=== TEST 2: Nhập thủ công P=61, Q=53 ===");
        KeyPair kp2 = AlgorithmRSA.generateKey(
                new BigInteger("61"), new BigInteger("53"));
        // SỬA: Dùng getter để lấy N, E, D
        System.out.println("N = " + kp2.getPublicKey().getN());   // 3233
        System.out.println("E = " + kp2.getPublicKey().getE());   // 17 (hoặc tùy cấu hình hàm chooseE)
        System.out.println("D = " + kp2.getPrivateKey().getD());  // 2753

        // ── TEST 3: Hash SHA-256 ──────────────────────────────────────────
        System.out.println("\n=== TEST 3: SHA-256 ===");
        String van_ban = "Xin chào, đây là văn bản cần ký!";
        String hashHex = AlgorithmRSA.hash(van_ban);
        System.out.println("SHA-256 = " + hashHex);

        // ── TEST 4: Ký văn bản ────────────────────────────────────────────
        System.out.println("\n=== TEST 4: Ký văn bản ===");
        PrivateKey priv = kp.getPrivateKey();
        BigInteger sig = AlgorithmRSA.sign(van_ban, priv);
        System.out.println("Chữ ký = " + sig.toString(16).substring(0, 32) + "...");

        // ── TEST 5: Kiểm tra tính đúng đắn ───────────────────────────────
        System.out.println("\n=== TEST 5: Xác minh tính toàn vẹn ===");
        byte[] rawHash = hexToBytes(hashHex);
        BigInteger hashInt  = new BigInteger(1, rawHash);
        
        // SỬA: Dùng getter
        BigInteger recovered = sig.modPow(kp.getPublicKey().getE(), kp.getPublicKey().getN());
        System.out.println("Giải mã thủ công (sig^E mod N) == hash gốc? " + (hashInt.equals(recovered) ? "✓ ĐÚNG" : "✗ SAI"));
        
        // BỔ SUNG: Test luôn hàm verify của Backend
        boolean isVerifyOk = AlgorithmRSA.verify(van_ban, sig, kp.getPublicKey());
        System.out.println("Gọi hàm AlgorithmRSA.verify()             ? " + (isVerifyOk ? "✓ ĐÚNG" : "✗ SAI"));

        // ── TEST 6: Lưu / đọc khóa dạng string ──────────────────────────
        System.out.println("\n=== TEST 6: Format lưu file ===");
        System.out.println("--- .pub ---\n" + kp.getPublicKey().toFileString());
        System.out.println("--- .priv ---\n" + kp.getPrivateKey().toFileString());
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        return data;
    }
}
