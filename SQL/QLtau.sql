CREATE DATABASE QuanLyBanVeTau;
GO

-- SỬ DỤNG DATABASE
USE QuanLyBanVeTau;
GO
-- DROP TABLE

IF OBJECT_ID('DonDoiTraVe') IS NOT NULL DROP TABLE DonDoiTraVe;
IF OBJECT_ID('Ve') IS NOT NULL DROP TABLE Ve;
IF OBJECT_ID('HoaDon') IS NOT NULL DROP TABLE HoaDon;
IF OBJECT_ID('Ghe') IS NOT NULL DROP TABLE Ghe;
IF OBJECT_ID('ChuyenTau') IS NOT NULL DROP TABLE ChuyenTau;
IF OBJECT_ID('ToaTau') IS NOT NULL DROP TABLE ToaTau;
IF OBJECT_ID('TaiKhoan') IS NOT NULL DROP TABLE TaiKhoan;
IF OBJECT_ID('KhuyenMai') IS NOT NULL DROP TABLE KhuyenMai;
IF OBJECT_ID('KhachHang') IS NOT NULL DROP TABLE KhachHang;
IF OBJECT_ID('NhanVien') IS NOT NULL DROP TABLE NhanVien;
IF OBJECT_ID('Tau') IS NOT NULL DROP TABLE Tau;
IF OBJECT_ID('Ga') IS NOT NULL DROP TABLE Ga;



CREATE TABLE NhanVien (
    maNV VARCHAR(20) PRIMARY KEY,
    hoTenNV NVARCHAR(100) NOT NULL,
    email VARCHAR(100),
    ngaySinh DATE,
    soDT VARCHAR(15),
    gioiTinh BIT,
    diaChi NVARCHAR(255),
    soCCCD VARCHAR(20) UNIQUE,
    loaiNV VARCHAR(50) NOT NULL CHECK (
        loaiNV IN ('NHAN_VIEN_QUAN_LY', 'NHAN_VIEN_BAN_VE')
    )
);

CREATE TABLE KhachHang (
    maKH VARCHAR(20) PRIMARY KEY,
    hoTenKH NVARCHAR(100) NOT NULL,
    cccd VARCHAR(20) UNIQUE,
    sdt VARCHAR(15),
    email VARCHAR(100),
    ngaySinh DATE,
    loaiKhachHang VARCHAR(50) CHECK (
        loaiKhachHang IN (
            'TRE_EM_DUOI_6',
            'TRE_EM_6_DEN_10',
            'HOC_SINH_SINH_VIEN',
            'NGUOI_LON',
            'NGUOI_CAO_TUOI'
        )
    ),
    trangThai VARCHAR(50) DEFAULT 'DANG_HOAT_DONG',
    ngayDangKy DATE DEFAULT GETDATE()
);

CREATE TABLE KhuyenMai (
    maKhuyenMai VARCHAR(20) PRIMARY KEY,
    tenKhuyenMai NVARCHAR(255),
    trangThai BIT,
    moTaChiTiet NVARCHAR(MAX),
    loaiHanhKhach VARCHAR(50)
);

CREATE TABLE Tau (
    maTau VARCHAR(20) PRIMARY KEY,
    tenTau NVARCHAR(100) NOT NULL,
    soLuongToa INT CHECK (soLuongToa > 0),
    trangThai VARCHAR(50) DEFAULT 'HOAT_DONG' CHECK (trangThai IN ('HOAT_DONG', 'BAO_TRI'))
);

CREATE TABLE Ga (
    maGa VARCHAR(20) PRIMARY KEY,
    tenGa NVARCHAR(100) NOT NULL,
    diaChi NVARCHAR(255)
);



CREATE TABLE TaiKhoan (
    maTaiKhoan VARCHAR(20) PRIMARY KEY,
    tenTaiKhoan VARCHAR(50) UNIQUE NOT NULL,
    matKhau VARCHAR(255) NOT NULL,
    ngayDangNhap DATETIME,
    ngayDangXuat DATETIME,
    trangThai BIT DEFAULT 1,
    maNV VARCHAR(20) UNIQUE NOT NULL,
    FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

CREATE TABLE ToaTau (
    maToaTau VARCHAR(20) PRIMARY KEY,
    soToa INT,
    soLuongGhe INT CHECK (soLuongGhe > 0),
    loaiToa VARCHAR(50) CHECK (loaiToa IN ('TOA_THUONG', 'TOA_VIP')),
    heSoLoaiToa DECIMAL(5,2) CHECK (heSoLoaiToa > 0),
    maTau VARCHAR(20) NOT NULL,
    FOREIGN KEY (maTau) REFERENCES Tau(maTau)
);

CREATE TABLE ChuyenTau (
    maChuyen VARCHAR(20) PRIMARY KEY,
    thoiGianKhoiHanh DATETIME,
    thoiGianDuKien DATETIME,
    ghiChu NVARCHAR(MAX),
    gaDi VARCHAR(20) NOT NULL,
    gaDen VARCHAR(20) NOT NULL,
    maTau VARCHAR(20) NOT NULL,
    FOREIGN KEY (gaDi) REFERENCES Ga(maGa),
    FOREIGN KEY (gaDen) REFERENCES Ga(maGa),
    FOREIGN KEY (maTau) REFERENCES Tau(maTau),
    CHECK (gaDi <> gaDen)
);



CREATE TABLE Ghe (
    maGhe VARCHAR(20) PRIMARY KEY,
    loaiGhe VARCHAR(50) CHECK (loaiGhe IN ('GIUONG_NAM', 'GHE_MEM', 'GHE_CUNG')),
    maToaTau VARCHAR(20) NOT NULL,
    FOREIGN KEY (maToaTau) REFERENCES ToaTau(maToaTau)
);

CREATE TABLE HoaDon (
    maHoaDon VARCHAR(20) PRIMARY KEY,
    ngayLapHD DATETIME DEFAULT GETDATE(),
    maNV VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NOT NULL,
    tongTien DECIMAL(18,2),
    tienNhan DECIMAL(18,2),
    tienThua AS (tienNhan - tongTien),
    phuongThucThanhToan VARCHAR(50) CHECK (
        phuongThucThanhToan IN (
            'TIEN_MAT',
            'CHUYEN_KHOAN',
            'THE_NGAN_HANG',
            'VI_DIEN_TU'
        )
    ),
    FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);

-- BẢNG VÉ


CREATE TABLE Ve (
    maVe VARCHAR(20) PRIMARY KEY,
    ngayMua DATETIME DEFAULT GETDATE(),
    loaiVe VARCHAR(50) CHECK (loaiVe IN ('MOT_CHIEU', 'KHU_HOI')),
    trangThaiVe VARCHAR(50) NOT NULL CHECK (
        trangThaiVe IN ('CHO_THANH_TOAN', 'DA_THANH_TOAN', 'DA_HUY')
    ),
    giaVe DECIMAL(18,2) CHECK (giaVe >= 0),
    maGhe VARCHAR(20) NOT NULL,
    maKhuyenMai VARCHAR(20),
    maChuyen VARCHAR(20) NOT NULL,

    FOREIGN KEY (maGhe) REFERENCES Ghe(maGhe),
    FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai(maKhuyenMai),
    FOREIGN KEY (maChuyen) REFERENCES ChuyenTau(maChuyen)
);

-- UNIQUE INDEX (CHỐNG TRÙNG GHẾ THÔNG MINH)


CREATE UNIQUE INDEX UX_ChongTrungGhe 
ON Ve (maGhe, maChuyen) 
WHERE trangThaiVe IN ('CHO_THANH_TOAN', 'DA_THANH_TOAN');


-- ĐƠN ĐỔI TRẢ VÉ


CREATE TABLE DonDoiTraVe (
    maDon VARCHAR(20) PRIMARY KEY,
    ngayLap DATETIME DEFAULT GETDATE(),
    loaiDon VARCHAR(50) CHECK (loaiDon IN ('DON_DOI', 'DON_TRA')),
    tienBu DECIMAL(18,2),
    tienHoanTra DECIMAL(18,2),
    maVe VARCHAR(20) UNIQUE,
    FOREIGN KEY (maVe) REFERENCES Ve(maVe)
);

-- ==========================================
-- DỮ LIỆU MẪU (MOCK DATA)
-- ==========================================

-- 1. NhanVien
INSERT INTO NhanVien (maNV, hoTenNV, email, ngaySinh, soDT, gioiTinh, diaChi, soCCCD, loaiNV) VALUES
('MP0001', N'Nguyễn Văn Quản Lý', 'quanly@tau.vn', '1985-05-15', '0901234567', 1, N'Hà Nội', '001085000001', 'NHAN_VIEN_QUAN_LY'),
('MP0002', N'Trần Thị Bán Vé 1', 'banve1@tau.vn', '1995-08-20', '0912345678', 0, N'Hà Nội', '001095000002', 'NHAN_VIEN_BAN_VE'),
('MP0003', N'Lê Văn Bán Vé 2', 'banve2@tau.vn', '1998-02-10', '0923456789', 1, N'TP.HCM', '079098000003', 'NHAN_VIEN_BAN_VE');

-- 2. TaiKhoan
INSERT INTO TaiKhoan (maTaiKhoan, tenTaiKhoan, matKhau, trangThai, maNV) VALUES
('USER0001', 'admin', '123456', 1, 'MP0001'),
('USER0002', 'nvbanve1', '123456', 1, 'MP0002'),
('USER0003', 'nvbanve2', '123456', 1, 'MP0003');

-- 3. KhachHang
INSERT INTO KhachHang (maKH, hoTenKH, cccd, sdt, email, ngaySinh, loaiKhachHang, trangThai) VALUES
('HK0000000190', N'Phạm Minh Tuấn', '001090001111', '0987654321', 'tuanpm@gmail.com', '1990-10-10', 'NGUOI_LON', 'DANG_HOAT_DONG'),
('HK0000000205', N'Nguyễn Thu Hà', '001202002222', '0976543210', 'hant@gmail.com', '2005-04-15', 'HOC_SINH_SINH_VIEN', 'DANG_HOAT_DONG'),
('HK0000000350', N'Trần Đình Long', '001050003333', '0965432109', 'longtd@gmail.com', '1950-12-25', 'NGUOI_CAO_TUOI', 'DANG_HOAT_DONG');

-- 4. KhuyenMai
INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, trangThai, moTaChiTiet, loaiHanhKhach) VALUES
('KM-0526-0001', N'Giảm giá cho Học sinh - Sinh viên', 1, N'Giảm 10% giá vé', 'HOC_SINH_SINH_VIEN'),
('KM-0526-0002', N'Giảm giá cho Người cao tuổi', 1, N'Giảm 15% giá vé', 'NGUOI_CAO_TUOI'),
('KM-0526-0003', N'Giảm giá cho Trẻ em', 1, N'Giảm 25% giá vé', 'TRE_EM_6_DEN_10');

-- 5. Ga
INSERT INTO Ga (maGa, tenGa, diaChi) VALUES
('G_00HAN', N'Ga Hà Nội', N'120 Lê Duẩn, Hoàn Kiếm, Hà Nội'),
('G_00DAN', N'Ga Đà Nẵng', N'791 Hải Phòng, Thanh Khê, Đà Nẵng'),
('G_00SGO', N'Ga Sài Gòn', N'01 Nguyễn Thông, Quận 3, TP.HCM');

-- 6. Tau
INSERT INTO Tau (maTau, tenTau, soLuongToa, trangThai) VALUES
('Train001', N'Tàu Bắc Nam SE1', 10, 'HOAT_DONG'),
('Train002', N'Tàu Bắc Nam SE2', 12, 'HOAT_DONG'),
('Train003', N'Tàu Thống Nhất 1', 8, 'BAO_TRI');

-- 7. ToaTau
INSERT INTO ToaTau (maToaTau, soToa, soLuongGhe, loaiToa, heSoLoaiToa, maTau) VALUES
('Toa01Train001', 1, 40, 'TOA_THUONG', 1.0, 'Train001'),
('Toa02Train001', 2, 30, 'TOA_VIP', 1.5, 'Train001'),
('Toa01Train002', 1, 40, 'TOA_THUONG', 1.0, 'Train002');

-- 8. Ghe
INSERT INTO Ghe (maGhe, loaiGhe, maToaTau) VALUES
('SEAT10101', 'GHE_MEM', 'Toa01Train001'),
('SEAT20101', 'GHE_MEM', 'Toa01Train001'),
('SEAT10201', 'GIUONG_NAM', 'Toa02Train001'),
('SEAT20201', 'GIUONG_NAM', 'Toa02Train001');

-- 9. ChuyenTau
INSERT INTO ChuyenTau (maChuyen, thoiGianKhoiHanh, thoiGianDuKien, ghiChu, gaDi, gaDen, maTau) VALUES
('CT01Train001', '2026-06-01 19:00:00', '2026-06-03 05:00:00', N'Chuyến đi thường lệ', 'G_00HAN', 'G_00SGO', 'Train001'),
('CT02Train002', '2026-06-02 20:00:00', '2026-06-04 06:00:00', N'Chuyến đi thường lệ', 'G_00SGO', 'G_00HAN', 'Train002');

-- 10. HoaDon
INSERT INTO HoaDon (maHoaDon, ngayLapHD, maNV, maKH, tongTien, tienNhan, phuongThucThanhToan) VALUES
('HD010526-0001', '2026-05-01 10:00:00', 'MP0002', 'HK0000000190', 1500000, 1500000, 'CHUYEN_KHOAN'),
('HD020526-0002', '2026-05-02 11:30:00', 'MP0003', 'HK0000000205', 800000, 1000000, 'TIEN_MAT');

-- 11. Ve
INSERT INTO Ve (maVe, ngayMua, loaiVe, trangThaiVe, giaVe, maGhe, maKhuyenMai, maChuyen) VALUES
('001CT01Train001123', '2026-05-01 10:05:00', 'MOT_CHIEU', 'DA_THANH_TOAN', 1500000, 'SEAT10201', NULL, 'CT01Train001'),
('001CT01Train001456', '2026-05-02 11:35:00', 'MOT_CHIEU', 'DA_THANH_TOAN', 800000, 'SEAT10101', 'KM-0526-0001', 'CT01Train001');

-- 12. DonDoiTraVe
INSERT INTO DonDoiTraVe (maDon, ngayLap, loaiDon, tienBu, tienHoanTra, maVe) VALUES
('DT-0526-0001', '2026-05-03 14:00:00', 'DON_TRA', 0, 1200000, '001CT01Train001123');
