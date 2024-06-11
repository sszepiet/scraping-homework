package co.cosmose.scraping_homework.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PublisherContentRepository extends JpaRepository<PublisherContent, UUID> {
    boolean existsByArticleUrl(String articleUrl);

}
