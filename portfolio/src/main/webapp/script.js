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

class CommentWidget {
    constructor(user) {
        this.user = user;
        this.requestId = 0;

        document.getElementById("commentOrder")
            .addEventListener("change", e => { this.load(); });

        document.getElementById("commentAmount")
            .addEventListener("change", e => { this.load(); });

        if (user["loggedIn"]) {
            document.getElementById("commentForm")
                .addEventListener("submit", e => {
                    this.submit();
                    e.preventDefault();
                });
        }

        if (user["isAdmin"]) {
            document.getElementById("deleteAllComments")
                .addEventListener("click", e => { this.deleteAll(); });
        }
    }

    /*
     * Display comment data in #comments.
     */
    renderComments(commentData) {
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
            commentElement.appendChild(authorElement);

            if (this.user["loggedIn"] && (this.user["isAdmin"] || this.user["id"] === comment["authorId"])) {
                const deleteLink = document.createElement("a");
                deleteLink.href = "#";
                deleteLink.innerText = "delete";
                deleteLink.className = "delete-link";
                deleteLink.addEventListener("click", (e) => {
                    this.deleteOne(comment.id);
                    e.preventDefault();
                });
                commentElement.appendChild(deleteLink);
            }

            const textElement = document.createElement("div");
            textElement.innerText = comment.text;
            commentElement.appendChild(textElement);

            container.appendChild(commentElement);
            isFirst = false;
        }

        origContainer.parentNode.replaceChild(container, origContainer);
    }

    /*
     * Fetch comments from server and display them in #comments
     */
    async load() {
        const amount = getSelectValue("commentAmount");
        const order = getSelectValue("commentOrder");
        const currentRequestId = ++this.requestId;

        const comments = await fetch(`/commentList?amount=${amount}&order=${order}`);
        if (this.requestId === currentRequestId) {
            // request wasn't interrupted by another call to loadComments()
            this.renderComments(await comments.json());
        }
    }


    /*
     * Submit the comment form and then reload the list of comments
     */

    async submit() {
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

        await this.load();
    }

    /* Delete all comments.
     * Once the comments are deleted, reload the list of comments
     */

    async deleteAll() {
        const result = await fetch("/commentDeleteAll", {
            method: "POST"
        });

        if (!result.ok) {
            console.warn("Failed to delete comments");
        }

        await this.load();
    }

    /*
     * Delete a comment identified by its id.
     * Once the comment is deleted, reload the list of comments
     */
    async deleteOne(commentId) {
        const result = await fetch("/commentDelete", {
            method: "POST",
            body: "id=" + commentId,
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            }
        });

        if (!result.ok) {
            console.error("Failed to delete comment " + commentId);
        }

        await this.load();
    }
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

        if (user["isAdmin"]) {
            document.getElementById("deleteAllComments").style.display = "";
        }
    } else {
        document.getElementById("loginPrompt").style.display = "";
        document.getElementById("loginLink").href = user["loginUrl"];
    }

    return user;
}

async function init() {
    const comments = new CommentWidget(await updateAuthInfo());
    await comments.load();
}