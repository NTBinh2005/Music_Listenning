package com.project.music_listenning.Service;

import com.project.music_listenning.dto.response.SearchDto.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SearchService {
    SearchResponse search(String keyword, SearchFilter filter);
    SuggestionsResponse suggest(String keyword);
    List<String> getGenres();
}
