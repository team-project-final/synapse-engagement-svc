package com.synapse.engagement.community.repository;

import com.synapse.engagement.community.entity.ContentType;
import com.synapse.engagement.community.entity.SharedContent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SharedContentRepository extends JpaRepository<SharedContent, UUID> {

    // shareToken은 공개 링크에 들어가는 값이므로 중복되면 안 됩니다.
    boolean existsByShareToken(String shareToken);

    // 공개 토큰 조회에서는 삭제되지 않은 공유 콘텐츠만 찾습니다.
    Optional<SharedContent> findByShareTokenAndDeletedAtIsNull(String shareToken);

    // 삭제 API에서는 id로 찾되, 이미 soft delete 된 row는 없는 것처럼 취급합니다.
    Optional<SharedContent> findByIdAndDeletedAtIsNull(UUID id);

    @Query("""
            select content
            from SharedContent content
            where content.deletedAt is null
              and (:contentType is null or content.contentType = :contentType)
              and (
                    :keyword is null
                    or lower(content.title) like lower(concat('%', :keyword, '%'))
                    or lower(content.description) like lower(concat('%', :keyword, '%'))
                    or lower(content.tags) like lower(concat('%', :keyword, '%'))
              )
            order by content.createdAt desc, content.id desc
            """)
    /*
     * 검색 조건은 모두 선택 사항입니다.
     * - keyword가 null이면 키워드 필터를 건너뜁니다.
     * - contentType이 null이면 DECK/NOTE 전체를 조회합니다.
     */
    List<SharedContent> search(
            @Param("keyword") String keyword,
            @Param("contentType") ContentType contentType,
            Pageable pageable);
}

