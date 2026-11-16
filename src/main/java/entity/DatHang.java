package entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "DatHang")
public class DatHang {

    @Id
    @Column(name = "MaDatHang", length = 10)
    private String maDatHang;

    @Column(name = "MaKH", length = 10, nullable = false)
    private String maKH;

    @Column(name = "MaNV", length = 10)
    private String maNV;

    @Column(name = "MaNVGiaoHang", length = 10)
    private String maNVGiaoHang;

    @Column(name = "DaNhanGiaoHang")
    private Boolean daNhanGiaoHang;

    @Column(name = "NgayDat")
    private Timestamp ngayDat;

    @Column(name = "TrangThai", length = 50)
    private String trangThai;

    @Column(name = "SoDienThoai", length = 15)
    private String soDienThoai;

    @Column(name = "DiaChiGiaoHang", length = 255)
    private String diaChiGiaoHang;

    @Column(name = "TongTien", precision = 18, scale = 2)
    private BigDecimal tongTien;

    public DatHang() {
    }

    public DatHang(String maDatHang, String maKH, Timestamp ngayDat, String trangThai,
                   String soDienThoai, String diaChiGiaoHang, BigDecimal tongTien) {
        this(maDatHang, maKH, null, ngayDat, trangThai, soDienThoai, diaChiGiaoHang, tongTien);
    }

    public DatHang(String maDatHang, String maKH, String maNV, Timestamp ngayDat, String trangThai,
                   String soDienThoai, String diaChiGiaoHang, BigDecimal tongTien) {
        this.maDatHang = maDatHang;
        this.maKH = maKH;
        this.maNV = maNV;
        this.ngayDat = ngayDat;
        this.trangThai = trangThai;
        this.soDienThoai = soDienThoai;
        this.diaChiGiaoHang = diaChiGiaoHang;
        this.tongTien = tongTien;
    }

    public String getMaDatHang() {
        return maDatHang;
    }

    public void setMaDatHang(String maDatHang) {
        this.maDatHang = maDatHang;
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

    public Boolean getDaNhanGiaoHang() {
        return daNhanGiaoHang;
    }

    public void setDaNhanGiaoHang(Boolean daNhanGiaoHang) {
        this.daNhanGiaoHang = daNhanGiaoHang;
    }

    public Timestamp getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(Timestamp ngayDat) {
        this.ngayDat = ngayDat;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
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

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }
}
