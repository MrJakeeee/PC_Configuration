document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-card-number]").forEach(input => {
        input.addEventListener("input", () => {
            const digits = input.value.replace(/\D/g, "").slice(0, 16);
            input.value = digits.replace(/(\d{4})(?=\d)/g, "$1 ");
        });
    });

    document.querySelectorAll("[data-card-expiry]").forEach(input => {
        input.addEventListener("input", () => {
            const digits = input.value.replace(/\D/g, "").slice(0, 4);
            input.value = digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits;
        });
    });

    document.querySelectorAll("[data-card-cvv]").forEach(input => {
        input.addEventListener("input", () => {
            input.value = input.value.replace(/\D/g, "").slice(0, 4);
        });
    });
});
