// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
 * Display comment data in #comments.
 */
function renderComments(commentData) {
    // Work on a copy of the container, then substitute it for the real one
    // Prevents the comment list from flashing too much
    const origContainer = document.getElementById("comments");
    const container = origContainer.cloneNode();
    container.innerHTML = "";

    let isFirst = true;

    for (const comment of commentData) {
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

    origContainer.parentNode.replaceChild(container, origContainer);
}

/*
 * loadComments():
 * Fetch comments from server and display them in #comments
 */
(() => {
    let requestId = 0;

    window.loadComments = async () => {
        const amount = getSelectValue("commentAmount");
        const order = getSelectValue("commentOrder");
        const currentRequestId = ++requestId;

        const comments = await fetch(`/commentList?amount=${amount}&order=${order}`);
        if (requestId === currentRequestId) {
            // request wasn't interrupted by another call to loadComments()
            renderComments(await comments.json());
        }
    };
})();


/*
 * Submit the comment form and then reload the list of comments
 */

async function submitComment() {
    const form = document.getElementById("commentForm");
    const body = new URLSearchParams();
    for (const [name, value] of new FormData(form)) {
        body.append(name, value);
    }

    // clear the form to dissuade temptation to spam comments
    document.getElementById("commentField").innerText = "";

    await fetch("/commentPost", {
        method: "POST",
        body,
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        }
    });

    await loadComments();
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
        console.error("Failed to delete comment " + commentId);
    }

    await loadComments();
}

async function updateAuthInfo() {
    const result = await fetch("/auth");

    if (!result.ok) {
        console.error("Failed to update auth info");
    }

    const user = await result.json();

    if (user["loggedIn"]) {
        document.getElementById("logoutPrompt").style.display = "";
        document.getElementById("commentForm").style.display = "";
        document.getElementById("logoutLink").href = user["logoutUrl"];
        document.getElementById("usernameDisplay").innerText = user["email"];
        document.getElementById("authorField").value = user["nickname"];
    } else {
        document.getElementById("loginPrompt").style.display = "";
        document.getElementById("loginLink").href = user["loginUrl"];
    }
}

/*
 * This function is called once after the page is loaded.
 */
async function init() {
    await updateAuthInfo();
    await loadComments();
}