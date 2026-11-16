package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "Size")
public class Size {

    @Id
    @Column(name = "MaSize", length = 10)
    private String maSize;

    @Column(name = "TenSize", length = 20)
    private String tenSize;

    @Column(name = "PhuThu", precision = 18, scale = 2)
    private BigDecimal phuThu;

    public Size() {
    }

    public String getMaSize() {
        return maSize;
    }

    public void setMaSize(String maSize) {
        this.maSize = maSize;
    }

    public String getTenSize() {
        return tenSize;
    }

    public void setTenSize(String tenSize) {
        this.tenSize = tenSize;
    }

    public BigDecimal getPhuThu() {
        return phuThu;
    }

    public void setPhuThu(BigDecimal phuThu) {
        this.phuThu = phuThu;
    }
}
