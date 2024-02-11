package org.example.stockdividend.scraper;

import org.example.stockdividend.model.Company;
import org.example.stockdividend.model.ScrapedResult;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);
    ScrapedResult scrap(Company company);
}
