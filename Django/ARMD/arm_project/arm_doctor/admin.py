from django.contrib import admin
from django.contrib.auth.admin import UserAdmin as BaseUserAdmin
from django.contrib.auth.models import User
from .models import PatientProfile, Appointment, TestResult, HealthLog, ChatMessage

# Этот класс позволяет редактировать ИИН прямо внутри страницы пользователя
class PatientProfileInline(admin.StackedInline):
    model = PatientProfile
    can_delete = False
    verbose_name_plural = 'Профиль пациента (ИИН и данные)'

# Настраиваем стандартную модель User
class UserAdmin(BaseUserAdmin):
    inlines = (PatientProfileInline, )
    list_display = ('username', 'get_iin', 'first_name', 'last_name', 'is_staff')

    def get_iin(self, obj):
        return obj.patientprofile.iin if hasattr(obj, 'patientprofile') else '-'
    get_iin.short_description = 'ИИН'

# Перерегистрируем User
admin.site.unregister(User)
admin.site.register(User, UserAdmin)

# Регистрируем остальные модели, чтобы они тоже были видны
admin.site.register(Appointment)
admin.site.register(TestResult)
admin.site.register(HealthLog)
admin.site.register(ChatMessage)