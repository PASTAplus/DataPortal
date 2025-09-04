document.addEventListener('DOMContentLoaded', () => {
    const triggers = document.querySelectorAll('.lightbox-trigger');

    triggers.forEach(trigger => {
        trigger.addEventListener('click', (event) => {
            event.preventDefault(); // Stop the link from navigating

            const fullSizeImageSrc = event.currentTarget.getAttribute('href');

            // Create the overlay and the image element
            const overlay = document.createElement('div');
            overlay.className = 'lightbox-overlay';

            const img = document.createElement('img');
            img.src = fullSizeImageSrc;

            // Append the image to the overlay and the overlay to the body
            overlay.appendChild(img);
            document.body.appendChild(overlay);

            // Handle closing the lightbox
            overlay.addEventListener('click', () => {
                document.body.removeChild(overlay);
            });
        });
    });
});
