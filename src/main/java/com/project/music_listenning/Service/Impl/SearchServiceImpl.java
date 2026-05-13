package com.project.music_listenning.Service.Impl;

import com.project.music_listenning.Entity.Genre;
import com.project.music_listenning.Repository.AlbumRepository;
import com.project.music_listenning.Repository.ArtistRepository;
import com.project.music_listenning.Repository.GenreRepository;
import com.project.music_listenning.Repository.SongRepository;
import com.project.music_listenning.Service.SearchService;
import com.project.music_listenning.dto.response.SearchDto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;

    /**
     * Search tổng hợp — tìm songs + artists + albums cùng lúc.
     * Mỗi loại trả tối đa 10 kết quả để trang kết quả không quá dài.
     */
    @Override
    public SearchResponse search(String keyword, SearchFilter filter) {
        String kw = keyword.trim();
        if (kw.isEmpty()) return empty(keyword);

        String  genre   = filter != null ? filter.genre()   : null;
        Boolean premium = filter != null ? filter.premium() : null;
        String  type    = filter != null ? filter.type()    : "all";

        List<SongResult>   songs   = List.of();
        List<ArtistResult> artists = List.of();
        List<AlbumResult>  albums  = List.of();

        // Chỉ tìm loại được chọn — tránh query thừa
        if ("all".equals(type) || "song".equals(type)) {
            songs = songRepository
                    .searchWithFilter(kw, genre, premium, 10, 0)
                    .stream().map(SongResult::from).toList();
        }

        if ("all".equals(type) || "artist".equals(type)) {
            artists = artistRepository
                    .searchByName(kw, PageRequest.of(0, 8))
                    .stream().map(ArtistResult::from).toList();
        }

        if ("all".equals(type) || "album".equals(type)) {
            albums = albumRepository
                    .searchByTitleOrArtist(kw, PageRequest.of(0, 8))
                    .stream().map(AlbumResult::from).toList();
        }

        int total = songs.size() + artists.size() + albums.size();
        return new SearchResponse(songs, artists, albums, keyword, total);

    }

    /**
     * Suggestions — gọi khi user đang gõ, cần nhanh.
     * Trả tối đa 8 gợi ý, mix songs + artists.
     */
    @Override
    public SuggestionsResponse suggest(String keyword) {
        String kw = keyword.trim();
        if (kw.length() < 2) return new SuggestionsResponse(List.of());

        List<Suggestion> suggestions = new ArrayList<>();

        // Artists trước — thường match rõ hơn
        artistRepository.searchByName(kw, PageRequest.of(0, 3))
                .forEach(a -> suggestions.add(new Suggestion(
                        "artist", a.getId(), a.getName(), "Nghệ sĩ")));

        // Songs
        songRepository.findSuggestionsForSongs(kw, PageRequest.of(0, 5))
                .forEach(s -> suggestions.add(new Suggestion(
                        "song", s.getId(), s.getTitle(), s.getArtist().getName())));

        return new SuggestionsResponse(suggestions);
    }

    /** Lấy tất cả genres để hiện filter chips */
    @Override
    public List<String> getGenres() {
        return genreRepository.findAllByOrderByNameAsc()
                .stream().map(Genre::getName).toList();
    }

    private SearchResponse empty(String keyword) {
        return new SearchResponse(List.of(), List.of(), List.of(), keyword, 0);
    }
}
