package org.example.stockdividend;

import org.example.stockdividend.model.Company;
import org.example.stockdividend.model.ScrapedResult;
import org.example.stockdividend.scraper.Scraper;
import org.example.stockdividend.scraper.YahooFinanceScraper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class StockDividendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockDividendApplication.class, args);
    }
}
