@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UsuarioNormalRepository usuarioNormalRepository;
    @Mock private ValidationService validationService;
    @Mock private Contenido contenido;  // ← AÑADIDO mock aquí
    
    @InjectMocks private NotificationService notificationService;

    private UsuarioNormal usuarioNormal;
    private Notification notification;

    @BeforeEach
    void setUp() {
        usuarioNormal = new UsuarioNormal();
        usuarioNormal.setIdUsuario("user-123");
        usuarioNormal.setRol(Rol.NORMAL);
        usuarioNormal.setGustosTags(List.of("musica", "rock"));

        notification = Notification.builder()
                .id("notif-1")
                .userId("user-123")
                .type(NotificationType.NOTIFICATION)
                .subtype(NotificationSubtype.NEW_CONTENT)
                .title("Test")
                .body("Test body")
                .payload("{\"contentId\":\"content-1\"}")
                .build();

        // ← CONFIGURACIÓN COMPLETA del mock Contenido
        when(contenido.getId()).thenReturn("content-1");
        when(contenido.getTitulo()).thenReturn("Canción Rock");
        when(contenido.isVisibilidad()).thenReturn(true);
        when(contenido.isEsVIP()).thenReturn(false);
        when(contenido.getRestriccionEdad()).thenReturn(null);
    }

    @Test
    void markAsRead_shouldMarkNotificationAsRead_whenExists() {
        when(notificationRepository.findByIdAndUserIdAndDeletedFalse("notif-1", "user-123"))
                .thenReturn(Optional.of(notification));

        notificationService.markAsRead("notif-1", "user-123");

        // ✅ CORREGIDO: verificar repo.save(), NO notification.markAsRead()
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_shouldDoNothing_whenNotificationNotFound() {
        when(notificationRepository.findByIdAndUserIdAndDeletedFalse("notif-1", "user-123"))
                .thenReturn(Optional.empty());

        notificationService.markAsRead("notif-1", "user-123");

        // ✅ CORREGIDO: verificar que NO se llama save
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createNotificationsForNewContent_shouldCreateRecommendedForMatchingTastes() {
        when(usuarioNormalRepository.findAll()).thenReturn(List.of(usuarioNormal));
        when(validationService.calculateAge(any())).thenReturn(25);
        when(contenido.getTags()).thenReturn(List.of("rock")); // ← Match gustos

        notificationService.createNotificationsForNewContent(contenido);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture()); // ✅ Se llama

        Notification saved = captor.getValue();
        assertEquals(NotificationSubtype.NEW_CONTENT_RECOMMENDED, saved.getSubtype());
    }

    @Test
    void createNotificationsForNewContent_shouldCreateGenericForNonMatchingTastes() {
        when(usuarioNormalRepository.findAll()).thenReturn(List.of(usuarioNormal));
        when(validationService.calculateAge(any())).thenReturn(25);
        when(contenido.getTags()).thenReturn(List.of("pop")); // ← NO match

        notificationService.createNotificationsForNewContent(contenido);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture()); // ✅ Se llama

        Notification saved = captor.getValue();
        assertEquals(NotificationSubtype.NEW_CONTENT, saved.getSubtype());
    }

    @Test
    void isExpiringWithinAWeek_shouldReturnTrue_whenWithin7Days() {
        Date fechaExpiracion = Date.from(LocalDateTime.now().plusDays(3)
                .atZone(ZoneId.systemDefault()).toInstant());
        when(contenido.getFechaDisponibleHasta()).thenReturn(fechaExpiracion);

        boolean result = notificationService.isExpiringWithinAWeek(contenido);

        assertTrue(result);
    }
}
