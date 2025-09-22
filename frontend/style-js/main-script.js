
// ==================== CONFIGURATION & INITIALIZATION ====================
const API_BASE_URL = 'http://localhost:8080';
let currentUser = null;
let jwtToken = null;
let notifications = [];
let allDeliveries = [];
let claimGuestId = null;

// Initialize SweetAlert2
const Toast = Swal.mixin({
    toast: true,
    position: 'top-end',
    showConfirmButton: false,
    timer: 1000,
    timerProgressBar: true,
    didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer);
        toast.addEventListener('mouseleave', Swal.resumeTimer);
    }
});

// Document ready function
$(document).ready(function() {
    initializeApp();
    setupEventListeners();
});

// ==================== INITIALIZATION FUNCTIONS ====================
function initializeApp() {
    const savedToken = localStorage.getItem('jwtToken');
    const savedUser = localStorage.getItem('currentUser');

    if (savedToken && savedUser) {
        jwtToken = savedToken;
        currentUser = JSON.parse(savedUser);
        updateUIAfterLogin();
        loadDashboardData();
    }
}

function setupEventListeners() {
    // Form submissions
    $('#loginForm').on('submit', (e) => { e.preventDefault(); loginUser(); });
    $('#registerForm').on('submit', (e) => { e.preventDefault(); registerUser(); });
    $('#reportForm').on('submit', (e) => { e.preventDefault(); reportLostItem(); });
    $('#foundForm').on('submit', (e) => { e.preventDefault(); reportFoundItem(); });
    $('#profileForm').on('submit', (e) => { e.preventDefault(); updateProfile(); });
    $('#securityForm').on('submit', (e) => { e.preventDefault(); updatePassword(); });
    $('#claimForm').on('submit', (e) => { e.preventDefault(); submitClaim(); });
    $('#editItemForm').on('submit', (e) => { e.preventDefault(); saveEditedItem(); });
    $('#matchItemForm').on('submit', (e) => { e.preventDefault(); submitMatch(); });
    $('#addHotelForm').on('submit', (e) => { e.preventDefault(); addHotel(); });
    $('#addStaffForm').on('submit', (e) => { e.preventDefault(); addStaff(); });
    $('#deliveryForm').on('submit', (e) => { e.preventDefault(); submitDeliveryRequest(); });

    // Navigation
    $('#logoutBtn').on('click', (e) => { e.preventDefault(); logoutUser(); });
    $('#profileLink').on('click', (e) => { e.preventDefault(); showProfileSection(); });
    $('#dashboardLink').on('click', (e) => { e.preventDefault(); showDashboardSection(); });
    $('#adminLink').on('click', (e) => { e.preventDefault(); showAdminSection(); });
    $('#homeLink').on('click', (e) => { e.preventDefault(); showHomeSection(); });

    // Modals
    $('#notificationsModal').on('show.bs.modal', () => loadNotifications());
    $('#markAllAsReadBtn').on('click', () => markAllNotificationsAsRead());
    $('#addStaffModal').on('show.bs.modal', () => loadHotelsForStaffModal());
    $('#archivedItemsModal').on('show.bs.modal', () => loadArchivedItems());

    // UI interactions
    $('#proofUploadArea').on('click', () => $('#claimProof').click());
    $('#claimProof').on('change', (e) => handleProofUpload(e));
    $('[data-filter]').on('click', function() {
        const filter = $(this).data('filter');
        $('[data-filter]').removeClass('active');
        $(this).addClass('active');
        filterItems(filter);
    });
    $('#addHotelBtn').on('click', () => $('#addHotelModal').modal('show'));
    $('#addStaffBtn').on('click', () => $('#addStaffModal').modal('show'));
    $("#deliveryMethod").on("change", function() {
        if ($(this).val() === "DELIVERY") {
            $("#deliveryAddressGroup").removeClass("d-none");
        } else {
            $("#deliveryAddressGroup").addClass("d-none");
        }
    });
}

// ==================== AUTHENTICATION FUNCTIONS ====================
function loginUser() {
    const email = $('#loginEmail').val();
    const password = $('#loginPassword').val();
    const isStaff = $('#userTypeCheck').is(':checked');

    const endpoint = isStaff ? '/auth/login' : '/auth/guest/login';
    const credentials = { email, password };

    $.ajax({
        url: API_BASE_URL + endpoint,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(credentials),
        success: function(response) {
            if (response.status === 200) {
                jwtToken = response.data.token;
                currentUser = response.data.user;

                localStorage.setItem('jwtToken', jwtToken);
                localStorage.setItem('currentUser', JSON.stringify(currentUser));

                updateUIAfterLogin();
                loadDashboardData();
                $('#loginModal').modal('hide');
                showSuccess('Login successful!');
            }
        },
        error: function(xhr) {
            showError('Login failed: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

// function registerUser() {
//     const name = $('#registerName').val();
//     const email = $('#registerEmail').val();
//     const phone = $('#registerPhone').val();
//     const password = $('#registerPassword').val();
//
//     const userData = { name, email, phone: phone || '', password };
//
//     $.ajax({
//         url: API_BASE_URL + '/auth/guest/register',
//         type: 'POST',
//         contentType: 'application/json',
//         data: JSON.stringify(userData),
//         success: function(response) {
//             if (response.status === 200) {
//                 showSuccess('Registration successful! Please login.');
//                 $('#registerModal').modal('hide');
//                 $('#loginModal').modal('show');
//             }
//         },
//         error: function(xhr) {
//             showError('Registration failed: ' + (xhr.responseJSON?.message || 'Unknown error'));
//         }
//     });
// }

async function registerUser() {
    const name = $('#registerName').val().trim();
    const email = $('#registerEmail').val().trim().toLowerCase();
    const phone = $('#registerPhone').val().trim();
    const password = $('#registerPassword').val();

    if (!name || !email || !password) {
        showError('Please fill all required fields.');
        return;
    }

    // Password policy
    const pwdPolicy = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
    if (!pwdPolicy.test(password)) {
        showError('Password must be at least 8 characters, include uppercase, lowercase and a number.');
        return;
    }

    try {
        const emailCheckResp = await $.ajax({
            url: API_BASE_URL + '/auth/check-email?email=' + encodeURIComponent(email),
            type: 'GET'
        });

        if (emailCheckResp?.data?.exists) {
            showError('Email is already registered. Please login or use another email.');
            return;
        }
    } catch (err) {
        console.warn('Email check failed (will still attempt register):', err);
    }

    const userData = { name, email, phone: phone || '', password };

    $.ajax({
        url: API_BASE_URL + '/auth/guest/register',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(userData),
        success: function(response) {
            if (response.status === 200 || response.status === 201) {
                showSuccess('Registration successful! Please login.');
                $('#registerModal').modal('hide');
                $('#loginModal').modal('show');
            }
        },
        error: function(xhr) {
            showError('Registration failed: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

function logoutUser() {
    showConfirm('Are you sure you want to logout?', function() {
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

// ==================== USER PROFILE FUNCTIONS ====================
function updateProfile() {
    const name = $('#profileName').val();
    const phone = $('#profilePhone').val();
    const email = $('#profileEmail').val();

    const profileData = { name, phone, email };
    const endpoint = currentUser.role === 'GUEST' ? '/guests/profile' : '/staff/profile';

    $.ajax({
        url: API_BASE_URL + endpoint,
        type: 'PUT',
        contentType: 'application/json',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        data: JSON.stringify(profileData),
        success: function(response) {
            if (response.status === 200) {
                currentUser.name = name;
                currentUser.phone = phone;
                currentUser.email = email;
                localStorage.setItem('currentUser', JSON.stringify(currentUser));

                $('#profileUserName').text(name);
                $('#profileUserEmail').text(email);
                showSuccess('Profile updated successfully!');
            }
        },
        error: function(xhr) {
            showError('Failed to update profile: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

function updatePassword() {
    const currentPassword = $('#currentPassword').val();
    const newPassword = $('#newPassword').val();
    const confirmPassword = $('#confirmPassword').val();

    if (newPassword !== confirmPassword) {
        showError('New passwords do not match!');
        return;
    }

    const passwordData = { oldPassword: currentPassword, newPassword };
    const endpoint = currentUser.role === 'GUEST' ? '/guests/me/password' : '/staff/me/password';

    $.ajax({
        url: API_BASE_URL + endpoint,
        type: 'PUT',
        contentType: 'application/json',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        data: JSON.stringify(passwordData),
        success: function(response) {
            if (response.status === 200) {
                showSuccess('Password updated successfully!');
                $('#securityForm')[0].reset();
            }
        },
        error: function(xhr) {
            showError('Failed to update password: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

function loadProfileData() {
    if (!currentUser) return;

    const endpoint = currentUser.role === 'GUEST' ? '/guests/profile' : '/staff/profile';

    $.ajax({
        url: API_BASE_URL + endpoint,
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                const profile = response.data;

                $('#profileUserName').text(profile.name || 'User Name');
                $('#profileUserRole').text(currentUser.role || 'Guest');
                $('#profileUserEmail').text(profile.email || 'email@example.com');

                $('#profileName').val(profile.name || '');
                $('#profileEmail').val(profile.email || '');
                $('#profilePhone').val(profile.phone || '');
            }
        },
        error: function(xhr) {
            console.error('Failed to load profile:', xhr);
        }
    });
}

// ==================== ITEM MANAGEMENT FUNCTIONS ====================
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
        locationLost: $('#lostLocation').val(),
        imagePath: imageUrl
    };

    $.ajax({
        url: API_BASE_URL + '/lost-items',
        type: 'POST',
        contentType: 'application/json',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        data: JSON.stringify(itemData),
        success: function(response) {
            if (response.status === 200 || response.status === 201) {
                showSuccess('Item reported successfully!');
                $('#reportModal').modal('hide');
                loadDashboardData();
            }
        },
        error: function(xhr) {
            console.error("Lost Item Error:", xhr);
            showError('Failed to report item: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

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
        locationFound: $('#foundLocation').val(),
        imagePath: imageUrl
    };

    $.ajax({
        url: API_BASE_URL + '/found-items',
        type: 'POST',
        contentType: 'application/json',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        data: JSON.stringify(itemData),
        success: function(response) {
            if (response.status === 200 || response.status === 201) {
                showSuccess('Found item reported successfully!');
                $('#foundModal').modal('hide');
                loadDashboardData();
            }
        },
        error: function(xhr) {
            console.error("Found Item Error:", xhr);
            showError('Failed to report found item: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

function submitClaim() {
    const foundItemId = $('#claimFoundItemId').val();
    const message = $('#claimMessage').val();

    const proofImageUrl = $('#claimProofImageUrl').val();

    const claimData = {
        foundItemId: foundItemId,
        message: message,
        proofImageUrl: proofImageUrl || null
    };

    $.ajax({
        url: API_BASE_URL + '/claims',
        type: 'POST',
        contentType: 'application/json',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        data: JSON.stringify(claimData),
        success: function(response) {
            if (response.status === 200) {
                showSuccess('Claim submitted successfully!');
                $('#claimItemModal').modal('hide');
                loadDashboardData();
            }
        },
        error: function(xhr) {
            console.error("Failed to submit claim:", xhr);
            showError('Failed to submit claim: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

function claimItem(itemId, itemName) {
    $('#claimItemId').val(itemId);
    $('#claimItemName').text(itemName);
    $('#claimMessage').val('');
    $('#proofPreview').html('');
    $('#claimItemModal').modal('show');
}

function editItem(itemId, type) {
    $.ajax({
        url: API_BASE_URL + `/${type}-items/${itemId}`,
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                const item = response.data;

                $('#editItemId').val(itemId);
                $('#editItemType').val(type);
                $('#editItemTitle').val(item.title || '');
                $('#editItemDescription').val(item.description || '');
                $('#editItemImagePath').val(item.imagePath || '');

                $('#editItemDate').val(type === 'lost' ? (item.dateLost || '') : (item.foundDate || ''));
                $('#editItemLocation').val(type === 'lost' ? (item.locationLost || '') : (item.locationFound || ''));

                if (item.imagePath) {
                    $('#editItemImagePreview').attr('src', `/uploads/${item.imagePath}`).show();
                } else {
                    $('#editItemImagePreview').hide();
                }

                $('#editItemModal').modal('show');
            }
        },
        error: function() {
            showError(`Failed to load ${type} item details`);
        }
    });
}

function saveEditedItem() {
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
        success: function(response) {
            if (response.status === 200) {
                showSuccess(`${type.charAt(0).toUpperCase() + type.slice(1)} item updated successfully!`);
                $('#editItemModal').modal('hide');
                loadDashboardData();
            }
        },
        error: function() {
            showError(`Failed to update ${type} item`);
        }
    });
}

function archiveItem(itemId, type) {
    showConfirm(`Are you sure you want to archive this ${type} item?`, function() {
        $.ajax({
            url: API_BASE_URL + `/${type}-items/${itemId}/archive`,
            type: 'PUT',
            headers: { 'Authorization': 'Bearer ' + jwtToken },
            success: function(response) {
                if (response.status === 200) {
                    showSuccess(`${type.charAt(0).toUpperCase() + type.slice(1)} item archived successfully!`);
                    loadDashboardData();
                }
            },
            error: function(xhr) {
                console.error(`Failed to archive ${type} item:`, xhr);
                showError(`Failed to archive ${type} item`);
            }
        });
    });
}

function unarchiveItem(itemId, type) {
    showConfirm(`Are you sure you want to unarchive this ${type} item?`, function() {
        $.ajax({
            url: API_BASE_URL + `/${type}-items/${itemId}/unarchive`,
            type: 'PUT',
            headers: { 'Authorization': 'Bearer ' + jwtToken },
            success: function(response) {
                if (response.status === 200) {
                    showSuccess(`${type.charAt(0).toUpperCase() + type.slice(1)} item unarchived successfully!`);
                    $('#archivedItemsModal').modal('hide');
                    loadDashboardData();
                }
            },
            error: function(xhr) {
                console.error(`Failed to unarchive ${type} item:`, xhr);
                showError(`Failed to unarchive ${type} item`);
            }
        });
    });
}

function matchItem(foundItemId) {
    $.ajax({
        url: API_BASE_URL + '/lost-items',
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                const lostItems = response.data;
                const select = $('#matchLostItemSelect');
                select.empty();

                lostItems.forEach(item => {
                    select.append(`<option value="${item.lostId}">${item.title} - ${item.guestName}</option>`);
                });

                $('#matchFoundItemId').val(foundItemId);
                $('#matchItemModal').modal('show');
            }
        },
        error: function(xhr) {
            showError('Failed to load lost items for matching');
        }
    });
}

function submitMatch() {
    const foundItemId = $('#matchFoundItemId').val();
    const lostItemId = $('#matchLostItemSelect').val();

    $.ajax({
        url: API_BASE_URL + `/found-items/matches?foundItemId=${foundItemId}&lostItemId=${lostItemId}`,
        type: 'POST',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                showSuccess('Item matched successfully!');
                $('#matchItemModal').modal('hide');
                loadDashboardData();
            }
        },
        error: function(xhr) {
            showError('Failed to match item: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

function viewItem(itemId, type) {
    $.ajax({
        url: API_BASE_URL + `/${type}-items/${itemId}`,
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                showItemDetails(response.data, type);
            }
        },
        error: function(xhr) {
            console.error(`Failed to load ${type} item:`, xhr);
            showError(`Failed to load ${type} item details`);
        }
    });
}

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
    $('#itemDetailsModal').modal('show');
}

// ==================== NOTIFICATION FUNCTIONS ====================
function loadNotifications() {
    if (!currentUser || currentUser.role !== 'GUEST') {
        $('#notificationsList').html('<p class="text-center">No notifications.</p>');
        $('#notificationsBadge').addClass('d-none');
        return;
    }

    if (currentUser.id) {
        $('#notificationsLoading').removeClass('d-none');

        $.ajax({
            url: API_BASE_URL + '/notifications/me',
            type: 'GET',
            headers: { 'Authorization': 'Bearer ' + jwtToken },
            success: function(response) {
                $('#notificationsLoading').addClass('d-none');

                if (response.status === 200) {
                    notifications = response.data;

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
            error: function(xhr) {
                $('#notificationsLoading').addClass('d-none');
                console.error('Failed to load notifications:', xhr);
                $('#notificationsList').html('<p class="text-center">Error loading notifications. Please try again later.</p>');
            }
        });
    }
}

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

    $('.mark-as-read-btn').on('click', function() {
        const notificationId = $(this).data('id');
        markNotificationAsRead(notificationId);
    });
}

function markNotificationAsRead(notificationId) {
    $.ajax({
        url: API_BASE_URL + '/notifications/' + notificationId + '/read',
        type: 'PUT',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                $(`.card[data-id="${notificationId}"]`).removeClass('border-primary');
                $(`.mark-as-read-btn[data-id="${notificationId}"]`).remove();

                const unreadCount = $('.card.border-primary').length;
                if (unreadCount > 0) {
                    $('#notificationsBadge').removeClass('d-none').text(unreadCount);
                } else {
                    $('#notificationsBadge').addClass('d-none');
                }

                showSuccess('Notification marked as read');
            }
        },
        error: function(xhr) {
            console.error('Failed to mark notification as read:', xhr);
            showError('Failed to mark notification as read. Please try again.');
        }
    });
}

function markAllNotificationsAsRead() {
    const unreadNotifications = notifications.filter(n => !n.isRead);

    if (unreadNotifications.length === 0) {
        showInfo('All notifications are already read');
        return;
    }

    $('.card').removeClass('border-primary');
    $('.mark-as-read-btn').remove();
    $('#notificationsBadge').addClass('d-none');

    const promises = unreadNotifications.map(notification => {
        return $.ajax({
            url: API_BASE_URL + '/notifications/' + notification.id + '/read',
            type: 'PUT',
            headers: { 'Authorization': 'Bearer ' + jwtToken }
        });
    });

    Promise.all(promises)
        .then(() => {
            showSuccess('All notifications marked as read');
        })
        .catch(error => {
            console.error('Failed to mark some notifications as read:', error);
            showError('Some notifications may not have been marked as read. Please try again.');
        });
}

// ==================== DASHBOARD FUNCTIONS ====================
function loadDashboardData() {
    if (!currentUser || !jwtToken) return;

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

    if (currentUser.role === 'GUEST') {
        loadNotifications();
    } else {
        $('#notificationsList').html('<p class="text-center">Notifications not available for this account.</p>');
        $('#notificationsBadge').addClass('d-none');
        notifications = [];
    }

    loadProfileData();
}

function loadAdminData() {
    // Load lost items
    $.ajax({
        url: API_BASE_URL + '/lost-items',
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                populateLostItemsTable(response.data);
            }
        },
        error: function(xhr) {
            console.error('Failed to load lost items:', xhr);
            showError('Failed to load lost items');
        }
    });

    // Load found items
    $.ajax({
        url: API_BASE_URL + '/found-items',
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                populateFoundItemsTable(response.data);
            }
        },
        error: function(xhr) {
            console.error('Failed to load found items:', xhr);
            showError('Failed to load found items');
        }
    });

    // Load stats
    loadAdminStats();

    // Load deliveries
    $.ajax({
        url: API_BASE_URL + '/deliveries',
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                $("#deliveryRequestsSection").removeClass("d-none");
                populateDeliveryRequestsTable(response.data);
                $("#deliveryRequestsCount").text(response.data.length);
            }
        },
        error: function(xhr) {
            console.error('Failed to load deliveries:', xhr);
        }
    });

    // Load hotels
    $.ajax({
        url: API_BASE_URL + '/hotels',
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                populateHotelsTable(response.data);
            }
        },
        error: function(xhr) {
            console.error('Failed to load hotels:', xhr);
        }
    });

    // Load claims
    $.ajax({
        url: API_BASE_URL + '/claims',
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                populateClaimRequestsTable(response.data);
            }
        },
        error: function(xhr) {
            console.error('Failed to load claims:', xhr);
        }
    });

    // Load staff
    $.ajax({
        url: API_BASE_URL + '/staff/all',
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                populateStaffTable(response.data);
            }
        },
        error: function(xhr) {
            console.error('Failed to load staff:', xhr);
        }
    });
}

function loadStaffData() {
    // Load lost items
    $.ajax({
        url: API_BASE_URL + '/lost-items',
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                populateLostItemsTable(response.data);
            }
        },
        error: function(xhr) {
            console.error('Failed to load lost items:', xhr);
            showError('Failed to load lost items');
        }
    });

    // Load found items
    $.ajax({
        url: API_BASE_URL + '/found-items',
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                populateFoundItemsTable(response.data);
            }
        },
        error: function(xhr) {
            console.error('Failed to load found items:', xhr);
            showError('Failed to load found items');
        }
    });

    // Load staff stats
    loadStaffStats();
}

function loadGuestData() {
    // Load guest's lost items
    $.ajax({
        url: API_BASE_URL + '/lost-items/me',
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                const guestItems = response.data.filter(item => {
                    return item.guestId === currentUser.guestId ||
                        (item.guest && item.guest.guestId === currentUser.guestId) ||
                        (currentUser.id && item.guestId === currentUser.id);
                });
                populateLostItemsTable(guestItems);
            }
        },
        error: function(xhr) {
            console.error('Failed to load lost items:', xhr);
            showError('Failed to load your lost items');
        }
    });

    // Load found items
    $.ajax({
        url: API_BASE_URL + '/found-items/unclaimed',
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                const unclaimedItems = response.data.filter(item =>
                    item.status && item.status.toLowerCase().includes('unclaimed')
                );
                populateFoundItemsTable(unclaimedItems);
            }
        },
        error: function(xhr) {
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

function showProfileSection() {
    $('#profileSection').removeClass('d-none');
    $('.dashboard-section:not(#profileSection)').addClass('d-none');
    $('.admin-panel').addClass('d-none');
}

function showDashboardSection() {
    $('#profileSection').addClass('d-none');
    $('.dashboard-section:not(#profileSection)').removeClass('d-none');
    $('.admin-panel').addClass('d-none');
}

function showAdminSection() {
    $('#profileSection').addClass('d-none');
    $('.dashboard-section').addClass('d-none');
    $('.admin-panel').removeClass('d-none');
}

function showHomeSection() {
    $('#dashboardSection').addClass('d-none');
}

// ==================== TABLE POPULATION FUNCTIONS ====================
async function populateLostItemsTable(items) {
    const tbody = $('#lostItemsTable tbody');
    tbody.empty();

    // Always reload deliveries to prevent duplicate request buttons
    let deliveries = [];
    try {
        const deliveryResp = await $.ajax({
            url: API_BASE_URL + "/deliveries",
            type: "GET",
            headers: { "Authorization": "Bearer " + jwtToken }
        });
        if (deliveryResp.status === 200) {
            deliveries = deliveryResp.data || [];
        }
    } catch (err) {
        console.warn("Failed to reload deliveries:", err);
    }

    if (!items || items.length === 0) {
        tbody.append('<tr><td colspan="6" class="text-center py-4">No lost items found</td></tr>');
        return;
    }

    items.forEach(item => {
        const statusClass = getStatusClass(item.status);
        const hasDelivery = deliveries.some(d => d.lostItemId === item.lostId);

        let actionHtml = `
            <button class="btn btn-sm btn-outline-primary view-item action-btn" data-id="${item.lostId}">View</button>
        `;

        if (currentUser.role === 'GUEST') {
            actionHtml += `
                <button class="btn btn-sm btn-outline-secondary edit-item action-btn" data-id="${item.lostId}">Edit</button>
            `;
            if ((item.status === 'CLAIMED' || item.status === 'MATCHED') && !hasDelivery) {
                actionHtml += `
                    <button class="btn btn-sm btn-primary request-delivery action-btn" data-id="${item.lostId}">
                        Request Delivery
                    </button>
                `;
            } else if (hasDelivery) {
                actionHtml += `<span class="text-muted">Delivery requested</span>`;
            }
        }

        if (currentUser.role === 'ADMIN' || currentUser.role === 'STAFF') {
            actionHtml += `
                <button class="btn btn-sm btn-outline-danger archive-item action-btn" data-id="${item.lostId}">
                    Archive
                </button>
            `;
        }

        const row = `
            <tr data-status="${item.status ? item.status.toLowerCase() : ''}">
                <td>${item.title || 'Unknown Item'}</td>
                <td>${item.guestName || (item.guest && item.guest.name) || 'Unknown Guest'}</td>
                <td>${item.location || 'N/A'}</td>
                <td>${formatDate(item.createdAt) || 'N/A'}</td>
                <td><span class="status-badge ${statusClass}">${item.status || 'UNKNOWN'}</span></td>
                <td>${actionHtml}</td>
            </tr>
        `;
        tbody.append(row);
    });

    // Bind events
    $('.view-item').on('click', function() {
        viewItem($(this).data('id'), 'lost');
    });

    $('.edit-item').on('click', function() {
        editItem($(this).data('id'), 'lost');
    });

    $('.archive-item').on('click', function() {
        archiveItem($(this).data('id'), 'lost');
    });

    $('.request-delivery').on('click', function() {
        openDeliveryModal($(this).data('id'));
    });
}

function populateFoundItemsTable() {
    const tbody = $('#foundItemsTable tbody');
    tbody.empty();

    // Build ajax options
    const ajaxOptions = {
        url: API_BASE_URL + '/found-items',
        type: 'GET',
        success: function(response) {
            if (response.status === 200 && response.data) {
                const items = response.data;

                if (!items || items.length === 0) {
                    tbody.append('<tr><td colspan="5" class="text-center py-4">No found items found</td></tr>');
                    return;
                }

                items.forEach(item => {
                    const isUnclaimed = (item.status || '').toUpperCase() === 'UNCLAIMED';
                    const isClaimed = (item.status || '').toUpperCase() === 'CLAIMED';
                    const isMatched = (item.status || '').toUpperCase() === 'MATCHED';

                    // always allow view
                    let actionHtml = `<button class="btn btn-sm btn-outline-primary view-item action-btn" data-id="${item.foundId}">View</button>`;

                    // admin/staff logic
                    if (currentUser?.role === 'ADMIN' || currentUser?.role === 'STAFF') {
                        if (isUnclaimed) {
                            actionHtml += `
                                <button class="btn btn-sm btn-outline-success match-item action-btn" data-id="${item.foundId}">Match</button>
                                <button class="btn btn-sm btn-outline-danger archive-item action-btn" data-id="${item.foundId}">Archive</button>
                            `;
                        }
                    }

                    // guest logic
                    if (currentUser?.role === 'GUEST' && isUnclaimed) {
                        actionHtml += `
                            <button class="btn btn-sm btn-outline-warning claim-item action-btn" 
                                data-id="${item.foundId}" 
                                data-name="${item.title || 'Unknown Item'}">
                                Claim
                            </button>
                        `;
                    }

                    const statusClass = getStatusClass(item.status);
                    const row = `
                        <tr data-status="${item.status ? item.status.toLowerCase() : ''}">
                            <td>${item.title || 'Unknown Item'}</td>
                            <td>${item.staffName || (item.staff && item.staff.name) || 'Unknown Staff'}</td>
                            <td>${formatDate(item.createdAt) || 'N/A'}</td>
                            <td><span class="status-badge ${statusClass}">${item.status || 'UNKNOWN'}</span></td>
                            <td>${actionHtml}</td>
                        </tr>
                    `;
                    tbody.append(row);
                });

                // bind actions
                $('.view-item').on('click', function() {
                    viewItem($(this).data('id'), 'found');
                });
                $('.match-item').on('click', function() {
                    matchItem($(this).data('id'));
                });
                $('.archive-item').on('click', function() {
                    archiveItem($(this).data('id'), 'found');
                });
                $('.claim-item').on('click', function() {
                    claimItem($(this).data('id'), $(this).data('name'));
                });
            }
        },
        error: function(xhr) {
            console.error("Failed to load found items:", xhr);
            tbody.append('<tr><td colspan="5" class="text-center py-4 text-danger">Error loading found items</td></tr>');
        }
    };

    // Only attach token if available
    if (jwtToken) {
        ajaxOptions.headers = { 'Authorization': 'Bearer ' + jwtToken };
    }

    $.ajax(ajaxOptions);
}

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

    $('.edit-hotel').on('click', function() {
        const id = $(this).data('id');
        editHotel(id);
    });

    $('.delete-hotel').on('click', function() {
        const id = $(this).data('id');
        deleteHotel(id);
    });
}

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

    $('.edit-staff').on('click', function() {
        const id = $(this).data('id');
        editStaff(id);
    });

    $('.delete-staff').on('click', function() {
        const id = $(this).data('id');
        deleteStaff(id);
    });
}

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

    $('.view-claim').on('click', function() {
        const claimId = $(this).data('id');
        viewClaim(claimId);
    });

    $('.approve-claim').on('click', function() {
        const claimId = $(this).data('id');
        approveClaim(claimId);
    });

    $('.reject-claim').on('click', function() {
        const claimId = $(this).data('id');
        rejectClaim(claimId);
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
        const isShipped = (d.status || '').toUpperCase() === 'SHIPPED';
        const isDelivered = (d.status || '').toUpperCase() === 'DELIVERED';
        let actionButtons = '';

        // Admin only can update; staff can view only
        if (currentUser.role === 'ADMIN') {
            // Show shipped button only if NOT already shipped or delivered
            if (!isShipped && !isDelivered) {
                actionButtons += `<button class="btn btn-sm btn-success update-delivery" data-id="${d.deliveryId}" data-status="SHIPPED">Mark Shipped</button> `;
                actionButtons += `<button class="btn btn-sm btn-primary update-delivery" data-id="${d.deliveryId}" data-status="DELIVERED">Mark Delivered</button>`;
            } else if (isShipped && !isDelivered) {
                actionButtons += `<button class="btn btn-sm btn-primary update-delivery" data-id="${d.deliveryId}" data-status="DELIVERED">Mark Delivered</button>`;
            } else {
                actionButtons += '<span class="text-muted">No actions</span>';
            }
        } else if (currentUser.role === 'STAFF') {
            actionButtons = '<span class="text-muted">View only</span>';
        } else {
            // guests: show nothing (or custom)
            actionButtons = '<span class="text-muted">N/A</span>';
        }

        tbody.append(`
        <tr>
            <td>${d.deliveryId}</td>
            <td>${d.lostItemId}</td>
            <td>${d.method}</td>
            <td>${d.address || "-"}</td>
            <td><span class="status-badge ${getStatusClass(d.status)}">${d.status}</span></td>
            <td>${actionButtons}</td>
        </tr>
    `);
    });

    $("#deliveryRequestsCount").text(deliveries.length);

    $(".update-delivery").on("click", function() {
        const id = $(this).data("id");
        const status = $(this).data("status");
        updateDeliveryStatus(id, status);
    });
}

// ==================== ADMIN MANAGEMENT FUNCTIONS ====================
function addHotel() {
    const name = $('#hotelName').val();
    const address = $('#hotelAddress').val();
    const phone = $('#hotelPhone').val();
    const hotelData = { name, address, phone };

    $.ajax({
        url: API_BASE_URL + '/hotels',
        type: 'POST',
        contentType: 'application/json',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        data: JSON.stringify(hotelData),
        success: function(response) {
            if (response.status === 200) {
                showSuccess('Hotel added successfully!');
                $('#addHotelModal').modal('hide');
                loadAdminData();
            }
        },
        error: function(xhr) {
            showError('Failed to add hotel: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

async function addStaff() {
    const staffData = {
        name: $('#staffName').val(),
        email: $('#staffEmail').val().trim().toLowerCase(),
        phone: $('#staffPhone').val(),
        password: $('#staffPassword').val(),
        role: $('#staffRole').val(),
        hotelId: parseInt($('#staffHotelId').val(), 10)
    };

    if (!staffData.name || !staffData.email || !staffData.password) {
        showError('Please fill all required fields.');
        return;
    }

    // Password policy
    const pwdPolicy = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
    if (!pwdPolicy.test(staffData.password)) {
        showError('Password must be at least 8 characters, include uppercase, lowercase and a number.');
        return;
    }

    try {
        const emailCheckResp = await $.ajax({
            url: API_BASE_URL + '/auth/check-email?email=' + encodeURIComponent(staffData.email),
            type: 'GET',
            headers: { 'Authorization': 'Bearer ' + jwtToken }
        });

        if (emailCheckResp?.data?.exists) {
            showError('Email is already registered. Please use another email.');
            return;
        }
    } catch (err) {
        console.warn('Email check failed (still attempting add staff):', err);
    }

    $.ajax({
        url: API_BASE_URL + '/auth/register',
        type: 'POST',
        contentType: 'application/json',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        data: JSON.stringify(staffData),
        success: function(response) {
            showSuccess('Staff added successfully!');
            $('#addStaffModal').modal('hide');
            loadAdminData();
        },
        error: function(xhr) {
            showError('Failed to add staff: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

function editHotel(hotelId) {
    $.ajax({
        url: API_BASE_URL + `/hotels/${hotelId}`,
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                const hotel = response.data;
                $('#editHotelId').val(hotel.hotelId);
                $('#editHotelName').val(hotel.name);
                $('#editHotelAddress').val(hotel.address);
                $('#editHotelPhone').val(hotel.phone);
                $('#editHotelModal').modal('show');
            }
        },
        error: function() {
            showError("Failed to load hotel details");
        }
    });
}

function deleteHotel(hotelId) {
    showConfirm("Are you sure you want to delete this hotel?", function() {
        $.ajax({
            url: API_BASE_URL + `/hotels/${hotelId}`,
            type: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + jwtToken },
            success: function(response) {
                if (response.status === 200) {
                    showSuccess("Hotel deleted successfully!");
                    loadAdminData();
                }
            },
            error: function() {
                showError("Failed to delete hotel");
            }
        });
    });
}

function editStaff(staffId) {
    $.ajax({
        url: API_BASE_URL + `/staff/${staffId}`,
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
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
        error: function() {
            showError("Failed to load staff details");
        }
    });
}

function deleteStaff(staffId) {
    showConfirm("Are you sure you want to delete this staff member?", function() {
        $.ajax({
            url: API_BASE_URL + `/staff/${staffId}`,
            type: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + jwtToken },
            success: function(response) {
                if (response.status === 200) {
                    showSuccess("Staff deleted successfully!");
                    loadAdminData();
                }
            },
            error: function() {
                showError("Failed to delete staff");
            }
        });
    });
}

function viewClaim(claimId) {
    $.ajax({
        url: API_BASE_URL + `/claims/${claimId}`,
        type: "GET",
        headers: { "Authorization": "Bearer " + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                const claim = response.data;
                claimGuestId = claim.guestId;

                $("#claimItemTitle").text(claim.foundItemTitle || "Unknown Item");
                $("#claimGuestName").text(claim.guestName || "Unknown Guest");
                $("#claimGuestEmail").text(claim.guestEmail || "N/A");
                $("#claimStatus").text(claim.status);

                if (claim.status === 'APPROVED') {
                    $('#approveClaimBtn').prop('disabled', true);
                    $('#rejectClaimBtn').prop('disabled', false);
                } else if (claim.status === 'REJECTED') {
                    $('#rejectClaimBtn').prop('disabled', true);
                    $('#approveClaimBtn').prop('disabled', false);
                } else {
                    $('#approveClaimBtn').prop('disabled', false);
                    $('#rejectClaimBtn').prop('disabled', false);
                }

                $("#claimMessageText").text(claim.message || "No message provided");

                if (claim.proofImagePath) {
                    $("#claimImage").attr("src", claim.proofImagePath);
                    $("#claimImageContainer").show();
                } else {
                    $("#claimImageContainer").hide();
                }

                $("#viewClaimModal").modal("show");

                $("#approveClaimBtn").off("click").on("click", function() {
                    approveClaim(claimId);
                });

                $("#rejectClaimBtn").off("click").on("click", function() {
                    const reason = prompt("Enter reason for rejection:");
                    if (reason) rejectClaim(claimId, reason);
                });
            }
        },
        error: function(xhr) {
            console.error("Failed to load claim details:", xhr);
            showError("Failed to load claim details");
        }
    });
}

function approveClaim(claimId) {
    $.ajax({
        url: API_BASE_URL + `/claims/${claimId}/approve`,
        type: "PUT",
        headers: { "Authorization": "Bearer " + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                showSuccess("Claim approved successfully!");
                $("#viewClaimModal").modal("hide");
                loadAdminData(); // refresh
            }
        },
        error: function(xhr) {
            console.error("Failed to approve claim:", xhr);
            showError("Failed to approve claim");
        }
    });
}

function rejectClaim(claimId, reason) {
    $.ajax({
        url: API_BASE_URL + `/claims/${claimId}/reject?reason=${encodeURIComponent(reason)}`,
        type: 'PUT',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                showSuccess('Claim rejected successfully!');
                loadDashboardData();
            }
        },
        error: function(xhr) {
            console.error("Failed to reject claim:", xhr);
            showError('Failed to reject claim: ' + (xhr.responseJSON?.message || 'Unauthorized'));
        }
    });
}

// ==================== DELIVERY FUNCTIONS ====================
function openDeliveryModal(lostItemId) {
    $("#deliveryLostItemId").val(lostItemId);
    $("#deliveryMethod").val("");
    $("#deliveryAddress").val("");
    $("#deliveryAddressGroup").addClass("d-none");
    $("#deliveryModal").modal("show");
}

function submitDeliveryRequest() {
    const lostItemId = $("#deliveryLostItemId").val();
    const method = $("#deliveryMethod").val();
    const address = method === "DELIVERY" ? $("#deliveryAddress").val() : null;

    $.ajax({
        url: API_BASE_URL + "/deliveries",
        type: "POST",
        headers: { "Authorization": "Bearer " + jwtToken },
        contentType: "application/json",
        data: JSON.stringify({ lostItemId, method, address }),
        success: function(response) {
            if (response.status === 200) {
                Swal.fire("Success", "Delivery request submitted!", "success");
                $("#deliveryModal").modal("hide");
                loadDeliveries();     // updates allDeliveries
                // disable the request button for the item
                $(`.request-delivery[data-id="${lostItemId}"]`).replaceWith('<span class="text-muted">Delivery requested</span>');
            }
        },
        error: function(xhr) {
            Swal.fire("Error", "Failed to request delivery.", "error");
        }
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


function loadDeliveries() {
    $.ajax({
        url: API_BASE_URL + "/deliveries",
        type: "GET",
        headers: { "Authorization": "Bearer " + jwtToken },
        success: function(response) {
            if (response.status === 200) {
                allDeliveries = response.data || [];
                if (currentUser && currentUser.role === 'GUEST') {
                    const guestDeliveries = allDeliveries.filter(d => d.guestId === currentUser.id || d.requestedBy === currentUser.id);
                    populateDeliveryRequestsTable(guestDeliveries);
                } else {
                    populateDeliveryRequestsTable(allDeliveries);
                }
            }
        }
    });
}

// ==================== UTILITY FUNCTIONS ====================
async function uploadImageToImgbb(file) {
    const formData = new FormData();
    formData.append("image", file);

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
            if ($('#proofPreview').length) {
                $('#proofPreview').html(`
                            <div class="alert alert-success">
                                <i class="fas fa-check-circle me-2"></i>
                                Image uploaded successfully!
                            </div>
                        `);
            }
            $('#claimProofImageUrl').val(uploadedImageUrl);
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

function handleProofUpload(e) {
    const file = e.target.files[0];
    if (!file) return;

    const apiKey = "c8855e05a325a45010e5109d472fce84";
    const formData = new FormData();
    formData.append("image", file);

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
        success: function(response) {
            if (response && response.data && response.data.url) {
                const imageUrl = response.data.url;
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
        error: function(err) {
            console.error("Upload failed:", err);
            $('#proofPreview').html(`
                        <div class="alert alert-danger">Error uploading file.</div>
                    `);
        }
    });
}

function loadHotelsForStaffModal() {
    $.ajax({
        url: API_BASE_URL + '/hotels',
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
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
        error: function(xhr) {
            showError('Failed to load hotels: ' + (xhr.responseJSON?.message || 'Unknown error'));
        }
    });
}

function loadArchivedItems() {
    const tbody = $('#archivedItemsTableBody');
    tbody.empty();

    // Load Lost Items
    $.ajax({
        url: API_BASE_URL + '/lost-items/archived',
        type: 'GET',
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
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
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(response) {
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
}

function filterItems(filter) {
    if (filter === 'all') {
        $('#lostItemsTable tbody tr, #foundItemsTable tbody tr').show();
    } else {
        $('#lostItemsTable tbody tr').hide();
        $('#foundItemsTable tbody tr').hide();
        $(`#lostItemsTable tbody tr[data-status*="${filter}"], #foundItemsTable tbody tr[data-status*="${filter}"]`).show();
    }
}

function getStatusClass(status) {
    switch ((status || '').toUpperCase()) {
        case 'APPROVED': return 'bg-success';
        case 'REJECTED': return 'bg-danger';
        case 'PENDING': return 'bg-warning';
        default: return 'bg-secondary';
    }
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';

    try {
        const normalized = dateString.replace(' ', 'T');
        const date = new Date(normalized);

        if (isNaN(date)) return 'N/A';

        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
    } catch (e) {
        return 'N/A';
    }
}

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

    if (currentUser.role === 'ADMIN') {
        $('#dashboardLink').text('Admin Dashboard');
    } else if (currentUser.role === 'STAFF') {
        $('#dashboardLink').text('Staff Dashboard');
    } else {
        $('#dashboardLink').text('My Dashboard');
    }
}

// ==================== STATISTICS FUNCTIONS ====================
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
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(res) {
            if (res && res.status === 200 && res.data) {
                $('#lostItemsCount').text(res.data.lostItems ?? 0);
                $('#foundItemsCount').text(res.data.foundItems ?? 0);
                $('#matchedItemsCount').text(res.data.matchedItems ?? 0);
                $('#guestsCount').text(res.data.guests ?? 0);
            } else {
                console.error("Admin stats response invalid:", res);
            }
        },
        error: function(xhr) {
            console.error("Failed to load admin stats:", xhr.responseJSON || xhr.statusText);
        }
    });
}

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
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(res) {
            if (res && res.status === 200 && res.data) {
                $('#staffLostItemsCount').text(res.data.lostItems ?? 0);
                $('#staffFoundItemsCount').text(res.data.foundItems ?? 0);
                $('#staffMatchedItemsCount').text(res.data.matchedItems ?? 0);
            } else {
                console.error("Staff stats response invalid:", res);
            }
        },
        error: function(xhr) {
            console.error("Failed to load staff stats:", xhr.responseJSON || xhr.statusText);
        }
    });
}

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
        headers: { 'Authorization': 'Bearer ' + jwtToken },
        success: function(res) {
            if (res && res.status === 200 && res.data) {
                $('#guestLostItemsCount').text(res.data.lostItems ?? 0);
                $('#guestMatchedItemsCount').text(res.data.matchedItems ?? 0);
            } else {
                console.error("Guest stats response invalid:", res);
            }
        },
        error: function(xhr) {
            console.error("Failed to load guest stats:", xhr.responseJSON || xhr.statusText);
        }
    });
}

// ==================== NOTIFICATION FUNCTIONS ====================
function showSuccess(message) {
    Toast.fire({
        icon: 'success',
        title: message
    });
}

function showError(message) {
    Toast.fire({
        icon: 'error',
        title: message
    });
}

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

function showInfo(message) {
    Swal.fire({
        icon: 'info',
        title: message,
        showConfirmButton: false,
        timer: 2000
    });
}

// ==================== Clear forms when modals open ====================
const modalFormResetMap = [
    { modal: '#loginModal', form: '#loginForm' },
    { modal: '#registerModal', form: '#registerForm' },
    { modal: '#reportModal', form: '#reportForm' },
    { modal: '#foundModal', form: '#foundForm' },
    { modal: '#deliveryModal', form: '#deliveryForm' },
    { modal: '#viewClaimModal', form: '#viewClaimForm' },
    { modal: '#addStaffModal', form: '#addStaffForm' },
    { modal: '#addHotelModal', form: '#addHotelForm' },
];

modalFormResetMap.forEach(m => {
    if ($(m.modal).length) {
        $(m.modal).on('show.bs.modal', function () {
            // reset HTML form fields
            const $form = $(m.form);
            if ($form.length) {
                $form[0].reset();
                // Clear any validation messages (if present)
                $form.find('.is-invalid').removeClass('is-invalid');
                $form.find('.invalid-feedback').remove();
            }

            // Clear file inputs / previews inside modal
            $(this).find('input[type="file"]').val('');
            $(this).find('#proofPreview, #imagePreview, #foundPreview, #lostPreview').html('');
            $(this).find('.d-none').addClass('d-none');
        });
    }
});

// after upload success
function handleImageUploadSuccess(uploadedImageUrl) {
    $('#claimProofImageUrl').val(uploadedImageUrl);
}
