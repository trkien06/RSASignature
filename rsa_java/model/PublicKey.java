package model;

import java.math.BigInteger;

/**
 * Khóa công khai RSA: (N, E)
 * Chia sẻ tự do cho bất kỳ ai muốn xác minh chữ ký.
 */
public class PublicKey {

    public final BigInteger n; // modulus N = P × Q
    public final BigInteger e; // số mũ công khai (thường = 65537)

    public PublicKey(BigInteger n, BigInteger e) {
        this.n = n;
        this.e = e;
    }

    /** Chuỗi lưu ra file .pub (mỗi tham số 1 dòng) */
    public String toFileString() {
        return "N=" + n + "\nE=" + e;
    }

    /** Đọc từ nội dung file .pub */
    public static PublicKey fromFileString(String content) {
        BigInteger n = null, e = null;
        for (String line : content.split("\n")) {
            if (line.startsWith("N=")) n = new BigInteger(line.substring(2).trim());
            if (line.startsWith("E=")) e = new BigInteger(line.substring(2).trim());
        }
        if (n == null || e == null)
            throw new IllegalArgumentException("File .pub không hợp lệ.");
        return new PublicKey(n, e);
    }

    @Override
    public String toString() {
        return "PublicKey{N=" + n.toString().substring(0, Math.min(20, n.toString().length()))
                + "..., E=" + e + "}";
    }
}
