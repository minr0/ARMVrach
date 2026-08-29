from django.shortcuts import render, get_object_or_404, redirect
from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.decorators import login_required, user_passes_test
from django.contrib.auth.models import User
from django.utils import timezone
from django.contrib import messages

from rest_framework import viewsets, status
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework.authentication import TokenAuthentication
from rest_framework.authtoken.models import Token
from rest_framework.views import APIView

from .models import PatientProfile, Appointment, TestResult, HealthLog, ChatMessage
from .serializers import (
    PatientProfileSerializer, AppointmentSerializer,
    TestResultSerializer, HealthLogSerializer, ChatMessageSerializer,
    IINLoginSerializer
)

# ─────────────────────────────────────────────
#  Helpers
# ─────────────────────────────────────────────

def is_doctor(user):
    return user.is_authenticated and user.is_staff


# ─────────────────────────────────────────────
#  Web Auth
# ─────────────────────────────────────────────

def web_login(request):
    if request.method == 'POST':
        username = request.POST.get('username')
        password = request.POST.get('password')
        user = authenticate(request, username=username, password=password)
        if user and user.is_staff:
            login(request, user)
            return redirect('dashboard')
        messages.error(request, 'Неверные данные или доступ запрещён.')
    return render(request, 'arm_project/login.html')


def web_logout(request):
    logout(request)
    return redirect('login')


# ─────────────────────────────────────────────
#  Web Views (Doctor)
# ─────────────────────────────────────────────

@login_required(login_url='login')
@user_passes_test(is_doctor, login_url='login')
def dashboard(request):
    today = timezone.now().date()
    appointments = Appointment.objects.filter(
        datetime__date=today
    ).select_related('patient').order_by('datetime')
    unread_count = ChatMessage.objects.filter(is_read=False).exclude(sender__is_staff=True).count()
    return render(request, 'arm_project/dashboard.html', {
        'appointments': appointments,
        'today': today,
        'unread_count': unread_count,
    })


@login_required(login_url='login')
@user_passes_test(is_doctor, login_url='login')
def patient_list(request):
    profiles = PatientProfile.objects.select_related('user').order_by('user__last_name')
    return render(request, 'arm_project/patient_list.html', {'profiles': profiles})


@login_required(login_url='login')
@user_passes_test(is_doctor, login_url='login')
def patient_detail(request, user_id):
    patient = get_object_or_404(User, pk=user_id, is_staff=False)
    profile = getattr(patient, 'patient_profile', None)
    appointments = Appointment.objects.filter(patient=patient).order_by('-datetime')
    test_results = TestResult.objects.filter(patient=patient).order_by('-date')
    health_logs = HealthLog.objects.filter(patient=patient).order_by('date')
    messages_qs = ChatMessage.objects.filter(patient=patient).order_by('created_at')

    # Mark incoming messages as read
    ChatMessage.objects.filter(patient=patient, is_read=False).exclude(sender__is_staff=True).update(is_read=True)

    # Prepare chart data
    chart_dates = [log.date.strftime('%d.%m') for log in health_logs]
    chart_weight = [float(log.weight) if log.weight else None for log in health_logs]
    chart_sugar = [float(log.blood_sugar) if log.blood_sugar else None for log in health_logs]
    chart_systolic = [log.systolic for log in health_logs]

    # Handle POST: add test result
    if request.method == 'POST':
        action = request.POST.get('action')
        if action == 'add_test':
            TestResult.objects.create(
                patient=patient,
                indicator=request.POST.get('indicator'),
                value=request.POST.get('value'),
                unit=request.POST.get('unit', ''),
                norm=request.POST.get('norm', ''),
                date=request.POST.get('date'),
                added_by=request.user,
            )
            messages.success(request, 'Анализ добавлен.')
        elif action == 'send_message':
            text = request.POST.get('text', '').strip()
            if text:
                ChatMessage.objects.create(patient=patient, sender=request.user, text=text)
                messages.success(request, 'Сообщение отправлено.')
        return redirect('patient_detail', user_id=user_id)

    return render(request, 'arm_project/patient_detail.html', {
        'patient': patient,
        'profile': profile,
        'appointments': appointments,
        'test_results': test_results,
        'health_logs': health_logs,
        'messages_qs': messages_qs,
        'chart_dates': chart_dates,
        'chart_weight': chart_weight,
        'chart_sugar': chart_sugar,
        'chart_systolic': chart_systolic,
    })


@login_required(login_url='login')
@user_passes_test(is_doctor, login_url='login')
def appointment_update(request, pk):
    appointment = get_object_or_404(Appointment, pk=pk)
    if request.method == 'POST':
        appointment.status = request.POST.get('status', appointment.status)
        appointment.doctor_note = request.POST.get('doctor_note', appointment.doctor_note)
        appointment.save()
        messages.success(request, 'Запись обновлена.')
    return redirect('patient_detail', user_id=appointment.patient_id)


@login_required(login_url='login')
@user_passes_test(is_doctor, login_url='login')
def chat_list(request):
    """List all patients who have sent messages."""
    patient_ids = ChatMessage.objects.values_list('patient_id', flat=True).distinct()
    patients = User.objects.filter(id__in=patient_ids, is_staff=False)
    unread_per_patient = {}
    for p in patients:
        unread_per_patient[p.id] = ChatMessage.objects.filter(
            patient=p, is_read=False
        ).exclude(sender__is_staff=True).count()
    return render(request, 'arm_project/chat_list.html', {
        'patients': patients,
        'unread_per_patient': unread_per_patient,
    })


# ─────────────────────────────────────────────
#  API Views
# ─────────────────────────────────────────────

class AuthAPIView(APIView):
    """Login by IIN — emulates eGov authentication."""
    permission_classes = [AllowAny]

    def post(self, request):
        serializer = IINLoginSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        iin = serializer.validated_data['iin']
        password = serializer.validated_data['password']

        try:
            profile = PatientProfile.objects.select_related('user').get(iin=iin)
        except PatientProfile.DoesNotExist:
            return Response({'error': 'Пациент с данным ИИН не найден.'}, status=status.HTTP_404_NOT_FOUND)

        user = authenticate(username=profile.user.username, password=password)
        if not user:
            return Response({'error': 'Неверный пароль.'}, status=status.HTTP_401_UNAUTHORIZED)

        token, _ = Token.objects.get_or_create(user=user)
        return Response({
            'token': token.key,
            'user_id': user.id,
            'full_name': user.get_full_name(),
            'iin': iin,
        })


class AppointmentViewSet(viewsets.ModelViewSet):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]
    serializer_class = AppointmentSerializer

    def get_queryset(self):
        return Appointment.objects.filter(patient=self.request.user).order_by('-datetime')


class TestResultViewSet(viewsets.ReadOnlyModelViewSet):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]
    serializer_class = TestResultSerializer

    def get_queryset(self):
        return TestResult.objects.filter(patient=self.request.user).order_by('-date')


class HealthLogViewSet(viewsets.ModelViewSet):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]
    serializer_class = HealthLogSerializer

    def get_queryset(self):
        return HealthLog.objects.filter(patient=self.request.user).order_by('-date')


class ChatViewSet(viewsets.ModelViewSet):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]
    serializer_class = ChatMessageSerializer
    http_method_names = ['get', 'post', 'head', 'options']

    def get_queryset(self):
        return ChatMessage.objects.filter(patient=self.request.user).order_by('created_at')

    def perform_create(self, serializer):
        serializer.save(patient=self.request.user, sender=self.request.user)
