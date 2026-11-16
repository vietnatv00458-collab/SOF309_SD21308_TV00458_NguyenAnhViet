package entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ChiTietGioHang")
public class ChiTietGioHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaCTGH")
    private Integer maCTGH;

    @Column(name = "MaGioHang", length = 10, nullable = false)
    private String maGioHang;

    @Column(name = "MaDatHang", length = 10)
    private String maDatHang;

    @Column(name = "MaSP", length = 10, nullable = false)
    private String maSP;

    @Column(name = "MaSize", length = 10)
    private String maSize;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "DonGia", precision = 18, scale = 2, nullable = false)
    private BigDecimal donGia;

    public ChiTietGioHang() {
    }

    public Integer getMaCTGH() {
        return maCTGH;
    }

    public void setMaCTGH(Integer maCTGH) {
        this.maCTGH = maCTGH;
    }

    public String getMaGioHang() {
        return maGioHang;
    }

    public void setMaGioHang(String maGioHang) {
        this.maGioHang = maGioHang;
    }

    public String getMaDatHang() {
        return maDatHang;
    }

    public void setMaDatHang(String maDatHang) {
        this.maDatHang = maDatHang;
    }

    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }

    public String getMaSize() {
        return maSize;
    }

    public void setMaSize(String maSize) {
        this.maSize = maSize;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }
}
