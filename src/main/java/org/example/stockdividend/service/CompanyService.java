package org.example.stockdividend.service;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.Trie;
import org.example.stockdividend.exception.impl.NoCompanyException;
import org.example.stockdividend.model.Company;
import org.example.stockdividend.model.ScrapedResult;
import org.example.stockdividend.persist.CompanyRepository;
import org.example.stockdividend.persist.DividendRepository;
import org.example.stockdividend.persist.entity.CompanyEntity;
import org.example.stockdividend.persist.entity.DividendEntity;
import org.example.stockdividend.scraper.Scraper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompanyService {

    private final Trie trie;

    private final Scraper yahooFinanaceScraper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {

        boolean exists = this.companyRepository.existsByTicker(ticker);
        if(exists) {
            throw new RuntimeException("Already exists ticker -> " + ticker);
        }

        return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable) {
        return this.companyRepository.findAll(pageable);
    }

    private Company storeCompanyAndDividend(String ticker) {

        // ticker 를 기준으로 회사 스크래핑
        Company company = this.yahooFinanaceScraper.scrapCompanyByTicker(ticker);
        if(ObjectUtils.isEmpty(company)) {
            throw new RuntimeException("Failed to scrap ticker -> " + ticker);
        }

        // 해당 회사가 존재할 경우, 회사의 배당금 정보 스크래핑
        ScrapedResult scrapedResult = this.yahooFinanaceScraper.scrap(company);

        // 스크래핑 결과
        CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
        List<DividendEntity> dividendEntities =
                scrapedResult.getDividends().stream()
                                .map(e -> new DividendEntity(companyEntity.getId(), e))
                                .collect(Collectors.toList());

        this.dividendRepository.saveAll(dividendEntities);
        return company;
    }

    public List<String> getCompanyNamesByKeyword(String keyword) {
        Pageable limit = PageRequest.of(0,10);
        Page<CompanyEntity> companyEntities =
                this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);

        return companyEntities.stream()
                .map(e -> e.getName())
                .collect(Collectors.toList());
    }

    public void addAutocompleteKeyword(String keyword) {
        this.trie.put(keyword,null);
    }

    public List<String> autocomplete(String keyword) {
        return (List<String>) this.trie.prefixMap(keyword).keySet()
                .stream()
                .collect(Collectors.toList());
    }

    public void deleteAutocompleteKeyword(String keyword){
        this.trie.remove(keyword);
    }

    public String deleteCompany(String ticker) {
        var company = this.companyRepository.findByTicker(ticker)
                .orElseThrow(() -> new NoCompanyException());

        this.dividendRepository.deleteAllByCompanyId(company.getId());
        this.companyRepository.delete(company);

        this.deleteAutocompleteKeyword(company.getName());
        return company.getName();
    }
}
