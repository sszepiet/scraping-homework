package co.cosmose.scraping_homework.content;

import co.cosmose.scraping_homework.utils.UrlConnectionFactory;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Service
public class PublisherContentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublisherContentService.class);
    private final PublisherContentRepository repository;
    private final UrlConnectionFactory urlConnectionFactory;

    public PublisherContentService(PublisherContentRepository repository, UrlConnectionFactory urlConnectionFactory) {
        this.repository = repository;
        this.urlConnectionFactory = urlConnectionFactory;
    }

    public void scrapAndStore(String rssFeedUrl) {
        try {
            URI uri = new URI(rssFeedUrl);
            URL feedSource = uri.toURL();
            InputStream is = urlConnectionFactory.openStream(feedSource);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(is));

            for (SyndEntry entry : feed.getEntries()) {
                if (!repository.existsByArticleUrl(entry.getLink())) {
                    String cleanedHtmlContent = cleanContent(entry.getDescription().getValue());
                    PublisherContent content = PublisherContent.builder()
                            .articleUrl(entry.getLink())
                            .title(entry.getTitle())
                            .author(entry.getAuthor())
                            .htmlContent(cleanedHtmlContent)
                            .originalContent(entry.getDescription().getValue())
                            .mainImageUrl(entry.getEnclosures().isEmpty() ? null : entry.getEnclosures().getFirst().getUrl())
                            .build();
                    repository.save(content);
                }
            }
        } catch (URISyntaxException e) {
            LOGGER.error("Invalid URI syntax: {}", rssFeedUrl, e);
        } catch (MalformedURLException e) {
            LOGGER.error("Invalid URL: {}", rssFeedUrl, e);
        } catch (FeedException e) {
            LOGGER.error("Error parsing feed from URL: {}", rssFeedUrl, e);
        } catch (IOException e) {
            LOGGER.error("Error reading from URL: {}", rssFeedUrl, e);
        }
    }

    private String cleanContent(String htmlContent) {
        return Jsoup.parse(htmlContent).text();
    }

}
