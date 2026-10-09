// SESSION
let currentUser = {
    role: null,
    userId: null,
    email: null,
    name: null
};


// INITIALIZATION
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


// LOGIN
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


        const responseText = await response.text();
        let data = null;
        try {
            data = responseText ? JSON.parse(responseText) : null;
        } catch (e) {
            data = responseText;
        }


        if (!response.ok) {
            let errorMessage;
            if (typeof data === "string") {
                errorMessage = data;
            } else {
                errorMessage = data?.error || data?.message || "Invalid email or password.";
            }
            message.textContent = errorMessage;
            message.classList.add("error");
            return;
        }


        // SAVE LOGIN INFORMATION
        currentUser.role = role;
        currentUser.email = data.email;


        if (role === "tenant") {
            currentUser.userId = data.tenantId;
        } else if (role === "landlord") {
            currentUser.userId = data.landlordId;
        } else {
            currentUser.userId = data.adminId;
        }


        currentUser.name = `${data.firstName || ""} ${data.lastName || ""}`.trim();


        sessionStorage.setItem("role", currentUser.role);
        sessionStorage.setItem("userId", currentUser.userId);
        sessionStorage.setItem("email", currentUser.email);
        sessionStorage.setItem("name", currentUser.name);


        currentUser.name =
            `${data.firstName || ""} ${data.lastName || ""}`.trim();


        sessionStorage.setItem("role", currentUser.role);
        sessionStorage.setItem("userId", currentUser.userId);
        sessionStorage.setItem("email", currentUser.email);
        sessionStorage.setItem("name", currentUser.name);




// Check landlord property setup status
        if (currentUser.role === "landlord") {


            const properties = await apiFetch(
                `/api/properties/landlord/${currentUser.userId}`,
                {
                    method: "GET"
                }
            );


            if (!properties || properties.length === 0) {
                showLandlordPropertySetup();
            } else {
                showDashboard();
            }


        } else {
            showDashboard();
        }
    } catch (error) {
        console.error(error);
        message.textContent = "Unable to connect to the server.";
        message.classList.add("error");
    }
}


// DASHBOARD DISPLAY
function showDashboard() {
    document.getElementById("loginPage").classList.add("hidden");


    document.getElementById("tenantRegistrationPage").classList.add("hidden");


    document.getElementById("landlordRegistrationPage").classList.add("hidden");


    document.getElementById("tenantSetupPage").classList.add("hidden");


    document.getElementById("landlordSetupPage").classList.add("hidden");


    document.getElementById("mainPage").classList.remove("hidden");


    document.getElementById("welcomeText").textContent =
        `Welcome, ${currentUser.name || "User"}`;


    // Hide all dashboards first
    document.getElementById("tenantDashboard").classList.add("hidden");
    document.getElementById("landlordDashboard").classList.add("hidden");
    document.getElementById("adminDashboard").classList.add("hidden");


    // Show correct dashboard
    if (currentUser.role === "tenant") {
        document.getElementById("tenantDashboard").classList.remove("hidden");
        loadTenantDashboard();


    } else if (currentUser.role === "landlord") {
        document.getElementById("landlordDashboard").classList.remove("hidden");
        loadLandlordDashboard();


    } else {
        document.getElementById("adminDashboard").classList.remove("hidden");
        loadAdminDashboard();
    }
}


// LOGIN PAGE
function showLogin() {
    document.getElementById("loginPage").classList.remove("hidden");


    document.getElementById("tenantRegistrationPage").classList.add("hidden");


    document.getElementById("landlordRegistrationPage").classList.add("hidden");


    document.getElementById("tenantSetupPage").classList.add("hidden");


    document.getElementById("landlordSetupPage").classList.add("hidden");


    document.getElementById("mainPage").classList.add("hidden");
}


// LOGOUT
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


// COMMON FETCH
async function apiFetch(url, options = {}) {
    if (!options.headers) {
        options.headers = {};
    }
    if (!(options.body instanceof FormData)) {
        options.headers["Content-Type"] = "application/json";
    }


    // Tenant / Landlord authentication
    if (currentUser.userId && (currentUser.role === "tenant" || currentUser.role === "landlord")) {
        options.headers["X-User-Id"] = currentUser.userId;
    }


    // Admin authentication
    if (currentUser.userId && currentUser.role === "admin") {
        options.headers["X-Admin-Id"] = currentUser.userId;
    }


    const response = await fetch(url, options);
    const responseText = await response.text();
    let data = null;


    try {
        data = responseText ? JSON.parse(responseText) : null;
    } catch (e) {
        data = responseText;
    }


    if (!response.ok) {
        let errorMessage;
        if (typeof data === "string") {
            errorMessage = data;
        } else {
            errorMessage = data?.error || data?.message || `Request failed (${response.status})`;
        }
        throw new Error(errorMessage);
    }


    return data;
}


// TENANT REGISTRATION
function showTenantRegistration() {
    document.getElementById("loginPage").classList.add("hidden");
    document.getElementById("mainPage").classList.add("hidden");
    document.getElementById("tenantRegistrationPage").classList.remove("hidden");


    const message = document.getElementById("registrationMessage");
    if (message) {
        message.textContent = "";
        message.className = "message";
    }
}


function hideTenantRegistration() {
    document.getElementById("tenantRegistrationPage").classList.add("hidden");
    document.getElementById("mainPage").classList.add("hidden");
    document.getElementById("loginPage").classList.remove("hidden");


    const message = document.getElementById("registrationMessage");
    if (message) {
        message.textContent = "";
        message.className = "message";
    }
}


async function registerTenant() {

    const registerButton =
        document.getElementById("tenantRegisterButton");

    if (registerButton.disabled) {
        return;
    }

    const message =
        document.getElementById("registrationMessage");

    message.textContent = "";
    message.className = "message";


    const firstName =
        document.getElementById("registerFirstName").value.trim();

    const lastName =
        document.getElementById("registerLastName").value.trim();

    const email =
        document.getElementById("registerEmail").value.trim();

    const phone =
        document.getElementById("registerPhone").value.trim();

    const gender =
        document.getElementById("registerGender").value;

    const occupation =
        document.getElementById("registerOccupation").value.trim();

    const age =
        Number(document.getElementById("registerAge").value);

    const tenantType =
        document.getElementById("registerTenantType").value;

    const hasPets =
        document.getElementById("registerHasPets").value === "true";

    const password =
        document.getElementById("registerPassword").value;


    // BASIC VALIDATION
    if (
        !firstName ||
        !lastName ||
        !email ||
        !phone ||
        !gender ||
        !occupation ||
        !age ||
        !tenantType ||
        !password
    ) {
        message.textContent =
            "Please fill in all required fields.";

        message.classList.add("error");
        return;
    }


    const namePattern =
        /^[A-Za-z]+(?:[ '-][A-Za-z]+)*$/;

    if (!namePattern.test(firstName)) {
        message.textContent =
            "First name can contain only letters.";

        message.classList.add("error");
        return;
    }


    if (!namePattern.test(lastName)) {
        message.textContent =
            "Last name can contain only letters.";

        message.classList.add("error");
        return;
    }


    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        message.textContent =
            "Please enter a valid email address.";

        message.classList.add("error");
        return;
    }


    if (!/^(\+91[- ]?)?[6-9]\d{9}$/.test(phone)) {
        message.textContent =
            "Please enter a valid Indian phone number.";

        message.classList.add("error");
        return;
    }


    if (age < 18 || age > 100) {
        message.textContent =
            "Age must be between 18 and 100.";

        message.classList.add("error");
        return;
    }


    if (password.length < 6) {
        message.textContent =
            "Password must be at least 6 characters.";

        message.classList.add("error");
        return;
    }


    const tenantData = {
        firstName: firstName,
        lastName: lastName,
        email: email,
        phone: phone,
        gender: gender,
        occupation: occupation,
        age: age,
        tenantType: tenantType,
        hasPets: hasPets,
        password: password
    };


    try {

        registerButton.disabled = true;
        registerButton.textContent = "Creating Account...";

        const data =
            await apiFetch("/api/tenants", {
                method: "POST",
                body: JSON.stringify(tenantData)
            });


        message.textContent =
            `Account created successfully! Your Tenant ID is ${data.tenantId}.`;

        message.classList.add("success");


        // CLEAR FORM
        document.getElementById("registerFirstName").value = "";
        document.getElementById("registerLastName").value = "";
        document.getElementById("registerEmail").value = "";
        document.getElementById("registerPhone").value = "";
        document.getElementById("registerGender").value = "";
        document.getElementById("registerOccupation").value = "";
        document.getElementById("registerAge").value = "";
        document.getElementById("registerTenantType").value = "";
        document.getElementById("registerHasPets").value = "false";
        document.getElementById("registerPassword").value = "";


        // RETURN TO LOGIN OR SETUP PREFERENCES
        currentUser.role = "tenant";
        currentUser.userId = data.tenantId;
        currentUser.email = data.email;
        currentUser.name =
            `${data.firstName || ""} ${data.lastName || ""}`.trim();


        sessionStorage.setItem(
            "role",
            currentUser.role
        );

        sessionStorage.setItem(
            "userId",
            currentUser.userId
        );

        sessionStorage.setItem(
            "email",
            currentUser.email
        );

        sessionStorage.setItem(
            "name",
            currentUser.name
        );


        showTenantPreferenceSetup();

    } catch (error) {

        message.textContent =
            error.message;

        message.classList.add("error");

        registerButton.disabled = false;
        registerButton.textContent = "Create Account";
    }
}
// =====================================================
// LANDLORD REGISTRATION
// =====================================================


function showLandlordRegistration() {


    document.getElementById("loginPage").classList.add("hidden");


    document.getElementById("tenantRegistrationPage").classList.add("hidden");


    document.getElementById("landlordSetupPage").classList.add("hidden");


    document.getElementById("mainPage").classList.add("hidden");


    document.getElementById("landlordRegistrationPage")
        .classList.remove("hidden");


    const message =
        document.getElementById("landlordRegistrationMessage");


    if (message) {
        message.textContent = "";
        message.className = "message";
    }
}




function hideLandlordRegistration() {


    document.getElementById("landlordRegistrationPage")
        .classList.add("hidden");


    document.getElementById("mainPage")
        .classList.add("hidden");


    document.getElementById("loginPage")
        .classList.remove("hidden");


    const message =
        document.getElementById("landlordRegistrationMessage");


    if (message) {
        message.textContent = "";
        message.className = "message";
    }
}




async function registerLandlord() {


    const message =
        document.getElementById("landlordRegistrationMessage");


    message.textContent = "";
    message.className = "message";
    const registerButton =
        document.getElementById("landlordRegisterButton");

    if (registerButton.disabled) {
        return;
    }

    const firstName =
        document.getElementById("landlordRegisterFirstName")
            .value.trim();


    const lastName =
        document.getElementById("landlordRegisterLastName")
            .value.trim();


    const email =
        document.getElementById("landlordRegisterEmail")
            .value.trim();


    const phone =
        document.getElementById("landlordRegisterPhone")
            .value.trim();


    const password =
        document.getElementById("landlordRegisterPassword")
            .value;




    if (
        !firstName ||
        !lastName ||
        !email ||
        !phone ||
        !password
    ) {
        message.textContent =
            "Please fill in all required fields.";


        message.classList.add("error");
        return;
    }

    if (!/^(\+91[- ]?)?[6-9]\d{9}$/.test(phone)) {
        message.textContent = "Please enter a valid Indian phone number.";
        message.classList.add("error");
        return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        message.textContent = "Please enter a valid email address.";
        message.classList.add("error");
        return;
    }
    const namePattern = /^[A-Za-z]+(?:[ '-][A-Za-z]+)*$/;
    if (!namePattern.test(firstName)) {
        message.textContent = "First name can contain only letters.";
        message.classList.add("error");
        return;
    }

    if (!namePattern.test(lastName)) {
        message.textContent = "Last name can contain only letters.";
        message.classList.add("error");
        return;
    }


    if (password.length < 6) {


        message.textContent =
            "Password must be at least 6 characters.";


        message.classList.add("error");
        return;
    }




    const landlordData = {


        firstName: firstName,


        lastName: lastName,


        email: email,


        phone: phone,


        password: password
    };




    try {

        registerButton.disabled = true;
        registerButton.textContent = "Creating Account...";

        const data = await apiFetch(
            "/api/landlords/register",
            {
                method: "POST",
                body: JSON.stringify(landlordData)
            }
        );




        // Automatically log the new landlord in


        currentUser.role = "landlord";


        currentUser.userId = data.landlordId;


        currentUser.email = data.email;


        currentUser.name =
            `${data.firstName || ""} ${data.lastName || ""}`
                .trim();




        sessionStorage.setItem(
            "role",
            currentUser.role
        );


        sessionStorage.setItem(
            "userId",
            currentUser.userId
        );


        sessionStorage.setItem(
            "email",
            currentUser.email
        );


        sessionStorage.setItem(
            "name",
            currentUser.name
        );




        // Start property setup


        showLandlordPropertySetup();




     } catch (error) {

    message.textContent =
        error.message;

    message.classList.add("error");

    registerButton.disabled = false;
    registerButton.textContent = "Create Account";
}
}
// TENANT DASHBOARD
async function loadTenantDashboard() {
    await loadTenantProfile();
    await loadTenantPreferences();
    await loadTenantAmenityPreferences();
    await loadAvailableProperties();
    await loadTenantApplications();
    await loadTenantLeases();
}


// TENANT PROFILE
async function loadTenantProfile() {
    const container = document.getElementById("tenantProfile");
    container.innerHTML = "Loading...";


    try {
        const data = await apiFetch(`/api/tenants/profile/${encodeURIComponent(currentUser.email)}`, {
            method: "GET"
        });


        container.innerHTML = `
     <div class="profile-item">
       <span class="profile-label">Tenant ID</span>
       <span class="profile-value">${safe(data.tenantId)}</span>
     </div>
     <div class="profile-item">
       <span class="profile-label">Name</span>
       <span class="profile-value">${safe(data.firstName)} ${safe(data.lastName)}</span>
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
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}


// EDIT TENANT PROFILE
async function showEditTenantProfile() {
    const container = document.getElementById("tenantProfile");
    container.innerHTML = "Loading...";


    try {
        const data = await apiFetch(`/api/tenants/profile/${encodeURIComponent(currentUser.email)}`, {
            method: "GET"
        });


        container.innerHTML = `
     <div class="profile-edit-form">
       <div class="form-group">
         <label>Tenant ID</label>
         <input type="text" value="${safe(data.tenantId)}" disabled>
       </div>
       <div class="form-group">
         <label>First Name</label>
         <input type="text" id="editFirstName" value="${safe(data.firstName)}">
       </div>
       <div class="form-group">
         <label>Last Name</label>
         <input type="text" id="editLastName" value="${safe(data.lastName)}">
       </div>
       <div class="form-group">
         <label>Email</label>
         <input type="email" id="editEmail" value="${safe(data.email)}">
       </div>
       <div class="form-group">
         <label>Phone</label>
         <input type="text" id="editPhone" value="${safe(data.phone)}">
       </div>
       <div class="form-group">
         <label>Gender</label>
         <select id="editGender">
           <option value="Male" ${data.gender === "Male" ? "selected" : ""}>Male</option>
           <option value="Female" ${data.gender === "Female" ? "selected" : ""}>Female</option>
           <option value="Other" ${data.gender === "Other" ? "selected" : ""}>Other</option>
         </select>
       </div>
       <div class="form-group">
         <label>Occupation</label>
         <input type="text" id="editOccupation" value="${safe(data.occupation)}">
       </div>
       <div class="form-group">
         <label>Age</label>
         <input type="number" id="editAge" min="18" max="100" value="${safe(data.age)}">
       </div>
       <div class="form-group">
         <label>Tenant Type</label>
         <select id="editTenantType">
           <option value="Student" ${data.tenantType === "Student" ? "selected" : ""}>Student</option>
           <option value="Working Professional" ${data.tenantType === "Working Professional" ? "selected" : ""}>Working Professional</option>
           <option value="Family" ${data.tenantType === "Family" ? "selected" : ""}>Family</option>
         </select>
       </div>
       <div class="form-group">
         <label>Pets</label>
         <select id="editHasPets">
           <option value="false" ${data.hasPets === false ? "selected" : ""}>No</option>
           <option value="true" ${data.hasPets === true ? "selected" : ""}>Yes</option>
         </select>
       </div>
       <div class="profile-form-actions">
         <button type="button" class="primary-btn" onclick="updateTenantProfile()">Save Changes</button>
         <button type="button" class="secondary-btn" onclick="loadTenantProfile()">Cancel</button>
       </div>
       <p id="tenantProfileMessage" class="message"></p>
     </div>
   `;
    } catch (error) {
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}


// SAVE TENANT PROFILE
async function updateTenantProfile() {
    const message = document.getElementById("tenantProfileMessage");
    message.textContent = "";
    message.className = "message";


    const updatedEmail = document.getElementById("editEmail").value.trim();
    const firstName = document.getElementById("editFirstName").value.trim();
    const lastName = document.getElementById("editLastName").value.trim();
    const phone = document.getElementById("editPhone").value.trim();
    const gender = document.getElementById("editGender").value;
    const occupation = document.getElementById("editOccupation").value.trim();
    const age = Number(document.getElementById("editAge").value);
    const tenantType = document.getElementById("editTenantType").value;
    const hasPets = document.getElementById("editHasPets").value === "true";


    if (!firstName || !lastName || !updatedEmail || !phone || !occupation || !age) {
        message.textContent = "Please fill in all required fields.";
        message.classList.add("error");
        return;
    }
    if (age < 18 || age > 100) {
        message.textContent = "Age must be between 18 and 100.";
        message.classList.add("error");
        return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(updatedEmail)) {
        message.textContent = "Please enter a valid email address.";
        message.classList.add("error");
        return;
    }

    if (!/^(\+91[- ]?)?[6-9]\d{9}$/.test(phone)) {
        message.textContent = "Please enter a valid Indian phone number.";
        message.classList.add("error");
        return;
    }
    const namePattern = /^[A-Za-z]+(?:[ '-][A-Za-z]+)*$/;

    if (!namePattern.test(firstName)) {
        message.textContent = "First name can contain only letters.";
        message.classList.add("error");
        return;
    }

    if (!namePattern.test(lastName)) {
        message.textContent = "Last name can contain only letters.";
        message.classList.add("error");
        return;
    }
    const updatedTenant = {
        firstName: firstName,
        lastName: lastName,
        email: updatedEmail,
        phone: phone,
        gender: gender,
        occupation: occupation,
        age: age,
        tenantType: tenantType,
        hasPets: hasPets
    };


    try {
        const oldEmail = currentUser.email;
        const data = await apiFetch(`/api/tenants/profile/${encodeURIComponent(oldEmail)}`, {
            method: "PUT",
            body: JSON.stringify(updatedTenant)
        });


        currentUser.email = data.email;
        currentUser.name = `${data.firstName || ""} ${data.lastName || ""}`.trim();
        sessionStorage.setItem("email", currentUser.email);
        sessionStorage.setItem("name", currentUser.name);


        document.getElementById("welcomeText").textContent = `Welcome, ${currentUser.name}`;
        alert("Profile updated successfully!");
        await loadTenantProfile();
    } catch (error) {
        message.textContent = error.message;
        message.classList.add("error");
    }
}


// DELETE TENANT ACCOUNT
// =====================================================
// DELETE TENANT ACCOUNT
// =====================================================


async function deleteTenantAccount() {


    const confirmed = confirm(
        "Are you sure you want to permanently delete your account?\n\n" +
        "This action cannot be undone."
    );


    if (!confirmed) {
        return;
    }


    const secondConfirmation = confirm(
        "Please confirm again that you want to delete your tenant account."
    );


    if (!secondConfirmation) {
        return;
    }


    try {


        await apiFetch(
            `/api/tenants/${encodeURIComponent(currentUser.userId)}`,
            {
                method: "DELETE"
            }
        );


        alert(
            "Your tenant account has been deleted successfully."
        );


        // CLEAR SESSION
        sessionStorage.clear();


        currentUser = {
            role: null,
            userId: null,
            email: null,
            name: null
        };


        // RETURN TO LOGIN
        showLogin();


    } catch (error) {


        // Show the exact message returned by the backend
        alert(error.message);
    }
}


// TENANT PREFERENCES
async function loadTenantPreferences() {
    const container = document.getElementById("tenantPreferences");
    container.innerHTML = '<div class="loading">Loading preferences...</div>';


    try {
        const preference = await apiFetch(`/api/tenant-preferences/${currentUser.userId}`, {
            method: "GET"
        });


        container.innerHTML = `
     <div class="profile-grid">
       <div class="profile-item">
         <span class="profile-label">Preferred Location</span>
         <span class="profile-value">${safe(preference.preferredLocation)}</span>
       </div>
       <div class="profile-item">
         <span class="profile-label">Budget</span>
         <span class="profile-value">${formatNumber(preference.minimumBudget)} - ${formatNumber(preference.maximumBudget)}</span>
       </div>
       <div class="profile-item">
         <span class="profile-label">Property Type</span>
         <span class="profile-value">${safe(preference.preferredPropertyType)}</span>
       </div>
       <div class="profile-item">
         <span class="profile-label">BHK</span>
         <span class="profile-value">${safe(preference.preferredBhk)}</span>
       </div>
       <div class="profile-item">
         <span class="profile-label">Minimum Area</span>
         <span class="profile-value">${safe(preference.minimumAreaSqft)} sqft</span>
       </div>
       <div class="profile-item">
         <span class="profile-label">Minimum Bathrooms</span>
         <span class="profile-value">${safe(preference.minimumBathrooms)}</span>
       </div>
       <div class="profile-item">
         <span class="profile-label">Number of Occupants</span>
         <span class="profile-value">${safe(preference.numberOfOccupants)}</span>
       </div>
       <div class="profile-item">
         <span class="profile-label">Furnishing</span>
         <span class="profile-value">${safe(preference.furnishingPreference)}</span>
       </div>
       <div class="profile-item">
         <span class="profile-label">Preferred Floor</span>
         <span class="profile-value">${safe(preference.preferredFloor)}</span>
       </div>
       <div class="profile-item">
         <span class="profile-label">Minimum Lease Duration</span>
         <span class="profile-value">${safe(preference.minimumLeaseDuration)} months</span>
       </div>
       <div class="profile-item">
         <span class="profile-label">Preferred Move-In Date</span>
         <span class="profile-value">${safe(preference.preferredMoveInDate)}</span>
       </div>
     </div>
     <div class="profile-form-actions">
       <button type="button" class="secondary-btn small-btn" onclick="showEditTenantPreferences()">Edit Preferences</button>
     </div>
   `;
    } catch (error) {
        if (
            error.message &&
            (error.message.toLowerCase().includes("preferences not found") ||
                error.message.toLowerCase().includes("request failed (404)"))
        ) {
            container.innerHTML = `
       <div class="empty">
         <p>You have not added your rental preferences yet.</p>
         <button type="button" class="primary-btn small-btn" onclick="showAddTenantPreferences()">Add Preferences</button>
       </div>
     `;
            return;
        }
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}


// ADD TENANT PREFERENCES
function showAddTenantPreferences() {
    const container = document.getElementById("tenantPreferences");
    container.innerHTML = `
   <div class="profile-edit-form">
     <div class="form-group">
       <label>Preferred Location</label>
       <input type="text" id="prefLocation" placeholder="e.g. Kothrud">
     </div>
     <div class="form-group">
       <label>Minimum Budget</label>
       <input type="number" id="prefMinBudget" min="1" placeholder="e.g. 15000">
     </div>
     <div class="form-group">
       <label>Maximum Budget</label>
       <input type="number" id="prefMaxBudget" min="1" placeholder="e.g. 30000">
     </div>
     <div class="form-group">
       <label>Property Type</label>
       <select id="prefPropertyType">
         <option value="">Select property type</option>
         <option value="Apartment">Apartment</option>
         <option value="Villa">Villa</option>
         <option value="Studio">Studio</option>
         <option value="1RK">1RK</option>
       </select>
     </div>
     <div class="form-group">
       <label>BHK</label>
       <select id="prefBhk">
         <option value="">Select BHK</option>
         <option value="1">1 BHK</option>
         <option value="2">2 BHK</option>
         <option value="3">3 BHK</option>
         <option value="4">4 BHK</option>
       </select>
     </div>
     <div class="form-group">
       <label>Minimum Area (sqft)</label>
       <input type="number" id="prefArea" min="0" placeholder="e.g. 800">
     </div>
     <div class="form-group">
       <label>Minimum Bathrooms</label>
       <input type="number" id="prefBathrooms" min="1" max="20" placeholder="e.g. 2">
     </div>
     <div class="form-group">
       <label>Number of Occupants</label>
      <input
    type="number"
    id="prefOccupants"
    min="1"
    max="20"
    oninput="if (this.value > 20) this.value = 20"
    placeholder="e.g. 2">
     </div>
     <div class="form-group">
       <label>Furnishing Preference</label>
       <select id="prefFurnishing">
         <option value="">Select furnishing</option>
         <option value="Fully Furnished">Fully Furnished</option>
         <option value="Semi-Furnished">Semi-Furnished</option>
         <option value="Unfurnished">Unfurnished</option>
       </select>
     </div>
     <div class="form-group">
       <label>Preferred Floor</label>
       <select id="prefFloor">
         <option value="Any">Any Floor</option>
         <option value="Ground">Ground Floor</option>
         <option value="Low">Lower Floor</option>
         <option value="High">Higher Floor</option>
       </select>
     </div>
     <div class="form-group">
       <label>Minimum Lease Duration (months)</label>
       <input type="number" id="prefLease" min="1" placeholder="e.g. 12">
     </div>
     <div class="form-group">
       <label>Preferred Move-In Date</label>
       <input type="date" id="prefMoveIn">
     </div>
     <div class="profile-form-actions">
       <button type="button" class="primary-btn" onclick="createTenantPreferences()">Save Preferences</button>
       <button type="button" class="secondary-btn" onclick="loadTenantPreferences()">Cancel</button>
     </div>
     <p id="tenantPreferenceMessage" class="message"></p>
   </div>
 `;
}


// CREATE TENANT PREFERENCES
async function createTenantPreferences() {
    const message = document.getElementById("tenantPreferenceMessage");
    message.textContent = "";
    message.className = "message";


    const preferredLocation = document.getElementById("prefLocation").value.trim();
    const minimumBudget = Number(document.getElementById("prefMinBudget").value);
    const maximumBudget = Number(document.getElementById("prefMaxBudget").value);
    const preferredPropertyType = document.getElementById("prefPropertyType").value;
    const preferredBhk = Number(document.getElementById("prefBhk").value);
    const minimumAreaSqft = Number(document.getElementById("prefArea").value);
    const minimumBathrooms = Number(document.getElementById("prefBathrooms").value);
    const numberOfOccupants = Number(document.getElementById("prefOccupants").value);
    const furnishingPreference = document.getElementById("prefFurnishing").value;
    const preferredFloor = document.getElementById("prefFloor").value;
    const minimumLeaseDuration = Number(document.getElementById("prefLease").value);
    const preferredMoveInDate = document.getElementById("prefMoveIn").value;


    if (
        !preferredLocation ||
        !minimumBudget ||
        !maximumBudget ||
        !preferredPropertyType ||
        !preferredBhk ||
        !minimumAreaSqft ||
        !minimumBathrooms ||
        !numberOfOccupants ||
        !furnishingPreference ||
        !preferredFloor ||
        !minimumLeaseDuration ||
        !preferredMoveInDate
    ) {
        message.textContent = "Please fill in all preference fields.";
        message.classList.add("error");
        return;
    }


    if (minimumBudget > maximumBudget) {
        message.textContent = "Minimum budget cannot be greater than maximum budget.";
        message.classList.add("error");
        return;
    }

    if (numberOfOccupants > 20) {
        message.textContent = "Number of occupants cannot exceed 20.";
        message.classList.add("error");
        return;
    }
    if (minimumBathrooms > 20) {
        message.textContent = "Number of bathrooms cannot exceed 20.";
        message.classList.add("error");
        return;
    }
    const preferenceId = "PR" + Date.now().toString().slice(-8);
    const preference = {
        preferenceId: preferenceId,
        tenantId: currentUser.userId,
        preferredLocation: preferredLocation,
        minimumBudget: minimumBudget,
        maximumBudget: maximumBudget,
        preferredPropertyType: preferredPropertyType,
        preferredBhk: preferredBhk,
        minimumAreaSqft: minimumAreaSqft,
        minimumBathrooms: minimumBathrooms,
        numberOfOccupants: numberOfOccupants,
        furnishingPreference: furnishingPreference,
        preferredFloor: preferredFloor,
        minimumLeaseDuration: minimumLeaseDuration,
        preferredMoveInDate: preferredMoveInDate
    };


    try {
        await apiFetch("/api/tenant-preferences", {
            method: "POST",
            body: JSON.stringify(preference)
        });
        alert("Rental preferences saved successfully!");
        await loadTenantPreferences();
    } catch (error) {
        message.textContent = error.message;
        message.classList.add("error");
    }
}


// EDIT TENANT PREFERENCES
// =====================================================
// EDIT TENANT PREFERENCES
// =====================================================

async function showEditTenantPreferences() {

    const container =
        document.getElementById("tenantPreferences");

    container.innerHTML =
        '<div class="loading">Loading preferences...</div>';

    try {

        const preference =
            await apiFetch(
                `/api/tenant-preferences/${currentUser.userId}`,
                {
                    method: "GET"
                }
            );

        container.innerHTML = `
            <div class="profile-edit-form">

                <div class="form-group">
                    <label>Preferred Location</label>
                    <input
                        type="text"
                        id="prefLocation"
                        value="${safe(preference.preferredLocation)}">
                </div>

                <div class="form-group">
                    <label>Minimum Budget</label>
                    <input
                        type="number"
                        id="prefMinBudget"
                        value="${safe(preference.minimumBudget)}">
                </div>

                <div class="form-group">
                    <label>Maximum Budget</label>
                    <input
                        type="number"
                        id="prefMaxBudget"
                        value="${safe(preference.maximumBudget)}">
                </div>

                <div class="form-group">
                    <label>Property Type</label>

                    <select id="prefPropertyType">
                        <option
                            value="Apartment"
                            ${preference.preferredPropertyType === "Apartment"
            ? "selected"
            : ""}>
                            Apartment
                        </option>

                        <option
                            value="Villa"
                            ${preference.preferredPropertyType === "Villa"
            ? "selected"
            : ""}>
                            Villa
                        </option>

                        <option
                            value="Studio"
                            ${preference.preferredPropertyType === "Studio"
            ? "selected"
            : ""}>
                            Studio
                        </option>

                        <option
                            value="1RK"
                            ${preference.preferredPropertyType === "1RK"
            ? "selected"
            : ""}>
                            1RK
                        </option>
                    </select>
                </div>

                <div class="form-group">
                    <label>BHK</label>

                    <select id="prefBhk">

                        <option
                            value="1"
                            ${preference.preferredBhk === 1
            ? "selected"
            : ""}>
                            1 BHK
                        </option>

                        <option
                            value="2"
                            ${preference.preferredBhk === 2
            ? "selected"
            : ""}>
                            2 BHK
                        </option>

                        <option
                            value="3"
                            ${preference.preferredBhk === 3
            ? "selected"
            : ""}>
                            3 BHK
                        </option>

                        <option
                            value="4"
                            ${preference.preferredBhk === 4
            ? "selected"
            : ""}>
                            4 BHK
                        </option>

                    </select>
                </div>

                <div class="form-group">
                    <label>Minimum Area (sqft)</label>

                    <input
                        type="number"
                        id="prefArea"
                        value="${safe(preference.minimumAreaSqft)}">
                </div>

                <div class="form-group">
                    <label>Minimum Bathrooms</label>

                    <input
                        type="number"
                        id="prefBathrooms"
                        value="${safe(preference.minimumBathrooms)}"
                        min="1"
                        max="20">
                </div>

                <div class="form-group">
                    <label>Number of Occupants</label>

                    <input
    type="number"
    id="prefOccupants"
    min="1"
    max="20"
    oninput="if (this.value > 20) this.value = 20"
    value="${safe(preference.numberOfOccupants)}">
                </div>

                <div class="form-group">
                    <label>Furnishing Preference</label>

                    <select id="prefFurnishing">

                        <option
                            value="Fully Furnished"
                            ${preference.furnishingPreference === "Fully Furnished"
            ? "selected"
            : ""}>
                            Fully Furnished
                        </option>

                        <option
                            value="Semi-Furnished"
                            ${preference.furnishingPreference === "Semi-Furnished"
            ? "selected"
            : ""}>
                            Semi-Furnished
                        </option>

                        <option
                            value="Unfurnished"
                            ${preference.furnishingPreference === "Unfurnished"
            ? "selected"
            : ""}>
                            Unfurnished
                        </option>

                    </select>
                </div>

                <div class="form-group">
                    <label>Preferred Floor</label>

                    <select id="prefFloor">

                        <option
                            value="Any"
                            ${preference.preferredFloor === "Any"
            ? "selected"
            : ""}>
                            Any Floor
                        </option>

                        <option
                            value="Ground"
                            ${preference.preferredFloor === "Ground"
            ? "selected"
            : ""}>
                            Ground Floor
                        </option>

                        <option
                            value="Low"
                            ${preference.preferredFloor === "Low"
            ? "selected"
            : ""}>
                            Lower Floor
                        </option>

                        <option
                            value="High"
                            ${preference.preferredFloor === "High"
            ? "selected"
            : ""}>
                            Higher Floor
                        </option>

                    </select>
                </div>

                <div class="form-group">
                    <label>
                        Minimum Lease Duration (months)
                    </label>

                    <input
                        type="number"
                        id="prefLease"
                        value="${safe(preference.minimumLeaseDuration)}">
                </div>

                <div class="form-group">
                    <label>Preferred Move-In Date</label>

                    <input
                        type="date"
                        id="prefMoveIn"
                        value="${safe(preference.preferredMoveInDate)}">
                </div>

                <div class="profile-form-actions">

                    <button
                        type="button"
                        class="primary-btn"
                        onclick="updateTenantPreferences(
                            '${safe(preference.id)}',
                            '${safe(preference.preferenceId)}'
                        )">
                        Save Changes
                    </button>

                    <button
                        type="button"
                        class="secondary-btn"
                        onclick="loadTenantPreferences()">
                        Cancel
                    </button>

                </div>

                <p
                    id="tenantPreferenceMessage"
                    class="message">
                </p>

            </div>
        `;

    } catch (error) {

        container.innerHTML =
            `<div class="error">${safe(error.message)}</div>`;
    }
}

// UPDATE TENANT PREFERENCES
// =====================================================
// UPDATE TENANT PREFERENCES
// =====================================================

async function updateTenantPreferences(
    preferenceDocumentId,
    preferenceId
) {

    const message =
        document.getElementById(
            "tenantPreferenceMessage"
        );

    message.textContent = "";
    message.className = "message";

    const preference = {

        // Required by TenantPreference validation
        preferenceId: preferenceId,

        // Keep the current tenant
        tenantId: currentUser.userId,

        preferredLocation:
            document
                .getElementById("prefLocation")
                .value
                .trim(),

        minimumBudget:
            Number(
                document
                    .getElementById("prefMinBudget")
                    .value
            ),

        maximumBudget:
            Number(
                document
                    .getElementById("prefMaxBudget")
                    .value
            ),

        preferredPropertyType:
        document
            .getElementById("prefPropertyType")
            .value,

        preferredBhk:
            Number(
                document
                    .getElementById("prefBhk")
                    .value
            ),

        minimumAreaSqft:
            Number(
                document
                    .getElementById("prefArea")
                    .value
            ),

        minimumBathrooms:
            Number(
                document
                    .getElementById("prefBathrooms")
                    .value
            ),

        numberOfOccupants:
            Number(
                document
                    .getElementById("prefOccupants")
                    .value
            ),

        furnishingPreference:
        document
            .getElementById("prefFurnishing")
            .value,

        preferredFloor:
        document
            .getElementById("prefFloor")
            .value,

        minimumLeaseDuration:
            Number(
                document
                    .getElementById("prefLease")
                    .value
            ),

        preferredMoveInDate:
        document
            .getElementById("prefMoveIn")
            .value
    };


    // =====================================================
    // BASIC VALIDATION
    // =====================================================

    if (
        !preferenceId ||
        !preference.preferredLocation ||
        !preference.minimumBudget ||
        !preference.maximumBudget ||
        !preference.preferredPropertyType ||
        !preference.preferredBhk ||
        !preference.minimumAreaSqft ||
        !preference.minimumBathrooms ||
        !preference.numberOfOccupants ||
        !preference.furnishingPreference ||
        !preference.preferredFloor ||
        !preference.minimumLeaseDuration ||
        !preference.preferredMoveInDate
    ) {

        message.textContent =
            "Please fill in all preference fields.";

        message.classList.add("error");

        return;
    }
    if (
        preference.minimumBudget <= 0 ||
        preference.maximumBudget <= 0 ||
        preference.preferredBhk <= 0 ||
        preference.minimumAreaSqft <= 0 ||
        preference.minimumBathrooms <= 0 ||
        preference.numberOfOccupants <= 0 ||
        preference.minimumLeaseDuration <= 0
    ) {
        message.textContent = "All numeric preference values must be greater than 0.";
        message.classList.add("error");
        return;
    }

    if (preference.numberOfOccupants > 20) {
        message.textContent = "Number of occupants cannot exceed 20.";
        message.classList.add("error");
        return;
    }
    if (preference.minimumBathrooms > 20) {
        message.textContent = "Number of bathrooms cannot exceed 20.";
        message.classList.add("error");
        return;
    }
    // =====================================================
    // BUDGET VALIDATION
    // =====================================================

    if (
        preference.minimumBudget >
        preference.maximumBudget
    ) {

        message.textContent =
            "Minimum budget cannot be greater than maximum budget.";

        message.classList.add("error");

        return;
    }


    // =====================================================
    // UPDATE REQUEST
    // =====================================================

    try {

        await apiFetch(
            `/api/tenant-preferences/${encodeURIComponent(
                preferenceDocumentId
            )}`,
            {
                method: "PUT",
                body: JSON.stringify(preference)
            }
        );

        alert(
            "Rental preferences updated successfully!"
        );

        await loadTenantPreferences();

    } catch (error) {

        message.textContent =
            error.message;

        message.classList.add("error");
    }
}


// TENANT AMENITY PREFERENCES
async function loadTenantAmenityPreferences() {
    const container = document.getElementById("tenantAmenityPreferences");
    container.innerHTML = '<div class="loading">Loading amenity preferences...</div>';


    try {
        const preference = await apiFetch(`/api/tenant-amenity-preferences/${currentUser.userId}`, {
            method: "GET"
        });


        const amenityIds = preference.amenityIds || [];
        const amenities = await apiFetch("/api/amenities", { method: "GET" });


        const amenityNames = amenityIds.map(amenityId => {
            const amenity = amenities.find(item => item.amenityId === amenityId);
            return amenity ? amenity.amenityName : amenityId;
        });


        if (amenityIds.length === 0) {
            container.innerHTML = '<div class="empty">No amenity preferences selected.</div>';
            return;
        }


        container.innerHTML = `
     <div class="amenity-list">
       ${amenityNames.map(amenityName => `<span class="amenity-tag">${safe(amenityName)}</span>`).join("")}
     </div>
     <div class="profile-form-actions">
       <button type="button" class="secondary-btn small-btn" onclick="showEditTenantAmenityPreferences()">Edit Amenities</button>
     </div>
   `;
    } catch (error) {
        if (
            error.message &&
            (error.message.toLowerCase().includes("amenity preferences not found") ||
                error.message.toLowerCase().includes("request failed (404)"))
        ) {
            container.innerHTML = `
       <div class="empty">
         <p>You have not selected any amenity preferences yet.</p>
         <button type="button" class="primary-btn small-btn" onclick="showAddTenantAmenityPreferences()">Add Amenity Preferences</button>
       </div>
     `;
            return;
        }
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}


async function showAddTenantAmenityPreferences() {
    await showTenantAmenityPreferenceForm(null);
}


async function showEditTenantAmenityPreferences() {
    const container = document.getElementById("tenantAmenityPreferences");
    try {
        const preference = await apiFetch(`/api/tenant-amenity-preferences/${currentUser.userId}`, {
            method: "GET"
        });
        await showTenantAmenityPreferenceForm(preference);
    } catch (error) {
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}


async function showTenantAmenityPreferenceForm(existingPreference) {
    const container = document.getElementById("tenantAmenityPreferences");
    container.innerHTML = '<div class="loading">Loading amenities...</div>';


    try {
        const amenities = await apiFetch("/api/amenities", { method: "GET" });
        const selectedIds = existingPreference?.amenityIds || [];


        container.innerHTML = `
     <div class="amenity-selection">
       <p>Select the amenities you would like to have in your rental property.</p>
       <div class="amenity-checkbox-grid">
         ${amenities.map(amenity => `
           <label class="amenity-checkbox">
             <input type="checkbox" name="tenantAmenity" value="${safe(amenity.amenityId)}" ${selectedIds.includes(amenity.amenityId) ? "checked" : ""}>
             <span>${safe(amenity.amenityName)}</span>
           </label>
         `).join("")}
       </div>
       <div class="profile-form-actions">
         <button type="button" class="primary-btn" onclick="saveTenantAmenityPreferences('${existingPreference ? safe(existingPreference.id) : "null"}')">Save Amenities</button>
         <button type="button" class="secondary-btn" onclick="loadTenantAmenityPreferences()">Cancel</button>
       </div>
       <p id="tenantAmenityMessage" class="message"></p>
     </div>
   `;
    } catch (error) {
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}


async function saveTenantAmenityPreferences(preferenceDocumentId) {
    const message = document.getElementById("tenantAmenityMessage");
    message.textContent = "";
    message.className = "message";


    const checkboxes = document.querySelectorAll('input[name="tenantAmenity"]:checked');
    const amenityIds = Array.from(checkboxes).map(checkbox => checkbox.value);


    if (amenityIds.length === 0) {
        message.textContent = "Please select at least one amenity.";
        message.classList.add("error");
        return;
    }


    const preference = {
        tenantId: currentUser.userId,
        amenityIds: amenityIds
    };


    try {
        if (preferenceDocumentId && preferenceDocumentId !== "null") {
            await apiFetch(`/api/tenant-amenity-preferences/${encodeURIComponent(preferenceDocumentId)}`, {
                method: "PUT",
                body: JSON.stringify(preference)
            });
        } else {
            await apiFetch("/api/tenant-amenity-preferences", {
                method: "POST",
                body: JSON.stringify(preference)
            });
        }
        alert("Amenity preferences saved successfully!");
        await loadTenantAmenityPreferences();
    } catch (error) {
        message.textContent = error.message;
        message.classList.add("error");
    }
}


// AVAILABLE PROPERTIES
async function loadAvailableProperties() {
    const container = document.getElementById("availableProperties");
    container.innerHTML = '<div class="loading">Loading available properties...</div>';


    try {
        const properties = await apiFetch("/api/properties/available", { method: "GET" });


        if (!properties || properties.length === 0) {
            container.innerHTML = '<div class="empty">No properties are currently available.</div>';
            return;
        }


        container.innerHTML = properties.map(property => createPropertyCard(property)).join("");
    } catch (error) {
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}


async function searchProperties() {
    const container = document.getElementById("propertyResults");
    container.innerHTML = '<div class="loading">Searching...</div>';

    try {
        const properties = await apiFetch(`/api/properties/search/${currentUser.userId}`, {
            method: "GET"
        });

        if (!properties || properties.length === 0) {
            container.innerHTML = '<div class="empty">No properties currently match your preferences.</div>';
            return;
        }

        const availableProperties = await apiFetch("/api/properties/available", {
            method: "GET"
        });

        const mediaMap = new Map(
            availableProperties.map(property => [
                property.propertyId,
                {
                    imageFileIds: property.imageFileIds || [],
                    videoFileId: property.videoFileId || ""
                }
            ])
        );

        properties.forEach(property => {
            const media = mediaMap.get(property.propertyId);

            if (media) {
                property.imageFileIds = media.imageFileIds;
                property.videoFileId = media.videoFileId;
            }
        });

        container.innerHTML = properties.map(property => createPropertyCard(property)).join("");

    } catch (error) {
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}


function createPropertyCard(property) {

    console.log("TENANT PROPERTY:", property);
    console.log("IMAGE IDS:", property.imageFileIds);
    console.log("VIDEO ID:", property.videoFileId);

    const amenities = property.amenities || [];
    const imageIds = property.imageFileIds || [];
    const videoId = property.videoFileId;

    return `
        <div
            class="property-card"
            data-images='${JSON.stringify(imageIds)}'
            data-video="${safe(videoId === "-" ? "" : videoId)}"
            data-amenities='${JSON.stringify(amenities)}'>

            <h3>
                ${safe(property.propertyId)} -
                ${safe(property.propertyType)}
            </h3>

            <div class="property-location">
                ${safe(property.location)}, ${safe(property.city)}
            </div>

            <div class="property-details">

                <div class="detail">
                    <strong>BHK</strong>
                    <br>
                    ${safe(property.bhk)}
                </div>

                <div class="detail">
                    <strong>Area</strong>
                    <br>
                    ${safe(property.areaSqft)} sqft
                </div>

                <div class="detail">
                    <strong>Bathrooms</strong>
                    <br>
                    ${safe(property.numberOfBathrooms)}
                </div>

                <div class="detail">
                    <strong>Floor</strong>
                    <br>
                    ${safe(property.floorNumber)}
                </div>

                <div class="detail">
                    <strong>Furnishing</strong>
                    <br>
                    ${safe(property.furnishingStatus)}
                </div>

            </div>

            <div class="rent">
                ${formatNumber(property.monthlyRent)}
                <span>/ month</span>
            </div>

            <div class="property-card-buttons">

                <button
                    type="button"
                    class="primary-btn small-btn"
                    onclick="showPropertyMedia(this)">
                    View Photos & Video
                </button>

                <button
                    type="button"
                    class="secondary-btn small-btn"
                    onclick="showPropertyAmenities(this)">
                    View Amenities
                </button>

            </div>

            <button
                class="primary-btn apply-btn"
                onclick="applyForProperty('${safe(property.propertyId)}')">
                Apply for Property
            </button>

        </div>
    `;
}
function viewImageFullscreen(image) {

    if (image.requestFullscreen) {
        image.requestFullscreen();
    } else if (image.webkitRequestFullscreen) {
        image.webkitRequestFullscreen();
    }
}

async function applyForProperty(propertyId) {
    const application = {
        tenantId: currentUser.userId,
        propertyId: propertyId
    };


    try {
        await apiFetch("/api/applications", {
            method: "POST",
            body: JSON.stringify(application)
        });
        alert("Application submitted successfully!");
        await loadTenantApplications();
    } catch (error) {
        alert("Application failed: " + error.message);
    }
}


// TENANT APPLICATIONS
async function loadTenantApplications() {
    const container = document.getElementById("tenantApplications");
    container.innerHTML = "Loading...";


    try {
        const applications = await apiFetch(`/api/applications/tenant/${currentUser.userId}`, {
            method: "GET"
        });


        if (!applications || applications.length === 0) {
            container.innerHTML = '<div class="empty">No applications found.</div>';
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
           ${applications.map(app => `
             <tr>
               <td>${safe(app.applicationId)}</td>
               <td>${safe(app.tenantId)}</td>
               <td>${safe(app.propertyId)}</td>
               <td>${safe(app.proposedMoveInDate)}</td>
               <td>${safe(app.numberOfOccupants)}</td>
               <td>${statusBadge(app.applicationStatus)}</td>
               <td>
                 ${
            app.applicationStatus === "Pending"
                ? `<button class="reject-btn" onclick="withdrawApplication('${safe(app.applicationId)}')">Withdraw</button>`
                : "-"
        }
               </td>
             </tr>
           `).join("")}
         </tbody>
       </table>
     </div>
   `;
    } catch (error) {
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}


// TENANT LEASES
async function loadTenantLeases() {
    const container = document.getElementById("tenantLeases");
    container.innerHTML = "Loading...";


    try {
        const leases = await apiFetch(`/api/leases/tenant/${currentUser.userId}`, {
            method: "GET"
        });


        if (!leases || leases.length === 0) {
            container.innerHTML = '<div class="empty">No leases found.</div>';
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
               <td>${formatNumber(lease.monthlyRent)}</td>
               <td>${statusBadge(lease.leaseStatus)}</td>
             </tr>
           `).join("")}
         </tbody>
       </table>
     </div>
   `;
    } catch (error) {
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}


// LANDLORD DASHBOARD
async function loadLandlordDashboard() {
    await loadLandlordProfile();
    await loadLandlordProperties();
    await loadLandlordApplications();
    await loadLandlordLeases();
}


// LANDLORD PROFILE
async function loadLandlordProfile() {
    const container = document.getElementById("landlordProfile");
    container.innerHTML = "Loading...";


    try {
        const data = await apiFetch(`/api/landlords/${encodeURIComponent(currentUser.userId)}`, {
            method: "GET"
        });


        let verificationMessage = "";
        if (data.verificationStatus === "Verified") {
            verificationMessage = '<div class="verification-message verified">Your account has been successfully verified!</div>';
        } else if (data.verificationStatus === "Rejected") {
            verificationMessage = '<div class="verification-message rejected">Your account verification was rejected.</div>';
        } else {
            verificationMessage = '<div class="verification-message pending">Your account is awaiting administrator verification.</div>';
        }


        container.innerHTML = `
     <div class="profile-item">
       <span class="profile-label">Landlord ID</span>
       <span class="profile-value">${safe(data.landlordId)}</span>
     </div>
     <div class="profile-item">
       <span class="profile-label">Name</span>
       <span class="profile-value">${safe(data.firstName)} ${safe(data.lastName)}</span>
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
       <span class="profile-label">Verification Status</span>
       <span class="profile-value">${safe(data.verificationStatus)}</span>
     </div>
     ${verificationMessage}
   `;
    } catch (error) {
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}
// SHOW LANDLORD PROFILE EDIT FORM
async function showLandlordProfileEdit() {


    const container = document.getElementById("landlordProfile");


    container.innerHTML = "Loading...";


    try {


        const data = await apiFetch(
            `/api/landlords/${encodeURIComponent(currentUser.userId)}`,
            {
                method: "GET"
            }
        );


        container.innerHTML = `
           <div class="profile-edit-form">


               <div class="form-group">


                   <label for="editLandlordFirstName">
                       First Name
                   </label>


                   <input
                       type="text"
                       id="editLandlordFirstName"
                       value="${safe(data.firstName)}"
                   >


               </div>




               <div class="form-group">


                   <label for="editLandlordLastName">
                       Last Name
                   </label>


                   <input
                       type="text"
                       id="editLandlordLastName"
                       value="${safe(data.lastName)}"
                   >


               </div>




               <div class="form-group">


                   <label for="editLandlordEmail">
                       Email
                   </label>


                   <input
                       type="email"
                       id="editLandlordEmail"
                       value="${safe(data.email)}"
                   >


               </div>




               <div class="form-group">


                   <label for="editLandlordPhone">
                       Phone
                   </label>


                   <input
                       type="text"
                       id="editLandlordPhone"
                       value="${safe(data.phone)}"
                   >


               </div>




               <div class="profile-form-actions">


                   <button
                           type="button"
                           class="primary-btn"
                           onclick="saveLandlordProfile()">
                       Save Changes
                   </button>


                   <button
                           type="button"
                           class="secondary-btn"
                           onclick="loadLandlordProfile()">
                       Cancel
                   </button>


               </div>




               <div
                       id="landlordProfileEditMessage"
                       class="form-message">
               </div>


           </div>
       `;


    } catch (error) {


        container.innerHTML = `
           <div class="error">
               ${safe(error.message)}
           </div>
       `;
    }
}


// SAVE LANDLORD PROFILE
async function saveLandlordProfile() {


    const message =
        document.getElementById(
            "landlordProfileEditMessage"
        );


    const firstName =
        document.getElementById(
            "editLandlordFirstName"
        ).value.trim();


    const lastName =
        document.getElementById(
            "editLandlordLastName"
        ).value.trim();


    const email =
        document.getElementById(
            "editLandlordEmail"
        ).value.trim();


    const phone =
        document.getElementById(
            "editLandlordPhone"
        ).value.trim();




    if (
        !firstName ||
        !lastName ||
        !email ||
        !phone
    ) {


        message.textContent =
            "Please fill in all fields.";


        message.className =
            "form-message error";


        return;
    }
    const namePattern = /^[A-Za-z]+(?:[ '-][A-Za-z]+)*$/;
    if (!namePattern.test(firstName)) {
        message.textContent = "First name can contain only letters.";
        message.classList.add("error");
        return;
    }

    if (!namePattern.test(lastName)) {
        message.textContent = "Last name can contain only letters.";
        message.classList.add("error");
        return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        message.textContent = "Please enter a valid email address.";
        message.classList.add("error");
        return;
    }
    if (!/^(\+91[- ]?)?[6-9]\d{9}$/.test(phone)) {
        message.textContent = "Please enter a valid Indian phone number.";
        message.classList.add("error");
        return;
    }

    const updatedLandlord = {


        firstName: firstName,
        lastName: lastName,
        email: email,
        phone: phone


    };




    message.textContent =
        "Saving...";


    message.className =
        "form-message";




    try {


        const updatedData =
            await apiFetch(
                `/api/landlords/${encodeURIComponent(currentUser.userId)}`,
                {
                    method: "PUT",


                    body: JSON.stringify(
                        updatedLandlord
                    )
                }
            );




        /*
         * If the email was changed,
         * keep the session email updated.
         */
        currentUser.email =
            updatedData.email;


        currentUser.name =
            `${updatedData.firstName} ${updatedData.lastName}`;




        sessionStorage.setItem(
            "email",
            currentUser.email
        );


        sessionStorage.setItem(
            "name",
            currentUser.name
        );




        message.textContent =
            "Profile updated successfully.";


        message.className =
            "form-message success";




        await loadLandlordProfile();


    } catch (error) {


        message.textContent =
            error.message;


        message.className =
            "form-message error";
    }
}
// DELETE LANDLORD ACCOUNT
async function deleteLandlordAccount() {


    const confirmed =
        confirm(
            "Are you sure you want to delete your landlord account? " +
            "This action cannot be undone."
        );


    if (!confirmed) {
        return;
    }




    try {


        await apiFetch(
            `/api/landlords/${encodeURIComponent(currentUser.userId)}`,
            {
                method: "DELETE"
            }
        );




        alert(
            "Your landlord account has been deleted successfully."
        );




        /*
         * Clear the current session.
         */
        sessionStorage.removeItem("role");
        sessionStorage.removeItem("userId");
        sessionStorage.removeItem("email");
        sessionStorage.removeItem("name");




        currentUser.role = null;
        currentUser.userId = null;
        currentUser.email = null;
        currentUser.name = null;




        showLogin();


    } catch (error) {


        alert(
            "Unable to delete your account: " +
            error.message
        );
    }
}
// LANDLORD PROPERTIES
async function loadLandlordProperties() {
    const container = document.getElementById("landlordProperties");
    container.innerHTML = "Loading...";


    try {
        const properties = await apiFetch(`/api/properties/landlord/${currentUser.userId}`, {
            method: "GET"
        });


        if (!properties || properties.length === 0) {
            container.innerHTML = '<div class="empty">No properties found.</div>';
            return;
        }


        container.innerHTML = properties.map(property => createLandlordPropertyCard(property)).join("");
    } catch (error) {
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}


function createLandlordPropertyCard(property) {

    const imageIds = property.imageFileIds || [];
    const videoId = property.videoFileId;

    return `
        <div
            class="property-card"
            id="property-card-${safe(property.propertyId)}"
            data-images='${JSON.stringify(imageIds)}'
            data-video="${safe(videoId || "")}">

            <h3>
                ${safe(property.propertyId)} -
                ${safe(property.propertyType)}
            </h3>

            <div class="property-location">
                ${safe(property.location)}, ${safe(property.city)}
            </div>

            <div class="property-details">

                <div class="detail">
                    <strong>BHK</strong>
                    <br>
                    ${safe(property.bhk)}
                </div>

                <div class="detail">
                    <strong>Area</strong>
                    <br>
                    ${safe(property.areaSqft)} sqft
                </div>

                <div class="detail">
                    <strong>Bathrooms</strong>
                    <br>
                    ${safe(property.numberOfBathrooms)}
                </div>

                <div class="detail">
                    <strong>Floor</strong>
                    <br>
                    ${safe(property.floorNumber)}
                </div>

                <div class="detail">
                    <strong>Furnishing</strong>
                    <br>
                    ${safe(property.furnishingStatus)}
                </div>

                <div class="detail">
                    <strong>Status</strong>
                    <br>
                    ${safe(property.propertyStatus)}
                </div>

            </div>

            <div class="rent">
                ₹${formatNumber(property.monthlyRent)}
                <span>/ month</span>
            </div>

            <button
                type="button"
                class="primary-btn small-btn"
               onclick="showPropertyMedia(this)">
                View Photos & Video
            </button>

            <div class="property-actions">

                <button
                    type="button"
                    class="secondary-btn small-btn"
                    onclick='showEditPropertyForm(${JSON.stringify(property)})'>
                    Edit Property
                </button>

                <button
                    type="button"
                    class="secondary-btn small-btn"
                    onclick="showEditPropertyAmenities('${safe(property.propertyId)}')">
                    Edit Amenities
                </button>

                <button
                    type="button"
                    class="danger-btn small-btn"
                    onclick="deleteLandlordProperty('${safe(property.propertyId)}')">
                    Delete Property
                </button>

            </div>

        </div>
    `;
}
function showPropertyMedia(button) {

    const propertyCard =
        button.closest(".property-card");

    if (!propertyCard) {
        alert("Property card not found.");
        return;
    }

    const images =
        JSON.parse(
            propertyCard.getAttribute("data-images") || "[]"
        );

    const rawVideoId =
        propertyCard.getAttribute("data-video") || "";

    const videoId =
        rawVideoId === "-" ? "" : rawVideoId;

    const modal =
        document.createElement("div");

    modal.style.position = "fixed";
    modal.style.top = "0";
    modal.style.left = "0";
    modal.style.width = "100%";
    modal.style.height = "100%";
    modal.style.background = "rgba(0,0,0,0.75)";
    modal.style.zIndex = "99999";
    modal.style.display = "flex";
    modal.style.alignItems = "center";
    modal.style.justifyContent = "center";
    modal.style.padding = "30px";
    modal.style.boxSizing = "border-box";

    const content =
        document.createElement("div");

    content.style.background = "white";
    content.style.borderRadius = "14px";
    content.style.padding = "30px";
    content.style.width = "90%";
    content.style.maxWidth = "900px";
    content.style.maxHeight = "90%";
    content.style.overflowY = "auto";
    content.style.position = "relative";

    const closeButton =
        document.createElement("button");

    closeButton.textContent = "×";
    closeButton.type = "button";

    closeButton.style.position = "absolute";
    closeButton.style.top = "10px";
    closeButton.style.right = "15px";
    closeButton.style.fontSize = "30px";
    closeButton.style.border = "none";
    closeButton.style.background = "none";
    closeButton.style.cursor = "pointer";

    closeButton.onclick = function () {
        modal.remove();
    };

    const title =
        document.createElement("h2");

    title.textContent =
        "Property Photos & Video";

    content.appendChild(closeButton);
    content.appendChild(title);


    /* IMAGES */

    const imageHeading =
        document.createElement("h3");

    imageHeading.textContent =
        "Property Images";

    content.appendChild(imageHeading);

    if (images.length === 0) {

        const message =
            document.createElement("p");

        message.textContent =
            "No images available.";

        content.appendChild(message);

    } else {

        images.forEach(function (imageId) {

            const image =
                document.createElement("img");

            image.src =
                "/api/properties/media/" +
                encodeURIComponent(imageId);

            image.alt =
                "Property image";

            image.style.width = "100%";
            image.style.maxHeight = "400px";
            image.style.objectFit = "contain";
            image.style.display = "block";
            image.style.marginBottom = "15px";
            image.style.borderRadius = "10px";
            image.style.background = "#f8fafc";
            image.style.cursor = "pointer";

            image.onclick = function () {
                viewImageFullscreen(image);
            };

            content.appendChild(image);
        });
    }


    /* VIDEO */

    const videoHeading =
        document.createElement("h3");

    videoHeading.textContent =
        "Property Video";

    content.appendChild(videoHeading);

    if (!videoId) {

        const message =
            document.createElement("p");

        message.textContent =
            "No video available.";

        content.appendChild(message);

    } else {

        const video =
            document.createElement("video");

        video.controls = true;

        video.style.width = "100%";
        video.style.maxHeight = "500px";
        video.style.background = "#000";
        video.style.borderRadius = "10px";

        video.src =
            "/api/properties/media/" +
            encodeURIComponent(videoId);

        content.appendChild(video);
    }


    modal.appendChild(content);

    document.body.appendChild(modal);
}

function showPropertyAmenities(button) {

    const propertyCard =
        button.closest(".property-card");

    if (!propertyCard) {
        alert("Property card not found.");
        return;
    }

    const amenities =
        JSON.parse(
            propertyCard.getAttribute("data-amenities") || "[]"
        );

    const modal =
        document.createElement("div");

    modal.style.position = "fixed";
    modal.style.top = "0";
    modal.style.left = "0";
    modal.style.width = "100%";
    modal.style.height = "100%";
    modal.style.background = "rgba(0,0,0,0.65)";
    modal.style.zIndex = "99999";
    modal.style.display = "flex";
    modal.style.alignItems = "center";
    modal.style.justifyContent = "center";
    modal.style.padding = "30px";
    modal.style.boxSizing = "border-box";

    const content =
        document.createElement("div");

    content.style.background = "white";
    content.style.borderRadius = "14px";
    content.style.padding = "30px";
    content.style.width = "90%";
    content.style.maxWidth = "650px";
    content.style.maxHeight = "80%";
    content.style.overflowY = "auto";
    content.style.position = "relative";

    const closeButton =
        document.createElement("button");

    closeButton.textContent = "×";
    closeButton.type = "button";

    closeButton.style.position = "absolute";
    closeButton.style.top = "10px";
    closeButton.style.right = "15px";
    closeButton.style.fontSize = "30px";
    closeButton.style.border = "none";
    closeButton.style.background = "none";
    closeButton.style.cursor = "pointer";

    closeButton.onclick = function () {
        modal.remove();
    };

    const title =
        document.createElement("h2");

    title.textContent =
        "Property Amenities";

    content.appendChild(closeButton);
    content.appendChild(title);

    if (amenities.length === 0) {

        const message =
            document.createElement("p");

        message.textContent =
            "No amenities listed.";

        content.appendChild(message);

    } else {

        const amenityContainer =
            document.createElement("div");

        amenityContainer.style.display = "flex";
        amenityContainer.style.flexWrap = "wrap";
        amenityContainer.style.gap = "10px";
        amenityContainer.style.marginTop = "20px";

        amenities.forEach(function (amenity) {

            const tag =
                document.createElement("span");

            tag.textContent = amenity;

            tag.style.display = "inline-block";
            tag.style.padding = "8px 14px";
            tag.style.borderRadius = "20px";
            tag.style.background = "#f1f5f9";
            tag.style.color = "#334155";
            tag.style.border = "1px solid #e2e8f0";
            tag.style.fontSize = "14px";

            amenityContainer.appendChild(tag);
        });

        content.appendChild(amenityContainer);
    }

    modal.appendChild(content);

    document.body.appendChild(modal);
}
function showEditPropertyForm(property) {


    const card =
        document.getElementById(
            `property-card-${property.propertyId}`
        );


    if (!card) {
        return;
    }


    card.innerHTML = `


       <div class="property-edit-form">


           <h3>
               Edit Property -
               ${safe(property.propertyId)}
           </h3>


           <div class="form-group">


               <label>Property Type</label>


               <select id="editPropertyType-${property.propertyId}">


                   <option value="Apartment"
                       ${property.propertyType === "Apartment" ? "selected" : ""}>
                       Apartment
                   </option>


                   <option value="Villa"
                       ${property.propertyType === "Villa" ? "selected" : ""}>
                       Villa
                   </option>


                   <option value="Studio"
                       ${property.propertyType === "Studio" ? "selected" : ""}>
                       Studio
                   </option>


                   <option value="1RK"
                       ${property.propertyType === "1RK" ? "selected" : ""}>
                       1RK
                   </option>


               </select>


           </div>




           <div class="form-group">


               <label>Location</label>


               <input
                   type="text"
                   id="editPropertyLocation-${property.propertyId}"
                   value="${safe(property.location)}">


           </div>




           <div class="form-group">


               <label>City</label>


               <input
                   type="text"
                   id="editPropertyCity-${property.propertyId}"
                   value="${safe(property.city)}">


           </div>




           <div class="form-group">


               <label>Pincode</label>


               <input
                   type="text"
                   id="editPropertyPincode-${property.propertyId}"
                   value="${safe(property.pincode)}"
                   maxlength="6">
                  

           </div>




           <div class="form-group">


               <label>BHK</label>


               <select id="editPropertyBhk-${property.propertyId}">


                   <option value="1"
                       ${property.bhk == 1 ? "selected" : ""}>
                       1 BHK
                   </option>


                   <option value="2"
                       ${property.bhk == 2 ? "selected" : ""}>
                       2 BHK
                   </option>


                   <option value="3"
                       ${property.bhk == 3 ? "selected" : ""}>
                       3 BHK
                   </option>


                   <option value="4"
                       ${property.bhk == 4 ? "selected" : ""}>
                       4 BHK
                   </option>


               </select>


           </div>




           <div class="form-group">


               <label>Area (sqft)</label>


               <input
                   type="number"
                   min="1"
                   id="editPropertyArea-${property.propertyId}"
                   value="${safe(property.areaSqft)}">


           </div>




           <div class="form-group">


               <label>Number of Bathrooms</label>


               <input
                   type="number"
                   min="1"
                   id="editPropertyBathrooms-${property.propertyId}"
                   value="${safe(property.numberOfBathrooms)}">


           </div>




           <div class="form-group">


               <label>Monthly Rent (₹)</label>


               <input
                   type="number"
                   min="1"
                   id="editPropertyRent-${property.propertyId}"
                   value="${safe(property.monthlyRent)}">


           </div>




           <div class="form-group">


               <label>Security Deposit (₹)</label>


               <input
                   type="number"
                   min="1"
                   id="editPropertyDeposit-${property.propertyId}"
                   value="${safe(property.securityDeposit)}">


           </div>




           <div class="form-group">


               <label>Floor Number</label>


               <input
                   type="number"
                   min="0"
                   id="editPropertyFloor-${property.propertyId}"
                   value="${safe(property.floorNumber)}">


           </div>




           <div class="form-group">


               <label>Furnishing Status</label>


               <select id="editPropertyFurnishing-${property.propertyId}">


                   <option value="Fully Furnished"
                       ${property.furnishingStatus === "Fully Furnished" ? "selected" : ""}>
                       Fully Furnished
                   </option>


                   <option value="Semi-Furnished"
                       ${property.furnishingStatus === "Semi-Furnished" ? "selected" : ""}>
                       Semi-Furnished
                   </option>


                   <option value="Unfurnished"
                       ${property.furnishingStatus === "Unfurnished" ? "selected" : ""}>
                       Unfurnished
                   </option>


               </select>


           </div>




           <div class="form-group">


               <label>Available From</label>


               <input
                   type="date"
                   id="editPropertyAvailableFrom-${property.propertyId}"
                   value="${safe(property.availableFrom)}">


           </div>




           <div class="detail">


               <strong>Status</strong>


               <br>


               ${safe(property.propertyStatus)}


               <small>
                   Property status is managed by the rental process.
               </small>


           </div>




           <div class="property-form-actions">


               <button
                   type="button"
                   class="primary-btn small-btn"
                   onclick="savePropertyEdit('${property.propertyId}')">
                   Save Changes
               </button>


               <button
                   type="button"
                   class="secondary-btn small-btn"
                   onclick="loadLandlordProperties()">
                   Cancel
               </button>


           </div>




           <div
               id="propertyEditMessage-${property.propertyId}"
               class="form-message">
           </div>


       </div>
   `;
}


async function savePropertyEdit(propertyId) {


    const message =
        document.getElementById(
            `propertyEditMessage-${propertyId}`
        );


    const propertyType =
        document.getElementById(
            `editPropertyType-${propertyId}`
        ).value;


    const location =
        document.getElementById(
            `editPropertyLocation-${propertyId}`
        ).value.trim();


    const city =
        document.getElementById(
            `editPropertyCity-${propertyId}`
        ).value.trim();


    const pincode =
        document.getElementById(
            `editPropertyPincode-${propertyId}`
        ).value.trim();


    const bhkValue =
        document.getElementById(
            `editPropertyBhk-${propertyId}`
        ).value;


    const areaValue =
        document.getElementById(
            `editPropertyArea-${propertyId}`
        ).value;


    const bathroomsValue =
        document.getElementById(
            `editPropertyBathrooms-${propertyId}`
        ).value;


    const rentValue =
        document.getElementById(
            `editPropertyRent-${propertyId}`
        ).value;


    const depositValue =
        document.getElementById(
            `editPropertyDeposit-${propertyId}`
        ).value;


    const floorValue =
        document.getElementById(
            `editPropertyFloor-${propertyId}`
        ).value;


    const furnishingStatus =
        document.getElementById(
            `editPropertyFurnishing-${propertyId}`
        ).value;


    const availableFrom =
        document.getElementById(
            `editPropertyAvailableFrom-${propertyId}`
        ).value;




    if (
        !propertyType ||
        !location ||
        !city ||
        !pincode ||
        bhkValue === "" ||
        areaValue === "" ||
        bathroomsValue === "" ||
        rentValue === "" ||
        depositValue === "" ||
        floorValue === "" ||
        !furnishingStatus ||
        !availableFrom
    ) {


        message.textContent =
            "Please fill in all property fields.";


        message.className =
            "form-message error";


        return;
    }




    const bhk =
        Number(bhkValue);


    const areaSqft =
        Number(areaValue);


    const numberOfBathrooms =
        Number(bathroomsValue);


    const monthlyRent =
        Number(rentValue);


    const securityDeposit =
        Number(depositValue);


    const floorNumber =
        Number(floorValue);


    if (bhk <= 0) {
        message.textContent = "BHK must be greater than 0.";
        message.classList.add("error");
        return;
    }

    if (areaSqft <= 0) {
        message.textContent = "Area must be greater than 0.";
        message.classList.add("error");
        return;
    }

    if (numberOfBathrooms <= 0) {
        message.textContent = "Number of bathrooms must be greater than 0.";
        message.classList.add("error");
        return;
    }

    if (monthlyRent <= 0) {
        message.textContent = "Monthly rent must be greater than 0.";
        message.classList.add("error");
        return;
    }

    if (securityDeposit <= 0) {
        message.textContent = "Security deposit must be greater than 0.";
        message.classList.add("error");
        return;
    }

    if (floorNumber < 0) {
        message.textContent = "Floor number cannot be negative.";
        message.classList.add("error");
        return;
    }
    if (!/^\d{6}$/.test(pincode)) {
        message.textContent = "Pincode must be exactly 6 digits.";
        message.classList.add("error");
        return;
    }
    if (numberOfBathrooms > 20) {
        message.textContent = "Number of bathrooms cannot exceed 20.";
        message.classList.add("error");
        return;
    }
    if (floorNumber > 100) {
        message.textContent = "Floor number cannot exceed 100.";
        message.classList.add("error");
        return;
    }
    message.textContent =
        "Saving...";


    message.className =
        "form-message";




    try {


        /*
         * Get the current property first.
         * This lets us preserve the landlord ID
         * and current property status.
         */


        const existingProperty =
            await apiFetch(
                `/api/properties/${encodeURIComponent(propertyId)}`,
                {
                    method: "GET"
                }
            );




        const updatedProperty = {


            propertyId: propertyId,


            landlordId:
            existingProperty.landlordId,


            propertyType:
            propertyType,


            location:
            location,


            city:
            city,


            pincode:
            pincode,


            bhk:
            bhk,


            areaSqft:
            areaSqft,


            numberOfBathrooms:
            numberOfBathrooms,


            monthlyRent:
            monthlyRent,


            securityDeposit:
            securityDeposit,


            floorNumber:
            floorNumber,


            furnishingStatus:
            furnishingStatus,


            availableFrom:
            availableFrom,


            propertyStatus:
            existingProperty.propertyStatus
        };




        await apiFetch(
            `/api/properties/${encodeURIComponent(propertyId)}`,
            {
                method: "PUT",
                body: JSON.stringify(updatedProperty)
            }
        );




        message.textContent =
            "Property updated successfully.";


        message.className =
            "form-message success";




        await loadLandlordProperties();




    } catch (error) {


        message.textContent =
            error.message;


        message.className =
            "form-message error";
    }
}


// =====================================================
// EDIT PROPERTY AMENITIES
// =====================================================


async function showEditPropertyAmenities(propertyId) {


    const card =
        document.getElementById(
            `property-card-${propertyId}`
        );


    if (!card) {
        return;
    }


    card.innerHTML = `


       <div class="property-edit-form">


           <h3>
               Edit Property Amenities -
               ${safe(propertyId)}
           </h3>


           <p class="amenity-edit-description">
               Select the amenities available in your property.
           </p>


           <div
               id="editPropertyAmenities-${propertyId}"
               class="amenity-edit-list">


               Loading amenities...


           </div>


           <div class="property-form-actions">


               <button
                   type="button"
                   class="primary-btn small-btn"
                   onclick="savePropertyAmenitiesEdit('${propertyId}')">
                   Save Changes
               </button>


               <button
                   type="button"
                   class="secondary-btn small-btn"
                   onclick="loadLandlordProperties()">
                   Cancel
               </button>


           </div>


           <div
               id="propertyAmenityEditMessage-${propertyId}"
               class="form-message">
           </div>


       </div>
   `;




    try {


        /*
         * Get all available amenities
         */


        const allAmenities =
            await apiFetch(
                "/api/amenities",
                {
                    method: "GET"
                }
            );




        /*
         * Get the amenities currently assigned
         * to this property
         */


        const currentPropertyAmenities =
            await apiFetch(
                `/api/property-amenities/${encodeURIComponent(propertyId)}`,
                {
                    method: "GET"
                }
            );




        const currentAmenityIds =
            currentPropertyAmenities.amenityIds || [];




        const container =
            document.getElementById(
                `editPropertyAmenities-${propertyId}`
            );




        if (!allAmenities ||
            allAmenities.length === 0) {


            container.innerHTML =
                "<p>No amenities are currently available.</p>";


            return;
        }




        /*
         * Create one checkbox per amenity
         */


        container.innerHTML = allAmenities
            .map(amenity => {


                const checked =
                    currentAmenityIds.includes(
                        amenity.amenityId
                    );


                return `


                   <label class="amenity-edit-option">


                       <input
                           type="checkbox"
                           name="editPropertyAmenity-${propertyId}"
                           value="${safe(amenity.amenityId)}"
                           ${checked ? "checked" : ""}
                       >


                       <span>
                           ${safe(amenity.amenityName)}
                       </span>


                   </label>


               `;


            })
            .join("");




    } catch (error) {


        const container =
            document.getElementById(
                `editPropertyAmenities-${propertyId}`
            );


        container.innerHTML =
            `<p class="error">${safe(error.message)}</p>`;
    }
}


// =====================================================
// SAVE EDITED PROPERTY AMENITIES
// =====================================================


async function savePropertyAmenitiesEdit(propertyId) {


    const message =
        document.getElementById(
            `propertyAmenityEditMessage-${propertyId}`
        );




    const selectedAmenities =
        document.querySelectorAll(
            `input[name="editPropertyAmenity-${propertyId}"]:checked`
        );




    const amenityIds =
        Array.from(selectedAmenities)
            .map(checkbox => checkbox.value);




    if (amenityIds.length === 0) {


        message.textContent =
            "Please select at least one amenity.";


        message.className =
            "form-message error";


        return;
    }




    const propertyAmenityData = {


        propertyId:
        propertyId,


        amenityIds:
        amenityIds
    };




    message.textContent =
        "Saving...";


    message.className =
        "form-message";




    try {


        await apiFetch(
            `/api/property-amenities/${encodeURIComponent(propertyId)}`,
            {
                method: "PUT",


                body:
                    JSON.stringify(
                        propertyAmenityData
                    )
            }
        );




        message.textContent =
            "Property amenities updated successfully.";


        message.className =
            "form-message success";




        await loadLandlordProperties();




    } catch (error) {


        message.textContent =
            error.message;


        message.className =
            "form-message error";
    }
}


// =====================================================
// DELETE LANDLORD PROPERTY
// =====================================================


async function deleteLandlordProperty(propertyId) {


    const confirmed = confirm(
        `Are you sure you want to delete property ${propertyId}?\n\n` +
        `This action cannot be undone.`
    );


    if (!confirmed) {
        return;
    }




    try {


        await apiFetch(
            `/api/properties/${encodeURIComponent(propertyId)}`,
            {
                method: "DELETE"
            }
        );




        alert(
            `Property ${propertyId} has been deleted successfully.`
        );




        await loadLandlordProperties();




    } catch (error) {


        alert(
            "Unable to delete property: " +
            error.message
        );
    }
}
// =====================================================
// LANDLORD PROPERTY SETUP
// =====================================================


function showLandlordPropertySetup() {


    const setupPage =
        document.getElementById("landlordSetupPage");


    const content =
        document.getElementById("landlordSetupContent");




    document.getElementById("loginPage")
        .classList.add("hidden");


    document.getElementById("tenantRegistrationPage")
        .classList.add("hidden");


    document.getElementById("landlordRegistrationPage")
        .classList.add("hidden");


    document.getElementById("mainPage")
        .classList.add("hidden");




    setupPage.classList.remove("hidden");




    content.innerHTML = `


       <h2>Add Your Property</h2>


       <p class="setup-subtitle">
           Enter the details of the property you want to rent out.
       </p>




       <div class="registration-form">




           <div class="form-group">
               <label>Property Type</label>


               <select id="landlordPropertyType">


                   <option value="">
                       Select Property Type
                   </option>


                   <option value="Apartment">
                       Apartment
                   </option>


                   <option value="Villa">
                       Villa
                   </option>


                   <option value="Studio">
                       Studio
                   </option>


                   <option value="1RK">
                       1RK
                   </option>


               </select>
           </div>




           <div class="form-group">


               <label>Location</label>


               <input
                   type="text"
                   id="landlordPropertyLocation"
                   placeholder="e.g. Baner">


           </div>




           <div class="form-group">


               <label>City</label>


               <input
                   type="text"
                   id="landlordPropertyCity"
                   placeholder="e.g. Pune">


           </div>




           <div class="form-group">


               <label>Pincode</label>


               <input
                   type="text"
                   id="landlordPropertyPincode"
                   inputmode="numeric"
                   placeholder="e.g. 411045"
                    maxlength="6">
                  

           </div>




           <div class="form-group">


               <label>BHK</label>


               <select id="landlordPropertyBhk">


                   <option value="">
                       Select BHK
                   </option>


                   <option value="1">1 BHK</option>
                   <option value="2">2 BHK</option>
                   <option value="3">3 BHK</option>
                   <option value="4">4 BHK</option>


               </select>


           </div>




           <div class="form-group">


               <label>Area (sqft)</label>


               <input
                   type="number"
                   id="landlordPropertyArea"
                   min="1"
                   placeholder="e.g. 1000">


           </div>




           <div class="form-group">


               <label>Number of Bathrooms</label>


               <input
                   type="number"
                   id="landlordPropertyBathrooms"
                   min="1"
                   placeholder="e.g. 2">


           </div>




           <div class="form-group">


               <label>Monthly Rent (₹)</label>


               <input
                   type="number"
                   id="landlordPropertyRent"
                   min="1"
                   placeholder="e.g. 25000">


           </div>




           <div class="form-group">


               <label>Security Deposit (₹)</label>


               <input
                   type="number"
                   id="landlordPropertyDeposit"
                   min="1"
                   placeholder="e.g. 50000">


           </div>




           <div class="form-group">


               <label>Floor Number</label>


               <input
                   type="number"
                   id="landlordPropertyFloor"
                   min="0"
                   placeholder="0 for Ground Floor">


           </div>




           <div class="form-group">


               <label>Furnishing Status</label>


               <select id="landlordPropertyFurnishing">


                   <option value="">
                       Select Furnishing
                   </option>


                   <option value="Fully Furnished">
                       Fully Furnished
                   </option>


                   <option value="Semi-Furnished">
                       Semi-Furnished
                   </option>


                   <option value="Unfurnished">
                       Unfurnished
                   </option>


               </select>


           </div>




           <div class="form-group">


               <label>Available From</label>


               <input
                   type="date"
                   id="landlordPropertyAvailableFrom">


           </div>
          <div class="form-group">
    <label>Property Images</label>
    <input
        type="file"
        id="landlordPropertyImages"
        accept="image/*"
        multiple>
    <small>Select multiple property images.</small>
</div>

<div class="form-group">
    <label>Property Video</label>
    <input
        type="file"
        id="landlordPropertyVideo"
        accept="video/*">
    <small>Optional property video.</small>
</div>



           <div class="registration-actions">


               <button
                   type="button"
                   class="primary-btn"
                   onclick="saveInitialLandlordProperty()">


                   Save Property & Continue


               </button>


           </div>




           <p
               id="landlordPropertyMessage"
               class="message">
           </p>


       </div>
   `;
}


// =====================================================
// SAVE INITIAL LANDLORD PROPERTY
// =====================================================


// =====================================================
// SAVE INITIAL LANDLORD PROPERTY
// =====================================================


async function saveInitialLandlordProperty() {


    const message =
        document.getElementById("landlordPropertyMessage");


    message.textContent = "";
    message.className = "message";




    // Read text fields


    const propertyType =
        document.getElementById(
            "landlordPropertyType"
        ).value;


    const location =
        document.getElementById(
            "landlordPropertyLocation"
        ).value.trim();


    const city =
        document.getElementById(
            "landlordPropertyCity"
        ).value.trim();


    const pincode =
        document.getElementById(
            "landlordPropertyPincode"
        ).value.trim();




    // Read number fields as strings first


    const bhkValue =
        document.getElementById(
            "landlordPropertyBhk"
        ).value;


    const areaValue =
        document.getElementById(
            "landlordPropertyArea"
        ).value;


    const bathroomsValue =
        document.getElementById(
            "landlordPropertyBathrooms"
        ).value;


    const rentValue =
        document.getElementById(
            "landlordPropertyRent"
        ).value;


    const depositValue =
        document.getElementById(
            "landlordPropertyDeposit"
        ).value;


    const floorValue =
        document.getElementById(
            "landlordPropertyFloor"
        ).value;




    const furnishingStatus =
        document.getElementById(
            "landlordPropertyFurnishing"
        ).value;


    const availableFrom =
        document.getElementById(
            "landlordPropertyAvailableFrom"
        ).value;
    const imageFiles =
        document.getElementById(
            "landlordPropertyImages"
        ).files;

    const videoFile =
        document.getElementById(
            "landlordPropertyVideo"
        ).files[0];


    // Check empty fields BEFORE converting numbers


    /*if (
        !propertyType ||
        !location ||
        !city ||
        !pincode ||
        bhkValue === "" ||
        areaValue === "" ||
        bathroomsValue === "" ||
        rentValue === "" ||
        depositValue === "" ||
        floorValue === "" ||
        !furnishingStatus ||
        !availableFrom
    ) {


        message.textContent =
            "Please fill in all property fields.";


        message.classList.add("error");


        return;
    }*/
    if (!propertyType) {
        message.textContent = "Please select a property type.";
        message.classList.add("error");
        return;
    }

    if (!location) {
        message.textContent = "Please enter the property location.";
        message.classList.add("error");
        return;
    }

    if (!city) {
        message.textContent = "Please enter the city.";
        message.classList.add("error");
        return;
    }

    if (!pincode) {
        message.textContent = "Please enter the pincode.";
        message.classList.add("error");
        return;
    }

    if (bhkValue === "") {
        message.textContent = "Please enter the BHK.";
        message.classList.add("error");
        return;
    }

    if (areaValue === "") {
        message.textContent = "Please enter the area.";
        message.classList.add("error");
        return;
    }

    if (bathroomsValue === "") {
        message.textContent = "Please enter the number of bathrooms.";
        message.classList.add("error");
        return;
    }

    if (rentValue === "") {
        message.textContent = "Please enter the monthly rent.";
        message.classList.add("error");
        return;
    }

    if (depositValue === "") {
        message.textContent = "Please enter the security deposit.";
        message.classList.add("error");
        return;
    }

    if (floorValue === "") {
        message.textContent = "Please enter the floor number.";
        message.classList.add("error");
        return;
    }

    if (!furnishingStatus) {
        message.textContent = "Please select the furnishing status.";
        message.classList.add("error");
        return;
    }

    if (!availableFrom) {
        message.textContent = "Please select the available-from date.";
        message.classList.add("error");
        return;
    }


    // Convert number fields


    const bhk =
        Number(bhkValue);


    const areaSqft =
        Number(areaValue);


    const numberOfBathrooms =
        Number(bathroomsValue);


    const monthlyRent =
        Number(rentValue);


    const securityDeposit =
        Number(depositValue);


    const floorNumber =
        Number(floorValue);

    // Validate numeric values
    if (bhk <= 0) {
        message.textContent = "BHK must be greater than 0.";
        message.classList.add("error");
        return;
    }

    if (areaSqft <= 0) {
        message.textContent = "Area must be greater than 0.";
        message.classList.add("error");
        return;
    }

    if (numberOfBathrooms <= 0) {
        message.textContent = "Number of bathrooms must be greater than 0.";
        message.classList.add("error");
        return;
    }

    if (monthlyRent <= 0) {
        message.textContent = "Monthly rent must be greater than 0.";
        message.classList.add("error");
        return;
    }

    if (securityDeposit <= 0) {
        message.textContent = "Security deposit must be greater than 0.";
        message.classList.add("error");
        return;
    }

    if (floorNumber < 0) {
        message.textContent = "Floor number cannot be negative.";
        message.classList.add("error");
        return;
    }
    if (!/^\d{6}$/.test(pincode)) {
        message.textContent = "Pincode must be exactly 6 digits.";
        message.classList.add("error");
        return;
    }
    if (numberOfBathrooms > 20) {
        message.textContent = "Number of bathrooms cannot exceed 20.";
        message.classList.add("error");
        return;
    }
    if (floorNumber > 100) {
        message.textContent = "Floor number cannot exceed 100.";
        message.classList.add("error");
        return;
    }
    // Generate Property ID


    const propertyId =
        "P" +
        Date.now()
            .toString()
            .slice(-8);

    // Prepare property data


    const propertyData = {


        propertyId: propertyId,


        landlordId: currentUser.userId,


        propertyType: propertyType,


        location: location,


        city: city,


        pincode: pincode,


        bhk: bhk,


        areaSqft: areaSqft,


        numberOfBathrooms:
        numberOfBathrooms,


        monthlyRent:
        monthlyRent,


        securityDeposit:
        securityDeposit,


        floorNumber:
        floorNumber,


        furnishingStatus:
        furnishingStatus,


        availableFrom:
        availableFrom,


        propertyStatus:
            "Available"
    };




    try {


        const savedProperty =
            await apiFetch(
                "/api/properties",
                {
                    method: "POST",
                    body: JSON.stringify(
                        propertyData
                    )
                }
            );

        const createdPropertyId =
            savedProperty.propertyId;


// Upload property images
        if (imageFiles.length > 0) {

            const formData = new FormData();

            for (const file of imageFiles) {
                formData.append("files", file);
            }

            await apiFetch(
                `/api/properties/${createdPropertyId}/images/multiple`,
                {
                    method: "POST",
                    body: formData
                }
            );
        }


// Upload property video
        if (videoFile) {

            const formData = new FormData();

            formData.append(
                "file",
                videoFile
            );

            await apiFetch(
                `/api/properties/${createdPropertyId}/video`,
                {
                    method: "POST",
                    body: formData
                }
            );
        }


// Continue to amenity setup

        showLandlordPropertyAmenitySetup(
            createdPropertyId
        );


    } catch (error) {


        message.textContent =
            error.message;


        message.classList.add("error");
    }
}


// =====================================================
// PROPERTY AMENITY SETUP
// =====================================================


async function showLandlordPropertyAmenitySetup(propertyId) {


    const setupPage =
        document.getElementById("landlordSetupPage");


    const content =
        document.getElementById("landlordSetupContent");




    setupPage.classList.remove("hidden");




    content.innerHTML = `


       <h2>Select Property Amenities</h2>


       <p class="setup-subtitle">
           Select the amenities available in your property.
       </p>


       <div id="landlordPropertyAmenities">
           Loading amenities...
       </div>


       <div class="registration-actions">


           <button
               type="button"
               class="primary-btn"
               onclick="saveInitialPropertyAmenities('${propertyId}')">


               Save & Continue


           </button>


       </div>


       <p
           id="landlordAmenityMessage"
           class="message">
       </p>
   `;




    try {


        const amenities =
            await apiFetch(
                "/api/amenities",
                {
                    method: "GET"
                }
            );




        const container =
            document.getElementById(
                "landlordPropertyAmenities"
            );




        if (!amenities || amenities.length === 0) {


            container.innerHTML =
                "<p>No amenities are currently available.</p>";


            return;
        }




        container.innerHTML = `


           <div class="amenity-grid">


               ${amenities.map(amenity => `


                   <label class="amenity-option">


                       <input
                           type="checkbox"
                           name="landlordPropertyAmenity"
                           value="${safe(amenity.amenityId)}"
                       >


                       <span>
                           ${safe(amenity.amenityName)}
                       </span>


                   </label>


               `).join("")}


           </div>
       `;




    } catch (error) {


        document.getElementById(
            "landlordPropertyAmenities"
        ).innerHTML =
            `<p class="error">${safe(error.message)}</p>`;
    }
}
// =====================================================
// SAVE INITIAL PROPERTY AMENITIES
// =====================================================


async function saveInitialPropertyAmenities(propertyId) {


    const message =
        document.getElementById(
            "landlordAmenityMessage"
        );


    message.textContent = "";
    message.className = "message";




    const selectedAmenities =
        document.querySelectorAll(
            'input[name="landlordPropertyAmenity"]:checked'
        );




    const amenityIds =
        Array.from(selectedAmenities)
            .map(
                checkbox => checkbox.value
            );




    if (amenityIds.length === 0) {


        message.textContent =
            "Please select at least one amenity.";


        message.classList.add("error");


        return;
    }




    const propertyAmenityData = {


        propertyId: propertyId,


        amenityIds: amenityIds
    };




    try {


        await apiFetch(
            "/api/property-amenities",
            {
                method: "POST",


                body:
                    JSON.stringify(
                        propertyAmenityData
                    )
            }
        );




        alert(
            "Property and amenities added successfully!"
        );




        document.getElementById(
            "landlordSetupPage"
        ).classList.add("hidden");




        showDashboard();




    } catch (error) {


        message.textContent =
            error.message;


        message.classList.add("error");
    }
}
// LANDLORD APPLICATIONS
async function loadLandlordApplications() {
    const container = document.getElementById("landlordApplications");
    container.innerHTML = "Loading...";


    try {
        const properties = await apiFetch(`/api/properties/landlord/${currentUser.userId}`, {
            method: "GET"
        });


        let allApplications = [];
        for (const property of properties) {
            try {
                const applications = await apiFetch(`/api/applications/property/${property.propertyId}`, {
                    method: "GET"
                });
                allApplications = allApplications.concat(applications);
            } catch (error) {
                console.error(`Could not load applications for ${property.propertyId}`, error);
            }
        }


        if (allApplications.length === 0) {
            container.innerHTML = '<div class="empty">No applications found.</div>';
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
               <td>${safe(app.applicationId)}</td>
               <td>${safe(app.tenantId)}</td>
               <td>${safe(app.propertyId)}</td>
               <td>${safe(app.proposedMoveInDate)}</td>
               <td>${safe(app.numberOfOccupants)}</td>
               <td>${statusBadge(app.applicationStatus)}</td>
               <td>
                 ${
            app.applicationStatus === "Pending"
                ? `<div class="action-row">
                         <button class="approve-btn" onclick="updateApplicationStatus('${safe(app.applicationId)}', 'Approved')">Approve</button>
                         <button class="reject-btn" onclick="updateApplicationStatus('${safe(app.applicationId)}', 'Rejected')">Reject</button>
                       </div>`
                : "-"
        }
               </td>
             </tr>
           `).join("")}
         </tbody>
       </table>
     </div>
   `;
    } catch (error) {
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}


async function updateApplicationStatus(applicationId, status) {
    const confirmed = confirm(`Are you sure you want to mark ${applicationId} as ${status}?`);
    if (!confirmed) return;


    try {
        await apiFetch(`/api/applications/${applicationId}/status`, {
            method: "PUT",
            body: JSON.stringify({ applicationStatus: status })
        });


        alert(`Application ${status.toLowerCase()} successfully.`);
        await loadLandlordApplications();
    } catch (error) {
        alert("Could not update application: " + error.message);
    }
}


async function withdrawApplication(applicationId) {
    const confirmed = confirm(`Are you sure you want to withdraw ${applicationId}?`);
    if (!confirmed) return;


    try {
        await apiFetch(`/api/applications/${applicationId}/withdraw`, {
            method: "PUT"
        });


        alert("Application withdrawn successfully.");
        await loadTenantApplications();
    } catch (error) {
        alert("Could not withdraw application: " + error.message);
    }
}


// LANDLORD LEASES
async function loadLandlordLeases() {
    const container = document.getElementById("landlordLeases");
    container.innerHTML = "Loading...";


    try {
        const properties = await apiFetch(`/api/properties/landlord/${currentUser.userId}`, {
            method: "GET"
        });


        let allLeases = [];
        for (const property of properties) {
            try {
                const leases = await apiFetch(`/api/leases/property/${property.propertyId}`, {
                    method: "GET"
                });
                allLeases = allLeases.concat(leases);
            } catch (error) {
                console.error(`Could not load leases for ${property.propertyId}`, error);
            }
        }


        if (allLeases.length === 0) {
            container.innerHTML = '<div class="empty">No leases found.</div>';
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
               <td>${formatNumber(lease.monthlyRent)}</td>
               <td>${statusBadge(lease.leaseStatus)}</td>
             </tr>
           `).join("")}
         </tbody>
       </table>
     </div>
   `;
    } catch (error) {
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}


// ADMIN DASHBOARD
async function loadAdminDashboard() {
    await loadAdminStatistics();
    await loadAdminLandlords();
    await loadAdminTenants();
    await loadAdminProperties();
    await loadAdminApplications();
}


async function loadAdminStatistics() {
    try {
        const stats = await apiFetch("/api/admin/stats", { method: "GET" });


        document.getElementById("adminTenantCount").textContent = stats.totalTenants;
        document.getElementById("adminLandlordCount").textContent = stats.totalLandlords;
        document.getElementById("adminPropertyCount").textContent = stats.totalProperties;
        document.getElementById("adminApplicationCount").textContent = stats.totalApplications;
        document.getElementById("adminLeaseCount").textContent = stats.totalLeases;
        document.getElementById("adminPendingLandlordCount").textContent = stats.pendingLandlords;
    } catch (error) {
        console.error("Could not load admin statistics", error);
    }
}


async function loadAdminLandlords() {
    const container = document.getElementById("adminLandlords");
    container.innerHTML = "Loading...";


    try {
        const landlords = await apiFetch("/api/admin/landlords", { method: "GET" });


        if (!landlords || landlords.length === 0) {
            container.innerHTML = '<div class="empty">No landlords found.</div>';
            return;
        }


        container.innerHTML = `
     <div class="table-wrapper">
       <table class="data-table">
         <thead>
           <tr>
             <th>Landlord ID</th>
             <th>Name</th>
             <th>Email</th>
             <th>Phone</th>
             <th>Verification</th>
             <th>Action</th>
           </tr>
         </thead>
         <tbody>
           ${landlords.map(landlord => `
             <tr>
               <td>${safe(landlord.landlordId)}</td>
               <td>${safe(landlord.firstName)}${safe(landlord.lastName)}</td>
               <td>${safe(landlord.email)}</td>
               <td>${safe(landlord.phone)}</td>
               <td>${statusBadge(landlord.verificationStatus)}</td>
               <td>
                 ${
            landlord.verificationStatus === "Pending"
                ? `<div class="action-row">
                         <button class="approve-btn" onclick="updateLandlordVerification('${safe(landlord.landlordId)}', 'Verified')">Verify</button>
                         <button class="reject-btn" onclick="updateLandlordVerification('${safe(landlord.landlordId)}', 'Rejected')">Reject</button>
                       </div>`
                : "-"
        }
               </td>
             </tr>
           `).join("")}
         </tbody>
       </table>
     </div>
   `;
    } catch (error) {
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}


async function updateLandlordVerification(landlordId, status) {
    const action = status === "Verified" ? "verify" : "reject";
    const confirmed = confirm(`Are you sure you want to ${action} landlord ${landlordId}?`);
    if (!confirmed) return;


    try {
        await apiFetch(`/api/admin/landlords/${landlordId}/verification`, {
            method: "PUT",
            body: JSON.stringify({ status: status })
        });


        alert(`Landlord ${status.toLowerCase()} successfully.`);
        await loadAdminStatistics();
        await loadAdminLandlords();
    } catch (error) {
        alert("Could not update landlord verification: " + error.message);
    }
}


async function loadAdminTenants() {
    const container = document.getElementById("adminTenants");
    container.innerHTML = "Loading...";


    try {
        const tenants = await apiFetch("/api/admin/tenants", { method: "GET" });


        if (!tenants || tenants.length === 0) {
            container.innerHTML = '<div class="empty">No tenants found.</div>';
            return;
        }


        container.innerHTML = `
     <div class="table-wrapper">
       <table class="data-table">
         <thead>
           <tr>
             <th>Tenant ID</th>
             <th>Name</th>
             <th>Email</th>
             <th>Phone</th>
             <th>Tenant Type</th>
             <th>Pets</th>
           </tr>
         </thead>
         <tbody>
           ${tenants.map(tenant => `
             <tr>
               <td>${safe(tenant.tenantId)}</td>
               <td>${safe(tenant.firstName)}${safe(tenant.lastName)}</td>
               <td>${safe(tenant.email)}</td>
               <td>${safe(tenant.phone)}</td>
               <td>${safe(tenant.tenantType)}</td>
               <td>${safe(tenant.hasPets)}</td>
             </tr>
           `).join("")}
         </tbody>
       </table>
     </div>
   `;
    } catch (error) {
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}


async function loadAdminProperties() {
    const container = document.getElementById("adminProperties");
    container.innerHTML = "Loading...";


    try {
        const properties = await apiFetch("/api/admin/properties", { method: "GET" });


        if (!properties || properties.length === 0) {
            container.innerHTML = '<div class="empty">No properties found.</div>';
            return;
        }


        container.innerHTML = `
     <div class="table-wrapper">
       <table class="data-table">
         <thead>
           <tr>
             <th>Property</th>
             <th>Landlord</th>
             <th>Type</th>
             <th>Location</th>
             <th>BHK</th>
             <th>Area</th>
             <th>Rent</th>
             <th>Status</th>
           </tr>
         </thead>
         <tbody>
           ${properties.map(property => `
             <tr>
               <td>${safe(property.propertyId)}</td>
               <td>${safe(property.landlordId)}</td>
               <td>${safe(property.propertyType)}</td>
               <td>${safe(property.location)},${safe(property.city)}</td>
               <td>${safe(property.bhk)}</td>
               <td>${safe(property.areaSqft)} sqft</td>
               <td>${formatNumber(property.monthlyRent)}</td>
               <td>${statusBadge(property.propertyStatus)}</td>
             </tr>
           `).join("")}
         </tbody>
       </table>
     </div>
   `;
    } catch (error) {
        container.innerHTML = `<div class="error">${safe(error.message)}</div>`;
    }
}

async function loadAdminApplications() {

    const container =
        document.getElementById("adminApplications");

    container.innerHTML = "Loading...";


    try {

        const applications =
            await apiFetch("/api/applications", {
                method: "GET"
            });


        if (!applications || applications.length === 0) {

            container.innerHTML =
                '<div class="empty">No applications found.</div>';

            return;
        }


        container.innerHTML = `
            <div class="table-wrapper">

                <table class="data-table">

                    <thead>

                        <tr>
                            <th>Application ID</th>
                            <th>Tenant ID</th>
                            <th>Property ID</th>
                            <th>Application Date</th>
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
                                    ${safe(app.tenantId)}
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
// UTILITY FUNCTIONS
function formatNumber(value) {
    if (value === null || value === undefined) {
        return "-";
    }
    return Number(value).toLocaleString("en-IN");
}


function safe(value) {
    if (value === null || value === undefined || value === "") {
        return "-";
    }
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}


function statusBadge(status) {
    if (!status) return "-";


    const normalized = status.toLowerCase();
    let className = "status ";


    if (normalized === "approved") {
        className += "status-approved";
    } else if (normalized === "pending") {
        className += "status-pending";
    } else if (normalized === "rejected") {
        className += "status-rejected";
    } else if (normalized === "withdrawn") {
        className += "status-withdrawn";
    } else if (normalized === "active") {
        className += "status-active";
    } else if (normalized === "completed") {
        className += "status-completed";
    }


    return `<span class="${className}">${safe(status)}</span>`;
}


function showTenantPreferenceSetup() {
    const setupPage = document.getElementById("tenantSetupPage");
    const content = document.getElementById("setupStepContent");


    document.getElementById("loginPage").classList.add("hidden");
    document.getElementById("tenantRegistrationPage").classList.add("hidden");
    document.getElementById("mainPage").classList.add("hidden");


    setupPage.classList.remove("hidden");


    content.innerHTML = `
   <h2>Set Your Rental Preferences</h2>
   <p class="setup-subtitle">Tell us what kind of property you are looking for.</p>
   <div class="registration-form">
     <div class="form-group">
       <label>Preferred Location</label>
       <input type="text" id="setupPreferredLocation" placeholder="e.g. Baner">
     </div>
     <div class="form-group">
       <label>Minimum Budget (₹)</label>
       <input type="number" id="setupMinimumBudget" min="0" placeholder="e.g. 15000">
     </div>
     <div class="form-group">
       <label>Maximum Budget (₹)</label>
       <input type="number" id="setupMaximumBudget" min="0" placeholder="e.g. 30000">
     </div>
     <div class="form-group">
       <label>Property Type</label>
       <select id="setupPropertyType">
         <option value="">Select Property Type</option>
         <option value="Apartment">Apartment</option>
         <option value="Villa">Villa</option>
         <option value="Studio">Studio</option>
         <option value="1RK">1RK</option>
       </select>
     </div>
     <div class="form-group">
       <label>Preferred BHK</label>
       <select id="setupBhk">
         <option value="">Select BHK</option>
         <option value="1">1 BHK</option>
         <option value="2">2 BHK</option>
         <option value="3">3 BHK</option>
         <option value="4">4 BHK</option>
       </select>
     </div>
     <div class="form-group">
       <label>Minimum Area (sqft)</label>
       <input type="number" id="setupMinimumArea" min="0" placeholder="e.g. 800">
     </div>
     <div class="form-group">
       <label>Minimum Bathrooms</label>
       <input type="number" id="setupMinimumBathrooms" min="1" placeholder="e.g. 2">
     </div>
     <div class="form-group">
       <label>Number of Occupants</label>
       <input type="number" id="setupOccupants" min="1" placeholder="e.g. 2">
     </div>
     <div class="form-group">
       <label>Furnishing Preference</label>
       <select id="setupFurnishing">
         <option value="">Select Furnishing</option>
         <option value="Fully Furnished">Fully Furnished</option>
         <option value="Semi-Furnished">Semi-Furnished</option>
         <option value="Unfurnished">Unfurnished</option>
       </select>
     </div>
     <div class="form-group">
       <label>Preferred Floor</label>
       <select id="setupFloor">
         <option value="">Select Floor Preference</option>
         <option value="Any">Any floor</option>
         <option value="Ground">Ground floor</option>
         <option value="Low">Low floor</option>
         <option value="High">High floor</option>
       </select>
     </div>
     <div class="form-group">
       <label>Lease Duration (months)</label>
       <input type="number" id="setupLeaseDuration" min="1" placeholder="e.g. 12">
     </div>
     <div class="form-group">
       <label>Preferred Move-In Date</label>
       <input type="date" id="setupMoveInDate">
     </div>
     <div class="registration-actions">
       <button type="button" class="primary-btn" onclick="saveInitialTenantPreferences()">Save & Continue</button>
     </div>
     <p id="setupPreferenceMessage" class="message"></p>
   </div>
 `;
}


async function saveInitialTenantPreferences() {
    const message = document.getElementById("setupPreferenceMessage");
    message.textContent = "";
    message.className = "message";


    const preferredLocation = document.getElementById("setupPreferredLocation").value.trim();
    const minimumBudget = Number(document.getElementById("setupMinimumBudget").value);
    const maximumBudget = Number(document.getElementById("setupMaximumBudget").value);
    const preferredPropertyType = document.getElementById("setupPropertyType").value;
    const preferredBhk = Number(document.getElementById("setupBhk").value);
    const minimumAreaSqft = Number(document.getElementById("setupMinimumArea").value);
    const minimumBathrooms = Number(document.getElementById("setupMinimumBathrooms").value);
    const numberOfOccupants = Number(document.getElementById("setupOccupants").value);
    const furnishingPreference = document.getElementById("setupFurnishing").value;
    const preferredFloor = document.getElementById("setupFloor").value;
    const minimumLeaseDuration = Number(document.getElementById("setupLeaseDuration").value);
    const preferredMoveInDate = document.getElementById("setupMoveInDate").value;


    if (
        !preferredLocation ||
        !minimumBudget ||
        !maximumBudget ||
        !preferredPropertyType ||
        !preferredBhk ||
        !minimumAreaSqft ||
        !minimumBathrooms ||
        !numberOfOccupants ||
        !furnishingPreference ||
        !preferredFloor ||
        !minimumLeaseDuration ||
        !preferredMoveInDate
    ) {
        message.textContent = "Please fill in all rental preference fields.";
        message.classList.add("error");
        return;
    }

    if (
        minimumBudget <= 0 ||
        maximumBudget <= 0 ||
        preferredBhk <= 0 ||
        minimumAreaSqft <= 0 ||
        minimumBathrooms <= 0 ||
        numberOfOccupants <= 0 ||
        minimumLeaseDuration <= 0
    ) {
        message.textContent = "All numeric preference values must be greater than 0.";
        message.classList.add("error");
        return;
    }


    if (minimumBudget > maximumBudget) {
        message.textContent = "Minimum budget cannot be greater than maximum budget.";
        message.classList.add("error");
        return;
    }


    const preferenceId =
        "PR" + Date.now().toString().slice(-8);


    const preferenceData = {
        preferenceId: preferenceId,
        tenantId: currentUser.userId,
        preferredLocation: preferredLocation,
        minimumBudget: minimumBudget,
        maximumBudget: maximumBudget,
        preferredPropertyType: preferredPropertyType,
        preferredBhk: preferredBhk,
        minimumAreaSqft: minimumAreaSqft,
        minimumBathrooms: minimumBathrooms,
        numberOfOccupants: numberOfOccupants,
        furnishingPreference: furnishingPreference,
        preferredFloor: preferredFloor,
        minimumLeaseDuration: minimumLeaseDuration,
        preferredMoveInDate: preferredMoveInDate
    };


    try {
        await apiFetch("/api/tenant-preferences", {
            method: "POST",
            body: JSON.stringify(preferenceData)
        });
        showAmenityPreferenceSetup();
    } catch (error) {
        message.textContent = error.message;
        message.classList.add("error");
    }
}


async function showAmenityPreferenceSetup() {
    const setupPage = document.getElementById("tenantSetupPage");
    const content = document.getElementById("setupStepContent");


    setupPage.classList.remove("hidden");


    content.innerHTML = `
   <h2>Amenity Preferences</h2>
   <p class="setup-subtitle">Select the amenities that are important to you.</p>
   <div id="setupAmenities">Loading amenities...</div>
   <div class="registration-actions">
     <button type="button" class="primary-btn" onclick="saveInitialAmenityPreferences()">Save & Continue</button>
   </div>
   <p id="setupAmenityMessage" class="message"></p>
 `;


    try {
        const amenities = await apiFetch("/api/amenities", { method: "GET" });
        const container = document.getElementById("setupAmenities");


        if (!amenities || amenities.length === 0) {
            container.innerHTML = "<p>No amenities are currently available.</p>";
            return;
        }


        container.innerHTML = `
     <div class="amenity-grid">
       ${amenities.map(amenity => `
         <label class="amenity-option">
           <input type="checkbox" name="setupAmenity" value="${safe(amenity.amenityId)}">
           <span>${safe(amenity.amenityName)}</span>
         </label>
       `).join("")}
     </div>
   `;
    } catch (error) {
        document.getElementById("setupAmenities").innerHTML = `<p class="error">${safe(error.message)}</p>`;
    }
}


async function saveInitialAmenityPreferences() {
    const message = document.getElementById("setupAmenityMessage");
    message.textContent = "";
    message.className = "message";


    const selectedAmenities = document.querySelectorAll('input[name="setupAmenity"]:checked');
    const amenityIds = Array.from(selectedAmenities).map(checkbox => checkbox.value);


    if (amenityIds.length === 0) {
        message.textContent = "Please select at least one amenity.";
        message.classList.add("error");
        return;
    }


    const amenityPreferenceData = {
        tenantId: currentUser.userId,
        amenityIds: amenityIds
    };


    try {
        await apiFetch("/api/tenant-amenity-preferences", {
            method: "POST",
            body: JSON.stringify(amenityPreferenceData)
        });


        alert("Your preferences have been saved successfully!");
        document.getElementById("tenantSetupPage").classList.add("hidden");
        showDashboard();
    } catch (error) {
        message.textContent = error.message;
        message.classList.add("error");
    }
}





