package com.esimedia.features.notifications.services;

import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.enums.Rol;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.features.auth.services.ValidationService;
import com.esimedia.features.content.entity.Contenido;
import com.esimedia.features.content.enums.RestriccionEdad;
import com.esimedia.features.notifications.dto.NotificationDTO;
import com.esimedia.features.notifications.entity.Notification;
import com.esimedia.features.notifications.enums.NotificationSubtype;
import com.esimedia.features.notifications.enums.NotificationType;
import com.esimedia.features.notifications.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UsuarioNormalRepository usuarioNormalRepository;
    private final ValidationService validationService;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ===== Buzón =====

    @Transactional(readOnly = true)
    public List<NotificationDTO> getInbox(String userId) {
        logger.debug("Obteniendo buzón de notificaciones para usuario {}", userId);
        List<NotificationDTO> result = notificationRepository
                .findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
        logger.debug("Usuario {} tiene {} notificaciones en el buzón", userId, result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        long count = notificationRepository.countByUserIdAndReadFalseAndDeletedFalse(userId);
        logger.debug("Usuario {} tiene {} notificaciones no leídas", userId, count);
        return count;
    }

    @Transactional
    public void markAsRead(String notificationId, String userId) {
        logger.debug("Marcando notificación {} como leída para usuario {}", notificationId, userId);
        notificationRepository.findByIdAndUserIdAndDeletedFalse(notificationId, userId)
                .ifPresentOrElse(notification -> {
                    notification.markAsRead();
                    notificationRepository.save(notification);
                    logger.info("Notificación {} marcada como leída para usuario {}", notificationId, userId);
                }, () -> logger.warn("No se encontró notificación {} para usuario {}", notificationId, userId));
    }

    @Transactional
    public void deleteNotification(String notificationId, String userId) {
        logger.debug("Marcando notificación {} como eliminada para usuario {}", notificationId, userId);
        notificationRepository.findByIdAndUserIdAndDeletedFalse(notificationId, userId)
                .ifPresentOrElse(notification -> {
                    notification.markAsDeleted();
                    notificationRepository.save(notification);
                    logger.info("Notificación {} marcada como eliminada para usuario {}", notificationId, userId);
                }, () -> logger.warn("No se encontró notificación {} para usuario {}", notificationId, userId));
    }

    // ===== Notificaciones por NUEVO CONTENIDO =====

    @Transactional
    public void createNotificationsForNewContent(Contenido contenido) {
        logger.info("Generando notificaciones para contenido {} (ID: {}, VIP: {}, restricciónEdad: {}, visibilidad: {})",
                contenido.getTitulo(),
                contenido.getId(),
                contenido.isEsVIP(),
                contenido.getRestriccionEdad(),
                contenido.isVisibilidad()
        );

        List<UsuarioNormal> usuarios = usuarioNormalRepository.findAll();
        logger.debug("Se han cargado {} usuarios para evaluar notificaciones", usuarios.size());

        int totalRecomendados = 0;
        int totalGenerales = 0;

        for (UsuarioNormal usuario : usuarios) {

            if (!userCanAccessContent(usuario, contenido)) {
                continue; // descartar usuario que no cumple reglas
            }

            boolean tieneGustos = usuario.getGustosTags() != null && !usuario.getGustosTags().isEmpty();
            boolean matchGustos = matchesUserTastes(usuario, contenido);

            Notification notification;

            if (tieneGustos && matchGustos) {
                // Notificación personalizada por gustos
                notification = Notification.builder()
                        .userId(usuario.getIdUsuario())
                        .type(NotificationType.NOTIFICATION)
                        .subtype(NotificationSubtype.NEW_CONTENT_RECOMMENDED)
                        .title("Lo más recomendado para ti")
                        .body("Nuevo contenido que coincide con tus gustos: " + contenido.getTitulo())
                        .payload("{\"contentId\":\"" + contenido.getId() + "\"}")
                        .build();
                totalRecomendados++;
            } else {
                // Notificación general (VIP, edad, visibilidad)
                notification = Notification.builder()
                        .userId(usuario.getIdUsuario())
                        .type(NotificationType.NOTIFICATION)
                        .subtype(NotificationSubtype.NEW_CONTENT)
                        .title("Esto te podría interesar")
                        .body("Se ha publicado: " + contenido.getTitulo())
                        .payload("{\"contentId\":\"" + contenido.getId() + "\"}")
                        .build();
                totalGenerales++;
            }

            notificationRepository.save(notification);
        }

        logger.info("Notificaciones generadas: recomendadas={}, generales={}", totalRecomendados, totalGenerales);
    }


    // ===== Reglas de filtrado =====
    private boolean userCanAccessContent(UsuarioNormal usuario, Contenido contenido) {
        // 1) Solo usuarios de rol NORMAL
        if (usuario.getRol() != Rol.NORMAL) {
            logger.trace("Usuario {} descartado: rol {} != NORMAL", usuario.getIdUsuario(), usuario.getRol());
            return false;
        }

        // 2) Contenido debe ser visible
        if (!contenido.isVisibilidad()) {
            logger.trace("Usuario {} descartado: contenido {} no es visible", usuario.getIdUsuario(), contenido.getId());
            return false;
        }

        // 3) Regla VIP
        boolean contenidoVip = contenido.isEsVIP(); // getter Lombok: isEsVIP()
        boolean usuarioVip = false;
        try {
            // Ajusta este getter si tu UsuarioNormal lo llama distinto
            usuarioVip = usuario.isFlagVIP(); // getter Lombok: isEsVIP()
        } catch (NoSuchMethodError e) {
            logger.warn("UsuarioNormal no tiene campo VIP implementado aún, se asumirá NO VIP para usuario {}",
                    usuario.getIdUsuario());
            usuarioVip = false;
        } catch (Exception e) {
            logger.error("Error evaluando VIP para usuario {}: {}", usuario.getIdUsuario(), e.getMessage());
            usuarioVip = false;
        }

        if (contenidoVip && !usuarioVip) {
            logger.trace("Usuario {} descartado: contenido VIP y usuario NO VIP", usuario.getIdUsuario());
            return false;
        }

        // 4) Regla de edad según RestriccionEdad
        RestriccionEdad restriccion = contenido.getRestriccionEdad();
        if (restriccion != null && usuario.getFechaNacimiento() != null) {
            int edadUsuario = validationService.calculateAge(usuario.getFechaNacimiento());
            int edadMinima = restriccion.getValor();

            logger.trace("Evaluando edad para usuario {}: edadUsuario={}, edadMinima={}",
                    usuario.getIdUsuario(), edadUsuario, edadMinima);

            if (edadUsuario < edadMinima) {
                logger.trace("Usuario {} descartado: edad {} < mínima {}", usuario.getIdUsuario(), edadUsuario, edadMinima);
                return false;
            }
        }

        logger.trace("Usuario {} pasa todas las reglas de acceso al contenido {}", usuario.getIdUsuario(), contenido.getId());
        return true;
    }

    private boolean matchesUserTastes(UsuarioNormal usuario, Contenido contenido) {
        List<String> gustos = usuario.getGustosTags();
        if (gustos == null || gustos.isEmpty()) {
            logger.trace("Usuario {} sin gustos configurados, no se marca como recomendado", usuario.getIdUsuario());
            return false;
        }

        List<String> tagsContenido = contenido.getTags();
        if (tagsContenido == null || tagsContenido.isEmpty()) {
            logger.trace("Contenido {} sin tags, no se puede cruzar con gustos del usuario {}", contenido.getId(), usuario.getIdUsuario());
            return false;
        }

        Set<String> gustosSet = gustos.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        boolean match = tagsContenido.stream()
                .map(String::toLowerCase)
                .anyMatch(gustosSet::contains);

        logger.trace("Resultado match gustos para usuario {} y contenido {}: {}",
                usuario.getIdUsuario(), contenido.getId(), match);

        return match;
    }

    // ===== Mapping a DTO =====

    private NotificationDTO toDto(Notification n) {
        Map<String, Object> payloadMap = new HashMap<>();

        if (n.getPayload() != null && !n.getPayload().isBlank()) {
            payloadMap.put("raw", n.getPayload());
        }
        return new NotificationDTO(
                n.getId(),
                n.getType() != null ? n.getType().name() : null,
                n.getSubtype() != null ? n.getSubtype().name() : null,
                n.getTitle(),
                n.getBody(),
                payloadMap,
                n.isRead(),
                n.getCreatedAt() != null ? n.getCreatedAt().format(DATE_FORMATTER) : null
        );
    }

    @Transactional
    public void createAlertsForExpiringContent(Contenido contenido) {
        List<UsuarioNormal> usuarios = usuarioNormalRepository.findAll();

        int totalRecomendados = 0;
        int totalGenerales = 0;
        int totalAlertas = 0;

        for (UsuarioNormal usuario : usuarios) {

            if (!userCanAccessContent(usuario, contenido)) continue;

            boolean coincidePreferencias = matchesUserTastes(usuario, contenido);


            // ============================================================
            // A) NOTIFICACIÓN PERSONALIZADA (gustos)
            // ============================================================

            if (coincidePreferencias) {

                if (!alreadyHasRecommendedNotification(usuario.getIdUsuario(), contenido.getId())) {

                    Notification noti = Notification.builder()
                            .userId(usuario.getIdUsuario())
                            .type(NotificationType.NOTIFICATION)
                            .subtype(NotificationSubtype.NEW_CONTENT_RECOMMENDED)
                            .title("✨ Recomendado para ti")
                            .body("Creemos que podría gustarte: \"" + contenido.getTitulo() + "\"")
                            .payload("{\"contentId\":\"" + contenido.getId() + "\"}")
                            .build();

                    notificationRepository.save(noti);
                    totalRecomendados++;
                }

            } else {

                // ============================================================
                // B) NOTIFICACIÓN NORMAL
                // ============================================================

                if (!alreadyHasGenericNotification(usuario.getIdUsuario(), contenido.getId())) {

                    Notification noti = Notification.builder()
                            .userId(usuario.getIdUsuario())
                            .type(NotificationType.NOTIFICATION)
                            .subtype(NotificationSubtype.NEW_CONTENT)
                            .title("🆕 Esto te podría interesar")
                            .body("Nuevo contenido disponible: \"" + contenido.getTitulo() + "\"")
                            .payload("{\"contentId\":\"" + contenido.getId() + "\"}")
                            .build();

                    notificationRepository.save(noti);
                    totalGenerales++;
                }
            }

            // ============================================================
            // C) ALERTAS (faltan 7 días para caducar)
            // ============================================================

            if (isExpiringWithinAWeek(contenido)) {

                if (!alreadyHasExpiringAlert(usuario.getIdUsuario(), contenido.getId())) {

                    Notification alerta = Notification.builder()
                            .userId(usuario.getIdUsuario())
                            .type(NotificationType.ALERT)
                            .subtype(NotificationSubtype.EXPIRING_SOON)
                            .title("⚠ Contenido por caducar")
                            .body("El contenido \"" + contenido.getTitulo() + "\" dejará de estar disponible pronto.")
                            .payload("{\"contentId\":\"" + contenido.getId() + "\"}")
                            .build();

                    notificationRepository.save(alerta);
                    totalAlertas++;
                }
            }
        }
        logger.debug("Contenido {} expira en menos de 7 días: {}", contenido.getId(), isExpiringWithinAWeek(contenido));
        logger.info("Personalizadas enviadas: {}", totalRecomendados);
        logger.info("Generales enviadas: {}", totalGenerales);
        logger.info("Alertas de caducidad enviadas: {}", totalAlertas);
    }

    private boolean alreadyHasRecommendedNotification(String userId, String contentId) {
        return notificationRepository.existsByUserIdAndSubtypeAndPayloadContaining(
                userId,
                NotificationSubtype.NEW_CONTENT_RECOMMENDED,
                contentId
        );
    }

    private boolean alreadyHasGenericNotification(String userId, String contentId) {
        return notificationRepository.existsByUserIdAndSubtypeAndPayloadContaining(
                userId,
                NotificationSubtype.NEW_CONTENT,
                contentId
        );
    }

    private boolean alreadyHasExpiringAlert(String userId, String contentId) {
        return notificationRepository.existsByUserIdAndSubtypeAndPayloadContaining(
                userId,
                NotificationSubtype.EXPIRING_SOON,
                contentId
        );
    }


    public boolean isExpiringWithinAWeek(Contenido contenido) {
        if (contenido.getFechaDisponibleHasta() == null) return false;

        // Convertimos Date a LocalDateTime
        Date date = contenido.getFechaDisponibleHasta();
        LocalDateTime fechaDisponible = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusDays(7);

        return fechaDisponible.isBefore(limit) && fechaDisponible.isAfter(now);
    }






}
