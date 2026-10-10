package com.myopty.workflow.controller;

import com.myopty.workflow.model.Dealer;
import com.myopty.workflow.service.DealerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dealers")
public class DealerController {

    private final DealerService dealerService;

    public DealerController(DealerService dealerService) {
        this.dealerService = dealerService;
    }

    @PostMapping
    public Dealer createDealer(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String itemType) {
        return dealerService.createDealer(name, email, itemType);
    }

    @GetMapping
    public List<Dealer> getAllDealers() {
        return dealerService.getAllDealers();
    }

    @GetMapping("/active")
    public List<Dealer> getActiveDealers() {
        return dealerService.getActiveDealers();
    }

    @GetMapping("/{email}")
    public Dealer getDealerByEmail(@PathVariable String email) {
        return dealerService.getDealerByEmail(email).orElseThrow();
    }

    @PutMapping("/{dealerId}")
    public Dealer updateDealer(@PathVariable Integer dealerId,
                               @RequestParam(required = false) String name,
                               @RequestParam(required = false) String email,
                               @RequestParam(required = false) String itemType,
                               @RequestParam(required = false) boolean active) {
        return dealerService.updateDealer(dealerId, name, email, itemType, active);
    }

    @DeleteMapping("/{dealerId}")
    public void deleteDealer(@PathVariable Integer dealerId) {
        dealerService.deleteDealer(dealerId);
    }
}