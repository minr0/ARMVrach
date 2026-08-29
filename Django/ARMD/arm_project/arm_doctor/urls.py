from django.urls import path, include
from rest_framework.routers import DefaultRouter
from . import views

# API Router
router = DefaultRouter()
router.register(r'appointments', views.AppointmentViewSet, basename='appointment')
router.register(r'test-results', views.TestResultViewSet, basename='testresult')
router.register(r'health-logs', views.HealthLogViewSet, basename='healthlog')
router.register(r'chat', views.ChatViewSet, basename='chat')

urlpatterns = [
    # ── Web (Doctor) ──────────────────────────────
    path('', views.dashboard, name='dashboard'),
    path('login/', views.web_login, name='login'),
    path('logout/', views.web_logout, name='logout'),
    path('patients/', views.patient_list, name='patient_list'),
    path('patients/<int:user_id>/', views.patient_detail, name='patient_detail'),
    path('appointments/<int:pk>/update/', views.appointment_update, name='appointment_update'),
    path('chat/', views.chat_list, name='chat_list'),

    # ── API ───────────────────────────────────────
    path('api/auth/login/', views.AuthAPIView.as_view(), name='api_login'),
    path('api/', include(router.urls)),
]
