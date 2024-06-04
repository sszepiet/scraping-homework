package co.cosmose.scraping_homework.content;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublisherContent {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();
    private String articleUrl;
    private String title;
    private String author;
    private String htmlContent;
    private String originalContent;
    private String mainImageUrl;
}
