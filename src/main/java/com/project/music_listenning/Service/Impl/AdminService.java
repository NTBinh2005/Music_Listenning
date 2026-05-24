package com.project.music_listenning.Service.Impl;

import com.project.music_listenning.Entity.*;
import com.project.music_listenning.Repository.*;
import com.project.music_listenning.dto.AdminDto.*;
import com.project.music_listenning.dto.response.AlbumDto.AlbumResponse;
import com.project.music_listenning.dto.response.ArtistDto.ArtistResponse;
import com.project.music_listenning.dto.response.SongDTO.SongResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository         userRepository;
    private final SongRepository         songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository        albumRepository;
    private final PlayHistoryRepository  historyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder        passwordEncoder;

    // ── Dashboard Stats ───────────────────────────────────────────────────────

    public DashboardStats getDashboardStats() {
        long totalUsers   = userRepository.count();
        long totalSongs   = songRepository.count();
        long totalArtists = artistRepository.count();
        long totalAlbums  = albumRepository.count();
        long totalPlays   = historyRepository.count();
        long premiumUsers = subscriptionRepository.countByIsActiveTrue();

        List<DailyPlay> last7Days = getLast7DaysPlays();

        return new DashboardStats(
                totalUsers, totalSongs, totalArtists, totalAlbums,
                totalPlays, premiumUsers, last7Days
        );
    }

    private List<DailyPlay> getLast7DaysPlays() {
        List<DailyPlay> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date  = LocalDate.now().minusDays(i);
            long      count = historyRepository.countByDate(date.toString());
            result.add(new DailyPlay(date.toString(), count));
        }
        return result;
    }

    // ── User Management ───────────────────────────────────────────────────────

    public PageResponse<UserAdminView> getUsers(int page, int size, String search) {
        Page<User> users = search != null && !search.isBlank()
                ? userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                search, search, PageRequest.of(page, size, Sort.by("createdAt").descending()))
                : userRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        Page<UserAdminView> mapped = users.map(u -> {
            String plan = subscriptionRepository.findByUserId(u.getId())
                    .map(s -> s.getPlan().name()).orElse("FREE");
            long plays = historyRepository.countByUserId(u.getId());
            return new UserAdminView(
                    u.getId(), u.getUsername(), u.getEmail(),
                    u.getRole().name(), u.isActive(),
                    plan, u.getCreatedAt(), plays
            );
        });

        return toPage(mapped);
    }

    @Transactional
    public void updateUserRole(UUID userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        user.setRole(User.Role.valueOf(role));
        userRepository.save(user);
    }

    @Transactional
    public void toggleUserActive(UUID userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        user.setActive(isActive);
        userRepository.save(user);
    }

    // ── Song Management ───────────────────────────────────────────────────────

    public PageResponse<SongResponse> getSongs(int page, int size, String search) {
        Page<Song> songs = search != null && !search.isBlank()
                ? songRepository.findByTitleContainingIgnoreCase(
                search, PageRequest.of(page, size, Sort.by("createdAt").descending()))
                : songRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return toPage(songs.map(SongResponse::from));
    }

    @Transactional
    public SongResponse createSong(CreateSongRequest req) {
        Artist artist = artistRepository.findById(req.artistId())
                .orElseThrow(() -> new IllegalArgumentException("Artist không tồn tại"));
        Album album = req.albumId() != null
                ? albumRepository.findById(req.albumId()).orElse(null) : null;

        Song song = Song.builder()
                .title(req.title()).audioUrl(req.audioUrl())
                .coverUrl(req.coverUrl()).artist(artist).album(album)
                .durationSeconds(req.durationSeconds())
                .trackNumber(req.trackNumber()).isPremium(req.isPremium())
                .build();
        return SongResponse.from(songRepository.save(song));
    }

    @Transactional
    public void deleteSong(UUID id) {
        songRepository.deleteById(id);
    }

    @Transactional
    public void toggleSongActive(UUID id, boolean active) {
        songRepository.findById(id).ifPresent(s -> {
            s.setActive(active);
            songRepository.save(s);
        });
    }

    // ── Artist Management ─────────────────────────────────────────────────────

    public PageResponse<ArtistResponse> getArtists(int page, int size, String search) {
        Page<Artist> artists = search != null && !search.isBlank()
                ? artistRepository.findByNameContainingIgnoreCase(
                search, PageRequest.of(page, size))
                : artistRepository.findAll(PageRequest.of(page, size));
        return toPage(artists.map(ArtistResponse::from));
    }

    @Transactional
    public ArtistResponse createArtist(CreateArtistRequest req) {
        Artist artist = Artist.builder()
                .name(req.name()).bio(req.bio())
                .avatarUrl(req.avatarUrl()).verified(req.verified())
                .build();
        return ArtistResponse.from(artistRepository.save(artist));
    }

    @Transactional
    public void deleteArtist(UUID id) { artistRepository.deleteById(id); }

    // ── Album Management ──────────────────────────────────────────────────────

    public PageResponse<AlbumResponse> getAlbums(int page, int size, String search) {
        Page<Album> albums = search != null && !search.isBlank()
                ? albumRepository.findByTitleContainingIgnoreCase(
                search, PageRequest.of(page, size))
                : albumRepository.findAll(PageRequest.of(page, size));
        return toPage(albums.map(AlbumResponse::from));
    }

    @Transactional
    public AlbumResponse createAlbum(CreateAlbumRequest req) {
        Artist artist = artistRepository.findById(req.artistId())
                .orElseThrow(() -> new IllegalArgumentException("Artist không tồn tại"));
        Album album = Album.builder()
                .title(req.title()).coverUrl(req.coverUrl()).artist(artist)
                .type(Album.AlbumType.valueOf(req.type() != null ? req.type() : "ALBUM"))
                .build();
        return AlbumResponse.from(albumRepository.save(album));
    }

    @Transactional
    public void deleteAlbum(UUID id) { albumRepository.deleteById(id); }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private <T> PageResponse<T> toPage(Page<T> page) {
        return new PageResponse<>(
                page.getContent(), page.getNumber(),
                page.getSize(), page.getTotalElements(), page.getTotalPages()
        );
    }
}