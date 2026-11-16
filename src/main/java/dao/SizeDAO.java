package dao;

import entity.Size;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import utils.JPAUtil;

import java.util.List;

public class SizeDAO {

    private final EntityManagerFactory emf = JPAUtil.EMF;

    public List<Size> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT s FROM Size s ORDER BY s.maSize",
                    Size.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public Size findById(String maSize) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Size.class, maSize);
        } finally {
            em.close();
        }
    }
}
