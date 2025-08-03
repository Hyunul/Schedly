// Global Variables
let currentUser = null;
let authToken = localStorage.getItem('authToken');
const API_BASE_URL = 'http://localhost:8080/api';

// DOM Content Loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    setupEventListeners();
    checkAuthStatus();
});

// Initialize Application
function initializeApp() {
    // Set up navigation
    setupNavigation();
    
    // Set today's date as default
    const today = new Date().toISOString().split('T')[0];
    const dateInputs = document.querySelectorAll('input[type="date"]');
    dateInputs.forEach(input => {
        if (!input.value) {
            input.value = today;
        }
    });

    // Load initial data
    if (authToken) {
        loadDashboardData();
        loadGroups();
        loadSchedules();
    }
}

// Setup Event Listeners
function setupEventListeners() {
    // Navigation toggle
    const navToggle = document.getElementById('navToggle');
    const navMenu = document.getElementById('navMenu');
    
    if (navToggle) {
        navToggle.addEventListener('click', () => {
            navMenu.classList.toggle('active');
            navToggle.classList.toggle('active');
        });
    }

    // Form submissions
    setupFormHandlers();
    
    // Modal close handlers
    setupModalHandlers();
}

// Setup Navigation
function setupNavigation() {
    const navLinks = document.querySelectorAll('.nav-link');
    const sections = document.querySelectorAll('.section');

    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = link.getAttribute('href').substring(1);
            
            // Update active nav link
            navLinks.forEach(l => l.classList.remove('active'));
            link.classList.add('active');
            
            // Show target section
            sections.forEach(section => {
                section.classList.remove('active');
                if (section.id === targetId) {
                    section.classList.add('active');
                    section.classList.add('fade-in');
                }
            });

            // Close mobile menu
            document.getElementById('navMenu').classList.remove('active');
            document.getElementById('navToggle').classList.remove('active');
        });
    });
}

// Setup Form Handlers
function setupFormHandlers() {
    // Create Group Form
    const createGroupForm = document.getElementById('createGroupForm');
    if (createGroupForm) {
        createGroupForm.addEventListener('submit', handleCreateGroup);
    }

    // Create Schedule Form
    const createScheduleForm = document.getElementById('createScheduleForm');
    if (createScheduleForm) {
        createScheduleForm.addEventListener('submit', handleCreateSchedule);
    }

    // Analysis Form
    const analysisForm = document.getElementById('analysisForm');
    if (analysisForm) {
        analysisForm.addEventListener('submit', handleAnalysisRequest);
    }
}

// Setup Modal Handlers
function setupModalHandlers() {
    // Close modals when clicking outside
    window.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal')) {
            e.target.style.display = 'none';
        }
    });
}

// Authentication Functions
function checkAuthStatus() {
    if (!authToken) {
        // Redirect to login page or show login modal
        showLoginRequired();
        return false;
    }
    return true;
}

function showLoginRequired() {
    showAlert('로그인이 필요합니다.', 'warning');
}

function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        localStorage.removeItem('authToken');
        authToken = null;
        currentUser = null;
        // Redirect to login page
        window.location.href = '/login';
    }
}

// API Functions
async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            ...(authToken && { 'Authorization': `Bearer ${authToken}` })
        }
    };

    const finalOptions = { ...defaultOptions, ...options };
    
    try {
        const response = await fetch(url, finalOptions);
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'API 요청 실패');
        }
        
        return data;
    } catch (error) {
        console.error('API Error:', error);
        showAlert(error.message, 'error');
        throw error;
    }
}

// Dashboard Functions
async function loadDashboardData() {
    try {
        // This would typically be separate API calls
        // For now, we'll use mock data
        updateDashboardStats({
            totalGroups: 3,
            todaySchedules: 5,
            upcomingMeetings: 2
        });
    } catch (error) {
        console.error('Failed to load dashboard data:', error);
    }
}

function updateDashboardStats(stats) {
    // Update stat cards if they exist
    const elements = {
        totalGroups: document.querySelector('[th\\:text="${totalGroups ?: 0}"]'),
        todaySchedules: document.querySelector('[th\\:text="${todaySchedules ?: 0}"]'),
        upcomingMeetings: document.querySelector('[th\\:text="${upcomingMeetings ?: 0}"]')
    };

    Object.keys(elements).forEach(key => {
        if (elements[key] && stats[key] !== undefined) {
            elements[key].textContent = stats[key];
        }
    });
}

// Group Functions
async function loadGroups() {
    try {
        const response = await apiRequest('/groups');
        displayGroups(response.data);
    } catch (error) {
        console.error('Failed to load groups:', error);
    }
}

function displayGroups(groups) {
    const groupsGrid = document.querySelector('.groups-grid');
    if (!groupsGrid || !groups) return;

    groupsGrid.innerHTML = groups.map(group => `
        <div class="card group-card">
            <div class="group-header">
                <h3>${group.name}</h3>
                <div class="group-actions">
                    <button class="btn-icon" onclick="viewGroup(${group.id})">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn-icon" onclick="editGroup(${group.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                </div>
            </div>
            <p class="group-description">${group.description || '설명 없음'}</p>
            <div class="group-stats">
                <div class="stat">
                    <i class="fas fa-users"></i>
                    <span>${group.memberCount}명</span>
                </div>
                <div class="stat">
                    <i class="fas fa-crown"></i>
                    <span>${group.ownerName}</span>
                </div>
            </div>
            <div class="group-footer">
                <span class="created-date">생성일: ${formatDate(group.createdAt)}</span>
            </div>
        </div>
    `).join('');
}

async function handleCreateGroup(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const groupData = {
        name: formData.get('groupName') || document.getElementById('groupName').value,
        description: formData.get('groupDescription') || document.getElementById('groupDescription').value
    };

    try {
        showLoading('그룹 생성 중...');
        const response = await apiRequest('/groups', {
            method: 'POST',
            body: JSON.stringify(groupData)
        });
        
        showAlert('그룹이 생성되었습니다.', 'success');
        closeModal('createGroupModal');
        e.target.reset();
        loadGroups();
    } catch (error) {
        console.error('Failed to create group:', error);
    } finally {
        hideLoading();
    }
}

function viewGroup(groupId) {
    // Navigate to group detail or show group members
    console.log('Viewing group:', groupId);
    showAlert('그룹 상세 기능은 구현 예정입니다.', 'info');
}

function editGroup(groupId) {
    // Open edit modal
    console.log('Editing group:', groupId);
    showAlert('그룹 편집 기능은 구현 예정입니다.', 'info');
}

// Schedule Functions
async function loadSchedules() {
    try {
        const startDate = document.getElementById('startDate')?.value || getTodayDate();
        const endDate = document.getElementById('endDate')?.value || getNextMonthDate();
        
        const response = await apiRequest(`/schedules?startDate=${startDate}&endDate=${endDate}`);
        displaySchedules(response.data);
    } catch (error) {
        console.error('Failed to load schedules:', error);
    }
}

function displaySchedules(schedules) {
    const scheduleList = document.querySelector('.schedule-list');
    if (!scheduleList || !schedules) return;

    if (schedules.length === 0) {
        scheduleList.innerHTML = '<div class="card"><p>등록된 일정이 없습니다.</p></div>';
        return;
    }

    scheduleList.innerHTML = schedules.map(schedule => `
        <div class="card schedule-item">
            <div class="schedule-time">
                <div class="time-badge">${schedule.startTime}-${schedule.endTime}</div>
                <div class="date-info">${formatDateKorean(schedule.date)}</div>
            </div>
            <div class="schedule-content">
                <h3>${schedule.title}</h3>
                <p>${schedule.description || '설명 없음'}</p>
                <div class="schedule-type ${schedule.type.toLowerCase()}">
                    <span>${getScheduleTypeText(schedule.type)}</span>
                </div>
            </div>
            <div class="schedule-actions">
                <button class="btn-icon" onclick="editSchedule(${schedule.id})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn-icon delete" onclick="deleteSchedule(${schedule.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
    `).join('');
}

async function handleCreateSchedule(e) {
    e.preventDefault();
    
    const scheduleData = {
        title: document.getElementById('scheduleTitle').value,
        date: document.getElementById('scheduleDate').value,
        startTime: document.getElementById('scheduleStartTime').value,
        endTime: document.getElementById('scheduleEndTime').value,
        type: document.getElementById('scheduleType').value,
        description: document.getElementById('scheduleDescription').value
    };

    try {
        showLoading('일정 생성 중...');
        const response = await apiRequest('/schedules', {
            method: 'POST',
            body: JSON.stringify(scheduleData)
        });
        
        showAlert('일정이 생성되었습니다.', 'success');
        closeModal('createScheduleModal');
        e.target.reset();
        loadSchedules();
    } catch (error) {
        console.error('Failed to create schedule:', error);
    } finally {
        hideLoading();
    }
}

function editSchedule(scheduleId) {
    console.log('Editing schedule:', scheduleId);
    showAlert('일정 편집 기능은 구현 예정입니다.', 'info');
}

async function deleteSchedule(scheduleId) {
    if (!confirm('이 일정을 삭제하시겠습니까?')) return;
    
    try {
        showLoading('일정 삭제 중...');
        await apiRequest(`/schedules/${scheduleId}`, {
            method: 'DELETE'
        });
        
        showAlert('일정이 삭제되었습니다.', 'success');
        loadSchedules();
    } catch (error) {
        console.error('Failed to delete schedule:', error);
    } finally {
        hideLoading();
    }
}

function filterSchedules() {
    loadSchedules();
}

// Analysis Functions
async function handleAnalysisRequest(e) {
    e.preventDefault();
    
    const groupId = document.getElementById('groupSelect').value;
    const analysisData = {
        targetDate: document.getElementById('targetDate').value,
        durationMinutes: parseInt(document.getElementById('duration').value),
        preferredStartTime: document.getElementById('preferredStart').value,
        preferredEndTime: document.getElementById('preferredEnd').value
    };

    if (!groupId) {
        showAlert('그룹을 선택해주세요.', 'warning');
        return;
    }

    try {
        showLoading('일정 분석 중...');
        const response = await apiRequest(`/groups/${groupId}/analysis/recommend`, {
            method: 'POST',
            body: JSON.stringify(analysisData)
        });
        
        displayAnalysisResults(response.data);
        document.getElementById('analysisResults').style.display = 'block';
        showAlert('일정 분석이 완료되었습니다.', 'success');
    } catch (error) {
        console.error('Failed to analyze schedule:', error);
    } finally {
        hideLoading();
    }
}

function displayAnalysisResults(data) {
    // Update summary
    document.getElementById('resultGroupName').textContent = data.groupName;
    document.getElementById('resultDate').textContent = formatDateKorean(data.targetDate);
    document.getElementById('resultTotalMembers').textContent = data.totalMembers;
    
    // Display recommendations
    const recommendationsList = document.getElementById('recommendationsList');
    if (data.recommendations && data.recommendations.length > 0) {
        recommendationsList.innerHTML = data.recommendations.map(rec => `
            <div class="recommendation-item">
                <div class="recommendation-time">
                    ${rec.formattedTimeRange}
                </div>
                <div class="recommendation-availability">
                    <div class="availability-score">${Math.round(rec.availabilityScore * 100)}%</div>
                    <div class="availability-text">${rec.availabilityText}</div>
                </div>
            </div>
        `).join('');
    } else {
        recommendationsList.innerHTML = '<p>추천할 수 있는 시간대가 없습니다.</p>';
    }
}

// Modal Functions
function openCreateGroupModal() {
    document.getElementById('createGroupModal').style.display = 'flex';
    document.getElementById('createGroupModal').classList.add('show');
}

function openCreateScheduleModal() {
    document.getElementById('createScheduleModal').style.display = 'flex';
    document.getElementById('createScheduleModal').classList.add('show');
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.style.display = 'none';
    modal.classList.remove('show');
}

// Utility Functions
function showAlert(message, type = 'info') {
    // Create alert element
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.innerHTML = `
        <i class="fas fa-${getAlertIcon(type)}"></i>
        <span>${message}</span>
    `;
    
    // Add to page
    const container = document.querySelector('.container');
    if (container) {
        container.insertBefore(alert, container.firstChild);
        
        // Remove after 5 seconds
        setTimeout(() => {
            alert.remove();
        }, 5000);
    }
}

function getAlertIcon(type) {
    const icons = {
        success: 'check-circle',
        error: 'exclamation-circle',
        warning: 'exclamation-triangle',
        info: 'info-circle'
    };
    return icons[type] || 'info-circle';
}

function showLoading(message = '처리 중...') {
    // This could be implemented as a loading overlay
    console.log('Loading:', message);
}

function hideLoading() {
    // Hide loading overlay
    console.log('Loading complete');
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR');
}

function formatDateKorean(dateString) {
    const date = new Date(dateString);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const dayOfWeek = ['일', '월', '화', '수', '목', '금', '토'][date.getDay()];
    return `${month}월 ${day}일 (${dayOfWeek})`;
}

function getScheduleTypeText(type) {
    const types = {
        BUSY: '바쁨',
        AVAILABLE: '가능',
        PREFERRED: '선호'
    };
    return types[type] || type;
}

function getTodayDate() {
    return new Date().toISOString().split('T')[0];
}

function getNextMonthDate() {
    const date = new Date();
    date.setMonth(date.getMonth() + 1);
    return date.toISOString().split('T')[0];
}

// Export functions for global access
window.openCreateGroupModal = openCreateGroupModal;
window.openCreateScheduleModal = openCreateScheduleModal;
window.closeModal = closeModal;
window.logout = logout;
window.viewGroup = viewGroup;
window.editGroup = editGroup;
window.editSchedule = editSchedule;
window.deleteSchedule = deleteSchedule;
window.filterSchedules = filterSchedules;