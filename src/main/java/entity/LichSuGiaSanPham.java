package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Entity
@Table(name = "LichSuGiaSanPham")
public class LichSuGiaSanPham {

    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter INPUT_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaLSGia")
    private Integer maLSGia;

    @Column(name = "MaSP", length = 10, nullable = false)
    private String maSP;

    @Column(name = "GiaCu", precision = 18, scale = 2, nullable = false)
    private BigDecimal giaCu;

    @Column(name = "GiaMoi", precision = 18, scale = 2, nullable = false)
    private BigDecimal giaMoi;

    @Column(name = "NgayApDung")
    private LocalDateTime ngayApDung;

    @Column(name = "LyDo", length = 255)
    private String lyDo;

    public LichSuGiaSanPham() {
    }

    @PrePersist
    protected void prePersist() {
        if (ngayApDung == null) {
            ngayApDung = LocalDateTime.now();
        }
    }

    public Integer getMaLSGia() {
        return maLSGia;
    }

    public void setMaLSGia(Integer maLSGia) {
        this.maLSGia = maLSGia;
    }

    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }

    public BigDecimal getGiaCu() {
        return giaCu;
    }

    public void setGiaCu(BigDecimal giaCu) {
        this.giaCu = giaCu;
    }

    public BigDecimal getGiaMoi() {
        return giaMoi;
    }

    public void setGiaMoi(BigDecimal giaMoi) {
        this.giaMoi = giaMoi;
    }

    public LocalDateTime getNgayApDung() {
        return ngayApDung;
    }

    public void setNgayApDung(LocalDateTime ngayApDung) {
        this.ngayApDung = ngayApDung;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    @Transient
    public String getNgayApDungText() {
        return ngayApDung != null ? ngayApDung.format(DISPLAY_DATE_TIME) : "";
    }

    @Transient
    public String getNgayApDungInputValue() {
        return ngayApDung != null ? ngayApDung.format(INPUT_DATE_TIME) : "";
    }

    @Transient
    public String getGiaCuText() {
        return formatMoney(giaCu);
    }

    @Transient
    public String getGiaMoiText() {
        return formatMoney(giaMoi);
    }

    private String formatMoney(BigDecimal amount) {
        BigDecimal safeAmount = amount != null ? amount : BigDecimal.ZERO;
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(safeAmount) + " đ";
    }
}
