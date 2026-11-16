package entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "HoaDon")
public class HoaDon {

    @Id
    @Column(name = "MaHD", length = 10)
    private String maHD;

    @Column(name = "MaKH", length = 10)
    private String maKH;

    @Column(name = "MaNV", length = 10)
    private String maNV;

    @Column(name = "MaNVGiaoHang", length = 10)
    private String maNVGiaoHang;

    @Column(name = "MaDatHang", length = 10)
    private String maDatHang;

    @Column(name = "MaKM", length = 10)
    private String maKM;

    @Column(name = "SoDienThoai", length = 15)
    private String soDienThoai;

    @Column(name = "DiaChiGiaoHang", length = 255)
    private String diaChiGiaoHang;

    @Column(name = "DaNhanGiaoHang")
    private Boolean daNhanGiaoHang;

    @Column(name = "NgayLap")
    private Timestamp ngayLap;

    @Column(name = "TongTien", precision = 18, scale = 2)
    private BigDecimal tongTien;

    @Column(name = "GiamGia", precision = 18, scale = 2)
    private BigDecimal giamGia;

    @Column(name = "ThanhTien", precision = 18, scale = 2)
    private BigDecimal thanhTien;

    @Column(name = "TrangThai", length = 30)
    private String trangThai;

    public HoaDon() {
    }

    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getMaNVGiaoHang() {
        return maNVGiaoHang;
    }

    public void setMaNVGiaoHang(String maNVGiaoHang) {
        this.maNVGiaoHang = maNVGiaoHang;
    }

    public String getMaDatHang() {
        return maDatHang;
    }

    public void setMaDatHang(String maDatHang) {
        this.maDatHang = maDatHang;
    }

    public String getMaKM() {
        return maKM;
    }

    public void setMaKM(String maKM) {
        this.maKM = maKM;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getDiaChiGiaoHang() {
        return diaChiGiaoHang;
    }

    public void setDiaChiGiaoHang(String diaChiGiaoHang) {
        this.diaChiGiaoHang = diaChiGiaoHang;
    }

    public Boolean getDaNhanGiaoHang() {
        return daNhanGiaoHang;
    }

    public void setDaNhanGiaoHang(Boolean daNhanGiaoHang) {
        this.daNhanGiaoHang = daNhanGiaoHang;
    }

    public Timestamp getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Timestamp ngayLap) {
        this.ngayLap = ngayLap;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public BigDecimal getGiamGia() {
        return giamGia;
    }

    public void setGiamGia(BigDecimal giamGia) {
        this.giamGia = giamGia;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
