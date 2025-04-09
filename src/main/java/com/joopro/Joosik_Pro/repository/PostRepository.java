package com.joopro.Joosik_Pro.repository;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.domain.Post.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Post.VsStockPost;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepository {

    private final EntityManager em;

    // Post 저장
    public void save(Post post) {
        if (post.getId() == null) {
            em.persist(post);
        } else {
            em.merge(post);
        }
    }

    // Post id로 찾기
    public Post findById(Long id) {
        return em.find(Post.class, id);
    }

    // Post id로 지우기
    public void deleteById(Long id) {
        Post post = findById(id);
        if (post != null) {
            em.remove(post);
        }
    }

    // Post 모두 찾기
    public List<Post> findAll() {
        return em.createQuery("SELECT p FROM Post p", Post.class).getResultList();
    }

    public List<SingleStockPost> findAllSingleStockPost(){
        return em.createQuery("SELECT s FROM SingleStockPost  s", SingleStockPost.class).getResultList();
    }

    public List<VsStockPost> findAllVsStockPost(){
        return em.createQuery("SELECT v FROM VsStockPost  v", VsStockPost.class).getResultList();
    }


    // singleStockPost ID로 찾기
    public List<SingleStockPost> findSingleStockPostByStockId(Long stockId) {
        return em.createQuery(
                        "SELECT s FROM SingleStockPost s WHERE s.stock.id = :stockId", SingleStockPost.class)
                .setParameter("stockId", stockId)
                .getResultList();
    }

    // VsStockPost ID로 찾기
    public List<VsStockPost> findVsStockPostByStockIds(Long stockId1, Long stockId2) {
        return em.createQuery(
                        "SELECT v FROM VsStockPost v " +
                                "WHERE (v.stock1.id = :stockId1 AND v.stock2.id = :stockId2) " +
                                "   OR (v.stock1.id = :stockId2 AND v.stock2.id = :stockId1)", VsStockPost.class)
                .setParameter("stockId1", stockId1)
                .setParameter("stockId2", stockId2)
                .getResultList();
    }

    // 모든 Post 중 Keyword로 찾기
    public List<Post> findBySimilarContent(String keyword) {
        return em.createQuery(
                        "SELECT p FROM Post p WHERE p.content LIKE :keyword", Post.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
    }

    // SingleStockPost keyword로 찾기
    public List<SingleStockPost> findSingleStockPostBySimilarContent(String keyword) {
        return em.createQuery(
                        "SELECT s FROM SingleStockPost s WHERE s.content LIKE :keyword", SingleStockPost.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
    }

    // VsStockPost keyword로 찾기
    public List<VsStockPost> findVsStockPostBySimilarContent(String keyword) {
        return em.createQuery(
                        "SELECT v FROM VsStockPost v WHERE v.content LIKE :keyword", VsStockPost.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
    }

    // 모든 Post 중 TOP 100 찾기
    public List<Post> getPopularArticles() {
        return em.createQuery(
                        "SELECT p FROM Post p ORDER BY p.viewCount DESC", Post.class)
                .setMaxResults(100)
                .getResultList();
    }

    // SingleStockPost 중 TOP 10 찾기
    public List<SingleStockPost> getPopularSingleStockPosts() {
        return em.createQuery(
                        "SELECT s FROM SingleStockPost s ORDER BY s.viewCount DESC", SingleStockPost.class)
                .setMaxResults(10)
                .getResultList();
    }

    // VsStockPost 중 TOP 10 찾기
    public List<VsStockPost> getPopularVsStockPosts() {
        return em.createQuery(
                        "SELECT v FROM VsStockPost v ORDER BY v.viewCount DESC", VsStockPost.class)
                .setMaxResults(10)
                .getResultList();
    }

}
