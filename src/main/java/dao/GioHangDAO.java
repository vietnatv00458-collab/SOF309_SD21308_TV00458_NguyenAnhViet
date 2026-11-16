package dao;

import entity.ChiTietGioHang;
import entity.GioHang;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import utils.JPAUtil;

import java.util.List;

public class GioHangDAO {

    private final EntityManagerFactory emf = JPAUtil.EMF;

    public List<GioHang> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT g FROM GioHang g ORDER BY g.maGioHang",
                    GioHang.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public GioHang findById(String maGioHang) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(GioHang.class, maGioHang);
        } finally {
            em.close();
        }
    }

    public boolean existsById(String maGioHang) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(GioHang.class, maGioHang) != null;
        } finally {
            em.close();
        }
    }

    public void create(GioHang gioHang) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(gioHang);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void update(GioHang gioHang) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(gioHang);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(String maGioHang) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            GioHang gh = em.find(GioHang.class, maGioHang);
            if (gh != null) {
                em.createQuery("DELETE FROM ChiTietGioHang c WHERE c.maGioHang = :maGioHang")
                        .setParameter("maGioHang", gh.getMaGioHang())
                        .executeUpdate();
                em.remove(gh);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
