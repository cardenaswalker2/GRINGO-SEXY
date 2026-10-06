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
    public List<Content> getContentsForDevice(DeviceType deviceType) {
        if (deviceType == null) {
            return contentRepository.findAll();
        }
        return contentRepository.findAllActiveByDevice(deviceType);
    }

    @Override
    public long countActiveVideosByDevice(DeviceType deviceType) {
        if (deviceType == null) {
            return 0;
        }
        return contentRepository.countActiveVideosByDevice(deviceType);
    }

    @Override
    public long countActivePhotosByDevice(DeviceType deviceType) {
        if (deviceType == null) {
            return 0;
        }
        return contentRepository.countActivePhotosByDevice(deviceType);
    }

    @Override
    public long countActiveContentsByDevice(DeviceType deviceType) {
        if (deviceType == null) {
            return 0;
        }
        return contentRepository.countActiveContentsByDevice(deviceType);
    }

    @Override
    public Content toggleActive(String id, String ipAddress) {
        Content content = findById(id);
        content.setActive(!content.isActive());
        content.setUpdatedAt(Instant.now());
        Content saved = contentRepository.save(content);
        activityLogService.log(
                "Admin",
                "Admin",
                "CONTENT_TOGGLE_ACTIVE",
                (saved.isActive() ? "Activó" : "Desactivó") + " el contenido: " + saved.getTitle(),
                ipAddress
        );
        return saved;
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
        // 1. Delete legacy contents: remove mockup wallpapers, placeholder texts, and guides without real media
        List<Content> all = contentRepository.findAll();
        for (Content c : all) {
            boolean isLegacyInvalidCat = (c.getCategory() == null || ContentCategory.fromSlug(c.getCategory().getSlug()) == null);
            boolean isMockupOrNoMedia = (c.getSlug() != null && (c.getSlug().contains("wallpapers") || c.getSlug().contains("cyberpunk") || c.getSlug().contains("estetica") || ( (c.getVideoUrl() == null || c.getVideoUrl().isEmpty()) && (c.getImageUrl() == null || c.getImageUrl().isEmpty()) )));
            if (isLegacyInvalidCat || isMockupOrNoMedia) {
                contentRepository.delete(c);
            }
        }

        log.info("Ensuring curated video and photo contents for all mobile devices...");

        Set<DeviceType> iosOnly = Set.of(DeviceType.IPHONE);
        Set<DeviceType> samsungOnly = Set.of(DeviceType.SAMSUNG);
        Set<DeviceType> motoOnly = Set.of(DeviceType.MOTOROLA);
        Set<DeviceType> xiaomiOnly = Set.of(DeviceType.XIAOMI);

        // ==========================================
        // 1. IPHONE VIDEOS
        // ==========================================
        upsertMediaContent(
                "iphone-config-sensi-baja-v1",
                "CONFIG & SENSI BAJA COMPLETA - IPHONE v1",
                "Calibración de baja sensibilidad para iPhone. Máxima estabilidad en miras telescópicas y control preciso de retroceso.",
                "Calibración de precisión para iPhone.",
                ContentCategory.SENSITIVITIES,
                iosOnly,
                false,
                "/videos/iphone-confi-sensi-baja-v1.mp4",
                null,
                1
        );

        upsertMediaContent(
                "iphone-config-sensi-media-v1",
                "CONFIG & SENSI MEDIA COMPLETA - IPHONE v1",
                "Calibración balanceada para iPhone. Equilibrio perfecto entre agilidad de giro 360° y levantamiento suave de mira.",
                "Calibración balanceada para iPhone.",
                ContentCategory.SENSITIVITIES,
                iosOnly,
                false,
                "/videos/iphone-confi-sensi-media-v1.mp4",
                null,
                2
        );

        upsertMediaContent(
                "iphone-config-sensi-media-v2",
                "CONFIG & SENSI MEDIA COMPLETA - IPHONE v2 (OLED & 120Hz)",
                "Calibración media avanzada v2 optimizada para pantallas Super Retina XDR y ProMotion 120Hz (iPhone 12 al 16 Pro Max).",
                "Calibración 120Hz para iPhone Pro / Pro Max.",
                ContentCategory.SENSITIVITIES,
                iosOnly,
                false,
                "/videos/iphone-confi-sensi-media-v2.mp4",
                null,
                3
        );

        // ==========================================
        // 2. IPHONE FOTOS (4 Sensibility Presets)
        // ==========================================
        upsertMediaContent(
                "iphone-sensi-preset-universal-clasico",
                "SENSI IPHONE - MODO UNIVERSAL CLÁSICO (FOTO HD)",
                "Captura oficial del Preset D: General 44, Punto Rojo 67, Mira 2X 67, Mira 4X 67, Francotirador 67.",
                "Valores exactos: General: 44, Red Dot: 67, 2X: 67, 4X: 67, Sniper: 67, Cámara: 67.",
                ContentCategory.SENSITIVITIES,
                iosOnly,
                false,
                null,
                "/images/sensibilidad-iphone-1.jpg",
                4
        );

        upsertMediaContent(
                "iphone-sensi-preset-awm-precision",
                "SENSI IPHONE - AWM & FRANCOTIRADOR PRECISIÓN (FOTO HD)",
                "Captura oficial del Preset B: General 190, Punto Rojo 50, Mira 2X 100, Mira 4X 100, AWM 20.",
                "Valores exactos: General: 190, Red Dot: 50, 2X: 100, 4X: 100, AWM: 20, Cámara: 88.",
                ContentCategory.SENSITIVITIES,
                iosOnly,
                false,
                null,
                "/images/sensibilidad-iphone-2.jpg",
                5
        );

        upsertMediaContent(
                "iphone-sensi-preset-competitivo-pro",
                "SENSI IPHONE - MODO COMPETITIVO PRO 200% (FOTO HD)",
                "Captura oficial del Preset A: General 178, Punto Rojo 178, Mira 2X 200, Mira 4X 200, Francotirador 150.",
                "Valores exactos: General: 178, Red Dot: 178, 2X: 200, 4X: 200, Sniper: 150, Cámara: 150.",
                ContentCategory.SENSITIVITIES,
                iosOnly,
                false,
                null,
                "/images/sensibilidad-iphone-3.jpg",
                6
        );

        upsertMediaContent(
                "iphone-sensi-preset-control-dispersion",
                "SENSI IPHONE - CONTROL DE DISPERSIÓN (FOTO HD)",
                "Captura oficial del Preset C: General 69, Red Dot 45, 2x Scope 70, 4x Scope 100, Sniper Scope 0.",
                "Valores exactos: General: 69, Red Dot: 45, 2X: 70, 4X: 100, Sniper: 0, Free Camera: 0.",
                ContentCategory.SENSITIVITIES,
                iosOnly,
                false,
                null,
                "/images/sensibilidad-iphone-4.jpg",
                7
        );

        // ==========================================
        // 3. SAMSUNG GALAXY VIDEOS & FOTOS
        // ==========================================
        upsertMediaContent(
                "samsung-confi-sensi-alta-v1",
                "CONFIG & SENSI ALTA COMPLETA - SAMSUNG GALAXY v1",
                "Ajuste de DPI perfecto para Samsung Galaxy One UI con Game Booster Plus y frecuencia de muestreo táctil aumentada.",
                "Ajustes de DPI y Game Booster Plus para Samsung Galaxy.",
                ContentCategory.SENSITIVITIES,
                samsungOnly,
                false,
                "/videos/samsung-confi-sensi-alta-v1.mp4",
                null,
                8
        );

        upsertMediaContent(
                "samsung-sensi-captura-oneui-pro",
                "SENSI SAMSUNG GALAXY - ONE UI 6.0 PRO (FOTO HD)",
                "Captura fotográfica de calibración para Samsung Galaxy S21/S22/S23/S24 y Serie A (A54/A34).",
                "Valores de sensibilidad para Samsung Galaxy en resolución nativa.",
                ContentCategory.SENSITIVITIES,
                samsungOnly,
                false,
                null,
                "/images/sensibilidad-iphone-1.jpg",
                9
        );

        // ==========================================
        // 4. MOTOROLA VIDEOS & FOTOS
        // ==========================================
        upsertMediaContent(
                "motorola-confi-sensi-alta-v1",
                "CONFIG & SENSI ALTA COMPLETA - MOTOROLA v1",
                "Calibración de DPI y sensibilidad pura para Motorola Moto Edge y serie G con Moto Gametime optimizado.",
                "Calibración Moto Gametime y DPI para Motorola.",
                ContentCategory.SENSITIVITIES,
                motoOnly,
                false,
                "/videos/motorola-confi-sensi-alta-v1.mp4",
                null,
                10
        );

        upsertMediaContent(
                "motorola-sensi-captura-gametime-pro",
                "SENSI MOTOROLA - MOTO GAMETIME 144HZ (FOTO HD)",
                "Captura de sensibilidad milimétrica optimizada para pantallas OLED de 144Hz de Motorola Edge.",
                "Ajuste táctil y sensibilidad en pantalla para Motorola.",
                ContentCategory.SENSITIVITIES,
                motoOnly,
                false,
                null,
                "/images/sensibilidad-iphone-2.jpg",
                11
        );

        // ==========================================
        // 5. XIAOMI / POCO VIDEOS & FOTOS
        // ==========================================
        upsertMediaContent(
                "xiaomi-hyperos-game-turbo-v1",
                "CONFIG & SENSI ALTA COMPLETA - XIAOMI / POCO (HyperOS & Game Turbo)",
                "Calibración de respuesta táctil a 480Hz, Game Turbo y optimización de DPI para MIUI e HyperOS.",
                "Optimización de Game Turbo y 480Hz táctil para Xiaomi y POCO.",
                ContentCategory.SENSITIVITIES,
                xiaomiOnly,
                false,
                "/videos/modificacionx.mp4",
                null,
                12
        );

        upsertMediaContent(
                "xiaomi-sensi-captura-poco-pro",
                "SENSI XIAOMI & POCO - GAME TURBO CALIBRADO (FOTO HD)",
                "Captura fotográfica oficial de calibración para POCO X3, X5, X6 Pro, F5 y Xiaomi Serie 13/14.",
                "Calibración de respuesta de pantalla para Xiaomi y POCO.",
                ContentCategory.SENSITIVITIES,
                xiaomiOnly,
                false,
                null,
                "/images/sensibilidad-iphone-3.jpg",
                13
        );

        log.info("Finished seeding/updating device-centric multimedia content.");
    }

    private void upsertMediaContent(String slug, String title, String summary, String body,
                                    ContentCategory category, Set<DeviceType> targetDevices,
                                    boolean applyToAllDevices, String videoUrl, String imageUrl, int sortOrder) {
        Content content = contentRepository.findBySlug(slug).orElse(new Content());
        content.setSlug(slug);
        content.setTitle(title);
        content.setSummary(summary);
        content.setBody(body != null ? body : summary);
        content.setCategory(category);
        content.setApplyToAllDevices(applyToAllDevices);
        content.setTargetDevices(applyToAllDevices ? new HashSet<>() : (targetDevices != null ? new HashSet<>(targetDevices) : new HashSet<>()));
        content.setVideoUrl(videoUrl);
        content.setImageUrl(imageUrl);
        content.setActive(true);
        content.setSortOrder(sortOrder);
        if (content.getCreatedAt() == null) {
            content.setCreatedAt(Instant.now());
        }
        content.setUpdatedAt(Instant.now());
        contentRepository.save(content);
    }
}

