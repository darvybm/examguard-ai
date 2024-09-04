document.addEventListener("DOMContentLoaded", function () {
    window.addEventListener("load", function () {
        var loadingScreen = document.getElementById('loading-screen');
        var mainContent = document.getElementById('main-content');

        if (loadingScreen) {
            loadingScreen.style.display = 'none'; // Oculta la pantalla de carga
        }

        if (mainContent) {
            mainContent.classList.remove('hidden'); // Muestra el contenido principal
        }
    });
});