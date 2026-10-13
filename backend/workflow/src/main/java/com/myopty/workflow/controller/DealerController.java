package com.myopty.workflow.controller;

import com.myopty.workflow.model.Dealer;
import com.myopty.workflow.service.DealerService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dealers")
public class DealerController {

    private final DealerService dealerService;

    public DealerController(DealerService dealerService) {
        this.dealerService = dealerService;
    }

    // Service එකේ no arguments නම් මෙතැනත් එහෙමමයි
    @GetMapping
    public List<Dealer> getAllDealers() {
        return dealerService.getAllDealers();
    }

    // Service එකේ parameters වලට අනුව (උදාහරණයක් ලෙස String 3ක් ගනී නම්)
    @PostMapping
    public ResponseEntity<?> createDealer(@RequestBody Dealer dealer) {
        Dealer created = dealerService.createDealer(dealer.getName(), dealer.getEmail(), dealer.getType());
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDealer(@PathVariable Integer id) {
        dealerService.deleteDealer(id);
        return ResponseEntity.ok().build();
    }
}
