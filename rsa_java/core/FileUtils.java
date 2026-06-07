package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Bộ công cụ đọc/ghi file vật lý trên ổ cứng.
 */
public class FileUtils {

    /** Ghi nội dung text văn bản (lưu file khóa .pub, .priv) */
    public static void writeTextFile(File file, String content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    /** Đọc nội dung text văn bản (nạp khóa từ file) */
    public static String readTextFile(File file) throws IOException {
        byte[] bytes = readBinaryFile(file);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /** Đọc file nhị phân bất kỳ (PDF, Word, Excel, Ảnh...) */
    public static byte[] readBinaryFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            int bytesRead = 0;
            while (bytesRead < data.length) {
                int read = fis.read(data, bytesRead, data.length - bytesRead);
                if (read == -1) break;
                bytesRead += read;
            }
            return data;
        }
    }
}
