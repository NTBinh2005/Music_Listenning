package com.project.music_listenning.Service;

import com.project.music_listenning.dto.request.AuthRequest;
import com.project.music_listenning.dto.response.AuthDTO;
import com.project.music_listenning.dto.response.AuthDTO.AuthResponse;

public interface AuthService {
    public AuthResponse register(AuthRequest.RegisterRequest request);
    public AuthResponse login(AuthRequest.LoginRequest request);
    public AuthResponse refresh(AuthDTO.RefreshRequest request);
}
