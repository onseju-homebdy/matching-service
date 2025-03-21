package com.onseju.matchingservice;

import com.onseju.matchingservice.domain.TradeOrder;
import com.onseju.matchingservice.engine.MatchingEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TempController {

    private final MatchingEngine matchingEngine;

    @PostMapping("/matching")
    public ResponseEntity<Void> received(
            @RequestBody final TradeOrder order
    ) {
        matchingEngine.processOrder(order);
        return ResponseEntity.ok().build();
    }
}
