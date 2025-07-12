package com.joopro.Joosik_Pro.service.FirstComeEventService;

import java.util.List;

public interface FirstComeEventService {
    boolean tryParticipate(Long stockId, Long memberId);

    List<Long> getParticipants(Long stockId);

    boolean hasParticipated(Long stockId, Long memberId);

    int getCurrentCount(Long stockId);
}

