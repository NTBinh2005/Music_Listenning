package com.project.music_listenning.Service;

import com.project.music_listenning.dto.response.PlayHistoryDto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface PlayHistoryService {
    void record(RecordPlayRequest request);
    List<PlayHistoryResponse> getHistory(int limit);
    List<PlayHistoryResponse> getRecentlyPlayed(int limit);
    void clearAll();
    void removeSong(UUID songId);
}
