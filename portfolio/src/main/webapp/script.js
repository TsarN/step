"use strict";

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

/*
 * Fetch comments from server and display them in #comments
 */
async function loadComments() {
    const amountSelector = document.getElementById("commentAmount");
    const amount = amountSelector.options[amountSelector.selectedIndex].value;

    const comments = await fetch("/commentList?amount=" + amount);
    const container = document.getElementById("comments");
    container.innerHTML = "";
    let isFirst = true;

    for (const comment of await comments.json()) {
        if (!isFirst) {
            const separator = document.createElement("hr");
            separator.className = "comment-separator";
            container.append(separator);
        }
        const commentElement = document.createElement("div");
        commentElement.className = "comment";
        
        const authorElement = document.createElement("div");
        authorElement.className = "light";
        authorElement.innerText = (comment.author || "unknown") + " writes:";

        const textElement = document.createElement("div");
        textElement.innerText = comment.text;

        commentElement.appendChild(authorElement);
        commentElement.appendChild(textElement);

        container.appendChild(commentElement);
        isFirst = false;
    }
}