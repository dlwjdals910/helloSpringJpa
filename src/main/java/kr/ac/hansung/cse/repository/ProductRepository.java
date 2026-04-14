package kr.ac.hansung.cse.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import kr.ac.hansung.cse.model.Product;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Product> findAll() {
        TypedQuery<Product> query = entityManager
                .createQuery("SELECT p FROM Product p LEFT JOIN FETCH p.category ORDER BY p.id ASC", Product.class);
        return query.getResultList();
    }

    public Optional<Product> findById(Long id) {
        List<Product> result = entityManager
                .createQuery("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :id", Product.class)
                .setParameter("id", id)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public Product save(Product product) {
        entityManager.persist(product);
        return product;
    }

    public Product update(Product product) {
        return entityManager.merge(product);
    }

    public void delete(Long id) {
        Product product = entityManager.find(Product.class, id);
        if (product != null) {
            entityManager.remove(product);
        }
    }

    // 상품명 키워드 부분 검색 (JPQL LIKE)
    public List<Product> findByNameContaining(String keyword) {
        return entityManager.createQuery(
                        "SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.name LIKE :keyword ORDER BY p.id ASC",
                        Product.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
    }

    // 카테고리 ID로 상품 필터링
    public List<Product> findByCategoryId(Long categoryId) {
        return entityManager.createQuery(
                        "SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.id = :cid ORDER BY p.id ASC",
                        Product.class)
                .setParameter("cid", categoryId)
                .getResultList();
    }

    // 키워드 + 카테고리 동시 필터링
    public List<Product> findByNameContainingAndCategoryId(String keyword, Long categoryId) {
        return entityManager.createQuery(
                        "SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.name LIKE :keyword AND p.category.id = :cid ORDER BY p.id ASC",
                        Product.class)
                .setParameter("keyword", "%" + keyword + "%")
                .setParameter("cid", categoryId)
                .getResultList();
    }
}
