package com.esimedia.features.auth.services;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int IP_USER_THRESHOLD = 20;
    private static final long IP_ADAPTIVE_DELAY_MS = 2000L;
    private static final long WINDOW_DURATION_MS = 5 * 60 * 1000L;
    private static final int ADAPTIVE_MAX_ATTEMPTS = 2;
    
    private final EmailService emailService;
    private final UsuarioNormalRepository userNormalRepository;
    private final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);

    @Autowired
    public LoginAttemptService(EmailService emailService, UsuarioNormalRepository userNormalRepository) {
        this.emailService = emailService;
        this.userNormalRepository = userNormalRepository;
    }

    private static final long[] BLOCK_TIMES_MS = {
        // 30 segundos
        30 * 1000L,
        // 2 minutos
        2 * 60 * 1000L,
        // 4 minutos
        4 * 60 * 1000L,
        // 5 minutos
        5 * 60 * 1000L
    };


    // Key: username|ip
    private final Cache<String, AttemptInfo> attempts = Caffeine.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build();

    // Para rate limiting adaptativo por IP
    private final ConcurrentHashMap<String, Set<String>> ipToUsers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> ipAdaptiveUntil = new ConcurrentHashMap<>();
    

    // Comprueba si usuario+ip está bloqueado
    public boolean isBlocked(String username, String ip) {
        String key = username + "|" + ip;
        AttemptInfo info = attempts.getIfPresent(key);
        if (info == null || info.blockStartTime == 0)  {
            return false;
        }
        long duration = BLOCK_TIMES_MS[Math.min(info.blockLevel, BLOCK_TIMES_MS.length - 1)];
        if (System.currentTimeMillis() - info.blockStartTime < duration) {
            return true;
        }
        // Desbloquear tras tiempo cumplido
        info.blockStartTime = 0;
        info.blockLevel = 0;
        info.attemptTimestamps.clear();
        attempts.put(key, info);
        return false;
    }

    // Comprueba si la IP está en modo adaptativo (umbral reducido)
    public boolean isIpBlocked(String ip) {
        Long until = ipAdaptiveUntil.get(ip);
        return until != null && until > System.currentTimeMillis();
    }

    // Devuelve segundos restantes para modo adaptativo de IP
    public long getIpRetryAfterSeconds(String ip) {
        Long until = ipAdaptiveUntil.get(ip);
        if (until == null) return 0;
        long diff = until - System.currentTimeMillis();
        return diff > 0 ? (diff / 1000) : 0;
    }

    // Devuelve segundos restantes para desbloqueo de usuario+ip
    public long getUserRetryAfterSeconds(String username, String ip) {
        String key = username + "|" + ip;
        AttemptInfo info = attempts.getIfPresent(key);
        if (info == null || info.blockStartTime == 0) return 0;
        long duration = BLOCK_TIMES_MS[Math.min(info.blockLevel, BLOCK_TIMES_MS.length - 1)];
        long diff = duration - (System.currentTimeMillis() - info.blockStartTime);
        return diff > 0 ? (diff / 1000) : 0;
    }

    public void recordFailedAttempt(String username, String ip) {
        String key = username + "|" + ip;
        AttemptInfo info = attempts.get(key, k -> new AttemptInfo());

        // No contar si está bloqueado
        if (isBlocked(username, ip)) return;

        boolean isAdaptive = isIpBlocked(ip);
        int maxAttempts = isAdaptive ? ADAPTIVE_MAX_ATTEMPTS : MAX_ATTEMPTS;

        long now = System.currentTimeMillis();
        pruneOldTimestamps(info, now);

        if (isAdaptive && !sleepSilently(IP_ADAPTIVE_DELAY_MS)) return;

        registerIpUser(ip, username);

        if (shouldActivateAdaptive(ip)) {
            activateAdaptiveMode(ip);
            if (!sleepSilently(IP_ADAPTIVE_DELAY_MS)) return;
        }

        // Registrar el intento fallido
        info.attemptTimestamps.add(now);

        int currentAttempts = info.attemptTimestamps.size();

        if (currentAttempts >= maxAttempts) {
            handleBlock(info, maxAttempts, username);
        }

        attempts.put(key, info);
    }

    public void resetAttempts(String username, String ip) {
        String key = username + "|" + ip;
        attempts.invalidate(key);
    }

    private static class AttemptInfo {
        java.util.List<Long> attemptTimestamps = new java.util.ArrayList<>();
        int blockLevel = 0;
        long blockStartTime = 0;
    }
    private void pruneOldTimestamps(AttemptInfo info, long now) {
        info.attemptTimestamps.removeIf(timestamp -> (now - timestamp) > WINDOW_DURATION_MS);
    }

    private boolean sleepSilently(long ms) {
        try {
            Thread.sleep(ms);
            return true;
        } 
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void registerIpUser(String ip, String username) {
        ipToUsers.computeIfAbsent(ip, k -> new HashSet<>()).add(username);
    }

    private boolean shouldActivateAdaptive(String ip) {
        Set<String> users = ipToUsers.get(ip);
        return users != null && users.size() >= IP_USER_THRESHOLD;
    }

    private void activateAdaptiveMode(String ip) {
        ipAdaptiveUntil.put(ip, System.currentTimeMillis() + WINDOW_DURATION_MS);
        Set<String> users = ipToUsers.get(ip);
        if (users != null) {
            users.clear();
        }
    }

    private void handleBlock(AttemptInfo info, int maxAttempts, String username) {
        info.blockLevel++;
        info.blockStartTime = System.currentTimeMillis();

        int totalAttempts = maxAttempts * info.blockLevel;

        userNormalRepository.findByemail(username).ifPresent(user -> {
            try {
                emailService.sendSecurityAlertEmail(user, totalAttempts);
            } 
            catch (Exception e) {
                logger.error("Error enviando correo de alerta: ", e);
            }
        });

        info.attemptTimestamps.clear();
    }

}
