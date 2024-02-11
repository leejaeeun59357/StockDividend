package org.example.stockdividend.web;

import lombok.AllArgsConstructor;
import org.example.stockdividend.model.ScrapedResult;
import org.example.stockdividend.service.FinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/finance")
@AllArgsConstructor
public class FinanceController {
    private final FinanceService financeService;
    @GetMapping("/dividend/{companyName}")
    public ResponseEntity<?> searchFinance(@PathVariable String companyName) {

        ScrapedResult result = this.financeService.getDividendByCompanyName(companyName);

        return ResponseEntity.ok(result);
    }
}
