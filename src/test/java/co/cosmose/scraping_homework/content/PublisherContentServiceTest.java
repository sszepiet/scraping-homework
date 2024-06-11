package co.cosmose.scraping_homework.content;

import co.cosmose.scraping_homework.utils.UrlConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PublisherContentServiceTest {


    @Mock
    private PublisherContentRepository repository;

    @Mock
    private UrlConnectionFactory urlConnectionFactory;

    @InjectMocks
    private PublisherContentService service;

    @BeforeEach
    public void setUp() {
        service = new PublisherContentService(repository, urlConnectionFactory);
    }

    @Test
    public void shouldScrapAndStoreFromLocalFile1() throws Exception {
        // given
        String rssContent = """
                <rss version="2.0">
                  <channel>
                    <title>Sample RSS Feed</title>
                    <link>http://www.example.com/</link>
                    <description>This is an example RSS feed</description>
                    <item>
                      <title>Example entry</title>
                      <link>http://www.example.com/entry1</link>
                      <description>This is an example entry with <a href="http://link.com">link</a>.</description>
                      <author>John Doe</author>
                      <enclosure url="http://www.example.com/image1.jpg" type="image/jpeg" />
                    </item>
                  </channel>
                </rss>
                """;
        mockHttpUrlConnection("http://test.com/rss1", rssContent);

        // when
        service.scrapAndStore("http://test.com/rss1");

        // then
        verify(repository, times(1)).save(any(PublisherContent.class));
        verify(repository).save(argThat(content -> {
            assertEquals("Example entry", content.getTitle());
            assertEquals("John Doe", content.getAuthor());
            assertEquals("This is an example entry with link.", content.getHtmlContent());
            assertEquals("http://www.example.com/entry1", content.getArticleUrl());
            assertEquals("http://www.example.com/image1.jpg", content.getMainImageUrl());
            return true;
        }));
    }

    @Test
    public void shouldNotSaveArticleIfExists() throws Exception {
        // given
        String rssContent = """
                <rss version="2.0">
                  <channel>
                    <title>Sample RSS Feed</title>
                    <link>http://www.example.com/</link>
                    <description>This is an example RSS feed</description>
                    <item>
                      <title>Example entry</title>
                      <link>http://www.example.com/entry1</link>
                      <description>This is an example entry</description>
                      <author>John Doe</author>
                      <enclosure url="http://www.example.com/image1.jpg" type="image/jpeg" />
                    </item>
                  </channel>
                </rss>
                """;
        mockHttpUrlConnection("http://test.com/rss1", rssContent);
        when(repository.existsByArticleUrl("http://www.example.com/entry1")).thenReturn(true);

        // when
        service.scrapAndStore("http://test.com/rss1");

        // then
        verify(repository, never()).save(any(PublisherContent.class));
    }

    @Test
    public void shouldCleanHtmlContent() throws Exception {
        // given
        String rssContent = """
                <rss version="2.0">
                  <channel>
                    <title>Sample RSS Feed</title>
                    <link>http://www.example.com/</link>
                    <description>This is an example RSS feed</description>
                    <item>
                      <title>Example entry</title>
                      <link>http://www.example.com/entry1</link>
                      <description>This is an example entry with <a href="http://link.com">link</a> and <strong>bold text</strong>.</description>
                      <author>John Doe</author>
                      <enclosure url="http://www.example.com/image1.jpg" type="image/jpeg" />
                    </item>
                  </channel>
                </rss>
                """;
        mockHttpUrlConnection("http://test.com/rss1", rssContent);

        // when
        service.scrapAndStore("http://test.com/rss1");

        // then
        verify(repository).save(argThat(content -> {
            assertEquals("Example entry", content.getTitle());
            assertEquals("John Doe", content.getAuthor());
            assertEquals("This is an example entry with link and bold text.", content.getHtmlContent());
            assertEquals("http://www.example.com/entry1", content.getArticleUrl());
            assertEquals("http://www.example.com/image1.jpg", content.getMainImageUrl());
            return true;
        }));
    }

    @Test
    public void shouldLogErrorForInvalidUrl() {
        // given
        String invalidUrl = "invalid url";

        // when
        service.scrapAndStore(invalidUrl);

        // then
        verifyNoInteractions(repository);
    }

    private void mockHttpUrlConnection(String sourceUrl, String content) throws URISyntaxException, IOException {
        URI uri = new URI(sourceUrl);
        URL url = uri.toURL();
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        when(urlConnectionFactory.openStream(url)).thenReturn(inputStream);
    }
}
