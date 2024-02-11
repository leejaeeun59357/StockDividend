package org.example.stockdividend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockdividend.exception.impl.NoCompanyException;
import org.example.stockdividend.model.Company;
import org.example.stockdividend.model.Dividend;
import org.example.stockdividend.model.ScrapedResult;
import org.example.stockdividend.model.constants.CacheKey;
import org.example.stockdividend.persist.CompanyRepository;
import org.example.stockdividend.persist.DividendRepository;
import org.example.stockdividend.persist.entity.CompanyEntity;
import org.example.stockdividend.persist.entity.DividendEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;


    @Cacheable(key = "#companyName" ,value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search company -> " + companyName);

        // 1. 회사명 기준으로 회사 정보를 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new NoCompanyException());

        // 2. 조회된 회사 ID로 배당금 정보 조회
        List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(company.getId());

        // 3. 결과 조합 후 반환

        List<Dividend> dividends =
                dividendEntities.stream()
                        .map(e -> new Dividend(e.getDate(), e.getDividend()))
                        .collect(Collectors.toList());

        return new ScrapedResult(
                new Company(company.getTicker(),company.getName()), dividends);
    }
}
