package entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "SanPham")
public class SanPham {

    @Id
    @Column(name = "MaSP", length = 10)
    private String maSP;

    @Column(name = "TenSP", nullable = false, length = 100)
    private String tenSP;

    @Column(name = "LoaiSP", length = 50)
    private String loaiSP;

    @Column(name = "SizeSP", length = 20)
    private String sizeSP;

    @Column(name = "DonGia")
    private BigDecimal donGia;

    @Column(name = "SoLuongTon")
    private Integer soLuongTon;

    @Column(name = "MoTa", length = 200)
    private String moTa;

    @Column(name = "ThanhPhan", length = 500)
    private String thanhPhan;

    @Column(name = "HinhAnh", length = 255)
    private String hinhAnh;

    public SanPham() {
    }

    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }

    public String getTenSP() {
        return tenSP;
    }

    public void setTenSP(String tenSP) {
        this.tenSP = tenSP;
    }

    public String getLoaiSP() {
        return loaiSP;
    }

    public void setLoaiSP(String loaiSP) {
        this.loaiSP = loaiSP;
    }

    public String getSizeSP() {
        return sizeSP;
    }

    public void setSizeSP(String sizeSP) {
        this.sizeSP = sizeSP;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public Integer getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(Integer soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getThanhPhan() {
        return thanhPhan;
    }

    public void setThanhPhan(String thanhPhan) {
        this.thanhPhan = thanhPhan;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }
}
