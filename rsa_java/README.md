# 🔐 Phần mềm Mô phỏng Chữ ký số RSA

Đây là dự án phần mềm mô phỏng quy trình hoạt động của hệ thống **Chữ ký số (Digital Signature)**, sử dụng thuật toán mã hóa bất đối xứng **RSA** kết hợp với thuật toán băm **SHA-256**. 

Dự án được xây dựng nhằm mục đích học tập, nghiên cứu và báo cáo đồ án môn học An toàn và Bảo mật thông tin.

## ✨ Tính năng chính

Phần mềm cung cấp giao diện trực quan với 2 luồng chức năng chính:

**1. Quản lý Khóa (Key Generation)**
- Sinh cặp khóa RSA ngẫu nhiên với độ dài bit an toàn (512-bit, 1024-bit).
- Hỗ trợ tính toán và tạo khóa thủ công từ hai số nguyên tố $p, q$ do người dùng nhập.
- Trích xuất và lưu trữ Khóa công khai (`.pub`) và Khóa riêng tư (`.priv`) ra tệp tin.

**2. Ký số & Xác minh (Sign & Verify)**
- **Phía Người Gửi:** Nạp tệp tin bất kỳ (TXT, PDF, DOCX, Hình ảnh...), băm dữ liệu bằng hàm SHA-256 và dùng Khóa riêng tư để tạo ra Chữ ký số. Hỗ trợ đóng gói xuất 3 file cùng lúc để gửi cho đối tác.
- **Phía Người Nhận:** Nạp tệp tin nhận được, giải mã chữ ký bằng Khóa công khai và đối chiếu hàm băm để xác minh Tính toàn vẹn (file có bị sửa đổi không) và Tính xác thực (người gửi là ai).

## 🛠 Công nghệ sử dụng

- **Ngôn ngữ lập trình:** Java (JDK 8 trở lên)
- **Giao diện người dùng:** Java Swing (Sử dụng `BasicButtonUI` tối ưu hóa hiển thị phẳng).
- **Xử lý số lớn:** Sử dụng thư viện lõi `java.math.BigInteger` của Java để xử lý các phép toán mã hóa RSA mà không phụ thuộc vào thư viện bên ngoài.
- **Môi trường phát triển (IDE):** Apache NetBeans.

## 📂 Cấu trúc Dự án

Dự án được thiết kế theo mô hình phân lớp rõ ràng:

QuanLyKySoRSA/
├── src/
│   ├── core/         # Chứa thuật toán mã hóa RSA, hàm băm SHA-256 và luồng I/O tệp tin.
│   ├── image/        # Chứa tài nguyên hình ảnh (Logo ứng dụng).
│   ├── model/        # Chứa các cấu trúc đối tượng dữ liệu (PublicKey, PrivateKey, KeyPair).
│   ├── test/         # Kịch bản kiểm thử (Test Cases) chạy trên Console.
│   └── ui/           # Giao diện đồ họa người dùng (MainForm).
├── README.md         # Tài liệu dự án.
└── ...
