/*
 * Open image modal.
 *
 * Arguments:
 *     src: image's URL
 *     altText: image's alt text
 */
function showImageModal(src, altText = "") {
    const modal = document.getElementById("image-modal");
    const img = document.getElementById("image-modal-img");

    modal.style.display = "";
    img.src = src;
    img.alt = altText;
}

/*
 * Close image modal.
 */
function closeImageModal() {
    const modal = document.getElementById("image-modal");
    const img = document.getElementById("image-modal-img");

    modal.style.display = "none";
    img.src = null;
    img.alt = "";
}

/*
 * Makes all <figure> tags containing images clickable,
 * so that when clicked an image modal pops up.
 */
function makeImagesClickable() {
    const imgs = document.querySelectorAll("figure > img");

    for (const img of imgs) {
        img.style.cursor = "pointer";
        img.addEventListener("click", () => {
            showImageModal(img.src, img.altText);
        });
    }
}