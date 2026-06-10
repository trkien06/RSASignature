#include <windows.h>
#include <commctrl.h>
#include <commdlg.h>
#include <shlobj.h>
#include <string>
#include <vector>
#include <fstream>
#include <sstream>
#include <ctime>
#include <iomanip>

// Gọi các package đã chia
#include "../core/InfInt.h"
#include "../core/AlgorithmRSA.h"
#include "../model/KeyPair.h"

#define ID_TABCTRL 100
#define ID_BTN_NGAUNHIEN 101
#define ID_BTN_TINHTOAN 102
#define ID_BTN_TAIFILE_GOC 103
#define ID_BTN_KYSO 104
#define ID_BTN_XUATFILE 106
#define ID_BTN_TAIFILE_NHAN 107
#define ID_BTN_TAICHUKY 108
#define ID_BTN_TAIPUBKEY 109
#define ID_BTN_BAM_DL 110
#define ID_BTN_GIAIMA 111
#define ID_BTN_XACMINH 112
#define ID_BTN_LAMMOI 113

LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
void CreateAllTabsControls(HWND hwndParent);
void ShowTabWindow(int tabIndex);

HFONT hFontNormal, hFontBold, hFontTitle;
HBRUSH hBackBrush, hWhiteBrush;
COLORREF cBlueDark = RGB(0, 102, 204), cTextDark = RGB(20, 50, 90), cBgLight = RGB(240, 244, 248);

std::vector<HWND> tabGroups[2];

HWND editP, editQ, editN, editPhi, editE, editD, editPublic, editPrivate;
HWND editKy_VanBan, editKy_Hash, editKy_ChuKy;
HWND editXM_VanBan, editXM_Hash, editXM_ChuKy, editXM_GiaiMa, lblPubKeyStatus;

model::KeyPair currentKeyPair;
model::PublicKey loadedPublicKey;
std::wstring fileGocPath = L"";
std::wstring fileNhanPath = L"";

std::string WStringToString(const std::wstring& wstr) {
    if (wstr.empty()) return "";
    int size_needed = WideCharToMultiByte(CP_UTF8, 0, &wstr[0], (int)wstr.size(), NULL, 0, NULL, NULL);
    std::string strTo(size_needed, 0);
    WideCharToMultiByte(CP_UTF8, 0, &wstr[0], (int)wstr.size(), &strTo[0], size_needed, NULL, NULL);
    return strTo;
}

std::string CleanNumberString(std::string str) {
    str.erase(str.find_last_not_of(" \n\r\t") + 1);
    str.erase(0, str.find_first_not_of(" \n\r\t"));
    return str;
}

// Chuyển đổi số nguyên lớn thành chuỗi HEX hiển thị
std::wstring InfIntToHexWString(InfInt n) {
    std::stringstream ss;
    ss << std::hex << n;
    std::string hexStr = ss.str();
    return std::wstring(hexStr.begin(), hexStr.end());
}

// Chuyển chuỗi HEX nhập từ màn hình thành số nguyên lớn InfInt
InfInt HexStringToInfInt(const std::string& hexStr) {
    std::string cleanHex = hexStr;
    if (cleanHex.substr(0, 2) != "0x" && cleanHex.substr(0, 2) != "0X") {
        cleanHex = "0x" + cleanHex;
    }
    return InfInt(cleanHex);
}

std::string InfIntToString(InfInt n) {
    std::stringstream ss; ss << n; return ss.str();
}

std::wstring InfIntToWString(InfInt n) {
    std::string str = InfIntToString(n);
    return std::wstring(str.begin(), str.end());
}

InfInt generateLargePrime(int length) {
    std::string s = "";
    s += std::to_string(rand() % 9 + 1);
    for (int i = 1; i < length - 1; i++) s += std::to_string(rand() % 10);
    s += "1";
    InfInt num(s);
    while (!core::AlgorithmRSA::isPrime(num)) num += 2;
    return num;
}

std::wstring OpenFileDialogBox(HWND hwnd) {
    OPENFILENAMEW ofn; wchar_t szFile[260] = { 0 }; ZeroMemory(&ofn, sizeof(ofn));
    ofn.lStructSize = sizeof(ofn); ofn.hwndOwner = hwnd; ofn.lpstrFile = szFile; ofn.nMaxFile = sizeof(szFile);
    ofn.lpstrFilter = L"Tất cả file\0*.*\0"; ofn.nFilterIndex = 1; ofn.Flags = OFN_PATHMUSTEXIST | OFN_FILEMUSTEXIST;
    if (GetOpenFileNameW(&ofn) == TRUE) return szFile;
    return L"";
}

std::wstring GetBaseFilename(const std::wstring& fullPath) {
    size_t lastSlash = fullPath.find_last_of(L"/\\");
    std::wstring fileName = (lastSlash != std::wstring::npos) ? fullPath.substr(lastSlash + 1) : fullPath;
    size_t lastDot = fileName.find_last_of(L".");
    if (lastDot != std::wstring::npos) return fileName.substr(0, lastDot);
    return fileName;
}

std::wstring SelectFolder(HWND hwnd) {
    wchar_t path[MAX_PATH];
    BROWSEINFOW bi = { 0 };
    bi.hwndOwner = hwnd;
    bi.lpszTitle = L"Chọn thư mục để xuất các file Chữ ký số:";
    bi.ulFlags = BIF_RETURNONLYFSDIRS | BIF_USENEWUI;
    LPITEMIDLIST pidl = SHBrowseForFolderW(&bi);
    if (pidl != 0) {
        SHGetPathFromIDListW(pidl, path);
        CoTaskMemFree(pidl);
        return std::wstring(path);
    }
    return L"";
}

void SaveTextToFile(const std::wstring& path, const std::string& content) {
    HANDLE hFile = CreateFileW(path.c_str(), GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
    if (hFile != INVALID_HANDLE_VALUE) {
        DWORD bytesWritten;
        WriteFile(hFile, content.c_str(), content.length(), &bytesWritten, NULL);
        CloseHandle(hFile);
    }
}

// Đọc file an toàn qua API Windows độc lập với thư viện C++
std::string ReadTextFile(const std::wstring& path) {
    HANDLE hFile = CreateFileW(path.c_str(), GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
    if (hFile == INVALID_HANDLE_VALUE) return "";

    DWORD fileSize = GetFileSize(hFile, NULL);
    if (fileSize == INVALID_FILE_SIZE || fileSize == 0) {
        CloseHandle(hFile);
        return "";
    }

    std::string content(fileSize, '\0');
    DWORD bytesRead;
    ReadFile(hFile, &content[0], fileSize, &bytesRead, NULL);
    CloseHandle(hFile);

    content.resize(bytesRead);
    return content;
}

void HandleCalculateKey(HWND hwnd) {
    wchar_t bufP[4096], bufQ[4096];
    GetWindowTextW(editP, bufP, 4096); GetWindowTextW(editQ, bufQ, 4096);
    std::string strP = WStringToString(bufP); std::string strQ = WStringToString(bufQ);

    if (strP.empty() || strQ.empty()) { MessageBoxW(hwnd, L"Vui lòng nhập P và Q!", L"Lỗi", MB_ICONWARNING); return; }

    InfInt p(strP), q(strQ);
    if (!core::AlgorithmRSA::isPrime(p) || !core::AlgorithmRSA::isPrime(q) || p == q) {
        MessageBoxW(hwnd, L"P và Q phải là các số nguyên tố khác nhau!", L"Lỗi", MB_ICONWARNING); return;
    }

    InfInt n = p * q;
    InfInt phi = (p - 1) * (q - 1);
    InfInt e = 65537;
    if (e >= phi || core::AlgorithmRSA::gcd(e, phi) != 1) {
        e = 3; while (e < phi) { if (core::AlgorithmRSA::gcd(e, phi) == 1) break; e += 2; }
    }
    InfInt d = core::AlgorithmRSA::modInverse(e, phi);

    currentKeyPair = model::KeyPair(model::PublicKey(n, e), model::PrivateKey(n, d));
    loadedPublicKey = currentKeyPair.publicKey;

    SetWindowTextW(editN, InfIntToWString(n).c_str());
    SetWindowTextW(editPhi, InfIntToWString(phi).c_str());
    SetWindowTextW(editE, InfIntToWString(e).c_str());
    SetWindowTextW(editD, InfIntToWString(d).c_str());
    SetWindowTextW(editPublic, (L"(" + InfIntToWString(n) + L", " + InfIntToWString(e) + L")").c_str());
    SetWindowTextW(editPrivate, (L"(" + InfIntToWString(n) + L", " + InfIntToWString(d) + L")").c_str());
    SetWindowTextW(lblPubKeyStatus, L"✅ Đã tải Public Key");
    MessageBoxW(hwnd, L"Tính toán & tạo khóa thành công!", L"Thành công", MB_ICONINFORMATION);
}

void HandleSign(HWND hwnd) {
    if (fileGocPath.empty() || currentKeyPair.privateKey.d == 0) {
        MessageBoxW(hwnd, L"Vui lòng tải file gốc và đảm bảo đã Tạo khóa!", L"Lỗi", MB_ICONWARNING); return;
    }
    std::wstring hashHex = core::AlgorithmRSA::CalculateSHA256(fileGocPath);
    SetWindowTextW(editKy_Hash, hashHex.c_str());

    if (hashHex.find(L"LỖI") != std::wstring::npos) {
        MessageBoxW(hwnd, (L"Không thể băm file!\nChi tiết: " + hashHex).c_str(), L"Lỗi Đọc File", MB_ICONERROR); return;
    }

    InfInt sig = core::AlgorithmRSA::powerMod(core::AlgorithmRSA::HashToInt(hashHex), currentKeyPair.privateKey.d, currentKeyPair.privateKey.n);
    SetWindowTextW(editKy_ChuKy, InfIntToHexWString(sig).c_str());
}

void HandleExportFiles(HWND hwnd) {
    wchar_t bufSig[4096]; GetWindowTextW(editKy_ChuKy, bufSig, 4096);
    if (wcslen(bufSig) == 0 || fileGocPath.empty()) {
        MessageBoxW(hwnd, L"Vui lòng tải file gốc và thực hiện Ký số trước khi xuất file!", L"Lỗi", MB_ICONWARNING); return;
    }

    std::wstring folderPath = SelectFolder(hwnd);
    if (folderPath.empty()) return;

    std::wstring baseName = GetBaseFilename(fileGocPath);

    std::wstring pathSigned = folderPath + L"\\" + baseName + L".signed";
    std::wstring pathSigTxt = folderPath + L"\\" + baseName + L"_signature.txt";
    std::wstring pathPubKey = folderPath + L"\\" + baseName + L"_public_key.txt";

    CopyFileW(fileGocPath.c_str(), pathSigned.c_str(), FALSE);
    SaveTextToFile(pathSigTxt, WStringToString(bufSig));

    std::string pubContent = "N=" + InfIntToString(currentKeyPair.publicKey.n) + "\nE=" + InfIntToString(currentKeyPair.publicKey.e);
    SaveTextToFile(pathPubKey, pubContent);

    std::wstring msg = L"Đã xuất file thành công!\r\n\r\n"
                       L"📄 File tài liệu đã ký: " + baseName + L".signed\r\n"
                       L"🔐 File chữ ký số (HEX): " + baseName + L"_signature.txt\r\n"
                       L"🔑 File public key: " + baseName + L"_public_key.txt\r\n\r\n"
                       L"Gửi các file này cho đối tác/khách hàng để xác minh.";

    MessageBoxW(hwnd, msg.c_str(), L"Thành công", MB_ICONINFORMATION);
}

void HandleLoadPublicKey(HWND hwnd) {
    std::wstring path = OpenFileDialogBox(hwnd);
    if (!path.empty()) {
        std::string content = ReadTextFile(path);
        if (content.empty()) {
            MessageBoxW(hwnd, L"Không thể mở file Public Key!", L"Lỗi", MB_ICONERROR);
            return;
        }

        std::istringstream file(content);
        std::string line, sN = "0", sE = "0";
        while (std::getline(file, line)) {
            if (line.find("N=") == 0) sN = CleanNumberString(line.substr(2));
            if (line.find("E=") == 0) sE = CleanNumberString(line.substr(2));
        }

        try {
            loadedPublicKey = model::PublicKey(InfInt(sN), InfInt(sE));
            SetWindowTextW(lblPubKeyStatus, L"✅ Đã tải Public Key từ file");
        } catch (...) {
            MessageBoxW(hwnd, L"Định dạng số trong file Public Key không hợp lệ!", L"Lỗi Dữ Liệu", MB_ICONERROR);
        }
    }
}

void HandleVerify(HWND hwnd) {
    wchar_t bufHash[4096], bufGiaiMa[4096];
    GetWindowTextW(editXM_Hash, bufHash, 4096);
    GetWindowTextW(editXM_GiaiMa, bufGiaiMa, 4096);
    std::wstring wHash(bufHash);

    if (wHash.empty() || wcslen(bufGiaiMa) == 0) {
        MessageBoxW(hwnd, L"Vui lòng băm dữ liệu và Giải mã trước!", L"Cảnh báo", MB_ICONWARNING); return;
    }
    if (wHash.find(L"LỖI") != std::wstring::npos) {
        MessageBoxW(hwnd, L"Mã băm hiện tại đang là thông báo lỗi, không thể xác minh!", L"Lỗi", MB_ICONERROR); return;
    }

    InfInt hashInt = core::AlgorithmRSA::HashToInt(wHash);
    std::string giaiMaStr = CleanNumberString(WStringToString(bufGiaiMa));

    try {
        InfInt decryptedInt = HexStringToInfInt(giaiMaStr);

        if (hashInt == decryptedInt) {
            MessageBoxW(hwnd, L"✓ CHỮ KÝ HỢP LỆ!\nDữ liệu vẹn toàn 100%.", L"Thành công", MB_ICONINFORMATION);
        } else {
            MessageBoxW(hwnd, L"✗ CHỮ KÝ KHÔNG HỢP LỆ!\nTài liệu có thể đã bị sửa đổi hoặc sai chữ ký.", L"Thất bại", MB_ICONERROR);
        }
    } catch (...) {
        MessageBoxW(hwnd, L"Dữ liệu giải mã không hợp lệ!", L"Lỗi", MB_ICONERROR);
    }
}

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    srand(time(NULL));
    CoInitialize(NULL);
    InitCommonControls();

    WNDCLASSW wc = {0};
    wc.lpszClassName = L"ColorRSAGUI";
    wc.hInstance = hInstance;
    wc.hbrBackground = CreateSolidBrush(cBgLight);
    wc.lpfnWndProc = WndProc;
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);

    // --- KHAI BÁO LOAD LOGO TỪ FILE RESOURCE MÃ 101 ---
    wc.hIcon = LoadIcon(hInstance, MAKEINTRESOURCE(101));

    if (!RegisterClassW(&wc)) return 0;

    CreateWindowW(L"ColorRSAGUI", L"Mô phỏng chữ ký số RSA - Nhóm 2", WS_OVERLAPPEDWINDOW | WS_VISIBLE, 120, 60, 1100, 750, NULL, NULL, hInstance, NULL);
    MSG msg; while (GetMessage(&msg, NULL, 0, 0)) { TranslateMessage(&msg); DispatchMessage(&msg); }

    CoUninitialize();
    return msg.wParam;
}

LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wp, LPARAM lp) {
    static HWND hwndTab; static HBRUSH hYellowBrush = CreateSolidBrush(RGB(255, 253, 235));
    switch (msg) {
        case WM_CREATE: {
            hFontNormal = CreateFontW(16, 0, 0, 0, FW_NORMAL, 0, 0, 0, DEFAULT_CHARSET, 0, 0, 0, 0, L"Segoe UI");
            hFontBold   = CreateFontW(16, 0, 0, 0, FW_BOLD,   0, 0, 0, DEFAULT_CHARSET, 0, 0, 0, 0, L"Segoe UI");
            hFontTitle  = CreateFontW(20, 0, 0, 0, FW_BOLD,   0, 0, 0, DEFAULT_CHARSET, 0, 0, 0, 0, L"Segoe UI");
            hWhiteBrush = CreateSolidBrush(RGB(255, 255, 255)); hBackBrush = CreateSolidBrush(cBgLight);
            hwndTab = CreateWindowW(WC_TABCONTROLW, L"", WS_CHILD | WS_VISIBLE | WS_CLIPSIBLINGS, 15, 15, 1050, 680, hwnd, (HMENU)ID_TABCTRL, GetModuleHandle(NULL), NULL);
            SendMessageW(hwndTab, WM_SETFONT, (WPARAM)hFontBold, TRUE);
            TCITEMW tie; tie.mask = TCIF_TEXT;
            tie.pszText = (wchar_t*)L"   TẠO CẶP KHÓA   "; SendMessageW(hwndTab, TCM_INSERTITEMW, 0, (LPARAM)&tie);
            tie.pszText = (wchar_t*)L"   KÝ SỐ XÁC MINH   "; SendMessageW(hwndTab, TCM_INSERTITEMW, 1, (LPARAM)&tie);
            CreateAllTabsControls(hwnd); ShowTabWindow(0); break;
        }
        case WM_NOTIFY: {
            NMHDR* nmhdr = (NMHDR*)lp; if (nmhdr->idFrom == ID_TABCTRL && nmhdr->code == TCN_SELCHANGE) ShowTabWindow(TabCtrl_GetCurSel(hwndTab));
            break;
        }
        case WM_CTLCOLORSTATIC: {
            HDC hdc = (HDC)wp; HWND hCtrl = (HWND)lp; SetBkMode(hdc, TRANSPARENT);
            wchar_t text[256]; GetWindowTextW(hCtrl, text, 256); std::wstring wText(text);
            if (wText == L"Số nguyên tố bí mật" || wText == L"Kết quả tính toán" || wText == L"Dữ liệu gốc" || wText == L"Xác minh chữ ký số") SetTextColor(hdc, RGB(0, 102, 204));
            else if (wText.find(L"✅") != std::wstring::npos) SetTextColor(hdc, RGB(0, 153, 0));
            else if (wText.find(L"⚠️") != std::wstring::npos) SetTextColor(hdc, RGB(204, 0, 0));
            else SetTextColor(hdc, RGB(30, 30, 30));
            return (LRESULT)hBackBrush;
        }
        case WM_CTLCOLOREDIT: {
            HDC hdc = (HDC)wp; HWND hCtrl = (HWND)lp;
            if (hCtrl == editKy_ChuKy || hCtrl == editXM_ChuKy || hCtrl == editXM_GiaiMa || hCtrl == editKy_Hash || hCtrl == editXM_Hash || hCtrl == editPublic || hCtrl == editPrivate) {
                SetTextColor(hdc, RGB(180, 0, 0)); SetBkColor(hdc, RGB(255, 253, 235)); return (LRESULT)hYellowBrush;
            }
            SetTextColor(hdc, RGB(0, 0, 0)); SetBkColor(hdc, RGB(255, 255, 255)); return (LRESULT)hWhiteBrush;
        }
        case WM_COMMAND: {
            switch (LOWORD(wp)) {
                case ID_BTN_NGAUNHIEN: {
                    InfInt p_rand = generateLargePrime(200); InfInt q_rand = generateLargePrime(200);
                    SetWindowTextW(editP, InfIntToWString(p_rand).c_str()); SetWindowTextW(editQ, InfIntToWString(q_rand).c_str());
                    break;
                }
                case ID_BTN_TINHTOAN: HandleCalculateKey(hwnd); break;
                case ID_BTN_TAIFILE_GOC: {
                    fileGocPath = OpenFileDialogBox(hwnd); if (!fileGocPath.empty()) SetWindowTextW(editKy_VanBan, (L"Đã tải file:\r\n" + fileGocPath).c_str());
                    break;
                }
                case ID_BTN_KYSO: HandleSign(hwnd); break;
                case ID_BTN_XUATFILE: HandleExportFiles(hwnd); break;
                case ID_BTN_TAIFILE_NHAN: {
                    fileNhanPath = OpenFileDialogBox(hwnd); if (!fileNhanPath.empty()) SetWindowTextW(editXM_VanBan, (L"Đã tải file:\r\n" + fileNhanPath).c_str());
                    break;
                }
                case ID_BTN_TAICHUKY: {
                    std::wstring path = OpenFileDialogBox(hwnd);
                    if (!path.empty()) {
                        std::string content = ReadTextFile(path);
                        if (!content.empty()) {
                            std::istringstream file(content);
                            std::string sig;
                            std::getline(file, sig);
                            sig = CleanNumberString(sig);
                            SetWindowTextW(editXM_ChuKy, std::wstring(sig.begin(), sig.end()).c_str());
                        } else {
                            MessageBoxW(hwnd, L"Không thể mở file chữ ký số!", L"Lỗi", MB_ICONERROR);
                        }
                    }
                    break;
                }
                case ID_BTN_TAIPUBKEY: HandleLoadPublicKey(hwnd); break;
                case ID_BTN_BAM_DL: {
                    if (!fileNhanPath.empty()) {
                        std::wstring hashHex = core::AlgorithmRSA::CalculateSHA256(fileNhanPath);
                        SetWindowTextW(editXM_Hash, hashHex.c_str());
                    }
                    break;
                }
                case ID_BTN_GIAIMA: {
                    wchar_t bufSig[4096];
                    GetWindowTextW(editXM_ChuKy, bufSig, 4096);
                    std::string sigStr = CleanNumberString(WStringToString(bufSig));

                    if (sigStr.empty() || loadedPublicKey.e == 0 || loadedPublicKey.n == 0) {
                        MessageBoxW(hwnd, L"Thiếu dữ liệu xác minh hoặc cấu trúc khóa công khai chưa đúng!", L"Cảnh báo", MB_ICONWARNING);
                        break;
                    }

                    try {
                        InfInt sig = HexStringToInfInt(sigStr);
                        InfInt dec = core::AlgorithmRSA::powerMod(sig, loadedPublicKey.e, loadedPublicKey.n);
                        SetWindowTextW(editXM_GiaiMa, InfIntToHexWString(dec).c_str());
                    }
                    catch (...) {
                        MessageBoxW(hwnd, L"Dữ liệu chữ ký bị lỗi cấu trúc hoặc chứa ký tự lạ không hợp lệ!", L"Lỗi Giải Mã RSA", MB_ICONERROR);
                    }
                    break;
                }
                case ID_BTN_XACMINH: HandleVerify(hwnd); break;
                case ID_BTN_LAMMOI: {
                    SetWindowTextW(editKy_VanBan, L""); SetWindowTextW(editKy_Hash, L""); SetWindowTextW(editKy_ChuKy, L"");
                    SetWindowTextW(editXM_VanBan, L""); SetWindowTextW(editXM_Hash, L""); SetWindowTextW(editXM_ChuKy, L""); SetWindowTextW(editXM_GiaiMa, L"");
                    fileGocPath = L""; fileNhanPath = L""; SetWindowTextW(lblPubKeyStatus, L"⚠️ Chưa tải Public Key"); break;
                }
            }
            break;
        }
        case WM_DESTROY: DeleteObject(hYellowBrush); PostQuitMessage(0); break;
        default: return DefWindowProcW(hwnd, msg, wp, lp);
    }
    return 0;
}

void ShowTabWindow(int tabIndex) {
    for (int i = 0; i < 2; i++) {
        for (HWND ctrl : tabGroups[i]) ShowWindow(ctrl, (i == tabIndex) ? SW_SHOW : SW_HIDE);
    }
}

void CreateAllTabsControls(HWND hwndParent) {
    HWND t1 = CreateWindowW(L"STATIC", L"Số nguyên tố bí mật", WS_CHILD, 40, 65, 300, 25, hwndParent, NULL, NULL, NULL); SendMessageW(t1, WM_SETFONT, (WPARAM)hFontTitle, TRUE); tabGroups[0].push_back(t1);
    HWND l1 = CreateWindowW(L"STATIC", L"Số nguyên tố p:", WS_CHILD, 40, 105, 180, 20, hwndParent, NULL, NULL, NULL); SendMessageW(l1, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(l1);
    editP = CreateWindowW(L"EDIT", L"", WS_CHILD | WS_BORDER | ES_AUTOHSCROLL, 230, 102, 180, 25, hwndParent, NULL, NULL, NULL); SendMessageW(editP, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(editP);
    HWND l2 = CreateWindowW(L"STATIC", L"Số nguyên tố q:", WS_CHILD, 40, 145, 180, 20, hwndParent, NULL, NULL, NULL); SendMessageW(l2, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(l2);
    editQ = CreateWindowW(L"EDIT", L"", WS_CHILD | WS_BORDER | ES_AUTOHSCROLL, 230, 142, 180, 25, hwndParent, NULL, NULL, NULL); SendMessageW(editQ, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(editQ);
    HWND b1 = CreateWindowW(L"BUTTON", L"Ngẫu nhiên", WS_CHILD, 40, 185, 110, 32, hwndParent, (HMENU)ID_BTN_NGAUNHIEN, NULL, NULL); SendMessageW(b1, WM_SETFONT, (WPARAM)hFontBold, TRUE); tabGroups[0].push_back(b1);
    HWND b2 = CreateWindowW(L"BUTTON", L"Tính toán", WS_CHILD, 160, 185, 110, 32, hwndParent, (HMENU)ID_BTN_TINHTOAN, NULL, NULL); SendMessageW(b2, WM_SETFONT, (WPARAM)hFontBold, TRUE); tabGroups[0].push_back(b2);

    HWND t2 = CreateWindowW(L"STATIC", L"Kết quả tính toán", WS_CHILD, 40, 250, 400, 25, hwndParent, NULL, NULL, NULL); SendMessageW(t2, WM_SETFONT, (WPARAM)hFontTitle, TRUE); tabGroups[0].push_back(t2);
    HWND l3 = CreateWindowW(L"STATIC", L"Modulus n:", WS_CHILD, 40, 295, 150, 20, hwndParent, NULL, NULL, NULL); SendMessageW(l3, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(l3);
    editN = CreateWindowW(L"EDIT", L"", WS_CHILD | WS_BORDER | ES_READONLY | ES_AUTOHSCROLL, 200, 292, 200, 25, hwndParent, NULL, NULL, NULL); SendMessageW(editN, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(editN);
    HWND l4 = CreateWindowW(L"STATIC", L"Hàm số Euler Φ(n):", WS_CHILD, 40, 345, 150, 20, hwndParent, NULL, NULL, NULL); SendMessageW(l4, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(l4);
    editPhi = CreateWindowW(L"EDIT", L"", WS_CHILD | WS_BORDER | ES_READONLY | ES_AUTOHSCROLL, 200, 342, 200, 25, hwndParent, NULL, NULL, NULL); SendMessageW(editPhi, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(editPhi);
    HWND l5 = CreateWindowW(L"STATIC", L"Số mũ công khai e:", WS_CHILD, 40, 395, 150, 20, hwndParent, NULL, NULL, NULL); SendMessageW(l5, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(l5);
    editE = CreateWindowW(L"EDIT", L"", WS_CHILD | WS_BORDER | ES_READONLY | ES_AUTOHSCROLL, 200, 392, 200, 25, hwndParent, NULL, NULL, NULL); SendMessageW(editE, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(editE);
    HWND l6 = CreateWindowW(L"STATIC", L"Số mũ bí mật d:", WS_CHILD, 480, 295, 150, 20, hwndParent, NULL, NULL, NULL); SendMessageW(l6, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(l6);
    editD = CreateWindowW(L"EDIT", L"", WS_CHILD | WS_BORDER | ES_READONLY | ES_AUTOHSCROLL, 640, 292, 240, 25, hwndParent, NULL, NULL, NULL); SendMessageW(editD, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(editD);
    HWND l7 = CreateWindowW(L"STATIC", L"Khóa Public (n, e):", WS_CHILD, 480, 345, 150, 20, hwndParent, NULL, NULL, NULL); SendMessageW(l7, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(l7);
    editPublic = CreateWindowW(L"EDIT", L"", WS_CHILD | WS_BORDER | ES_READONLY | ES_AUTOHSCROLL, 640, 342, 240, 25, hwndParent, NULL, NULL, NULL); SendMessageW(editPublic, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(editPublic);
    HWND l8 = CreateWindowW(L"STATIC", L"Khóa Private (n, d):", WS_CHILD, 480, 395, 150, 20, hwndParent, NULL, NULL, NULL); SendMessageW(l8, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(l8);
    editPrivate = CreateWindowW(L"EDIT", L"", WS_CHILD | WS_BORDER | ES_READONLY | ES_AUTOHSCROLL, 640, 392, 240, 25, hwndParent, NULL, NULL, NULL); SendMessageW(editPrivate, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[0].push_back(editPrivate);

    HWND t3 = CreateWindowW(L"STATIC", L"Dữ liệu gốc", WS_CHILD, 40, 65, 300, 25, hwndParent, NULL, NULL, NULL); SendMessageW(t3, WM_SETFONT, (WPARAM)hFontTitle, TRUE); tabGroups[1].push_back(t3);
    HWND b_taiGoc = CreateWindowW(L"BUTTON", L"Tải file", WS_CHILD, 40, 100, 90, 30, hwndParent, (HMENU)ID_BTN_TAIFILE_GOC, NULL, NULL); SendMessageW(b_taiGoc, WM_SETFONT, (WPARAM)hFontBold, TRUE); tabGroups[1].push_back(b_taiGoc);
    HWND b_kySo = CreateWindowW(L"BUTTON", L"Ký số", WS_CHILD, 140, 100, 90, 30, hwndParent, (HMENU)ID_BTN_KYSO, NULL, NULL); SendMessageW(b_kySo, WM_SETFONT, (WPARAM)hFontBold, TRUE); tabGroups[1].push_back(b_kySo);
    editKy_VanBan = CreateWindowW(L"EDIT", L"", WS_CHILD | WS_BORDER | ES_MULTILINE | WS_VSCROLL | ES_READONLY, 40, 140, 430, 220, hwndParent, NULL, NULL, NULL); SendMessageW(editKy_VanBan, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[1].push_back(editKy_VanBan);
    HWND l9 = CreateWindowW(L"STATIC", L"Kết quả băm (SHA-256):", WS_CHILD, 40, 380, 180, 20, hwndParent, NULL, NULL, NULL); SendMessageW(l9, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[1].push_back(l9);
    editKy_Hash = CreateWindowW(L"EDIT", L"", WS_CHILD | WS_BORDER | ES_READONLY | ES_AUTOHSCROLL, 230, 377, 240, 25, hwndParent, NULL, NULL, NULL); SendMessageW(editKy_Hash, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[1].push_back(editKy_Hash);
    HWND l10 = CreateWindowW(L"STATIC", L"Chữ ký số lớn (HEX):", WS_CHILD, 40, 420, 180, 20, hwndParent, NULL, NULL, NULL); SendMessageW(l10, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[1].push_back(l10);
    editKy_ChuKy = CreateWindowW(L"EDIT", L"", WS_CHILD | WS_BORDER | ES_READONLY | ES_AUTOHSCROLL, 230, 417, 240, 25, hwndParent, NULL, NULL, NULL); SendMessageW(editKy_ChuKy, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[1].push_back(editKy_ChuKy);
    HWND b_xuatFile = CreateWindowW(L"BUTTON", L"Xuất file", WS_CHILD, 40, 460, 150, 30, hwndParent, (HMENU)ID_BTN_XUATFILE, NULL, NULL); SendMessageW(b_xuatFile, WM_SETFONT, (WPARAM)hFontBold, TRUE); tabGroups[1].push_back(b_xuatFile);

    HWND t4 = CreateWindowW(L"STATIC", L"Xác minh chữ ký số", WS_CHILD, 520, 65, 250, 25, hwndParent, NULL, NULL, NULL); SendMessageW(t4, WM_SETFONT, (WPARAM)hFontTitle, TRUE); tabGroups[1].push_back(t4);
    HWND b_taiTL = CreateWindowW(L"BUTTON", L"Tải file tài liệu", WS_CHILD, 520, 100, 130, 30, hwndParent, (HMENU)ID_BTN_TAIFILE_NHAN, NULL, NULL); SendMessageW(b_taiTL, WM_SETFONT, (WPARAM)hFontBold, TRUE); tabGroups[1].push_back(b_taiTL);
    HWND b_taiCKS = CreateWindowW(L"BUTTON", L"Tải chữ ký số", WS_CHILD, 660, 100, 120, 30, hwndParent, (HMENU)ID_BTN_TAICHUKY, NULL, NULL); SendMessageW(b_taiCKS, WM_SETFONT, (WPARAM)hFontBold, TRUE); tabGroups[1].push_back(b_taiCKS);
    HWND b_taiPub = CreateWindowW(L"BUTTON", L"Tải Public Key", WS_CHILD, 790, 100, 130, 30, hwndParent, (HMENU)ID_BTN_TAIPUBKEY, NULL, NULL); SendMessageW(b_taiPub, WM_SETFONT, (WPARAM)hFontBold, TRUE); tabGroups[1].push_back(b_taiPub);
    HWND b_bamDL = CreateWindowW(L"BUTTON", L"Băm dữ liệu (SHA-256)", WS_CHILD, 520, 140, 400, 30, hwndParent, (HMENU)ID_BTN_BAM_DL, NULL, NULL); SendMessageW(b_bamDL, WM_SETFONT, (WPARAM)hFontBold, TRUE); tabGroups[1].push_back(b_bamDL);
    lblPubKeyStatus = CreateWindowW(L"STATIC", L"⚠️ Chưa tải Public Key", WS_CHILD, 520, 175, 200, 20, hwndParent, NULL, NULL, NULL); SendMessageW(lblPubKeyStatus, WM_SETFONT, (WPARAM)hFontBold, TRUE); tabGroups[1].push_back(lblPubKeyStatus);
    editXM_VanBan = CreateWindowW(L"EDIT", L"", WS_CHILD | WS_BORDER | ES_MULTILINE | WS_VSCROLL | ES_READONLY, 520, 200, 400, 160, hwndParent, NULL, NULL, NULL); SendMessageW(editXM_VanBan, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[1].push_back(editXM_VanBan);
    HWND l12 = CreateWindowW(L"STATIC", L"Kết quả băm (SHA-256):", WS_CHILD, 520, 380, 180, 20, hwndParent, NULL, NULL, NULL); SendMessageW(l12, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[1].push_back(l12);
    editXM_Hash = CreateWindowW(L"EDIT", L"", WS_CHILD | WS_BORDER | ES_READONLY | ES_AUTOHSCROLL, 700, 377, 220, 25, hwndParent, NULL, NULL, NULL); SendMessageW(editXM_Hash, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[1].push_back(editXM_Hash);
    HWND l11 = CreateWindowW(L"STATIC", L"Chữ ký số lớn (HEX):", WS_CHILD, 520, 420, 180, 20, hwndParent, NULL, NULL, NULL); SendMessageW(l11, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[1].push_back(l11);
    editXM_ChuKy = CreateWindowW(L"EDIT", L"", WS_CHILD | WS_BORDER | ES_AUTOHSCROLL, 700, 417, 130, 25, hwndParent, NULL, NULL, NULL); SendMessageW(editXM_ChuKy, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[1].push_back(editXM_ChuKy);
    HWND b_giaiMa = CreateWindowW(L"BUTTON", L"Giải mã", WS_CHILD, 840, 415, 80, 30, hwndParent, (HMENU)ID_BTN_GIAIMA, NULL, NULL); SendMessageW(b_giaiMa, WM_SETFONT, (WPARAM)hFontBold, TRUE); tabGroups[1].push_back(b_giaiMa);
    HWND l13 = CreateWindowW(L"STATIC", L"Giải mã chữ ký số:", WS_CHILD, 520, 460, 180, 20, hwndParent, NULL, NULL, NULL); SendMessageW(l13, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[1].push_back(l13);
    editXM_GiaiMa = CreateWindowW(L"EDIT", L"", WS_CHILD | WS_BORDER | ES_READONLY | ES_AUTOHSCROLL, 700, 457, 130, 25, hwndParent, NULL, NULL, NULL); SendMessageW(editXM_GiaiMa, WM_SETFONT, (WPARAM)hFontNormal, TRUE); tabGroups[1].push_back(editXM_GiaiMa);
    HWND b_xacMinh = CreateWindowW(L"BUTTON", L"Xác minh", WS_CHILD, 840, 455, 80, 30, hwndParent, (HMENU)ID_BTN_XACMINH, NULL, NULL); SendMessageW(b_xacMinh, WM_SETFONT, (WPARAM)hFontBold, TRUE); tabGroups[1].push_back(b_xacMinh);
    HWND b_lamMoi = CreateWindowW(L"BUTTON", L"Làm mới", WS_CHILD, 450, 560, 120, 35, hwndParent, (HMENU)ID_BTN_LAMMOI, NULL, NULL); SendMessageW(b_lamMoi, WM_SETFONT, (WPARAM)hFontBold, TRUE); tabGroups[1].push_back(b_lamMoi);
}

