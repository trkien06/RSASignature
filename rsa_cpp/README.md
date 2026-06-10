# 🔐 Phần mềm Quản lý Chữ ký số RSA (C++ Win32 API)

Đây là một ứng dụng Desktop hoàn chỉnh được viết bằng **C++ (Windows API)** giúp mô phỏng quá trình tạo khóa, ký số và xác minh tài liệu bằng thuật toán **RSA số nguyên lớn** (BigInteger) kết hợp băm dữ liệu **SHA-256**.

Project này được thiết kế với giao diện đồ họa thân thiện, đồng bộ hóa hệ cơ số HEX và xử lý an toàn với mọi loại file (kể cả thư mục chứa tiếng Việt).

## ✨ Các tính năng chính

* **Tạo cặp khóa RSA (BigInt):** Tự động sinh hoặc tính toán khóa Công khai (Public Key) và Bí mật (Private Key) từ 2 số nguyên tố P, Q.
* **Thuật toán băm SHA-256 tự xây dựng:** Xử lý băm file độc lập 100% bằng C++, không phụ thuộc vào CryptoAPI của Windows, hỗ trợ mọi định dạng tệp (.txt, .docx, .pdf, .png...).
* **Ký số tài liệu:** Mã hóa mã băm bằng Private Key để tạo chữ ký số hệ cơ số Thập lục phân (HEX).
* **Xác minh chữ ký:** Giải mã chữ ký bằng Public Key và đối chiếu để kiểm tra tính toàn vẹn của dữ liệu.
* **Giao diện Tiếng Việt (Unicode):** Đọc/ghi mượt mà các file/thư mục có dấu.
* **Quản lý tệp chuyên nghiệp:** Xuất đồng loạt các tệp `.signed`, `_signature.txt`, và `_public_key.txt` để gửi cho đối tác.

---

## 🛠 Yêu cầu hệ thống (Prerequisites)

Để biên dịch và chạy dự án này, máy tính của bạn cần có:

1. **Hệ điều hành:** Windows (10, 11).
2. **Trình biên dịch / IDE:** Code::Blocks (MinGW).
3. **Thư viện Boost (Bắt buộc):** Project sử dụng `boost::multiprecision::cpp_int` để xử lý các phép toán số lớn vượt quá giới hạn 64-bit cơ bản của C++. (Sử dụng bản `boost_1_66_0`).

---

## 🚀 Hướng dẫn Cài đặt & Cấu hình Code::Blocks

### Bước 1: Tải và cài đặt thư viện Boost
1. Tải về tệp thư viện `boost_1_66_0.7z`.
2. Dùng 7-Zip để giải nén file vừa tải vào một thư mục cố định trên máy (Ví dụ: `C:\local\boost_1_66_0`).

### Bước 2: Cấu hình Compiler & Linker trong Code::Blocks
Mở Code::Blocks, trỏ chuột phải vào tên Project chọn **Build options...** (hoặc vào menu **Settings** -> **Compiler...**) và thiết lập như sau:

**1. Khai báo đường dẫn thư viện (Search directories):**
* Chuyển sang tab **Search directories** -> tab con **Compiler**.
* Bấm **Add** và trỏ đường dẫn tới thư mục Boost bạn vừa giải nén (Ví dụ: `C:\local\boost_1_66_0`). Bấm OK.

**2. Cấu hình Linker settings (Thư viện Win32 & Đóng gói EXE):**
Chuyển sang tab **Linker settings** và điền chính xác các thông số sau:

* **Tại cột "Link libraries" (Khung bên trái):** Bấm Add lần lượt các thư viện cốt lõi của Windows:
  * `gdi32`
  * `user32`
  * `kernel32`
  * `comctl32`

* **Tại ô "Other linker options" (Khung bên phải):** Copy và dán toàn bộ dòng cờ lệnh sau:
  ```text
  -lole32 -lshell32 -lcomdlg32 -mwindows -static-libgcc -static-libstdc++
