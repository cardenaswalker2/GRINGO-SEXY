package com.gringosexy.service.impl;

import com.gringosexy.dto.ContentRequest;
import com.gringosexy.enums.ContentCategory;
import com.gringosexy.enums.DeviceType;
import com.gringosexy.exception.ResourceNotFoundException;
import com.gringosexy.exception.ValidationException;
import com.gringosexy.model.Content;
import com.gringosexy.model.User;
import com.gringosexy.repository.ContentRepository;
import com.gringosexy.service.ActivityLogService;
import com.gringosexy.service.ContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ContentServiceImpl implements ContentService {

    private static final Logger log = LoggerFactory.getLogger(ContentServiceImpl.class);

    private final ContentRepository contentRepository;
    private final ActivityLogService activityLogService;
    private final MongoTemplate mongoTemplate;

    public ContentServiceImpl(ContentRepository contentRepository,
                              ActivityLogService activityLogService,
                              MongoTemplate mongoTemplate) {
        this.contentRepository = contentRepository;
        this.activityLogService = activityLogService;
        this.mongoTemplate = mongoTemplate;
    }

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String toSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    @Override
    public Content createContent(ContentRequest request, String createdBy, String ipAddress) {
        String slug = (request.getSlug() != null && !request.getSlug().trim().isEmpty())
                ? toSlug(request.getSlug().trim())
                : toSlug(request.getTitle());

        if (contentRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        Content content = new Content();
        content.setTitle(request.getTitle().trim());
        content.setSlug(slug);
        content.setSummary(request.getSummary().trim());
        content.setBody(request.getBody().trim());
        content.setCategory(request.getCategory());
        content.setApplyToAllDevices(request.isApplyToAllDevices());
        content.setTargetDevices(request.isApplyToAllDevices() ? new HashSet<>() : request.getTargetDevices());
        content.setImageUrl(request.getImageUrl());
        content.setVideoUrl(request.getVideoUrl());
        content.setActive(request.isActive());
        content.setFeatured(request.isFeatured());
        content.setSortOrder(request.getSortOrder());
        content.setCreatedBy(createdBy);
        content.setCreatedAt(Instant.now());
        content.setUpdatedAt(Instant.now());

        Content saved = contentRepository.save(content);
        activityLogService.log(createdBy, "Admin", "CONTENT_CREATED", "Contenido creado: " + saved.getTitle(), ipAddress);
        return saved;
    }

    @Override
    public Content updateContent(String id, ContentRequest request, String ipAddress) {
        Content content = findById(id);

        content.setTitle(request.getTitle().trim());
        if (request.getSlug() != null && !request.getSlug().trim().isEmpty()) {
            content.setSlug(toSlug(request.getSlug().trim()));
        }
        content.setSummary(request.getSummary().trim());
        content.setBody(request.getBody().trim());
        content.setCategory(request.getCategory());
        content.setApplyToAllDevices(request.isApplyToAllDevices());
        content.setTargetDevices(request.isApplyToAllDevices() ? new HashSet<>() : request.getTargetDevices());
        content.setImageUrl(request.getImageUrl());
        content.setVideoUrl(request.getVideoUrl());
        content.setActive(request.isActive());
        content.setFeatured(request.isFeatured());
        content.setSortOrder(request.getSortOrder());
        content.setUpdatedAt(Instant.now());

        Content updated = contentRepository.save(content);
        activityLogService.log("Admin", "Admin", "CONTENT_UPDATED", "Contenido actualizado: " + updated.getTitle(), ipAddress);
        return updated;
    }

    @Override
    public void deleteContent(String id, String ipAddress) {
        Content content = findById(id);
        contentRepository.delete(content);
        activityLogService.log("Admin", "Admin", "CONTENT_DELETED", "Contenido eliminado: " + content.getTitle(), ipAddress);
    }

    @Override
    public Content findById(String id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contenido no encontrado con ID: " + id));
    }

    @Override
    public Content findBySlug(String slug) {
        return contentRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Contenido no encontrado para slug: " + slug));
    }

    @Override
    public List<Content> getContentsForUserCategory(User user, ContentCategory category) {
        if (user == null || user.getDeviceType() == null) {
            return contentRepository.findByCategoryAndActiveTrueOrderBySortOrderAscCreatedAtDesc(category);
        }
        return contentRepository.findByCategoryAndDevice(category, user.getDeviceType());
    }

    @Override
    public Page<Content> searchContents(String queryStr, ContentCategory category, DeviceType deviceType, Pageable pageable) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (queryStr != null && !queryStr.trim().isEmpty()) {
            String s = queryStr.trim();
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("title").regex(s, "i"),
                    Criteria.where("summary").regex(s, "i"),
                    Criteria.where("body").regex(s, "i")
            ));
        }

        if (category != null) {
            criteriaList.add(Criteria.where("category").is(category));
        }

        if (deviceType != null) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("applyToAllDevices").is(true),
                    Criteria.where("targetDevices").is(deviceType)
            ));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long count = mongoTemplate.count(query, Content.class);
        query.with(pageable);
        List<Content> list = mongoTemplate.find(query, Content.class);

        return new PageImpl<>(list, pageable, count);
    }

    @Override
    public void initDefaultContents() {
        if (contentRepository.count() == 0) {
            log.info("Seeding initial curated contents for GRINGO SEXY platform...");

            // 1. Sensibilidades iPhone
            Set<DeviceType> iosOnly = Set.of(DeviceType.IPHONE);
            Content c1 = new Content(
                    "Sensibilidad Competitiva iOS Pro - iPhone 11 al 15 Pro Max",
                    "sensibilidad-competitiva-ios-pro",
                    "Configuración milimétrica de respuesta 3D Touch/Haptic Touch y velocidad de puntero para máxima precisión en disparos a la cabeza.",
                    "### Pasos de Configuración en iPhone:\n\n1. Ve a **Ajustes > Accesibilidad > Tocar > AssistiveTouch**.\n2. Establece la **Sensibilidad del seguimiento** al 100% (hacia la liebre).\n3. En **Control del puntero**: Velocidad de desplazamiento: 120%. Tamaño del puntero: Mínimo.\n4. En **Control por botón** (Switch Control): Ciclos: 10, Presión de cursor refinado: 120 DPI.\n5. En Juego (Free Fire / COD): General 98, Mira de Punto Rojo 95, Mira 2X 92, Mira 4X 88, Francotirador 50, Cámara 100.",
                    ContentCategory.SENSITIVITIES,
                    iosOnly,
                    false
            );
            c1.setSortOrder(1);
            contentRepository.save(c1);

            // 2. Sensibilidades Samsung
            Set<DeviceType> samsungOnly = Set.of(DeviceType.SAMSUNG);
            Content c2 = new Content(
                    "Calibración DPI & Sensibilidad Ultra One UI - Galaxy S/A Series",
                    "calibracion-dpi-sensibilidad-samsung-galaxy",
                    "Ajuste de DPI perfecto para Samsung Galaxy One UI con Game Booster Plus y frecuencia de muestreo táctil aumentada.",
                    "### Pasos para Samsung Galaxy:\n\n1. Activa **Opciones de desarrollador** (7 toques en Número de compilación).\n2. Modifica el **Ancho mínimo (DPI)** a **580** (para serie A) o **640** (para serie S / Ultra).\n3. Ve a **Ajustes > Administración general > Ratón y panel táctil**:\n   - Velocidad del puntero: Rápido (al máximo).\n   - Velocidad de desplazamiento de rueda: Rápido.\n4. Abre **Game Plugins > Game Booster Plus**:\n   - Modo: Máximo rendimiento de FPS (100% calidad, 100% brillo máx).\n5. En Juego: General 99, Punto Rojo 94, Mira 2X 90, Mira 4X 85.",
                    ContentCategory.SENSITIVITIES,
                    samsungOnly,
                    false
            );
            c2.setSortOrder(2);
            contentRepository.save(c2);

            // 3. Optimizaciones universales
            Content c3 = new Content(
                    "Optimización de RAM y Aceleración de Tasa de Refresco a 120Hz/90Hz",
                    "optimizacion-ram-tasa-refresco-120hz",
                    "Libera recursos críticos en segundo plano, reduce la latencia de entrada táctil y fuerza la tasa de refresco más fluida.",
                    "### Instrucciones Generales:\n\n1. Desactiva las animaciones del sistema en Opciones de desarrollador:\n   - Escala de animación de ventana: 0.5x o desactivada.\n   - Escala de transición: 0.5x.\n   - Escala de duración de animador: 0.5x.\n2. Limita los procesos en segundo plano a máximo **3 procesos**.\n3. Habilita **Forzar 4x MSAA** si dispones de GPU Adreno 600+ o Apple Bionic.\n4. Limpia la partición de memoria caché desde el menú Recovery cada 15 días.",
                    ContentCategory.OPTIMIZATIONS,
                    null,
                    true
            );
            c3.setSortOrder(3);
            contentRepository.save(c3);

            // 4. Modificaciones
            Content c4 = new Content(
                    "Ajustes Secretos del Desarrollador y Registro del Sistema (Buffer 16M)",
                    "ajustes-secretos-desarrollador-buffer-16m",
                    "Elimina el stuttering y el retardo de cuadros ajustando el tamaño del búfer de registro y las capas de depuración GPU.",
                    "### Parámetros recomendados:\n\n- **Tamaño de búfer de registro (Logger Buffer Size)**: 16M (Máxima reducción de lag).\n- **Controlador de gráficos (Game Driver)**: Selecciona tus juegos favoritos y asígnalos a 'Controlador de gráficos del sistema' en vez del predeterminado.\n- **Desactivar superposiciones HW**: Activado (Fuerza a la GPU a encargarse siempre del renderizado de pantalla).",
                    ContentCategory.MODIFICATIONS,
                    null,
                    true
            );
            c4.setSortOrder(4);
            contentRepository.save(c4);

            log.info("Initialized default content articles for the 3 active categories.");
        }
    }

}
