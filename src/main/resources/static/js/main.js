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

    const deviceData = {
        'iphone': {
            title: 'iPhone & iOS',
            subtitle: 'Rendimiento y estabilidad garantizada con calibración táctil de alta precisión.',
            icon: 'fab fa-apple'
        },
        'samsung': {
            title: 'Samsung Galaxy',
            subtitle: 'Optimizaciones de One UI, Game Booster avanzado y calibración de latencia de pantalla.',
            icon: 'fas fa-mobile-alt'
        },
        'android': {
            title: 'Android Universal (Xiaomi, POCO, etc.)',
            subtitle: 'Ajustes de memoria virtual, aceleración gráfica por GPU y configuraciones de DPI.',
            icon: 'fab fa-android'
        }
    };

    if (deviceTabs.length > 0) {
        deviceTabs.forEach(tab => {
            tab.addEventListener('click', () => {
                deviceTabs.forEach(t => t.classList.remove('active'));
                tab.classList.add('active');

                const brand = tab.getAttribute('data-brand') || 'iphone';
                const data = deviceData[brand];
                if (data && deviceTitle && deviceSubtitle && deviceIcon) {
                    deviceTitle.textContent = data.title;
                    deviceSubtitle.textContent = data.subtitle;
                    deviceIcon.className = data.icon;
                }
            });
        });
    }
});
