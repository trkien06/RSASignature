# 🔐 Phần mềm Mô phỏng Chữ ký số RSA (Phiên bản C++)

Đây là dự án phần mềm mô phỏng quy trình hoạt động của hệ thống **Chữ ký số (Digital Signature)**, sử dụng thuật toán mã hóa bất đối xứng **RSA** kết hợp với thuật toán băm **SHA-256**. 

Dự án được xây dựng nhằm mục đích học tập, nghiên cứu và báo cáo đồ án môn học An toàn và Bảo mật thông tin.

## ✨ Tính năng chính

Phần mềm cung cấp giao diện trực quan với 2 luồng chức năng chính:

**1. Quản lý Khóa (Key Generation)**
* Sinh cặp khóa RSA ngẫu nhiên với các số nguyên siêu lớn (BigInt).
* Hỗ trợ tính toán và tạo khóa thủ công từ hai số nguyên tố $p, q$ do người dùng nhập.
* Hiển thị trực quan Modulus $n$, hàm Euler $\Phi(n)$, số mũ công khai $e$, số mũ bí mật $d$.

**2. Ký số & Xác minh (Sign & Verify)**
* **Phía Người Gửi:** Nạp tệp tin bất kỳ (TXT, PDF, DOCX, Hình ảnh...), băm dữ liệu bằng hàm SHA-256 và dùng Khóa riêng tư để tạo ra Chữ ký số. Dữ liệu băm và chữ ký được đồng bộ hiển thị dưới dạng hệ cơ số Thập lục phân (HEX) để đảm bảo độ chính xác và đồng bộ 100% mã chữ ký với các nền tảng khác (như Java). Hỗ trợ đóng gói xuất 3 file cùng lúc (`.signed`, `_signature.txt`, `_public_key.txt`) để gửi cho đối tác.
* **Phía Người Nhận:** Nạp tệp tin nhận được, giải mã chữ ký HEX bằng Khóa công khai và đối chiếu hàm băm để xác minh Tính toàn vẹn (file có bị sửa đổi không) và Tính xác thực (người gửi là ai).

## 🛠 Công nghệ sử dụng

* **Ngôn ngữ lập trình:** C++ (Chuẩn C++11 trở lên).
* **Giao diện người dùng:** Windows API (Win32 API) thiết kế giao diện native, tích hợp tệp tài nguyên (Resource file) để nhúng cứng Logo `.ico` vào ứng dụng.
* **Xử lý số lớn:** Sử dụng thư viện `boost::multiprecision::cpp_int` (phiên bản `boost_1_66_0`) để xử lý các phép toán mã hóa RSA phức tạp vượt giới hạn bộ nhớ 64-bit mặc định của C++.
* **Thuật toán băm:** Tự xây dựng lõi SHA-256 hoàn toàn bằng C++, độc lập với CryptoAPI của hệ điều hành. Hỗ trợ I/O đọc file an toàn tuyệt đối với các đường dẫn thư mục chứa Tiếng Việt có dấu.
* **Môi trường phát triển (IDE):** Code::Blocks (Sử dụng trình biên dịch MinGW).
* ## 🚀 Hướng dẫn Cài đặt & Cấu hình Code::Blocks

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

## 📂 Cấu trúc Dự án

Dự án được thiết kế theo mô hình phân lớp rõ ràng để dễ dàng quản lý:

```text
RSASignature/
├── core/             # Chứa thuật toán mã hóa RSA (tích hợp Boost) và lõi băm SHA-256 tự code.
│   ├── AlgorithmRSA.cpp
│   ├── AlgorithmRSA.h
│   └── InfInt.h      # Wrapper ánh xạ định nghĩa số lớn cpp_int của Boost
├── model/            # Chứa các cấu trúc đối tượng dữ liệu
│   ├── KeyPair.h
│   ├── PrivateKey.h
│   └── PublicKey.h
├── MainForm.cpp      # Tệp nguồn chính khởi tạo giao diện đồ họa (GUI) và xử lý luồng sự kiện.
├── logo.ico          # Tài nguyên hình ảnh (Logo ứng dụng).
├── resource.rc       # Tệp cấu hình tài nguyên để nhúng logo vào tệp thực thi .exe.
└── RSASignature.cbp  # Tệp quản lý cấu hình dự án (Project file) của Code::Blocks.
