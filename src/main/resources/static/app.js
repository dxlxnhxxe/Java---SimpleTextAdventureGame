// STAG Web Client Application

const state = {
    gameId: null,
    playerName: localStorage.getItem("stag_user") || "Alice",
    currentLocation: "cabin",
    health: 3,
    inventory: [],
    availablePaths: [],
    availableArtefacts: [],
    availableExtendedCommands: [],
    history: [],
    historyIndex: -1,
    stompClient: null,
    locationSubscription: null,
    authToken: localStorage.getItem("stag_jwt") || null,
    authEmail: localStorage.getItem("stag_user") || null,
    authRole: localStorage.getItem("stag_role") || null,
    authMode: "login" // "login" or "register"
};

// DOM Elements
const terminalOutput = document.getElementById("terminal-output");
const terminalScreen = document.getElementById("terminal-screen");
const commandForm = document.getElementById("command-form");
const commandInput = document.getElementById("command-input");
const promptLabel = document.getElementById("prompt-label");

const hudPlayerName = document.getElementById("hud-player-name");
const hudLocation = document.getElementById("hud-location");
const hudInventory = document.getElementById("hud-inventory");
const displayGameId = document.getElementById("display-game-id");

// Modals & Controls
const sessionModal = document.getElementById("session-modal");
const saveModal = document.getElementById("save-modal");
const authModal = document.getElementById("auth-modal");
const btnSwitchGame = document.getElementById("btn-switch-game");
const btnSaveGame = document.getElementById("btn-save-game");
const btnAuth = document.getElementById("btn-auth");
const closeModalBtn = document.getElementById("close-modal-btn");
const closeSaveModalBtn = document.getElementById("close-save-modal-btn");
const closeAuthModalBtn = document.getElementById("close-auth-modal-btn");
const btnCreateGame = document.getElementById("btn-create-game");
const btnJoinGame = document.getElementById("btn-join-game");
const btnConfirmSave = document.getElementById("btn-confirm-save");
const savedGamesList = document.getElementById("saved-games-list");
const saveStatusMsg = document.getElementById("save-status-msg");

// Auth Elements
const tabLogin = document.getElementById("tab-login");
const tabRegister = document.getElementById("tab-register");
const authForm = document.getElementById("auth-form");
const authEmail = document.getElementById("auth-email");
const authPassword = document.getElementById("auth-password");
const btnAuthSubmit = document.getElementById("btn-auth-submit");
const authStatusMsg = document.getElementById("auth-status-msg");
const authLoggedInPanel = document.getElementById("auth-logged-in-panel");
const authCurrentUserDisplay = document.getElementById("auth-current-user-display");
const authCurrentRoleDisplay = document.getElementById("auth-current-role-display");
const btnLogoutSubmit = document.getElementById("btn-logout-submit");

// Initialize on Load
document.addEventListener("DOMContentLoaded", async () => {
    setupEventListeners();
    updateHUD();
    await initializeDefaultGame();
});

function getAuthHeaders() {
    const headers = { "Content-Type": "application/json" };
    if (state.authToken) {
        headers["Authorization"] = `Bearer ${state.authToken}`;
    }
    return headers;
}

function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/[&<>'"]/g, 
        tag => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            "'": '&#39;',
            '"': '&quot;'
        }[tag] || tag)
    );
}

function setupEventListeners() {
    commandForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const cmd = commandInput.value.trim();
        if (!cmd) return;

        state.history.push(cmd);
        state.historyIndex = state.history.length;
        commandInput.value = "";

        appendEcho(cmd);
        await executeCommand(cmd);
    });

    // History Navigation via Up/Down keys
    commandInput.addEventListener("keydown", (e) => {
        if (e.key === "ArrowUp") {
            if (state.historyIndex > 0) {
                state.historyIndex--;
                commandInput.value = state.history[state.historyIndex];
            }
            e.preventDefault();
        } else if (e.key === "ArrowDown") {
            if (state.historyIndex < state.history.length - 1) {
                state.historyIndex++;
                commandInput.value = state.history[state.historyIndex];
            } else {
                state.historyIndex = state.history.length;
                commandInput.value = "";
            }
            e.preventDefault();
        }
    });

    // Initial render of toolbar buttons
    renderQuickActionButtons();

    // Modal Triggers
    btnSwitchGame.addEventListener("click", () => {
        openSessionModal();
    });

    closeModalBtn.addEventListener("click", () => {
        sessionModal.classList.remove("active");
    });

    btnSaveGame.addEventListener("click", () => {
        saveStatusMsg.textContent = "";
        saveModal.classList.add("active");
    });

    closeSaveModalBtn.addEventListener("click", () => {
        saveModal.classList.remove("active");
    });

    btnCreateGame.addEventListener("click", async () => {
        const name = document.getElementById("new-game-name").value.trim() || "New Realm";
        await createNewGame(name);
        sessionModal.classList.remove("active");
    });

    btnJoinGame.addEventListener("click", async () => {
        const gameId = document.getElementById("join-game-id").value.trim();
        const player = document.getElementById("join-player-name").value.trim() || state.playerName || "Alice";
        if (gameId) {
            await joinGame(gameId, player);
            sessionModal.classList.remove("active");
        }
    });

    btnConfirmSave.addEventListener("click", async () => {
        const slot = document.getElementById("save-slot-input").value.trim() || "checkpoint-1";
        await saveCurrentGame(slot);
    });

    // Auth Listeners
    btnAuth.addEventListener("click", () => {
        openAuthModal();
    });

    closeAuthModalBtn.addEventListener("click", () => {
        authModal.classList.remove("active");
    });

    tabLogin.addEventListener("click", () => {
        setAuthMode("login");
    });

    tabRegister.addEventListener("click", () => {
        setAuthMode("register");
    });

    authForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        await handleAuthSubmit();
    });

    btnLogoutSubmit.addEventListener("click", () => {
        handleLogout();
    });
}

function openAuthModal() {
    authStatusMsg.textContent = "";
    if (state.authToken && state.authEmail) {
        authLoggedInPanel.style.display = "block";
        authForm.style.display = "none";
        tabLogin.parentElement.style.display = "none";
        authCurrentUserDisplay.textContent = state.authEmail;
        authCurrentRoleDisplay.textContent = state.authRole || "ROLE_USER";
        document.getElementById("auth-modal-title").textContent = "AUTHENTICATED AGENT";
    } else {
        authLoggedInPanel.style.display = "none";
        authForm.style.display = "block";
        tabLogin.parentElement.style.display = "flex";
        document.getElementById("auth-modal-title").textContent = "AGENT IDENTIFICATION";
        setAuthMode("login");
        authEmail.value = "";
        authPassword.value = "";
    }
    authModal.classList.add("active");
}

function setAuthMode(mode) {
    state.authMode = mode;
    authStatusMsg.textContent = "";
    if (mode === "login") {
        tabLogin.classList.add("active");
        tabRegister.classList.remove("active");
        btnAuthSubmit.textContent = "SIGN IN";
        btnAuthSubmit.style.background = "var(--border-color)";
    } else {
        tabRegister.classList.add("active");
        tabLogin.classList.remove("active");
        btnAuthSubmit.textContent = "CREATE ACCOUNT";
        btnAuthSubmit.style.background = "#286638";
    }
}

async function handleAuthSubmit() {
    const email = authEmail.value.trim();
    const password = authPassword.value;

    if (!email) {
        authStatusMsg.style.color = "var(--terminal-red)";
        authStatusMsg.textContent = "Please enter an email address or identifier.";
        return;
    }
    if (!password || password.length < 4) {
        authStatusMsg.style.color = "var(--terminal-red)";
        authStatusMsg.textContent = "Password must be at least 4 characters.";
        return;
    }

    const endpoint = state.authMode === "login" ? "/api/v1/auth/login" : "/api/v1/auth/register";

    try {
        authStatusMsg.style.color = "var(--terminal-cyan)";
        authStatusMsg.textContent = "Encrypting & verifying credentials...";

        const res = await fetch(endpoint, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username: email, email: email, password: password })
        });

        if (!res.ok) {
            let errorText = "Authentication failed";
            try {
                const errJson = await res.json();
                errorText = errJson.message || errJson.error || errorText;
            } catch (_) {
                errorText = await res.text() || errorText;
            }
            authStatusMsg.style.color = "var(--terminal-red)";
            authStatusMsg.textContent = `${errorText}`;
            return;
        }

        const data = await res.json();
        state.authToken = data.token;
        state.authEmail = data.username;
        state.authRole = data.role;
        state.playerName = data.username.split("@")[0] || data.username;

        localStorage.setItem("stag_jwt", data.token);
        localStorage.setItem("stag_user", data.username);
        localStorage.setItem("stag_role", data.role);

        authStatusMsg.style.color = "var(--terminal-green)";
        authStatusMsg.textContent = state.authMode === "login" 
            ? `✓ Signed in successfully as ${data.username}` 
            : `✓ Account registered and authenticated!`;

        appendSystemLine(`[SECURITY] Authenticated as [${data.username}] (Stateless JWT active)`);
        updateHUD();

        setTimeout(() => {
            authModal.classList.remove("active");
        }, 1200);

    } catch (err) {
        authStatusMsg.style.color = "var(--terminal-red)";
        authStatusMsg.textContent = "Network error: " + err.message;
    }
}

function handleLogout() {
    const prevUser = state.authEmail || state.playerName;
    state.authToken = null;
    state.authEmail = null;
    state.authRole = null;
    state.playerName = "Alice";

    localStorage.removeItem("stag_jwt");
    localStorage.removeItem("stag_user");
    localStorage.removeItem("stag_role");

    appendSystemLine(`[SECURITY] Disconnected session for [${prevUser}]. Operating in Guest mode.`);
    updateHUD();
    authModal.classList.remove("active");
}

// REST API Calls
async function initializeDefaultGame() {
    try {
        appendSystemLine("Transporting you to alternate dimension...");
        const res = await fetch("/api/v1/games", {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify({ gameName: "The Old Manor", template: "extended" })
        });
        const game = await res.json();
        state.gameId = game.gameId;
        if (!state.authToken) {
            state.playerName = "Alice";
        } else if (state.authEmail) {
            state.playerName = state.authEmail.split("@")[0] || state.authEmail;
        }

        await joinGame(state.gameId, state.playerName);
        connectWebSocket();
    } catch (err) {
        appendErrorLine("Failed to connect to backend: " + err.message);
    }
}

async function createNewGame(name) {
    try {
        const res = await fetch("/api/v1/games", {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify({ gameName: name, template: "extended" })
        });
        const game = await res.json();
        state.gameId = game.gameId;
        await joinGame(state.gameId, state.playerName);
        connectWebSocket();
        appendSystemLine(`Created new game session [${game.gameId}]: ${name}`);
    } catch (err) {
        appendErrorLine("Error creating game: " + err.message);
    }
}

async function joinGame(gameId, playerName) {
    try {
        state.gameId = gameId;
        state.playerName = playerName;

        const res = await fetch(`/api/v1/games/${gameId}/join`, {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify({ playerName: playerName })
        });
        const joinData = await res.json();

        state.currentLocation = joinData.currentLocation;
        state.health = joinData.health;
        state.inventory = joinData.inventory || [];
        state.availablePaths = joinData.availablePaths || [];
        state.availableArtefacts = joinData.availableArtefacts || [];
        state.availableExtendedCommands = joinData.availableExtendedCommands || [];

        updateHUD();
        renderQuickActionButtons();
        appendSystemLine(`Joined game session ${gameId} as [${playerName}] at location: ${state.currentLocation.toUpperCase()}`);
    } catch (err) {
        appendErrorLine("Error joining game: " + err.message);
    }
}

async function executeCommand(cmd) {
    if (!state.gameId) {
        appendErrorLine("No active game session. Please join or create a game.");
        return;
    }

    try {
        const res = await fetch(`/api/v1/games/${state.gameId}/command`, {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify({ playerName: state.playerName, command: cmd })
        });

        if (!res.ok) {
            const errBody = await res.text();
            appendErrorLine("Server error: " + errBody);
            return;
        }

        const data = await res.json();
        const oldLocation = state.currentLocation;

        state.currentLocation = data.currentLocation || state.currentLocation;
        state.health = data.health !== undefined ? data.health : state.health;
        state.inventory = data.inventory || [];
        state.availablePaths = data.availablePaths || [];
        state.availableArtefacts = data.availableArtefacts || [];
        state.availableExtendedCommands = data.availableExtendedCommands || [];

        updateHUD();
        renderQuickActionButtons();
        appendNarrative(data.narrative);

        // If location changed, re-subscribe to websocket location topic
        if (oldLocation !== state.currentLocation && state.stompClient && state.stompClient.connected) {
            subscribeLocationTopic();
        }
    } catch (err) {
        appendErrorLine("Command execution failed: " + err.message);
    }
}

async function saveCurrentGame(slotName) {
    if (!state.gameId) return;
    try {
        const res = await fetch(`/api/v1/games/${state.gameId}/save`, {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify({ saveSlotName: slotName })
        });
        const data = await res.json();
        saveStatusMsg.textContent = `✓ Saved checkpoint successfully: ${slotName}`;
        appendSystemLine(`Game state saved to PostgreSQL checkpoint: [${slotName}]`);
        setTimeout(() => saveModal.classList.remove("active"), 1200);
    } catch (err) {
        saveStatusMsg.textContent = "Failed to save: " + err.message;
    }
}

async function openSessionModal() {
    sessionModal.classList.add("active");
    try {
        const res = await fetch("/api/v1/games/saves", {
            headers: getAuthHeaders()
        });
        if (res.ok) {
            const saves = await res.json();
            savedGamesList.innerHTML = "";
            if (saves.length === 0) {
                savedGamesList.innerHTML = "<em>No saved checkpoints found.</em>";
            } else {
                saves.forEach(s => {
                    const item = document.createElement("div");
                    item.className = "save-slot-item";
                    item.innerHTML = `<strong>${s.saveSlotName}</strong> <span>Game: ${s.gameId}</span>`;
                    item.addEventListener("click", async () => {
                        await loadSavedGame(s.saveSlotName);
                        sessionModal.classList.remove("active");
                    });
                    savedGamesList.appendChild(item);
                });
            }
        }
    } catch (e) {
        console.error(e);
    }
}

async function loadSavedGame(slotName) {
    try {
        const res = await fetch(`/api/v1/games/load/${encodeURIComponent(slotName)}`, {
            method: "POST",
            headers: getAuthHeaders()
        });
        if (res.ok) {
            const session = await res.json();
            state.gameId = session.gameId;
            appendSystemLine(`✓ Restored checkpoint: [${slotName}]`);
            await joinGame(state.gameId, state.playerName);
            connectWebSocket();
        }
    } catch (e) {
        appendErrorLine("Failed to load checkpoint: " + e.message);
    }
}

// WebSocket & STOMP Real-Time Synchronization
function connectWebSocket() {
    try {
        if (typeof SockJS === "undefined" || typeof Stomp === "undefined") {
            return;
        }
        const socket = new SockJS("/ws-game");
        state.stompClient = Stomp.over(socket);
        state.stompClient.debug = null; // suppress STOMP verbose logs

        state.stompClient.connect({}, () => {
            subscribeLocationTopic();
            state.stompClient.subscribe(`/topic/games/${state.gameId}/global`, (msg) => {
                const event = JSON.parse(msg.body);
                appendBroadcastLine(`[BROADCAST] ${event.message || event}`);
            });
        }, (err) => {
            console.log("WebSocket connecting or fallback:", err);
        });
    } catch (err) {
        console.log("WebSocket setup info:", err);
    }
}

function subscribeLocationTopic() {
    if (!state.stompClient || !state.stompClient.connected || !state.gameId) return;

    if (state.locationSubscription) {
        state.locationSubscription.unsubscribe();
    }

    const topic = `/topic/games/${state.gameId}/locations/${state.currentLocation}`;
    state.locationSubscription = state.stompClient.subscribe(topic, (msg) => {
        const event = JSON.parse(msg.body);
        if (event.player && event.player !== state.playerName) {
            appendBroadcastLine(`${event.message}`);
        }
    });
}

function renderQuickActionButtons() {
    const toolbar = document.getElementById("quick-toolbar");
    if (!toolbar) return;

    toolbar.innerHTML = "";

    // 1. Standard utility buttons
    const standardActions = [
        { label: "LOOK", cmd: "look" },
        { label: "INVENTORY", cmd: "inventory" },
        { label: "HEALTH", cmd: "health" }
    ];

    standardActions.forEach(action => {
        const btn = document.createElement("button");
        btn.className = "cmd-btn";
        btn.textContent = action.label;
        btn.setAttribute("data-cmd", action.cmd);
        btn.addEventListener("click", async () => {
            commandInput.value = "";
            appendEcho(action.cmd);
            await executeCommand(action.cmd);
        });
        toolbar.appendChild(btn);
    });

    // 2. Dynamic Location Paths (separate button for each available exit)
    if (state.availablePaths && state.availablePaths.length > 0) {
        state.availablePaths.forEach(path => {
            const btn = document.createElement("button");
            btn.className = "cmd-btn cmd-btn-nav";
            btn.textContent = `GOTO ${path.toUpperCase()}`;
            const cmd = `goto ${path}`;
            btn.setAttribute("data-cmd", cmd);
            btn.addEventListener("click", async () => {
                commandInput.value = "";
                appendEcho(cmd);
                await executeCommand(cmd);
            });
            toolbar.appendChild(btn);
        });
    }

    // 3. Dynamic Artefacts available in the current location
    if (state.availableArtefacts && state.availableArtefacts.length > 0) {
        state.availableArtefacts.forEach(item => {
            const btn = document.createElement("button");
            btn.className = "cmd-btn cmd-btn-item";
            btn.textContent = `GET ${item.toUpperCase()}`;
            const cmd = `get ${item}`;
            btn.setAttribute("data-cmd", cmd);
            btn.addEventListener("click", async () => {
                commandInput.value = "";
                appendEcho(cmd);
                await executeCommand(cmd);
            });
            toolbar.appendChild(btn);
        });
    }

    // 4. Dynamic Extended Actions available in current context
    if (state.availableExtendedCommands && state.availableExtendedCommands.length > 0) {
        state.availableExtendedCommands.forEach(actionCmd => {
            const btn = document.createElement("button");
            btn.className = "cmd-btn cmd-btn-action";
            btn.textContent = `${actionCmd.toUpperCase()}`;
            btn.setAttribute("data-cmd", actionCmd);
            btn.addEventListener("click", async () => {
                commandInput.value = "";
                appendEcho(actionCmd);
                await executeCommand(actionCmd);
            });
            toolbar.appendChild(btn);
        });
    }
}

function updateHUD() {
    if (state.authToken && state.authEmail) {
        hudPlayerName.innerHTML = `<span class="highlight">${escapeHtml(state.playerName)}</span> <span style="font-size: 0.72rem; color: var(--terminal-cyan);">[AUTH]</span>`;
        btnAuth.textContent = `${state.authEmail.split("@")[0]}`;
        btnAuth.title = `Authenticated as ${state.authEmail} (Click to manage)`;
        btnAuth.style.borderColor = "var(--terminal-cyan)";
        btnAuth.style.color = "var(--terminal-cyan)";
    } else {
        hudPlayerName.textContent = state.playerName || "Guest";
        btnAuth.textContent = "SIGN IN";
        btnAuth.title = "Sign in or register account";
        btnAuth.style.borderColor = "var(--border-color)";
        btnAuth.style.color = "var(--terminal-green)";
    }

    hudLocation.textContent = state.currentLocation;
    displayGameId.textContent = state.gameId || "NONE";
    promptLabel.textContent = `${state.playerName.toLowerCase()}@${state.currentLocation.toLowerCase()} >`;

    // Inventory Chips
    hudInventory.innerHTML = "";
    if (!state.inventory || state.inventory.length === 0) {
        hudInventory.innerHTML = '<span class="empty-inv">EMPTY</span>';
    } else {
        state.inventory.forEach(item => {
            const chip = document.createElement("span");
            chip.className = "inv-chip";
            chip.textContent = item;
            chip.title = `Click to drop ${item}`;
            chip.addEventListener("click", async () => {
                appendEcho(`drop ${item}`);
                await executeCommand(`drop ${item}`);
            });
            hudInventory.appendChild(chip);
        });
    }
}

function appendEcho(cmd) {
    const div = document.createElement("div");
    div.className = "terminal-line user-echo";
    div.textContent = `${state.playerName.toLowerCase()}@${state.currentLocation.toLowerCase()} > ${cmd}`;
    terminalOutput.appendChild(div);
    scrollToBottom();
}

function appendNarrative(text) {
    const div = document.createElement("div");
    div.className = "terminal-line narrative-msg";
    div.textContent = text;
    terminalOutput.appendChild(div);
    scrollToBottom();
}

function appendSystemLine(text) {
    const div = document.createElement("div");
    div.className = "terminal-line system-msg";
    div.textContent = `[SYSTEM] ${text}`;
    terminalOutput.appendChild(div);
    scrollToBottom();
}

function appendBroadcastLine(text) {
    const div = document.createElement("div");
    div.className = "terminal-line broadcast-msg";
    div.textContent = text;
    terminalOutput.appendChild(div);
    scrollToBottom();
}

function appendErrorLine(text) {
    const div = document.createElement("div");
    div.className = "terminal-line error-msg";
    div.textContent = `[ERROR] ${text}`;
    terminalOutput.appendChild(div);
    scrollToBottom();
}

function scrollToBottom() {
    terminalScreen.scrollTop = terminalScreen.scrollHeight;
}
