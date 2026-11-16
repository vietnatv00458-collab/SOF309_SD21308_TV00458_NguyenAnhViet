package dao;

import entity.ChiTietHoaDon;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import utils.JPAUtil;

import java.util.List;

public class ChiTietHoaDonDAO {

    private final EntityManagerFactory emf = JPAUtil.EMF;

    public List<ChiTietHoaDon> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM ChiTietHoaDon c ORDER BY c.maCTHD",
                    ChiTietHoaDon.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public ChiTietHoaDon findById(Integer maCTHD) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(ChiTietHoaDon.class, maCTHD);
        } finally {
            em.close();
        }
    }

    public boolean existsById(Integer maCTHD) {
        return findById(maCTHD) != null;
    }

    public void create(ChiTietHoaDon ct) {
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

    public void update(ChiTietHoaDon ct) {
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

    public void delete(Integer maCTHD) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            ChiTietHoaDon ct = em.find(ChiTietHoaDon.class, maCTHD);
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
}