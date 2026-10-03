/* ===================================================================
   GRINGO SEXY — Main Vanilla JavaScript Interaction Engine
   =================================================================== */

document.addEventListener('DOMContentLoaded', () => {
    // 1. Mobile Menu Toggle
    const hamburgerBtn = document.querySelector('#hamburgerBtn');
    const mobileNavLinks = document.querySelector('#navLinks');

    if (hamburgerBtn && mobileNavLinks) {
        hamburgerBtn.addEventListener('click', () => {
            mobileNavLinks.classList.toggle('active');
            const icon = hamburgerBtn.querySelector('i');
            if (icon) {
                if (mobileNavLinks.classList.contains('active')) {
                    icon.classList.remove('fa-bars');
                    icon.classList.add('fa-times');
                } else {
                    icon.classList.remove('fa-times');
                    icon.classList.add('fa-bars');
                }
            }
        });
    }

    // 2. Dashboard / Admin Sidebar Toggle for Mobile
    const sidebarToggleBtn = document.querySelector('#sidebarToggleBtn');
    const appSidebar = document.querySelector('#appSidebar');
    const sidebarOverlay = document.querySelector('#sidebarOverlay');

    if (sidebarToggleBtn && appSidebar) {
        sidebarToggleBtn.addEventListener('click', () => {
            appSidebar.classList.toggle('open');
            if (sidebarOverlay) {
                sidebarOverlay.classList.toggle('active');
            }
        });
    }

    if (sidebarOverlay && appSidebar) {
        sidebarOverlay.addEventListener('click', () => {
            appSidebar.classList.remove('open');
            sidebarOverlay.classList.remove('active');
        });
    }

    // 3. Dismiss Alerts and Auto-hide Toasts
    const toastElements = document.querySelectorAll('.toast');
    toastElements.forEach(toast => {
        setTimeout(() => {
            toast.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            setTimeout(() => toast.remove(), 400);
        }, 5000);
    });

    // 4. Modal Triggers & Dynamic Openers
    const modalTriggers = document.querySelectorAll('[data-modal-target]');
    modalTriggers.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.getAttribute('data-modal-target');
            const modal = document.getElementById(targetId);
            if (modal) {
                modal.style.display = 'flex';
                
                // If editing device
                if (btn.dataset.deviceId) {
                    const form = modal.querySelector('form');
                    if (form) {
                        form.querySelector('#deviceId').value = btn.dataset.deviceId;
                        form.querySelector('#deviceName').value = btn.dataset.deviceName || '';
                        form.querySelector('#deviceCode').value = btn.dataset.deviceCode || 'OTHER';
                        form.querySelector('#deviceDesc').value = btn.dataset.deviceDesc || '';
                        form.querySelector('#deviceIcon').value = btn.dataset.deviceIcon || 'fas fa-mobile-alt';
                        form.querySelector('#deviceOrder').value = btn.dataset.deviceOrder || '0';
                        modal.querySelector('#deviceModalTitle').textContent = 'Editar Dispositivo';
                    }
                } else if (targetId === 'deviceModal') {
                    // Reset device modal for creation
                    const form = modal.querySelector('form');
                    if (form) {
                        form.reset();
                        form.querySelector('#deviceId').value = '';
                        modal.querySelector('#deviceModalTitle').textContent = 'Añadir Nuevo Dispositivo';
                    }
                }

                // If editing category
                if (btn.dataset.categoryId) {
                    const form = modal.querySelector('form');
                    if (form) {
                        form.querySelector('#categoryId').value = btn.dataset.categoryId;
                        form.querySelector('#categoryName').value = btn.dataset.categoryName || '';
                        form.querySelector('#categorySlug').value = btn.dataset.categorySlug || '';
                        form.querySelector('#categoryDesc').value = btn.dataset.categoryDesc || '';
                        form.querySelector('#categoryIcon').value = btn.dataset.categoryIcon || 'fas fa-folder';
                        form.querySelector('#categoryOrder').value = btn.dataset.categoryOrder || '0';
                        modal.querySelector('#categoryModalTitle').textContent = 'Editar Categoría';
                    }
                } else if (targetId === 'categoryModal') {
                    const form = modal.querySelector('form');
                    if (form) {
                        form.reset();
                        form.querySelector('#categoryId').value = '';
                        modal.querySelector('#categoryModalTitle').textContent = 'Añadir Nueva Categoría';
                    }
                }
            }
        });
    });

    const modalCloseButtons = document.querySelectorAll('[data-modal-close]');
    modalCloseButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const modal = btn.closest('.modal-backdrop');
            if (modal) {
                modal.style.display = 'none';
            }
        });
    });

    // Close modal when clicking outside dialog
    document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
        backdrop.addEventListener('click', (e) => {
            if (e.target === backdrop) {
                backdrop.style.display = 'none';
            }
        });
    });

    // 5. Global confirmation handler for destructive forms
    const confirmForms = document.querySelectorAll('form[data-confirm]');
    confirmForms.forEach(form => {
        form.addEventListener('submit', (e) => {
            const msg = form.getAttribute('data-confirm') || '¿Estás seguro de realizar esta acción?';
            if (!window.confirm(msg)) {
                e.preventDefault();
            }
        });
    });

    // 6. Device Compatibility Tab Switcher (Landing Page)
    const deviceTabs = document.querySelectorAll('.device-tab-btn');
    const deviceTitle = document.querySelector('#compatDeviceTitle');
    const deviceSubtitle = document.querySelector('#compatDeviceSubtitle');
    const deviceIcon = document.querySelector('#compatDeviceIcon');
    const compatFeature1 = document.querySelector('#compatFeature1');
    const compatFeature2 = document.querySelector('#compatFeature2');
    const compatFeature3 = document.querySelector('#compatFeature3');

    const deviceData = {
        'samsung': {
            title: 'Samsung Galaxy',
            subtitle: 'Calibración especial One UI, DPI recomendado y Game Booster Plus optimizado para máxima tasa de refresco táctil.',
            icon: 'fas fa-mobile-alt',
            f1: 'DPI óptimo (580 - 640) para One UI',
            f2: 'Optimización de Game Booster y táctil',
            f3: 'Limpieza de RAM y procesos secundarios'
        },
        'motorola': {
            title: 'Motorola Moto Series',
            subtitle: 'Configuración para pantallas Moto 120Hz/144Hz, velocidad de puntero y ajuste de latencia táctil en Edge y Moto G.',
            icon: 'fas fa-mobile-android',
            f1: 'Aceleración táctil y DPI para Moto G & Edge',
            f2: 'GameTime Turbo configurado al 100%',
            f3: 'Estabilidad de FPS en partidas largas'
        },
        'xiaomi': {
            title: 'Xiaomi, Redmi & POCO',
            subtitle: 'Optimización especial para MIUI & HyperOS, Game Turbo avanzado y eliminación del retraso en el panel táctil.',
            icon: 'fas fa-mobile',
            f1: 'Respuesta táctil y sensibilidad Game Turbo',
            f2: 'DPI y aceleración de hardware HyperOS/MIUI',
            f3: 'Bloqueo de thermal throttling y caídas de FPS'
        },
        'realme': {
            title: 'Realme & GT Series',
            subtitle: 'Modo GT al máximo, frecuencia de muestreo táctil instantánea y calibración de puntero para tiros precisos.',
            icon: 'fas fa-bolt',
            f1: 'Modo GT & Ultra Touch Response',
            f2: 'Calibración DPI milimétrica para Realme UI',
            f3: 'Aceleración de gráficos y enfriamiento de CPU'
        },
        'iphone': {
            title: 'iPhone & iOS',
            subtitle: 'Configuración de AssistiveTouch, Switch Control a 120 DPI y velocidad de puntero para iPhone 11 al 16 Pro Max.',
            icon: 'fab fa-apple',
            f1: 'Sensibilidad de seguimiento 100% y 120 DPI',
            f2: 'Respuesta táctil 3D/Haptic Touch calibrada',
            f3: '120 FPS estables sin calentamiento'
        }
    };

    if (deviceTabs.length > 0) {
        deviceTabs.forEach(tab => {
            tab.addEventListener('click', () => {
                deviceTabs.forEach(t => t.classList.remove('active'));
                tab.classList.add('active');

                const brand = tab.getAttribute('data-brand') || 'samsung';
                const data = deviceData[brand];
                if (data) {
                    if (deviceTitle) deviceTitle.textContent = data.title;
                    if (deviceSubtitle) deviceSubtitle.textContent = data.subtitle;
                    if (deviceIcon) deviceIcon.className = data.icon;
                    if (compatFeature1 && data.f1) compatFeature1.textContent = data.f1;
                    if (compatFeature2 && data.f2) compatFeature2.textContent = data.f2;
                    if (compatFeature3 && data.f3) compatFeature3.textContent = data.f3;
                }
            });
        });
    }
});
