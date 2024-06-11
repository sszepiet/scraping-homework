package co.cosmose.scraping_homework.content;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublisherContentController {

    private final PublisherContentService service;

    public PublisherContentController(PublisherContentService service) {
        this.service = service;
    }

    @GetMapping("/scrape")
    public String scrape(@RequestParam String url) {
        try {
            service.scrapAndStore(url);
            return "Scraping completed.";
        } catch (Exception e) {
            return "Scraping failed: " + e.getMessage();
        }
    }
}
