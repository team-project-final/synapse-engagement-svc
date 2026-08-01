package com.synapse.engagement.community.repository;

import com.synapse.engagement.community.domain.ContentType;
import com.synapse.engagement.community.domain.SharedContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SharedContentRepository extends JpaRepository<SharedContent, Long> {
    Optional<SharedContent> findByShareTokenAndDeletedAtIsNull(String shareToken);

    Optional<SharedContent> findByIdAndDeletedAtIsNull(Long id);

    Optional<SharedContent> findByIdAndContentTypeAndDeletedAtIsNull(Long id, ContentType contentType);

    // 공개 검색은 익명 접근이 가능하므로(SecurityConfig permitAll) 그룹 전용 공유를 절대 노출하지 않는다.
    @Query("""
            select c from SharedContent c
            where c.deletedAt is null
              and c.groupId is null
              and (:contentType is null or c.contentType = :contentType)
              and (
                :keyword is null
                or lower(c.title) like lower(concat('%', :keyword, '%'))
                or lower(c.description) like lower(concat('%', :keyword, '%'))
                or lower(c.tags) like lower(concat('%', :keyword, '%'))
              )
            order by c.createdAt desc
            """)
    List<SharedContent> search(@Param("keyword") String keyword, @Param("contentType") ContentType contentType);

    @Query("""
            select c from SharedContent c
            where c.deletedAt is null
              and c.groupId = :groupId
              and (:contentType is null or c.contentType = :contentType)
              and (
                :keyword is null
                or lower(c.title) like lower(concat('%', :keyword, '%'))
                or lower(c.description) like lower(concat('%', :keyword, '%'))
                or lower(c.tags) like lower(concat('%', :keyword, '%'))
              )
            order by c.createdAt desc
            """)
    List<SharedContent> searchByGroupId(
            @Param("groupId") Long groupId,
            @Param("keyword") String keyword,
            @Param("contentType") ContentType contentType
    );
}
