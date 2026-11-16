package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "DoanhThu")
public class DoanhThu {

    @Id
    @Column(name = "MaDT", length = 10)
    private String maDT;

    @Column(name = "MaHD", length = 10)
    private String maHD;

    @Column(name = "Ngay")
    private LocalDate ngay;

    @Column(name = "DoanhThu", precision = 18, scale = 2)
    private BigDecimal doanhThu;

    @Column(name = "ChiPhi", precision = 18, scale = 2)
    private BigDecimal chiPhi;

    @Column(name = "LoiNhuan", insertable = false, updatable = false)
    private BigDecimal loiNhuan;

    public DoanhThu() {
    }

    public String getMaDT() {
        return maDT;
    }

    public void setMaDT(String maDT) {
        this.maDT = maDT;
    }

    public String getMaHD() {
        return maHD;
    }

    public void setMaHD(String maHD) {
        this.maHD = maHD;
    }

    public LocalDate getNgay() {
        return ngay;
    }

    public void setNgay(LocalDate ngay) {
        this.ngay = ngay;
    }

    public BigDecimal getDoanhThu() {
        return doanhThu;
    }

    public void setDoanhThu(BigDecimal doanhThu) {
        this.doanhThu = doanhThu;
    }

    public BigDecimal getChiPhi() {
        return chiPhi;
    }

    public void setChiPhi(BigDecimal chiPhi) {
        this.chiPhi = chiPhi;
    }

    public BigDecimal getLoiNhuan() {
        return loiNhuan;
    }
}
