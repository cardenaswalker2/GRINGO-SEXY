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
        // 1. Delete legacy contents with invalid categories
        List<Content> all = contentRepository.findAll();
        for (Content c : all) {
            if (c.getCategory() == null || ContentCategory.fromSlug(c.getCategory().getSlug()) == null) {
                contentRepository.delete(c);
            }
        }

        log.info("Ensuring curated video contents & configurations for iPhone and all devices...");

        Set<DeviceType> iosOnly = Set.of(DeviceType.IPHONE);
        Set<DeviceType> samsungOnly = Set.of(DeviceType.SAMSUNG);
        Set<DeviceType> motoOnly = Set.of(DeviceType.MOTOROLA);

        // 1. iPhone Sensi Baja v1 (Video)
        upsertContent(
                "iphone-config-sensi-baja-v1",
                "CONFIG & SENSI BAJA COMPLETA - IPHONE v1",
                "Calibración de baja sensibilidad para iPhone. Máxima estabilidad en miras telescópicas, suavizado de jitter y control preciso de retroceso.",
                "### 📱 Configuración & Sensibilidad Baja Completa — iPhone (v1)\n\nCalibración ideal para jugadores que buscan estabilidad absoluta en miras de precisión y cero temblor en pantalla:\n\n#### 1. ⚙️ Ajustes del Sistema iOS\n* Ve a **Ajustes > Accesibilidad > Tocar > AssistiveTouch**:\n  - Activa AssistiveTouch.\n  - **Sensibilidad del seguimiento**: 35% - 40% (deslizador hacia la tortuga).\n  - **Acciones personalizadas**: Tocar dos veces: *Ninguno*.\n* Ve a **Control del Puntero**:\n  - **Velocidad de desplazamiento**: 60%.\n  - **Tamaño del puntero**: Medio.\n  - **Color**: Ninguno o Blanco.\n\n#### 2. 🎯 Switch Control (Control por Botón)\n* Ve a **Ajustes > Accesibilidad > Control por botón**:\n  - **Ciclos**: 4\n  - **Tiempo de exploración automática**: 0.05s\n  - **Modo de cursor**: Refinado\n  - **Presión de cursor refinado**: 85 DPI\n\n#### 3. 🔥 Sensibilidad dentro del Juego (Free Fire / COD Mobile)\n* **General**: 78\n* **Mira de Punto Rojo**: 72\n* **Mira 2X**: 68\n* **Mira 4X**: 62\n* **Francotirador (AWM)**: 35\n* **Cámara 360**: 60\n\n> 💡 **Consejo Pro**: Mantén la pantalla limpia y utiliza un protector mate o cerámico para optimizar el deslizamiento táctil.",
                ContentCategory.SENSITIVITIES,
                iosOnly,
                false,
                "/videos/iphone-confi-sensi-baja-v1.mp4",
                1
        );

        // 2. iPhone Sensi Media v1 (Video)
        upsertContent(
                "iphone-config-sensi-media-v1",
                "CONFIG & SENSI MEDIA COMPLETA - IPHONE v1",
                "Calibración balanceada para iPhone. Equilibrio perfecto entre agilidad de giro 360° y levantamiento suave de mira.",
                "### 📱 Configuración & Sensibilidad Media Completa — iPhone (v1)\n\nAjuste balanceado para jugadores agresivos y de soporte con respuesta táctil fluida:\n\n#### 1. ⚙️ Ajustes del Sistema iOS\n* Ve a **Ajustes > Accesibilidad > Tocar > AssistiveTouch**:\n  - **Sensibilidad del seguimiento**: 70% (3/4 hacia la liebre).\n  - **Tolerancia al movimiento**: Media.\n* Ve a **Control del Puntero**:\n  - **Velocidad de desplazamiento**: 85%.\n  - **Ocultar puntero automáticamente**: 2.0s.\n\n#### 2. 🎯 Switch Control (Control por Botón)\n* Ve a **Ajustes > Accesibilidad > Control por botón**:\n  - **Ciclos**: 8\n  - **Modo de cursor**: Refinado\n  - **Presión de cursor refinado**: 105 DPI\n\n#### 3. 🔥 Sensibilidad dentro del Juego\n* **General**: 92\n* **Mira de Punto Rojo**: 88\n* **Mira 2X**: 84\n* **Mira 4X**: 80\n* **Francotirador**: 45\n* **Cámara 360**: 80",
                ContentCategory.SENSITIVITIES,
                iosOnly,
                false,
                "/videos/iphone-confi-sensi-media-v1.mp4",
                2
        );

        // 3. iPhone Sensi Media v2 (Video - OLED & 120Hz)
        upsertContent(
                "iphone-config-sensi-media-v2",
                "CONFIG & SENSI MEDIA COMPLETA - IPHONE v2 (OLED & 120Hz)",
                "Calibración media avanzada v2 optimizada para pantallas Super Retina XDR y ProMotion 120Hz (iPhone 12 al 16 Pro Max).",
                "### 📱 Configuración & Sensibilidad Media Avanzada — iPhone (v2)\n\nDiseñada específicamente para dispositivos iPhone con paneles OLED de alta tasa de muestreo táctil:\n\n#### 1. ⚙️ Ajustes del Sistema iOS\n* Ve a **Ajustes > Accesibilidad > Tocar > AssistiveTouch**:\n  - **Sensibilidad del seguimiento**: 80% (hacia la liebre).\n  - **Tolerancia al movimiento**: Mínima (para respuesta instantánea).\n* Ve a **Pantalla y brillo**:\n  - **Frecuencia de cuadros**: Asegúrate de tener ProMotion activado (120Hz).\n\n#### 2. 🎯 Switch Control (Control por Botón)\n* Ve a **Ajustes > Accesibilidad > Control por botón**:\n  - **Ciclos**: 10\n  - **Modo de cursor**: Refinado\n  - **Presión de cursor refinado**: 115 DPI\n\n#### 3. 🔥 Sensibilidad dentro del Juego\n* **General**: 95\n* **Mira de Punto Rojo**: 91\n* **Mira 2X**: 88\n* **Mira 4X**: 84\n* **Francotirador**: 48\n* **Cámara 360**: 90",
                ContentCategory.SENSITIVITIES,
                iosOnly,
                false,
                "/videos/iphone-confi-sensi-media-v2.mp4",
                3
        );

        // 4. iPhone Sensi Alta v4 / Pro (Includes 4 Sensitivity Presets & Visual Calibration Captures)
        upsertContent(
                "sensibilidad-competitiva-ios-pro",
                "CONFIG & SENSI ALTA COMPLETA - IPHONE v4 (PRO)",
                "Configuración milimétrica de máxima velocidad, respuesta 3D/Haptic Touch instantánea, puntero a 120 DPI y 4 calibraciones in-game certificadas con capturas.",
                "### 📱 Configuración & Sensibilidad Alta Completa — iPhone v4 (PRO)\n\nCalibración extrema utilizada por jugadores profesionales en torneos competitivos para levantamiento rápido de mira, control de dispersión y cero temblor.\n\n---\n\n#### ⚙️ 1. Ajustes del Sistema iOS (AssistiveTouch & Puntero)\n1. Ve a **Ajustes > Accesibilidad > Tocar > AssistiveTouch**:\n   - **Sensibilidad del seguimiento**: 100% (Deslizador completamente hacia la liebre 🐇).\n   - **Tolerancia al movimiento**: Mínima (0px) para respuesta táctil inmediata.\n2. Ve a **Control del puntero**:\n   - **Velocidad de desplazamiento**: 120% (Máxima aceleración).\n   - **Tamaño del puntero**: Mínimo.\n   - **Color**: Ninguno / Blanco.\n3. Ve a **Control por botón (Switch Control)**:\n   - **Ciclos**: 10\n   - **Modo de cursor**: Refinado\n   - **Presión de cursor refinado**: 120 DPI.\n\n---\n\n#### 🎯 2. Presets Oficiales de Sensibilidad In-Game (Free Fire)\n\nA continuación tienes los **4 Presets Milimétricos** calibrados según tu estilo de combate y armas preferidas:\n\n##### 🔹 Preset A — Modo Competitivo Pro (Miras Rápidas 200%)\n* **General**: 178\n* **Mira de Punto Rojo**: 178\n* **Mira 2X**: 200\n* **Mira 4X**: 200\n* **Mira Francotirador**: 150\n* **Botón de Cámara 360°**: 150\n\n##### 🔹 Preset B — Modo AWM & Francotirador Precisión\n* **General**: 190\n* **Mira de Punto Rojo**: 50\n* **Mira 2X**: 100\n* **Mira 4X**: 100\n* **Mira AWM**: 20\n* **Botón de Cámara Libre**: 88\n\n##### 🔹 Preset C — Modo Control de Dispersión (Equilibrado)\n* **General**: 69\n* **Punto Rojo (Red Dot)**: 45\n* **Mira 2X (2x Scope)**: 70\n* **Mira 4X (4x Scope)**: 100\n* **Sniper Scope**: 0\n* **Free Camera Button**: 0\n\n##### 🔹 Preset D — Modo Universal Clásico\n* **General**: 44\n* **Mira de Punto Rojo**: 67\n* **Mira 2X**: 67\n* **Mira 4X**: 67\n* **Mira Francotirador**: 67\n* **Botón de Cámara 360°**: 67\n\n---\n\n#### 📸 3. Galería de Capturas de Pantalla Oficiales\n*Revisa la galería fotográfica a continuación para comparar y aplicar los valores exactos en tu panel de ajustes del juego:*",
                ContentCategory.SENSITIVITIES,
                iosOnly,
                false,
                null,
                4
        );

        // 5. Samsung Sensi Alta v1 (Video)
        upsertContent(
                "samsung-confi-sensi-alta-v1",
                "CONFIG & SENSI ALTA COMPLETA - SAMSUNG GALAXY v1",
                "Ajuste de DPI perfecto para Samsung Galaxy One UI con Game Booster Plus y frecuencia de muestreo táctil aumentada.",
                "### 📱 Pasos para Samsung Galaxy:\n\n1. Activa **Opciones de desarrollador** (7 toques en Número de compilación).\n2. Modifica el **Ancho mínimo (DPI)** a **580** (para serie A) o **640** (para serie S / Ultra).\n3. Ve a **Ajustes > Administración general > Ratón y panel táctil**:\n   - Velocidad del puntero: Rápido (al máximo).\n   - Velocidad de desplazamiento de rueda: Rápido.\n4. Abre **Game Plugins > Game Booster Plus**:\n   - Modo: Máximo rendimiento de FPS (100% calidad, 100% brillo máx).\n5. En Juego: General 99, Punto Rojo 94, Mira 2X 90, Mira 4X 85.",
                ContentCategory.SENSITIVITIES,
                samsungOnly,
                false,
                "/videos/samsung-confi-sensi-alta-v1.mp4",
                5
        );

        // 6. Motorola Sensi Alta v1 (Video)
        upsertContent(
                "motorola-confi-sensi-alta-v1",
                "CONFIG & SENSI ALTA COMPLETA - MOTOROLA v1",
                "Calibración de DPI y sensibilidad pura para Motorola Moto Edge y serie G con Moto Gametime optimizado.",
                "### 📱 Pasos para Motorola Moto Series:\n\n1. Abre **Ajustes > Sistema > Opciones para desarrolladores**.\n2. Establece el **Ancho más pequeño (DPI)** en **596 DPI**.\n3. En **Moto Gametime**: activa Modo Alto Rendimiento y Bloqueo de Toques Involuntarios.\n4. En Juego: General 97, Punto Rojo 93, Mira 2X 89, Mira 4X 86.",
                ContentCategory.SENSITIVITIES,
                motoOnly,
                false,
                "/videos/motorola-confi-sensi-alta-v1.mp4",
                6
        );

        // 7. Xiaomi / POCO Sensi HyperOS (Video)
        Set<DeviceType> xiaomiOnly = Set.of(DeviceType.XIAOMI);
        upsertContent(
                "xiaomi-hyperos-game-turbo-v1",
                "CONFIG & SENSI ALTA COMPLETA - XIAOMI / POCO (HyperOS & Game Turbo)",
                "Calibración de respuesta táctil a 480Hz, Game Turbo y optimización de DPI para MIUI e HyperOS.",
                "### 📱 Pasos para Xiaomi / Redmi / POCO:\n\n1. Ve a **Ajustes > Ajustes adicionales > Opciones de desarrollador**.\n2. Establece el **Ancho más pequeño (DPI)** en **600 DPI** o **640 DPI** (POCO X3/X6 Pro/F5).\n3. Desactiva la **Optimización MIUI / HyperOS** sólo si experimentas caídas de frames.\n4. Abre **Seguridad > Game Turbo**:\n   - Ajustes adicionales > Free Fire / Tu juego:\n   - Respuesta táctil: 100% (Deslizador al máximo).\n   - Sensibilidad a toques continuos: 100%.\n   - Área resistente al tacto: Ninguna (Ningún borde).\n5. En Juego: General 98, Punto Rojo 94, Mira 2X 90, Mira 4X 86.",
                ContentCategory.SENSITIVITIES,
                xiaomiOnly,
                false,
                "/videos/modificacionx.mp4",
                7
        );

        // 8. Modificaciones con Video (Buffer 16M)
        upsertContent(
                "ajustes-secretos-desarrollador-buffer-16m",
                "Ajustes Secretos del Desarrollador y Registro del Sistema (Buffer 16M)",
                "Elimina el stuttering y el retardo de cuadros ajustando el tamaño del búfer de registro y las capas de depuración GPU.",
                "### 🛠️ Parámetros recomendados:\n\n- **Tamaño de búfer de registro (Logger Buffer Size)**: 16M (Máxima reducción de lag y latencia táctil).\n- **Controlador de gráficos (Game Driver)**: Selecciona tus juegos favoritos y asígnalos a 'Controlador de gráficos del sistema' en vez del predeterminado.\n- **Desactivar superposiciones HW**: Activado (Fuerza a la GPU a encargarse siempre del renderizado de pantalla).\n- **Forzar 4x MSAA**: Habilitado en procesadores Bionic / Snapdragon de gama media y alta.",
                ContentCategory.MODIFICATIONS,
                null,
                true,
                "/videos/modificacionx.mp4",
                1
        );

        // 8. Optimizaciones universales
        upsertContent(
                "optimizacion-ram-tasa-refresco-120hz",
                "Optimización de RAM y Aceleración de Tasa de Refresco a 120Hz/90Hz",
                "Libera recursos críticos en segundo plano, reduce la latencia de entrada táctil y fuerza la tasa de refresco más fluida.",
                "### ⚡ Instrucciones Generales:\n\n1. Desactiva las animaciones del sistema en Opciones de desarrollador:\n   - Escala de animación de ventana: 0.5x o desactivada.\n   - Escala de transición: 0.5x.\n   - Escala de duración de animador: 0.5x.\n2. Limita los procesos en segundo plano a máximo **3 procesos**.\n3. Habilita **Forzar 4x MSAA** si dispones de GPU Adreno 600+ o Apple Bionic.\n4. Limpia la memoria caché periódicamente cada 15 días.",
                ContentCategory.OPTIMIZATIONS,
                null,
                true,
                null,
                1
        );

        log.info("Finished seeding/updating video catalog and contents.");
    }

    private void upsertContent(String slug, String title, String summary, String body,
                               ContentCategory category, Set<DeviceType> targetDevices,
                               boolean applyToAllDevices, String videoUrl, int sortOrder) {
        Content content = contentRepository.findBySlug(slug).orElse(new Content());
        content.setSlug(slug);
        content.setTitle(title);
        content.setSummary(summary);
        content.setBody(body);
        content.setCategory(category);
        content.setApplyToAllDevices(applyToAllDevices);
        content.setTargetDevices(applyToAllDevices ? new HashSet<>() : (targetDevices != null ? new HashSet<>(targetDevices) : new HashSet<>()));
        content.setVideoUrl(videoUrl);
        content.setActive(true);
        content.setSortOrder(sortOrder);
        if (content.getCreatedAt() == null) {
            content.setCreatedAt(Instant.now());
        }
        content.setUpdatedAt(Instant.now());
        contentRepository.save(content);
    }
}
