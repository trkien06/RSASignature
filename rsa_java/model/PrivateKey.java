package model;

import java.math.BigInteger;

/**
 * Khóa riêng tư RSA: (N, D)
 * Giữ bí mật — chỉ người ký mới được có.
 */
public class PrivateKey {

    public final BigInteger n; // modulus N = P × Q
    public final BigInteger d; // số mũ riêng tư D = E⁻¹ mod φ

    public PrivateKey(BigInteger n, BigInteger d) {
        this.n = n;
        this.d = d;
    }

    /** Chuỗi lưu ra file .priv */
    public String toFileString() {
        return "N=" + n + "\nD=" + d;
    }

    /** Đọc từ nội dung file .priv */
    public static PrivateKey fromFileString(String content) {
        BigInteger n = null, d = null;
        for (String line : content.split("\n")) {
            if (line.startsWith("N=")) n = new BigInteger(line.substring(2).trim());
            if (line.startsWith("D=")) d = new BigInteger(line.substring(2).trim());
        }
        if (n == null || d == null)
            throw new IllegalArgumentException("File .priv không hợp lệ.");
        return new PrivateKey(n, d);
    }

    @Override
    public String toString() {
        return "PrivateKey{N=" + n.toString().substring(0, Math.min(20, n.toString().length()))
                + "..., D=" + d.toString().substring(0, Math.min(20, d.toString().length())) + "...}";
    }
}
