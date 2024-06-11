package co.cosmose.scraping_homework.utils;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Component
public class UrlConnectionFactory {

    public InputStream openStream(URL url) throws IOException {
        return url.openStream();
    }
}
