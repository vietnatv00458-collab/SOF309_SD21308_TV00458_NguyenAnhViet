package utils;

import entity.TaiKhoan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class AuthStore {

    private static final String STORE_DIR = ".quanlycafe";
    private static final String STORE_FILE = "auth-users.txt";
    private static final String STORE_DIR_PROPERTY = "auth.store.dir";
    private static final String STORE_DIR_ENV = "AUTH_STORE_DIR";

    public AuthStore() {
        try {
            seedDefaultCustomer();
            seedDefaultAdmin();
            seedDefaultDelivery();
        } catch (IOException ignored) {
        }
    }

    public synchronized boolean existsByUsername(String username) throws IOException {
        return findByUsername(username) != null;
    }

    public synchronized boolean existsByPhone(String sdt) throws IOException {
        for (StoredUser user : loadUsers()) {
            if (sdt != null && sdt.equals(user.sdt)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean existsByEmail(String email) throws IOException {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return false;
        }

        for (StoredUser user : loadUsers()) {
            if (normalizedEmail.equals(normalizeEmail(user.email))) {
                return true;
            }
        }
        return false;
    }

    public synchronized TaiKhoan login(String username, String password) throws IOException {
        StoredUser user = findStoredUser(username);
        if (user == null || !PasswordUtils.matches(password, user.password)) {
            return null;
        }
        return user.toTaiKhoan();
    }

    public synchronized TaiKhoan loginByPhone(String sdt, String password) throws IOException {
        StoredUser user = findStoredUserByPhone(sdt);
        if (user == null || !PasswordUtils.matches(password, user.password)) {
            return null;
        }
        return user.toTaiKhoan();
    }

    public synchronized TaiKhoan findByUsername(String username) throws IOException {
        StoredUser user = findStoredUser(username);
        return user != null ? user.toTaiKhoan() : null;
    }

    public synchronized TaiKhoan findByPhone(String sdt) throws IOException {
        StoredUser user = findStoredUserByPhone(sdt);
        return user != null ? user.toTaiKhoan() : null;
    }

    public synchronized TaiKhoan findByEmail(String email) throws IOException {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return null;
        }

        StoredUser user = findStoredUserByEmail(email);
        return user != null ? user.toTaiKhoan() : null;
    }

    public synchronized String generateNextMaKH() throws IOException {
        int max = 0;
        for (StoredUser user : loadUsers()) {
            if (user.maKH != null && user.maKH.matches("KH\\d+")) {
                int value = Integer.parseInt(user.maKH.substring(2));
                if (value > max) {
                    max = value;
                }
            }
        }
        return String.format("KH%02d", max + 1);
    }

    public synchronized void save(TaiKhoan tk, String hoTen, String sdt, String email, String diaChi) throws IOException {
        List<StoredUser> users = loadUsers();

        StoredUser next = new StoredUser();
        next.username = tk.getTenDangNhap();
        next.password = normalizePassword(tk.getMatKhau());
        next.role = tk.getVaiTro();
        next.maNV = tk.getMaNV();
        next.maKH = tk.getMaKH();
        next.trangThai = tk.getTrangThai() != null && tk.getTrangThai();
        next.hoTen = hoTen;
        next.sdt = sdt;
        next.email = email;
        next.diaChi = diaChi;

        boolean replaced = false;
        for (int i = 0; i < users.size(); i++) {
            StoredUser current = users.get(i);
            boolean sameUsername = safeEquals(current.username, next.username);
            boolean sameCustomerPhone = "KHACHHANG".equalsIgnoreCase(next.role)
                    && "KHACHHANG".equalsIgnoreCase(current.role)
                    && safeEquals(current.sdt, next.sdt);
            if (sameUsername || sameCustomerPhone) {
                users.set(i, next);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            users.add(next);
        }

        writeUsers(users);
    }

    public synchronized void deleteByUsername(String username) throws IOException {
        List<StoredUser> users = loadUsers();
        boolean changed = users.removeIf(user -> safeEquals(user.username, username));
        if (changed) {
            writeUsers(users);
        }
    }

    public synchronized boolean updatePasswordByPhone(String sdt, String newPassword) throws IOException {
        List<StoredUser> users = loadUsers();
        boolean updated = false;

        for (StoredUser user : users) {
            if (safeEquals(user.sdt, sdt)) {
                user.password = normalizePassword(newPassword);
                updated = true;
                break;
            }
        }

        if (updated) {
            writeUsers(users);
        }

        return updated;
    }

    public synchronized boolean updatePasswordByEmail(String email, String newPassword) throws IOException {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return false;
        }

        List<StoredUser> users = loadUsers();
        boolean updated = false;

        for (StoredUser user : users) {
            if (normalizedEmail.equals(normalizeEmail(user.email))) {
                user.password = normalizePassword(newPassword);
                updated = true;
                break;
            }
        }

        if (updated) {
            writeUsers(users);
        }

        return updated;
    }

    public synchronized boolean updatePasswordByUsername(String username, String newPassword) throws IOException {
        String normalizedUsername = normalizeUsername(username);
        if (normalizedUsername == null) {
            return false;
        }

        List<StoredUser> users = loadUsers();
        boolean updated = false;

        for (StoredUser user : users) {
            if (normalizedUsername.equals(normalizeUsername(user.username))) {
                user.password = normalizePassword(newPassword);
                updated = true;
                break;
            }
        }

        if (updated) {
            writeUsers(users);
        }

        return updated;
    }

    private void seedDefaultCustomer() throws IOException {
        if (existsByUsername("khachhang01")) {
            return;
        }

        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap("khachhang01");
        tk.setMatKhau(PasswordUtils.hash("123456"));
        tk.setVaiTro("KHACHHANG");
        tk.setMaKH("KH01");
        tk.setTrangThai(true);

        save(tk, "Khach hang 01", "0900000001", "khachhang01@gmail.com", "");
    }

    private void seedDefaultAdmin() throws IOException {
        if (existsByUsername("admin")) {
            return;
        }

        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap("admin");
        tk.setMatKhau(PasswordUtils.hash("123456"));
        tk.setVaiTro("Admin");
        tk.setMaNV("NV01");
        tk.setMaKH(null);
        tk.setTrangThai(true);

        save(tk, "Admin", "0900000000", "admin@cafe.local", "");
    }

    private void seedDefaultDelivery() throws IOException {
        if (existsByUsername("giaohang01")) {
            return;
        }

        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap("giaohang01");
        tk.setMatKhau(PasswordUtils.hash("123456"));
        tk.setVaiTro("GiaoHang");
        tk.setMaNV("NV02");
        tk.setMaKH(null);
        tk.setTrangThai(true);

        save(tk, "Giao hang 01", "0900000002", "giaohang01@cafe.local", "");
    }

    private StoredUser findStoredUser(String username) throws IOException {
        for (StoredUser user : loadUsers()) {
            if (safeEquals(user.username, username)) {
                return user;
            }
        }
        return null;
    }

    private StoredUser findStoredUserByPhone(String sdt) throws IOException {
        for (StoredUser user : loadUsers()) {
            if (safeEquals(user.sdt, sdt)) {
                return user;
            }
        }
        return null;
    }

    private StoredUser findStoredUserByEmail(String email) throws IOException {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return null;
        }

        for (StoredUser user : loadUsers()) {
            if (normalizedEmail.equals(normalizeEmail(user.email))) {
                return user;
            }
        }
        return null;
    }

    private List<StoredUser> loadUsers() throws IOException {
        Path path = getStorePath();
        List<StoredUser> users = new ArrayList<>();

        if (Files.notExists(path)) {
            return users;
        }

        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            if (line == null || line.trim().isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\\|", -1);
            if (parts.length < 10) {
                continue;
            }

            StoredUser user = new StoredUser();
            user.username = dec(parts[0]);
            user.password = dec(parts[1]);
            user.role = dec(parts[2]);
            user.maNV = dec(parts[3]);
            user.maKH = dec(parts[4]);
            user.trangThai = Boolean.parseBoolean(dec(parts[5]));
            user.hoTen = dec(parts[6]);
            user.sdt = dec(parts[7]);
            user.email = dec(parts[8]);
            user.diaChi = dec(parts[9]);
            users.add(user);
        }

        return users;
    }

    private void writeUsers(List<StoredUser> users) throws IOException {
        Path path = getStorePath();
        List<String> lines = new ArrayList<>();
        lines.add("# username|password|role|maNV|maKH|trangThai|hoTen|sdt|email|diaChi");
        for (StoredUser user : users) {
            lines.add(enc(user.username) + "|" +
                    enc(user.password) + "|" +
                    enc(user.role) + "|" +
                    enc(user.maNV) + "|" +
                    enc(user.maKH) + "|" +
                    enc(Boolean.toString(user.trangThai)) + "|" +
                    enc(user.hoTen) + "|" +
                    enc(user.sdt) + "|" +
                    enc(user.email) + "|" +
                    enc(user.diaChi));
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    private Path getStorePath() throws IOException {
        String configuredDir = firstNonBlank(
                System.getProperty(STORE_DIR_PROPERTY),
                System.getenv(STORE_DIR_ENV)
        );

        Path dir = configuredDir != null
                ? Paths.get(configuredDir)
                : Paths.get(System.getProperty("user.home"), STORE_DIR);
        if (Files.notExists(dir)) {
            Files.createDirectories(dir);
        }
        return dir.resolve(STORE_FILE);
    }

    private String enc(String value) {
        if (value == null) {
            value = "";
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String dec(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private boolean safeEquals(String a, String b) {
        return a == null ? b == null || b.isEmpty() : a.equals(b);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }

        return null;
    }

    private String normalizePassword(String password) {
        if (password == null || PasswordUtils.isHashed(password)) {
            return password;
        }
        return PasswordUtils.hash(password);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        String trimmed = email.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            return null;
        }

        String trimmed = username.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }

    private static class StoredUser {
        String username;
        String password;
        String role;
        String maNV;
        String maKH;
        boolean trangThai;
        String hoTen;
        String sdt;
        String email;
        String diaChi;

        TaiKhoan toTaiKhoan() {
            TaiKhoan tk = new TaiKhoan();
            tk.setTenDangNhap(username);
            tk.setMatKhau(password);
            tk.setVaiTro(role);
            tk.setMaNV(maNV != null && !maNV.isEmpty() ? maNV : null);
            tk.setMaKH(maKH != null && !maKH.isEmpty() ? maKH : null);
            tk.setTrangThai(trangThai);
            return tk;
        }
    }
}
