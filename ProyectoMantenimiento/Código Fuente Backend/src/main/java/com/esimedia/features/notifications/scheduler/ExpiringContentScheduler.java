package com.esimedia.features.notifications.scheduler;

import com.esimedia.features.auth.repository.UsuarioNormalRepository;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.content.entity.ContenidosAudio;
import com.esimedia.features.content.entity.ContenidosVideo;
import com.esimedia.features.content.repository.ContenidosAudioRepository;
import com.esimedia.features.content.repository.ContenidosVideoRepository;
import com.esimedia.features.notifications.entity.Notification;
import com.esimedia.features.notifications.enums.NotificationSubtype;
import com.esimedia.features.notifications.enums.NotificationType;
import com.esimedia.features.notifications.repository.NotificationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ExpiringContentScheduler {

    private final ContenidosAudioRepository audioRepository;
    private final ContenidosVideoRepository videoRepository;
    private final NotificationRepository notificationRepository;
    private final UsuarioNormalRepository usuarioRepository;

    private static final Logger logger = LoggerFactory.getLogger(ExpiringContentScheduler.class);

    public ExpiringContentScheduler(ContenidosAudioRepository audioRepository,
                                    ContenidosVideoRepository videoRepository,
                                    NotificationRepository notificationRepository,
                                    UsuarioNormalRepository usuarioRepository) {
        this.audioRepository = audioRepository;
        this.videoRepository = videoRepository;
        this.notificationRepository = notificationRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Cron para las 2 y 5 de la mañana
    @Scheduled(cron = "0 0 2,5 * * *")
    //@Scheduled(fixedRate = 10000) //10 segundos
    public void processDailyExpiringContent() {
        logger.info("🔔 Ejecutando proceso de alertas de contenido por caducar...");
        int totalGeneradas = 0;
        totalGeneradas += createAlertsForExpiringAudio();
        totalGeneradas += createAlertsForExpiringVideo();
        logger.info("🔔 Alertas de caducidad generadas en esta ejecución: {}", totalGeneradas);
    }

    private int createAlertsForExpiringAudio() {
        List<ContenidosAudio> audios = audioRepository.findAll();
        int generadas = 0;
        List<UsuarioNormal> usuarios = usuarioRepository.findAll();

        for (ContenidosAudio contenido : audios) {
            if (contenido.getFechaDisponibleHasta() == null) continue;

            LocalDateTime fechaDisponible = contenido.getFechaDisponibleHasta().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();

            if (isExpiringWithinAWeek(fechaDisponible)) {
                logger.info("⚠ Contenido de audio '{}' (ID: {}) expira el {}",
                        contenido.getTitulo(), contenido.getId(), fechaDisponible);

                for (UsuarioNormal usuario : usuarios) {
                    String userId = usuario.getIdUsuario();

                    if (!notificationRepository.existsByUserIdAndTypeAndSubtypeAndPayloadContaining(
                            userId,
                            NotificationType.ALERT,
                            NotificationSubtype.EXPIRING_SOON,
                            "\"contentId\":\"" + contenido.getId() + "\"")) {

                        Notification alert = new Notification();
                        alert.setUserId(userId); // asignamos al usuario real
                        alert.setType(NotificationType.ALERT);
                        alert.setSubtype(NotificationSubtype.EXPIRING_SOON);
                        alert.setTitle("⏳ Contenido de audio por expirar");
                        alert.setBody("El contenido \"" + contenido.getTitulo() +
                                "\" dejará de estar disponible el " + fechaDisponible + ".");
                        alert.setCreatedAt(LocalDateTime.now());

                        notificationRepository.save(alert);
                        generadas++;
                        logger.info("✅ Alerta creada para el usuario {} y audio '{}'", userId, contenido.getTitulo());
                    }
                }
            }
        }
        return generadas;
    }

    private int createAlertsForExpiringVideo() {
        List<ContenidosVideo> videos = videoRepository.findAll();
        int generadas = 0;
        List<UsuarioNormal> usuarios = usuarioRepository.findAll();

        for (ContenidosVideo contenido : videos) {
            if (contenido.getFechaDisponibleHasta() == null) continue;

            LocalDateTime fechaDisponible = contenido.getFechaDisponibleHasta().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();

            if (isExpiringWithinAWeek(fechaDisponible)) {
                logger.info("⚠ Contenido de video '{}' (ID: {}) expira el {}",
                        contenido.getTitulo(), contenido.getId(), fechaDisponible);

                for (UsuarioNormal usuario : usuarios) {
                    String userId = usuario.getIdUsuario();

                    if (!notificationRepository.existsByUserIdAndTypeAndSubtypeAndPayloadContaining(
                            userId,
                            NotificationType.ALERT,
                            NotificationSubtype.EXPIRING_SOON,
                            "\"contentId\":\"" + contenido.getId() + "\"")) {

                        Notification alert = new Notification();
                        alert.setUserId(userId);
                        alert.setType(NotificationType.ALERT);
                        alert.setSubtype(NotificationSubtype.EXPIRING_SOON);
                        alert.setTitle("⏳ Contenido de video por expirar");
                        alert.setBody("El contenido \"" + contenido.getTitulo() +
                                "\" dejará de estar disponible el " + fechaDisponible + ".");
                        alert.setCreatedAt(LocalDateTime.now());

                        notificationRepository.save(alert);
                        generadas++;
                        logger.info("✅ Alerta creada para el usuario {} y video '{}'", userId, contenido.getTitulo());
                    }
                }
            }
        }
        return generadas;
    }

    private boolean isExpiringWithinAWeek(LocalDateTime fechaDisponibleHasta) {
        LocalDateTime now = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime lowerBound = now.plusDays(6);
        LocalDateTime upperBound = now.plusDays(7);

        LocalDateTime fecha = fechaDisponibleHasta.withHour(0).withMinute(0).withSecond(0).withNano(0);

        boolean within = (fecha.isEqual(lowerBound) || fecha.isAfter(lowerBound)) &&
                (fecha.isEqual(upperBound) || fecha.isBefore(upperBound));

        logger.debug("Comparando fecha {}: dentro de 6-7 días? {} (rango {} - {})",
                fecha, within, lowerBound, upperBound);

        return within;
    }

}
