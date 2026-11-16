package entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "GioHang")
public class GioHang {

    @Id
    @Column(name = "MaGioHang", length = 10)
    private String maGioHang;

    @Column(name = "MaKH", length = 10)
    private String maKH;

    @Column(name = "NgayTao")
    private Timestamp ngayTao;

    public GioHang() {
    }

    public GioHang(String maGioHang, String maKH, Timestamp ngayTao) {
        this.maGioHang = maGioHang;
        this.maKH = maKH;
        this.ngayTao = ngayTao;
    }

    public String getMaGioHang() {
        return maGioHang;
    }

    public void setMaGioHang(String maGioHang) {
        this.maGioHang = maGioHang;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public Timestamp getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Timestamp ngayTao) {
        this.ngayTao = ngayTao;
    }
}
