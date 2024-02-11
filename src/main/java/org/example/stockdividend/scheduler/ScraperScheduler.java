package org.example.stockdividend.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockdividend.model.Company;
import org.example.stockdividend.model.ScrapedResult;
import org.example.stockdividend.model.constants.CacheKey;
import org.example.stockdividend.persist.CompanyRepository;
import org.example.stockdividend.persist.DividendRepository;
import org.example.stockdividend.persist.entity.CompanyEntity;
import org.example.stockdividend.persist.entity.DividendEntity;
import org.example.stockdividend.scraper.Scraper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@EnableCaching
@Component
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final Scraper yahooFinanaceScraper;
    private final DividendRepository dividendRepository;

    // 일정 주기마다 수행
    @CacheEvict(value = CacheKey.KEY_FINANCE,allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {

        log.info("scraping scheduler is started");

        // 저장된 회사 목록 조회
        List<CompanyEntity> companies = this.companyRepository.findAll();

        // 회사마다 배당금 정보를 새로 스크래핑
        for (var company : companies) {
            log.info("scraping scheduler is started -> " + company.getName());

            ScrapedResult scrapedResult = this.yahooFinanaceScraper.scrap(
                    new Company(company.getTicker(), company.getName()));

            // 스크래핑한 배당금 정보 중 데이터베이스에 없는 값은 저장
            scrapedResult.getDividends().stream()
                    // Dividend 모델을 Dividend Entitiy로 매핑
                    .map(e -> new DividendEntity(company.getId(), e))

                    // Element 하나씩 존재하지 않는 Element 만 Dividend Repository에 삽입
                    .forEach(e -> {
                        boolean exists =
                                this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());

                        if (!exists) {
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> " + e.toString());
                        }
                    });

            // 연속적으로 스크래핑 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000);  // 3 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }
}
