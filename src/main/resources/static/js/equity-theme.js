(function () {
    const STORAGE_KEY = 'equity-theme';

    function getPreferredTheme() {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored === 'light' || stored === 'dark') {
            return stored;
        }
        return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }

    function applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        document.documentElement.style.colorScheme = theme;

        document.querySelectorAll('[data-theme-toggle], #themeToggle').forEach((button) => {
            const icon = button.querySelector('[data-theme-icon], #themeIcon');
            const isDark = theme === 'dark';

            button.setAttribute('aria-label', isDark ? 'Switch to light mode' : 'Switch to dark mode');
            button.setAttribute('aria-pressed', String(isDark));
            button.title = isDark ? 'Light mode' : 'Dark mode';

            if (icon) {
                icon.className = isDark ? 'fas fa-sun' : 'fas fa-moon';
            }
        });
    }

    function toggleTheme() {
        const current = document.documentElement.getAttribute('data-theme') || getPreferredTheme();
        const next = current === 'dark' ? 'light' : 'dark';
        localStorage.setItem(STORAGE_KEY, next);
        applyTheme(next);
    }

    function initThemeControls() {
        const theme = getPreferredTheme();
        applyTheme(theme);

        document.querySelectorAll('[data-theme-toggle], #themeToggle').forEach((button) => {
            if (button.dataset.themeBound === 'true') {
                return;
            }
            button.dataset.themeBound = 'true';
            button.addEventListener('click', toggleTheme);
        });
    }

    window.EquityTheme = {
        initThemeControls,
        applyTheme,
        toggleTheme
    };

    document.addEventListener('DOMContentLoaded', initThemeControls, { once: true });
})();
