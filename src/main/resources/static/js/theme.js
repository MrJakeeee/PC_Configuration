(() => {
    const storageKey = "pc-theme";
    const saved = localStorage.getItem(storageKey);
    const prefersDark = window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
    const initialTheme = saved || (prefersDark ? "dark" : "light");

    document.documentElement.dataset.theme = initialTheme;

    document.addEventListener("DOMContentLoaded", () => {
        const button = document.querySelector("[data-theme-toggle]");
        const icon = document.querySelector("[data-theme-icon]");

        function render(theme) {
            document.documentElement.dataset.theme = theme;
            if (icon) {
                icon.textContent = theme === "dark" ? "☀" : "☾";
            }
        }

        render(initialTheme);

        if (button) {
            button.addEventListener("click", () => {
                const nextTheme = document.documentElement.dataset.theme === "dark" ? "light" : "dark";
                localStorage.setItem(storageKey, nextTheme);
                render(nextTheme);
            });
        }
    });
})();
