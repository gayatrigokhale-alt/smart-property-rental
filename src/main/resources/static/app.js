// =====================================================
// SMART PROPERTY RENTAL - FRONTEND
// =====================================================


// =====================================================
// SESSION
// =====================================================

let currentUser = {
    role: null,
    userId: null,
    email: null,
    name: null
};


// =====================================================
// INITIALIZATION
// =====================================================

document.addEventListener("DOMContentLoaded", function () {

    const savedRole = sessionStorage.getItem("role");
    const savedUserId = sessionStorage.getItem("userId");
    const savedEmail = sessionStorage.getItem("email");
    const savedName = sessionStorage.getItem("name");

    if (savedRole && savedUserId) {

        currentUser.role = savedRole;
        currentUser.userId = savedUserId;
        currentUser.email = savedEmail;
        currentUser.name = savedName;

        showDashboard();

    } else {

        showLogin();

    }
});


// =====================================================
// LOGIN
// =====================================================

async function login() {

    const role = document.getElementById("role").value;
    const email = document.getElementById("loginEmail").value.trim();
    const password = document.getElementById("loginPassword").value;

    const message = document.getElementById("loginMessage");

    message.textContent = "";
    message.className = "message";

    if (!email || !password) {
        message.textContent = "Please enter email and password.";
        message.classList.add("error");
        return;
    }

    let endpoint;

    if (role === "tenant") {
        endpoint = "/api/tenants/login";
    } else if (role === "landlord") {
        endpoint = "/api/landlords/login";
    } else {
        endpoint = "/api/admin/login";
    }

    try {

        const response = await fetch(endpoint, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                email: email,
                password: password
            })

        });

        const data = await response.json();

        if (!response.ok) {

            message.textContent =
                data.error || "Invalid email or password.";

            message.classList.add("error");

            return;
        }


        // ---------------------------------------------
        // SAVE LOGIN INFORMATION
        // ---------------------------------------------

        currentUser.role = role;
        currentUser.email = data.email;

        if (role === "tenant") {

            currentUser.userId = data.tenantId;

        } else if (role === "landlord") {

            currentUser.userId = data.landlordId;

        } else {

            currentUser.userId = data.adminId;

        }

        currentUser.name =
            `${data.firstName || ""} ${data.lastName || ""}`.trim();


        sessionStorage.setItem("role", currentUser.role);
        sessionStorage.setItem("userId", currentUser.userId);
        sessionStorage.setItem("email", currentUser.email);
        sessionStorage.setItem("name", currentUser.name);


        showDashboard();

    } catch (error) {

        console.error(error);

        message.textContent =
            "Unable to connect to the server.";

        message.classList.add("error");
    }
}


// =====================================================
// DASHBOARD DISPLAY
// =====================================================

function showDashboard() {

    document.getElementById("loginPage").classList.add("hidden");
    document.getElementById("mainPage").classList.remove("hidden");

    document.getElementById("welcomeText").textContent =
        `Welcome, ${currentUser.name || "User"}`;


    // Hide all dashboards first

    document.getElementById("tenantDashboard")
        .classList.add("hidden");

    document.getElementById("landlordDashboard")
        .classList.add("hidden");

    document.getElementById("adminDashboard")
        .classList.add("hidden");


    // Show correct dashboard

    if (currentUser.role === "tenant") {

        document.getElementById("tenantDashboard")
            .classList.remove("hidden");

        loadTenantDashboard();

    }

    else if (currentUser.role === "landlord") {

        document.getElementById("landlordDashboard")
            .classList.remove("hidden");

        loadLandlordDashboard();

    }

    else {

        document.getElementById("adminDashboard")
            .classList.remove("hidden");

        document.getElementById("adminName").textContent =
            currentUser.name;

    }
}


// =====================================================
// LOGIN PAGE
// =====================================================

function showLogin() {

    document.getElementById("loginPage")
        .classList.remove("hidden");

    document.getElementById("mainPage")
        .classList.add("hidden");
}


// =====================================================
// LOGOUT
// =====================================================

function logout() {

    sessionStorage.clear();

    currentUser = {
        role: null,
        userId: null,
        email: null,
        name: null
    };

    document.getElementById("loginEmail").value = "";
    document.getElementById("loginPassword").value = "";

    showLogin();
}


// =====================================================
// COMMON FETCH
// =====================================================

async function apiFetch(url, options = {}) {

    if (!options.headers) {
        options.headers = {};
    }

    options.headers["Content-Type"] = "application/json";


    if (
        currentUser.userId &&
        (
            currentUser.role === "tenant" ||
            currentUser.role === "landlord"
        )
    ) {

        options.headers["X-User-Id"] =
            currentUser.userId;

    }


    const response = await fetch(url, options);

    const responseText = await response.text();

    let data = null;

    try {

        data = responseText
            ? JSON.parse(responseText)
            : null;

    } catch (e) {

        // Backend returned plain text instead of JSON.
        data = responseText;

    }


    if (!response.ok) {

        let errorMessage;

        if (typeof data === "string") {

            errorMessage = data;

        } else {

            errorMessage =
                data?.error ||
                data?.message ||
                `Request failed (${response.status})`;

        }

        throw new Error(errorMessage);
    }

    return data;
}


// =====================================================
// TENANT DASHBOARD
// =====================================================

async function loadTenantDashboard() {

    await loadTenantProfile();
    await loadTenantApplications();
    await loadTenantLeases();

}


// =====================================================
// TENANT PROFILE
// =====================================================

async function loadTenantProfile() {

    const container =
        document.getElementById("tenantProfile");

    container.innerHTML = "Loading...";

    try {

        const data = await apiFetch(
            `/api/tenants/profile/${encodeURIComponent(currentUser.email)}`,
            {
                method: "GET"
            }
        );

        container.innerHTML = `

            <div class="profile-item">
                <span class="profile-label">Tenant ID</span>
                <span class="profile-value">${safe(data.tenantId)}</span>
            </div>

            <div class="profile-item">
                <span class="profile-label">Name</span>
                <span class="profile-value">
                    ${safe(data.firstName)} ${safe(data.lastName)}
                </span>
            </div>

            <div class="profile-item">
                <span class="profile-label">Email</span>
                <span class="profile-value">${safe(data.email)}</span>
            </div>

            <div class="profile-item">
                <span class="profile-label">Phone</span>
                <span class="profile-value">${safe(data.phone)}</span>
            </div>

            <div class="profile-item">
                <span class="profile-label">Gender</span>
                <span class="profile-value">${safe(data.gender)}</span>
            </div>

            <div class="profile-item">
                <span class="profile-label">Occupation</span>
                <span class="profile-value">${safe(data.occupation)}</span>
            </div>

            <div class="profile-item">
                <span class="profile-label">Age</span>
                <span class="profile-value">${safe(data.age)}</span>
            </div>

            <div class="profile-item">
                <span class="profile-label">Tenant Type</span>
                <span class="profile-value">${safe(data.tenantType)}</span>
            </div>

        `;

    } catch (error) {

        container.innerHTML =
            `<div class="error">${safe(error.message)}</div>`;

    }
}


// =====================================================
// SEARCH PROPERTIES
// =====================================================

async function searchProperties() {

    const container =
        document.getElementById("propertyResults");

    container.innerHTML =
        `<div class="loading">Searching...</div>`;

    try {

        const properties = await apiFetch(
            `/api/properties/search/${currentUser.userId}`,
            {
                method: "GET"
            }
        );

        if (!properties || properties.length === 0) {

            container.innerHTML = `
                <div class="empty">
                    No properties currently match your preferences.
                </div>
            `;

            return;
        }


        container.innerHTML =
            properties.map(property =>
                createPropertyCard(property)
            ).join("");


    } catch (error) {

        container.innerHTML =
            `<div class="error">${safe(error.message)}</div>`;

    }
}


// =====================================================
// PROPERTY CARD
// =====================================================

function createPropertyCard(property) {

    return `

        <div class="property-card">

            <h3>
                ${safe(property.propertyId)}
                - ${safe(property.propertyType)}
            </h3>

            <div class="property-location">
                📍 ${safe(property.location)},
                ${safe(property.city)}
            </div>

            <div class="property-details">

                <div class="detail">
                    <strong>BHK</strong><br>
                    ${safe(property.bhk)}
                </div>

                <div class="detail">
                    <strong>Area</strong><br>
                    ${safe(property.areaSqft)} sqft
                </div>

                <div class="detail">
                    <strong>Bathrooms</strong><br>
                    ${safe(property.numberOfBathrooms)}
                </div>

                <div class="detail">
                    <strong>Floor</strong><br>
                    ${safe(property.floorNumber)}
                </div>

                <div class="detail">
                    <strong>Furnishing</strong><br>
                    ${safe(property.furnishingStatus)}
                </div>

                <div class="detail">
                    <strong>Available</strong><br>
                    ${safe(property.availableFrom)}
                </div>

            </div>

            <div class="rent">
                ₹${formatNumber(property.monthlyRent)}
                <span>/ month</span>
            </div>

            <button
                class="primary-btn apply-btn"
                onclick="applyForProperty('${safe(property.propertyId)}')"
            >
                Apply for Property
            </button>

        </div>

    `;
}


// =====================================================
// APPLY FOR PROPERTY
// =====================================================

async function applyForProperty(propertyId) {

    const occupants =
        prompt(
            "Enter number of occupants:",
            "1"
        );

    if (!occupants) {
        return;
    }

    const moveInDate =
        prompt(
            "Enter preferred move-in date (YYYY-MM-DD):",
            ""
        );

    if (!moveInDate) {
        return;
    }


    const application = {

        tenantId: currentUser.userId,

        propertyId: propertyId,

        numberOfOccupants:
            Number(occupants),

        proposedMoveInDate:
        moveInDate

    };


    try {

        await apiFetch(
            "/api/applications",
            {
                method: "POST",

                body: JSON.stringify(application)
            }
        );


        alert(
            "Application submitted successfully!"
        );

        await loadTenantApplications();

    } catch (error) {

        alert(
            "Application failed: " +
            error.message
        );
    }
}


// =====================================================
// TENANT APPLICATIONS
// =====================================================

async function loadTenantApplications() {

    const container =
        document.getElementById("tenantApplications");

    container.innerHTML = "Loading...";

    try {

        const applications = await apiFetch(
            `/api/applications/tenant/${currentUser.userId}`,
            {
                method: "GET"
            }
        );

        if (!applications || applications.length === 0) {

            container.innerHTML =
                `<div class="empty">No applications found.</div>`;

            return;
        }


        container.innerHTML = `

            <div class="table-wrapper">

                <table class="data-table">

                    <thead>

                        <tr>
                            <th>Application</th>
                            <th>Property</th>
                            <th>Applied On</th>
                            <th>Move-In Date</th>
                            <th>Occupants</th>
                            <th>Status</th>
                        </tr>

                    </thead>

                    <tbody>

                        ${applications.map(app => `

                            <tr>

                                <td>
                                    ${safe(app.applicationId)}
                                </td>

                                <td>
                                    ${safe(app.propertyId)}
                                </td>

                                <td>
                                    ${safe(app.applicationDate)}
                                </td>

                                <td>
                                    ${safe(app.proposedMoveInDate)}
                                </td>

                                <td>
                                    ${safe(app.numberOfOccupants)}
                                </td>

                                <td>
                                    ${statusBadge(app.applicationStatus)}
                                </td>

                            </tr>

                        `).join("")}

                    </tbody>

                </table>

            </div>

        `;

    } catch (error) {

        container.innerHTML =
            `<div class="error">${safe(error.message)}</div>`;

    }
}


// =====================================================
// TENANT LEASES
// =====================================================

async function loadTenantLeases() {

    const container =
        document.getElementById("tenantLeases");

    container.innerHTML = "Loading...";

    try {

        const leases = await apiFetch(
            `/api/leases/tenant/${currentUser.userId}`,
            {
                method: "GET"
            }
        );

        if (!leases || leases.length === 0) {

            container.innerHTML =
                `<div class="empty">No leases found.</div>`;

            return;
        }


        container.innerHTML = `

            <div class="table-wrapper">

                <table class="data-table">

                    <thead>

                        <tr>
                            <th>Lease</th>
                            <th>Application</th>
                            <th>Property</th>
                            <th>Start</th>
                            <th>End</th>
                            <th>Rent</th>
                            <th>Status</th>
                        </tr>

                    </thead>

                    <tbody>

                        ${leases.map(lease => `

                            <tr>

                                <td>${safe(lease.leaseId)}</td>

                                <td>${safe(lease.applicationId)}</td>

                                <td>${safe(lease.propertyId)}</td>

                                <td>${safe(lease.startDate)}</td>

                                <td>${safe(lease.endDate)}</td>

                                <td>
                                    ₹${formatNumber(lease.monthlyRent)}
                                </td>

                                <td>
                                    ${statusBadge(lease.leaseStatus)}
                                </td>

                            </tr>

                        `).join("")}

                    </tbody>

                </table>

            </div>

        `;

    } catch (error) {

        container.innerHTML =
            `<div class="error">${safe(error.message)}</div>`;

    }
}


// =====================================================
// LANDLORD DASHBOARD
// =====================================================

async function loadLandlordDashboard() {

    await loadLandlordProfile();
    await loadLandlordProperties();
    await loadLandlordApplications();
    await loadLandlordLeases();

}
// =====================================================
// LANDLORD PROFILE
// =====================================================

async function loadLandlordProfile() {

    const container =
        document.getElementById("landlordProfile");

    container.innerHTML = "Loading...";

    try {

        const data = await apiFetch(
            `/api/landlords/${encodeURIComponent(currentUser.userId)}`,
            {
                method: "GET"
            }
        );

        container.innerHTML = `

            <div class="profile-item">
                <span class="profile-label">Landlord ID</span>
                <span class="profile-value">
                    ${safe(data.landlordId)}
                </span>
            </div>

            <div class="profile-item">
                <span class="profile-label">Name</span>
                <span class="profile-value">
                    ${safe(data.firstName)}
                    ${safe(data.lastName)}
                </span>
            </div>

            <div class="profile-item">
                <span class="profile-label">Email</span>
                <span class="profile-value">
                    ${safe(data.email)}
                </span>
            </div>

            <div class="profile-item">
                <span class="profile-label">Phone</span>
                <span class="profile-value">
                    ${safe(data.phone)}
                </span>
            </div>

            <div class="profile-item">
                <span class="profile-label">Verification Status</span>
                <span class="profile-value">
                    ${safe(data.verificationStatus)}
                </span>
            </div>

        `;

    } catch (error) {

        container.innerHTML =
            `<div class="error">${safe(error.message)}</div>`;

    }
}

// =====================================================
// LANDLORD PROPERTIES
// =====================================================

async function loadLandlordProperties() {

    const container =
        document.getElementById("landlordProperties");

    container.innerHTML = "Loading...";

    try {

        const properties = await apiFetch(
            `/api/properties/landlord/${currentUser.userId}`,
            {
                method: "GET"
            }
        );

        if (!properties || properties.length === 0) {

            container.innerHTML =
                `<div class="empty">No properties found.</div>`;

            return;
        }


        container.innerHTML =
            properties.map(property =>
                createLandlordPropertyCard(property)
            ).join("");


    } catch (error) {

        container.innerHTML =
            `<div class="error">${safe(error.message)}</div>`;

    }
}


// =====================================================
// LANDLORD PROPERTY CARD
// =====================================================

function createLandlordPropertyCard(property) {

    return `

        <div class="property-card">

            <h3>
                ${safe(property.propertyId)}
                - ${safe(property.propertyType)}
            </h3>

            <div class="property-location">
                📍 ${safe(property.location)},
                ${safe(property.city)}
            </div>

            <div class="property-details">

                <div class="detail">
                    <strong>BHK</strong><br>
                    ${safe(property.bhk)}
                </div>

                <div class="detail">
                    <strong>Area</strong><br>
                    ${safe(property.areaSqft)} sqft
                </div>

                <div class="detail">
                    <strong>Bathrooms</strong><br>
                    ${safe(property.numberOfBathrooms)}
                </div>

                <div class="detail">
                    <strong>Floor</strong><br>
                    ${safe(property.floorNumber)}
                </div>

                <div class="detail">
                    <strong>Furnishing</strong><br>
                    ${safe(property.furnishingStatus)}
                </div>

                <div class="detail">
                    <strong>Status</strong><br>
                    ${safe(property.propertyStatus)}
                </div>

            </div>

            <div class="rent">
                ₹${formatNumber(property.monthlyRent)}
                <span>/ month</span>
            </div>

        </div>

    `;
}


// =====================================================
// LANDLORD APPLICATIONS
// =====================================================

async function loadLandlordApplications() {

    const container =
        document.getElementById("landlordApplications");

    container.innerHTML = "Loading...";

    try {

        const properties = await apiFetch(
            `/api/properties/landlord/${currentUser.userId}`,
            {
                method: "GET"
            }
        );


        let allApplications = [];


        for (const property of properties) {

            try {

                const applications = await apiFetch(
                    `/api/applications/property/${property.propertyId}`,
                    {
                        method: "GET"
                    }
                );

                allApplications =
                    allApplications.concat(applications);

            } catch (error) {

                console.error(
                    `Could not load applications for ${property.propertyId}`,
                    error
                );

            }

        }


        if (allApplications.length === 0) {

            container.innerHTML =
                `<div class="empty">No applications found.</div>`;

            return;
        }


        container.innerHTML = `

            <div class="table-wrapper">

                <table class="data-table">

                    <thead>

                        <tr>
                            <th>Application</th>
                            <th>Tenant</th>
                            <th>Property</th>
                            <th>Move-In</th>
                            <th>Occupants</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>

                    </thead>

                    <tbody>

                        ${allApplications.map(app => `

                            <tr>

                                <td>
                                    ${safe(app.applicationId)}
                                </td>

                                <td>
                                    ${safe(app.tenantId)}
                                </td>

                                <td>
                                    ${safe(app.propertyId)}
                                </td>

                                <td>
                                    ${safe(app.proposedMoveInDate)}
                                </td>

                                <td>
                                    ${safe(app.numberOfOccupants)}
                                </td>

                                <td>
                                    ${statusBadge(app.applicationStatus)}
                                </td>

                                <td>

                                    ${
            app.applicationStatus === "Pending"

                ?

                `

                                        <div class="action-row">

                                            <button
                                                class="approve-btn"
                                                onclick="updateApplicationStatus(
                                                    '${safe(app.applicationId)}',
                                                    'Approved'
                                                )"
                                            >
                                                Approve
                                            </button>

                                            <button
                                                class="reject-btn"
                                                onclick="updateApplicationStatus(
                                                    '${safe(app.applicationId)}',
                                                    'Rejected'
                                                )"
                                            >
                                                Reject
                                            </button>

                                        </div>

                                        `

                :

                "—"
        }

                                </td>

                            </tr>

                        `).join("")}

                    </tbody>

                </table>

            </div>

        `;

    } catch (error) {

        container.innerHTML =
            `<div class="error">${safe(error.message)}</div>`;

    }
}


// =====================================================
// UPDATE APPLICATION STATUS
// =====================================================

async function updateApplicationStatus(
    applicationId,
    status
) {

    const confirmed =
        confirm(
            `Are you sure you want to mark ${applicationId} as ${status}?`
        );

    if (!confirmed) {
        return;
    }


    try {

        await apiFetch(
            `/api/applications/${applicationId}/status`,
            {
                method: "PUT",

                body: JSON.stringify({
                    applicationStatus: status
                })
            }
        );


        alert(
            `Application ${status.toLowerCase()} successfully.`
        );


        await loadLandlordApplications();

    } catch (error) {

        alert(
            "Could not update application: " +
            error.message
        );

    }
}


// =====================================================
// LANDLORD LEASES
// =====================================================

async function loadLandlordLeases() {

    const container =
        document.getElementById("landlordLeases");

    container.innerHTML = "Loading...";

    try {

        const properties = await apiFetch(
            `/api/properties/landlord/${currentUser.userId}`,
            {
                method: "GET"
            }
        );


        let allLeases = [];


        for (const property of properties) {

            try {

                const leases = await apiFetch(
                    `/api/leases/property/${property.propertyId}`,
                    {
                        method: "GET"
                    }
                );

                allLeases =
                    allLeases.concat(leases);

            } catch (error) {

                console.error(
                    `Could not load leases for ${property.propertyId}`,
                    error
                );

            }

        }


        if (allLeases.length === 0) {

            container.innerHTML =
                `<div class="empty">No leases found.</div>`;

            return;
        }


        container.innerHTML = `

            <div class="table-wrapper">

                <table class="data-table">

                    <thead>

                        <tr>
                            <th>Lease</th>
                            <th>Application</th>
                            <th>Tenant</th>
                            <th>Property</th>
                            <th>Start</th>
                            <th>End</th>
                            <th>Rent</th>
                            <th>Status</th>
                        </tr>

                    </thead>

                    <tbody>

                        ${allLeases.map(lease => `

                            <tr>

                                <td>${safe(lease.leaseId)}</td>

                                <td>${safe(lease.applicationId)}</td>

                                <td>${safe(lease.tenantId)}</td>

                                <td>${safe(lease.propertyId)}</td>

                                <td>${safe(lease.startDate)}</td>

                                <td>${safe(lease.endDate)}</td>

                                <td>
                                    ₹${formatNumber(lease.monthlyRent)}
                                </td>

                                <td>
                                    ${statusBadge(lease.leaseStatus)}
                                </td>

                            </tr>

                        `).join("")}

                    </tbody>

                </table>

            </div>

        `;

    } catch (error) {

        container.innerHTML =
            `<div class="error">${safe(error.message)}</div>`;

    }
}


// =====================================================
// UTILITY FUNCTIONS
// =====================================================

function formatNumber(value) {

    if (value === null || value === undefined) {
        return "—";
    }

    return Number(value).toLocaleString("en-IN");
}


function safe(value) {

    if (value === null || value === undefined || value === "") {
        return "—";
    }

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}


function statusBadge(status) {

    if (!status) {
        return "—";
    }

    const normalized =
        status.toLowerCase();

    let className =
        "status";

    if (normalized === "approved") {
        className += " status-approved";
    }

    else if (normalized === "pending") {
        className += " status-pending";
    }

    else if (normalized === "rejected") {
        className += " status-rejected";
    }

    else if (normalized === "withdrawn") {
        className += " status-withdrawn";
    }

    else if (normalized === "active") {
        className += " status-active";
    }

    else if (normalized === "completed") {
        className += " status-completed";
    }

    return `
        <span class="${className}">
            ${safe(status)}
        </span>
    `;
}