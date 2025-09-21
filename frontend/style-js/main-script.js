
// Base URL for API
const API_BASE_URL = 'http://localhost:8080';
let currentUser = null;
let jwtToken = null;
let notifications = [];

// Initialize SweetAlert2
const Toast = Swal.mixin({
    toast: true,
    position: 'top-end',
    showConfirmButton: false,
    timer: 1000,
    timerProgressBar: true,
    didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer)
        toast.addEventListener('mouseleave', Swal.resumeTimer)
    }
});

// Check if user is already logged in
$(document).ready(function () {
    const savedToken = localStorage.getItem('jwtToken');
    const savedUser = localStorage.getItem('currentUser');

    if (savedToken && savedUser) {
        jwtToken = savedToken;
        currentUser = JSON.parse(savedUser);
        updateUIAfterLogin();
        loadDashboardData();
    }

    // Login form submission
    $('#loginForm').on('submit', function (e) {
        e.preventDefault();
        loginUser();
    });

    // Register form submission
    $('#registerForm').on('submit', function (e) {
        e.preventDefault();
        registerUser();
    });

    // Report form submission
    $('#reportForm').on('submit', function (e) {
        e.preventDefault();
        reportLostItem();
    });

    // Found item form submission
    $('#foundForm').on('submit', function (e) {
        e.preventDefault();
        reportFoundItem();
    });

    // Profile form submission
    $('#profileForm').on('submit', function (e) {
        e.preventDefault();
        updateProfile();
    });

    // Security form submission
    $('#securityForm').on('submit', function (e) {
        e.preventDefault();
        updatePassword();
    });

    // Claim form submission
    $('#claimForm').on('submit', function (e) {
        e.preventDefault();
        submitClaim();
    });

    // Logout button
    $('#logoutBtn').on('click', function (e) {
        e.preventDefault();
        logoutUser();
    });

    // Notifications modal
    $('#notificationsModal').on('show.bs.modal', function () {
        loadNotifications();
    });

    // Mark all as read button
    $('#markAllAsReadBtn').on('click', function () {
        markAllNotificationsAsRead();
    });

    // Profile link
    $('#profileLink').on('click', function (e) {
        e.preventDefault();
        showProfileSection();
    });

    // Dashboard link
    $('#dashboardLink').on('click', function (e) {
        e.preventDefault();
        showDashboardSection();
    });

    // Admin link
    $('#adminLink').on('click', function (e) {
        e.preventDefault();
        showAdminSection();
    });

    // Home link
    $('#homeLink').on('click', function (e) {
        e.preventDefault();
        showHomeSection();
    });

    // Proof upload area
    $('#proofUploadArea').on('click', function () {
        $('#claimProof').click();
    });

    // Claim proof change
    $('#claimProof').on('change', function (e) {
        handleProofUpload(e);
    });

    // Filter buttons
    $('[data-filter]').on('click', function () {
        const filter = $(this).data('filter');
        $('[data-filter]').removeClass('active');
        $(this).addClass('active');
        filterItems(filter);
    });

    // Open Add Hotel form
    $('#addHotelBtn').on('click', function () {
        $('#addHotelModal').modal('show');
    });

    // Open Add Staff form
    $('#addStaffBtn').on('click', function () {
        $('#addStaffModal').modal('show');
    });

    // Submit Add Hotel form
    $('#addHotelForm').on('submit', function (e) {
        e.preventDefault();
        addHotel();
    });

    $('#addStaffModal').on('show.bs.modal', function () {
        loadHotelsForStaffModal();
    });

    // Submit Add Staff form
    $('#addStaffForm').on('submit', function (e) {
        e.preventDefault();
        addStaff();
    });
});

// Show success message
function showSuccess(message) {
    Toast.fire({
        icon: 'success',
        title: message
    });
}

// Show error message
function showError(message) {
    Toast.fire({
        icon: 'error',
        title: message
    });
}

// Show confirmation dialog
function showConfirm(message, confirmCallback) {
    Swal.fire({
        title: 'Are you sure?',
        text: message,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d4af37',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Yes, proceed!'
    }).then((result) => {
        if (result.isConfirmed) {
            confirmCallback();
        }
    });
}

// Login function
function loginUser() {
    const email = $('#loginEmail').val();
    const password = $('#loginPassword').val();
    const isStaff = $('#userTypeCheck').is(':checked');

    const endpoint = isStaff ? '/auth/login' : '/auth/guest/login';
    const credentials = isStaff ?
        {email: email, password: password} :
        {email: email, password: password};

    $.ajax({
        url: API_BASE_URL + endpoint,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(credentials),
        success: function (response) {
            if (response.status === 200) {
                jwtToken = response.data.token;
                currentUser = response.data.user;

                // Store in localStorage
                localStorage.setItem('jwtToken', jwtToken);
                localStorage.setItem('currentUser', JSON.stringify(currentUser));

                // Update UI
                updateUIAfterLogin();
                loadDashboardData();

                // Close modal
                $('#loginModal').modal('hide');

                // Show success message
                showSuccess('Login successful!');
            }
        },
        error: function (xhr) {
            showError('Login failed: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

// Register function
function registerUser() {
    const name = $('#registerName').val();
    const email = $('#registerEmail').val();
    const phone = $('#registerPhone').val();
    const password = $('#registerPassword').val();

    const userData = {
        name: name,
        email: email,
        phone: phone || '',
        password: password
    };

    $.ajax({
        url: API_BASE_URL + '/auth/guest/register',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(userData),
        success: function (response) {
            if (response.status === 200) {
                showSuccess('Registration successful! Please login.');
                $('#registerModal').modal('hide');
                $('#loginModal').modal('show');
            }
        },
        error: function (xhr) {
            showError('Registration failed: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

// Update profile
function updateProfile() {
    const name = $('#profileName').val();
    const phone = $('#profilePhone').val();
    const email = $('#profileEmail').val();

    const profileData = {
        name: name,
        phone: phone,
        email: email
    };

    // Determine endpoint based on user role
    const endpoint = currentUser.role === 'GUEST' ? '/guests/profile' : '/staff/profile';

    $.ajax({
        url: API_BASE_URL + endpoint,
        type: 'PUT',
        contentType: 'application/json',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        },
        data: JSON.stringify(profileData),
        success: function (response) {
            if (response.status === 200) {
                // Update current user data
                currentUser.name = name;
                currentUser.phone = phone;
                currentUser.email = email;
                localStorage.setItem('currentUser', JSON.stringify(currentUser));

                // Update UI
                $('#profileUserName').text(name);
                $('#profileUserEmail').text(email);

                showSuccess('Profile updated successfully!');
            }
        },
        error: function (xhr) {
            showError('Failed to update profile: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

// Update password
function updatePassword() {
    const currentPassword = $('#currentPassword').val();
    const newPassword = $('#newPassword').val();
    const confirmPassword = $('#confirmPassword').val();

    if (newPassword !== confirmPassword) {
        showError('New passwords do not match!');
        return;
    }

    const passwordData = {
        oldPassword: currentPassword,
        newPassword: newPassword
    };

    // Determine endpoint based on user role
    const endpoint = currentUser.role === 'GUEST' ? '/guests/me/password' : '/staff/me/password';

    $.ajax({
        url: API_BASE_URL + endpoint,
        type: 'PUT',
        contentType: 'application/json',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        },
        data: JSON.stringify(passwordData),
        success: function (response) {
            if (response.status === 200) {
                showSuccess('Password updated successfully!');
                $('#securityForm')[0].reset();
            }
        },
        error: function (xhr) {
            showError('Failed to update password: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

// Upload image to imgbb and return the hosted URL
async function uploadImageToImgbb(file) {
    const formData = new FormData();
    formData.append("image", file);

    // Show "Uploading…" message if there’s a preview container available
    if ($('#proofPreview').length) {
        $('#proofPreview').html('<div class="alert alert-info">Uploading image...</div>');
    }

    try {
        const response = await $.ajax({
            url: `https://api.imgbb.com/1/upload?key=c8855e05a325a45010e5109d472fce84`,
            type: "POST",
            data: formData,
            processData: false,
            contentType: false
        });

        if (response && response.data && response.data.url) {
            // Show success message only for proof uploads
            if ($('#proofPreview').length) {
                $('#proofPreview').html(`
                    <div class="alert alert-success">
                        <i class="fas fa-check-circle me-2"></i>
                        Image uploaded successfully!
                    </div>
                `);
            }
            return response.data.url;
        } else {
            throw new Error("Invalid response from imgbb");
        }
    } catch (err) {
        console.error("Image upload failed:", err);
        showError("Failed to upload image. Please try again.");
        if ($('#proofPreview').length) {
            $('#proofPreview').html(`
                <div class="alert alert-danger">
                    <i class="fas fa-exclamation-circle me-2"></i>
                    Upload failed
                </div>
            `);
        }
        return null;
    }
}

// Report lost item
async function reportLostItem() {
    if (!currentUser || !jwtToken) {
        showError('Please login first to report a lost item.');
        $('#reportModal').modal('hide');
        $('#loginModal').modal('show');
        return;
    }

    const file = $('#lostItemImage')[0]?.files[0];
    let imageUrl = '';

    if (file) {
        imageUrl = await uploadImageToImgbb(file);
        if (!imageUrl) return;
    }

    const itemData = {
        guestId: currentUser.id,
        title: $('#itemName').val(),
        description: $('#itemDescription').val(),
        // dateLost: $('#lostDate').val(),
        // dateLost: $('#lostDate').val().split('T')[0],
        locationLost: $('#lostLocation').val(),
        imagePath: imageUrl
    };

    console.log("Sending Lost Item:", itemData);

    $.ajax({
        url: API_BASE_URL + '/lost-items',
        type: 'POST',
        contentType: 'application/json',
        headers: {'Authorization': 'Bearer ' + jwtToken},
        data: JSON.stringify(itemData),
        success: function (response) {
            if (response.status === 200 || response.status === 201) {
                showSuccess('Item reported successfully!');
                $('#reportModal').modal('hide');
                loadDashboardData();
            }
        },
        error: function (xhr) {
            console.error("Lost Item Error:", xhr);
            showError('Failed to report item: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

// Report found item
async function reportFoundItem() {
    if (!currentUser || !jwtToken) {
        showError('Please login first to report a found item.');
        $('#foundModal').modal('hide');
        $('#loginModal').modal('show');
        return;
    }

    if (!currentUser.role || currentUser.role === 'GUEST') {
        showError('Only hotel staff can report found items.');
        $('#foundModal').modal('hide');
        return;
    }

    const file = $('#foundItemImage')[0]?.files[0];
    let imageUrl = '';

    if (file) {
        imageUrl = await uploadImageToImgbb(file);
        if (!imageUrl) return;
    }

    const itemData = {
        staffId: currentUser.staffId,
        title: $('#foundItemName').val(),
        description: $('#foundItemDescription').val(),
        // foundDate: $('#foundDate').val(),   // must be "YYYY-MM-DD"
        // foundDate: $('#foundDate').val().split('T')[0],
        locationFound: $('#foundLocation').val(),
        imagePath: imageUrl
    };

    console.log("Sending Found Item:", itemData);

    $.ajax({
        url: API_BASE_URL + '/found-items',
        type: 'POST',
        contentType: 'application/json',
        headers: {'Authorization': 'Bearer ' + jwtToken},
        data: JSON.stringify(itemData),
        success: function (response) {
            if (response.status === 200 || response.status === 201) {
                showSuccess('Found item reported successfully!');
                $('#foundModal').modal('hide');
                loadDashboardData();
            }
        },
        error: function (xhr) {
            console.error("Found Item Error:", xhr);
            showError('Failed to report found item: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

// Submit claim for an item
function submitClaim() {
    const itemId = $('#claimItemId').val();
    const message = $('#claimMessage').val();
    const proofImageUrl = $('#claimProof').data("uploadedUrl") || null;

    const claimData = {
        foundItemId: itemId,
        message: message,
        proofImageUrl: proofImageUrl
    };

    $.ajax({
        url: API_BASE_URL + '/claims',
        type: 'POST',
        contentType: 'application/json',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        },
        data: JSON.stringify(claimData),
        success: function (response) {
            if (response.status === 200) {
                showSuccess('Claim submitted successfully! The admin will review your request.');
                $('#claimItemModal').modal('hide');
                loadDashboardData();
            }
        },
        error: function (xhr) {
            showError('Failed to submit claim: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

// Handle proof upload
function handleProofUpload(e) {
    const file = e.target.files[0];
    if (!file) return;

    const apiKey = "c8855e05a325a45010e5109d472fce84"; // ImgBB API key
    const formData = new FormData();
    formData.append("image", file);

    // Show "uploading..." while waiting
    $('#proofPreview').html(`
        <div class="alert alert-warning">
            <i class="fas fa-spinner fa-spin me-2"></i>
            Uploading ${file.name}...
        </div>
    `);

    $.ajax({
        url: `https://api.imgbb.com/1/upload?key=${apiKey}`,
        type: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function (response) {
            if (response && response.data && response.data.url) {
                const imageUrl = response.data.url;

                // Save this URL globally to attach in submitClaim()
                $('#claimProof').data("uploadedUrl", imageUrl);

                $('#proofPreview').html(`
                    <div class="alert alert-success">
                        <i class="fas fa-check-circle me-2"></i>
                        Uploaded successfully! <br>
                        <a href="${imageUrl}" target="_blank">View Image</a>
                    </div>
                `);
            } else {
                $('#proofPreview').html(`
                    <div class="alert alert-danger">Upload failed, please try again.</div>
                `);
            }
        },
        error: function (err) {
            console.error("Upload failed:", err);
            $('#proofPreview').html(`
                <div class="alert alert-danger">Error uploading file.</div>
            `);
        }
    });
}

// Load dashboard data based on user role
function loadDashboardData() {
    if (!currentUser || !jwtToken) return;

    // Update dashboard title based on user role
    if (currentUser.role === 'ADMIN') {
        $('#dashboardTitle').text('Admin Dashboard');
        $('#adminPanelSection').removeClass('d-none');
        $('#claimRequestsSection').removeClass('d-none');
        $('#addItemBtn').removeClass('d-none');
        loadAdminData();
    } else if (currentUser.role === 'STAFF') {
        $('#dashboardTitle').text('Staff Dashboard');
        $('#adminPanelSection').addClass('d-none');
        $('#claimRequestsSection').addClass('d-none');
        $('#addItemBtn').removeClass('d-none');
        loadStaffData();
    } else {
        $('#dashboardTitle').text('My Dashboard');
        $('#adminPanelSection').addClass('d-none');
        $('#claimRequestsSection').addClass('d-none');
        $('#addItemBtn').addClass('d-none');
        loadGuestData();
    }

    // Load profile data
    loadProfileData();
}

// Load profile data
function loadProfileData() {
    if (!currentUser) return;

    // Determine endpoint based on user role
    const endpoint = currentUser.role === 'GUEST' ? '/guests/profile' : '/staff/profile';

    $.ajax({
        url: API_BASE_URL + endpoint,
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        },
        success: function (response) {
            if (response.status === 200) {
                const profile = response.data;

                // Update profile fields
                $('#profileUserName').text(profile.name || 'User Name');
                $('#profileUserRole').text(currentUser.role || 'Guest');
                $('#profileUserEmail').text(profile.email || 'email@example.com');

                $('#profileName').val(profile.name || '');
                $('#profileEmail').val(profile.email || '');
                $('#profilePhone').val(profile.phone || '');
            }
        },
        error: function (xhr) {
            console.error('Failed to load profile:', xhr);
        }
    });
}

// Show profile section
function showProfileSection() {
    $('#profileSection').removeClass('d-none');
    $('.dashboard-section:not(#profileSection)').addClass('d-none');
    $('.admin-panel').addClass('d-none');
}

// Show dashboard section
function showDashboardSection() {
    $('#profileSection').addClass('d-none');
    $('.dashboard-section:not(#profileSection)').removeClass('d-none');
    $('.admin-panel').addClass('d-none');
}

// Show admin section
function showAdminSection() {
    $('#profileSection').addClass('d-none');
    $('.dashboard-section').addClass('d-none');
    $('.admin-panel').removeClass('d-none');
}

// Show home section
function showHomeSection() {
    $('#dashboardSection').addClass('d-none');
}

// Load data for admin users
function loadAdminData() {
    // Load lost items (admin can see all)
    $.ajax({
        url: API_BASE_URL + '/lost-items',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        },
        success: function (response) {
            if (response.status === 200) {
                populateLostItemsTable(response.data);
            }
        },
        error: function (xhr) {
            console.error('Failed to load lost items:', xhr);
            showError('Failed to load lost items');
        }
    });

    // Load found items (admin can see all)
    $.ajax({
        url: API_BASE_URL + '/found-items',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        },
        success: function (response) {
            if (response.status === 200) {
                populateFoundItemsTable(response.data);
            }
        },
        error: function (xhr) {
            console.error('Failed to load found items:', xhr);
            showError('Failed to load found items');
        }
    });

    // Load stats
    loadAdminStats();

    // Load deliveries (admin only)
    $.ajax({
        url: API_BASE_URL + '/deliveries',
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function (response) {
            if (response.status === 200) {
                $("#deliveryRequestsSection").removeClass("d-none");
                populateDeliveryRequestsTable(response.data);
                $("#deliveryRequestsCount").text(response.data.length);
            }
        },
        error: function (xhr) {
            console.error('Failed to load deliveries:', xhr);
        }
    });

    // Load guests (admin only)
    $.ajax({
        url: API_BASE_URL + '/guests',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        },
        success: function (response) {
            if (response.status === 200) {
                console.log('Guests loaded:', response.data.length);
            }
        },
        error: function (xhr) {
            console.error('Failed to load guests:', xhr);
        }
    });

    // Load hotels (admin only)
    $.ajax({
        url: API_BASE_URL + '/hotels',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        },
        success: function (response) {
            if (response.status === 200) {
                populateHotelsTable(response.data);
            }
        },
        error: function (xhr) {
            console.error('Failed to load hotels:', xhr);
        }
    });

    // Load claims (admin only)
    $.ajax({
        url: API_BASE_URL + '/claims',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        },
        success: function (response) {
            if (response.status === 200) {
                populateClaimRequestsTable(response.data);
            }
            // loadClaimRequests();
        },
        error: function (xhr) {
            console.error('Failed to load claims:', xhr);
        }
    });

    // Load staff (admin only)
    $.ajax({
        url: API_BASE_URL + '/staff/all',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        },
        success: function (response) {
            if (response.status === 200) {
                populateStaffTable(response.data);
            }
        },
        error: function (xhr) {
            console.error('Failed to load staff:', xhr);
        }
    });
}

// Load data for staff users
function loadStaffData() {
    // Load lost items (staff can see all)
    $.ajax({
        url: API_BASE_URL + '/lost-items',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        },
        success: function (response) {
            if (response.status === 200) {
                populateLostItemsTable(response.data);
            }
        },
        error: function (xhr) {
            console.error('Failed to load lost items:', xhr);
            showError('Failed to load lost items');
        }
    });

    // Load found items (staff can see all)
    $.ajax({
        url: API_BASE_URL + '/found-items',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        },
        success: function (response) {
            if (response.status === 200) {
                populateFoundItemsTable(response.data);
            }
        },
        error: function (xhr) {
            console.error('Failed to load found items:', xhr);
            showError('Failed to load found items');
        }
    });

    // Load staff stats
    loadStaffStats();
}

// Load data for guest users
function loadGuestData() {
    // Load guest's lost items
    $.ajax({
        url: API_BASE_URL + '/lost-items/me',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        },
        success: function (response) {
            if (response.status === 200) {
                // For guests, we need to filter by their ID
                const guestItems = response.data.filter(item => {
                    // Check if the item belongs to the current guest
                    return item.guestId === currentUser.guestId ||
                        (item.guest && item.guest.guestId === currentUser.guestId) ||
                        (currentUser.id && item.guestId === currentUser.id);
                });
                populateLostItemsTable(guestItems);
            }
        },
        error: function (xhr) {
            console.error('Failed to load lost items:', xhr);
            showError('Failed to load your lost items');
        }
    });

    // Load found items that might match guest's lost items
    // Guests can see all unclaimed found items to check if their item is there
    $.ajax({
        url: API_BASE_URL + '/found-items/unclaimed',
        type: 'GET',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        },
        success: function (response) {
            if (response.status === 200) {
                // Filter to show only unclaimed items for guests
                const unclaimedItems = response.data.filter(item =>
                    item.status && item.status.toLowerCase().includes('unclaimed')
                );
                populateFoundItemsTable(unclaimedItems);
            }
        },
        error: function (xhr) {
            console.error('Failed to load found items:', xhr);
        }
    });

    // Load notifications for guest
    if (currentUser.guestId) {
        loadNotifications();
    }

    // Load guest stats
    loadGuestStats();
}

function loadDeliveries() {
    $.ajax({
        url: API_BASE_URL + "/deliveries",
        type: "GET",
        headers: { "Authorization": "Bearer " + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                populateDeliveryRequestsTable(response.data);
            }
        },
        error: function(xhr) {
            console.error("Failed to load deliveries:", xhr);
        }
    });
}

function populateDeliveryRequestsTable(deliveries) {
    const tbody = $("#deliveryRequestsTable tbody");
    tbody.empty();

    if (!deliveries || deliveries.length === 0) {
        tbody.append('<tr><td colspan="6" class="text-center py-4">No delivery requests</td></tr>');
        $("#deliveryRequestsCount").text(0);
        return;
    }

    deliveries.forEach(d => {
        tbody.append(`
            <tr>
                <td>${d.deliveryId}</td>
                <td>${d.lostItemId}</td>
                <td>${d.method}</td>
                <td>${d.address || "-"}</td>
                <td><span class="status-badge ${getStatusClass(d.status)}">${d.status}</span></td>
                <td>
                    <button class="btn btn-sm btn-success update-delivery" data-id="${d.deliveryId}" data-status="SHIPPED">Mark Shipped</button>
                    <button class="btn btn-sm btn-primary update-delivery" data-id="${d.deliveryId}" data-status="DELIVERED">Mark Delivered</button>
                </td>
            </tr>
        `);
    });

    $("#deliveryRequestsCount").text(deliveries.length);

    // Bind action buttons
    $(".update-delivery").on("click", function() {
        const id = $(this).data("id");
        const status = $(this).data("status");
        updateDeliveryStatus(id, status);
    });
}

function updateDeliveryStatus(id, status) {
    $.ajax({
        url: API_BASE_URL + `/deliveries/${id}/status?status=${status}`,
        type: "PUT",
        headers: { "Authorization": "Bearer " + jwtToken },
        success: function(response) {
            Swal.fire("Updated!", "Delivery status updated.", "success");
            loadDeliveries();
        },
        error: function(xhr) {
            Swal.fire("Error", "Could not update status.", "error");
        }
    });
}

// Open modal with selected lost item
function openDeliveryModal(lostItemId) {
    $("#deliveryLostItemId").val(lostItemId);
    $("#deliveryMethod").val("");
    $("#deliveryAddress").val("");
    $("#deliveryAddressGroup").addClass("d-none");
    $("#deliveryModal").modal("show");
}

// Show address input only if "DELIVERY" selected
$("#deliveryMethod").on("change", function () {
    if ($(this).val() === "DELIVERY") {
        $("#deliveryAddressGroup").removeClass("d-none");
    } else {
        $("#deliveryAddressGroup").addClass("d-none");
    }
});

// Submit delivery request
$("#deliveryForm").on("submit", function (e) {
    e.preventDefault();

    const lostItemId = $("#deliveryLostItemId").val();
    const method = $("#deliveryMethod").val();
    const address = method === "DELIVERY" ? $("#deliveryAddress").val() : null;

    $.ajax({
        url: API_BASE_URL + "/deliveries",
        type: "POST",
        headers: {"Authorization": "Bearer " + jwtToken},
        contentType: "application/json",
        data: JSON.stringify({lostItemId, method, address}),
        success: function (response) {
            if (response.status === 200) {
                Swal.fire("Success", "Delivery request submitted!", "success");
                $("#deliveryModal").modal("hide");
            }
        },
        error: function (xhr) {
            Swal.fire("Error", "Failed to request delivery.", "error");
        }
    });
});

// Load notifications for the current user
function loadNotifications() {
    if (!currentUser) {
        $('#notificationsList').html('<p class="text-center">Please log in to see notifications.</p>');
        return;
    }

    // guests: use notifications/me
    if (currentUser.id) {
        $('#notificationsLoading').removeClass('d-none');

        $.ajax({
            url: API_BASE_URL + '/notifications/me',
            type: 'GET',
            headers: {
                'Authorization': 'Bearer ' + jwtToken
            },
            success: function (response) {
                $('#notificationsLoading').addClass('d-none');

                if (response.status === 200) {
                    notifications = response.data;
                    // update badge + UI
                    if (notifications && notifications.length > 0) {
                        const unreadCount = notifications.filter(n => !n.isRead).length;
                        $('#notificationsBadge').removeClass('d-none').text(unreadCount);
                    } else {
                        $('#notificationsBadge').addClass('d-none');
                    }
                    displayNotifications(notifications);
                } else {
                    $('#notificationsList').html('<p class="text-center">Failed to load notifications.</p>');
                }
            },
            error: function (xhr) {
                $('#notificationsLoading').addClass('d-none');
                console.error('Failed to load notifications:', xhr);
                $('#notificationsList').html('<p class="text-center">Error loading notifications. Please try again later.</p>');
            }
        });
    }
}

// Display notifications in the modal
function displayNotifications(notifications) {
    const notificationsList = $('#notificationsList');
    notificationsList.empty();

    if (!notifications || notifications.length === 0) {
        notificationsList.html('<p class="text-center">No notifications found.</p>');
        return;
    }

    notifications.forEach(notification => {
        const notificationItem = `
                <div class="card mb-3 ${notification.isRead ? '' : 'border-primary'}" data-id="${notification.id}">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <h6 class="card-title mb-1">${notification.type || 'Notification'}</h6>
                                <p class="card-text mb-2">${notification.message || 'No message content'}</p>
                                <small class="text-muted">${formatDate(notification.sentDate)}</small>
                            </div>
                            ${!notification.isRead ? `
                            <button class="btn btn-sm btn-outline-primary mark-as-read-btn" data-id="${notification.id}">
                                Mark as Read
                            </button>
                            ` : ''}
                        </div>
                    </div>
                </div>
            `;
        notificationsList.append(notificationItem);
    });

    // Add event listeners for mark as read buttons
    $('.mark-as-read-btn').on('click', function () {
        const notificationId = $(this).data('id');
        markNotificationAsRead(notificationId);
    });
}

// Mark a notification as read
function markNotificationAsRead(notificationId) {
    $.ajax({
        url: API_BASE_URL + '/notifications/' + notificationId + '/read',
        type: 'PUT',
        headers: {
            'Authorization': 'Bearer ' + jwtToken
        },
        success: function (response) {
            if (response.status === 200) {
                // Update the UI
                $(`.card[data-id="${notificationId}"]`).removeClass('border-primary');
                $(`.mark-as-read-btn[data-id="${notificationId}"]`).remove();

                // Update the notification badge
                const unreadCount = $('.card.border-primary').length;
                if (unreadCount > 0) {
                    $('#notificationsBadge').removeClass('d-none').text(unreadCount);
                } else {
                    $('#notificationsBadge').addClass('d-none');
                }

                showSuccess('Notification marked as read');
            }
        },
        error: function (xhr) {
            console.error('Failed to mark notification as read:', xhr);
            showError('Failed to mark notification as read. Please try again.');
        }
    });
}

// Mark all notifications as read
function markAllNotificationsAsRead() {
    const unreadNotifications = notifications.filter(n => !n.isRead);

    if (unreadNotifications.length === 0) {
        showInfo('All notifications are already read');
        return;
    }

    // Mark all as read in the UI first for better UX
    $('.card').removeClass('border-primary');
    $('.mark-as-read-btn').remove();
    $('#notificationsBadge').addClass('d-none');

    // Send requests to mark all as read
    const promises = unreadNotifications.map(notification => {
        return $.ajax({
            url: API_BASE_URL + '/notifications/' + notification.id + '/read',
            type: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + jwtToken
            }
        });
    });

    // Handle all requests
    Promise.all(promises)
        .then(() => {
            showSuccess('All notifications marked as read');
        })
        .catch(error => {
            console.error('Failed to mark some notifications as read:', error);
            showError('Some notifications may not have been marked as read. Please try again.');
        });
}

// Populate lost items table
function populateLostItemsTable(items) {
    const tbody = $('#lostItemsTable tbody');
    tbody.empty();

    if (!items || items.length === 0) {
        tbody.append('<tr><td colspan="6" class="text-center py-4">No lost items found</td></tr>');
        return;
    }

    items.forEach(item => {
        const statusClass = getStatusClass(item.status);

        const row = `
        <tr data-status="${item.status ? item.status.toLowerCase() : ''}">
            <td>${item.title || 'Unknown Item'}</td>
            <td>${item.guestName || (item.guest && item.guest.name) || 'Unknown Guest'}</td>
            <td>${item.location || 'N/A'}</td>   <!-- Show location -->
            <td>${formatDate(item.createdAt) || 'N/A'}</td> <!-- Lost date -->
            <td><span class="status-badge ${statusClass}">${item.status || 'UNKNOWN'}</span></td>

            <td>
                <button class="btn btn-sm btn-outline-primary view-item action-btn" data-id="${item.lostId}">View</button>
                ${currentUser.role === 'GUEST' ? `
                    <button class="btn btn-sm btn-outline-secondary edit-item action-btn" data-id="${item.lostId}">Edit</button>
                    ${(item.status === 'CLAIMED' || item.status === 'MATCHED') ? `
                        <button class="btn btn-sm btn-primary request-delivery action-btn" data-id="${item.lostId}">
                            Request Delivery
                        </button>
                    ` : ''}
                ` : ''}
                ${(currentUser.role === 'ADMIN' || currentUser.role === 'STAFF') ? `
                    <button class="btn btn-sm btn-outline-danger archive-item action-btn" data-id="${item.lostId}">Archive</button>
                ` : ''}
            </td>
        </tr>
    `;
        tbody.append(row);
    });

    // Add event listeners
    $('.view-item').on('click', function () {
        const itemId = $(this).data('id');
        viewItem(itemId, 'lost');
    });

    $('.edit-item').on('click', function () {
        const itemId = $(this).data('id');
        editItem(itemId, 'lost');
    });

    $('.archive-item').on('click', function () {
        const itemId = $(this).data('id');
        archiveItem(itemId, 'lost');
    });

    $('.request-delivery').on('click', function() {
        const itemId = $(this).data('id');
        openDeliveryModal(itemId);
    });
}

// Populate found items table
function populateFoundItemsTable(items) {
    const tbody = $('#foundItemsTable tbody');
    tbody.empty();

    if (!items || items.length === 0) {
        tbody.append('<tr><td colspan="5" class="text-center py-4">No found items found</td></tr>');
        return;
    }

    items.forEach(item => {
        const statusClass = getStatusClass(item.status);
        const row = `
                <tr data-status="${item.status ? item.status.toLowerCase() : ''}">
                    <td>${item.title || 'Unknown Item'}</td>
                    <td>${item.staffName || (item.staff && item.staff.name) || 'Unknown Staff'}</td>
                    <td>${formatDate(item.createdAt) || 'N/A'}</td>
                    <td><span class="status-badge ${statusClass}">${item.status || 'UNKNOWN'}</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary view-item action-btn" data-id="${item.foundId}">View</button>
                        ${(currentUser.role === 'ADMIN' || currentUser.role === 'STAFF') ? `
                        <button class="btn btn-sm btn-outline-success match-item action-btn" data-id="${item.foundId}">Match</button>
                        <button class="btn btn-sm btn-outline-danger archive-item action-btn" data-id="${item.foundId}">Archive</button>
                        ` : ''}
                        ${(currentUser.role === 'GUEST' && item.status && item.status.toLowerCase().includes('unclaimed')) ? `
                        <button class="btn btn-sm btn-outline-warning claim-item action-btn" data-id="${item.foundId}" data-name="${item.title || 'Unknown Item'}">Claim</button>
                        ` : ''}
                    </td>
                </tr>
            `;
        tbody.append(row);
    });

    // Add event listeners
    $('.view-item').on('click', function () {
        const itemId = $(this).data('id');
        viewItem(itemId, 'found');
    });

    $('.match-item').on('click', function () {
        const itemId = $(this).data('id');
        matchItem(itemId);
    });

    $('.archive-item').on('click', function () {
        const itemId = $(this).data('id');
        archiveItem(itemId, 'found');
    });

    $('.claim-item').on('click', function () {
        const itemId = $(this).data('id');
        const itemName = $(this).data('name');
        claimItem(itemId, itemName);
    });
}

// Populate hotels table
function populateHotelsTable(hotels) {
    const tbody = $('#hotelsTable tbody');
    tbody.empty();

    if (!hotels || hotels.length === 0) {
        tbody.append('<tr><td colspan="4" class="text-center py-4">No hotels found</td></tr>');
        return;
    }

    hotels.forEach(hotel => {
        const row = `
                <tr>
                    <td>${hotel.name || 'Unknown Hotel'}</td>
                    <td>${hotel.address || 'N/A'}</td>
                    <td>${hotel.phone || 'N/A'}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary edit-hotel action-btn" data-id="${hotel.hotelId}">Edit</button>
                        <button class="btn btn-sm btn-outline-danger delete-hotel action-btn" data-id="${hotel.hotelId}">Delete</button>
                    </td>
                </tr>
            `;
        tbody.append(row);
    });

    $('.edit-hotel').on('click', function () {
        const id = $(this).data('id');
        editHotel(id);
    });

    $('.delete-hotel').on('click', function () {
        const id = $(this).data('id');
        deleteHotel(id);
    });
}

function editHotel(hotelId) {
    $.ajax({
        url: API_BASE_URL + `/hotels/${hotelId}`,
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function (response) {
            if (response.status === 200) {
                const hotel = response.data;
                $('#editHotelId').val(hotel.hotelId);
                $('#editHotelName').val(hotel.name);
                $('#editHotelAddress').val(hotel.address);
                $('#editHotelPhone').val(hotel.phone);
                $('#editHotelModal').modal('show');
            }
        },
        error: function () {
            showError("Failed to load hotel details");
        }
    });
}

function deleteHotel(hotelId) {
    showConfirm("Are you sure you want to delete this hotel?", function () {
        $.ajax({
            url: API_BASE_URL + `/hotels/${hotelId}`,
            type: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + jwtToken },
            success: function (response) {
                if (response.status === 200) {
                    showSuccess("Hotel deleted successfully!");
                    loadAdminData();
                }
            },
            error: function () {
                showError("Failed to delete hotel");
            }
        });
    });
}

// Save hotel changes
$('#saveHotelChanges').on('click', function () {
    const hotelId = $('#editHotelId').val();
    const updatedHotel = {
        hotelId: hotelId,
        name: $('#editHotelName').val(),
        address: $('#editHotelAddress').val(),
        phone: $('#editHotelPhone').val()
    };

    $.ajax({
        url: API_BASE_URL + `/hotels/${hotelId}`,
        type: 'PUT',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        contentType: 'application/json',
        data: JSON.stringify(updatedHotel),
        success: function (response) {
            if (response.status === 200) {
                showSuccess("Hotel updated successfully!");
                $('#editHotelModal').modal('hide');
                loadAdminData(); // reload table
            }
        },
        error: function () {
            showError("Failed to update hotel");
        }
    });
});

// Populate staff table
function populateStaffTable(staff) {
    const tbody = $('#staffTable tbody');
    tbody.empty();

    if (!staff || staff.length === 0) {
        tbody.append('<tr><td colspan="5" class="text-center py-4">No staff members found</td></tr>');
        return;
    }

    staff.forEach(staffMember => {
        const row = `
                <tr>
                    <td>${staffMember.name || 'Unknown Staff'}</td>
                    <td>${staffMember.email || 'N/A'}</td>
                    <td>${staffMember.role || 'N/A'}</td>
                    <td>${staffMember.hotelId || 'N/A'}</td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary edit-staff action-btn" data-id="${staffMember.staffId}">Edit</button>
                        <button class="btn btn-sm btn-outline-danger delete-staff action-btn" data-id="${staffMember.staffId}">Delete</button>
                    </td>
                </tr>
            `;
        tbody.append(row);
    });

    $('.edit-staff').on('click', function () {
        const id = $(this).data('id');
        editStaff(id);
    });

    $('.delete-staff').on('click', function () {
        const id = $(this).data('id');
        deleteStaff(id);
    });
}

function editStaff(staffId) {
    $.ajax({
        url: API_BASE_URL + `/staff/${staffId}`,
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function (response) {
            if (response.status === 200) {
                const staff = response.data;
                $('#editStaffId').val(staff.staffId);
                $('#editStaffName').val(staff.name);
                $('#editStaffEmail').val(staff.email);
                $('#editStaffRole').val(staff.role);
                $('#editStaffHotel').val(staff.hotelId);
                $('#editStaffModal').modal('show');
            }
        },
        error: function () {
            showError("Failed to load staff details");
        }
    });
}

function deleteStaff(staffId) {
    showConfirm("Are you sure you want to delete this staff member?", function () {
        $.ajax({
            url: API_BASE_URL + `/staff/${staffId}`,
            type: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + jwtToken },
            success: function (response) {
                if (response.status === 200) {
                    showSuccess("Staff deleted successfully!");
                    loadAdminData();
                }
            },
            error: function () {
                showError("Failed to delete staff");
            }
        });
    });
}

// Save staff changes
$('#saveStaffChanges').on('click', function () {
    const staffId = $('#editStaffId').val();
    const updatedStaff = {
        staffId: staffId,
        name: $('#editStaffName').val(),
        email: $('#editStaffEmail').val(),
        role: $('#editStaffRole').val(),
        hotelId: $('#editStaffHotel').val()
    };

    $.ajax({
        url: API_BASE_URL + `/staff/${staffId}`,
        type: 'PUT',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        contentType: 'application/json',
        data: JSON.stringify(updatedStaff),
        success: function (response) {
            if (response.status === 200) {
                showSuccess("Staff updated successfully!");
                $('#editStaffModal').modal('hide');
                loadAdminData(); // reload table
            }
        },
        error: function () {
            showError("Failed to update staff");
        }
    });
});

// Claim item
function claimItem(itemId, itemName) {
    $('#claimItemId').val(itemId);
    $('#claimItemName').text(itemName);
    $('#claimMessage').val('');
    $('#proofPreview').html('');
    $('#claimItemModal').modal('show');
}

// Populate claim requests table
function populateClaimRequestsTable(claims) {
    const tbody = $('#claimRequestsTable tbody');
    tbody.empty();

    if (!claims || claims.length === 0) {
        tbody.append('<tr><td colspan="5" class="text-center py-4">No claim requests found</td></tr>');
        $('#claimRequestsCount').text('0');
        return;
    }

    $('#claimRequestsCount').text(claims.length);

    claims.forEach(claim => {
        const statusClass = getStatusClass(claim.status);
        const row = `
        <tr>
            <td>${claim.foundItemId || 'Unknown Item'}</td>
            <td>${claim.guestName || 'Unknown Guest'}</td>
            <td>${formatDate(claim.createdAt) || 'N/A'}</td>
            <td><span class="status-badge ${statusClass}">${claim.status || 'PENDING'}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-primary view-claim action-btn" data-id="${claim.claimId}">View</button>
                ${claim.status === 'PENDING' ? `
                    <button class="btn btn-sm btn-outline-success approve-claim action-btn" data-id="${claim.claimId}">Approve</button>
                    <button class="btn btn-sm btn-outline-danger reject-claim action-btn" data-id="${claim.claimId}">Reject</button>
                ` : ''}
            </td>
        </tr>
    `;
        tbody.append(row);
    });

    // Bind buttons
    $('.view-claim').on('click', function () {
        const claimId = $(this).data('id');
        viewClaim(claimId);
    });

    $('.approve-claim').on('click', function () {
        const claimId = $(this).data('id');
        approveClaim(claimId);
    });

    $('.reject-claim').on('click', function () {
        const claimId = $(this).data('id');
        rejectClaim(claimId);
    });
}

// View item details
function viewItem(itemId, type) {
    const role = getUserRole();
    let url;

    if (type === 'lost') {
        if (role === 'GUEST') {
            url = API_BASE_URL + '/lost-items/me'; // guest can only see their own
        } else {
            url = API_BASE_URL + `/lost-items/${itemId}`; // staff/admin can see all
        }
    } else {
        url = API_BASE_URL + `/found-items/${itemId}`; // all roles allowed
    }

    $.ajax({
        url: url,
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function (response) {
            if (response.status === 200) {
                let item = (role === 'GUEST' && type === 'lost')
                    ? response.data.find(i => i.lostId === itemId) // filter from guest's items
                    : response.data;

                if (!item) {
                    showError("Item not found or not accessible");
                    return;
                }

                showItemDetails(item, type);
            }
        },
        error: function (xhr) {
            console.error(`Failed to load ${type} item:`, xhr);
            showError(`Failed to load ${type} item details`);
        }
    });
}

function getUserRole() {
    if (!jwtToken) return null;
    try {
        const payload = JSON.parse(atob(jwtToken.split('.')[1]));
        // Spring Security usually puts role in "authorities" or "role"
        return payload.roles ? payload.roles[0] : payload.authorities ? payload.authorities[0] : null;
    } catch (e) {
        console.error("Failed to decode JWT:", e);
        return null;
    }
}

// Display item details (lost or found) in a modal
function showItemDetails(item, type) {
    const modalBody = $('#itemDetailsModal .modal-body');
    modalBody.empty();

    const imageHtml = item.imagePath
        ? `<img src="${item.imagePath}" class="img-fluid rounded mb-3" alt="Item Image">`
        : `<div class="alert alert-secondary">No image available</div>`;

    const detailsHtml = `
        <h5>${item.title || 'Unknown Item'}</h5>
        <p>${item.description || 'No description provided.'}</p>
        <ul class="list-unstyled">
            <li><strong>Reported By:</strong> ${item.guestName || item.staffName || 'N/A'}</li>
            <li><strong>Date:</strong> ${formatDate(item.createdAt || item.createdAt)}</li>
            <li><strong>Location:</strong> ${item.location || item.location || 'N/A'}</li>
            <li><strong>Status:</strong> ${item.status || 'UNKNOWN'}</li>
        </ul>
        ${imageHtml}
    `;

    modalBody.html(detailsHtml);

    // Show the modal
    $('#itemDetailsModal').modal('show');
}

// View claim details
function viewClaim(claimId) {
    $.ajax({
        url: API_BASE_URL + `/claims/${claimId}`,
        type: "GET",
        headers: { "Authorization": "Bearer " + jwtToken },
        success: function (response) {
            if (response.status === 200) {
                const claim = response.data;

                // Fill modal
                $("#claimItemTitle").text(claim.foundItemTitle || "Unknown Item");
                $("#claimGuestName").text(claim.guestName || "Unknown Guest");
                $("#claimGuestEmail").text(claim.guestEmail || "N/A");
                $("#claimStatus").text(claim.status);
                $("#claimMessageText").text(claim.message || "No message provided");

                if (claim.proofImagePath) {
                    $("#claimImage").attr("src", claim.proofImagePath);
                    $("#claimImageContainer").show();
                } else {
                    $("#claimImageContainer").hide();
                }

                // Show modal
                $("#viewClaimModal").modal("show");

                // Approve button
                $("#approveClaimBtn").off("click").on("click", function () {
                    approveClaim(claimId);
                });

                // Reject button
                $("#rejectClaimBtn").off("click").on("click", function () {
                    const reason = prompt("Enter reason for rejection:");
                    if (reason) rejectClaim(claimId, reason);
                });
            }
        },
        error: function (xhr) {
            console.error("Failed to load claim details:", xhr);
            showError("Failed to load claim details");
        }
    });
}

// Approve claim
function approveClaim(claimId) {
    $.ajax({
        url: API_BASE_URL + `/claims/${claimId}/approve`,
        type: "PUT",
        headers: { "Authorization": "Bearer " + jwtToken },
        success: function (response) {
            if (response.status === 200) {
                showSuccess("Claim approved successfully!");
                $("#viewClaimModal").modal("hide");
                loadAdminData(); // refresh
            }
        },
        error: function (xhr) {
            console.error("Failed to approve claim:", xhr);
            showError("Failed to approve claim");
        }
    });
}

// Reject claim
function rejectClaim(claimId, reason) {
    $.ajax({
        url: API_BASE_URL + `/claims/${claimId}/reject?reason=${encodeURIComponent(reason)}`,
        type: "PUT",
        headers: { "Authorization": "Bearer " + jwtToken },
        success: function (response) {
            if (response.status === 200) {
                showSuccess("Claim rejected successfully!");
                $("#viewClaimModal").modal("hide");
                loadAdminData(); // refresh
            }
        },
        error: function (xhr) {
            console.error("Failed to reject claim:", xhr);
            showError("Failed to reject claim");
        }
    });
}

// Open edit modal
function editItem(itemId, type) {
    $.ajax({
        url: API_BASE_URL + `/${type}-items/${itemId}`,
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function (response) {
            if (response.status === 200) {
                const item = response.data;

                $('#editItemId').val(itemId);
                $('#editItemType').val(type);
                $('#editItemTitle').val(item.title || '');
                $('#editItemDescription').val(item.description || '');
                $('#editItemImagePath').val(item.imagePath || '');

                $('#editItemDate').val(type === 'lost' ? (item.dateLost || '') : (item.foundDate || ''));
                $('#editItemLocation').val(type === 'lost' ? (item.locationLost || '') : (item.locationFound || ''));

                // Show current image
                if (item.imagePath) {
                    $('#editItemImagePreview').attr('src', `/uploads/${item.imagePath}`).show();
                } else {
                    $('#editItemImagePreview').hide();
                }

                $('#editItemModal').modal('show');
            }
        },
        error: function () {
            showError(`Failed to load ${type} item details`);
        }
    });
}

// Save edited item (with optional new image)
$('#editItemForm').on('submit', function (e) {
    e.preventDefault();

    const itemId = $('#editItemId').val();
    const type = $('#editItemType').val();
    const formData = new FormData();

    formData.append("title", $('#editItemTitle').val());
    formData.append("description", $('#editItemDescription').val());

    if (type === 'lost') {
        formData.append("dateLost", $('#editItemDate').val());
        formData.append("locationLost", $('#editItemLocation').val());
    } else {
        formData.append("foundDate", $('#editItemDate').val());
        formData.append("locationFound", $('#editItemLocation').val());
    }

    // If new image selected, use it; otherwise send old imagePath
    const fileInput = $('#editItemImageFile')[0].files[0];
    if (fileInput) {
        formData.append("imageFile", fileInput);
    } else {
        formData.append("imagePath", $('#editItemImagePath').val());
    }

    $.ajax({
        url: API_BASE_URL + `/${type}-items/${itemId}`,
        type: 'PUT',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        data: formData,
        processData: false,
        contentType: false,
        success: function (response) {
            if (response.status === 200) {
                showSuccess(`${type.charAt(0).toUpperCase() + type.slice(1)} item updated successfully!`);
                $('#editItemModal').modal('hide');
                loadDashboardData();
            }
        },
        error: function () {
            showError(`Failed to update ${type} item`);
        }
    });
});

// Archive item
function archiveItem(itemId, type) {
    showConfirm(`Are you sure you want to archive this ${type} item?`, function () {
        $.ajax({
            url: API_BASE_URL + `/${type}-items/${itemId}/archive`,
            type: 'PUT',
            headers: { 'Authorization': 'Bearer ' + jwtToken },
            success: function (response) {
                if (response.status === 200) {
                    showSuccess(`${type.charAt(0).toUpperCase() + type.slice(1)} item archived successfully!`);
                    loadDashboardData();
                }
            },
            error: function (xhr) {
                console.error(`Failed to archive ${type} item:`, xhr);
                showError(`Failed to archive ${type} item`);
            }
        });
    });
}

$('#archivedItemsModal').on('show.bs.modal', function () {
    const tbody = $('#archivedItemsTableBody');
    tbody.empty();

    // Load Lost Items
    $.ajax({
        url: API_BASE_URL + '/lost-items/archived',
        type: 'GET',
        headers: {'Authorization': 'Bearer ' + jwtToken},
        success: function (response) {
            response.data.forEach(item => {
                tbody.append(`
                    <tr>
                        <td>Lost</td>
                        <td>${item.title}</td>
                        <td>${item.description}</td>
                        <td>${item.createdAt}</td>
                        <td>
                            <button class="btn btn-sm btn-outline-success"
                                onclick="unarchiveItem(${item.lostId}, 'lost')">
                                Unarchive
                            </button>
                        </td>
                    </tr>
                `);
            });
        }
    });

    // Load Found Items
    $.ajax({
        url: API_BASE_URL + '/found-items/archived',
        type: 'GET',
        headers: {'Authorization': 'Bearer ' + jwtToken},
        success: function (response) {
            response.data.forEach(item => {
                tbody.append(`
                    <tr>
                        <td>Found</td>
                        <td>${item.title}</td>
                        <td>${item.description}</td>
                        <td>${item.createdAt}</td>
                        <td>
                            <button class="btn btn-sm btn-outline-success"
                                onclick="unarchiveItem(${item.foundId}, 'found')">
                                Unarchive
                            </button>
                        </td>
                    </tr>
                `);
            });
        }
    });
});

function unarchiveItem(itemId, type) {
    showConfirm(`Are you sure you want to unarchive this ${type} item?`, function () {
        $.ajax({
            url: API_BASE_URL + `/${type}-items/${itemId}/unarchive`,
            type: 'PUT',
            headers: { 'Authorization': 'Bearer ' + jwtToken },
            success: function (response) {
                if (response.status === 200) {
                    showSuccess(`${type.charAt(0).toUpperCase() + type.slice(1)} item unarchived successfully!`);
                    $('#archivedItemsModal').modal('hide');
                    loadDashboardData();
                }
            },
            error: function (xhr) {
                console.error(`Failed to unarchive ${type} item:`, xhr);
                showError(`Failed to unarchive ${type} item`);
            }
        });
    });
}

// Match item
function matchItem(foundItemId) {
    // Fetch all lost items
    $.ajax({
        url: API_BASE_URL + '/lost-items',
        type: 'GET',
        headers: {'Authorization': 'Bearer ' + jwtToken},
        success: function (response) {
            if (response.status === 200) {
                const lostItems = response.data;

                // Populate dropdown
                const select = $('#matchLostItemSelect');
                select.empty();
                lostItems.forEach(item => {
                    select.append(`<option value="${item.lostId}">${item.title} - ${item.guestName}</option>`);
                });

                // Store the found item id
                $('#matchFoundItemId').val(foundItemId);

                // Show modal
                $('#matchItemModal').modal('show');
            }
        },
        error: function (xhr) {
            showError('Failed to load lost items for matching');
        }
    });
}

// Submit match
$('#matchItemForm').on('submit', function (e) {
    e.preventDefault();

    const foundItemId = $('#matchFoundItemId').val();
    const lostItemId = $('#matchLostItemSelect').val();

    const matchData = {foundItemId, lostItemId};

    $.ajax({
        url: API_BASE_URL + '/matches',
        type: 'POST',
        contentType: 'application/json',
        headers: {'Authorization': 'Bearer ' + jwtToken},
        data: JSON.stringify(matchData),
        success: function (response) {
            if (response.status === 200) {
                showSuccess('Item matched successfully!');
                $('#matchItemModal').modal('hide');
                loadDashboardData();
            }
        },
        error: function (xhr) {
            showError('Failed to match item');
        }
    });
});

// Filter items by status
function filterItems(filter) {
    if (filter === 'all') {
        $('#lostItemsTable tbody tr, #foundItemsTable tbody tr').show();
    } else {
        $('#lostItemsTable tbody tr').hide();
        $('#foundItemsTable tbody tr').hide();
        $(`#lostItemsTable tbody tr[data-status*="${filter}"], #foundItemsTable tbody tr[data-status*="${filter}"]`).show();
    }
}

// Helper function to get CSS class for status
function getStatusClass(status) {
    switch ((status || '').toUpperCase()) {
        case 'APPROVED': return 'bg-success';
        case 'REJECTED': return 'bg-danger';
        case 'PENDING': return 'bg-warning';
        default: return 'bg-secondary';
    }
}

// Helper function to format date
function formatDate(dateString) {
    if (!dateString) return 'N/A';

    try {
        const normalized = dateString.replace(' ', 'T'); // fix format
        const date = new Date(normalized);

        if (isNaN(date)) return 'N/A';

        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    } catch (e) {
        return 'N/A';
    }
}

// Update UI after login
function updateUIAfterLogin() {
    $('#authButtons').addClass('d-none');
    $('#userMenu').removeClass('d-none');
    $('#dashboardNavItem').removeClass('d-none');

    if (currentUser.role === 'ADMIN') {
        $('#adminNavItem').removeClass('d-none');
    }

    const username = currentUser.name || currentUser.email;
    $('#username').text(username);

    $('#dashboardSection').removeClass('d-none');

    // Update dashboard link based on role
    if (currentUser.role === 'ADMIN') {
        $('#dashboardLink').text('Admin Dashboard');
    } else if (currentUser.role === 'STAFF') {
        $('#dashboardLink').text('Staff Dashboard');
    } else {
        $('#dashboardLink').text('My Dashboard');
    }
}

// Logout function
function logoutUser() {
    showConfirm('Are you sure you want to logout?', function () {
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('currentUser');
        jwtToken = null;
        currentUser = null;
        notifications = [];

        $('#authButtons').removeClass('d-none');
        $('#userMenu').addClass('d-none');
        $('#dashboardNavItem').addClass('d-none');
        $('#adminNavItem').addClass('d-none');
        $('#dashboardSection').addClass('d-none');
        $('#notificationsBadge').addClass('d-none');

        showSuccess('You have been logged out successfully!');
    });
}

// Show info message
function showInfo(message) {
    Swal.fire({
        icon: 'info',
        title: message,
        showConfirmButton: false,
        timer: 2000
    });
}

// Add hotel
function addHotel() {
    const name = $('#hotelName').val();
    const address = $('#hotelAddress').val();
    const phone = $('#hotelPhone').val();

    const hotelData = {name, address, phone};

    $.ajax({
        url: API_BASE_URL + '/hotels',
        type: 'POST',
        contentType: 'application/json',
        headers: {'Authorization': 'Bearer ' + jwtToken},
        data: JSON.stringify(hotelData),
        success: function (response) {
            if (response.status === 200) {
                showSuccess('Hotel added successfully!');
                $('#addHotelModal').modal('hide');
                loadAdminData(); // reload table
            }
        },
        error: function (xhr) {
            showError('Failed to add hotel: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

$('#addStaffForm').on('submit', function (e) {
    e.preventDefault();

    const staffData = {
        name: $('#staffName').val(),
        email: $('#staffEmail').val(),
        phone: $('#staffPhone').val(),
        password: $('#staffPassword').val(),
        role: $('#staffRole').val(),
        hotelId: parseInt($('#staffHotelId').val(), 10)
    };

    $.ajax({
        url: API_BASE_URL + '/auth/register',
        type: 'POST',
        contentType: 'application/json',
        headers: {'Authorization': 'Bearer ' + jwtToken},
        data: JSON.stringify(staffData),
        success: function (response) {
            showSuccess('Staff added successfully!');
            $('#addStaffModal').modal('hide');
            loadAdminData();
        },
        error: function (xhr) {
            showError('Failed to add staff: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
});

function loadHotelsForStaffModal() {
    $.ajax({
        url: API_BASE_URL + '/hotels',
        type: 'GET',
        headers: {'Authorization': 'Bearer ' + jwtToken},
        success: function (response) {
            if (response.status === 200 && response.data) {
                const hotels = response.data;
                const $select = $('#staffHotelId');
                $select.empty();
                $select.append('<option value="">Select Hotel</option>');

                hotels.forEach(hotel => {
                    $select.append(`<option value="${hotel.hotelId}">${hotel.name}</option>`);
                });
            }
        },
        error: function (xhr) {
            showError('Failed to load hotels: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

// Load Admin statistics
function loadAdminStats() {
    const statsHtml = `
    <div class="col-md-3">
        <div class="stat-card text-center p-3">
            <div class="stat-number" id="lostItemsCount">0</div>
            <div class="stat-label">Lost Items</div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="stat-card text-center p-3">
            <div class="stat-number" id="foundItemsCount">0</div>
            <div class="stat-label">Found Items</div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="stat-card text-center p-3">
            <div class="stat-number" id="matchedItemsCount">0</div>
            <div class="stat-label">Matched Items</div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="stat-card text-center p-3">
            <div class="stat-number" id="guestsCount">0</div>
            <div class="stat-label">Registered Guests</div>
        </div>
    </div>
    `;
    $('#statsSection').html(statsHtml);

    $.ajax({
        url: API_BASE_URL + '/stats/admin',
        type: 'GET',
        headers: {'Authorization': 'Bearer ' + jwtToken},
        success: function (res) {
            if (res && res.status === 200 && res.data) {
                $('#lostItemsCount').text(res.data.lostItems ?? 0);
                $('#foundItemsCount').text(res.data.foundItems ?? 0);
                $('#matchedItemsCount').text(res.data.matchedItems ?? 0);
                $('#guestsCount').text(res.data.guests ?? 0);
            } else {
                console.error("Admin stats response invalid:", res);
            }
        },
        error: function (xhr) {
            console.error("Failed to load admin stats:", xhr.responseJSON || xhr.statusText);
        }
    });
}

// Load Staff statistics
function loadStaffStats() {
    const statsHtml = `
    <div class="col-md-4">
        <div class="stat-card text-center p-3">
            <div class="stat-number" id="staffLostItemsCount">0</div>
            <div class="stat-label">Lost Items</div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="stat-card text-center p-3">
            <div class="stat-number" id="staffFoundItemsCount">0</div>
            <div class="stat-label">Found Items</div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="stat-card text-center p-3">
            <div class="stat-number" id="staffMatchedItemsCount">0</div>
            <div class="stat-label">Matched Items</div>
        </div>
    </div>
    `;
    $('#statsSection').html(statsHtml);

    $.ajax({
        url: API_BASE_URL + '/stats/staff',
        type: 'GET',
        headers: {'Authorization': 'Bearer ' + jwtToken},
        success: function (res) {
            if (res && res.status === 200 && res.data) {
                $('#staffLostItemsCount').text(res.data.lostItems ?? 0);
                $('#staffFoundItemsCount').text(res.data.foundItems ?? 0);
                $('#staffMatchedItemsCount').text(res.data.matchedItems ?? 0);
            } else {
                console.error("Staff stats response invalid:", res);
            }
        },
        error: function (xhr) {
            console.error("Failed to load staff stats:", xhr.responseJSON || xhr.statusText);
        }
    });
}

// Load Guest statistics
function loadGuestStats() {
    const statsHtml = `
    <div class="col-md-6">
        <div class="stat-card text-center p-3">
            <div class="stat-number" id="guestLostItemsCount">0</div>
            <div class="stat-label">Your Lost Items</div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="stat-card text-center p-3">
            <div class="stat-number" id="guestMatchedItemsCount">0</div>
            <div class="stat-label">Matched Items</div>
        </div>
    </div>
    `;
    $('#statsSection').html(statsHtml);

    $.ajax({
        url: API_BASE_URL + '/stats/guest',
        type: 'GET',
        headers: {'Authorization': 'Bearer ' + jwtToken},
        success: function (res) {
            if (res && res.status === 200 && res.data) {
                $('#guestLostItemsCount').text(res.data.lostItems ?? 0);
                $('#guestMatchedItemsCount').text(res.data.matchedItems ?? 0);
            } else {
                console.error("Guest stats response invalid:", res);
            }
        },
        error: function (xhr) {
            console.error("Failed to load guest stats:", xhr.responseJSON || xhr.statusText);
        }
    });
}
