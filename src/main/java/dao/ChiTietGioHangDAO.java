package dao;

import entity.ChiTietGioHang;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import utils.JPAUtil;

import java.util.List;

public class ChiTietGioHangDAO {

    private final EntityManagerFactory emf = JPAUtil.EMF;

    public List<ChiTietGioHang> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM ChiTietGioHang c ORDER BY c.maCTGH",
                    ChiTietGioHang.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public ChiTietGioHang findById(Integer maCTGH) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(ChiTietGioHang.class, maCTGH);
        } finally {
            em.close();
        }
    }

    public boolean existsById(Integer maCTGH) {
        return findById(maCTGH) != null;
    }

    public void create(ChiTietGioHang ct) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(ct);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void update(ChiTietGioHang ct) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(ct);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public int deleteByMaDatHang(EntityManager em, String maDatHang) {
        String normalizedMaDatHang = trimToNull(maDatHang);
        if (em == null || normalizedMaDatHang == null) {
            return 0;
        }

        return em.createQuery(
                        "DELETE FROM ChiTietGioHang c WHERE c.maDatHang = :maDatHang"
                )
                .setParameter("maDatHang", normalizedMaDatHang)
                .executeUpdate();
    }

    public int deleteByMaDatHang(String maDatHang) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            int deleted = deleteByMaDatHang(em, maDatHang);
            em.getTransaction().commit();
            return deleted;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Integer maCTGH) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            ChiTietGioHang ct = em.find(ChiTietGioHang.class, maCTGH);
            if (ct != null) {
                em.remove(ct);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
