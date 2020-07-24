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
 * Returns value of selected option in <select> element
 * identified by selectId.
 */

function getSelectValue(selectId) {
    const selector = document.getElementById(selectId);
    return selector.options[selector.selectedIndex].value;
}

/*
 * Fetch comments from server and display them in #comments
 */
async function loadComments() {
    const amount = getSelectValue("commentAmount");
    const order = getSelectValue("commentOrder");

    const comments = await fetch(`/commentList?amount=${amount}&order=${order}`);
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

        const deleteLink = document.createElement("a");
        deleteLink.href = "#";
        deleteLink.innerText = "delete";
        deleteLink.className = "delete-link";
        deleteLink.addEventListener("click", (e) => {
            deleteComment(comment.id);
            e.preventDefault();
        });

        commentElement.appendChild(authorElement);
        commentElement.appendChild(deleteLink);
        commentElement.appendChild(textElement);

        container.appendChild(commentElement);
        isFirst = false;
    }
}

/* Delete all comments.
 * Once the comments are deleted, reload the list of comments
 */

async function deleteAllComments() {
    const result = await fetch( "/commentDeleteAll", {
        method: "POST"
    });

    if (!result.ok) {
        console.warn("Failed to delete comments");
    }

    await loadComments();
}

/*
 * Delete a comment identified by its id.
 * Once the comment is deleted, reload the list of comments
 */
async function deleteComment(commentId) {
    const result = await fetch( "/commentDelete", {
        method: "POST",
        body: "id=" + commentId,
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        }
    });

    if (!result.ok) {
        console.warn("Failed to delete comment " + commentId);
    }

    await loadComments();
}