package ui;

import core.AlgorithmRSA;
import core.FileUtils;
import model.KeyPair;
import model.PublicKey;

// Thư viện giao diện và hệ thống cơ bản
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class MainForm extends JFrame {

    private JTabbedPane tabbedPane;

    // --- TAB 1: TẠO CẶP KHÓA ---
    private JTextField txtP, txtQ;
    private JTextField txtN, txtPhi, txtE, txtD, txtPubKey, txtPrivKey;
    private JButton btnRandomPQ, btnCalculate;

    // --- TAB 2: KÝ SỐ XÁC MINH ---
    // Người Gửi (Bên trái)
    private JButton btnTaiFileGoc, btnKySo, btnSaoChep, btnXuatFile;
    private JTextArea txtContentGoc;
    private JTextField txtHashGoc, txtChuKyGoc;
    private File fileGoc;

    // Người Nhận (Bên phải)
    private JButton btnTaiFileTaiLieu, btnTaiChuKySo, btnTaiPublicKey;
    private JButton btnBamDuLieu, btnGiaiMa, btnXacMinh;
    private JLabel lblPublicKeyStatus;
    private JTextArea txtContentXacMinh;
    private JTextField txtHashXacMinh, txtChuKyNhan, txtGiaiMaChuKy;
    private File fileTaiLieu;

    // Nút chung
    private JButton btnSinhKhoaNgauNhien, btnLamMoi;

    // Trạng thái lưu trữ
    private KeyPair currentKeyPair;
    private PublicKey loadedPublicKey;

    public MainForm() {
        setTitle("Mô phỏng chữ ký số RSA");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Đặt Logo cho ứng dụng
        try {
            java.net.URL imgURL = getClass().getResource("/image/logo.png");
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                setIconImage(icon.getImage());
            } else {
                System.out.println("Không tìm thấy file logo tại /image/logo.png");
            }
        } catch (Exception ex) {
            System.out.println("Lỗi load logo: " + ex.getMessage());
        }

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        tabbedPane.addTab("TẠO CẶP KHÓA", createTab1());
        tabbedPane.addTab("KÝ SỐ XÁC MINH", createTab2());

        add(tabbedPane, BorderLayout.CENTER);

        initEvents();
        updatePublicKeyStatus(false);
    }

    // =====================================================================
    // GIAO DIỆN TAB 1: TẠO CẶP KHÓA
    // =====================================================================
    private JPanel createTab1() {
        JPanel pnlMain = new JPanel(new BorderLayout(10, 10));
        pnlMain.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Font titleFont = new Font("Arial", Font.BOLD, 18);

        JPanel pnlSecret = new JPanel(new GridBagLayout());
        pnlSecret.setBorder(BorderFactory.createTitledBorder(null, "Số nguyên tố bí mật",
                TitledBorder.LEFT, TitledBorder.TOP, titleFont, new Color(0, 102, 204)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; pnlSecret.add(new JLabel("Số nguyên tố bí mật p:"), gbc);
        txtP = new JTextField(30); gbc.gridx = 1; pnlSecret.add(txtP, gbc);

        gbc.gridx = 0; gbc.gridy = 1; pnlSecret.add(new JLabel("Số nguyên tố bí mật q:"), gbc);
        txtQ = new JTextField(30); gbc.gridx = 1; pnlSecret.add(txtQ, gbc);

        JPanel pnlButtons1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRandomPQ = createColorButton("Ngẫu nhiên", new Color(51, 153, 255));
        btnCalculate = createColorButton("Tính toán", new Color(0, 102, 204));
        pnlButtons1.add(btnRandomPQ);
        pnlButtons1.add(btnCalculate);
        gbc.gridx = 1; gbc.gridy = 2; pnlSecret.add(pnlButtons1, gbc);

        JPanel pnlResult = new JPanel(new GridLayout(3, 4, 15, 20));
        pnlResult.setBorder(BorderFactory.createTitledBorder(null, "Kết quả tính toán",
                TitledBorder.LEFT, TitledBorder.TOP, titleFont, new Color(0, 102, 204)));

        txtN = new JTextField(); txtN.setEditable(false);
        txtPhi = new JTextField(); txtPhi.setEditable(false);
        txtE = new JTextField(); txtE.setEditable(false);
        txtD = new JTextField(); txtD.setEditable(false);
        txtPubKey = new JTextField(); txtPubKey.setEditable(false);
        txtPrivKey = new JTextField(); txtPrivKey.setEditable(false);

        pnlResult.add(new JLabel("Modulus n:")); pnlResult.add(txtN);
        pnlResult.add(new JLabel("Số mũ bí mật d:")); pnlResult.add(txtD);
        pnlResult.add(new JLabel("Hàm số Euler Φ(n):")); pnlResult.add(txtPhi);
        pnlResult.add(new JLabel("Khóa public (n, e):")); pnlResult.add(txtPubKey);
        pnlResult.add(new JLabel("Số mũ công khai e:")); pnlResult.add(txtE);
        pnlResult.add(new JLabel("Khóa private (n, d):")); pnlResult.add(txtPrivKey);

        pnlMain.add(pnlSecret, BorderLayout.NORTH);
        pnlMain.add(pnlResult, BorderLayout.CENTER);

        return pnlMain;
    }

    // =====================================================================
    // GIAO DIỆN TAB 2: KÝ SỐ XÁC MINH
    // =====================================================================
    private JPanel createTab2() {
        JPanel pnlMain = new JPanel(new BorderLayout(10, 10));
        pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JPanel pnlTop = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("Ký số và Xác minh chữ ký số");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(0, 51, 153));
        pnlTop.add(lblTitle, BorderLayout.WEST);

        btnSinhKhoaNgauNhien = createColorButton("Sinh khóa ngẫu nhiên", new Color(0, 102, 204));
        pnlTop.add(btnSinhKhoaNgauNhien, BorderLayout.EAST);
        pnlMain.add(pnlTop, BorderLayout.NORTH);

        JPanel pnlCenter = new JPanel(new GridLayout(1, 2, 20, 0));

        // --- CỘT TRÁI: DỮ LIỆU GỐC ---
        JPanel pnlLeft = new JPanel(new BorderLayout(5, 5));
        pnlLeft.setBorder(BorderFactory.createTitledBorder(null, "Dữ liệu gốc", 
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 18), new Color(0, 102, 204)));

        JPanel pnlLeftTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnTaiFileGoc = createColorButton("Tải file", new Color(40, 167, 69));
        btnKySo = createColorButton("Ký số", new Color(0, 102, 204));
        pnlLeftTop.add(btnTaiFileGoc);
        pnlLeftTop.add(btnKySo);

        txtContentGoc = new JTextArea();
        txtContentGoc.setBorder(BorderFactory.createLineBorder(new Color(51, 153, 255), 1));
        txtContentGoc.setLineWrap(true);

        JPanel pnlLeftBottom = new JPanel(new GridBagLayout());
        GridBagConstraints gbcL = new GridBagConstraints();
        gbcL.fill = GridBagConstraints.HORIZONTAL; gbcL.insets = new Insets(5, 5, 5, 5);
        gbcL.weightx = 1.0;

        txtHashGoc = new JTextField(); txtHashGoc.setEditable(false);
        txtChuKyGoc = new JTextField();

        gbcL.gridx = 0; gbcL.gridy = 0; gbcL.weightx = 0; pnlLeftBottom.add(new JLabel("Kết quả băm (SHA-256):"), gbcL);
        gbcL.gridx = 1; gbcL.weightx = 1; pnlLeftBottom.add(txtHashGoc, gbcL);

        gbcL.gridx = 0; gbcL.gridy = 1; gbcL.weightx = 0; pnlLeftBottom.add(new JLabel("Chữ ký số:"), gbcL);
        gbcL.gridx = 1; gbcL.weightx = 1; pnlLeftBottom.add(txtChuKyGoc, gbcL);

        JPanel pnlLeftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnSaoChep = createColorButton("Sao chép chữ ký", new Color(255, 153, 51));
        btnXuatFile = createColorButton("Xuất file", new Color(0, 102, 204));
        pnlLeftButtons.add(btnSaoChep);
        pnlLeftButtons.add(btnXuatFile);

        gbcL.gridx = 0; gbcL.gridy = 2; gbcL.gridwidth = 2; pnlLeftBottom.add(pnlLeftButtons, gbcL);

        pnlLeft.add(pnlLeftTop, BorderLayout.NORTH);
        pnlLeft.add(new JScrollPane(txtContentGoc), BorderLayout.CENTER);
        pnlLeft.add(pnlLeftBottom, BorderLayout.SOUTH);

        // --- CỘT PHẢI: XÁC MINH CHỮ KÝ ---
        JPanel pnlRight = new JPanel(new BorderLayout(5, 5));
        pnlRight.setBorder(BorderFactory.createTitledBorder(null, "Xác minh chữ ký số", 
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 18), new Color(0, 102, 204)));

        JPanel pnlRightTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnTaiFileTaiLieu = createColorButton("Tải file tài liệu", new Color(40, 167, 69));
        btnTaiChuKySo = createColorButton("Tải chữ ký số", new Color(51, 153, 255));
        btnTaiPublicKey = createColorButton("Tải Public Key", new Color(153, 51, 204));
        pnlRightTop.add(btnTaiFileTaiLieu);
        pnlRightTop.add(btnTaiChuKySo);
        pnlRightTop.add(btnTaiPublicKey);

        JPanel pnlRightHeader = new JPanel(new BorderLayout());
        pnlRightHeader.add(pnlRightTop, BorderLayout.NORTH);
        btnBamDuLieu = createColorButton("Băm dữ liệu (SHA-256)", new Color(0, 102, 204));
        pnlRightHeader.add(btnBamDuLieu, BorderLayout.CENTER);
        lblPublicKeyStatus = new JLabel("⚠️ Chưa tải Public Key", JLabel.LEFT);
        lblPublicKeyStatus.setForeground(Color.RED);
        lblPublicKeyStatus.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        pnlRightHeader.add(lblPublicKeyStatus, BorderLayout.SOUTH);

        txtContentXacMinh = new JTextArea();
        txtContentXacMinh.setBorder(BorderFactory.createLineBorder(new Color(51, 153, 255), 1));
        txtContentXacMinh.setLineWrap(true);

        JPanel pnlRightBottom = new JPanel(new GridBagLayout());
        GridBagConstraints gbcR = new GridBagConstraints();
        gbcR.fill = GridBagConstraints.HORIZONTAL; gbcR.insets = new Insets(5, 5, 5, 5);

        txtHashXacMinh = new JTextField(); txtHashXacMinh.setEditable(false);
        txtChuKyNhan = new JTextField();
        txtGiaiMaChuKy = new JTextField(); txtGiaiMaChuKy.setEditable(false);
        btnGiaiMa = createColorButton("Giải mã", new Color(153, 51, 204));
        btnXacMinh = createColorButton("Xác minh", new Color(255, 102, 51));

        gbcR.gridx = 0; gbcR.gridy = 0; gbcR.weightx = 0; pnlRightBottom.add(new JLabel("Kết quả băm (SHA-256):"), gbcR);
        gbcR.gridx = 1; gbcR.weightx = 1; pnlRightBottom.add(txtHashXacMinh, gbcR);

        gbcR.gridx = 0; gbcR.gridy = 1; gbcR.weightx = 0; pnlRightBottom.add(new JLabel("Chữ ký số:"), gbcR);
        gbcR.gridx = 1; gbcR.weightx = 1; pnlRightBottom.add(txtChuKyNhan, gbcR);
        gbcR.gridx = 2; gbcR.weightx = 0; pnlRightBottom.add(btnGiaiMa, gbcR);

        gbcR.gridx = 0; gbcR.gridy = 2; gbcR.weightx = 0; pnlRightBottom.add(new JLabel("Giải mã chữ ký số:"), gbcR);
        gbcR.gridx = 1; gbcR.weightx = 1; pnlRightBottom.add(txtGiaiMaChuKy, gbcR);
        gbcR.gridx = 2; gbcR.weightx = 0; pnlRightBottom.add(btnXacMinh, gbcR);

        pnlRight.add(pnlRightHeader, BorderLayout.NORTH);
        pnlRight.add(new JScrollPane(txtContentXacMinh), BorderLayout.CENTER);
        pnlRight.add(pnlRightBottom, BorderLayout.SOUTH);

        pnlCenter.add(pnlLeft);
        pnlCenter.add(pnlRight);
        pnlMain.add(pnlCenter, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnLamMoi = createColorButton("Làm mới", new Color(108, 117, 125));
        pnlBottom.add(btnLamMoi);
        pnlMain.add(pnlBottom, BorderLayout.SOUTH);

        return pnlMain;
    }

    // =====================================================================
    // HÀM TIỆN ÍCH
    // =====================================================================
    private JButton createColorButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Đoạn code phép thuật giúp giữ nguyên màu nút trên Java 8
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        
        return btn;
    }

    private void updatePublicKeyStatus(boolean isLoaded) {
        if (isLoaded) {
            lblPublicKeyStatus.setText("✅ Đã tải Public Key");
            lblPublicKeyStatus.setForeground(new Color(40, 167, 69));
        } else {
            lblPublicKeyStatus.setText("⚠️ Chưa tải Public Key");
            lblPublicKeyStatus.setForeground(Color.RED);
        }
    }

    private void previewFileOnly(File file, JTextArea targetContentArea, JTextField targetHashArea) {
        try {
            String fileName = file.getName().toLowerCase();
            targetHashArea.setText(""); 
            
            if (fileName.endsWith(".txt")) {
                String plainText = FileUtils.readTextFile(file);
                targetContentArea.setText(plainText);
            } else if (fileName.endsWith(".pdf") || fileName.endsWith(".docx") || fileName.endsWith(".doc")) {
                long sizeKB = file.length() / 1024;
                String metadata = "[Định dạng]: " + (fileName.endsWith(".pdf") ? "Tài liệu PDF (.pdf)" : "Microsoft Word") + "\n"
                                + "[Tên Tệp]: " + file.getName() + "\n"
                                + "[Dung lượng]: " + sizeKB + " KB\n"
                                + "[Đường dẫn]: " + file.getAbsolutePath() + "\n"
                                + "---------------------------------------------------------\n"
                                + "(*) File đã sẵn sàng nạp vào bộ nhớ để ký số nhị phân.";
                targetContentArea.setText(metadata);
            } else {
                targetContentArea.setText("[Thông tin File Hệ Thống]\n" 
                        + "- Tên tệp: " + file.getName() + "\n"
                        + "- Kích thước: " + (file.length() / 1024) + " KB\n"
                        + "- Đường dẫn: " + file.getAbsolutePath() + "\n"
                        + "---------------------------------------------------------\n"
                        + "(*) File đã sẵn sàng để băm dữ liệu.");
            }
            targetContentArea.setCaretPosition(0);

        } catch (Exception ex) {
            targetContentArea.setText("Lỗi đọc thông tin tệp: " + ex.getMessage());
        }
    }

    // =====================================================================
    // SỰ KIỆN LOGIC
    // =====================================================================
    private void initEvents() {

        btnRandomPQ.addActionListener(e -> {
            SecureRandom rng = new SecureRandom();
            BigInteger p = BigInteger.probablePrime(512, rng);
            BigInteger q = BigInteger.probablePrime(512, rng);
            txtP.setText(p.toString());
            txtQ.setText(q.toString());
        });

        btnCalculate.addActionListener(e -> {
            try {
                BigInteger p = new BigInteger(txtP.getText().trim());
                BigInteger q = new BigInteger(txtQ.getText().trim());
                
                currentKeyPair = AlgorithmRSA.generateKey(p, q);
                loadedPublicKey = currentKeyPair.getPublicKey(); 
                updatePublicKeyStatus(true); 

                txtN.setText(currentKeyPair.getPublicKey().getN().toString());
                txtE.setText(currentKeyPair.getPublicKey().getE().toString());
                txtD.setText(currentKeyPair.getPrivateKey().getD().toString());
                
                BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
                txtPhi.setText(phi.toString());

                txtPubKey.setText("(" + currentKeyPair.getPublicKey().getN() + ", " + currentKeyPair.getPublicKey().getE() + ")");
                txtPrivKey.setText("(" + currentKeyPair.getPrivateKey().getN() + ", " + currentKeyPair.getPrivateKey().getD() + ")");
                
                JOptionPane.showMessageDialog(this, "Tính toán & tạo khóa thành công!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnSinhKhoaNgauNhien.addActionListener(e -> {
            currentKeyPair = AlgorithmRSA.generateKey(1024);
            loadedPublicKey = currentKeyPair.getPublicKey();
            updatePublicKeyStatus(true);
            JOptionPane.showMessageDialog(this, "Đã sinh cặp khóa RSA ngẫu nhiên (1024-bit) thành công!");
        });

        btnTaiFileGoc.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                fileGoc = chooser.getSelectedFile();
                previewFileOnly(fileGoc, txtContentGoc, txtHashGoc);
                txtChuKyGoc.setText("");
            }
        });

        btnKySo.addActionListener(e -> {
            if (fileGoc == null || currentKeyPair == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng tải file gốc và đảm bảo đã tạo khóa bí mật!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                byte[] data = FileUtils.readBinaryFile(fileGoc);
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                BigInteger hash = new BigInteger(1, md.digest(data));
                txtHashGoc.setText(hash.toString(16).toUpperCase());

                BigInteger sig = AlgorithmRSA.sign(data, currentKeyPair.getPrivateKey());
                txtChuKyGoc.setText(sig.toString(16));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi ký số: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnSaoChep.addActionListener(e -> {
            String text = txtChuKyGoc.getText();
            if (!text.isEmpty()) {
                StringSelection selection = new StringSelection(text);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, null);
                JOptionPane.showMessageDialog(this, "Đã copy chữ ký vào Clipboard!");
            }
        });

        btnXuatFile.addActionListener(e -> {
            if (txtChuKyGoc.getText().isEmpty() || fileGoc == null || currentKeyPair == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn file và thực hiện ký số trước khi xuất!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Chọn thư mục để xuất các file");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
            
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File dir = chooser.getSelectedFile();
                    
                    String originalName = fileGoc.getName();
                    int dotIndex = originalName.lastIndexOf('.');
                    String baseName = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
                    
                    String fileSignedName = baseName + ".signed";
                    String fileSigName = baseName + "_signature.txt";
                    String filePubKeyName = baseName + "_public_key.txt";
                    
                    File fSigned = new File(dir, fileSignedName);
                    File fSig = new File(dir, fileSigName);
                    File fPubKey = new File(dir, filePubKeyName);

                    java.nio.file.Files.copy(fileGoc.toPath(), fSigned.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    FileUtils.writeTextFile(fSig, txtChuKyGoc.getText());
                    FileUtils.writeTextFile(fPubKey, currentKeyPair.getPublicKey().toFileString());

                    String htmlMessage = "<html><body style='font-family: Tahoma, Arial, sans-serif; font-size: 12px; padding: 5px;'>"
                            + "Đã xuất file thành công!<br><br>"
                            + "&nbsp;&nbsp;📄 File tài liệu đã ký: " + fileSignedName + "<br>"
                            + "&nbsp;&nbsp;🔐 File chữ ký số: " + fileSigName + "<br>"
                            + "&nbsp;&nbsp;🔑 File public key: " + filePubKeyName + "<br><br>"
                            + "Gửi các file này cho khách hàng để xác minh."
                            + "</body></html>";

                    JOptionPane.showMessageDialog(this, htmlMessage, "Thành công", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnTaiFileTaiLieu.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                fileTaiLieu = chooser.getSelectedFile();
                previewFileOnly(fileTaiLieu, txtContentXacMinh, txtHashXacMinh);
            }
        });

        btnTaiChuKySo.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    String sigContent = FileUtils.readTextFile(chooser.getSelectedFile());
                    txtChuKyNhan.setText(sigContent.trim());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi đọc chữ ký: " + ex.getMessage());
                }
            }
        });

        btnTaiPublicKey.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    String pubContent = FileUtils.readTextFile(chooser.getSelectedFile());
                    loadedPublicKey = PublicKey.fromFileString(pubContent);
                    updatePublicKeyStatus(true);
                    JOptionPane.showMessageDialog(this, "Tải Public Key thành công!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi đọc Public Key: " + ex.getMessage());
                }
            }
        });

        btnBamDuLieu.addActionListener(e -> {
            if (fileTaiLieu == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng tải file tài liệu trước!");
                return;
            }
            try {
                byte[] data = FileUtils.readBinaryFile(fileTaiLieu);
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                BigInteger hash = new BigInteger(1, md.digest(data));
                txtHashXacMinh.setText(hash.toString(16).toUpperCase());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi băm dữ liệu: " + ex.getMessage());
            }
        });

        btnGiaiMa.addActionListener(e -> {
            if (loadedPublicKey == null || txtChuKyNhan.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng tải Public Key và điền Chữ ký số!");
                return;
            }
            try {
                BigInteger sig = new BigInteger(txtChuKyNhan.getText().trim(), 16);
                BigInteger decryptedSig = sig.modPow(loadedPublicKey.getE(), loadedPublicKey.getN());
                txtGiaiMaChuKy.setText(decryptedSig.toString(16).toUpperCase());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi giải mã: " + ex.getMessage());
            }
        });

        btnXacMinh.addActionListener(e -> {
            String hashGoc = txtHashXacMinh.getText().trim();
            String hashGiaiMa = txtGiaiMaChuKy.getText().trim();

            if (hashGoc.isEmpty() || hashGiaiMa.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng thực hiện Băm dữ liệu và Giải mã chữ ký trước khi Xác minh!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (hashGoc.equals(hashGiaiMa)) {
                JOptionPane.showMessageDialog(this, "✓ CHỮ KÝ HỢP LỆ!\nTài liệu toàn vẹn, xác minh thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String errorHtmlMessage = "<html><body style='font-family: Tahoma, Arial, sans-serif; font-size: 12px; padding: 2px;'>"
                        + "<b style='color: #e53935; font-size: 13px;'>❌ Chữ ký không hợp lệ!</b><br><br>"
                        + "Có thể do:<br>"
                        + "- Chữ ký đã bị thay đổi<br>"
                        + "- Nội dung dữ liệu đã bị sửa đổi<br>"
                        + "- Chữ ký không đúng<br>"
                        + "- Public Key không khớp"
                        + "</body></html>";
                
                JOptionPane.showMessageDialog(this, errorHtmlMessage, "Xác minh thất bại", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnLamMoi.addActionListener(e -> {
            txtP.setText(""); txtQ.setText(""); txtN.setText(""); txtPhi.setText("");
            txtE.setText(""); txtD.setText(""); txtPubKey.setText(""); txtPrivKey.setText("");
            txtContentGoc.setText(""); txtHashGoc.setText(""); txtChuKyGoc.setText("");
            txtContentXacMinh.setText(""); txtHashXacMinh.setText(""); txtChuKyNhan.setText(""); txtGiaiMaChuKy.setText("");
            fileGoc = null; fileTaiLieu = null;
            currentKeyPair = null; loadedPublicKey = null;
            updatePublicKeyStatus(false);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new MainForm().setVisible(true);
        });
    }
}
